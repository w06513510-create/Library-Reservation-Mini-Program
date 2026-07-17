package org.dromara.pay.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.pay.domain.AppFundFlow;
import org.dromara.pay.domain.AppWallet;
import org.dromara.pay.domain.vo.AppFundFlowVo;
import org.dromara.pay.domain.vo.AppWalletVo;
import org.dromara.pay.mapper.AppFundFlowMapper;
import org.dromara.pay.mapper.AppWalletMapper;
import org.dromara.pay.service.IWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * 钱包对平引擎实现（动钱唯一入口）。
 * <p>所有 DB 操作包 {@link TenantHelper#ignore}：严格按 {@code user_id}(雪花全局唯一)归属，
 * 与租户上下文解耦（notify 回调无登录=无租户上下文也能正确动钱），并使自写 CAS 的 where 不被租户插件改写。
 * <p>方向语义：流水记录对可用余额 {@code balance} 的影响。recharge/unfreeze→入(1)，deduct/freeze→出(2)。
 * 对平不变式：{@code balance == Σ入 − Σ出}。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class WalletServiceImpl implements IWalletService {

    /** 方向: 入 / 出 */
    private static final int DIRECTION_IN = 1;
    private static final int DIRECTION_OUT = 2;

    private final AppWalletMapper baseMapper;
    private final AppFundFlowMapper fundFlowMapper;

    @Override
    public AppWallet getOrCreate(Long userId) {
        return TenantHelper.ignore(() -> {
            AppWallet w = baseMapper.selectOne(
                Wrappers.<AppWallet>lambdaQuery().eq(AppWallet::getUserId, userId));
            if (w == null) {
                w = new AppWallet();
                w.setUserId(userId);
                w.setBalance(BigDecimal.ZERO);
                w.setFrozen(BigDecimal.ZERO);
                w.setTotalRecharge(BigDecimal.ZERO);
                w.setVersion(0);
                baseMapper.insert(w);
            }
            return w;
        });
    }

    @Override
    public AppWalletVo getWalletVo(Long userId) {
        AppWallet w = getOrCreate(userId);
        AppWalletVo vo = new AppWalletVo();
        vo.setId(w.getId());
        vo.setUserId(w.getUserId());
        vo.setBalance(w.getBalance());
        vo.setFrozen(w.getFrozen());
        vo.setTotalRecharge(w.getTotalRecharge());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark) {
        mutate(userId, amount, DIRECTION_IN,
            () -> baseMapper.increaseForRecharge(userId, amount), "充值入账失败",
            bizType, bizNo, idempotentNo, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark) {
        mutate(userId, amount, DIRECTION_OUT,
            () -> baseMapper.deductBalance(userId, amount), "可用余额不足或并发冲突",
            bizType, bizNo, idempotentNo, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark) {
        mutate(userId, amount, DIRECTION_OUT,
            () -> baseMapper.freezeBalance(userId, amount), "可用余额不足或并发冲突",
            bizType, bizNo, idempotentNo, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark) {
        mutate(userId, amount, DIRECTION_IN,
            () -> baseMapper.unfreezeBalance(userId, amount), "冻结余额不足或并发冲突",
            bizType, bizNo, idempotentNo, remark);
    }

    /**
     * 动钱统一流程：校验 → 确保钱包 → 幂等预检查 → CAS 条件更新 → 回读 balance_after → 写 append-only 流水。
     * 全程 {@link TenantHelper#ignore}，按 user_id 归属。
     *
     * @param direction 本次对可用余额的影响方向(1入/2出)
     * @param casOp     执行钱包 CAS 条件更新, 返回影响行数(0=余额不足/并发失败)
     */
    private void mutate(Long userId, BigDecimal amount, int direction,
                        Supplier<Integer> casOp, String insufficientMsg,
                        String bizType, String bizNo, String idempotentNo, String remark) {
        requirePositive(amount);
        if (StringUtils.isBlank(idempotentNo)) {
            throw new ServiceException("幂等键 idempotentNo 不能为空");
        }
        TenantHelper.ignore(() -> {
            getOrCreate(userId);
            // 幂等预检查: 覆盖 notify + 查单轮询等顺序重试 (唯一键做并发硬兜底)
            Long dup = fundFlowMapper.selectCount(
                Wrappers.<AppFundFlow>lambdaQuery().eq(AppFundFlow::getIdempotentNo, idempotentNo));
            if (dup != null && dup > 0) {
                return;
            }
            int rows = casOp.get();
            if (rows <= 0) {
                throw new ServiceException(insufficientMsg);
            }
            AppWallet w = baseMapper.selectOne(
                Wrappers.<AppWallet>lambdaQuery().eq(AppWallet::getUserId, userId));
            writeFlow(userId, direction, amount, w.getBalance(), bizType, bizNo, idempotentNo, remark);
        });
    }

    private void writeFlow(Long userId, int direction, BigDecimal amount, BigDecimal balanceAfter,
                           String bizType, String bizNo, String idempotentNo, String remark) {
        AppFundFlow f = new AppFundFlow();
        f.setUserId(userId);
        f.setDirection(direction);
        f.setAmount(amount);
        f.setBalanceAfter(balanceAfter);
        f.setBizType(bizType);
        f.setBizNo(bizNo);
        f.setIdempotentNo(idempotentNo);
        f.setRemark(remark);
        f.setCreateTime(new Date());
        // 唯一键 uk_flow_idem 做并发幂等硬兜底: 重复 idempotentNo 插入冲突 → 整事务回滚 → 撤销上面的 CAS 变动
        fundFlowMapper.insert(f);
    }

    @Override
    public BigDecimal checkInvariant(Long userId) {
        return TenantHelper.ignore(() -> {
            AppWallet w = baseMapper.selectOne(
                Wrappers.<AppWallet>lambdaQuery().eq(AppWallet::getUserId, userId));
            BigDecimal balance = w == null ? BigDecimal.ZERO : nz(w.getBalance());
            List<AppFundFlow> flows = fundFlowMapper.selectList(
                Wrappers.<AppFundFlow>lambdaQuery().eq(AppFundFlow::getUserId, userId));
            BigDecimal in = BigDecimal.ZERO;
            BigDecimal out = BigDecimal.ZERO;
            for (AppFundFlow f : flows) {
                if (f.getDirection() != null && f.getDirection() == DIRECTION_IN) {
                    in = in.add(nz(f.getAmount()));
                } else {
                    out = out.add(nz(f.getAmount()));
                }
            }
            return balance.subtract(in.subtract(out));
        });
    }

    @Override
    public TableDataInfo<AppFundFlowVo> pageFlows(Long userId, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> {
            Page<AppFundFlowVo> page = fundFlowMapper.selectVoPage(pageQuery.build(),
                Wrappers.<AppFundFlow>lambdaQuery()
                    .eq(AppFundFlow::getUserId, userId)
                    .orderByDesc(AppFundFlow::getId));
            return TableDataInfo.build(page);
        });
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ServiceException("金额必须大于0");
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

}

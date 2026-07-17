package org.dromara.pay.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.pay.domain.AppWallet;
import org.dromara.pay.domain.vo.AppFundFlowVo;
import org.dromara.pay.domain.vo.AppWalletVo;

import java.math.BigDecimal;

/**
 * 钱包对平引擎（纯技术, 与业务无关）——所有动钱路径的唯一入口。
 * <p>四个动钱方法均满足：幂等({@code idempotentNo})、CAS 防超扣、append-only 流水、同事务。
 * 业务模块调 {@code deduct/freeze/unfreeze} 时须自带业务幂等键。
 *
 * @author ruoyi-template
 */
public interface IWalletService {

    /**
     * 取用户钱包；不存在则初始化一行(三层余额 0)。
     */
    AppWallet getOrCreate(Long userId);

    /**
     * 我的钱包视图。
     */
    AppWalletVo getWalletVo(Long userId);

    /**
     * 充值入账：可用 += amount，累计充值 += amount，记入账流水(direction=1)。
     *
     * @param idempotentNo 幂等键(充值场景通常传 out_trade_no)
     */
    void recharge(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark);

    /**
     * 扣款出账：可用 -= amount(CAS 防超扣)，记出账流水(direction=2)。
     */
    void deduct(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark);

    /**
     * 冻结：可用 -= amount 且 冻结 += amount(CAS 防超扣)，记出账流水(direction=2, 可用减少)。
     */
    void freeze(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark);

    /**
     * 解冻：冻结 -= amount 且 可用 += amount(CAS 防超扣)，记入账流水(direction=1, 可用增加)。
     */
    void unfreeze(Long userId, BigDecimal amount, String bizType, String bizNo, String idempotentNo, String remark);

    /**
     * 对平自检：返回差额 {@code diff = balance − (Σ入 − Σ出)}，正常恒为 0。
     */
    BigDecimal checkInvariant(Long userId);

    /**
     * 我的资金流水分页。
     */
    TableDataInfo<AppFundFlowVo> pageFlows(Long userId, PageQuery pageQuery);

}

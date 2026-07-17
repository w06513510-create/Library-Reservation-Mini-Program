package org.dromara.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.biz.constant.AuditStatus;
import org.dromara.biz.domain.BizAudit;
import org.dromara.biz.domain.BizAuditLog;
import org.dromara.biz.domain.bo.BizAuditBo;
import org.dromara.biz.domain.vo.BizAuditVo;
import org.dromara.biz.mapper.BizAuditLogMapper;
import org.dromara.biz.mapper.BizAuditMapper;
import org.dromara.biz.service.AuditCallback;
import org.dromara.biz.service.IBizAuditService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用审核单 Service 实现（三态审核状态机 + 可选流转日志 + 回调钩子）
 * <p>核心范式（提炼自宿舍 RepairServiceImpl 的 getRepair/writeLog + 跑腿审核的 status 守卫）：
 * <ol>
 *   <li>每个流转方法：① 加载并校验存在 → ② 守卫当前状态（非法流转抛异常）→ ③ 改状态 updateById → ④ append 流转日志 → ⑤ 触发回调</li>
 *   <li>审核人 auditBy / operatorUserId 一律取 {@link LoginHelper#getUserId()}，不信前端</li>
 * </ol>
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class BizAuditServiceImpl implements IBizAuditService {

    private final BizAuditMapper baseMapper;
    private final BizAuditLogMapper auditLogMapper;
    /** 所有回调实现按 bizType 归集；无实现时为空集合，不影响主流程 */
    private final List<AuditCallback> auditCallbacks;

    /** bizType -> 回调（启动时建索引） */
    private Map<String, AuditCallback> callbackMap() {
        return auditCallbacks.stream()
            .collect(Collectors.toMap(AuditCallback::bizType, Function.identity(), (a, b) -> a));
    }

    @Override
    public TableDataInfo<BizAuditVo> queryPageList(BizAuditBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizAudit> lqw = Wrappers.<BizAudit>lambdaQuery()
            .eq(StringUtils.isNotBlank(bo.getBizType()), BizAudit::getBizType, bo.getBizType())
            .eq(bo.getStatus() != null, BizAudit::getStatus, bo.getStatus())
            .eq(bo.getBizId() != null, BizAudit::getBizId, bo.getBizId())
            .orderByDesc(BizAudit::getId);
        Page<BizAuditVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public BizAuditVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public Long submit(BizAuditBo bo) {
        BizAudit audit = MapstructUtils.convert(bo, BizAudit.class);
        audit.setApplyUserId(LoginHelper.getUserId());   // 申请人取登录态
        audit.setStatus(AuditStatus.PENDING);
        baseMapper.insert(audit);
        writeLog(audit.getId(), "submit", null, AuditStatus.PENDING, null);
        return audit.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(Long id) {
        BizAudit audit = getAudit(id);
        guardPending(audit);                             // 非法流转守卫
        Integer from = audit.getStatus();
        audit.setStatus(AuditStatus.PASS);
        audit.setAuditBy(LoginHelper.getUserId());
        audit.setAuditTime(new Date());
        baseMapper.updateById(audit);
        writeLog(id, "approve", from, AuditStatus.PASS, null);
        // 回调钩子：授角色/改标志/发通知等业务副作用（同事务）
        AuditCallback cb = callbackMap().get(audit.getBizType());
        if (cb != null) {
            cb.afterPass(audit);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String rejectReason) {
        if (StringUtils.isBlank(rejectReason)) {
            throw new ServiceException("驳回原因不能为空");
        }
        BizAudit audit = getAudit(id);
        guardPending(audit);
        Integer from = audit.getStatus();
        audit.setStatus(AuditStatus.REJECT);
        audit.setRejectReason(rejectReason);
        audit.setAuditBy(LoginHelper.getUserId());
        audit.setAuditTime(new Date());
        baseMapper.updateById(audit);
        writeLog(id, "reject", from, AuditStatus.REJECT, rejectReason);
        AuditCallback cb = callbackMap().get(audit.getBizType());
        if (cb != null) {
            cb.afterReject(audit);
        }
        return true;
    }

    @Override
    public TableDataInfo<BizAuditLog> queryLogPage(Long auditId, PageQuery pageQuery) {
        LambdaQueryWrapper<BizAuditLog> lqw = Wrappers.<BizAuditLog>lambdaQuery()
            .eq(auditId != null, BizAuditLog::getAuditId, auditId)
            .orderByDesc(BizAuditLog::getId);
        Page<BizAuditLog> page = auditLogMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    // ============ 私有辅助（模板核心，直接复用） ============

    /** 取审核单并校验存在 */
    private BizAudit getAudit(Long id) {
        if (id == null) {
            throw new ServiceException("审核单ID不能为空");
        }
        BizAudit audit = baseMapper.selectById(id);
        if (audit == null) {
            throw new ServiceException("审核单不存在");
        }
        return audit;
    }

    /** 守卫：仅"待审"可流转，防重复审核 */
    private void guardPending(BizAudit audit) {
        if (audit.getStatus() == null || audit.getStatus() != AuditStatus.PENDING) {
            throw new ServiceException("该申请已审核，请勿重复操作");
        }
    }

    /**
     * 写一条流转轨迹（同事务）。仅在建了 biz_audit_log 表时有意义；
     * 简单三态审核若不需要留痕，可删掉本方法及其调用。
     */
    private void writeLog(Long auditId, String action, Integer fromStatus, Integer toStatus, String remark) {
        BizAuditLog log = new BizAuditLog();
        log.setAuditId(auditId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorUserId(LoginHelper.getUserId());
        log.setRemark(remark);
        log.setActionTime(new Date());
        auditLogMapper.insert(log);
    }

}

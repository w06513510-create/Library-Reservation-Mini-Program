package org.dromara.biz.service;

import org.dromara.biz.domain.BizAudit;

/**
 * 审核回调钩子（可选扩展点）
 * <p>提炼自跑腿的差异化"审核通过后副作用"：实名审核通过→改 profile.realname_status；
 * 跑腿员申请通过→profile.is_runner=1 且授角色；提现驳回→退款。这些副作用各不相同，
 * 抽成回调，让审核状态机本身保持纯粹。
 *
 * <p><b>用法：</b>每种 bizType 各写一个 {@code @Component} 实现本接口，返回自己负责的 bizType，
 * 在 afterPass/afterReject 里做副作用（授角色、改标志、退款、发通知…）。
 * {@link IBizAuditService} 实现类会注入 {@code List<AuditCallback>}，按 bizType 匹配后回调（同一事务内）。
 * 若某 bizType 无副作用，不写实现即可（钩子可为空）。
 *
 * @author ruoyi-template
 */
public interface AuditCallback {

    /** 本回调负责的业务类型，与 biz_audit.biz_type 对应 */
    String bizType();

    /** 审核通过后的副作用（默认空实现） */
    default void afterPass(BizAudit audit) {
    }

    /** 审核驳回后的副作用（默认空实现） */
    default void afterReject(BizAudit audit) {
    }

}

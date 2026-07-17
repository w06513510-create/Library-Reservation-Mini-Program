package org.dromara.biz.constant;

/**
 * 审核状态常量（通用三态：待审 / 通过 / 驳回）
 * <p>提炼自跑腿 realname/runnerApply 审核的私有常量 STATUS_PENDING/PASS/REJECT，抽为公共类。
 * <p>多状态工单（受理/处理中/挂起/完工…）请在本类补充自己的状态码，或另建业务专属常量类。
 *
 * @author ruoyi-template
 */
public interface AuditStatus {

    /** 待审核 */
    int PENDING = 0;

    /** 审核通过 */
    int PASS = 1;

    /** 审核驳回 */
    int REJECT = 2;

}

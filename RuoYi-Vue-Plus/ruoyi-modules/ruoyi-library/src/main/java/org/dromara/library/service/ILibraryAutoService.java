package org.dromara.library.service;

/**
 * 定时任务自动处置Service（亮点②：超时释放 / 违约判定 / 信用恢复）
 * 每个方法独立事务，由定时任务跨 bean 调用（避免自调用事务失效）。
 *
 * @author library
 */
public interface ILibraryAutoService {

    /** 超时未签到释放（爽约） */
    int scanNoShow();

    /** 暂离超时释放 */
    int scanAwayTimeout();

    /** 到期未签退释放 */
    int scanExpired();

    /** 图书逾期判定 */
    int scanOverdueBooks();

    /** 预约架超期释放 */
    int scanHoldExpired();

    /** 信用分时间衰减恢复（无违约满周期 +分） */
    int scanCreditDecay();

}

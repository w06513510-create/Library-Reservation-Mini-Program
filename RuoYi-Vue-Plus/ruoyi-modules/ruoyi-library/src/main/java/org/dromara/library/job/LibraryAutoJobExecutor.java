package org.dromara.library.job;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import lombok.RequiredArgsConstructor;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.library.service.IBlacklistService;
import org.dromara.library.service.ILibraryAutoService;
import org.springframework.stereotype.Component;

/**
 * 图书馆定时任务执行器（SnailJob，亮点②自动化）
 * 一个任务串起 9 类自动处置：超时未签到/暂离/到期释放、图书逾期、预约架超期、信用衰减、黑名单到期解除。
 * 无登录态，整段包 TenantHelper.dynamic("000000")；逐个跨 bean 调 service 的 @Transactional 方法。
 *
 * SnailJob 后台任务的 执行器名称 填：libraryAutoJob
 *
 * @author library
 */
@Component
@RequiredArgsConstructor
@JobExecutor(name = "libraryAutoJob")
public class LibraryAutoJobExecutor {

    private final ILibraryAutoService autoService;
    private final IBlacklistService blacklistService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        int[] c = new int[8];
        TenantHelper.dynamic("000000", () -> {
            c[0] = autoService.scanNoShow();
            c[1] = autoService.scanAwayTimeout();
            c[2] = autoService.scanExpired();
            c[3] = autoService.scanOverdueBooks();
            c[4] = autoService.scanHoldExpired();
            c[5] = autoService.scanCreditDecay();
            c[6] = blacklistService.autoReleaseExpired();
            c[7] = autoService.scanSuperviseTimeout();
        });
        String msg = String.format("爽约释放%d 暂离超时%d 到期未签退%d 图书逾期%d 预约架超期%d 信用衰减%d 黑名单解除%d 监督超时%d",
            c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
        SnailJobLog.LOCAL.info("libraryAutoJob 执行完成：{}", msg);
        SnailJobLog.REMOTE.info("libraryAutoJob 执行完成：{}", msg);
        return ExecuteResult.success(msg);
    }
}

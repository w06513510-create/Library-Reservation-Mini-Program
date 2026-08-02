package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.service.IBlacklistService;
import org.dromara.library.service.ILibraryAutoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务手动触发Controller
 * SnailJob(libraryAutoJob) 按周期自动跑；本接口供管理员手动强制跑一轮（运维/演示用）。
 *
 * @author library
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/task")
public class LibraryTaskController extends BaseController {

    private final ILibraryAutoService autoService;
    private final IBlacklistService blacklistService;

    /** 手动执行一轮自动处置 */
    @SaCheckPermission("library:task:run")
    @Log(title = "定时任务手动触发", businessType = BusinessType.OTHER)
    @PostMapping("/runAuto")
    public R<String> runAuto() {
        int a = autoService.scanNoShow();
        int b = autoService.scanAwayTimeout();
        int c = autoService.scanExpired();
        int d = autoService.scanOverdueBooks();
        int e = autoService.scanHoldExpired();
        int f = autoService.scanCreditDecay();
        int g = blacklistService.autoReleaseExpired();
        String msg = String.format("爽约释放%d 暂离超时%d 到期未签退%d 图书逾期%d 预约架超期%d 信用衰减%d 黑名单解除%d",
            a, b, c, d, e, f, g);
        return R.ok(msg);
    }

}

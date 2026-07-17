package org.dromara.message.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.vo.AppNotificationVo;
import org.dromara.message.service.INotificationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * C端(小程序) 站内通知 Controller。
 * <p>{@code @SaCheckLogin} 校 C端登录 + {@link AppLoginHelper#getUserId()} 强制归属，只能读写本人通知。
 *
 * @author ruoyi-template
 */
@Validated
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/notice")
public class AppNoticeController {

    private final INotificationService notificationService;

    /** 我的通知分页（按创建时间倒序） */
    @GetMapping("/list")
    public TableDataInfo<AppNotificationVo> list(PageQuery pageQuery) {
        return notificationService.pageOfUser(AppLoginHelper.getUserId(), pageQuery);
    }

    /** 我的未读通知数 */
    @GetMapping("/unreadCount")
    public R<Long> unreadCount() {
        return R.ok(notificationService.unreadCount(AppLoginHelper.getUserId()));
    }

    /** 标记单条通知已读（越权无效） */
    @PutMapping("/read/{id}")
    public R<Void> read(@NotNull(message = "通知ID不能为空") @PathVariable Long id) {
        notificationService.markRead(AppLoginHelper.getUserId(), id);
        return R.ok();
    }

    /** 我的全部未读置已读 */
    @PutMapping("/readAll")
    public R<Void> readAll() {
        notificationService.markAllRead(AppLoginHelper.getUserId());
        return R.ok();
    }

}

package org.dromara.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.message.domain.bo.NotificationQueryBo;
import org.dromara.message.domain.bo.NotificationSendBo;
import org.dromara.message.domain.vo.AppNotificationVo;
import org.dromara.message.service.INotificationService;
import org.dromara.message.utils.NotificationHelper;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 站内通知 Controller。
 * <p>权限走 {@code @SaCheckPermission}；内置 admin(*:*:*) 可直接访问，普通角色需在菜单管理配
 * {@code message:notification:*} 权限（本模块未附菜单/权限 SQL）。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/notification")
public class NotificationController extends BaseController {

    private final INotificationService notificationService;

    /** 通知分页列表（可按接收人/业务类型/是否已读/标题过滤） */
    @SaCheckPermission("message:notification:list")
    @GetMapping("/list")
    public TableDataInfo<AppNotificationVo> list(NotificationQueryBo bo, PageQuery pageQuery) {
        return notificationService.adminPage(bo, pageQuery);
    }

    /** 发送通知给指定 C端用户 */
    @SaCheckPermission("message:notification:send")
    @Log(title = "站内通知", businessType = BusinessType.INSERT)
    @PostMapping("/send")
    public R<Long> send(@Valid @RequestBody NotificationSendBo bo) {
        Long id = NotificationHelper.send(bo.getReceiverId(), bo.getTitle(), bo.getContent(), bo.getBizType(), bo.getBizId());
        return R.ok(id);
    }

}

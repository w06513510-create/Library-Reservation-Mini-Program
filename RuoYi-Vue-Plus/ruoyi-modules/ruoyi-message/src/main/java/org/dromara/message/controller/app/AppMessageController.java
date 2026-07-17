package org.dromara.message.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.bo.MessageSendBo;
import org.dromara.message.domain.vo.AppMessageVo;
import org.dromara.message.domain.vo.ConversationVo;
import org.dromara.message.service.IMessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端(小程序) 站内私信 Controller。
 * <p>{@code @SaCheckLogin} + {@link AppLoginHelper#getUserId()} 强制归属：发送人恒为当前登录用户，
 * 会话/消息只能读写与自己相关的记录。
 *
 * @author ruoyi-template
 */
@Validated
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/message")
public class AppMessageController {

    private final IMessageService messageService;

    /** 我的会话列表（按对端聚合，最近在前，带对端昵称/头像与未读数） */
    @GetMapping("/conversations")
    public R<List<ConversationVo>> conversations() {
        return R.ok(messageService.conversations(AppLoginHelper.getUserId()));
    }

    /** 与某对端的消息分页（倒序：page1=最新一页），并把对端发我的未读置已读 */
    @GetMapping("/chat")
    public TableDataInfo<AppMessageVo> chat(@NotNull(message = "对端用户不能为空") Long peerId, PageQuery pageQuery) {
        return messageService.chatPage(AppLoginHelper.getUserId(), peerId, pageQuery);
    }

    /** 发送私信（发送人取当前登录用户，禁给自己发） */
    @PostMapping("/send")
    public R<Long> send(@Valid @RequestBody MessageSendBo bo) {
        return R.ok(messageService.send(AppLoginHelper.getUserId(), bo.getToId(), bo.getContent()));
    }

    /** 我的未读私信总数 */
    @GetMapping("/unreadCount")
    public R<Long> unreadCount() {
        return R.ok(messageService.unreadCount(AppLoginHelper.getUserId()));
    }

}

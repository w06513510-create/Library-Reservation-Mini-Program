package org.dromara.message.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.vo.AppMessageVo;
import org.dromara.message.domain.vo.ConversationVo;

import java.util.List;

/**
 * 站内私信 Service。
 * <p>所有方法均以当前用户 userId 作强制归属条件，只能读写与自己相关的会话/消息。
 *
 * @author ruoyi-template
 */
public interface IMessageService {

    /**
     * 发送私信。
     *
     * @param fromId  发送人（当前登录用户）
     * @param toId    接收人
     * @param content 内容
     * @return 新消息ID
     */
    Long send(Long fromId, Long toId, String content);

    /** 我的会话列表（按对端聚合，最近在前，带对端昵称/头像与未读数） */
    List<ConversationVo> conversations(Long userId);

    /**
     * 与某对端的消息分页（倒序取，页内翻正序返回）；同时把对端发我的未读置为已读。
     *
     * @param userId 当前登录用户
     * @param peerId 对端用户
     */
    TableDataInfo<AppMessageVo> chatPage(Long userId, Long peerId, PageQuery pageQuery);

    /** 我的未读私信总数（to_id=userId 且未读） */
    long unreadCount(Long userId);

}

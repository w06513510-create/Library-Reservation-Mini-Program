package org.dromara.message.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.app.domain.AppUser;
import org.dromara.app.mapper.AppUserMapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.AppMessage;
import org.dromara.message.domain.vo.AppMessageVo;
import org.dromara.message.domain.vo.ConversationVo;
import org.dromara.message.mapper.AppMessageMapper;
import org.dromara.message.service.IMessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内私信 Service 实现。会话按无序对 (from_id,to_id) 归并。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements IMessageService {

    private static final int UNREAD = 0, READ = 1;
    /** 会话聚合时扫描的近期消息上限（够覆盖活跃会话，避免全表扫） */
    private static final int CONVERSATION_SCAN_LIMIT = 500;

    private final AppMessageMapper baseMapper;
    private final AppUserMapper appUserMapper;

    @Override
    public Long send(Long fromId, Long toId, String content) {
        if (toId == null) {
            throw new ServiceException("请选择聊天对象");
        }
        if (fromId.equals(toId)) {
            throw new ServiceException("不能给自己发消息");
        }
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("消息内容不能为空");
        }
        AppUser peer = appUserMapper.selectById(toId);
        if (peer == null) {
            throw new ServiceException("对方不存在");
        }
        AppMessage m = new AppMessage();
        m.setFromId(fromId);
        m.setToId(toId);
        m.setContent(content.trim());
        m.setIsRead(UNREAD);
        baseMapper.insert(m);
        return m.getId();
    }

    @Override
    public List<ConversationVo> conversations(Long userId) {
        // 取涉及我的近期消息（倒序），按对端归并，首次出现即为该会话最新一条
        List<AppMessage> msgs = baseMapper.selectList(Wrappers.<AppMessage>lambdaQuery()
            .and(w -> w.eq(AppMessage::getFromId, userId).or().eq(AppMessage::getToId, userId))
            .orderByDesc(AppMessage::getCreateTime)
            .last("limit " + CONVERSATION_SCAN_LIMIT));

        LinkedHashMap<Long, ConversationVo> convMap = new LinkedHashMap<>();
        for (AppMessage m : msgs) {
            Long peer = userId.equals(m.getFromId()) ? m.getToId() : m.getFromId();
            ConversationVo c = convMap.get(peer);
            if (c == null) {
                c = new ConversationVo();
                c.setPeerId(peer);
                c.setLastContent(m.getContent());
                c.setLastTime(m.getCreateTime());
                c.setUnread(0);
                convMap.put(peer, c);
            }
            // 未读 = 对端发给我且未读
            if (userId.equals(m.getToId()) && (m.getIsRead() == null || m.getIsRead() == UNREAD)) {
                c.setUnread(c.getUnread() + 1);
            }
        }
        if (convMap.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量补对端昵称/头像
        List<AppUser> peers = appUserMapper.selectByIds(convMap.keySet());
        Map<Long, AppUser> um = new HashMap<>();
        for (AppUser u : peers) {
            um.put(u.getId(), u);
        }
        for (ConversationVo c : convMap.values()) {
            AppUser u = um.get(c.getPeerId());
            c.setPeerNickname(u == null ? "用户" + c.getPeerId() : u.getNickname());
            c.setPeerAvatar(u == null ? null : u.getAvatar());
        }
        return new ArrayList<>(convMap.values());
    }

    @Override
    public TableDataInfo<AppMessageVo> chatPage(Long userId, Long peerId, PageQuery pageQuery) {
        if (peerId == null) {
            throw new ServiceException("请选择聊天对象");
        }
        // 把对端发我的未读全部置已读（进入会话即已读）
        baseMapper.update(null, Wrappers.<AppMessage>lambdaUpdate()
            .eq(AppMessage::getFromId, peerId)
            .eq(AppMessage::getToId, userId)
            .eq(AppMessage::getIsRead, UNREAD)
            .set(AppMessage::getIsRead, READ));
        // 只拉与自己相关的该会话消息，按时间倒序分页（page1=最新一页），前端翻转为时间正序展示。
        // 双向条件整体再包一层 and，得到单个括号组 ((A) OR (B))，避免与框架自动追加的
        // del_flag/tenant AND 条件产生 SQL 优先级错误（否则可能漏过逻辑删除/租户过滤）。
        Page<AppMessageVo> page = baseMapper.selectVoPage(pageQuery.build(),
            Wrappers.<AppMessage>lambdaQuery()
                .and(q -> q
                    .and(w -> w.eq(AppMessage::getFromId, userId).eq(AppMessage::getToId, peerId))
                    .or(w -> w.eq(AppMessage::getFromId, peerId).eq(AppMessage::getToId, userId)))
                .orderByDesc(AppMessage::getCreateTime)
                .orderByDesc(AppMessage::getId));
        return TableDataInfo.build(page);
    }

    @Override
    public long unreadCount(Long userId) {
        return baseMapper.selectCount(Wrappers.<AppMessage>lambdaQuery()
            .eq(AppMessage::getToId, userId)
            .eq(AppMessage::getIsRead, UNREAD));
    }

}

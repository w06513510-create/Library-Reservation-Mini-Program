package org.dromara.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.AppNotification;
import org.dromara.message.domain.bo.NotificationQueryBo;
import org.dromara.message.domain.vo.AppNotificationVo;
import org.dromara.message.mapper.AppNotificationMapper;
import org.dromara.message.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 站内系统通知 Service 实现。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements INotificationService {

    private static final int UNREAD = 0, READ = 1;

    private final AppNotificationMapper baseMapper;

    /**
     * 独立事务落库：调用方(业务)事务回滚不牵连本通知，本通知落库失败也不 taint 调用方事务。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Long send(Long receiverId, String title, String content, String bizType, Long bizId) {
        AppNotification n = new AppNotification();
        n.setReceiverId(receiverId);
        n.setTitle(title);
        n.setContent(content);
        n.setBizType(bizType);
        n.setBizId(bizId);
        n.setIsRead(UNREAD);
        baseMapper.insert(n);
        return n.getId();
    }

    @Override
    public TableDataInfo<AppNotificationVo> pageOfUser(Long userId, PageQuery pageQuery) {
        Page<AppNotificationVo> page = baseMapper.selectVoPage(pageQuery.build(),
            Wrappers.<AppNotification>lambdaQuery()
                .eq(AppNotification::getReceiverId, userId)
                .orderByDesc(AppNotification::getCreateTime));
        return TableDataInfo.build(page);
    }

    @Override
    public long unreadCount(Long userId) {
        return baseMapper.selectCount(Wrappers.<AppNotification>lambdaQuery()
            .eq(AppNotification::getReceiverId, userId)
            .eq(AppNotification::getIsRead, UNREAD));
    }

    @Override
    public boolean markRead(Long userId, Long id) {
        // CAS：带 receiver_id 归属条件，越权(标记他人通知)不会命中任何行
        return baseMapper.update(null, Wrappers.<AppNotification>lambdaUpdate()
            .eq(AppNotification::getId, id)
            .eq(AppNotification::getReceiverId, userId)
            .eq(AppNotification::getIsRead, UNREAD)
            .set(AppNotification::getIsRead, READ)
            .set(AppNotification::getReadTime, new Date())) > 0;
    }

    @Override
    public long markAllRead(Long userId) {
        return baseMapper.update(null, Wrappers.<AppNotification>lambdaUpdate()
            .eq(AppNotification::getReceiverId, userId)
            .eq(AppNotification::getIsRead, UNREAD)
            .set(AppNotification::getIsRead, READ)
            .set(AppNotification::getReadTime, new Date()));
    }

    @Override
    public TableDataInfo<AppNotificationVo> adminPage(NotificationQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AppNotification> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReceiverId() != null, AppNotification::getReceiverId, bo.getReceiverId());
        lqw.eq(StringUtils.isNotBlank(bo.getBizType()), AppNotification::getBizType, bo.getBizType());
        lqw.eq(bo.getIsRead() != null, AppNotification::getIsRead, bo.getIsRead());
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), AppNotification::getTitle, bo.getTitle());
        lqw.orderByDesc(AppNotification::getCreateTime);
        Page<AppNotificationVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

}

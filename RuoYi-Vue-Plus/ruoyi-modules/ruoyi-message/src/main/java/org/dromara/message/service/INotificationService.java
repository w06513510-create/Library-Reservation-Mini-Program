package org.dromara.message.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.message.domain.bo.NotificationQueryBo;
import org.dromara.message.domain.vo.AppNotificationVo;

/**
 * 站内系统通知 Service。
 * <p>C端读写方法均以 userId 作强制归属条件，越权无法读写他人通知。
 *
 * @author ruoyi-template
 */
public interface INotificationService {

    /**
     * 发送通知（在独立事务中落库，供 {@link org.dromara.message.utils.NotificationHelper} 调用）。
     *
     * @return 新通知ID
     */
    Long send(Long receiverId, String title, String content, String bizType, Long bizId);

    /** 我的通知分页（receiver_id=userId，按创建时间倒序） */
    TableDataInfo<AppNotificationVo> pageOfUser(Long userId, PageQuery pageQuery);

    /** 我的未读通知数 */
    long unreadCount(Long userId);

    /** 标记单条已读（CAS：仅当该通知属于 userId 时置已读，防越权） */
    boolean markRead(Long userId, Long id);

    /** 我的全部未读置已读，返回影响条数 */
    long markAllRead(Long userId);

    /** 管理端分页查询（可按 receiverId/bizType/isRead/title 过滤） */
    TableDataInfo<AppNotificationVo> adminPage(NotificationQueryBo bo, PageQuery pageQuery);

}

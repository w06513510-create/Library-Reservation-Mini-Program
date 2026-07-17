package org.dromara.message.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.message.domain.AppNotification;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 站内系统通知视图对象 app_notification
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppNotification.class)
public class AppNotificationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long id;

    /** 接收用户ID */
    private Long receiverId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 关联业务类型 */
    private String bizType;

    /** 关联业务ID */
    private Long bizId;

    /** 是否已读(0未读 1已读) */
    private Integer isRead;

    /** 阅读时间 */
    private Date readTime;

    /** 创建时间 */
    private Date createTime;

}

package org.dromara.message.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理端站内通知查询条件
 *
 * @author ruoyi-template
 */
@Data
public class NotificationQueryBo implements Serializable {

    /** 接收用户ID */
    private Long receiverId;

    /** 关联业务类型 */
    private String bizType;

    /** 是否已读(0未读 1已读) */
    private Integer isRead;

    /** 标题(模糊) */
    private String title;

}

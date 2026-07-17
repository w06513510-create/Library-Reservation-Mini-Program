package org.dromara.message.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.message.domain.AppMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 站内私信视图对象 app_message
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppMessage.class)
public class AppMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 发送人ID */
    private Long fromId;

    /** 接收人ID */
    private Long toId;

    /** 内容 */
    private String content;

    /** 是否已读(0未读 1已读) */
    private Integer isRead;

    /** 创建时间 */
    private Date createTime;

}

package org.dromara.message.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 站内私信对象 app_message（通用，去业务耦合）
 * <p>C端用户之间的一对一私信，扁平存储；会话按无序对 (from_id, to_id) 归并（不建 conversation 表）。
 * 内容为纯文本 {@code content}；如需图片消息可由业务方将 OSS url 塞进 content 约定，本模块不做富消息。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_message")
public class AppMessage extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @TableId(value = "id")
    private Long id;

    /** 发送人ID(app_user) */
    private Long fromId;

    /** 接收人ID(app_user) */
    private Long toId;

    /** 内容(纯文本) */
    private String content;

    /** 是否已读(0未读 1已读; 站在接收人视角) */
    private Integer isRead;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

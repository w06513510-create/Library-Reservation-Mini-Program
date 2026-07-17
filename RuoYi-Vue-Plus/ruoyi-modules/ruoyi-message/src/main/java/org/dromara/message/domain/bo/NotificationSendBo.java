package org.dromara.message.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理端发送站内通知入参
 *
 * @author ruoyi-template
 */
@Data
public class NotificationSendBo implements Serializable {

    /** 接收用户ID(app_user) */
    @NotNull(message = "接收用户不能为空")
    private Long receiverId;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 内容 */
    private String content;

    /** 关联业务类型(可空) */
    private String bizType;

    /** 关联业务ID(可空) */
    private Long bizId;

}

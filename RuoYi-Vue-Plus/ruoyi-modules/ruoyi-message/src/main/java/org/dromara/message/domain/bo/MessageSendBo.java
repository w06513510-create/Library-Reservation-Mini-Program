package org.dromara.message.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * C端发送私信入参（发送人恒为当前登录用户，不由前端传入）
 *
 * @author ruoyi-template
 */
@Data
public class MessageSendBo implements Serializable {

    /** 接收人ID(app_user) */
    @NotNull(message = "请选择聊天对象")
    private Long toId;

    /** 内容(纯文本) */
    @NotBlank(message = "消息内容不能为空")
    private String content;

}

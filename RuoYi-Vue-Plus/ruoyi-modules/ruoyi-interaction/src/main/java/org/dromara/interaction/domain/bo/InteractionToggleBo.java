package org.dromara.interaction.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 互动开关请求体。
 *
 * @author ruoyi-template
 */
@Data
public class InteractionToggleBo {

    /** 动作: favorite/like/follow */
    @NotBlank(message = "动作不能为空")
    private String action;

    /** 业务类型 */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** 业务对象ID */
    @NotNull(message = "业务对象ID不能为空")
    private Long bizId;

}

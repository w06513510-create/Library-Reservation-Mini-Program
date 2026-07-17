package org.dromara.app.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * C端(小程序)登录请求体：手机号 + 密码
 *
 * @author ruoyi-template
 */
@Data
public class AppLoginBody {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

}

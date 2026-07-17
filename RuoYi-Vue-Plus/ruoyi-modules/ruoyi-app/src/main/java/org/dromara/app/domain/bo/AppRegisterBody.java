package org.dromara.app.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * C端(小程序)注册请求体：手机号 + 密码(+ 可选昵称)
 *
 * @author ruoyi-template
 */
@Data
public class AppRegisterBody {

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 昵称(可空, 缺省用"用户"+手机后四位) */
    private String nickname;

}

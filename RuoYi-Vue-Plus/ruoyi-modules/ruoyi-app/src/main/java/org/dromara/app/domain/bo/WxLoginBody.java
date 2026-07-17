package org.dromara.app.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信 openid 登录请求体（预留占位，默认未启用）。
 * <p>启用时：前端 uni.login 拿 code 传来，后端换取 openid → upsert app_user → 签发 token。
 *
 * @author ruoyi-template
 */
@Data
public class WxLoginBody {

    /** 微信登录临时凭证 code */
    @NotBlank(message = "code不能为空")
    private String code;

}

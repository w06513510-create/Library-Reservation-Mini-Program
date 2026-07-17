package org.dromara.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.app.domain.AppUser;
import org.dromara.app.domain.bo.AppLoginBody;
import org.dromara.app.domain.bo.AppRegisterBody;
import org.dromara.app.domain.bo.WxLoginBody;
import org.dromara.app.domain.vo.AppUserVo;
import org.dromara.app.service.IAppUserService;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * C端(小程序)认证 Controller —— app_user 手机号+密码 登录/注册。
 * <p>{@code /app/auth/login}、{@code /app/auth/register} 已在 application.yml
 * security.excludes 放行(免鉴权)；其余接口走安全拦截器 + {@link AppLoginHelper#getUserId()} 二次校身份。
 * <p>C端与后台 sys_user 走同一 Sa-Token，靠 loginId 命名空间 {@code app_user:{id}} 隔离。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/auth")
public class AppAuthController {

    private final IAppUserService appUserService;

    /** 登录：手机号 + 密码，返回 {token} */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody AppLoginBody body, HttpServletRequest request) {
        AppUser u = appUserService.getByPhone(body.getPhone());
        String hashed = u == null || u.getPassword() == null ? "" : u.getPassword();
        if (u == null || !BCrypt.checkpw(body.getPassword(), hashed)) {
            throw new ServiceException("手机号或密码错误");
        }
        if (u.getStatus() != null && u.getStatus() == 2) {
            throw new ServiceException("账号已被封禁，请联系客服");
        }
        String clientId = request.getHeader(AppLoginHelper.CLIENT_KEY);
        AppLoginHelper.login(u.getId(), clientId);
        appUserService.touchLoginTime(u.getId());
        Map<String, Object> data = new HashMap<>(1);
        data.put("token", StpUtil.getTokenValue());
        return R.ok(data);
    }

    /** 注册：手机号 + 密码(+ 可选昵称) */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody AppRegisterBody body) {
        if (appUserService.existsByPhone(body.getPhone())) {
            throw new ServiceException("该手机号已注册");
        }
        appUserService.register(body);
        return R.ok();
    }

    /** 当前登录 C端用户信息(脱敏) */
    @SaCheckLogin
    @GetMapping("/getInfo")
    public R<AppUserVo> getInfo() {
        return R.ok(appUserService.getVoById(AppLoginHelper.getUserId()));
    }

    /** 更新头像(url 来自 /resource/media/upload 的返回) */
    @SaCheckLogin
    @PutMapping("/avatar")
    public R<Void> updateAvatar(@RequestParam String url) {
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("头像地址不能为空");
        }
        appUserService.updateAvatar(AppLoginHelper.getUserId(), url);
        return R.ok();
    }

    /** 更新昵称 */
    @SaCheckLogin
    @PutMapping("/nickname")
    public R<Void> updateNickname(@RequestParam String nickname) {
        if (StringUtils.isBlank(nickname)) {
            throw new ServiceException("昵称不能为空");
        }
        appUserService.updateNickname(AppLoginHelper.getUserId(), nickname.trim());
        return R.ok();
    }

    /** 退出登录 */
    @SaCheckLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    /**
     * 微信 openid 登录（预留占位，默认未启用）。
     * <p>启用步骤：① 补微信 SDK/HTTP 换取依赖；② 实现 code→openid、按 openid upsert app_user；
     * ③ 在 application.yml security.excludes 放行 {@code /app/auth/wxLogin}。
     */
    @PostMapping("/wxLogin")
    public R<Map<String, Object>> wxLogin(@Valid @RequestBody WxLoginBody body) {
        throw new ServiceException("微信登录未启用（预留接口，见 AppAuthController#wxLogin 注释）");
    }

}

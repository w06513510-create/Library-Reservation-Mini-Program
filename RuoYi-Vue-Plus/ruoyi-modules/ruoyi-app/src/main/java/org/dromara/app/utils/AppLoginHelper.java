package org.dromara.app.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import org.dromara.common.core.exception.ServiceException;

/**
 * C端(小程序)登录助手。
 * <p>app_user 走同一 Sa-Token，loginId 命名空间为 {@code app_user:{id}}，与后台 sys_user 隔离并存。
 * 登录时把 clientid/userId/tenantId 写进 token 的 extra，以通过框架安全拦截器的 clientid 一致性校验。
 * <p>业务模块可直接调用 {@link #getUserId()} 拿到当前 C 端用户ID（非 C 端登录会抛 403）。
 *
 * @author ruoyi-template
 */
public class AppLoginHelper {

    /** C端 loginId 前缀 */
    public static final String PREFIX = "app_user:";
    /** 必须与框架 LoginHelper 的 clientid key 同名，否则过不了拦截器校验 */
    public static final String CLIENT_KEY = "clientid";
    public static final String USER_KEY = "userId";
    public static final String TENANT_KEY = "tenantId";
    /** 设备类型标识 */
    public static final String DEVICE = "app";
    /** 单租户默认租户号（多租户场景改为按域名/参数解析） */
    public static final String DEFAULT_TENANT = "000000";

    /**
     * C端登录：签发 app_user token。
     *
     * @param userId   app_user 主键
     * @param clientId 请求头带来的 clientid，须与后续请求头保持一致
     */
    public static void login(Long userId, String clientId) {
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(DEVICE);
        model.setExtra(CLIENT_KEY, clientId);
        model.setExtra(USER_KEY, userId);
        model.setExtra(TENANT_KEY, DEFAULT_TENANT);
        StpUtil.login(PREFIX + userId, model);
    }

    /**
     * 取当前 C端用户ID；未登录抛 401，非 C端(如后台 sys_user)登录抛 403。
     */
    public static Long getUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new ServiceException("未登录或登录已过期", 401);
        }
        String s = loginId.toString();
        if (!s.startsWith(PREFIX)) {
            throw new ServiceException("仅限C端用户访问", 403);
        }
        return Long.parseLong(s.substring(PREFIX.length()));
    }

}

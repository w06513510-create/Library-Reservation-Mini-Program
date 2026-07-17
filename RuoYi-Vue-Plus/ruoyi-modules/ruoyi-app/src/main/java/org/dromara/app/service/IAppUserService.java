package org.dromara.app.service;

import org.dromara.app.domain.AppUser;
import org.dromara.app.domain.bo.AppRegisterBody;
import org.dromara.app.domain.vo.AppUserVo;

/**
 * C端用户服务。业务模块可注入本接口按 id/手机号查 C 端用户。
 *
 * @author ruoyi-template
 */
public interface IAppUserService {

    /** 按手机号查用户（登录用，内部忽略租户过滤：单租户手机号全局唯一） */
    AppUser getByPhone(String phone);

    /** 手机号是否已注册 */
    boolean existsByPhone(String phone);

    /** 注册，返回新用户ID */
    Long register(AppRegisterBody body);

    /** 按ID查视图对象（已剥离敏感字段） */
    AppUserVo getVoById(Long id);

    /** 更新头像 */
    void updateAvatar(Long id, String url);

    /** 更新昵称 */
    void updateNickname(Long id, String nickname);

    /** 记录最后登录时间 */
    void touchLoginTime(Long id);

}

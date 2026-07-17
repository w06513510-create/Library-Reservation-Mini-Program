package org.dromara.app.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.app.domain.AppUser;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * C端用户视图对象 app_user
 * <p>下发给 C 端用，剥离 password/openid/unionid 等敏感字段。
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppUser.class)
public class AppUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 头像(OSS url) */
    private String avatar;

    /** 性别(0未知 1男 2女) */
    private Integer gender;

    /** 账号状态(0正常 1受限 2封禁) */
    private Integer status;

    /** 注册时间 */
    private Date registerTime;

    /** 最后登录时间 */
    private Date lastLoginTime;

}

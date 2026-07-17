package org.dromara.app.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * C端用户对象 app_user（通用接入基座）
 * <p>只保留跨项目通用字段；实名认证/信用分/业务归属/多角色等业务字段由各业务模块自行加列或加子表。
 * 登录名为手机号；与后台 sys_user 分表并存，靠 Sa-Token loginId 命名空间 {@code app_user:{id}} 隔离。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_user")
public class AppUser extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "id")
    private Long id;

    /** 手机号(登录名) */
    private String phone;

    /** 密码(BCrypt; 敏感, 不下发) */
    private String password;

    /** 微信openid(登录方式预留, 可空; 敏感, 不下发) */
    private String openid;

    /** 微信unionid(预留, 可空; 敏感, 不下发) */
    private String unionid;

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

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

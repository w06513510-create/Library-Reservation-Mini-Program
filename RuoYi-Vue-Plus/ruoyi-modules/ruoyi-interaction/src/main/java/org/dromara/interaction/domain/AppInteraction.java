package org.dromara.interaction.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 通用互动对象 app_interaction
 * <p>一条 = 一个用户对一个业务对象(biz_type/biz_id)的一种动作(favorite/like/follow)。
 * 取消互动为物理删除（无审计价值），故不设逻辑删除。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_interaction")
public class AppInteraction extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 发起用户(app_user) */
    private Long userId;

    /** 动作: favorite/like/follow */
    private String action;

    /** 业务类型(product/post/user...) */
    private String bizType;

    /** 业务对象ID(关注人时为目标 userId) */
    private Long bizId;

}

package org.dromara.message.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 站内系统通知对象 app_notification（通用，去业务耦合）
 * <p>系统 → C端用户 的单向通知。业务耦合信息统一挂到 {@code bizType(varchar)+bizId(bigint)} 两个通用字段，
 * 由业务模块通过 {@link org.dromara.message.utils.NotificationHelper#send} 产生；本模块只负责存/读/已读。
 * 接收者为 app_user（{@code receiverId}）。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_notification")
public class AppNotification extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @TableId(value = "id")
    private Long id;

    /** 接收用户ID(app_user) */
    private Long receiverId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 关联业务类型(通用挂载点, 如 order/comment/system; 由业务方自定义, 可空) */
    private String bizType;

    /** 关联业务ID(通用挂载点, 可空) */
    private Long bizId;

    /** 是否已读(0未读 1已读) */
    private Integer isRead;

    /** 阅读时间 */
    private Date readTime;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

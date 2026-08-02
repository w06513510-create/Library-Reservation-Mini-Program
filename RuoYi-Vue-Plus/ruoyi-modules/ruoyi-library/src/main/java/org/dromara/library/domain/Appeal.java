package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 违约申诉对象 biz_appeal
 * 状态：0待审 1通过(解除违约+冲正) 2驳回
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_appeal")
public class Appeal extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 被申诉的违约记录ID */
    private Long violationId;

    /** 申诉读者ID（app_user） */
    private Long readerId;

    /** 申诉理由 */
    private String reason;

    /** 状态：0待审 1通过 2驳回 */
    private Integer status;

    /** 审批人（sys_user） */
    private Long auditBy;

    /** 审批时间 */
    private Date auditTime;

    /** 审批意见 */
    private String auditRemark;

    @TableLogic
    private String delFlag;

}

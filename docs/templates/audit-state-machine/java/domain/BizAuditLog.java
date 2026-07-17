package org.dromara.biz.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 审核流转轨迹对象 biz_audit_log
 * <p>提炼自宿舍 dorm_repair_log：每发生一次状态流转 append 一行，形成操作台账。
 * <p>仅在"多状态工单/需要审批留痕"时才用；简单三态审核可不建此表。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_audit_log")
public class BizAuditLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 关联审核单(biz_audit.id) */
    private Long auditId;

    /** 动作(submit/approve/reject/accept/finish... 业务自定义) */
    private String action;

    /** 原状态 */
    private Integer fromStatus;

    /** 新状态 */
    private Integer toStatus;

    /** 操作人(sys_user.id) */
    private Long operatorUserId;

    /** 备注/审批意见/驳回原因 */
    private String remark;

    /** 操作时间 */
    private Date actionTime;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

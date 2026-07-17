package org.dromara.biz.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.biz.domain.BizAudit;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用审核单视图对象 biz_audit
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = BizAudit.class)
public class BizAuditVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 业务类型 */
    private String bizType;

    /** 关联业务主键 */
    private Long bizId;

    /** 申请人 */
    private Long applyUserId;

    /** 申请内容 */
    private String content;

    /** 审核状态(0待审 1通过 2驳回) */
    private Integer status;

    /** 驳回原因 */
    private String rejectReason;

    /** 审核人 */
    private Long auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 申请时间 */
    private Date createTime;

}

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
 * 通用审核单对象 biz_audit
 * <p>提炼自跑腿 errand_realname_auth / errand_runner_apply（0待审 1通过 2驳回 同构审核）。
 * <p>把包名 {@code org.dromara.biz}、类名 BizAudit、表名 biz_audit 换成你的业务。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_audit")
public class BizAudit extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 业务类型(一表多用时区分不同审核) */
    private String bizType;

    /** 关联业务主键(审核对象在别的业务表时填其ID) */
    private Long bizId;

    /** 申请人 */
    private Long applyUserId;

    /** 申请内容/提交材料(纯文本或JSON) */
    private String content;

    /** 审核状态(0待审 1通过 2驳回) */
    private Integer status;

    /** 驳回原因(status=2 时必填) */
    private String rejectReason;

    /** 审核人 */
    private Long auditBy;

    /** 审核时间 */
    private Date auditTime;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

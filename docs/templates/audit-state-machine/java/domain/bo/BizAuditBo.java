package org.dromara.biz.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.biz.domain.BizAudit;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 通用审核单业务对象 biz_audit（提交/查询用）
 * <p>提交时申请人 applyUserId 由服务端从登录态取，不接收前端传入；审核字段(status/auditBy/rejectReason)也不由此 BO 写入。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizAudit.class, reverseConvertGenerate = false)
public class BizAuditBo extends BaseEntity {

    /** 业务类型(一表多用时区分不同审核) */
    @NotBlank(message = "业务类型不能为空", groups = {AddGroup.class, QueryGroup.class})
    private String bizType;

    /** 关联业务主键(可空) */
    private Long bizId;

    /** 申请内容/提交材料 */
    private String content;

    /** 审核状态(仅列表查询用作过滤条件) */
    private Integer status;

}

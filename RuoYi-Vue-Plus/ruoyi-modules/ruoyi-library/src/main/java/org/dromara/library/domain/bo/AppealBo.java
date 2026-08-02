package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Appeal;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 违约申诉业务对象 biz_appeal
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Appeal.class, reverseConvertGenerate = false)
public class AppealBo extends BaseEntity {

    private Long id;

    /** 被申诉的违约记录ID */
    @NotNull(message = "违约记录不能为空", groups = {AddGroup.class})
    private Long violationId;

    /** 申诉读者ID（app_user） */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /** 申诉理由 */
    private String reason;

    /** 状态（查询） */
    private Integer status;

}

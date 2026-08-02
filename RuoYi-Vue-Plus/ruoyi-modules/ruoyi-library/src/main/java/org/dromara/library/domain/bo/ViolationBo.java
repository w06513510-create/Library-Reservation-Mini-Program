package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Violation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 违约记录业务对象 biz_violation
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Violation.class, reverseConvertGenerate = false)
public class ViolationBo extends BaseEntity {

    private Long id;

    /** 读者ID（app_user） */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /** 违约类型 1-7 */
    @NotNull(message = "违约类型不能为空", groups = {AddGroup.class})
    private Integer violationType;

    /** 扣分 */
    private Integer deductScore;

    /** 状态（查询） */
    private Integer status;

}

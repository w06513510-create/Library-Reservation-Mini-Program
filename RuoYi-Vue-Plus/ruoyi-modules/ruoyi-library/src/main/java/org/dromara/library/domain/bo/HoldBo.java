package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Hold;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 图书预约(hold)业务对象 biz_hold
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Hold.class, reverseConvertGenerate = false)
public class HoldBo extends BaseEntity {

    private Long id;

    /** 读者ID（app_user） */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /** 书目ID */
    @NotNull(message = "书目不能为空", groups = {AddGroup.class})
    private Long bookId;

    /** 状态（查询） */
    private Integer status;

}

package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Supervise;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 占座监督业务对象 biz_supervise
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Supervise.class, reverseConvertGenerate = false)
public class SuperviseBo extends BaseEntity {

    /**
     * 监督ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 被监督的座位预约单ID（biz_reservation）
     */
    @NotNull(message = "被监督的预约单不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long reservationId;

    /**
     * 座位ID（由预约单推导，无需前端传）
     */
    private Long seatId;

    /**
     * 举报读者ID（app_user）
     */
    @NotNull(message = "举报读者不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long reporterId;

    /**
     * 状态：0进行中 1已解除已落座 2超时释放（查询用）
     */
    private Integer status;

}

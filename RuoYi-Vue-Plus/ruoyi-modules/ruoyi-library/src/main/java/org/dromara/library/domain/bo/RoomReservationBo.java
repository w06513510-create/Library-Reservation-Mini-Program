package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.RoomReservation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 研讨间预约业务对象 biz_room_reservation
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = RoomReservation.class, reverseConvertGenerate = false)
public class RoomReservationBo extends BaseEntity {

    /**
     * 研讨间预约ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 预约读者ID（app_user）
     */
    @NotNull(message = "预约读者不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long readerId;

    /**
     * 研讨间ID（biz_room）
     */
    @NotNull(message = "研讨间不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long roomId;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空", groups = {AddGroup.class, EditGroup.class})
    private Date reserveDate;

    /**
     * 时段开始
     */
    @NotNull(message = "时段开始不能为空", groups = {AddGroup.class, EditGroup.class})
    private Date startTime;

    /**
     * 时段结束
     */
    @NotNull(message = "时段结束不能为空", groups = {AddGroup.class, EditGroup.class})
    private Date endTime;

    /**
     * 使用人数
     */
    private Integer userCount;

    /**
     * 状态（查询用）：0待审批 1已通过待使用 2使用中 3已完成 4已取消 5已驳回 6已违约
     */
    private Integer status;

}

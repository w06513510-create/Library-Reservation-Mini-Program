package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Reservation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 座位预约单业务对象 biz_reservation
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Reservation.class, reverseConvertGenerate = false)
public class ReservationBo extends BaseEntity {

    /**
     * 座位预约单ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 读者ID（app_user）
     */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /**
     * 座位ID
     */
    @NotNull(message = "座位不能为空", groups = {AddGroup.class})
    private Long seatId;

    /**
     * 场馆/楼层/区域（冗余，可由座位推导，前端可不传）
     */
    private Long venueId;
    private Long floorId;
    private Long areaId;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空", groups = {AddGroup.class})
    private Date reserveDate;

    /**
     * 时段开始
     */
    @NotNull(message = "时段开始不能为空", groups = {AddGroup.class})
    private Date startTime;

    /**
     * 时段结束
     */
    @NotNull(message = "时段结束不能为空", groups = {AddGroup.class})
    private Date endTime;

    /**
     * 预约方式：1平面图选座 2快速选座 3现场扫码
     */
    private Integer source;

    /**
     * 状态（查询用）
     */
    private Integer status;

}

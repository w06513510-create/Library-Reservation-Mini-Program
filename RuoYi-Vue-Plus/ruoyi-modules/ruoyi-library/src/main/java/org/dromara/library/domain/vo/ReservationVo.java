package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Reservation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 座位预约单视图对象 biz_reservation
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Reservation.class)
public class ReservationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "预约单ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "座位ID")
    private Long seatId;

    private Long venueId;
    private Long floorId;
    private Long areaId;

    @ExcelProperty(value = "预约日期")
    private Date reserveDate;

    @ExcelProperty(value = "时段开始")
    private Date startTime;

    @ExcelProperty(value = "时段结束")
    private Date endTime;

    @ExcelProperty(value = "预约方式")
    private Integer source;

    @ExcelProperty(value = "状态")
    private Integer status;

    @ExcelProperty(value = "签到时间")
    private Date checkInTime;

    private Date awayStartTime;

    private Integer awayCount;

    private Date actualEndTime;

    private Date cancelTime;

    private String remark;

    private Date createTime;

}

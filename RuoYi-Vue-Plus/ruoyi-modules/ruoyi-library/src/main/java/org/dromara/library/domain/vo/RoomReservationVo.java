package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.RoomReservation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 研讨间预约视图对象 biz_room_reservation
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = RoomReservation.class)
public class RoomReservationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 研讨间预约ID
     */
    @ExcelProperty(value = "预约ID")
    private Long id;

    /**
     * 预约读者ID（app_user）
     */
    @ExcelProperty(value = "读者ID")
    private Long readerId;

    /**
     * 研讨间ID（biz_room）
     */
    @ExcelProperty(value = "研讨间ID")
    private Long roomId;

    /**
     * 预约日期
     */
    @ExcelProperty(value = "预约日期")
    private Date reserveDate;

    /**
     * 时段开始
     */
    @ExcelProperty(value = "时段开始")
    private Date startTime;

    /**
     * 时段结束
     */
    @ExcelProperty(value = "时段结束")
    private Date endTime;

    /**
     * 使用人数
     */
    @ExcelProperty(value = "使用人数")
    private Integer userCount;

    /**
     * 状态：0待审批 1已通过待使用 2使用中 3已完成 4已取消 5已驳回 6已违约
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 签到时间
     */
    @ExcelProperty(value = "签到时间")
    private Date checkInTime;

    /**
     * 审批人
     */
    @ExcelProperty(value = "审批人")
    private Long approveBy;

    /**
     * 审批时间
     */
    @ExcelProperty(value = "审批时间")
    private Date approveTime;

    /**
     * 驳回原因
     */
    @ExcelProperty(value = "驳回原因")
    private String rejectReason;

    /**
     * 创建时间
     */
    private Date createTime;

}

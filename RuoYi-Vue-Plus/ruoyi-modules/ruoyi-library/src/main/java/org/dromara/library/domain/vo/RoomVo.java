package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Room;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 研讨间视图对象 biz_room
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Room.class)
public class RoomVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 研讨间ID
     */
    @ExcelProperty(value = "研讨间ID")
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    @ExcelProperty(value = "所属楼层ID")
    private Long floorId;

    /**
     * 研讨间名称/编号
     */
    @ExcelProperty(value = "研讨间名称")
    private String roomName;

    /**
     * 容纳人数
     */
    @ExcelProperty(value = "容纳人数")
    private Integer capacity;

    /**
     * 预约最少人数
     */
    @ExcelProperty(value = "预约最少人数")
    private Integer minUsers;

    /**
     * 是否需审批：0否 1是
     */
    @ExcelProperty(value = "是否需审批")
    private Integer needApprove;

    /**
     * 是否需签到：0否 1是
     */
    @ExcelProperty(value = "是否需签到")
    private Integer needCheckin;

    /**
     * 平面图X坐标
     */
    @ExcelProperty(value = "平面图X坐标")
    private Integer posX;

    /**
     * 平面图Y坐标
     */
    @ExcelProperty(value = "平面图Y坐标")
    private Integer posY;

    /**
     * 状态：0正常 1停用
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

}

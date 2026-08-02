package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Seat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 座位视图对象 biz_seat
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Seat.class)
public class SeatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @ExcelProperty(value = "座位ID")
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    @ExcelProperty(value = "所属区域ID")
    private Long areaId;

    /**
     * 座位编号（区域内唯一，如 A-012）
     */
    @ExcelProperty(value = "座位编号")
    private String seatNo;

    /**
     * 座位类型：0普通 1靠窗 2沙发 3单间
     */
    @ExcelProperty(value = "座位类型")
    private Integer seatType;

    /**
     * 有无插座：0无 1有
     */
    @ExcelProperty(value = "有无插座")
    private Integer hasPower;

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
     * 桌面二维码标识（扫码签到用）
     */
    @ExcelProperty(value = "桌面二维码标识")
    private String qrCode;

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

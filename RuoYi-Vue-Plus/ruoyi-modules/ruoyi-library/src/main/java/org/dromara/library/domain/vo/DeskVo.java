package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Desk;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 桌子视图对象 biz_desk
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Desk.class)
public class DeskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 桌子ID
     */
    @ExcelProperty(value = "桌子ID")
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    @ExcelProperty(value = "所属区域ID")
    private Long areaId;

    /**
     * 桌号（如 D01）
     */
    @ExcelProperty(value = "桌号")
    private String deskNo;

    /**
     * 容量：1单人 2双人 4四人 6六人
     */
    @ExcelProperty(value = "容量")
    private Integer capacity;

    /**
     * 桌形：0矩形 1圆 2吧台
     */
    @ExcelProperty(value = "桌形")
    private Integer shape;

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
     * 平面图宽度
     */
    @ExcelProperty(value = "平面图宽度")
    private Integer width;

    /**
     * 平面图高度
     */
    @ExcelProperty(value = "平面图高度")
    private Integer height;

    /**
     * 旋转角度
     */
    @ExcelProperty(value = "旋转角度")
    private Integer rotation;

    /**
     * 排序
     */
    @ExcelProperty(value = "排序")
    private Integer sort;

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

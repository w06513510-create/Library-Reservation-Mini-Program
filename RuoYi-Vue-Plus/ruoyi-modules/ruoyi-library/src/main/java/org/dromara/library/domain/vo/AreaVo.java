package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Area;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 区域视图对象 biz_area
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Area.class)
public class AreaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    @ExcelProperty(value = "区域ID")
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    @ExcelProperty(value = "所属楼层ID")
    private Long floorId;

    /**
     * 区域名称（如 A区自习/研讨区）
     */
    @ExcelProperty(value = "区域名称")
    private String areaName;

    /**
     * 区域类型：0自习阅览 1研讨区 2其它
     */
    @ExcelProperty(value = "区域类型")
    private Integer areaType;

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

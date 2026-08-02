package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Floor;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 楼层视图对象 biz_floor
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Floor.class)
public class FloorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 楼层ID
     */
    @ExcelProperty(value = "楼层ID")
    private Long id;

    /**
     * 所属场馆ID（biz_venue）
     */
    @ExcelProperty(value = "所属场馆ID")
    private Long venueId;

    /**
     * 楼层名称（如 三楼社科阅览）
     */
    @ExcelProperty(value = "楼层名称")
    private String floorName;

    /**
     * 楼层号
     */
    @ExcelProperty(value = "楼层号")
    private Integer floorNo;

    /**
     * 楼层平面图底图URL
     */
    @ExcelProperty(value = "楼层平面图底图URL")
    private String floorPlanUrl;

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

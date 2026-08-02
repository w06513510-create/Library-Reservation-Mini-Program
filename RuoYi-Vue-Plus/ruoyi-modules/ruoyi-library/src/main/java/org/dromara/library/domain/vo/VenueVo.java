package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Venue;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 场馆视图对象 biz_venue
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Venue.class)
public class VenueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 场馆ID
     */
    @ExcelProperty(value = "场馆ID")
    private Long id;

    /**
     * 场馆名称
     */
    @ExcelProperty(value = "场馆名称")
    private String venueName;

    /**
     * 地址
     */
    @ExcelProperty(value = "地址")
    private String address;

    /**
     * 开馆时间
     */
    @ExcelProperty(value = "开馆时间")
    private String openTime;

    /**
     * 闭馆时间
     */
    @ExcelProperty(value = "闭馆时间")
    private String closeTime;

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

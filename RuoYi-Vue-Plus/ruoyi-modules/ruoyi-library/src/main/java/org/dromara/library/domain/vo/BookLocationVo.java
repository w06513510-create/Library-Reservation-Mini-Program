package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.BookLocation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 藏地视图对象 biz_book_location
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BookLocation.class)
public class BookLocationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 藏地ID
     */
    @ExcelProperty(value = "藏地ID")
    private Long id;

    /**
     * 藏地名称（如 三楼社科借阅室）
     */
    @ExcelProperty(value = "藏地名称")
    private String locationName;

    /**
     * 所在楼层ID
     */
    @ExcelProperty(value = "所在楼层ID")
    private Long floorId;

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

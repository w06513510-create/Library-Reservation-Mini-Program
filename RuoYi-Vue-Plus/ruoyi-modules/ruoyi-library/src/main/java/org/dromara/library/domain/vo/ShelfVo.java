package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Shelf;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 书架视图对象 biz_shelf
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Shelf.class)
public class ShelfVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 书架ID
     */
    @ExcelProperty(value = "书架ID")
    private Long id;

    /**
     * 所属藏地ID（biz_book_location）
     */
    @ExcelProperty(value = "所属藏地ID")
    private Long locationId;

    /**
     * 架号（如 A12）
     */
    @ExcelProperty(value = "架号")
    private String shelfNo;

    /**
     * 索书号起（排架区间起）
     */
    @ExcelProperty(value = "索书号起")
    private String callNoStart;

    /**
     * 索书号止（排架区间止）
     */
    @ExcelProperty(value = "索书号止")
    private String callNoEnd;

    /**
     * 平面图X坐标（亮点①寻书）
     */
    @ExcelProperty(value = "平面图X坐标")
    private Integer posX;

    /**
     * 平面图Y坐标（亮点①寻书）
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

package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.BookItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 馆藏册（册/Item）视图对象 biz_book_item
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BookItem.class)
public class BookItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 馆藏册ID（册/Item）
     */
    @ExcelProperty(value = "馆藏册ID")
    private Long id;

    /**
     * 所属书目ID（biz_book）
     */
    @ExcelProperty(value = "所属书目ID")
    private Long bookId;

    /**
     * 条码（每册唯一）
     */
    @ExcelProperty(value = "条码")
    private String barcode;

    /**
     * 索书号（含别本/种次号）
     */
    @ExcelProperty(value = "索书号")
    private String callNo;

    /**
     * 藏地ID（biz_book_location）
     */
    @ExcelProperty(value = "藏地ID")
    private Long locationId;

    /**
     * 书架/排架位ID（biz_shelf，寻书定位）
     */
    @ExcelProperty(value = "书架ID")
    private Long shelfId;

    /**
     * 状态：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 注销类型：1剔旧 2报损 3遗失核销（status=6时填）
     */
    @ExcelProperty(value = "注销类型")
    private Integer withdrawType;

    /**
     * 注销原因
     */
    @ExcelProperty(value = "注销原因")
    private String withdrawReason;

    /**
     * 注销时间
     */
    @ExcelProperty(value = "注销时间")
    private Date withdrawTime;

    /**
     * 创建时间
     */
    private Date createTime;

}

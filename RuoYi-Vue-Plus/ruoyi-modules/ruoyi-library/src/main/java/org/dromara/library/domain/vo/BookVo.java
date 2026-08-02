package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Book;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 书目（种/Bib）视图对象 biz_book
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Book.class)
public class BookVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 书目ID（种/Bib）
     */
    @ExcelProperty(value = "书目ID")
    private Long id;

    /**
     * ISBN
     */
    @ExcelProperty(value = "ISBN")
    private String isbn;

    /**
     * 题名
     */
    @ExcelProperty(value = "题名")
    private String title;

    /**
     * 著者
     */
    @ExcelProperty(value = "著者")
    private String author;

    /**
     * 出版社
     */
    @ExcelProperty(value = "出版社")
    private String publisher;

    /**
     * 出版日期
     */
    @ExcelProperty(value = "出版日期")
    private String publishDate;

    /**
     * 中图法分类号（CLC）
     */
    @ExcelProperty(value = "中图法分类号")
    private String clcNo;

    /**
     * 索书号
     */
    @ExcelProperty(value = "索书号")
    private String callNo;

    /**
     * 封面图URL
     */
    @ExcelProperty(value = "封面图URL")
    private String coverUrl;

    /**
     * 内容简介
     */
    @ExcelProperty(value = "内容简介")
    private String summary;

    /**
     * 定价
     */
    @ExcelProperty(value = "定价")
    private BigDecimal price;

    /**
     * 复本总数
     */
    @ExcelProperty(value = "复本总数")
    private Integer totalQty;

    /**
     * 当前可借册数
     */
    @ExcelProperty(value = "当前可借册数")
    private Integer availQty;

    /**
     * 状态：0在编 1已上架(可借) 2已下架
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

}

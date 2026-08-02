package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Book;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 书目（种/Bib）业务对象 biz_book
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Book.class, reverseConvertGenerate = false)
public class BookBo extends BaseEntity {

    /**
     * 书目ID（种/Bib）
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 题名
     */
    @NotBlank(message = "题名不能为空", groups = {AddGroup.class, EditGroup.class})
    private String title;

    /**
     * 著者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版日期
     */
    private String publishDate;

    /**
     * 中图法分类号（CLC）
     */
    private String clcNo;

    /**
     * 索书号（种级基准 = 分类号+著者号）
     */
    private String callNo;

    /**
     * 封面图URL（MinIO）
     */
    private String coverUrl;

    /**
     * 内容简介
     */
    private String summary;

    /**
     * 定价（登记用，非交易；本项目不涉支付）
     */
    private BigDecimal price;

    /**
     * 复本总数（册数，冗余统计）
     */
    private Integer totalQty;

    /**
     * 当前可借册数（冗余，借还时维护）
     */
    private Integer availQty;

    /**
     * 状态：0在编 1已上架(可借) 2已下架
     */
    private Integer status;

}

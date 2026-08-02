package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 书目（种/Bib）对象 biz_book
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_book")
public class Book extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 书目ID（种/Bib）
     */
    @TableId(value = "id")
    private Long id;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 题名
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

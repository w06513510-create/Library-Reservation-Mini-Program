package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.PurchaseSuggest;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 读者荐购视图对象 biz_purchase_suggest
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PurchaseSuggest.class)
public class PurchaseSuggestVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 荐购ID
     */
    @ExcelProperty(value = "荐购ID")
    private Long id;

    /**
     * 荐购读者（app_user）
     */
    @ExcelProperty(value = "荐购读者")
    private Long readerId;

    /**
     * 书名
     */
    @ExcelProperty(value = "书名")
    private String title;

    /**
     * 著者
     */
    @ExcelProperty(value = "著者")
    private String author;

    /**
     * ISBN
     */
    @ExcelProperty(value = "ISBN")
    private String isbn;

    /**
     * 荐购理由
     */
    @ExcelProperty(value = "荐购理由")
    private String reason;

    /**
     * 状态：0待受理 1已受理转采购 2已驳回 3已采购
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 处理人（sys_user）
     */
    @ExcelProperty(value = "处理人")
    private Long handleBy;

    /**
     * 处理时间
     */
    @ExcelProperty(value = "处理时间")
    private Date handleTime;

    /**
     * 驳回原因
     */
    @ExcelProperty(value = "驳回原因")
    private String rejectReason;

    /**
     * 创建时间
     */
    private Date createTime;

}

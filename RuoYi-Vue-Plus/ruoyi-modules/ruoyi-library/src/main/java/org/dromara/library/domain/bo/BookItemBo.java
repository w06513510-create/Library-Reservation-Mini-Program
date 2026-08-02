package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.BookItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 馆藏册（册/Item）业务对象 biz_book_item
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BookItem.class, reverseConvertGenerate = false)
public class BookItemBo extends BaseEntity {

    /**
     * 馆藏册ID（册/Item）
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属书目ID（biz_book）
     */
    @NotNull(message = "所属书目不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long bookId;

    /**
     * 条码（每册唯一）
     */
    @NotBlank(message = "条码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String barcode;

    /**
     * 索书号（含别本/种次号）
     */
    private String callNo;

    /**
     * 藏地ID（biz_book_location）
     */
    private Long locationId;

    /**
     * 书架/排架位ID（biz_shelf，寻书定位）
     */
    private Long shelfId;

    /**
     * 状态：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销
     */
    private Integer status;

    /**
     * 注销类型：1剔旧 2报损 3遗失核销（status=6时填）
     */
    private Integer withdrawType;

    /**
     * 注销原因
     */
    private String withdrawReason;

    /**
     * 注销时间
     */
    private Date withdrawTime;

}

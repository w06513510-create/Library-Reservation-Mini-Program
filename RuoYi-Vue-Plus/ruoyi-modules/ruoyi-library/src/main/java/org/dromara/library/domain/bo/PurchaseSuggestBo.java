package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.PurchaseSuggest;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 读者荐购业务对象 biz_purchase_suggest
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PurchaseSuggest.class, reverseConvertGenerate = false)
public class PurchaseSuggestBo extends BaseEntity {

    /**
     * 荐购ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 荐购读者（app_user）
     */
    @NotNull(message = "荐购读者不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long readerId;

    /**
     * 书名
     */
    @NotBlank(message = "书名不能为空", groups = {AddGroup.class, EditGroup.class})
    private String title;

    /**
     * 著者
     */
    private String author;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 荐购理由
     */
    private String reason;

    /**
     * 状态：0待受理 1已受理转采购 2已驳回 3已采购
     */
    private Integer status;

}

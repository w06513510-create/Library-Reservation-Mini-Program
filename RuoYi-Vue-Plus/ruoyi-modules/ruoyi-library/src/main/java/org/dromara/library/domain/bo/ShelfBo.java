package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Shelf;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 书架业务对象 biz_shelf
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Shelf.class, reverseConvertGenerate = false)
public class ShelfBo extends BaseEntity {

    /**
     * 书架ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属藏地ID（biz_book_location）
     */
    @NotNull(message = "所属藏地不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long locationId;

    /**
     * 架号（如 A12）
     */
    @NotBlank(message = "架号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String shelfNo;

    /**
     * 索书号起（排架区间起）
     */
    private String callNoStart;

    /**
     * 索书号止（排架区间止）
     */
    private String callNoEnd;

    /**
     * 平面图X坐标（亮点①寻书）
     */
    private Integer posX;

    /**
     * 平面图Y坐标（亮点①寻书）
     */
    private Integer posY;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

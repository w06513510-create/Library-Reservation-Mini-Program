package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.BookLocation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 藏地业务对象 biz_book_location
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BookLocation.class, reverseConvertGenerate = false)
public class BookLocationBo extends BaseEntity {

    /**
     * 藏地ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 藏地名称（如 三楼社科借阅室）
     */
    @NotBlank(message = "藏地名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String locationName;

    /**
     * 所在楼层ID（biz_floor，寻书平面图定位）
     */
    private Long floorId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

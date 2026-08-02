package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Area;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 区域业务对象 biz_area
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Area.class, reverseConvertGenerate = false)
public class AreaBo extends BaseEntity {

    /**
     * 区域ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    @NotNull(message = "所属楼层不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long floorId;

    /**
     * 区域名称（如 A区自习/研讨区）
     */
    @NotBlank(message = "区域名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String areaName;

    /**
     * 区域类型：0自习阅览 1研讨区 2其它
     */
    private Integer areaType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

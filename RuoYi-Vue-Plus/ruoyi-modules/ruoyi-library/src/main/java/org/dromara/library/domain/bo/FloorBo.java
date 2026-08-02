package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Floor;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 楼层业务对象 biz_floor
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Floor.class, reverseConvertGenerate = false)
public class FloorBo extends BaseEntity {

    /**
     * 楼层ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属场馆ID（biz_venue）
     */
    @NotNull(message = "所属场馆不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long venueId;

    /**
     * 楼层名称（如 三楼社科阅览）
     */
    @NotBlank(message = "楼层名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String floorName;

    /**
     * 楼层号
     */
    private Integer floorNo;

    /**
     * 楼层平面图底图URL（MinIO；选座与寻书共用）
     */
    private String floorPlanUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

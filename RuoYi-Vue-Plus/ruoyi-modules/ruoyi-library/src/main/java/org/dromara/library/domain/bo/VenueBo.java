package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Venue;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 场馆业务对象 biz_venue
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Venue.class, reverseConvertGenerate = false)
public class VenueBo extends BaseEntity {

    /**
     * 场馆ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 场馆名称
     */
    @NotBlank(message = "场馆名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String venueName;

    /**
     * 地址
     */
    private String address;

    /**
     * 开馆时间（HH:mm）
     */
    private String openTime;

    /**
     * 闭馆时间（HH:mm）
     */
    private String closeTime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Desk;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 桌子业务对象 biz_desk
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Desk.class, reverseConvertGenerate = false)
public class DeskBo extends BaseEntity {

    /**
     * 桌子ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    @NotNull(message = "所属区域不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long areaId;

    /**
     * 桌号（如 D01）
     */
    @NotBlank(message = "桌号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String deskNo;

    /**
     * 容量：1单人 2双人 4四人 6六人
     */
    private Integer capacity;

    /**
     * 桌形：0矩形 1圆 2吧台
     */
    private Integer shape;

    /**
     * 平面图X坐标
     */
    private Integer posX;

    /**
     * 平面图Y坐标
     */
    private Integer posY;

    /**
     * 平面图宽度
     */
    private Integer width;

    /**
     * 平面图高度
     */
    private Integer height;

    /**
     * 旋转角度
     */
    private Integer rotation;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

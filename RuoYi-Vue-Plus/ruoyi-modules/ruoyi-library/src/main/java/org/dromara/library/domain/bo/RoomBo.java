package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Room;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 研讨间业务对象 biz_room
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Room.class, reverseConvertGenerate = false)
public class RoomBo extends BaseEntity {

    /**
     * 研讨间ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    @NotNull(message = "所属楼层不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long floorId;

    /**
     * 研讨间名称/编号
     */
    @NotBlank(message = "研讨间名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String roomName;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 预约最少人数
     */
    private Integer minUsers;

    /**
     * 是否需审批：0否 1是
     */
    private Integer needApprove;

    /**
     * 是否需签到：0否 1是
     */
    private Integer needCheckin;

    /**
     * 平面图X坐标
     */
    private Integer posX;

    /**
     * 平面图Y坐标
     */
    private Integer posY;

    /**
     * 状态：0正常 1停用
     */
    private Integer status;

}

package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 研讨间对象 biz_room
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_room")
public class Room extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 研讨间ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    private Long floorId;

    /**
     * 研讨间名称/编号
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

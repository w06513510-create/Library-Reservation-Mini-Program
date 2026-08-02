package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 研讨间预约对象 biz_room_reservation
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_room_reservation")
public class RoomReservation extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 研讨间预约ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 预约读者ID（app_user）
     */
    private Long readerId;

    /**
     * 研讨间ID（biz_room）
     */
    private Long roomId;

    /**
     * 预约日期
     */
    private Date reserveDate;

    /**
     * 时段开始
     */
    private Date startTime;

    /**
     * 时段结束
     */
    private Date endTime;

    /**
     * 使用人数
     */
    private Integer userCount;

    /**
     * 状态：0待审批 1已通过待使用 2使用中 3已完成 4已取消 5已驳回 6已违约
     */
    private Integer status;

    /**
     * 签到时间
     */
    private Date checkInTime;

    /**
     * 审批人
     */
    private Long approveBy;

    /**
     * 审批时间
     */
    private Date approveTime;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

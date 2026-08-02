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
 * 座位预约单对象 biz_reservation
 * 状态机：0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_reservation")
public class Reservation extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 座位预约单ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 读者ID（app_user）
     */
    private Long readerId;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 场馆ID（冗余）
     */
    private Long venueId;

    /**
     * 楼层ID（冗余）
     */
    private Long floorId;

    /**
     * 区域ID（冗余）
     */
    private Long areaId;

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
     * 预约方式：1平面图选座 2快速选座 3现场扫码
     */
    private Integer source;

    /**
     * 状态：0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约
     */
    private Integer status;

    /**
     * 签到时间
     */
    private Date checkInTime;

    /**
     * 本次暂离开始时间
     */
    private Date awayStartTime;

    /**
     * 暂离次数
     */
    private Integer awayCount;

    /**
     * 实际退座/结束时间
     */
    private Date actualEndTime;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 备注（强制释放原因等）
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

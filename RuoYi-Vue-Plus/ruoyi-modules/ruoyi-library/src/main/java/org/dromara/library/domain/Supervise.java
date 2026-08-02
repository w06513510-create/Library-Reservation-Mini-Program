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
 * 占座监督对象 biz_supervise
 * 读者举报某使用中座位无人落座（占座），限时（默认15分钟）内原用户须回座；
 * status：0进行中 1已解除已落座 2超时释放。
 * 超时→违约→释放座位的定时扫描由 SnailJob 另行接入，本对象不含超时逻辑。
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_supervise")
public class Supervise extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 监督ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 被监督的座位预约单ID（biz_reservation）
     */
    private Long reservationId;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 举报读者ID（app_user）
     */
    private Long reporterId;

    /**
     * 举报时间
     */
    private Date reportTime;

    /**
     * 落座截止时间
     */
    private Date deadline;

    /**
     * 状态：0进行中 1已解除已落座 2超时释放
     */
    private Integer status;

    /**
     * 解除时间
     */
    private Date resolveTime;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

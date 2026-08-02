package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 座位对象 biz_seat
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_seat")
public class Seat extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    private Long areaId;

    /**
     * 座位编号（区域内唯一，如 A-012）
     */
    private String seatNo;

    /**
     * 座位类型：0普通 1靠窗 2沙发 3单间
     */
    private Integer seatType;

    /**
     * 有无插座：0无 1有
     */
    private Integer hasPower;

    /**
     * 平面图X坐标
     */
    private Integer posX;

    /**
     * 平面图Y坐标
     */
    private Integer posY;

    /**
     * 桌面二维码标识（扫码签到用）
     */
    private String qrCode;

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

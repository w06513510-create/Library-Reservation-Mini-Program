package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Seat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 座位业务对象 biz_seat
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Seat.class, reverseConvertGenerate = false)
public class SeatBo extends BaseEntity {

    /**
     * 座位ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    @NotNull(message = "所属区域不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long areaId;

    /**
     * 座位编号（区域内唯一，如 A-012）
     */
    @NotBlank(message = "座位编号不能为空", groups = {AddGroup.class, EditGroup.class})
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

}

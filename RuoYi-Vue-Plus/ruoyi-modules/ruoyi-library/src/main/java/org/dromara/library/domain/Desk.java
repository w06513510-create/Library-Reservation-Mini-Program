package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 桌子对象 biz_desk
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_desk")
public class Desk extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 桌子ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属区域ID（biz_area）
     */
    private Long areaId;

    /**
     * 桌号（如 D01）
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 区域对象 biz_area
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_area")
public class Area extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属楼层ID（biz_floor）
     */
    private Long floorId;

    /**
     * 区域名称（如 A区自习/研讨区）
     */
    private String areaName;

    /**
     * 区域类型：0自习阅览 1研讨区 2其它
     */
    private Integer areaType;

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

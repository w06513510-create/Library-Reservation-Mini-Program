package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 楼层对象 biz_floor
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_floor")
public class Floor extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 楼层ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属场馆ID（biz_venue）
     */
    private Long venueId;

    /**
     * 楼层名称（如 三楼社科阅览）
     */
    private String floorName;

    /**
     * 楼层号
     */
    private Integer floorNo;

    /**
     * 楼层平面图底图URL（MinIO；选座与寻书共用）
     */
    private String floorPlanUrl;

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

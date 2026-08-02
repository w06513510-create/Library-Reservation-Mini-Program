package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 场馆对象 biz_venue
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_venue")
public class Venue extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 场馆ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 场馆名称
     */
    private String venueName;

    /**
     * 地址
     */
    private String address;

    /**
     * 开馆时间（HH:mm）
     */
    private String openTime;

    /**
     * 闭馆时间（HH:mm）
     */
    private String closeTime;

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

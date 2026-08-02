package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 藏地对象 biz_book_location
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_book_location")
public class BookLocation extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 藏地ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 藏地名称（如 三楼社科借阅室）
     */
    private String locationName;

    /**
     * 所在楼层ID（biz_floor，寻书平面图定位）
     */
    private Long floorId;

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

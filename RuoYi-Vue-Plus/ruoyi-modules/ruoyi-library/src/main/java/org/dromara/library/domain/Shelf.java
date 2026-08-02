package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 书架对象 biz_shelf
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_shelf")
public class Shelf extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 书架ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属藏地ID（biz_book_location）
     */
    private Long locationId;

    /**
     * 架号（如 A12）
     */
    private String shelfNo;

    /**
     * 索书号起（排架区间起）
     */
    private String callNoStart;

    /**
     * 索书号止（排架区间止）
     */
    private String callNoEnd;

    /**
     * 平面图X坐标（亮点①寻书）
     */
    private Integer posX;

    /**
     * 平面图Y坐标（亮点①寻书）
     */
    private Integer posY;

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

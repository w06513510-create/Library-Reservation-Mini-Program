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
 * 读者荐购对象 biz_purchase_suggest
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_suggest")
public class PurchaseSuggest extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 荐购ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 荐购读者（app_user）
     */
    private Long readerId;

    /**
     * 书名
     */
    private String title;

    /**
     * 著者
     */
    private String author;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 荐购理由
     */
    private String reason;

    /**
     * 状态：0待受理 1已受理转采购 2已驳回 3已采购
     */
    private Integer status;

    /**
     * 处理人（sys_user）
     */
    private Long handleBy;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

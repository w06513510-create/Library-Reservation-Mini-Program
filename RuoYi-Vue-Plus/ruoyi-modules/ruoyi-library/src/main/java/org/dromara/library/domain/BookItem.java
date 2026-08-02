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
 * 馆藏册（册/Item）对象 biz_book_item
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_book_item")
public class BookItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 馆藏册ID（册/Item）
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属书目ID（biz_book）
     */
    private Long bookId;

    /**
     * 条码（每册唯一）
     */
    private String barcode;

    /**
     * 索书号（含别本/种次号）
     */
    private String callNo;

    /**
     * 藏地ID（biz_book_location）
     */
    private Long locationId;

    /**
     * 书架/排架位ID（biz_shelf，寻书定位）
     */
    private Long shelfId;

    /**
     * 状态：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销
     */
    private Integer status;

    /**
     * 注销类型：1剔旧 2报损 3遗失核销（status=6时填）
     */
    private Integer withdrawType;

    /**
     * 注销原因
     */
    private String withdrawReason;

    /**
     * 注销时间
     */
    private Date withdrawTime;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

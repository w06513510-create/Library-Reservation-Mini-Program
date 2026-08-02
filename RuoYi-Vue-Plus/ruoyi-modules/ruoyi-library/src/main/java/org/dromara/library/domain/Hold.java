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
 * 图书预约(hold)对象 biz_hold
 * 状态：0排队中 1到书保留(在预约架) 2已取书 3已取消 4过期释放
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_hold")
public class Hold extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 读者ID（app_user） */
    private Long readerId;

    /** 书目ID */
    private Long bookId;

    /** 到书保留的馆藏册ID（到书后指定） */
    private Long itemId;

    /** 队列位次 */
    private Integer queueNo;

    /** 状态：0排队中 1到书保留 2已取书 3已取消 4过期释放 */
    private Integer status;

    /** 预约时间 */
    private Date holdTime;

    /** 到书时间 */
    private Date readyTime;

    /** 预约架保留期截止 */
    private Date holdDeadline;

    /** 取书时间 */
    private Date pickupTime;

    /** 取消时间 */
    private Date cancelTime;

    /** 删除标志（0存在 1删除） */
    @TableLogic
    private String delFlag;

}

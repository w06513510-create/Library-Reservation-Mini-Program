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
 * 借阅单对象 biz_loan
 * 状态：0在借 1已还 2逾期(在借且超期)
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_loan")
public class Loan extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 读者ID（app_user） */
    private Long readerId;

    /** 馆藏册ID */
    private Long itemId;

    /** 书目ID（冗余） */
    private Long bookId;

    /** 借出时间 */
    private Date borrowTime;

    /** 应还日期 */
    private Date dueTime;

    /** 已续借次数 */
    private Integer renewCount;

    /** 归还时间 */
    private Date returnTime;

    /** 状态：0在借 1已还 2逾期 */
    private Integer status;

    /** 是否曾逾期：0否 1是 */
    private Integer overdueFlag;

    /** 是否被预约催还：0否 1是 */
    private Integer recallFlag;

    /** 催还时间 */
    private Date recallTime;

    /** 借出藏地（通借通还预留） */
    private Long borrowLocation;

    /** 归还藏地（通借通还预留） */
    private Long returnLocation;

    /** 经办流通员（sys_user） */
    private Long operatorId;

    /** 删除标志（0存在 1删除） */
    @TableLogic
    private String delFlag;

}

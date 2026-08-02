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
 * 违约记录对象 biz_violation
 * 类型：1座位爽约 2暂离超时 3监督未落座 4未签退 5图书逾期 6预约架超期 7遗失损坏
 * 状态：0有效 1已申诉解除
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_violation")
public class Violation extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 读者ID（app_user） */
    private Long readerId;

    /** 违约类型 1-7 */
    private Integer violationType;

    /** 关联业务类型 */
    private String bizType;

    /** 关联业务ID */
    private Long bizId;

    /** 扣分 */
    private Integer deductScore;

    /** 发生时间 */
    private Date occurTime;

    /** 来源：0系统判定 1管理员登记 */
    private Integer source;

    /** 状态：0有效 1已申诉解除 */
    private Integer status;

    @TableLogic
    private String delFlag;

}

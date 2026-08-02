package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 信用流水对象 biz_credit_log（append-only，无 del_flag）
 * 一致性不变式：reader 当前分 = clamp(Σ delta, 0, 100)
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_credit_log")
public class CreditLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 信用流水ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 读者ID（app_user）
     */
    private Long readerId;

    /**
     * 本次变动分值（带符号 +/-）
     */
    private Integer delta;

    /**
     * 事由：1建档 2座位爽约 3暂离超时 4监督未落座 5未签退 6图书逾期 7预约架超期 8遗失损坏 9履约加分 10时间衰减 11申诉冲正 12黑名单校准
     */
    private Integer reasonType;

    /**
     * 事由说明
     */
    private String reasonDesc;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 变动后信用分（= clamp(截至本条 Σdelta, 0, 100)，供查库对平）
     */
    private Integer scoreAfter;

}

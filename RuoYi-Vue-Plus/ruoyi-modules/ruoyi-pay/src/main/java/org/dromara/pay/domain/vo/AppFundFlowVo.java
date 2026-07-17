package org.dromara.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.pay.domain.AppFundFlow;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 资金流水视图对象 app_fund_flow
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppFundFlow.class)
public class AppFundFlowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long id;

    /** 所属 C端用户ID */
    private Long userId;

    /** 方向: 1入 2出 */
    private Integer direction;

    /** 变动额(恒正) */
    private BigDecimal amount;

    /** 本次操作后可用余额 */
    private BigDecimal balanceAfter;

    /** 业务类型 */
    private String bizType;

    /** 业务单号 */
    private String bizNo;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

}

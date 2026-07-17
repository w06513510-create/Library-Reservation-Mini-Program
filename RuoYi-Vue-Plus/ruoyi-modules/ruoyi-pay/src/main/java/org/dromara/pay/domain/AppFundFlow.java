package org.dromara.pay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 资金流水 app_fund_flow（append-only：只增不改不删）。
 * <p>{@code direction} 记录的是对可用余额 {@code balance} 的影响(1入/2出)，{@code amount} 恒正。
 * 对平不变式：{@code balance == Σ(入的amount) − Σ(出的amount)}。
 * {@code idempotent_no} 唯一键做幂等硬兜底。
 *
 * @author ruoyi-template
 */
@Data
@TableName("app_fund_flow")
public class AppFundFlow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    @TableId(value = "id")
    private Long id;

    /** 所属 C端用户ID */
    private Long userId;

    /** 方向: 1入(可用增加) 2出(可用减少) */
    private Integer direction;

    /** 变动额(恒正) */
    private BigDecimal amount;

    /** 本次操作后可用余额 balance */
    private BigDecimal balanceAfter;

    /** 业务类型(recharge/deduct/freeze/unfreeze/...) */
    private String bizType;

    /** 业务单号 */
    private String bizNo;

    /** 幂等键(唯一键, 防重) */
    private String idempotentNo;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Date createTime;

}

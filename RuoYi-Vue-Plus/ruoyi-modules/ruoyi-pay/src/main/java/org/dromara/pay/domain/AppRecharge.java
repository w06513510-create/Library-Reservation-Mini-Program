package org.dromara.pay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值单 app_recharge。
 * <p>{@code out_trade_no} 商户订单号(唯一键)。到账以支付宝查单结果为准；
 * 未配置支付宝时走 {@code channel=simulate} 模拟到账。状态流转 0待支付→1已到账/2已关闭。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_recharge")
public class AppRecharge extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 充值单ID */
    @TableId(value = "id")
    private Long id;

    /** 所属 C端用户ID */
    private Long userId;

    /** 商户订单号(唯一键) */
    private String outTradeNo;

    /** 充值金额 */
    private BigDecimal amount;

    /** 状态: 0待支付 1已到账 2已关闭 */
    private Integer status;

    /** 支付渠道: alipay / simulate */
    private String channel;

    /** 支付宝交易号 */
    private String tradeNo;

    /** 订单标题 */
    private String subject;

    /** 到账时间 */
    private Date payTime;

    /** 最近查单时间 */
    private Date queryTime;

}

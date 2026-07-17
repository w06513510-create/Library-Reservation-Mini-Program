package org.dromara.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.pay.domain.AppRecharge;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值单视图对象 app_recharge
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppRecharge.class)
public class AppRechargeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 充值单ID */
    private Long id;

    /** 所属 C端用户ID */
    private Long userId;

    /** 商户订单号 */
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

    /** 创建时间 */
    private Date createTime;

}

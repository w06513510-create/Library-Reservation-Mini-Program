package org.dromara.pay.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建充值单返回对象。
 * <p>已配置支付宝：{@code alipayConfigured=true}、{@code payForm} 为 trade.page.pay 返回的支付表单 HTML，前端直接渲染跳转。
 * <p>未配置支付宝：{@code alipayConfigured=false}、{@code payForm=null}，前端改调 {@code /app/recharge/simulatePaid} 模拟到账。
 *
 * @author ruoyi-template
 */
@Data
public class AppRechargeCreateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商户订单号 */
    private String outTradeNo;

    /** 支付渠道: alipay / simulate */
    private String channel;

    /** 是否已配置支付宝(true 走真实支付, false 走模拟到账) */
    private Boolean alipayConfigured;

    /** 支付表单 HTML(仅 alipayConfigured=true 时非空) */
    private String payForm;

}

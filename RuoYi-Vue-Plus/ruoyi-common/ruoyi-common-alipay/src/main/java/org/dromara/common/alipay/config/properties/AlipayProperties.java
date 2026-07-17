package org.dromara.common.alipay.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付宝配置属性（绑定 application-alipay.yml 的 alipay 前缀，沙箱/正式通用）
 *
 * @author ruoyi
 */
@Data
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 应用 AppID
     */
    private String appId;

    /**
     * 网关地址（沙箱：https://openapi-sandbox.dl.alipaydev.com/gateway.do ；正式：https://openapi.alipay.com/gateway.do）
     */
    private String gateway;

    /**
     * 签名算法，固定 RSA2
     */
    private String signType = "RSA2";

    /**
     * 数据格式，固定 json
     */
    private String format = "json";

    /**
     * 编码，固定 utf-8
     */
    private String charset = "utf-8";

    /**
     * 支付成功同步跳回地址
     */
    private String returnUrl;

    /**
     * 支付结果异步通知地址
     */
    private String notifyUrl;

    /**
     * 应用私钥（沙箱-RSA2 应用私钥）
     */
    private String appPrivateKey;

    /**
     * 支付宝公钥（沙箱-支付宝公钥）
     */
    private String alipayPublicKey;

}

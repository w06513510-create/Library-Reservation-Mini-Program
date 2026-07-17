package org.dromara.common.alipay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.dromara.common.alipay.config.properties.AlipayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 支付宝客户端自动配置。
 * <p>
 * 仅当配置了 {@code alipay.app-id} 时才装配 {@link AlipayClient}，
 * 因此缺少沙箱配置不影响项目启动。业务模块注入 {@link AlipayClient} 即可下单 / 查单 / 退款。
 *
 * @author ruoyi
 */
@AutoConfiguration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfig {

    @Bean
    @ConditionalOnProperty(prefix = "alipay", name = "app-id")
    public AlipayClient alipayClient(AlipayProperties properties) {
        return new DefaultAlipayClient(
            properties.getGateway(),
            properties.getAppId(),
            properties.getAppPrivateKey(),
            properties.getFormat(),
            properties.getCharset(),
            properties.getAlipayPublicKey(),
            properties.getSignType());
    }

}

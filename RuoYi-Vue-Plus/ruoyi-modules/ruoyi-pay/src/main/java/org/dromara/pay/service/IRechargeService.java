package org.dromara.pay.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.pay.domain.vo.AppRechargeCreateVo;
import org.dromara.pay.domain.vo.AppRechargeVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 充值 Service（支付宝沙箱：下单 → 查单为准 → 通知验签；未配置时提供模拟到账降级）。
 *
 * @author ruoyi-template
 */
public interface IRechargeService {

    /**
     * 支付宝是否已配置(装配了 AlipayClient)。
     */
    boolean isAlipayConfigured();

    /**
     * 创建充值单。已配置支付宝→返回支付表单 HTML；未配置→返回 alipayConfigured=false 供前端走模拟到账。
     */
    AppRechargeCreateVo createRecharge(Long userId, BigDecimal amount);

    /**
     * 主动查单结算(以支付宝 trade.query 结果为准)，幂等入账。
     *
     * @return 是否已支付
     */
    boolean queryAndSettle(String outTradeNo);

    /**
     * 模拟即时到账(降级)：仅当支付宝未配置时可用。校验单归属后直接结算入账。
     */
    void simulatePaid(Long userId, String outTradeNo);

    /**
     * 处理支付宝异步通知：验签通过且交易成功→触发查单结算(仍以查单为准)。
     *
     * @return "success" / "failure"
     */
    String handleNotify(Map<String, String> params);

    /**
     * 我的充值单分页。
     */
    TableDataInfo<AppRechargeVo> pageRecharges(Long userId, PageQuery pageQuery);

}

package org.dromara.pay.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.pay.domain.vo.AppRechargeCreateVo;
import org.dromara.pay.domain.vo.AppRechargeVo;
import org.dromara.pay.service.IRechargeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * C端充值 Controller —— 创建充值单 / 主动查单结算 / 模拟到账(降级) / 我的充值单 / 支付宝异步通知。
 * <p>除 {@code /notify}(支付宝服务器回调, 已在 application.yml security.excludes 放行, 无 token)外均 {@link SaCheckLogin}。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/recharge")
public class AppRechargeController {

    private final IRechargeService rechargeService;

    /** 创建充值单：已配支付宝→返回支付表单 HTML；未配→alipayConfigured=false 走模拟到账 */
    @SaCheckLogin
    @PostMapping("/create")
    public R<AppRechargeCreateVo> create(@RequestParam BigDecimal amount) {
        return R.ok(rechargeService.createRecharge(AppLoginHelper.getUserId(), amount));
    }

    /** 主动查单结算(以支付宝 trade.query 为准, 幂等)，返回是否已支付 */
    @SaCheckLogin
    @GetMapping("/query")
    public R<Boolean> query(@RequestParam String outTradeNo) {
        return R.ok(rechargeService.queryAndSettle(outTradeNo));
    }

    /** 模拟即时到账(仅未配置支付宝时可用)：用于无沙箱凭证时验证 充值→对平 */
    @SaCheckLogin
    @PostMapping("/simulatePaid")
    public R<Void> simulatePaid(@RequestParam String outTradeNo) {
        rechargeService.simulatePaid(AppLoginHelper.getUserId(), outTradeNo);
        return R.ok();
    }

    /** 我的充值单分页 */
    @SaCheckLogin
    @GetMapping("/page")
    public TableDataInfo<AppRechargeVo> page(PageQuery pageQuery) {
        return rechargeService.pageRecharges(AppLoginHelper.getUserId(), pageQuery);
    }

    /**
     * 支付宝异步通知回调(无 token, 已放行)。验签通过且交易成功→查单结算(幂等)。
     * 必须返回纯文本 "success"/"failure"，否则支付宝会重复回调。
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v != null && v.length > 0 ? v[0] : ""));
        return rechargeService.handleNotify(params);
    }

}

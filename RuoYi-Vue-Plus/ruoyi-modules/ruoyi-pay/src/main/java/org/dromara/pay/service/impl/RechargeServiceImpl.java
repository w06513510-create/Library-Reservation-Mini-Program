package org.dromara.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.alipay.config.properties.AlipayProperties;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.pay.domain.AppRecharge;
import org.dromara.pay.domain.vo.AppRechargeCreateVo;
import org.dromara.pay.domain.vo.AppRechargeVo;
import org.dromara.pay.mapper.AppRechargeMapper;
import org.dromara.pay.service.IRechargeService;
import org.dromara.pay.service.IWalletService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 充值 Service 实现（支付宝沙箱：下单 → 查单为准 → 通知验签；未配置降级模拟到账）。
 * <p>{@link AlipayClient} 用 {@link ObjectProvider} 惰性取：未配置支付宝(缺 app-id)时后端照常启动，
 * {@code getIfAvailable()==null} 即"未配置"，此时提供 {@link #simulatePaid} 让 充值→对平 离线可验证。
 * <p>到账一律走 {@link IWalletService#recharge}(idempotentNo=out_trade_no)。所有 DB 操作包
 * {@link TenantHelper#ignore}：notify 回调无登录=无租户上下文，且充值单/钱包均按业务键归属。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class RechargeServiceImpl implements IRechargeService {

    private static final int STATUS_UNPAID = 0;
    private static final int STATUS_PAID = 1;

    private static final String CHANNEL_ALIPAY = "alipay";
    private static final String CHANNEL_SIMULATE = "simulate";

    private static final String BIZ_TYPE = "recharge";
    private static final String SUBJECT = "钱包充值";

    private final AppRechargeMapper baseMapper;
    private final IWalletService walletService;
    private final AlipayProperties alipayProperties;
    private final ObjectProvider<AlipayClient> alipayClientProvider;

    @Override
    public boolean isAlipayConfigured() {
        return alipayClientProvider.getIfAvailable() != null;
    }

    private AlipayClient client() {
        AlipayClient c = alipayClientProvider.getIfAvailable();
        if (c == null) {
            throw new ServiceException("支付宝未配置(application-alipay.yml 缺 app-id)，请改用模拟到账 /app/recharge/simulatePaid");
        }
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppRechargeCreateVo createRecharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ServiceException("充值金额必须大于0");
        }
        boolean configured = isAlipayConfigured();
        String outTradeNo = "RC" + IdUtil.getSnowflakeNextId();

        AppRecharge r = new AppRecharge();
        r.setUserId(userId);
        r.setOutTradeNo(outTradeNo);
        r.setAmount(amount);
        r.setStatus(STATUS_UNPAID);
        r.setChannel(configured ? CHANNEL_ALIPAY : CHANNEL_SIMULATE);
        r.setSubject(SUBJECT);
        TenantHelper.ignore(() -> {
            baseMapper.insert(r);
        });

        AppRechargeCreateVo vo = new AppRechargeCreateVo();
        vo.setOutTradeNo(outTradeNo);
        vo.setAlipayConfigured(configured);
        vo.setChannel(configured ? CHANNEL_ALIPAY : CHANNEL_SIMULATE);
        if (configured) {
            vo.setPayForm(buildPayForm(outTradeNo, amount));
        }
        return vo;
    }

    private String buildPayForm(String outTradeNo, BigDecimal amount) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());
        Map<String, Object> biz = Map.of(
            "out_trade_no", outTradeNo,
            "total_amount", amount.toPlainString(),
            "subject", SUBJECT,
            "product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(JsonUtils.toJsonString(biz));
        try {
            AlipayTradePagePayResponse resp = client().pageExecute(request, "GET");
            return resp.getBody();
        } catch (AlipayApiException e) {
            throw new ServiceException("发起支付宝支付失败：" + e.getErrMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean queryAndSettle(String outTradeNo) {
        // 查单为准
        boolean paid;
        String tradeNo;
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(JsonUtils.toJsonString(Map.of("out_trade_no", outTradeNo)));
        try {
            AlipayTradeQueryResponse resp = client().execute(request);
            String ts = resp.getTradeStatus();
            paid = resp.isSuccess() && ("TRADE_SUCCESS".equals(ts) || "TRADE_FINISHED".equals(ts));
            tradeNo = resp.getTradeNo();
        } catch (AlipayApiException e) {
            throw new ServiceException("查询支付宝订单失败：" + e.getErrMsg());
        }
        settle(outTradeNo, paid, CHANNEL_ALIPAY, tradeNo, "支付宝充值到账");
        return paid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePaid(Long userId, String outTradeNo) {
        if (isAlipayConfigured()) {
            throw new ServiceException("已配置支付宝，请走真实支付(create→query)，不可模拟到账");
        }
        AppRecharge r = TenantHelper.ignore(() -> baseMapper.selectOne(
            Wrappers.<AppRecharge>lambdaQuery().eq(AppRecharge::getOutTradeNo, outTradeNo)));
        if (r == null) {
            throw new ServiceException("充值单不存在");
        }
        if (!r.getUserId().equals(userId)) {
            throw new ServiceException("无权操作该充值单");
        }
        settle(outTradeNo, true, CHANNEL_SIMULATE, null, "模拟充值到账");
    }

    /**
     * 结算：仅 paid 时用 CAS {@code markPaid} 抢占结算权(status 0→1)，抢到者调 walletService.recharge 入账。
     * 幂等：markPaid 只会成功一次 + 流水唯一键(idempotentNo=outTradeNo)双层兜底。
     */
    private void settle(String outTradeNo, boolean paid, String channel, String tradeNo, String remark) {
        if (!paid) {
            return;
        }
        AppRecharge r = TenantHelper.ignore(() -> baseMapper.selectOne(
            Wrappers.<AppRecharge>lambdaQuery().eq(AppRecharge::getOutTradeNo, outTradeNo)));
        if (r == null) {
            throw new ServiceException("充值单不存在");
        }
        Date now = new Date();
        int won = TenantHelper.ignore(() -> baseMapper.markPaid(outTradeNo, channel, tradeNo, now, now));
        if (won == 1) {
            walletService.recharge(r.getUserId(), r.getAmount(), BIZ_TYPE, outTradeNo, outTradeNo, remark);
        }
    }

    @Override
    public String handleNotify(Map<String, String> params) {
        try {
            boolean signOk = AlipaySignature.rsaCheckV1(params,
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getCharset(),
                alipayProperties.getSignType());
            if (!signOk) {
                return "failure";
            }
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            if (StringUtils.isNotBlank(outTradeNo)
                && ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))) {
                // 以查单结果为准结算(幂等)，不凭 notify 直接入账
                queryAndSettle(outTradeNo);
            }
            return "success";
        } catch (AlipayApiException e) {
            return "failure";
        }
    }

    @Override
    public TableDataInfo<AppRechargeVo> pageRecharges(Long userId, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> {
            Page<AppRechargeVo> page = baseMapper.selectVoPage(pageQuery.build(),
                Wrappers.<AppRecharge>lambdaQuery()
                    .eq(AppRecharge::getUserId, userId)
                    .orderByDesc(AppRecharge::getId));
            return TableDataInfo.build(page);
        });
    }

}

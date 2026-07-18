package org.dromara.test;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.message.domain.AppNotification;
import org.dromara.message.mapper.AppNotificationMapper;
import org.dromara.message.service.INotificationService;
import org.dromara.message.utils.NotificationHelper;
import org.dromara.pay.domain.AppFundFlow;
import org.dromara.pay.domain.AppRecharge;
import org.dromara.pay.domain.AppWallet;
import org.dromara.pay.domain.vo.AppRechargeCreateVo;
import org.dromara.pay.domain.vo.AppWalletVo;
import org.dromara.pay.mapper.AppFundFlowMapper;
import org.dromara.pay.mapper.AppRechargeMapper;
import org.dromara.pay.mapper.AppWalletMapper;
import org.dromara.pay.service.IRechargeService;
import org.dromara.pay.service.IWalletService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 钱包对平引擎(ruoyi-pay) + 站内通知(ruoyi-message) 服务级集成验证。
 * <p>覆盖 HTTP 层没暴露的服务级 API：{@code deduct/freeze/unfreeze} 与 {@code NotificationHelper.send}。
 * 全程校验对平不变式 {@code balance == Σ入 − Σ出}(checkInvariant==0)。
 * <p>用合成 userId(无需真实 app_user、无外键)，{@code webEnvironment=NONE} 不占端口(与本机 8199 互不干扰)，
 * {@code @AfterAll} 清理测试数据。
 *
 * @author ruoyi-template
 */
@Tag("dev")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("钱包对平引擎 + 站内通知 集成验证")
public class WalletEngineVerifyTest {

    @Autowired
    private IWalletService walletService;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private AppWalletMapper walletMapper;
    @Autowired
    private AppFundFlowMapper fundFlowMapper;
    @Autowired
    private AppNotificationMapper notificationMapper;
    @Autowired
    private IRechargeService rechargeService;
    @Autowired
    private AppRechargeMapper rechargeMapper;

    /** 合成用户ID(唯一, 避开真实雪花ID; System.currentTimeMillis 在测试代码里可用) */
    private static final Long UID = 900_000_000_000_000_000L + (System.currentTimeMillis() % 1_000_000_000L);

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private AppWalletVo vo() {
        return walletService.getWalletVo(UID);
    }

    private void assertInvariantZero() {
        assertEquals(0, walletService.checkInvariant(UID).compareTo(BigDecimal.ZERO),
            "对平不变式必须恒为 0");
    }

    @Test
    @Order(1)
    @DisplayName("充值入账 + 幂等键防重复")
    void recharge() {
        walletService.getOrCreate(UID);
        walletService.recharge(UID, bd("200"), "recharge", "T1", "IDEM-R1", "首次充值");
        assertEquals(0, vo().getBalance().compareTo(bd("200")), "充值后可用余额应为 200");
        // 相同幂等键重复充值 → 不应重复入账
        walletService.recharge(UID, bd("200"), "recharge", "T1", "IDEM-R1", "重复(应被幂等拦截)");
        assertEquals(0, vo().getBalance().compareTo(bd("200")), "相同幂等键不应重复入账");
        assertInvariantZero();
    }

    @Test
    @Order(2)
    @DisplayName("冻结 / 解冻")
    void freezeAndUnfreeze() {
        walletService.freeze(UID, bd("50"), "order", "O1", "IDEM-F1", "下单冻结");
        assertEquals(0, vo().getBalance().compareTo(bd("150")), "冻结后可用应为 150");
        assertEquals(0, vo().getFrozen().compareTo(bd("50")), "冻结额应为 50");
        assertInvariantZero();

        walletService.unfreeze(UID, bd("50"), "order", "O1", "IDEM-U1", "取消解冻");
        assertEquals(0, vo().getBalance().compareTo(bd("200")), "解冻后可用应还原为 200");
        assertEquals(0, vo().getFrozen().compareTo(BigDecimal.ZERO), "冻结额应归零");
        assertInvariantZero();
    }

    @Test
    @Order(3)
    @DisplayName("扣款 + CAS 防超扣")
    void deductAndOverdraftGuard() {
        walletService.deduct(UID, bd("30"), "order", "O2", "IDEM-D1", "支付扣款");
        assertEquals(0, vo().getBalance().compareTo(bd("170")), "扣款后可用应为 170");
        assertInvariantZero();

        // 超额扣款应失败(CAS 影响行数=0 抛异常)，且余额不变、对平不破
        assertThrows(Exception.class,
            () -> walletService.deduct(UID, bd("99999"), "order", "O3", "IDEM-D2", "超额扣款"),
            "超额扣款应抛异常");
        assertEquals(0, vo().getBalance().compareTo(bd("170")), "超额扣款失败后余额不应改变");
        assertInvariantZero();
    }

    @Test
    @Order(4)
    @DisplayName("NotificationHelper.send 真实发送链路")
    void notificationSend() {
        long before = notificationService.unreadCount(UID);
        Long nid = NotificationHelper.send(UID, "订单已发货", "您的订单#1001已发货", "order", 1001L);
        assertNotNull(nid, "send 应返回新通知ID");
        assertEquals(before + 1, notificationService.unreadCount(UID), "发送后未读数应 +1");
        assertTrue(notificationService.markRead(UID, nid), "标记已读应成功");
        assertEquals(before, notificationService.unreadCount(UID), "标记已读后未读数应还原");
    }

    @Test
    @Order(5)
    @DisplayName("支付宝真实下单（复用沙箱配置签名生成支付表单）")
    void alipayRealCreateOrder() {
        // 未配置 application-alipay.yml 时跳过（本用例依赖真实沙箱配置；基座默认可无此文件）
        assumeTrue(rechargeService.isAlipayConfigured(), "未配置支付宝(application-alipay.yml)，跳过真实下单验证");
        walletService.getOrCreate(UID);
        AppRechargeCreateVo vo = rechargeService.createRecharge(UID, bd("100"));
        assertTrue(Boolean.TRUE.equals(vo.getAlipayConfigured()), "应走真实支付宝渠道");
        assertEquals("alipay", vo.getChannel(), "渠道应为 alipay");
        assertNotNull(vo.getPayForm(), "应返回签名后的支付表单 HTML");
        // trade.page.pay 返回的是自动提交到支付宝网关的 HTML form（能生成即证明沙箱私钥可签名）
        assertTrue(vo.getPayForm().toLowerCase().contains("form")
            && vo.getPayForm().contains("alipay"), "支付表单应含提交到支付宝网关的 form");
    }

    @AfterAll
    void cleanup() {
        TenantHelper.ignore(() -> {
            fundFlowMapper.delete(Wrappers.<AppFundFlow>lambdaQuery().eq(AppFundFlow::getUserId, UID));
            walletMapper.delete(Wrappers.<AppWallet>lambdaQuery().eq(AppWallet::getUserId, UID));
            rechargeMapper.delete(Wrappers.<AppRecharge>lambdaQuery().eq(AppRecharge::getUserId, UID));
            notificationMapper.delete(Wrappers.<AppNotification>lambdaQuery().eq(AppNotification::getReceiverId, UID));
            return null;
        });
    }

}

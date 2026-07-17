package org.dromara.pay.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.pay.domain.vo.AppFundFlowVo;
import org.dromara.pay.domain.vo.AppWalletVo;
import org.dromara.pay.service.IWalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * C端钱包 Controller —— 我的钱包 / 资金流水分页 / 对平自检。
 * <p>均 {@link SaCheckLogin} + {@link AppLoginHelper#getUserId()} 强制按当前 C端用户归属，不接收外部 userId。
 * 动钱(扣款/冻结/解冻)不出 C端接口，仅作 {@link IWalletService} 服务级 API 供业务模块调用。
 *
 * @author ruoyi-template
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/wallet")
public class AppWalletController {

    private final IWalletService walletService;

    /** 我的钱包(不存在则初始化) */
    @GetMapping("/me")
    public R<AppWalletVo> me() {
        return R.ok(walletService.getWalletVo(AppLoginHelper.getUserId()));
    }

    /** 我的资金流水分页 */
    @GetMapping("/flow/page")
    public TableDataInfo<AppFundFlowVo> flowPage(PageQuery pageQuery) {
        return walletService.pageFlows(AppLoginHelper.getUserId(), pageQuery);
    }

    /** 我的钱包对平自检：返回差额(0 为平) */
    @GetMapping("/checkInvariant")
    public R<BigDecimal> checkInvariant() {
        return R.ok("查询成功", walletService.checkInvariant(AppLoginHelper.getUserId()));
    }

}

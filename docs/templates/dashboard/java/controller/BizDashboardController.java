package org.dromara.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.biz.domain.vo.BizDashboardVo;
import org.dromara.biz.service.IBizDashboardService;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 看板 Controller —— /overview 一次性下发全部卡片 + 图表数据。
 * <p>无分页、无入参（若要时间范围筛选，加 {@code @RequestParam} beginTime/endTime 传到 mapper）。
 *
 * @author ruoyi-template
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/biz/dashboard")
public class BizDashboardController extends BaseController {

    private final IBizDashboardService bizDashboardService;

    /** 看板聚合数据 */
    @SaCheckPermission("biz:dashboard:view")
    @GetMapping("/overview")
    public R<BizDashboardVo> overview() {
        return R.ok(bizDashboardService.getDashboardData());
    }

}

package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.vo.DashboardVo;
import org.dromara.library.service.IDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据可视化大屏Controller（亮点③）
 *
 * @author library
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/dashboard")
public class DashboardController extends BaseController {

    private final IDashboardService dashboardService;

    /** 大屏概览指标 */
    @SaCheckPermission("library:dashboard:list")
    @GetMapping("/overview")
    public R<DashboardVo> overview() {
        return R.ok(dashboardService.overview());
    }

}

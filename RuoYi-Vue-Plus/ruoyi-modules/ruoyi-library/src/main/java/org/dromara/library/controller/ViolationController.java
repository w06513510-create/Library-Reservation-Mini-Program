package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.ViolationBo;
import org.dromara.library.domain.vo.ViolationVo;
import org.dromara.library.service.IViolationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 违约记录Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/violation")
public class ViolationController extends BaseController {

    private final IViolationService violationService;

    @SaCheckPermission("library:violation:list")
    @GetMapping("/list")
    public TableDataInfo<ViolationVo> list(@Validated(QueryGroup.class) ViolationBo bo, PageQuery pageQuery) {
        return violationService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("library:violation:query")
    @GetMapping("/{id}")
    public R<ViolationVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(violationService.queryById(id));
    }

    /** 管理员登记违约（自动扣分 + 黑名单判定） */
    @SaCheckPermission("library:violation:add")
    @Log(title = "违约登记", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ViolationBo bo) {
        return toAjax(violationService.addByBo(bo));
    }

    @SaCheckPermission("library:violation:export")
    @Log(title = "违约记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ViolationBo bo, HttpServletResponse response) {
        List<ViolationVo> list = violationService.queryList(bo);
        ExcelUtil.exportExcel(list, "违约记录", ViolationVo.class, response);
    }

}

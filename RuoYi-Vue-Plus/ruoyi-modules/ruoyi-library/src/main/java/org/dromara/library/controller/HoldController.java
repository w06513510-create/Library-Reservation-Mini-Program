package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.HoldBo;
import org.dromara.library.domain.vo.HoldVo;
import org.dromara.library.service.IHoldService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 图书预约(hold)Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/hold")
public class HoldController extends BaseController {

    private final IHoldService holdService;

    @SaCheckPermission("library:hold:list")
    @GetMapping("/list")
    public TableDataInfo<HoldVo> list(@Validated(QueryGroup.class) HoldBo bo, PageQuery pageQuery) {
        return holdService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("library:hold:query")
    @GetMapping("/{id}")
    public R<HoldVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(holdService.queryById(id));
    }

    /** 预约（排队） */
    @SaCheckPermission("library:hold:add")
    @Log(title = "图书预约", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HoldBo bo) {
        return toAjax(holdService.createHold(bo));
    }

    /** 取书（到书保留→已取书，生成借阅单） */
    @SaCheckPermission("library:hold:manage")
    @Log(title = "预约取书", businessType = BusinessType.UPDATE)
    @PutMapping("/pickup/{id}")
    public R<Void> pickup(@PathVariable Long id) {
        return toAjax(holdService.pickup(id));
    }

    /** 取消预约 */
    @SaCheckPermission("library:hold:manage")
    @Log(title = "取消图书预约", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        return toAjax(holdService.cancelHold(id));
    }

}

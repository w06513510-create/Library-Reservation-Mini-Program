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
import org.dromara.library.domain.bo.BlacklistBo;
import org.dromara.library.domain.vo.BlacklistVo;
import org.dromara.library.service.IBlacklistService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 黑名单Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/blacklist")
public class BlacklistController extends BaseController {

    private final IBlacklistService blacklistService;

    @SaCheckPermission("library:blacklist:list")
    @GetMapping("/list")
    public TableDataInfo<BlacklistVo> list(@Validated(QueryGroup.class) BlacklistBo bo, PageQuery pageQuery) {
        return blacklistService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("library:blacklist:query")
    @GetMapping("/{id}")
    public R<BlacklistVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(blacklistService.queryById(id));
    }

    /** 手动加入黑名单 */
    @SaCheckPermission("library:blacklist:add")
    @Log(title = "加入黑名单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BlacklistBo bo) {
        return toAjax(blacklistService.addByBo(bo));
    }

    /** 手动解除黑名单（恢复权限 + 校准信用至门槛分） */
    @SaCheckPermission("library:blacklist:manage")
    @Log(title = "解除黑名单", businessType = BusinessType.UPDATE)
    @PutMapping("/release/{id}")
    public R<Void> release(@PathVariable Long id) {
        return toAjax(blacklistService.release(id, 3));
    }

}

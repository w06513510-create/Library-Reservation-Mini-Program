package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.DeskBo;
import org.dromara.library.domain.vo.DeskVo;
import org.dromara.library.service.IDeskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 桌子Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/desk")
public class DeskController extends BaseController {

    private final IDeskService deskService;

    /**
     * 查询桌子列表
     */
    @SaCheckPermission("library:desk:list")
    @GetMapping("/list")
    public TableDataInfo<DeskVo> list(@Validated(QueryGroup.class) DeskBo bo, PageQuery pageQuery) {
        return deskService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出桌子列表
     */
    @SaCheckPermission("library:desk:export")
    @Log(title = "桌子", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(DeskBo bo, HttpServletResponse response) {
        List<DeskVo> list = deskService.queryList(bo);
        ExcelUtil.exportExcel(list, "桌子", DeskVo.class, response);
    }

    /**
     * 获取桌子详细信息
     */
    @SaCheckPermission("library:desk:query")
    @GetMapping("/{id}")
    public R<DeskVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(deskService.queryById(id));
    }

    /**
     * 新增桌子
     */
    @SaCheckPermission("library:desk:add")
    @Log(title = "桌子", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DeskBo bo) {
        return toAjax(deskService.insertByBo(bo));
    }

    /**
     * 修改桌子
     */
    @SaCheckPermission("library:desk:edit")
    @Log(title = "桌子", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DeskBo bo) {
        return toAjax(deskService.updateByBo(bo));
    }

    /**
     * 删除桌子
     */
    @SaCheckPermission("library:desk:remove")
    @Log(title = "桌子", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(deskService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

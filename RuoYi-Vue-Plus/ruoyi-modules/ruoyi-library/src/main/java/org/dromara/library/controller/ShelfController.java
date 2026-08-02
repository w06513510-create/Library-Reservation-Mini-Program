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
import org.dromara.library.domain.bo.ShelfBo;
import org.dromara.library.domain.vo.ShelfVo;
import org.dromara.library.service.IShelfService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 书架Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/shelf")
public class ShelfController extends BaseController {

    private final IShelfService shelfService;

    /**
     * 查询书架列表
     */
    @SaCheckPermission("library:shelf:list")
    @GetMapping("/list")
    public TableDataInfo<ShelfVo> list(@Validated(QueryGroup.class) ShelfBo bo, PageQuery pageQuery) {
        return shelfService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出书架列表
     */
    @SaCheckPermission("library:shelf:export")
    @Log(title = "书架", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ShelfBo bo, HttpServletResponse response) {
        List<ShelfVo> list = shelfService.queryList(bo);
        ExcelUtil.exportExcel(list, "书架", ShelfVo.class, response);
    }

    /**
     * 获取书架详细信息
     */
    @SaCheckPermission("library:shelf:query")
    @GetMapping("/{id}")
    public R<ShelfVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(shelfService.queryById(id));
    }

    /**
     * 新增书架
     */
    @SaCheckPermission("library:shelf:add")
    @Log(title = "书架", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ShelfBo bo) {
        return toAjax(shelfService.insertByBo(bo));
    }

    /**
     * 修改书架
     */
    @SaCheckPermission("library:shelf:edit")
    @Log(title = "书架", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ShelfBo bo) {
        return toAjax(shelfService.updateByBo(bo));
    }

    /**
     * 删除书架
     */
    @SaCheckPermission("library:shelf:remove")
    @Log(title = "书架", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(shelfService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

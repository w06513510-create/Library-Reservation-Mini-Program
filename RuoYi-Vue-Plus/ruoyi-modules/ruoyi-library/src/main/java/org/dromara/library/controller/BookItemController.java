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
import org.dromara.library.domain.bo.BookItemBo;
import org.dromara.library.domain.vo.BookItemVo;
import org.dromara.library.service.IBookItemService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 馆藏册（册/Item）Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/bookItem")
public class BookItemController extends BaseController {

    private final IBookItemService bookItemService;

    /**
     * 查询馆藏册列表
     */
    @SaCheckPermission("library:bookItem:list")
    @GetMapping("/list")
    public TableDataInfo<BookItemVo> list(@Validated(QueryGroup.class) BookItemBo bo, PageQuery pageQuery) {
        return bookItemService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出馆藏册列表
     */
    @SaCheckPermission("library:bookItem:export")
    @Log(title = "馆藏册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BookItemBo bo, HttpServletResponse response) {
        List<BookItemVo> list = bookItemService.queryList(bo);
        ExcelUtil.exportExcel(list, "馆藏册", BookItemVo.class, response);
    }

    /**
     * 获取馆藏册详细信息
     */
    @SaCheckPermission("library:bookItem:query")
    @GetMapping("/{id}")
    public R<BookItemVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(bookItemService.queryById(id));
    }

    /**
     * 新增馆藏册
     */
    @SaCheckPermission("library:bookItem:add")
    @Log(title = "馆藏册", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BookItemBo bo) {
        return toAjax(bookItemService.insertByBo(bo));
    }

    /**
     * 修改馆藏册
     */
    @SaCheckPermission("library:bookItem:edit")
    @Log(title = "馆藏册", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BookItemBo bo) {
        return toAjax(bookItemService.updateByBo(bo));
    }

    /**
     * 删除馆藏册
     */
    @SaCheckPermission("library:bookItem:remove")
    @Log(title = "馆藏册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(bookItemService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

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
import org.dromara.library.domain.bo.BookLocationBo;
import org.dromara.library.domain.vo.BookLocationVo;
import org.dromara.library.service.IBookLocationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 藏地Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/location")
public class BookLocationController extends BaseController {

    private final IBookLocationService bookLocationService;

    /**
     * 查询藏地列表
     */
    @SaCheckPermission("library:location:list")
    @GetMapping("/list")
    public TableDataInfo<BookLocationVo> list(@Validated(QueryGroup.class) BookLocationBo bo, PageQuery pageQuery) {
        return bookLocationService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出藏地列表
     */
    @SaCheckPermission("library:location:export")
    @Log(title = "藏地", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BookLocationBo bo, HttpServletResponse response) {
        List<BookLocationVo> list = bookLocationService.queryList(bo);
        ExcelUtil.exportExcel(list, "藏地", BookLocationVo.class, response);
    }

    /**
     * 获取藏地详细信息
     */
    @SaCheckPermission("library:location:query")
    @GetMapping("/{id}")
    public R<BookLocationVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(bookLocationService.queryById(id));
    }

    /**
     * 新增藏地
     */
    @SaCheckPermission("library:location:add")
    @Log(title = "藏地", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BookLocationBo bo) {
        return toAjax(bookLocationService.insertByBo(bo));
    }

    /**
     * 修改藏地
     */
    @SaCheckPermission("library:location:edit")
    @Log(title = "藏地", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BookLocationBo bo) {
        return toAjax(bookLocationService.updateByBo(bo));
    }

    /**
     * 删除藏地
     */
    @SaCheckPermission("library:location:remove")
    @Log(title = "藏地", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(bookLocationService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

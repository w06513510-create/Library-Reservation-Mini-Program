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
import org.dromara.library.domain.bo.BookBo;
import org.dromara.library.domain.vo.BookVo;
import org.dromara.library.service.IBookService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 书目（种/Bib）Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/book")
public class BookController extends BaseController {

    private final IBookService bookService;

    /**
     * 查询书目列表
     */
    @SaCheckPermission("library:book:list")
    @GetMapping("/list")
    public TableDataInfo<BookVo> list(@Validated(QueryGroup.class) BookBo bo, PageQuery pageQuery) {
        return bookService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出书目列表
     */
    @SaCheckPermission("library:book:export")
    @Log(title = "书目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BookBo bo, HttpServletResponse response) {
        List<BookVo> list = bookService.queryList(bo);
        ExcelUtil.exportExcel(list, "书目", BookVo.class, response);
    }

    /**
     * 获取书目详细信息
     */
    @SaCheckPermission("library:book:query")
    @GetMapping("/{id}")
    public R<BookVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(bookService.queryById(id));
    }

    /**
     * 新增书目
     */
    @SaCheckPermission("library:book:add")
    @Log(title = "书目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BookBo bo) {
        return toAjax(bookService.insertByBo(bo));
    }

    /**
     * 修改书目
     */
    @SaCheckPermission("library:book:edit")
    @Log(title = "书目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BookBo bo) {
        return toAjax(bookService.updateByBo(bo));
    }

    /**
     * 删除书目
     */
    @SaCheckPermission("library:book:remove")
    @Log(title = "书目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(bookService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

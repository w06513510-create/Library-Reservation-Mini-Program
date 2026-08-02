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
import org.dromara.library.domain.bo.ReaderBo;
import org.dromara.library.domain.vo.ReaderVo;
import org.dromara.library.service.IReaderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 读者档案Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/reader")
public class ReaderController extends BaseController {

    private final IReaderService readerService;

    /**
     * 查询读者档案列表
     */
    @SaCheckPermission("library:reader:list")
    @GetMapping("/list")
    public TableDataInfo<ReaderVo> list(@Validated(QueryGroup.class) ReaderBo bo, PageQuery pageQuery) {
        return readerService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出读者档案列表
     */
    @SaCheckPermission("library:reader:export")
    @Log(title = "读者档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ReaderBo bo, HttpServletResponse response) {
        List<ReaderVo> list = readerService.queryList(bo);
        ExcelUtil.exportExcel(list, "读者档案", ReaderVo.class, response);
    }

    /**
     * 获取读者档案详细信息
     */
    @SaCheckPermission("library:reader:query")
    @GetMapping("/{id}")
    public R<ReaderVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(readerService.queryById(id));
    }

    /**
     * 新增读者档案
     */
    @SaCheckPermission("library:reader:add")
    @Log(title = "读者档案", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ReaderBo bo) {
        return toAjax(readerService.insertByBo(bo));
    }

    /**
     * 修改读者档案
     */
    @SaCheckPermission("library:reader:edit")
    @Log(title = "读者档案", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ReaderBo bo) {
        return toAjax(readerService.updateByBo(bo));
    }

    /**
     * 删除读者档案
     */
    @SaCheckPermission("library:reader:remove")
    @Log(title = "读者档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(readerService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

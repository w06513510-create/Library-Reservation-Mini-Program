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
import org.dromara.library.domain.bo.AreaBo;
import org.dromara.library.domain.vo.AreaVo;
import org.dromara.library.service.IAreaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 区域Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/area")
public class AreaController extends BaseController {

    private final IAreaService areaService;

    /**
     * 查询区域列表
     */
    @SaCheckPermission("library:area:list")
    @GetMapping("/list")
    public TableDataInfo<AreaVo> list(@Validated(QueryGroup.class) AreaBo bo, PageQuery pageQuery) {
        return areaService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出区域列表
     */
    @SaCheckPermission("library:area:export")
    @Log(title = "区域", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AreaBo bo, HttpServletResponse response) {
        List<AreaVo> list = areaService.queryList(bo);
        ExcelUtil.exportExcel(list, "区域", AreaVo.class, response);
    }

    /**
     * 获取区域详细信息
     */
    @SaCheckPermission("library:area:query")
    @GetMapping("/{id}")
    public R<AreaVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(areaService.queryById(id));
    }

    /**
     * 新增区域
     */
    @SaCheckPermission("library:area:add")
    @Log(title = "区域", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AreaBo bo) {
        return toAjax(areaService.insertByBo(bo));
    }

    /**
     * 修改区域
     */
    @SaCheckPermission("library:area:edit")
    @Log(title = "区域", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AreaBo bo) {
        return toAjax(areaService.updateByBo(bo));
    }

    /**
     * 删除区域
     */
    @SaCheckPermission("library:area:remove")
    @Log(title = "区域", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(areaService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

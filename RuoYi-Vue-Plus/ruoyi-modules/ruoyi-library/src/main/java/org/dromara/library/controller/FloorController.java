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
import org.dromara.library.domain.bo.FloorBo;
import org.dromara.library.domain.vo.FloorVo;
import org.dromara.library.service.IFloorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 楼层Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/floor")
public class FloorController extends BaseController {

    private final IFloorService floorService;

    /**
     * 查询楼层列表
     */
    @SaCheckPermission("library:floor:list")
    @GetMapping("/list")
    public TableDataInfo<FloorVo> list(@Validated(QueryGroup.class) FloorBo bo, PageQuery pageQuery) {
        return floorService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出楼层列表
     */
    @SaCheckPermission("library:floor:export")
    @Log(title = "楼层", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(FloorBo bo, HttpServletResponse response) {
        List<FloorVo> list = floorService.queryList(bo);
        ExcelUtil.exportExcel(list, "楼层", FloorVo.class, response);
    }

    /**
     * 获取楼层详细信息
     */
    @SaCheckPermission("library:floor:query")
    @GetMapping("/{id}")
    public R<FloorVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(floorService.queryById(id));
    }

    /**
     * 新增楼层
     */
    @SaCheckPermission("library:floor:add")
    @Log(title = "楼层", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody FloorBo bo) {
        return toAjax(floorService.insertByBo(bo));
    }

    /**
     * 修改楼层
     */
    @SaCheckPermission("library:floor:edit")
    @Log(title = "楼层", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody FloorBo bo) {
        return toAjax(floorService.updateByBo(bo));
    }

    /**
     * 删除楼层
     */
    @SaCheckPermission("library:floor:remove")
    @Log(title = "楼层", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(floorService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

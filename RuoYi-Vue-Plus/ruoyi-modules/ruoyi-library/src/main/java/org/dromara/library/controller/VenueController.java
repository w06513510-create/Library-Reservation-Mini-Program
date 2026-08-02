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
import org.dromara.library.domain.bo.VenueBo;
import org.dromara.library.domain.vo.VenueVo;
import org.dromara.library.service.IVenueService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 场馆Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/venue")
public class VenueController extends BaseController {

    private final IVenueService venueService;

    /**
     * 查询场馆列表
     */
    @SaCheckPermission("library:venue:list")
    @GetMapping("/list")
    public TableDataInfo<VenueVo> list(@Validated(QueryGroup.class) VenueBo bo, PageQuery pageQuery) {
        return venueService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出场馆列表
     */
    @SaCheckPermission("library:venue:export")
    @Log(title = "场馆", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(VenueBo bo, HttpServletResponse response) {
        List<VenueVo> list = venueService.queryList(bo);
        ExcelUtil.exportExcel(list, "场馆", VenueVo.class, response);
    }

    /**
     * 获取场馆详细信息
     */
    @SaCheckPermission("library:venue:query")
    @GetMapping("/{id}")
    public R<VenueVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(venueService.queryById(id));
    }

    /**
     * 新增场馆
     */
    @SaCheckPermission("library:venue:add")
    @Log(title = "场馆", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody VenueBo bo) {
        return toAjax(venueService.insertByBo(bo));
    }

    /**
     * 修改场馆
     */
    @SaCheckPermission("library:venue:edit")
    @Log(title = "场馆", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody VenueBo bo) {
        return toAjax(venueService.updateByBo(bo));
    }

    /**
     * 删除场馆
     */
    @SaCheckPermission("library:venue:remove")
    @Log(title = "场馆", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(venueService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

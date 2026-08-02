package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.SuperviseBo;
import org.dromara.library.domain.vo.SuperviseVo;
import org.dromara.library.service.ISuperviseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 占座监督Controller（CRUD + 举报 + 手动解除）
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/supervise")
public class SuperviseController extends BaseController {

    private final ISuperviseService superviseService;

    /**
     * 查询占座监督列表
     */
    @SaCheckPermission("library:supervise:list")
    @GetMapping("/list")
    public TableDataInfo<SuperviseVo> list(@Validated(QueryGroup.class) SuperviseBo bo, PageQuery pageQuery) {
        return superviseService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出占座监督列表
     */
    @SaCheckPermission("library:supervise:export")
    @Log(title = "占座监督", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SuperviseBo bo, HttpServletResponse response) {
        List<SuperviseVo> list = superviseService.queryList(bo);
        ExcelUtil.exportExcel(list, "占座监督", SuperviseVo.class, response);
    }

    /**
     * 获取占座监督详细信息
     */
    @SaCheckPermission("library:supervise:query")
    @GetMapping("/{id}")
    public R<SuperviseVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(superviseService.queryById(id));
    }

    /**
     * 发起占座监督（举报某使用中座位无人落座）
     */
    @SaCheckPermission("library:supervise:add")
    @Log(title = "占座监督", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SuperviseBo bo) {
        return toAjax(superviseService.report(bo));
    }

    /**
     * 标记已落座：手动解除监督（原用户已按时落座）0→1
     */
    @SaCheckPermission("library:supervise:edit")
    @Log(title = "占座监督-解除", businessType = BusinessType.UPDATE)
    @PostMapping("/reseat/{id}")
    public R<Void> reseat(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return toAjax(superviseService.reseat(id));
    }

    /**
     * 删除占座监督
     */
    @SaCheckPermission("library:supervise:remove")
    @Log(title = "占座监督", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(superviseService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

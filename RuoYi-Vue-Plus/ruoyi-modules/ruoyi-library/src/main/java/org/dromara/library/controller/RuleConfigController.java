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
import org.dromara.library.domain.bo.RuleConfigBo;
import org.dromara.library.domain.vo.RuleConfigVo;
import org.dromara.library.service.IRuleConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 规则配置Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/ruleConfig")
public class RuleConfigController extends BaseController {

    private final IRuleConfigService ruleConfigService;

    /**
     * 查询规则配置列表
     */
    @SaCheckPermission("library:ruleConfig:list")
    @GetMapping("/list")
    public TableDataInfo<RuleConfigVo> list(@Validated(QueryGroup.class) RuleConfigBo bo, PageQuery pageQuery) {
        return ruleConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出规则配置列表
     */
    @SaCheckPermission("library:ruleConfig:export")
    @Log(title = "规则配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RuleConfigBo bo, HttpServletResponse response) {
        List<RuleConfigVo> list = ruleConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "规则配置", RuleConfigVo.class, response);
    }

    /**
     * 获取规则配置详细信息
     */
    @SaCheckPermission("library:ruleConfig:query")
    @GetMapping("/{id}")
    public R<RuleConfigVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(ruleConfigService.queryById(id));
    }

    /**
     * 新增规则配置
     */
    @SaCheckPermission("library:ruleConfig:add")
    @Log(title = "规则配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RuleConfigBo bo) {
        return toAjax(ruleConfigService.insertByBo(bo));
    }

    /**
     * 修改规则配置
     */
    @SaCheckPermission("library:ruleConfig:edit")
    @Log(title = "规则配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RuleConfigBo bo) {
        return toAjax(ruleConfigService.updateByBo(bo));
    }

    /**
     * 删除规则配置
     */
    @SaCheckPermission("library:ruleConfig:remove")
    @Log(title = "规则配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(ruleConfigService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

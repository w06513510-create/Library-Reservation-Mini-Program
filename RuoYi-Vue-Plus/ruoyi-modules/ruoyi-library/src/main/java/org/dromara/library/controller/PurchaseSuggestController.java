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
import org.dromara.library.domain.bo.PurchaseSuggestBo;
import org.dromara.library.domain.vo.PurchaseSuggestVo;
import org.dromara.library.service.IPurchaseSuggestService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 读者荐购Controller（CRUD + 状态机：受理/驳回/已采购）
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/purchaseSuggest")
public class PurchaseSuggestController extends BaseController {

    private final IPurchaseSuggestService purchaseSuggestService;

    /**
     * 查询读者荐购列表
     */
    @SaCheckPermission("library:purchaseSuggest:list")
    @GetMapping("/list")
    public TableDataInfo<PurchaseSuggestVo> list(@Validated(QueryGroup.class) PurchaseSuggestBo bo, PageQuery pageQuery) {
        return purchaseSuggestService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出读者荐购列表
     */
    @SaCheckPermission("library:purchaseSuggest:export")
    @Log(title = "荐购", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PurchaseSuggestBo bo, HttpServletResponse response) {
        List<PurchaseSuggestVo> list = purchaseSuggestService.queryList(bo);
        ExcelUtil.exportExcel(list, "荐购", PurchaseSuggestVo.class, response);
    }

    /**
     * 获取读者荐购详细信息
     */
    @SaCheckPermission("library:purchaseSuggest:query")
    @GetMapping("/{id}")
    public R<PurchaseSuggestVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(purchaseSuggestService.queryById(id));
    }

    /**
     * 新增读者荐购
     */
    @SaCheckPermission("library:purchaseSuggest:add")
    @Log(title = "荐购", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PurchaseSuggestBo bo) {
        return toAjax(purchaseSuggestService.insertByBo(bo));
    }

    /**
     * 修改读者荐购
     */
    @SaCheckPermission("library:purchaseSuggest:edit")
    @Log(title = "荐购", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PurchaseSuggestBo bo) {
        return toAjax(purchaseSuggestService.updateByBo(bo));
    }

    /**
     * 删除读者荐购
     */
    @SaCheckPermission("library:purchaseSuggest:remove")
    @Log(title = "荐购", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(purchaseSuggestService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

    /**
     * 受理转采购 0→1
     */
    @SaCheckPermission("library:purchaseSuggest:handle")
    @Log(title = "荐购", businessType = BusinessType.UPDATE)
    @PostMapping("/accept/{id}")
    public R<Void> accept(@PathVariable Long id) {
        return toAjax(purchaseSuggestService.accept(id));
    }

    /**
     * 驳回 0→2
     */
    @SaCheckPermission("library:purchaseSuggest:handle")
    @Log(title = "荐购", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{id}")
    public R<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        return toAjax(purchaseSuggestService.reject(id, reason));
    }

    /**
     * 标记已采购 1→3
     */
    @SaCheckPermission("library:purchaseSuggest:handle")
    @Log(title = "荐购", businessType = BusinessType.UPDATE)
    @PostMapping("/purchased/{id}")
    public R<Void> purchased(@PathVariable Long id) {
        return toAjax(purchaseSuggestService.purchased(id));
    }

}

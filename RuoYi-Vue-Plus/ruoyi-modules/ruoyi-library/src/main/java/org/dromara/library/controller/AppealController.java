package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.AppealBo;
import org.dromara.library.domain.vo.AppealVo;
import org.dromara.library.service.IAppealService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 违约申诉Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/appeal")
public class AppealController extends BaseController {

    private final IAppealService appealService;

    @SaCheckPermission("library:appeal:list")
    @GetMapping("/list")
    public TableDataInfo<AppealVo> list(@Validated(QueryGroup.class) AppealBo bo, PageQuery pageQuery) {
        return appealService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("library:appeal:query")
    @GetMapping("/{id}")
    public R<AppealVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(appealService.queryById(id));
    }

    /** 提交申诉 */
    @SaCheckPermission("library:appeal:add")
    @Log(title = "违约申诉", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AppealBo bo) {
        return toAjax(appealService.submit(bo));
    }

    /** 审批：pass=true 通过(解除违约+冲正)，false 驳回 */
    @SaCheckPermission("library:appeal:audit")
    @Log(title = "申诉审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id, @RequestParam boolean pass, @RequestParam(required = false) String remark) {
        return toAjax(appealService.audit(id, pass, remark));
    }

}

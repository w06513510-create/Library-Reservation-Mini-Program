package org.dromara.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.biz.domain.BizAuditLog;
import org.dromara.biz.domain.bo.BizAuditBo;
import org.dromara.biz.domain.vo.BizAuditVo;
import org.dromara.biz.service.IBizAuditService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 通用审核单 Controller
 * <p>approve/reject 共用一个 {@code biz:audit:audit} 权限（同跑腿：审核动作合并授权）。
 * <p>把 {@code /biz/audit}、包名 {@code org.dromara.biz}、权限前缀 {@code biz:audit} 换成你的业务。
 *
 * @author ruoyi-template
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/biz/audit")
public class BizAuditController extends BaseController {

    private final IBizAuditService bizAuditService;

    /** 审核列表（后台） */
    @SaCheckPermission("biz:audit:list")
    @GetMapping("/list")
    public TableDataInfo<BizAuditVo> list(@Validated(QueryGroup.class) BizAuditBo bo, PageQuery pageQuery) {
        return bizAuditService.queryPageList(bo, pageQuery);
    }

    /** 详情 */
    @SaCheckPermission("biz:audit:query")
    @GetMapping("/{id}")
    public R<BizAuditVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(bizAuditService.queryById(id));
    }

    /** 提交申请 */
    @SaCheckPermission("biz:audit:add")
    @Log(title = "审核单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> submit(@Validated(AddGroup.class) @RequestBody BizAuditBo bo) {
        bizAuditService.submit(bo);
        return R.ok();
    }

    /** 审核通过 */
    @SaCheckPermission("biz:audit:audit")
    @Log(title = "审核单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/approve/{id}")
    public R<Void> approve(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(bizAuditService.approve(id));
    }

    /** 审核驳回（需带驳回原因） */
    @SaCheckPermission("biz:audit:audit")
    @Log(title = "审核单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/reject/{id}")
    public R<Void> reject(@NotNull(message = "主键不能为空") @PathVariable Long id,
                          @NotBlank(message = "驳回原因不能为空") @RequestParam String rejectReason) {
        return toAjax(bizAuditService.reject(id, rejectReason));
    }

    /** 某审核单的流转轨迹（仅多状态工单场景） */
    @SaCheckPermission("biz:audit:list")
    @GetMapping("/logs/{auditId}")
    public TableDataInfo<BizAuditLog> logs(@PathVariable Long auditId, PageQuery pageQuery) {
        return bizAuditService.queryLogPage(auditId, pageQuery);
    }

}

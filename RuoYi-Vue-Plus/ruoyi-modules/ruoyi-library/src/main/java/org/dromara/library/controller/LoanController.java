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
import org.dromara.library.domain.bo.LoanBo;
import org.dromara.library.domain.vo.LoanVo;
import org.dromara.library.service.ILoanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * 借阅流通Controller（借出/归还/续借/催还）
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/loan")
public class LoanController extends BaseController {

    private final ILoanService loanService;

    @SaCheckPermission("library:loan:list")
    @GetMapping("/list")
    public TableDataInfo<LoanVo> list(@Validated(QueryGroup.class) LoanBo bo, PageQuery pageQuery) {
        return loanService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("library:loan:query")
    @GetMapping("/{id}")
    public R<LoanVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(loanService.queryById(id));
    }

    /** 借出办理 */
    @SaCheckPermission("library:loan:manage")
    @Log(title = "图书借出", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/borrow")
    public R<Void> borrow(@Validated(AddGroup.class) @RequestBody LoanBo bo) {
        return toAjax(loanService.borrow(bo));
    }

    /** 归还办理 */
    @SaCheckPermission("library:loan:manage")
    @Log(title = "图书归还", businessType = BusinessType.UPDATE)
    @PutMapping("/return/{id}")
    public R<Void> returnBook(@PathVariable Long id) {
        return toAjax(loanService.returnBook(id));
    }

    /** 续借 */
    @SaCheckPermission("library:loan:manage")
    @Log(title = "图书续借", businessType = BusinessType.UPDATE)
    @PutMapping("/renew/{id}")
    public R<Void> renew(@PathVariable Long id) {
        return toAjax(loanService.renew(id));
    }

    /** 预约催还 */
    @SaCheckPermission("library:loan:manage")
    @Log(title = "预约催还", businessType = BusinessType.UPDATE)
    @PutMapping("/recall/{id}")
    public R<Void> recall(@PathVariable Long id) {
        return toAjax(loanService.recall(id));
    }

}

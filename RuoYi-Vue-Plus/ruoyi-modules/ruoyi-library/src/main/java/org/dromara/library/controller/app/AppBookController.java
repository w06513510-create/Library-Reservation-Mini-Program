package org.dromara.library.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.BookBo;
import org.dromara.library.domain.bo.HoldBo;
import org.dromara.library.domain.bo.LoanBo;
import org.dromara.library.domain.vo.BookVo;
import org.dromara.library.domain.vo.HoldVo;
import org.dromara.library.domain.vo.LoanVo;
import org.dromara.library.service.IBookService;
import org.dromara.library.service.IHoldService;
import org.dromara.library.service.ILoanService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * C端(小程序) 图书 Controller —— 书目检索 / 详情 / 我的借阅 / 续借 / 图书预约(hold)。
 * <p>薄封装复用 {@link IBookService}/{@link ILoanService}/{@link IHoldService}；
 * 借阅、预约的 readerId 强制取当前登录读者，续借 / 取消预约前先校归属，防越权。
 *
 * @author library
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/library/book")
public class AppBookController {

    private final IBookService bookService;
    private final ILoanService loanService;
    private final IHoldService holdService;

    /** 书目检索（BookBo 支持 title / isbn / author 过滤，前端按需传） */
    @GetMapping("/list")
    public TableDataInfo<BookVo> list(BookBo bo, PageQuery pageQuery) {
        return bookService.queryPageList(bo, pageQuery);
    }

    /** 书目详情（含馆藏 / 可借数等，寻书用索书号 + 书架定位） */
    @GetMapping("/{id}")
    public R<BookVo> detail(@PathVariable Long id) {
        return R.ok(bookService.queryById(id));
    }

    /** 我的借阅（status: 0在借 1已还 2逾期 ...） */
    @GetMapping("/loans")
    public TableDataInfo<LoanVo> loans(@RequestParam(required = false) Integer status, PageQuery pageQuery) {
        LoanBo bo = new LoanBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setStatus(status);
        return loanService.queryPageList(bo, pageQuery);
    }

    /** 续借（仅本人在借单；未逾期 / 未被催还 / 未超次数在 Service 校验） */
    @PutMapping("/renew/{loanId}")
    public R<Void> renew(@PathVariable Long loanId) {
        LoanVo vo = loanService.queryById(loanId);
        if (vo == null) {
            throw new ServiceException("借阅单不存在");
        }
        if (!Objects.equals(vo.getReaderId(), AppLoginHelper.getUserId())) {
            throw new ServiceException("无权操作他人借阅", 403);
        }
        return loanService.renew(loanId) ? R.ok() : R.fail();
    }

    /** 图书预约(hold)：复本全借出时排队，分配队列位次（readerId 强制当前登录读者） */
    @RepeatSubmit()
    @PostMapping("/hold/{bookId}")
    public R<Void> hold(@PathVariable Long bookId) {
        HoldBo bo = new HoldBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setBookId(bookId);
        return holdService.createHold(bo) ? R.ok() : R.fail();
    }

    /** 我的图书预约 */
    @GetMapping("/holds")
    public TableDataInfo<HoldVo> holds(@RequestParam(required = false) Integer status, PageQuery pageQuery) {
        HoldBo bo = new HoldBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setStatus(status);
        return holdService.queryPageList(bo, pageQuery);
    }

    /** 取消图书预约（仅本人；排队中直接取消，到书保留则回架，逻辑在 Service） */
    @PutMapping("/hold/cancel/{holdId}")
    public R<Void> cancelHold(@PathVariable Long holdId) {
        HoldVo vo = holdService.queryById(holdId);
        if (vo == null) {
            throw new ServiceException("图书预约不存在");
        }
        if (!Objects.equals(vo.getReaderId(), AppLoginHelper.getUserId())) {
            throw new ServiceException("无权操作他人预约", 403);
        }
        return holdService.cancelHold(holdId) ? R.ok() : R.fail();
    }

}

package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.*;
import org.dromara.library.domain.bo.LoanBo;
import org.dromara.library.domain.vo.LoanVo;
import org.dromara.library.mapper.*;
import org.dromara.library.service.ILoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 借阅流通Service实现（状态跃迁用 CAS，库存冗余 avail_qty 借还即时维护）
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class LoanServiceImpl implements ILoanService {

    private final LoanMapper baseMapper;
    private final BookItemMapper itemMapper;
    private final BookMapper bookMapper;
    private final HoldMapper holdMapper;
    private final ReaderMapper readerMapper;

    private static final int BORROW_DAYS = 30;
    private static final int RENEW_DAYS = 30;
    private static final int MAX_LOANS = 5;
    private static final int MAX_RENEW = 2;
    private static final int HOLD_KEEP_DAYS = 5;
    private static final int BLACKLIST_SCORE = 20;
    private static final long DAY_MS = 24L * 3600 * 1000;

    @Override
    public LoanVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<LoanVo> queryPageList(LoanBo bo, PageQuery pageQuery) {
        Page<LoanVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        fillNames(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public List<LoanVo> queryList(LoanBo bo) {
        List<LoanVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        fillNames(list);
        return list;
    }

    /** 批量把 读者ID→姓名（学号）、馆藏册ID→条码、书目ID→书名 填进 VO，供列表以人话展示（SOP 06 §5） */
    private void fillNames(List<LoanVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> readerIds = list.stream().map(LoanVo::getReaderId).filter(Objects::nonNull).distinct().toList();
        List<Long> itemIds = list.stream().map(LoanVo::getItemId).filter(Objects::nonNull).distinct().toList();
        List<Long> bookIds = list.stream().map(LoanVo::getBookId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> readerNames = new HashMap<>();
        if (!readerIds.isEmpty()) {
            readerMapper.selectList(Wrappers.<Reader>lambdaQuery().in(Reader::getUserId, readerIds))
                .forEach(r -> readerNames.put(r.getUserId(), fmtReader(r)));
        }
        Map<Long, String> barcodes = new HashMap<>();
        if (!itemIds.isEmpty()) {
            itemMapper.selectList(Wrappers.<BookItem>lambdaQuery().in(BookItem::getId, itemIds))
                .forEach(i -> barcodes.put(i.getId(), i.getBarcode()));
        }
        Map<Long, String> bookNames = new HashMap<>();
        if (!bookIds.isEmpty()) {
            bookMapper.selectList(Wrappers.<Book>lambdaQuery().in(Book::getId, bookIds))
                .forEach(b -> bookNames.put(b.getId(), b.getTitle()));
        }
        for (LoanVo vo : list) {
            if (vo.getReaderId() != null) {
                vo.setReaderName(readerNames.get(vo.getReaderId()));
            }
            if (vo.getItemId() != null) {
                vo.setBarcode(barcodes.get(vo.getItemId()));
            }
            if (vo.getBookId() != null) {
                vo.setBookName(bookNames.get(vo.getBookId()));
            }
        }
    }

    /** 读者显示名：姓名（学号） */
    private String fmtReader(Reader r) {
        String name = r.getRealName() == null ? "" : r.getRealName();
        String sn = r.getStudentNo() == null ? "" : r.getStudentNo();
        if (!name.isBlank() && !sn.isBlank()) {
            return name + "（" + sn + "）";
        }
        return !name.isBlank() ? name : sn;
    }

    private LambdaQueryWrapper<Loan> buildQueryWrapper(LoanBo bo) {
        LambdaQueryWrapper<Loan> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Loan::getReaderId, bo.getReaderId());
        lqw.eq(bo.getBookId() != null, Loan::getBookId, bo.getBookId());
        lqw.eq(bo.getStatus() != null, Loan::getStatus, bo.getStatus());
        lqw.orderByDesc(Loan::getCreateTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean borrow(LoanBo bo) {
        // 前置校验：黑名单/信用
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, bo.getReaderId()));
        if (reader != null) {
            if (reader.getBlacklistFlag() != null && reader.getBlacklistFlag() == 1) {
                throw new ServiceException("该读者在黑名单中，暂停借阅");
            }
            if (reader.getCreditScore() != null && reader.getCreditScore() < BLACKLIST_SCORE) {
                throw new ServiceException("该读者信用分过低，暂停借阅");
            }
        }
        BookItem item = itemMapper.selectById(bo.getItemId());
        if (item == null) {
            throw new ServiceException("馆藏册不存在");
        }
        if (item.getStatus() == null || item.getStatus() != 1) {
            throw new ServiceException("该册当前不可借（需为可借在架）");
        }
        Long active = baseMapper.selectCount(Wrappers.<Loan>lambdaQuery()
            .eq(Loan::getReaderId, bo.getReaderId()).in(Loan::getStatus, 0, 2));
        if (active != null && active >= MAX_LOANS) {
            throw new ServiceException("已达可借册数上限（" + MAX_LOANS + "）");
        }
        // CAS 册 1→2 借出
        int rows = itemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
            .set(BookItem::getStatus, 2).eq(BookItem::getId, item.getId()).eq(BookItem::getStatus, 1));
        if (rows != 1) {
            throw new ServiceException("该册状态已变更，借出失败");
        }
        Date now = new Date();
        Loan loan = new Loan();
        loan.setReaderId(bo.getReaderId());
        loan.setItemId(item.getId());
        loan.setBookId(item.getBookId());
        loan.setBorrowTime(now);
        loan.setDueTime(new Date(now.getTime() + (long) BORROW_DAYS * DAY_MS));
        loan.setRenewCount(0);
        loan.setStatus(0);
        loan.setOverdueFlag(0);
        loan.setRecallFlag(0);
        baseMapper.insert(loan);
        // 可借数 -1
        bookMapper.update(null, Wrappers.lambdaUpdate(Book.class)
            .setSql("avail_qty = IFNULL(avail_qty, 0) - 1").eq(Book::getId, item.getBookId()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean returnBook(Long loanId) {
        Loan loan = baseMapper.selectById(loanId);
        if (loan == null) {
            throw new ServiceException("借阅单不存在");
        }
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Loan.class)
            .set(Loan::getStatus, 1).set(Loan::getReturnTime, new Date())
            .eq(Loan::getId, loanId).in(Loan::getStatus, 0, 2));
        if (rows != 1) {
            throw new ServiceException("该借阅单非在借状态，无法归还");
        }
        // 是否有该书的排队预约（队首）
        List<Hold> queue = holdMapper.selectList(Wrappers.<Hold>lambdaQuery()
            .eq(Hold::getBookId, loan.getBookId()).eq(Hold::getStatus, 0)
            .orderByAsc(Hold::getQueueNo));
        if (!queue.isEmpty()) {
            Hold head = queue.get(0);
            // 该册转预约架 2→3
            itemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
                .set(BookItem::getStatus, 3).eq(BookItem::getId, loan.getItemId()).eq(BookItem::getStatus, 2));
            // 到书保留：hold 0→1
            Date now = new Date();
            holdMapper.update(null, Wrappers.lambdaUpdate(Hold.class)
                .set(Hold::getStatus, 1).set(Hold::getItemId, loan.getItemId())
                .set(Hold::getReadyTime, now).set(Hold::getHoldDeadline, new Date(now.getTime() + (long) HOLD_KEEP_DAYS * DAY_MS))
                .eq(Hold::getId, head.getId()).eq(Hold::getStatus, 0));
            // 可借数不变（册在预约架，不上架）
        } else {
            // 无预约：回架 2→1，可借数 +1
            itemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
                .set(BookItem::getStatus, 1).eq(BookItem::getId, loan.getItemId()).eq(BookItem::getStatus, 2));
            bookMapper.update(null, Wrappers.lambdaUpdate(Book.class)
                .setSql("avail_qty = IFNULL(avail_qty, 0) + 1").eq(Book::getId, loan.getBookId()));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean renew(Long loanId) {
        Loan loan = baseMapper.selectById(loanId);
        if (loan == null) {
            throw new ServiceException("借阅单不存在");
        }
        if (loan.getStatus() == null || loan.getStatus() != 0) {
            throw new ServiceException("非在借状态不可续借");
        }
        if (loan.getRecallFlag() != null && loan.getRecallFlag() == 1) {
            throw new ServiceException("该书已被预约催还，不可续借");
        }
        if (loan.getRenewCount() != null && loan.getRenewCount() >= MAX_RENEW) {
            throw new ServiceException("续借次数已达上限（" + MAX_RENEW + "）");
        }
        if (loan.getDueTime() != null && new Date().after(loan.getDueTime())) {
            throw new ServiceException("已逾期，不可续借");
        }
        Date newDue = new Date(loan.getDueTime().getTime() + (long) RENEW_DAYS * DAY_MS);
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Loan.class)
            .set(Loan::getDueTime, newDue).setSql("renew_count = IFNULL(renew_count, 0) + 1")
            .eq(Loan::getId, loanId).eq(Loan::getStatus, 0).eq(Loan::getRecallFlag, 0));
        if (rows != 1) {
            throw new ServiceException("续借失败，状态已变更");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recall(Long loanId) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Loan.class)
            .set(Loan::getRecallFlag, 1).set(Loan::getRecallTime, new Date())
            .eq(Loan::getId, loanId).in(Loan::getStatus, 0, 2));
        if (rows != 1) {
            throw new ServiceException("仅在借图书可催还");
        }
        return true;
    }

}

package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.*;
import org.dromara.library.domain.bo.HoldBo;
import org.dromara.library.domain.vo.HoldVo;
import org.dromara.library.mapper.*;
import org.dromara.library.service.IHoldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 图书预约(hold)队列Service实现
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class HoldServiceImpl implements IHoldService {

    private final HoldMapper baseMapper;
    private final BookMapper bookMapper;
    private final BookItemMapper itemMapper;
    private final LoanMapper loanMapper;
    private final ReaderMapper readerMapper;

    private static final int BORROW_DAYS = 30;
    private static final int BLACKLIST_SCORE = 20;
    private static final long DAY_MS = 24L * 3600 * 1000;

    @Override
    public HoldVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<HoldVo> queryPageList(HoldBo bo, PageQuery pageQuery) {
        Page<HoldVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<HoldVo> queryList(HoldBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Hold> buildQueryWrapper(HoldBo bo) {
        LambdaQueryWrapper<Hold> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Hold::getReaderId, bo.getReaderId());
        lqw.eq(bo.getBookId() != null, Hold::getBookId, bo.getBookId());
        lqw.eq(bo.getStatus() != null, Hold::getStatus, bo.getStatus());
        lqw.orderByDesc(Hold::getCreateTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createHold(HoldBo bo) {
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, bo.getReaderId()));
        if (reader != null) {
            if (reader.getBlacklistFlag() != null && reader.getBlacklistFlag() == 1) {
                throw new ServiceException("该读者在黑名单中，暂停预约");
            }
            if (reader.getCreditScore() != null && reader.getCreditScore() < BLACKLIST_SCORE) {
                throw new ServiceException("该读者信用分过低，暂停预约");
            }
        }
        Book book = bookMapper.selectById(bo.getBookId());
        if (book == null) {
            throw new ServiceException("书目不存在");
        }
        if (book.getAvailQty() != null && book.getAvailQty() > 0) {
            throw new ServiceException("该书有可借复本，请直接借阅");
        }
        Long dup = baseMapper.selectCount(Wrappers.<Hold>lambdaQuery()
            .eq(Hold::getReaderId, bo.getReaderId()).eq(Hold::getBookId, bo.getBookId())
            .in(Hold::getStatus, 0, 1));
        if (dup != null && dup > 0) {
            throw new ServiceException("您已预约该书，请勿重复预约");
        }
        // 队列位次 = 当前有效队列最大位次 + 1
        List<Hold> queue = baseMapper.selectList(Wrappers.<Hold>lambdaQuery()
            .eq(Hold::getBookId, bo.getBookId()).in(Hold::getStatus, 0, 1)
            .orderByDesc(Hold::getQueueNo));
        int nextNo = queue.isEmpty() || queue.get(0).getQueueNo() == null ? 1 : queue.get(0).getQueueNo() + 1;
        Hold hold = new Hold();
        hold.setReaderId(bo.getReaderId());
        hold.setBookId(bo.getBookId());
        hold.setQueueNo(nextNo);
        hold.setStatus(0);
        hold.setHoldTime(new Date());
        return baseMapper.insert(hold) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pickup(Long holdId) {
        Hold hold = baseMapper.selectById(holdId);
        if (hold == null) {
            throw new ServiceException("预约不存在");
        }
        if (hold.getStatus() == null || hold.getStatus() != 1) {
            throw new ServiceException("该预约非到书保留状态，无法取书");
        }
        // CAS hold 1→2 已取书
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Hold.class)
            .set(Hold::getStatus, 2).set(Hold::getPickupTime, new Date())
            .eq(Hold::getId, holdId).eq(Hold::getStatus, 1));
        if (rows != 1) {
            throw new ServiceException("取书失败，状态已变更");
        }
        // 预约架册 3→2 借出
        itemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
            .set(BookItem::getStatus, 2).eq(BookItem::getId, hold.getItemId()).eq(BookItem::getStatus, 3));
        // 生成借阅单（该册原就不在可借数内，avail_qty 不变）
        Date now = new Date();
        Loan loan = new Loan();
        loan.setReaderId(hold.getReaderId());
        loan.setItemId(hold.getItemId());
        loan.setBookId(hold.getBookId());
        loan.setBorrowTime(now);
        loan.setDueTime(new Date(now.getTime() + (long) BORROW_DAYS * DAY_MS));
        loan.setRenewCount(0);
        loan.setStatus(0);
        loan.setOverdueFlag(0);
        loan.setRecallFlag(0);
        loanMapper.insert(loan);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelHold(Long holdId) {
        Hold hold = baseMapper.selectById(holdId);
        if (hold == null) {
            throw new ServiceException("预约不存在");
        }
        if (hold.getStatus() != null && hold.getStatus() == 0) {
            baseMapper.update(null, Wrappers.lambdaUpdate(Hold.class)
                .set(Hold::getStatus, 3).set(Hold::getCancelTime, new Date())
                .eq(Hold::getId, holdId).eq(Hold::getStatus, 0));
        } else if (hold.getStatus() != null && hold.getStatus() == 1) {
            baseMapper.update(null, Wrappers.lambdaUpdate(Hold.class)
                .set(Hold::getStatus, 3).set(Hold::getCancelTime, new Date())
                .eq(Hold::getId, holdId).eq(Hold::getStatus, 1));
            // 到书保留的册回架，可借数 +1
            itemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
                .set(BookItem::getStatus, 1).eq(BookItem::getId, hold.getItemId()).eq(BookItem::getStatus, 3));
            bookMapper.update(null, Wrappers.lambdaUpdate(Book.class)
                .setSql("avail_qty = IFNULL(avail_qty, 0) + 1").eq(Book::getId, hold.getBookId()));
        } else {
            throw new ServiceException("当前状态不可取消");
        }
        return true;
    }

}

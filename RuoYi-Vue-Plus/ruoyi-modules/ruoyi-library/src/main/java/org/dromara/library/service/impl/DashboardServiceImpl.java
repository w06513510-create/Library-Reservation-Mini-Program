package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.library.domain.*;
import org.dromara.library.domain.vo.DashboardVo;
import org.dromara.library.domain.vo.NameValueVo;
import org.dromara.library.mapper.*;
import org.dromara.library.service.IDashboardService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据可视化大屏Service实现（聚合各 biz 表统计）
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class DashboardServiceImpl implements IDashboardService {

    private final SeatMapper seatMapper;
    private final RoomMapper roomMapper;
    private final ReservationMapper reservationMapper;
    private final BookMapper bookMapper;
    private final BookItemMapper bookItemMapper;
    private final LoanMapper loanMapper;
    private final HoldMapper holdMapper;
    private final ReaderMapper readerMapper;
    private final ViolationMapper violationMapper;

    @Override
    public DashboardVo overview() {
        DashboardVo vo = new DashboardVo();

        // —— 座位/空间 ——
        long seatTotal = c(seatMapper.selectCount(null));
        long seatDisabled = c(seatMapper.selectCount(Wrappers.<Seat>lambdaQuery().eq(Seat::getStatus, 1)));
        long reservationActive = c(reservationMapper.selectCount(Wrappers.<Reservation>lambdaQuery().in(Reservation::getStatus, 0, 1, 2)));
        long usable = Math.max(0, seatTotal - seatDisabled);
        long seatOccupied = Math.min(reservationActive, usable);
        long seatFree = Math.max(0, usable - seatOccupied);
        vo.setSeatTotal(seatTotal);
        vo.setSeatDisabled(seatDisabled);
        vo.setSeatOccupied(seatOccupied);
        vo.setSeatFree(seatFree);
        vo.setReservationActive(reservationActive);
        vo.setRoomTotal(c(roomMapper.selectCount(null)));

        // —— 图书/流通 ——
        long itemAvail = c(bookItemMapper.selectCount(status(1)));
        long itemBorrowed = c(bookItemMapper.selectCount(status(2)));
        long itemOnHold = c(bookItemMapper.selectCount(status(3)));
        long itemEditing = c(bookItemMapper.selectCount(status(0)));
        long itemLost = c(bookItemMapper.selectCount(Wrappers.<BookItem>lambdaQuery().in(BookItem::getStatus, 4, 5)));
        long itemWithdrawn = c(bookItemMapper.selectCount(status(6)));
        vo.setBookTitles(c(bookMapper.selectCount(null)));
        vo.setItemTotal(itemAvail + itemBorrowed + itemOnHold + itemEditing + itemLost);
        vo.setItemAvail(itemAvail);
        vo.setItemBorrowed(itemBorrowed);
        vo.setItemOnHold(itemOnHold);
        vo.setItemWithdrawn(itemWithdrawn);
        vo.setLoanOnLoan(c(loanMapper.selectCount(Wrappers.<Loan>lambdaQuery().in(Loan::getStatus, 0, 2))));
        vo.setLoanOverdue(c(loanMapper.selectCount(Wrappers.<Loan>lambdaQuery().eq(Loan::getStatus, 2))));
        vo.setHoldInTransit(c(holdMapper.selectCount(Wrappers.<Hold>lambdaQuery().in(Hold::getStatus, 0, 1))));
        vo.setTodayBorrow(c(loanMapper.selectCount(Wrappers.<Loan>lambdaQuery().apply("DATE(borrow_time) = CURDATE()"))));
        vo.setTodayReturn(c(loanMapper.selectCount(Wrappers.<Loan>lambdaQuery().apply("DATE(return_time) = CURDATE()"))));

        // —— 读者/信用 ——
        vo.setReaderTotal(c(readerMapper.selectCount(null)));
        vo.setBlacklistCount(c(readerMapper.selectCount(Wrappers.<Reader>lambdaQuery().eq(Reader::getBlacklistFlag, 1))));

        // —— 分布图 ——
        List<NameValueVo> seatStatus = new ArrayList<>();
        seatStatus.add(new NameValueVo("空闲", seatFree));
        seatStatus.add(new NameValueVo("占用", seatOccupied));
        seatStatus.add(new NameValueVo("停用", seatDisabled));
        vo.setSeatStatus(seatStatus);

        List<NameValueVo> itemStatus = new ArrayList<>();
        itemStatus.add(new NameValueVo("可借", itemAvail));
        itemStatus.add(new NameValueVo("借出", itemBorrowed));
        itemStatus.add(new NameValueVo("在预约架", itemOnHold));
        itemStatus.add(new NameValueVo("在编", itemEditing));
        itemStatus.add(new NameValueVo("遗失损坏", itemLost));
        itemStatus.add(new NameValueVo("已注销", itemWithdrawn));
        vo.setItemStatus(itemStatus);

        String[] vt = {"座位爽约", "暂离超时", "监督未落座", "未签退", "图书逾期", "预约架超期", "遗失损坏"};
        List<NameValueVo> violationTypes = new ArrayList<>();
        for (int i = 0; i < vt.length; i++) {
            long n = c(violationMapper.selectCount(Wrappers.<Violation>lambdaQuery().eq(Violation::getViolationType, i + 1)));
            violationTypes.add(new NameValueVo(vt[i], n));
        }
        vo.setViolationTypes(violationTypes);

        int[][] ranges = {{0, 20}, {20, 40}, {40, 60}, {60, 80}, {80, 101}};
        String[] rangeName = {"0-19", "20-39", "40-59", "60-79", "80-100"};
        List<NameValueVo> creditDist = new ArrayList<>();
        for (int i = 0; i < ranges.length; i++) {
            long n = c(readerMapper.selectCount(Wrappers.<Reader>lambdaQuery()
                .ge(Reader::getCreditScore, ranges[i][0]).lt(Reader::getCreditScore, ranges[i][1])));
            creditDist.add(new NameValueVo(rangeName[i], n));
        }
        vo.setCreditDist(creditDist);

        String[] rs = {"待签到", "使用中", "暂离中", "已完成", "已取消", "已违约"};
        List<NameValueVo> reservationStatus = new ArrayList<>();
        for (int i = 0; i < rs.length; i++) {
            long n = c(reservationMapper.selectCount(Wrappers.<Reservation>lambdaQuery().eq(Reservation::getStatus, i)));
            reservationStatus.add(new NameValueVo(rs[i], n));
        }
        vo.setReservationStatus(reservationStatus);

        return vo;
    }

    private LambdaQueryWrapper<BookItem> status(int s) {
        return Wrappers.<BookItem>lambdaQuery().eq(BookItem::getStatus, s);
    }

    private long c(Long v) {
        return v == null ? 0 : v;
    }

}

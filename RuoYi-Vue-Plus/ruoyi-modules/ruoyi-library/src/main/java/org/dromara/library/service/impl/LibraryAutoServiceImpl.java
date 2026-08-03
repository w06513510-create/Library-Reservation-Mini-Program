package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.library.domain.*;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.*;
import org.dromara.library.service.ICreditService;
import org.dromara.library.service.ILibraryAutoService;
import org.dromara.library.service.IViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 定时任务自动处置实现。状态跃迁用 CAS，命中即记违约/扣分（走 ViolationService 中央入口）。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class LibraryAutoServiceImpl implements ILibraryAutoService {

    private final ReservationMapper reservationMapper;
    private final LoanMapper loanMapper;
    private final HoldMapper holdMapper;
    private final BookItemMapper bookItemMapper;
    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final CreditLogMapper creditLogMapper;
    private final SuperviseMapper superviseMapper;
    private final IViolationService violationService;
    private final ICreditService creditService;
    private final RuleConfigHelper ruleConfig;

    private static final long MIN_MS = 60 * 1000L;
    private static final long DAY_MS = 24L * 3600 * 1000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanNoShow() {
        int checkinWindowMin = ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "checkin_window_min", 30);
        Date threshold = new Date(System.currentTimeMillis() - (long) checkinWindowMin * MIN_MS);
        List<Reservation> list = reservationMapper.selectList(Wrappers.<Reservation>lambdaQuery()
            .eq(Reservation::getStatus, 0).lt(Reservation::getStartTime, threshold));
        int n = 0;
        for (Reservation r : list) {
            int rows = reservationMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
                .set(Reservation::getStatus, 5).set(Reservation::getRemark, "超时未签到自动释放")
                .eq(Reservation::getId, r.getId()).eq(Reservation::getStatus, 0));
            if (rows == 1) {
                violationService.recordViolation(r.getReaderId(), 1, null, "reservation", r.getId(), 0);
                n++;
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanAwayTimeout() {
        int awayKeepMin = ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "away_keep_min", 30);
        long now = System.currentTimeMillis();
        // 取所有暂离中的预约，逐单按「暂离起始时刻是否落在就餐时段」决定保留时长（就餐时段更久）
        List<Reservation> list = reservationMapper.selectList(Wrappers.<Reservation>lambdaQuery()
            .eq(Reservation::getStatus, 2).isNotNull(Reservation::getAwayStartTime));
        int n = 0;
        for (Reservation r : list) {
            int keepMin = effectiveKeepMinutes(r.getAwayStartTime(), awayKeepMin);
            if (now - r.getAwayStartTime().getTime() < (long) keepMin * MIN_MS) {
                continue; // 尚在保留期内
            }
            int rows = reservationMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
                .set(Reservation::getStatus, 5).set(Reservation::getRemark, "暂离超时自动释放")
                .eq(Reservation::getId, r.getId()).eq(Reservation::getStatus, 2));
            if (rows == 1) {
                violationService.recordViolation(r.getReaderId(), 2, null, "reservation", r.getId(), 0);
                n++;
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanExpired() {
        int graceMin = ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "overdue_grace_min", 10);
        long now = System.currentTimeMillis();
        // 取所有已到结束时间但仍占用的预约，逐单按「应签退时刻是否落在就餐时段」决定宽限（就餐时段更久）
        List<Reservation> list = reservationMapper.selectList(Wrappers.<Reservation>lambdaQuery()
            .in(Reservation::getStatus, 1, 2).lt(Reservation::getEndTime, new Date()));
        int n = 0;
        for (Reservation r : list) {
            int keepMin = effectiveKeepMinutes(r.getEndTime(), graceMin);
            if (r.getEndTime() != null && now - r.getEndTime().getTime() < (long) keepMin * MIN_MS) {
                continue; // 尚在宽限期内
            }
            int rows = reservationMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
                .set(Reservation::getStatus, 5).set(Reservation::getActualEndTime, new Date()).set(Reservation::getRemark, "到期未签退自动释放")
                .eq(Reservation::getId, r.getId()).in(Reservation::getStatus, 1, 2));
            if (rows == 1) {
                violationService.recordViolation(r.getReaderId(), 4, null, "reservation", r.getId(), 0);
                n++;
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanOverdueBooks() {
        Date now = new Date();
        List<Loan> list = loanMapper.selectList(Wrappers.<Loan>lambdaQuery()
            .eq(Loan::getStatus, 0).lt(Loan::getDueTime, now));
        int n = 0;
        for (Loan l : list) {
            int rows = loanMapper.update(null, Wrappers.lambdaUpdate(Loan.class)
                .set(Loan::getStatus, 2).set(Loan::getOverdueFlag, 1)
                .eq(Loan::getId, l.getId()).eq(Loan::getStatus, 0));
            if (rows == 1) {
                violationService.recordViolation(l.getReaderId(), 5, null, "loan", l.getId(), 0);
                n++;
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanHoldExpired() {
        Date now = new Date();
        List<Hold> list = holdMapper.selectList(Wrappers.<Hold>lambdaQuery()
            .eq(Hold::getStatus, 1).lt(Hold::getHoldDeadline, now));
        int n = 0;
        for (Hold h : list) {
            int rows = holdMapper.update(null, Wrappers.lambdaUpdate(Hold.class)
                .set(Hold::getStatus, 4).eq(Hold::getId, h.getId()).eq(Hold::getStatus, 1));
            if (rows == 1) {
                // 预约架册回架 3→1，可借数 +1
                bookItemMapper.update(null, Wrappers.lambdaUpdate(BookItem.class)
                    .set(BookItem::getStatus, 1).eq(BookItem::getId, h.getItemId()).eq(BookItem::getStatus, 3));
                bookMapper.update(null, Wrappers.lambdaUpdate(Book.class)
                    .setSql("avail_qty = IFNULL(avail_qty, 0) + 1").eq(Book::getId, h.getBookId()));
                violationService.recordViolation(h.getReaderId(), 6, null, "hold", h.getId(), 0);
                n++;
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanCreditDecay() {
        int decayDays = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "decay_days", 30);
        int decayScore = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "decay_score", 5);
        int scoreMax = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "score_max", 100);
        Date decayThreshold = new Date(System.currentTimeMillis() - (long) decayDays * DAY_MS);
        List<Reader> readers = readerMapper.selectList(Wrappers.<Reader>lambdaQuery().lt(Reader::getCreditScore, scoreMax));
        int n = 0;
        for (Reader rd : readers) {
            // 最近一次信用变动早于衰减周期（即已稳定满周期无扣分）才回补
            List<CreditLog> logs = creditLogMapper.selectList(Wrappers.<CreditLog>lambdaQuery()
                .eq(CreditLog::getReaderId, rd.getUserId()).orderByDesc(CreditLog::getCreateTime).last("limit 1"));
            if (logs.isEmpty() || logs.get(0).getCreateTime() == null) {
                continue;
            }
            if (logs.get(0).getCreateTime().before(decayThreshold)) {
                creditService.changeCredit(rd.getUserId(), decayScore, 10, "无违约时间衰减恢复", "decay", null);
                n++;
            }
        }
        return n;
    }

    /**
     * 就餐时段特殊保留：refTime（暂离起始 / 应签退时刻）落在午餐或晚餐窗口内时，返回该餐段配置的保留时长，
     * 否则返回普通时长。参考真实馆规——南开/华师大/清华/武大/北师大/哈工大等普遍设就餐延长保留。
     */
    private int effectiveKeepMinutes(Date refTime, int normalKeep) {
        if (refTime == null) {
            return normalKeep;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(refTime);
        int minuteOfDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        Integer ls = ruleConfig.getMinuteOfDay(RuleConfigHelper.GROUP_SEAT, "lunch_start");
        Integer le = ruleConfig.getMinuteOfDay(RuleConfigHelper.GROUP_SEAT, "lunch_end");
        if (ls != null && le != null && minuteOfDay >= ls && minuteOfDay < le) {
            return ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "lunch_keep_min", normalKeep);
        }
        Integer ds = ruleConfig.getMinuteOfDay(RuleConfigHelper.GROUP_SEAT, "dinner_start");
        Integer de = ruleConfig.getMinuteOfDay(RuleConfigHelper.GROUP_SEAT, "dinner_end");
        if (ds != null && de != null && minuteOfDay >= ds && minuteOfDay < de) {
            return ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "dinner_keep_min", normalKeep);
        }
        return normalKeep;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanSuperviseTimeout() {
        Date now = new Date();
        List<Supervise> list = superviseMapper.selectList(Wrappers.<Supervise>lambdaQuery()
            .eq(Supervise::getStatus, 0).lt(Supervise::getDeadline, now));
        int n = 0;
        for (Supervise s : list) {
            int rows = superviseMapper.update(null, Wrappers.lambdaUpdate(Supervise.class)
                .set(Supervise::getStatus, 2).set(Supervise::getResolveTime, new Date())
                .eq(Supervise::getId, s.getId()).eq(Supervise::getStatus, 0));
            if (rows == 1) {
                // 被监督预约（使用中/暂离中）→ 已违约释放，并记"监督未落座"违约（type=3）
                Reservation r = reservationMapper.selectById(s.getReservationId());
                reservationMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
                    .set(Reservation::getStatus, 5).set(Reservation::getActualEndTime, new Date())
                    .set(Reservation::getRemark, "占座监督超时未落座自动释放")
                    .eq(Reservation::getId, s.getReservationId()).in(Reservation::getStatus, 1, 2));
                if (r != null) {
                    violationService.recordViolation(r.getReaderId(), 3, null, "supervise", s.getId(), 0);
                }
                n++;
            }
        }
        return n;
    }

}

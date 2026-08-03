package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.*;
import org.dromara.library.domain.bo.ReservationBo;
import org.dromara.library.domain.vo.ReservationVo;
import org.dromara.library.domain.vo.SeatStatusVo;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.*;
import org.dromara.library.service.ICreditService;
import org.dromara.library.service.IReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 座位预约状态机Service实现
 * 状态跃迁一律用 CAS（UPDATE ... WHERE id=? AND status=前置），影响行数=1 才继续，保证幂等/并发安全。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements IReservationService {

    private final ReservationMapper baseMapper;
    private final SeatMapper seatMapper;
    private final AreaMapper areaMapper;
    private final FloorMapper floorMapper;
    private final ReaderMapper readerMapper;
    private final DeskMapper deskMapper;
    private final ICreditService creditService;
    private final RuleConfigHelper ruleConfig;

    @Override
    public ReservationVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ReservationVo> queryPageList(ReservationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Reservation> lqw = buildQueryWrapper(bo);
        Page<ReservationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ReservationVo> queryList(ReservationBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Reservation> buildQueryWrapper(ReservationBo bo) {
        LambdaQueryWrapper<Reservation> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Reservation::getReaderId, bo.getReaderId());
        lqw.eq(bo.getSeatId() != null, Reservation::getSeatId, bo.getSeatId());
        lqw.eq(bo.getStatus() != null, Reservation::getStatus, bo.getStatus());
        lqw.eq(bo.getReserveDate() != null, Reservation::getReserveDate, bo.getReserveDate());
        lqw.orderByDesc(Reservation::getCreateTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createReservation(ReservationBo bo) {
        // 1. 前置校验：黑名单 / 信用阈值
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, bo.getReaderId()));
        if (reader != null) {
            if (reader.getBlacklistFlag() != null && reader.getBlacklistFlag() == 1) {
                throw new ServiceException("该读者在黑名单中，暂停预约");
            }
            int pauseScore = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "pause_score", 20);
            if (reader.getCreditScore() != null && reader.getCreditScore() < pauseScore) {
                throw new ServiceException("该读者信用分过低（<" + pauseScore + "），暂停预约");
            }
        }
        // 2. 座位可用性（悲观行锁：锁住该座位行，串行化同座并发约座，杜绝重复占用）
        Seat seat = seatMapper.selectForUpdate(bo.getSeatId());
        if (seat == null) {
            throw new ServiceException("座位不存在");
        }
        if (seat.getStatus() != null && seat.getStatus() != 0) {
            throw new ServiceException("座位已停用，无法预约");
        }
        // 3. 座位资源不变式：同座同时段至多一条有效占用（状态 0/1/2）；行锁下检查+插入原子，防并发双订
        Long occupied = baseMapper.selectCount(Wrappers.<Reservation>lambdaQuery()
            .eq(Reservation::getSeatId, bo.getSeatId())
            .in(Reservation::getStatus, 0, 1, 2)
            .lt(Reservation::getStartTime, bo.getEndTime())
            .gt(Reservation::getEndTime, bo.getStartTime()));
        if (occupied != null && occupied > 0) {
            throw new ServiceException("该座位在所选时段已被占用");
        }
        // 4. 冗余场馆/楼层/区域（由座位推导）
        Long areaId = seat.getAreaId();
        Long floorId = null;
        Long venueId = null;
        if (areaId != null) {
            Area area = areaMapper.selectById(areaId);
            if (area != null) {
                floorId = area.getFloorId();
                if (floorId != null) {
                    Floor floor = floorMapper.selectById(floorId);
                    if (floor != null) {
                        venueId = floor.getVenueId();
                    }
                }
            }
        }
        Reservation add = MapstructUtils.convert(bo, Reservation.class);
        add.setAreaId(areaId);
        add.setFloorId(floorId);
        add.setVenueId(venueId);
        add.setStatus(0);
        add.setAwayCount(0);
        if (add.getSource() == null) {
            add.setSource(1);
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkIn(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 1)
            .set(Reservation::getCheckInTime, new Date())
            .eq(Reservation::getId, id)
            .eq(Reservation::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法签到（需为待签到）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean away(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 2)
            .set(Reservation::getAwayStartTime, new Date())
            .setSql("away_count = IFNULL(away_count, 0) + 1")
            .eq(Reservation::getId, id)
            .eq(Reservation::getStatus, 1));
        if (rows != 1) {
            throw new ServiceException("当前状态无法暂离（需为使用中）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean back(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 1)
            .set(Reservation::getAwayStartTime, null)
            .eq(Reservation::getId, id)
            .eq(Reservation::getStatus, 2));
        if (rows != 1) {
            throw new ServiceException("当前状态无法返回落座（需为暂离中）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean leave(Long id) {
        Reservation r = baseMapper.selectById(id);
        if (r == null) {
            throw new ServiceException("预约单不存在");
        }
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 3)
            .set(Reservation::getActualEndTime, new Date())
            .eq(Reservation::getId, id)
            .in(Reservation::getStatus, 1, 2));
        if (rows != 1) {
            throw new ServiceException("当前状态无法退座（需为使用中/暂离中）");
        }
        // 履约加分（事件触发，写信用流水）+ 守信次数 +1
        int bonus = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "perform_bonus", 1);
        if (bonus != 0) {
            creditService.changeCredit(r.getReaderId(), bonus, 9, "按时退座履约", "reservation", id);
        }
        readerMapper.update(null, Wrappers.lambdaUpdate(Reader.class)
            .setSql("perform_count = IFNULL(perform_count, 0) + 1")
            .eq(Reader::getUserId, r.getReaderId()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id) {
        Reservation r = baseMapper.selectById(id);
        if (r == null) {
            throw new ServiceException("预约单不存在");
        }
        // 每日取消上限：同一读者当日已取消次数达上限则拒绝（对齐真实馆规，如华师大/贵医；0=不限）
        int dailyLimit = ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "daily_cancel_limit", 2);
        if (dailyLimit > 0) {
            // 用字符串与 datetime 列比较，避免 Date 经 JDBC 时区偏移（与 seatStatus 一致）
            String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
            Long cancelledToday = baseMapper.selectCount(Wrappers.<Reservation>lambdaQuery()
                .eq(Reservation::getReaderId, r.getReaderId())
                .eq(Reservation::getStatus, 4)
                .ge(Reservation::getCancelTime, todayStart));
            if (cancelledToday != null && cancelledToday >= dailyLimit) {
                throw new ServiceException("今日取消预约次数已达上限（" + dailyLimit + " 次），请明日再试");
            }
        }
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 4)
            .set(Reservation::getCancelTime, new Date())
            .eq(Reservation::getId, id)
            .eq(Reservation::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法取消（仅待签到可取消）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean extend(Long id, Date newEndTime) {
        Reservation r = baseMapper.selectById(id);
        if (r == null) {
            throw new ServiceException("预约单不存在");
        }
        if (r.getStatus() == null || (r.getStatus() != 1 && r.getStatus() != 2)) {
            throw new ServiceException("仅使用中/暂离中可续座");
        }
        if (newEndTime == null || !newEndTime.after(r.getEndTime())) {
            throw new ServiceException("续座结束时间需晚于当前结束时间");
        }
        // 后续时段是否被同座其它有效预约占用
        Long occupied = baseMapper.selectCount(Wrappers.<Reservation>lambdaQuery()
            .eq(Reservation::getSeatId, r.getSeatId())
            .ne(Reservation::getId, id)
            .in(Reservation::getStatus, 0, 1, 2)
            .lt(Reservation::getStartTime, newEndTime)
            .gt(Reservation::getEndTime, r.getEndTime()));
        if (occupied != null && occupied > 0) {
            throw new ServiceException("后续时段该座位已被占用，无法续座");
        }
        return baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getEndTime, newEndTime)
            .eq(Reservation::getId, id)
            .in(Reservation::getStatus, 1, 2)) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean forceRelease(Long id, String reason) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Reservation.class)
            .set(Reservation::getStatus, 3)
            .set(Reservation::getActualEndTime, new Date())
            .set(Reservation::getRemark, "管理员强制释放：" + (reason == null ? "" : reason))
            .eq(Reservation::getId, id)
            .in(Reservation::getStatus, 0, 1, 2));
        if (rows != 1) {
            throw new ServiceException("当前状态无法强制释放");
        }
        return true;
    }

    @Override
    public List<SeatStatusVo> seatStatus(Long floorId, String start, String end) {
        List<SeatStatusVo> result = new ArrayList<>();
        List<Area> areas = areaMapper.selectList(Wrappers.<Area>lambdaQuery().eq(Area::getFloorId, floorId));
        if (areas.isEmpty()) {
            return result;
        }
        Map<Long, String> areaNames = areas.stream().collect(Collectors.toMap(Area::getId, Area::getAreaName, (a, b) -> a));
        List<Long> areaIds = areas.stream().map(Area::getId).collect(Collectors.toList());
        List<Seat> seats = seatMapper.selectList(Wrappers.<Seat>lambdaQuery()
            .in(Seat::getAreaId, areaIds).orderByAsc(Seat::getSeatNo));
        if (seats.isEmpty()) {
            return result;
        }
        List<Long> seatIds = seats.stream().map(Seat::getId).collect(Collectors.toList());
        // 所属桌子（工位组）：一次查出，供平面图「一桌展开多座」成组渲染
        List<Long> deskIds = seats.stream().map(Seat::getDeskId).filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Desk> deskMap = new java.util.HashMap<>();
        if (!deskIds.isEmpty()) {
            deskMapper.selectList(Wrappers.<Desk>lambdaQuery().in(Desk::getId, deskIds))
                .forEach(d -> deskMap.put(d.getId(), d));
        }
        // 所选时段被占用的座位（有效占用 0待签到/1使用中/2暂离中，且时段重叠）
        Set<Long> occupied = new HashSet<>();
        if (start != null && !start.isBlank() && end != null && !end.isBlank()) {
            // 时间用字符串与 datetime 列比较（MySQL 隐式转换），与手写 SQL 一致，避免 Date 经 JDBC 时区偏移
            List<Reservation> active = baseMapper.selectList(Wrappers.<Reservation>lambdaQuery()
                .in(Reservation::getSeatId, seatIds)
                .in(Reservation::getStatus, 0, 1, 2)
                .lt(Reservation::getStartTime, end)
                .gt(Reservation::getEndTime, start));
            active.forEach(r -> occupied.add(r.getSeatId()));
        }
        for (Seat s : seats) {
            SeatStatusVo vo = new SeatStatusVo();
            vo.setId(s.getId());
            vo.setSeatNo(s.getSeatNo());
            vo.setAreaId(s.getAreaId());
            vo.setAreaName(areaNames.get(s.getAreaId()));
            vo.setSeatType(s.getSeatType());
            vo.setHasPower(s.getHasPower());
            vo.setPosX(s.getPosX());
            vo.setPosY(s.getPosY());
            vo.setOffsetX(s.getOffsetX());
            vo.setOffsetY(s.getOffsetY());
            vo.setSeatStatus(s.getStatus());
            vo.setOccupied(occupied.contains(s.getId()));
            // 填充所属桌子几何信息
            vo.setDeskId(s.getDeskId());
            Desk desk = s.getDeskId() == null ? null : deskMap.get(s.getDeskId());
            if (desk != null) {
                vo.setDeskNo(desk.getDeskNo());
                vo.setCapacity(desk.getCapacity());
                vo.setShape(desk.getShape());
                vo.setDeskPosX(desk.getPosX());
                vo.setDeskPosY(desk.getPosY());
                vo.setDeskWidth(desk.getWidth());
                vo.setDeskHeight(desk.getHeight());
                vo.setDeskRotation(desk.getRotation());
            }
            result.add(vo);
        }
        return result;
    }

}

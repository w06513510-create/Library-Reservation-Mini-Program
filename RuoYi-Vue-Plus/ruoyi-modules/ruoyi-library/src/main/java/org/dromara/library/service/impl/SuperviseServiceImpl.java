package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Reader;
import org.dromara.library.domain.Reservation;
import org.dromara.library.domain.Seat;
import org.dromara.library.domain.Supervise;
import org.dromara.library.domain.bo.SuperviseBo;
import org.dromara.library.domain.vo.SuperviseVo;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.mapper.ReservationMapper;
import org.dromara.library.mapper.SeatMapper;
import org.dromara.library.mapper.SuperviseMapper;
import org.dromara.library.service.ISuperviseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 占座监督Service业务层处理
 * 只含 CRUD + 举报(report) + 手动解除(reseat)。
 * 超时→违约→释放座位的定时扫描由 SnailJob 另行接入，本类不含超时/违约/释放逻辑。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class SuperviseServiceImpl implements ISuperviseService {

    private final SuperviseMapper baseMapper;
    private final ReservationMapper reservationMapper;
    private final ReaderMapper readerMapper;
    private final SeatMapper seatMapper;
    private final RuleConfigHelper ruleConfig;

    @Override
    public SuperviseVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SuperviseVo> queryPageList(SuperviseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Supervise> lqw = buildQueryWrapper(bo);
        Page<SuperviseVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        fillNames(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public List<SuperviseVo> queryList(SuperviseBo bo) {
        List<SuperviseVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        fillNames(list);
        return list;
    }

    /** 批量把 举报人ID→姓名（学号）、座位ID→座位编号 填进 VO，供列表以人话展示（SOP 06 §5） */
    private void fillNames(List<SuperviseVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> reporterIds = list.stream().map(SuperviseVo::getReporterId).filter(Objects::nonNull).distinct().toList();
        List<Long> seatIds = list.stream().map(SuperviseVo::getSeatId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> reporterNames = new HashMap<>();
        if (!reporterIds.isEmpty()) {
            readerMapper.selectList(Wrappers.<Reader>lambdaQuery().in(Reader::getUserId, reporterIds))
                .forEach(r -> reporterNames.put(r.getUserId(), fmtReader(r)));
        }
        Map<Long, String> seatNos = new HashMap<>();
        if (!seatIds.isEmpty()) {
            seatMapper.selectList(Wrappers.<Seat>lambdaQuery().in(Seat::getId, seatIds))
                .forEach(s -> seatNos.put(s.getId(), s.getSeatNo()));
        }
        for (SuperviseVo vo : list) {
            if (vo.getReporterId() != null) {
                vo.setReporterName(reporterNames.get(vo.getReporterId()));
            }
            if (vo.getSeatId() != null) {
                vo.setSeatNo(seatNos.get(vo.getSeatId()));
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

    private LambdaQueryWrapper<Supervise> buildQueryWrapper(SuperviseBo bo) {
        LambdaQueryWrapper<Supervise> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReservationId() != null, Supervise::getReservationId, bo.getReservationId());
        lqw.eq(bo.getReporterId() != null, Supervise::getReporterId, bo.getReporterId());
        lqw.eq(bo.getStatus() != null, Supervise::getStatus, bo.getStatus());
        lqw.orderByDesc(Supervise::getCreateTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean report(SuperviseBo bo) {
        // 1. 取被监督的座位预约单
        Reservation reservation = reservationMapper.selectById(bo.getReservationId());
        if (reservation == null) {
            throw new ServiceException("预约单不存在");
        }
        // 2. 仅使用中（status=1）的座位可被举报占座；暂离中(2)属正常暂离，不可举报
        if (reservation.getStatus() == null || reservation.getStatus() != 1) {
            throw new ServiceException("仅使用中的座位可发起占座监督（暂离中不可举报）");
        }
        // 3. 同一预约单不可有多条进行中的监督
        Long count = baseMapper.selectCount(Wrappers.<Supervise>lambdaQuery()
            .eq(Supervise::getReservationId, bo.getReservationId())
            .eq(Supervise::getStatus, 0));
        if (count != null && count > 0) {
            throw new ServiceException("该座位已有进行中的监督");
        }
        // 4. 组装并写入：座位由预约单推导，落座截止 = 现在 + 监督响应时长（可配，默认20分钟对齐超星）
        int responseMinutes = ruleConfig.getInt(RuleConfigHelper.GROUP_SEAT, "supervise_deadline_min", 20);
        Supervise add = MapstructUtils.convert(bo, Supervise.class);
        add.setSeatId(reservation.getSeatId());
        add.setReportTime(new Date());
        add.setDeadline(new Date(System.currentTimeMillis() + (long) responseMinutes * 60 * 1000L));
        add.setStatus(0);
        add.setResolveTime(null);
        return baseMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reseat(Long id) {
        // 原用户已按时落座，监督解除：CAS 0→1，并发/幂等安全
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Supervise.class)
            .set(Supervise::getStatus, 1)
            .set(Supervise::getResolveTime, new Date())
            .eq(Supervise::getId, id)
            .eq(Supervise::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前监督无法解除（需为进行中）");
        }
        return true;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

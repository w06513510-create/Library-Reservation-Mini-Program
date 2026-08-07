package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.library.domain.Room;
import org.dromara.library.domain.RoomReservation;
import org.dromara.library.domain.bo.RoomReservationBo;
import org.dromara.library.domain.vo.RoomReservationVo;
import org.dromara.library.mapper.RoomMapper;
import org.dromara.library.mapper.RoomReservationMapper;
import org.dromara.library.service.IRoomReservationService;
import org.dromara.message.utils.NotificationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 研讨间预约状态机Service实现
 * 状态跃迁一律用 CAS（UPDATE ... WHERE id=? AND status=前置），影响行数=1 才继续，保证幂等/并发安全。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class RoomReservationServiceImpl implements IRoomReservationService {

    private final RoomReservationMapper baseMapper;
    private final RoomMapper roomMapper;

    @Override
    public RoomReservationVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<RoomReservationVo> queryPageList(RoomReservationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<RoomReservation> lqw = buildQueryWrapper(bo);
        Page<RoomReservationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<RoomReservationVo> queryList(RoomReservationBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<RoomReservation> buildQueryWrapper(RoomReservationBo bo) {
        LambdaQueryWrapper<RoomReservation> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, RoomReservation::getReaderId, bo.getReaderId());
        lqw.eq(bo.getRoomId() != null, RoomReservation::getRoomId, bo.getRoomId());
        lqw.eq(bo.getStatus() != null, RoomReservation::getStatus, bo.getStatus());
        lqw.eq(bo.getReserveDate() != null, RoomReservation::getReserveDate, bo.getReserveDate());
        lqw.orderByDesc(RoomReservation::getCreateTime);
        return lqw;
    }

    @Override
    public Boolean insertByBo(RoomReservationBo bo) {
        // 新增统一走预约创建逻辑（悲观行锁 + 时段重叠校验 + 按需审批初始状态）
        return createReservation(bo);
    }

    @Override
    public Boolean updateByBo(RoomReservationBo bo) {
        RoomReservation update = MapstructUtils.convert(bo, RoomReservation.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createReservation(RoomReservationBo bo) {
        // 1. 研讨间可用性（悲观行锁：锁住该研讨间行，串行化同研讨间并发预约，杜绝重复占用）
        Room room = roomMapper.selectForUpdate(bo.getRoomId());
        if (room == null) {
            throw new ServiceException("研讨间不存在");
        }
        if (room.getStatus() != null && room.getStatus() != 0) {
            throw new ServiceException("研讨间已停用，无法预约");
        }
        // 2. 研讨间资源不变式：同研讨间同时段至多一条有效占用（状态 0/1/2）；行锁下检查+插入原子，防并发双订
        Long occupied = baseMapper.selectCount(Wrappers.<RoomReservation>lambdaQuery()
            .eq(RoomReservation::getRoomId, bo.getRoomId())
            .in(RoomReservation::getStatus, 0, 1, 2)
            .lt(RoomReservation::getStartTime, bo.getEndTime())
            .gt(RoomReservation::getEndTime, bo.getStartTime()));
        if (occupied != null && occupied > 0) {
            throw new ServiceException("该研讨间在所选时段已被预约");
        }
        // 3. 初始状态：需审批则待审批(0)，否则已通过待使用(1)
        RoomReservation add = MapstructUtils.convert(bo, RoomReservation.class);
        if (room.getNeedApprove() != null && room.getNeedApprove() == 1) {
            add.setStatus(0);
        } else {
            add.setStatus(1);
        }
        return baseMapper.insert(add) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(RoomReservation.class)
            .set(RoomReservation::getStatus, 1)
            .set(RoomReservation::getApproveBy, LoginHelper.getUserId())
            .set(RoomReservation::getApproveTime, new Date())
            .eq(RoomReservation::getId, id)
            .eq(RoomReservation::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法审批通过（需为待审批）");
        }
        RoomReservation r = baseMapper.selectById(id);
        if (r != null) {
            NotificationHelper.send(r.getReaderId(), "研讨间预约已通过",
                "你的研讨间预约已通过审批，请按预约时段到场使用。", "roomReservation", id);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String reason) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(RoomReservation.class)
            .set(RoomReservation::getStatus, 5)
            .set(RoomReservation::getApproveBy, LoginHelper.getUserId())
            .set(RoomReservation::getApproveTime, new Date())
            .set(RoomReservation::getRejectReason, reason)
            .eq(RoomReservation::getId, id)
            .eq(RoomReservation::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法驳回（需为待审批）");
        }
        RoomReservation r = baseMapper.selectById(id);
        if (r != null) {
            NotificationHelper.send(r.getReaderId(), "研讨间预约被驳回",
                "你的研讨间预约未通过审批。" + (reason != null && !reason.isBlank() ? "原因：" + reason : ""), "roomReservation", id);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkIn(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(RoomReservation.class)
            .set(RoomReservation::getStatus, 2)
            .set(RoomReservation::getCheckInTime, new Date())
            .eq(RoomReservation::getId, id)
            .eq(RoomReservation::getStatus, 1));
        if (rows != 1) {
            throw new ServiceException("当前状态无法签到（需为已通过待使用）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean complete(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(RoomReservation.class)
            .set(RoomReservation::getStatus, 3)
            .eq(RoomReservation::getId, id)
            .eq(RoomReservation::getStatus, 2));
        if (rows != 1) {
            throw new ServiceException("当前状态无法完成（需为使用中）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(RoomReservation.class)
            .set(RoomReservation::getStatus, 4)
            .eq(RoomReservation::getId, id)
            .in(RoomReservation::getStatus, 0, 1));
        if (rows != 1) {
            throw new ServiceException("当前状态无法取消（仅待审批/待使用可取消）");
        }
        return true;
    }

}

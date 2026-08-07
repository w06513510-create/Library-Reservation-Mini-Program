package org.dromara.library.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.RoomBo;
import org.dromara.library.domain.bo.RoomReservationBo;
import org.dromara.library.domain.vo.RoomReservationVo;
import org.dromara.library.domain.vo.RoomVo;
import org.dromara.library.service.IRoomReservationService;
import org.dromara.library.service.IRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * C端(小程序) 研讨间预约 Controller。
 * <p>薄封装复用 {@link IRoomReservationService}：并发行锁 / 时段重叠 / 最少人数 / 按需审批全在 Service。
 * readerId 强制取当前登录读者、动作前校归属；审批(approve/reject)与完成(complete)留管理端，C 端只暴露约/签到/取消。
 *
 * @author library
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/library/room")
public class AppRoomController {

    private final IRoomService roomService;
    private final IRoomReservationService roomReservationService;

    /** 研讨间列表（可按楼层筛；只列正常状态，含容量/最少人数/是否需审批） */
    @GetMapping("/list")
    public R<List<RoomVo>> list(@RequestParam(required = false) Long floorId) {
        RoomBo bo = new RoomBo();
        bo.setFloorId(floorId);
        bo.setStatus(0);
        return R.ok(roomService.queryList(bo));
    }

    /** 预约研讨间：readerId 强制当前读者；只能约尚未开始的时段 */
    @RepeatSubmit()
    @PostMapping("/reserve")
    public R<Void> reserve(@RequestBody RoomReservationBo bo) {
        if (bo.getRoomId() == null || bo.getReserveDate() == null
            || bo.getStartTime() == null || bo.getEndTime() == null) {
            throw new ServiceException("研讨间、预约日期、时段起止不能为空");
        }
        if (!bo.getEndTime().after(bo.getStartTime())) {
            throw new ServiceException("结束时间须晚于开始时间");
        }
        if (!bo.getStartTime().after(new Date())) {
            throw new ServiceException("只能预约尚未开始的时段，开始时间须晚于当前时间");
        }
        bo.setId(null);
        bo.setReaderId(AppLoginHelper.getUserId());
        return toAjax(roomReservationService.createReservation(bo));
    }

    /** 我的研讨间预约（状态：0待审批 1待使用 2使用中 3已完成 4已取消 5已驳回 6已违约） */
    @GetMapping("/reservations")
    public TableDataInfo<RoomReservationVo> myReservations(@RequestParam(required = false) Integer status, PageQuery pageQuery) {
        RoomReservationBo bo = new RoomReservationBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setStatus(status);
        return roomReservationService.queryPageList(bo, pageQuery);
    }

    /** 签到 1→2 */
    @PutMapping("/checkIn/{id}")
    public R<Void> checkIn(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(roomReservationService.checkIn(id));
    }

    /** 取消 {0,1}→4 */
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(roomReservationService.cancel(id));
    }

    /** 归属校验：研讨间预约必须属于当前登录读者，否则 403 */
    private void assertOwn(Long id) {
        RoomReservationVo vo = roomReservationService.queryById(id);
        if (vo == null) {
            throw new ServiceException("预约不存在");
        }
        if (!Objects.equals(vo.getReaderId(), AppLoginHelper.getUserId())) {
            throw new ServiceException("无权操作他人预约", 403);
        }
    }

    private R<Void> toAjax(boolean b) {
        return b ? R.ok() : R.fail();
    }

}

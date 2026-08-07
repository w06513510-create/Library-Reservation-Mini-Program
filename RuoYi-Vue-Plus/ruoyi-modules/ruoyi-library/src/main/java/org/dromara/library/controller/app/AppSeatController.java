package org.dromara.library.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.FloorBo;
import org.dromara.library.domain.bo.ReservationBo;
import org.dromara.library.domain.vo.FloorVo;
import org.dromara.library.domain.vo.ReservationVo;
import org.dromara.library.domain.vo.SeatStatusVo;
import org.dromara.library.service.IFloorService;
import org.dromara.library.service.IReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * C端(小程序) 选座预约 Controller。
 * <p>薄封装复用 {@link IReservationService}：并发行锁 / 时段重叠 / 信用黑名单 / CAS 状态机全在 Service 一套逻辑，
 * 本层只负责「把 readerId 强制钉到当前登录读者、动作前校归属」，杜绝越权操作他人预约。
 * <p>走 app_user 会话（{@link AppLoginHelper#getUserId()}，非 C 端 token 抛 403），不挂 sys_user 权限点。
 *
 * @author library
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/library/seat")
public class AppSeatController {

    private final IReservationService reservationService;
    private final IFloorService floorService;

    /** 楼层列表（选座第一步） */
    @GetMapping("/floors")
    public R<List<FloorVo>> floors() {
        return R.ok(floorService.queryList(new FloorBo()));
    }

    /** 平面图选座：某楼层座位坐标 + 所选时段占用状态（复用亮点①，一桌多座成组信息随 VO 下发） */
    @GetMapping("/status")
    public R<List<SeatStatusVo>> seatStatus(@RequestParam Long floorId,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime) {
        return R.ok(reservationService.seatStatus(floorId, startTime, endTime));
    }

    /** 约座：readerId 强制取当前登录读者，忽略前端传入，防越权 */
    @RepeatSubmit()
    @PostMapping("/reserve")
    public R<Void> reserve(@RequestBody ReservationBo bo) {
        if (bo.getSeatId() == null || bo.getReserveDate() == null
            || bo.getStartTime() == null || bo.getEndTime() == null) {
            throw new ServiceException("座位、预约日期、时段起止不能为空");
        }
        bo.setId(null);
        bo.setReaderId(AppLoginHelper.getUserId());
        return toAjax(reservationService.createReservation(bo));
    }

    /** 我的预约（可按状态过滤：0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约） */
    @GetMapping("/reservations")
    public TableDataInfo<ReservationVo> myReservations(@RequestParam(required = false) Integer status, PageQuery pageQuery) {
        ReservationBo bo = new ReservationBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setStatus(status);
        return reservationService.queryPageList(bo, pageQuery);
    }

    /** 签到 0→1 */
    @PutMapping("/checkIn/{id}")
    public R<Void> checkIn(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(reservationService.checkIn(id));
    }

    /** 暂离 1→2 */
    @PutMapping("/away/{id}")
    public R<Void> away(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(reservationService.away(id));
    }

    /** 返回落座 2→1 */
    @PutMapping("/back/{id}")
    public R<Void> back(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(reservationService.back(id));
    }

    /** 退座/签退 {1,2}→3（履约加分在 Service） */
    @PutMapping("/leave/{id}")
    public R<Void> leave(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(reservationService.leave(id));
    }

    /** 取消预约 0→4（每日取消上限在 Service） */
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        assertOwn(id);
        return toAjax(reservationService.cancel(id));
    }

    /** 归属校验：该预约必须属于当前登录读者，否则 403 —— C 端禁止操作他人预约 */
    private void assertOwn(Long id) {
        ReservationVo vo = reservationService.queryById(id);
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

package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.ReservationBo;
import org.dromara.library.domain.vo.ReservationVo;
import org.dromara.library.service.IReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 座位预约Controller（状态机 + 管理干预）
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/reservation")
public class ReservationController extends BaseController {

    private final IReservationService reservationService;

    /**
     * 预约总览
     */
    @SaCheckPermission("library:reservation:list")
    @GetMapping("/list")
    public TableDataInfo<ReservationVo> list(@Validated(QueryGroup.class) ReservationBo bo, PageQuery pageQuery) {
        return reservationService.queryPageList(bo, pageQuery);
    }

    /**
     * 预约详情
     */
    @SaCheckPermission("library:reservation:query")
    @GetMapping("/{id}")
    public R<ReservationVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(reservationService.queryById(id));
    }

    /**
     * 约座（前置校验黑名单/信用 + 座位资源不变式）
     */
    @SaCheckPermission("library:reservation:add")
    @Log(title = "座位预约", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ReservationBo bo) {
        return toAjax(reservationService.createReservation(bo));
    }

    /**
     * 签到 0→1
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-签到", businessType = BusinessType.UPDATE)
    @PutMapping("/checkIn/{id}")
    public R<Void> checkIn(@PathVariable Long id) {
        return toAjax(reservationService.checkIn(id));
    }

    /**
     * 暂离 1→2
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-暂离", businessType = BusinessType.UPDATE)
    @PutMapping("/away/{id}")
    public R<Void> away(@PathVariable Long id) {
        return toAjax(reservationService.away(id));
    }

    /**
     * 返回落座 2→1
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-返回", businessType = BusinessType.UPDATE)
    @PutMapping("/back/{id}")
    public R<Void> back(@PathVariable Long id) {
        return toAjax(reservationService.back(id));
    }

    /**
     * 退座/签退 {1,2}→3（履约加分）
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-退座", businessType = BusinessType.UPDATE)
    @PutMapping("/leave/{id}")
    public R<Void> leave(@PathVariable Long id) {
        return toAjax(reservationService.leave(id));
    }

    /**
     * 取消预约 0→4
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-取消", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        return toAjax(reservationService.cancel(id));
    }

    /**
     * 续座（延长结束时间）
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-续座", businessType = BusinessType.UPDATE)
    @PutMapping("/extend/{id}")
    public R<Void> extend(@PathVariable Long id,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date newEndTime) {
        return toAjax(reservationService.extend(id, newEndTime));
    }

    /**
     * 管理员强制释放 {0,1,2}→3
     */
    @SaCheckPermission("library:reservation:manage")
    @Log(title = "座位预约-强制释放", businessType = BusinessType.UPDATE)
    @PutMapping("/forceRelease/{id}")
    public R<Void> forceRelease(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return toAjax(reservationService.forceRelease(id, reason));
    }

}

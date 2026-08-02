package org.dromara.library.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.library.domain.bo.RoomReservationBo;
import org.dromara.library.domain.vo.RoomReservationVo;
import org.dromara.library.service.IRoomReservationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 研讨间预约Controller（状态机 + 审批）
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/roomReservation")
public class RoomReservationController extends BaseController {

    private final IRoomReservationService roomReservationService;

    /**
     * 查询研讨间预约列表
     */
    @SaCheckPermission("library:roomReservation:list")
    @GetMapping("/list")
    public TableDataInfo<RoomReservationVo> list(@Validated(QueryGroup.class) RoomReservationBo bo, PageQuery pageQuery) {
        return roomReservationService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出研讨间预约列表
     */
    @SaCheckPermission("library:roomReservation:export")
    @Log(title = "研讨间预约", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RoomReservationBo bo, HttpServletResponse response) {
        List<RoomReservationVo> list = roomReservationService.queryList(bo);
        ExcelUtil.exportExcel(list, "研讨间预约", RoomReservationVo.class, response);
    }

    /**
     * 获取研讨间预约详细信息
     */
    @SaCheckPermission("library:roomReservation:query")
    @GetMapping("/{id}")
    public R<RoomReservationVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(roomReservationService.queryById(id));
    }

    /**
     * 新增研讨间预约（悲观行锁 + 时段重叠校验 + 按需审批初始状态）
     */
    @SaCheckPermission("library:roomReservation:add")
    @Log(title = "研讨间预约", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RoomReservationBo bo) {
        return toAjax(roomReservationService.createReservation(bo));
    }

    /**
     * 修改研讨间预约
     */
    @SaCheckPermission("library:roomReservation:edit")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RoomReservationBo bo) {
        return toAjax(roomReservationService.updateByBo(bo));
    }

    /**
     * 删除研讨间预约
     */
    @SaCheckPermission("library:roomReservation:remove")
    @Log(title = "研讨间预约", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomReservationService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

    /**
     * 审批通过 0→1
     */
    @SaCheckPermission("library:roomReservation:approve")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{id}")
    public R<Void> approve(@PathVariable Long id) {
        return toAjax(roomReservationService.approve(id));
    }

    /**
     * 审批驳回 0→5
     */
    @SaCheckPermission("library:roomReservation:approve")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{id}")
    public R<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        return toAjax(roomReservationService.reject(id, reason));
    }

    /**
     * 签到 1→2
     */
    @SaCheckPermission("library:roomReservation:edit")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @PostMapping("/checkIn/{id}")
    public R<Void> checkIn(@PathVariable Long id) {
        return toAjax(roomReservationService.checkIn(id));
    }

    /**
     * 完成 2→3
     */
    @SaCheckPermission("library:roomReservation:edit")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @PostMapping("/complete/{id}")
    public R<Void> complete(@PathVariable Long id) {
        return toAjax(roomReservationService.complete(id));
    }

    /**
     * 取消 {0,1}→4
     */
    @SaCheckPermission("library:roomReservation:edit")
    @Log(title = "研讨间预约", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        return toAjax(roomReservationService.cancel(id));
    }

}

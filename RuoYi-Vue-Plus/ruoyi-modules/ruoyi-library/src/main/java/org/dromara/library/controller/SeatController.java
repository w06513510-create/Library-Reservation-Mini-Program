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
import org.dromara.library.domain.bo.SeatBo;
import org.dromara.library.domain.vo.SeatVo;
import org.dromara.library.service.ISeatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 座位Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/seat")
public class SeatController extends BaseController {

    private final ISeatService seatService;

    /**
     * 查询座位列表
     */
    @SaCheckPermission("library:seat:list")
    @GetMapping("/list")
    public TableDataInfo<SeatVo> list(@Validated(QueryGroup.class) SeatBo bo, PageQuery pageQuery) {
        return seatService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出座位列表
     */
    @SaCheckPermission("library:seat:export")
    @Log(title = "座位", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SeatBo bo, HttpServletResponse response) {
        List<SeatVo> list = seatService.queryList(bo);
        ExcelUtil.exportExcel(list, "座位", SeatVo.class, response);
    }

    /**
     * 获取座位详细信息
     */
    @SaCheckPermission("library:seat:query")
    @GetMapping("/{id}")
    public R<SeatVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(seatService.queryById(id));
    }

    /**
     * 新增座位
     */
    @SaCheckPermission("library:seat:add")
    @Log(title = "座位", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SeatBo bo) {
        return toAjax(seatService.insertByBo(bo));
    }

    /**
     * 修改座位
     */
    @SaCheckPermission("library:seat:edit")
    @Log(title = "座位", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SeatBo bo) {
        return toAjax(seatService.updateByBo(bo));
    }

    /**
     * 删除座位
     */
    @SaCheckPermission("library:seat:remove")
    @Log(title = "座位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(seatService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

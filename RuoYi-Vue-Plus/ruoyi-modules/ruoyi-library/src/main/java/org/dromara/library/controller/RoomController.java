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
import org.dromara.library.domain.bo.RoomBo;
import org.dromara.library.domain.vo.RoomVo;
import org.dromara.library.service.IRoomService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * 研讨间Controller
 *
 * @author library
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/library/room")
public class RoomController extends BaseController {

    private final IRoomService roomService;

    /**
     * 查询研讨间列表
     */
    @SaCheckPermission("library:room:list")
    @GetMapping("/list")
    public TableDataInfo<RoomVo> list(@Validated(QueryGroup.class) RoomBo bo, PageQuery pageQuery) {
        return roomService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出研讨间列表
     */
    @SaCheckPermission("library:room:export")
    @Log(title = "研讨间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(RoomBo bo, HttpServletResponse response) {
        List<RoomVo> list = roomService.queryList(bo);
        ExcelUtil.exportExcel(list, "研讨间", RoomVo.class, response);
    }

    /**
     * 获取研讨间详细信息
     */
    @SaCheckPermission("library:room:query")
    @GetMapping("/{id}")
    public R<RoomVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(roomService.queryById(id));
    }

    /**
     * 新增研讨间
     */
    @SaCheckPermission("library:room:add")
    @Log(title = "研讨间", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RoomBo bo) {
        return toAjax(roomService.insertByBo(bo));
    }

    /**
     * 修改研讨间
     */
    @SaCheckPermission("library:room:edit")
    @Log(title = "研讨间", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody RoomBo bo) {
        return toAjax(roomService.updateByBo(bo));
    }

    /**
     * 删除研讨间
     */
    @SaCheckPermission("library:room:remove")
    @Log(title = "研讨间", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(roomService.deleteWithValidByIds(Arrays.asList(ids), true));
    }

}

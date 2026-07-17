package org.dromara.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.biz.domain.bo.RatingBo;
import org.dromara.biz.domain.vo.RatingVo;
import org.dromara.biz.service.IRatingService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 通用评价 Controller
 *
 * @author ruoyi-template
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/biz/rating")
public class RatingController extends BaseController {

    private final IRatingService ratingService;

    /** 提交评价（前端只传 bizType/bizId/score/content；方向服务端解析） */
    @SaCheckPermission("biz:rating:add")
    @Log(title = "评价", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody RatingBo bo) {
        ratingService.evaluate(bo);
        return R.ok();
    }

    /** 查某业务某方向的评价（前端判"是否已评"） */
    @SaCheckPermission("biz:rating:query")
    @GetMapping("/one/{bizType}/{bizId}/{evalRole}")
    public R<RatingVo> getByBizAndRole(@PathVariable String bizType,
                                       @PathVariable Long bizId,
                                       @PathVariable Integer evalRole) {
        return R.ok(ratingService.getByBizAndRole(bizType, bizId, evalRole));
    }

    /** 被评价人平均分 */
    @SaCheckPermission("biz:rating:query")
    @GetMapping("/avg/{toUserId}")
    public R<Double> avgScore(@NotNull(message = "被评价人不能为空") @PathVariable Long toUserId) {
        return R.ok(ratingService.avgScore(toUserId));
    }

    /** 评价列表（后台） */
    @SaCheckPermission("biz:rating:list")
    @GetMapping("/list")
    public TableDataInfo<RatingVo> list(@Validated(QueryGroup.class) RatingBo bo, PageQuery pageQuery) {
        return ratingService.queryPageList(bo, pageQuery);
    }

}

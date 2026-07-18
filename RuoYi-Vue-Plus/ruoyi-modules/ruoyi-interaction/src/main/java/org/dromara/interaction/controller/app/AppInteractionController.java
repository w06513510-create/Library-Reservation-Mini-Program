package org.dromara.interaction.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.app.domain.vo.AppUserVo;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.interaction.constant.InteractionAction;
import org.dromara.interaction.domain.bo.InteractionToggleBo;
import org.dromara.interaction.service.IInteractionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * C端(小程序) 通用互动 Controller —— 收藏/点赞/关注。
 * <p>{@code @SaCheckLogin} + {@link AppLoginHelper#getUserId()} 强制归属，只能操作/查询自己的互动。
 *
 * @author ruoyi-template
 */
@Validated
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/interaction")
public class AppInteractionController {

    private final IInteractionService interactionService;

    /** 开关互动，返回最新状态(active)与该对象计数(count) */
    @PostMapping("/toggle")
    public R<Map<String, Object>> toggle(@Valid @RequestBody InteractionToggleBo bo) {
        InteractionAction.validate(bo.getAction());
        Long uid = AppLoginHelper.getUserId();
        boolean active = interactionService.toggle(uid, bo.getAction(), bo.getBizType(), bo.getBizId());
        long count = interactionService.count(bo.getAction(), bo.getBizType(), bo.getBizId());
        Map<String, Object> data = new HashMap<>(2);
        data.put("active", active);
        data.put("count", count);
        return R.ok(data);
    }

    /** 我是否对该对象做过该动作 */
    @GetMapping("/has")
    public R<Boolean> has(@RequestParam String action, @RequestParam String bizType, @RequestParam Long bizId) {
        return R.ok(interactionService.has(AppLoginHelper.getUserId(), action, bizType, bizId));
    }

    /** 该对象被多少人做过该动作 */
    @GetMapping("/count")
    public R<Long> count(@RequestParam String action, @RequestParam String bizType, @RequestParam Long bizId) {
        return R.ok(interactionService.count(action, bizType, bizId));
    }

    /** 我的收藏/点赞/关注 对象ID 分页（业务据此查详情） */
    @GetMapping("/my/page")
    public TableDataInfo<Long> myPage(@RequestParam String action, @RequestParam String bizType, PageQuery pageQuery) {
        return interactionService.pageMyBizIds(AppLoginHelper.getUserId(), action, bizType, pageQuery);
    }

    /** 我关注的人 */
    @GetMapping("/following/page")
    public TableDataInfo<AppUserVo> following(PageQuery pageQuery) {
        return interactionService.pageFollowing(AppLoginHelper.getUserId(), pageQuery);
    }

    /** 关注我的人(粉丝) */
    @GetMapping("/followers/page")
    public TableDataInfo<AppUserVo> followers(PageQuery pageQuery) {
        return interactionService.pageFollowers(AppLoginHelper.getUserId(), pageQuery);
    }

}

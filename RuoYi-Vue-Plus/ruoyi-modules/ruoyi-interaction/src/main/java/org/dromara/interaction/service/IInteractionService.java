package org.dromara.interaction.service;

import org.dromara.app.domain.vo.AppUserVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 通用互动服务：用户对任意业务对象(biz_type/biz_id)的 收藏/点赞/关注。
 * <p>业务模块注入本接口即可为自己的列表拼"我操作过没 + 计数"，无需各自建表。
 *
 * @author ruoyi-template
 */
public interface IInteractionService {

    /**
     * 开关互动（幂等）：无则加返回 true，有则取消返回 false。
     */
    boolean toggle(Long userId, String action, String bizType, Long bizId);

    /**
     * 我是否对该对象做过该动作。
     */
    boolean has(Long userId, String action, String bizType, Long bizId);

    /**
     * 该对象被多少人做过该动作（如点赞数 / 收藏数 / 粉丝数）。
     */
    long count(String action, String bizType, Long bizId);

    /**
     * 一批对象里，我操作过哪些（列表渲染用）。
     */
    Set<Long> hasBatch(Long userId, String action, String bizType, Collection<Long> bizIds);

    /**
     * 一批对象各自的计数（列表渲染用）。
     */
    Map<Long, Long> countBatch(String action, String bizType, Collection<Long> bizIds);

    /**
     * 我的收藏/点赞/关注 对象ID 分页（业务据此查详情）。
     */
    TableDataInfo<Long> pageMyBizIds(Long userId, String action, String bizType, PageQuery pageQuery);

    /**
     * 我关注的人（biz_type=user）分页，返回用户视图。
     */
    TableDataInfo<AppUserVo> pageFollowing(Long userId, PageQuery pageQuery);

    /**
     * 关注我的人（粉丝）分页，返回用户视图。
     */
    TableDataInfo<AppUserVo> pageFollowers(Long userId, PageQuery pageQuery);

}

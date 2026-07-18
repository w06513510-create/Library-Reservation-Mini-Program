package org.dromara.interaction.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.app.domain.vo.AppUserVo;
import org.dromara.app.mapper.AppUserMapper;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.interaction.constant.InteractionAction;
import org.dromara.interaction.domain.AppInteraction;
import org.dromara.interaction.mapper.AppInteractionMapper;
import org.dromara.interaction.service.IInteractionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通用互动服务实现。取消互动为物理删除；计数从表实时聚合，不改业务表。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class InteractionServiceImpl implements IInteractionService {

    private final AppInteractionMapper baseMapper;
    private final AppUserMapper appUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, String action, String bizType, Long bizId) {
        InteractionAction.validate(action);
        AppInteraction exist = baseMapper.selectOne(Wrappers.<AppInteraction>lambdaQuery()
            .eq(AppInteraction::getUserId, userId)
            .eq(AppInteraction::getAction, action)
            .eq(AppInteraction::getBizType, bizType)
            .eq(AppInteraction::getBizId, bizId));
        if (exist != null) {
            baseMapper.deleteById(exist.getId());
            return false;
        }
        AppInteraction row = new AppInteraction();
        row.setUserId(userId);
        row.setAction(action);
        row.setBizType(bizType);
        row.setBizId(bizId);
        try {
            baseMapper.insert(row);
        } catch (DuplicateKeyException e) {
            // 并发下另一请求已插入，视为已添加
            return true;
        }
        return true;
    }

    @Override
    public boolean has(Long userId, String action, String bizType, Long bizId) {
        return baseMapper.exists(Wrappers.<AppInteraction>lambdaQuery()
            .eq(AppInteraction::getUserId, userId)
            .eq(AppInteraction::getAction, action)
            .eq(AppInteraction::getBizType, bizType)
            .eq(AppInteraction::getBizId, bizId));
    }

    @Override
    public long count(String action, String bizType, Long bizId) {
        return baseMapper.selectCount(Wrappers.<AppInteraction>lambdaQuery()
            .eq(AppInteraction::getAction, action)
            .eq(AppInteraction::getBizType, bizType)
            .eq(AppInteraction::getBizId, bizId));
    }

    @Override
    public Set<Long> hasBatch(Long userId, String action, String bizType, Collection<Long> bizIds) {
        if (bizIds == null || bizIds.isEmpty()) {
            return Collections.emptySet();
        }
        return baseMapper.selectList(Wrappers.<AppInteraction>lambdaQuery()
                .select(AppInteraction::getBizId)
                .eq(AppInteraction::getUserId, userId)
                .eq(AppInteraction::getAction, action)
                .eq(AppInteraction::getBizType, bizType)
                .in(AppInteraction::getBizId, bizIds))
            .stream().map(AppInteraction::getBizId).collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Long> countBatch(String action, String bizType, Collection<Long> bizIds) {
        Map<Long, Long> result = new HashMap<>();
        if (bizIds == null || bizIds.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> rows = baseMapper.selectMaps(Wrappers.<AppInteraction>query()
            .select("biz_id AS bizId", "count(*) AS cnt")
            .eq("action", action)
            .eq("biz_type", bizType)
            .in("biz_id", bizIds)
            .groupBy("biz_id"));
        for (Map<String, Object> m : rows) {
            Object k = m.get("bizId");
            Object v = m.get("cnt");
            if (k != null) {
                result.put(((Number) k).longValue(), v == null ? 0L : ((Number) v).longValue());
            }
        }
        return result;
    }

    @Override
    public TableDataInfo<Long> pageMyBizIds(Long userId, String action, String bizType, PageQuery pageQuery) {
        Page<AppInteraction> page = baseMapper.selectPage(pageQuery.build(),
            Wrappers.<AppInteraction>lambdaQuery()
                .eq(AppInteraction::getUserId, userId)
                .eq(AppInteraction::getAction, action)
                .eq(AppInteraction::getBizType, bizType)
                .orderByDesc(AppInteraction::getId));
        List<Long> bizIds = page.getRecords().stream().map(AppInteraction::getBizId).collect(Collectors.toList());
        TableDataInfo<Long> rsp = new TableDataInfo<>();
        rsp.setCode(200);
        rsp.setMsg("查询成功");
        rsp.setRows(bizIds);
        rsp.setTotal(page.getTotal());
        return rsp;
    }

    @Override
    public TableDataInfo<AppUserVo> pageFollowing(Long userId, PageQuery pageQuery) {
        Page<AppInteraction> page = baseMapper.selectPage(pageQuery.build(),
            Wrappers.<AppInteraction>lambdaQuery()
                .eq(AppInteraction::getUserId, userId)
                .eq(AppInteraction::getAction, InteractionAction.FOLLOW)
                .eq(AppInteraction::getBizType, InteractionAction.BIZ_TYPE_USER)
                .orderByDesc(AppInteraction::getId));
        List<Long> ids = page.getRecords().stream().map(AppInteraction::getBizId).collect(Collectors.toList());
        return usersPage(ids, page.getTotal());
    }

    @Override
    public TableDataInfo<AppUserVo> pageFollowers(Long userId, PageQuery pageQuery) {
        Page<AppInteraction> page = baseMapper.selectPage(pageQuery.build(),
            Wrappers.<AppInteraction>lambdaQuery()
                .eq(AppInteraction::getAction, InteractionAction.FOLLOW)
                .eq(AppInteraction::getBizType, InteractionAction.BIZ_TYPE_USER)
                .eq(AppInteraction::getBizId, userId)
                .orderByDesc(AppInteraction::getId));
        List<Long> ids = page.getRecords().stream().map(AppInteraction::getUserId).collect(Collectors.toList());
        return usersPage(ids, page.getTotal());
    }

    /** 按 id 列表取 AppUserVo 并保持原顺序 */
    private TableDataInfo<AppUserVo> usersPage(List<Long> ids, long total) {
        TableDataInfo<AppUserVo> rsp = new TableDataInfo<>();
        rsp.setCode(200);
        rsp.setMsg("查询成功");
        rsp.setTotal(total);
        if (ids.isEmpty()) {
            rsp.setRows(Collections.emptyList());
            return rsp;
        }
        List<AppUserVo> vos = appUserMapper.selectVoByIds(ids);
        Map<Long, AppUserVo> byId = vos.stream().collect(Collectors.toMap(AppUserVo::getId, v -> v, (a, b) -> a));
        List<AppUserVo> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
        rsp.setRows(ordered);
        return rsp;
    }

}

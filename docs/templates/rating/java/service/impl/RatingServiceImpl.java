package org.dromara.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.biz.domain.Rating;
import org.dromara.biz.domain.bo.RatingBo;
import org.dromara.biz.domain.vo.RatingVo;
import org.dromara.biz.mapper.RatingMapper;
import org.dromara.biz.service.IRatingService;
import org.dromara.biz.service.RatingPartyResolver;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用评价 Service 实现
 * <p>提炼自跑腿 ErrandEvaluationServiceImpl.evaluate：登录取评价人 → 解析方向与双方 →
 * 两层查重(先查友好提示 + uk 兜底) → 落库。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class RatingServiceImpl implements IRatingService {

    private final RatingMapper baseMapper;
    /** 各 bizType 的方向解析器；无匹配则不支持该业务评价 */
    private final List<RatingPartyResolver> resolvers;

    private RatingPartyResolver resolverOf(String bizType) {
        RatingPartyResolver r = resolvers.stream()
            .collect(Collectors.toMap(RatingPartyResolver::bizType, Function.identity(), (a, b) -> a))
            .get(bizType);
        if (r == null) {
            throw new ServiceException("不支持的评价业务类型: " + bizType);
        }
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(RatingBo bo) {
        if (StringUtils.isBlank(bo.getBizType())) {
            throw new ServiceException("业务类型不能为空");
        }
        if (bo.getScore() == null || bo.getScore() < 1 || bo.getScore() > 5) {
            throw new ServiceException("评分须为 1~5 星");
        }
        Long currentUserId = LoginHelper.getUserId();
        // 服务端解析：校验可评价 + 定方向与双方（业务耦合部分委托 resolver）
        RatingPartyResolver.Party party = resolverOf(bo.getBizType()).resolve(bo.getBizId(), currentUserId);

        // 查重(一单一评不可改)：先查一次给友好提示，DB uk_biz_role 做并发兜底
        Long exists = baseMapper.selectCount(Wrappers.<Rating>lambdaQuery()
            .eq(Rating::getBizType, bo.getBizType())
            .eq(Rating::getBizId, bo.getBizId())
            .eq(Rating::getEvalRole, party.getEvalRole()));
        if (exists != null && exists > 0) {
            throw new ServiceException("您已评价过该业务");
        }

        Rating rating = new Rating();
        rating.setBizType(bo.getBizType());
        rating.setBizId(bo.getBizId());
        rating.setBizNo(party.getBizNo());
        rating.setEvalRole(party.getEvalRole());
        rating.setFromUserId(currentUserId);   // 评价人取登录态，不信前端
        rating.setToUserId(party.getToUserId());
        rating.setScore(bo.getScore());
        rating.setContent(bo.getContent());
        rating.setIsDefault(0);
        baseMapper.insert(rating);
    }

    @Override
    public RatingVo getByBizAndRole(String bizType, Long bizId, Integer evalRole) {
        return baseMapper.selectVoOne(Wrappers.<Rating>lambdaQuery()
            .eq(Rating::getBizType, bizType)
            .eq(Rating::getBizId, bizId)
            .eq(Rating::getEvalRole, evalRole), false);
    }

    @Override
    public Double avgScore(Long toUserId) {
        List<Rating> list = baseMapper.selectList(Wrappers.<Rating>lambdaQuery()
            .select(Rating::getScore)
            .eq(Rating::getToUserId, toUserId));
        double avg = list.stream().filter(r -> r.getScore() != null)
            .mapToInt(Rating::getScore).average().orElse(0);
        return Math.round(avg * 100.0) / 100.0;
    }

    @Override
    public TableDataInfo<RatingVo> queryPageList(RatingBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Rating> lqw = Wrappers.<Rating>lambdaQuery()
            .eq(StringUtils.isNotBlank(bo.getBizType()), Rating::getBizType, bo.getBizType())
            .eq(bo.getBizId() != null, Rating::getBizId, bo.getBizId())
            .eq(bo.getEvalRole() != null, Rating::getEvalRole, bo.getEvalRole())
            .eq(bo.getScore() != null, Rating::getScore, bo.getScore())
            .orderByDesc(Rating::getId);
        Page<RatingVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

}

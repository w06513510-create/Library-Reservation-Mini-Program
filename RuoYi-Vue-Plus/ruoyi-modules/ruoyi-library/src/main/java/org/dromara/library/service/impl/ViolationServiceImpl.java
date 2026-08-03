package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Reader;
import org.dromara.library.domain.Violation;
import org.dromara.library.domain.bo.ViolationBo;
import org.dromara.library.domain.vo.ViolationVo;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.mapper.ViolationMapper;
import org.dromara.library.service.IBlacklistService;
import org.dromara.library.service.ICreditService;
import org.dromara.library.service.IViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 违约记录Service实现（记违约 → 扣分 → 黑名单判定 一站式）
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class ViolationServiceImpl implements IViolationService {

    private final ViolationMapper baseMapper;
    private final ReaderMapper readerMapper;
    private final ICreditService creditService;
    private final IBlacklistService blacklistService;
    private final RuleConfigHelper ruleConfig;

    @Override
    public ViolationVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ViolationVo> queryPageList(ViolationBo bo, PageQuery pageQuery) {
        Page<ViolationVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<ViolationVo> queryList(ViolationBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Violation> buildQueryWrapper(ViolationBo bo) {
        LambdaQueryWrapper<Violation> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Violation::getReaderId, bo.getReaderId());
        lqw.eq(bo.getViolationType() != null, Violation::getViolationType, bo.getViolationType());
        lqw.eq(bo.getStatus() != null, Violation::getStatus, bo.getStatus());
        lqw.orderByDesc(Violation::getOccurTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordViolation(Long readerId, int type, Integer deductScore, String bizType, Long bizId, int source) {
        int deduct = deductScore != null ? deductScore : defaultDeduct(type);
        Violation v = new Violation();
        v.setReaderId(readerId);
        v.setViolationType(type);
        v.setBizType(bizType);
        v.setBizId(bizId);
        v.setDeductScore(deduct);
        v.setOccurTime(new Date());
        v.setSource(source);
        v.setStatus(0);
        baseMapper.insert(v);
        // 信用扣分（足额）
        creditService.changeCredit(readerId, -deduct, creditReason(type), "违约扣分·类型" + type, bizType, bizId);
        // 黑名单阈值判定：两条线互不覆盖——
        //   ① 信用线：信用分低于 pause_score 暂停
        //   ② 违约线：近 ban_window_days 天内有效违约达 ban_violation_count 次（滑动窗口，对齐中传等真实馆规）
        int pauseScore = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "pause_score", 20);
        int banWindowDays = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "ban_window_days", 7);
        int banVioCount = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "ban_violation_count", 3);
        int banDays = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "ban_days", 7);
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, readerId));
        // 用字符串与 datetime 列比较，避免 Date 经 JDBC 时区偏移（与 seatStatus 一致）
        String windowStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date(System.currentTimeMillis() - (long) banWindowDays * 24 * 3600 * 1000));
        Long recentVio = baseMapper.selectCount(Wrappers.<Violation>lambdaQuery()
            .eq(Violation::getReaderId, readerId).eq(Violation::getStatus, 0)
            .ge(Violation::getOccurTime, windowStart));
        boolean lowScore = reader != null && reader.getCreditScore() != null && reader.getCreditScore() < pauseScore;
        boolean tooMany = recentVio != null && recentVio >= banVioCount;
        if (lowScore || tooMany) {
            String reason = lowScore
                ? "信用分低于" + pauseScore
                : "近" + banWindowDays + "天内有效违约累计达" + banVioCount + "次";
            blacklistService.addToBlacklist(readerId, reason, banDays);
        }
        return v.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addByBo(ViolationBo bo) {
        recordViolation(bo.getReaderId(), bo.getViolationType(), bo.getDeductScore(), "manual", null, 1);
        return true;
    }

    /** 各类型默认扣分：优先读 biz_rule_config 的 deduct_type{N}，缺失时用内置默认值兜底 */
    private int defaultDeduct(int type) {
        int fallback = switch (type) {
            case 1 -> 10; // 座位爽约
            case 2 -> 5;  // 暂离超时
            case 3 -> 10; // 监督未落座
            case 4 -> 5;  // 未签退
            case 5 -> 5;  // 图书逾期
            case 6 -> 5;  // 预约架超期
            case 7 -> 20; // 遗失损坏
            default -> 5;
        };
        return ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "deduct_type" + type, fallback);
    }

    /** 违约类型 → 信用流水事由类型 */
    private int creditReason(int type) {
        return switch (type) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            case 5 -> 6;
            case 6 -> 7;
            case 7 -> 8;
            default -> 2;
        };
    }

}

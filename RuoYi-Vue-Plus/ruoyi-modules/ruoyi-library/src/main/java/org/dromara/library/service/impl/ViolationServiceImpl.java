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
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.mapper.ViolationMapper;
import org.dromara.library.service.IBlacklistService;
import org.dromara.library.service.ICreditService;
import org.dromara.library.service.IViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 黑名单阈值：信用分低于此 或 有效违约累计达此次数 */
    private static final int BLACKLIST_SCORE = 20;
    private static final int BLACKLIST_VIO_COUNT = 3;
    private static final int BLACKLIST_DAYS = 7;

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
        // 黑名单阈值判定
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, readerId));
        Long activeVio = baseMapper.selectCount(Wrappers.<Violation>lambdaQuery()
            .eq(Violation::getReaderId, readerId).eq(Violation::getStatus, 0));
        boolean lowScore = reader != null && reader.getCreditScore() != null && reader.getCreditScore() < BLACKLIST_SCORE;
        boolean tooMany = activeVio != null && activeVio >= BLACKLIST_VIO_COUNT;
        if (lowScore || tooMany) {
            blacklistService.addToBlacklist(readerId, lowScore ? "信用分低于" + BLACKLIST_SCORE : "有效违约累计达" + BLACKLIST_VIO_COUNT + "次", BLACKLIST_DAYS);
        }
        return v.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addByBo(ViolationBo bo) {
        recordViolation(bo.getReaderId(), bo.getViolationType(), bo.getDeductScore(), "manual", null, 1);
        return true;
    }

    /** 各类型默认扣分 */
    private int defaultDeduct(int type) {
        return switch (type) {
            case 1 -> 10; // 座位爽约
            case 2 -> 5;  // 暂离超时
            case 3 -> 10; // 监督未落座
            case 4 -> 5;  // 未签退
            case 5 -> 5;  // 图书逾期
            case 6 -> 5;  // 预约架超期
            case 7 -> 20; // 遗失损坏
            default -> 5;
        };
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

package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.library.domain.CreditLog;
import org.dromara.library.domain.Reader;
import org.dromara.library.helper.RuleConfigHelper;
import org.dromara.library.mapper.CreditLogMapper;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.service.ICreditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 信用记账Service实现
 * 不变式：读者当前分 = clamp(Σ biz_credit_log.delta, 0, 100)；扣分足额落账、校准/冲正按未clamp的 raw_sum 计算。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class CreditServiceImpl implements ICreditService {

    private final CreditLogMapper creditLogMapper;
    private final ReaderMapper readerMapper;
    private final RuleConfigHelper ruleConfig;

    @Override
    public Reader ensureReader(Long readerId) {
        Reader reader = readerMapper.selectOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, readerId));
        if (reader == null) {
            reader = new Reader();
            reader.setUserId(readerId);
            reader.setStudentNo("U" + readerId);
            reader.setCreditScore(ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "init_score", 100));
            reader.setPerformCount(0);
            reader.setBlacklistFlag(0);
            reader.setStatus(0);
            readerMapper.insert(reader);
        }
        return reader;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeCredit(Long readerId, int delta, Integer reasonType, String reasonDesc, String bizType, Long bizId) {
        Reader reader = ensureReader(readerId);
        seedIfNeeded(reader);
        return applyDelta(readerId, reader, delta, reasonType, reasonDesc, bizType, bizId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int calibrateCredit(Long readerId, int targetScore, String reasonDesc, String bizType, Long bizId) {
        Reader reader = ensureReader(readerId);
        seedIfNeeded(reader);
        int rawBefore = sumDelta(readerId);
        int delta = targetScore - rawBefore;
        return applyDelta(readerId, reader, delta, 12, reasonDesc, bizType, bizId);
    }

    /**
     * 首次变动时把账本种子对齐到读者当前分，保证 Σdelta 与 credit_score 一致
     */
    private void seedIfNeeded(Reader reader) {
        Long count = creditLogMapper.selectCount(Wrappers.<CreditLog>lambdaQuery().eq(CreditLog::getReaderId, reader.getUserId()));
        if (count == null || count == 0L) {
            int initScore = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "init_score", 100);
            int seed = reader.getCreditScore() == null ? initScore : reader.getCreditScore();
            writeLog(reader.getUserId(), seed, 1, "建档", "reader", reader.getId(), clamp(seed));
        }
    }

    private int applyDelta(Long readerId, Reader reader, int delta, Integer reasonType, String reasonDesc, String bizType, Long bizId) {
        int rawBefore = sumDelta(readerId);
        int scoreAfter = clamp(rawBefore + delta);
        writeLog(readerId, delta, reasonType, reasonDesc, bizType, bizId, scoreAfter);
        reader.setCreditScore(scoreAfter);
        readerMapper.updateById(reader);
        return scoreAfter;
    }

    private int sumDelta(Long readerId) {
        List<CreditLog> logs = creditLogMapper.selectList(
            Wrappers.<CreditLog>lambdaQuery().eq(CreditLog::getReaderId, readerId).select(CreditLog::getDelta));
        return logs.stream().mapToInt(l -> l.getDelta() == null ? 0 : l.getDelta()).sum();
    }

    private void writeLog(Long readerId, int delta, Integer reasonType, String reasonDesc, String bizType, Long bizId, int scoreAfter) {
        CreditLog log = new CreditLog();
        log.setReaderId(readerId);
        log.setDelta(delta);
        log.setReasonType(reasonType);
        log.setReasonDesc(reasonDesc);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setScoreAfter(scoreAfter);
        creditLogMapper.insert(log);
    }

    private int clamp(int v) {
        int min = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "score_min", 0);
        int max = ruleConfig.getInt(RuleConfigHelper.GROUP_CREDIT, "score_max", 100);
        return Math.max(min, Math.min(max, v));
    }

}

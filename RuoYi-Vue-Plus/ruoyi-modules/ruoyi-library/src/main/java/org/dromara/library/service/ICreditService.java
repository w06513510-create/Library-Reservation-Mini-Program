package org.dromara.library.service;

import org.dromara.library.domain.Reader;

/**
 * 信用记账Service（信用流水账，一致性不变式 credit_score = clamp(Σ delta, 0, 100)）
 *
 * @author library
 */
public interface ICreditService {

    /**
     * 确保读者档案存在（不存在则按默认分建档），返回档案
     */
    Reader ensureReader(Long readerId);

    /**
     * 信用变动：写一条流水（delta 带符号），更新读者当前分并返回变动后分。
     * reasonType：1建档 2座位爽约 3暂离超时 4监督未落座 5未签退 6图书逾期 7预约架超期 8遗失损坏 9履约加分 10时间衰减 11申诉冲正 12黑名单校准
     */
    int changeCredit(Long readerId, int delta, Integer reasonType, String reasonDesc, String bizType, Long bizId);

    /**
     * 信用校准（赋值型）：把当前分校准到 targetScore（delta = target − 当前 raw_sum），用于黑名单解除/重置。
     */
    int calibrateCredit(Long readerId, int targetScore, String reasonDesc, String bizType, Long bizId);

}

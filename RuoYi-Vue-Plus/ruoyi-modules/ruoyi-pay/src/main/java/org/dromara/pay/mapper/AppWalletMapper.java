package org.dromara.pay.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.pay.domain.AppWallet;
import org.dromara.pay.domain.vo.AppWalletVo;

import java.math.BigDecimal;

/**
 * 钱包 Mapper。
 * <p>动钱一律走本 Mapper 的条件 UPDATE(CAS)：出账带 {@code balance>=#{amount}} / {@code frozen>=#{amount}} 条件，
 * 影响行数=0 即余额不足或并发冲突。每次更新 {@code version+1} 保留乐观锁语义。
 * <p>注意：调用方须在 {@code TenantHelper.ignore} 内执行，避免租户插件给自定义 SQL 追加 tenant_id 条件。
 *
 * @author ruoyi-template
 */
public interface AppWalletMapper extends BaseMapperPlus<AppWallet, AppWalletVo> {

    /**
     * 充值入账：可用余额 += amount，累计充值 += amount。
     */
    @Update("update app_wallet set balance = balance + #{amount}, total_recharge = total_recharge + #{amount}, "
        + "version = version + 1 where user_id = #{userId}")
    int increaseForRecharge(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 扣款(出账)：可用余额 -= amount，条件 balance>=amount 防超扣。
     */
    @Update("update app_wallet set balance = balance - #{amount}, version = version + 1 "
        + "where user_id = #{userId} and balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 冻结：可用 -= amount 且 冻结 += amount，条件 balance>=amount 防超扣。
     */
    @Update("update app_wallet set balance = balance - #{amount}, frozen = frozen + #{amount}, version = version + 1 "
        + "where user_id = #{userId} and balance >= #{amount}")
    int freezeBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 解冻：冻结 -= amount 且 可用 += amount，条件 frozen>=amount 防超扣。
     */
    @Update("update app_wallet set frozen = frozen - #{amount}, balance = balance + #{amount}, version = version + 1 "
        + "where user_id = #{userId} and frozen >= #{amount}")
    int unfreezeBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

}

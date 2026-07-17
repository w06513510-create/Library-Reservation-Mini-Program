package org.dromara.pay.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.pay.domain.AppRecharge;
import org.dromara.pay.domain.vo.AppRechargeVo;

import java.util.Date;

/**
 * 充值单 Mapper。
 * <p>{@link #markPaid} 用条件 UPDATE(CAS) 抢占结算权：{@code where out_trade_no=? and status=0}，
 * 影响行数=1 者才可入账，天然防并发重复到账(notify + 查单轮询)。
 * <p>注意：调用方须在 {@code TenantHelper.ignore} 内执行(notify 路径无登录=无租户上下文)。
 *
 * @author ruoyi-template
 */
public interface AppRechargeMapper extends BaseMapperPlus<AppRecharge, AppRechargeVo> {

    /**
     * 抢占结算：仅当前 status=0 时置为已到账并回填交易信息。
     *
     * @return 影响行数(1=抢占成功可入账, 0=已被结算/关闭)
     */
    @Update("update app_recharge set status = 1, channel = #{channel}, trade_no = #{tradeNo}, "
        + "pay_time = #{payTime}, query_time = #{queryTime} "
        + "where out_trade_no = #{outTradeNo} and status = 0")
    int markPaid(@Param("outTradeNo") String outTradeNo, @Param("channel") String channel,
                 @Param("tradeNo") String tradeNo, @Param("payTime") Date payTime,
                 @Param("queryTime") Date queryTime);

}

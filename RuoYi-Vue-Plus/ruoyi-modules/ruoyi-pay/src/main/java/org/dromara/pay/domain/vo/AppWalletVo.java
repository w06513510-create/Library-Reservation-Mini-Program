package org.dromara.pay.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.pay.domain.AppWallet;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包视图对象 app_wallet
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = AppWallet.class)
public class AppWalletVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 钱包ID */
    private Long id;

    /** 所属 C端用户ID */
    private Long userId;

    /** 可用余额 */
    private BigDecimal balance;

    /** 冻结余额 */
    private BigDecimal frozen;

    /** 累计充值 */
    private BigDecimal totalRecharge;

}

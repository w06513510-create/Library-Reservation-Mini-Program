package org.dromara.pay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * C端用户钱包 app_wallet（一个 app_user 一行，三层余额）。
 * <p>{@code balance} 可用余额 / {@code frozen} 冻结余额 / {@code total_recharge} 累计充值(统计位, 不参与对平)。
 * 动钱唯一入口为 {@code IWalletService}，禁止业务代码直接改本表余额列。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_wallet")
public class AppWallet extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 钱包ID */
    @TableId(value = "id")
    private Long id;

    /** 所属 C端用户ID(app_user.id) */
    private Long userId;

    /** 可用余额 */
    private BigDecimal balance;

    /** 冻结余额 */
    private BigDecimal frozen;

    /** 累计充值(单调递增统计位, 不参与对平不变式) */
    private BigDecimal totalRecharge;

    /** 乐观锁版本 */
    @Version
    private Integer version;

}

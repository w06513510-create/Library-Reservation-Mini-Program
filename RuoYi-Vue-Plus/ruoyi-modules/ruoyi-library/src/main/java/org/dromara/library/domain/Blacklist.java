package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 黑名单对象 biz_blacklist
 * 状态：0生效中 1已解除
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_blacklist")
public class Blacklist extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 读者ID（app_user） */
    private Long readerId;

    /** 拉黑原因 */
    private String reason;

    /** 生效时间 */
    private Date startTime;

    /** 暂停到期时间 */
    private Date endTime;

    /** 状态：0生效中 1已解除 */
    private Integer status;

    /** 解除方式：1到期自动 2申诉通过 3手动 */
    private Integer releaseType;

    /** 解除时间 */
    private Date releaseTime;

    @TableLogic
    private String delFlag;

}

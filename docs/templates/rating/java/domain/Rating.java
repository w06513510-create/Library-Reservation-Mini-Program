package org.dromara.biz.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 通用评价/打分对象 rating
 * <p>提炼自跑腿 errand_evaluation：biz_type+biz_id 定位被评业务，eval_role 定方向，uk 保证一单一评。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rating")
public class Rating extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 业务类型(order/repair/course...) */
    private String bizType;

    /** 被评业务主键 */
    private Long bizId;

    /** 业务单号(冗余,可空) */
    private String bizNo;

    /** 评价方向(1甲评乙 2乙评甲;单向固定1) */
    private Integer evalRole;

    /** 评价人 */
    private Long fromUserId;

    /** 被评价人(可空) */
    private Long toUserId;

    /** 评分(1-5星) */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 是否系统默认好评(0用户评 1系统默认) */
    private Integer isDefault;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

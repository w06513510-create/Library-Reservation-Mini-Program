package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 规则配置对象 biz_rule_config
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_rule_config")
public class RuleConfig extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则配置ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 规则分组：seat座位/book图书/credit信用/task定时任务
     */
    private String ruleGroup;

    /**
     * 规则键
     */
    private String ruleKey;

    /**
     * 规则值
     */
    private String ruleValue;

    /**
     * 说明
     */
    private String remark;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.RuleConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 规则配置业务对象 biz_rule_config
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = RuleConfig.class, reverseConvertGenerate = false)
public class RuleConfigBo extends BaseEntity {

    /**
     * 规则配置ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 规则分组：seat座位/book图书/credit信用/task定时任务
     */
    @NotBlank(message = "规则分组不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ruleGroup;

    /**
     * 规则键
     */
    @NotBlank(message = "规则键不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ruleKey;

    /**
     * 规则值
     */
    @NotBlank(message = "规则值不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ruleValue;

    /**
     * 说明
     */
    private String remark;

}

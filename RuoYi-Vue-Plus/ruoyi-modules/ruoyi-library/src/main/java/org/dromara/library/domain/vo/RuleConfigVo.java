package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.RuleConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 规则配置视图对象 biz_rule_config
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = RuleConfig.class)
public class RuleConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则配置ID
     */
    @ExcelProperty(value = "规则配置ID")
    private Long id;

    /**
     * 规则分组：seat座位/book图书/credit信用/task定时任务
     */
    @ExcelProperty(value = "规则分组")
    private String ruleGroup;

    /**
     * 规则键
     */
    @ExcelProperty(value = "规则键")
    private String ruleKey;

    /**
     * 规则值
     */
    @ExcelProperty(value = "规则值")
    private String ruleValue;

    /**
     * 说明
     */
    @ExcelProperty(value = "说明")
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

}

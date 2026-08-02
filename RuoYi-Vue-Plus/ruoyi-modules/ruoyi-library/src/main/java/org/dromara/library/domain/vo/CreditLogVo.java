package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.CreditLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 信用流水视图对象 biz_credit_log
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CreditLog.class)
public class CreditLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "流水ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "变动分值")
    private Integer delta;

    @ExcelProperty(value = "事由类型")
    private Integer reasonType;

    @ExcelProperty(value = "事由说明")
    private String reasonDesc;

    private String bizType;

    private Long bizId;

    @ExcelProperty(value = "变动后分值")
    private Integer scoreAfter;

    @ExcelProperty(value = "时间")
    private Date createTime;

}

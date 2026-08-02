package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Violation;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 违约记录视图对象 biz_violation
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Violation.class)
public class ViolationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "违约ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "违约类型")
    private Integer violationType;

    private String bizType;

    private Long bizId;

    @ExcelProperty(value = "扣分")
    private Integer deductScore;

    @ExcelProperty(value = "发生时间")
    private Date occurTime;

    @ExcelProperty(value = "来源")
    private Integer source;

    @ExcelProperty(value = "状态")
    private Integer status;

    private Date createTime;

}

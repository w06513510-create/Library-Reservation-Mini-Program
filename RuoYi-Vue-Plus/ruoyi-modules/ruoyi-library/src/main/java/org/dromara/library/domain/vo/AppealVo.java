package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Appeal;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 违约申诉视图对象 biz_appeal
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Appeal.class)
public class AppealVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "申诉ID")
    private Long id;

    @ExcelProperty(value = "违约记录ID")
    private Long violationId;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "申诉理由")
    private String reason;

    @ExcelProperty(value = "状态")
    private Integer status;

    private Long auditBy;

    private Date auditTime;

    private String auditRemark;

    private Date createTime;

}

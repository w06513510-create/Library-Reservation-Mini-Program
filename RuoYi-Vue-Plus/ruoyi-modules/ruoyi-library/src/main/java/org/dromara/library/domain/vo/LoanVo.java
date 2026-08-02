package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Loan;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 借阅单视图对象 biz_loan
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Loan.class)
public class LoanVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "借阅单ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "馆藏册ID")
    private Long itemId;

    @ExcelProperty(value = "书目ID")
    private Long bookId;

    @ExcelProperty(value = "借出时间")
    private Date borrowTime;

    @ExcelProperty(value = "应还日")
    private Date dueTime;

    @ExcelProperty(value = "续借次数")
    private Integer renewCount;

    @ExcelProperty(value = "归还时间")
    private Date returnTime;

    @ExcelProperty(value = "状态")
    private Integer status;

    private Integer overdueFlag;

    private Integer recallFlag;

    private Date recallTime;

    private Date createTime;

}

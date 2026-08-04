package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Hold;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 图书预约(hold)视图对象 biz_hold
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Hold.class)
public class HoldVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "预约ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    /** 读者姓名（学号）——列表展示用，非表字段 */
    @ExcelProperty(value = "读者")
    private String readerName;

    @ExcelProperty(value = "书目ID")
    private Long bookId;

    /** 书名——列表展示用，非表字段 */
    @ExcelProperty(value = "书名")
    private String bookName;

    @ExcelProperty(value = "馆藏册ID")
    private Long itemId;

    @ExcelProperty(value = "队列位次")
    private Integer queueNo;

    @ExcelProperty(value = "状态")
    private Integer status;

    @ExcelProperty(value = "预约时间")
    private Date holdTime;

    @ExcelProperty(value = "到书时间")
    private Date readyTime;

    @ExcelProperty(value = "保留期截止")
    private Date holdDeadline;

    private Date pickupTime;

    private Date cancelTime;

    private Date createTime;

}

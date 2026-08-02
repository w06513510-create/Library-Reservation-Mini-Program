package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Blacklist;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 黑名单视图对象 biz_blacklist
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Blacklist.class)
public class BlacklistVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "黑名单ID")
    private Long id;

    @ExcelProperty(value = "读者ID")
    private Long readerId;

    @ExcelProperty(value = "原因")
    private String reason;

    @ExcelProperty(value = "生效时间")
    private Date startTime;

    @ExcelProperty(value = "到期时间")
    private Date endTime;

    @ExcelProperty(value = "状态")
    private Integer status;

    private Integer releaseType;

    private Date releaseTime;

    private Date createTime;

}

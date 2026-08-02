package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Supervise;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 占座监督视图对象 biz_supervise
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Supervise.class)
public class SuperviseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 监督ID
     */
    @ExcelProperty(value = "监督ID")
    private Long id;

    /**
     * 被监督的座位预约单ID
     */
    @ExcelProperty(value = "被监督预约单ID")
    private Long reservationId;

    /**
     * 座位ID
     */
    @ExcelProperty(value = "座位ID")
    private Long seatId;

    /**
     * 举报读者ID（app_user）
     */
    @ExcelProperty(value = "举报读者ID")
    private Long reporterId;

    /**
     * 举报时间
     */
    @ExcelProperty(value = "举报时间")
    private Date reportTime;

    /**
     * 落座截止时间
     */
    @ExcelProperty(value = "落座截止")
    private Date deadline;

    /**
     * 状态：0进行中 1已解除已落座 2超时释放
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 解除时间
     */
    @ExcelProperty(value = "解除时间")
    private Date resolveTime;

    /**
     * 创建时间
     */
    private Date createTime;

}

package org.dromara.library.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.library.domain.Reader;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 读者档案视图对象 biz_reader
 *
 * @author library
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Reader.class)
public class ReaderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 读者档案ID
     */
    @ExcelProperty(value = "读者档案ID")
    private Long id;

    /**
     * 关联C端账号ID（app_user，1:1）
     */
    @ExcelProperty(value = "关联C端账号ID")
    private Long userId;

    /**
     * 学号/校园卡号（实名唯一）
     */
    @ExcelProperty(value = "学号/校园卡号")
    private String studentNo;

    /**
     * 真实姓名
     */
    @ExcelProperty(value = "真实姓名")
    private String realName;

    /**
     * 院系
     */
    @ExcelProperty(value = "院系")
    private String college;

    /**
     * 专业
     */
    @ExcelProperty(value = "专业")
    private String major;

    /**
     * 当前信用分（0-100）
     */
    @ExcelProperty(value = "当前信用分")
    private Integer creditScore;

    /**
     * 守信(履约)次数
     */
    @ExcelProperty(value = "守信次数")
    private Integer performCount;

    /**
     * 是否黑名单：0否 1是
     */
    @ExcelProperty(value = "是否黑名单")
    private Integer blacklistFlag;

    /**
     * 黑名单暂停到期时间
     */
    @ExcelProperty(value = "黑名单暂停到期时间")
    private Date blacklistEndTime;

    /**
     * 状态：0正常 1受限 2停用
     */
    @ExcelProperty(value = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

}

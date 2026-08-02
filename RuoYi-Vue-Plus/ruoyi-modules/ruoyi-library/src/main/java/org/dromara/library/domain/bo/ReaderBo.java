package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Reader;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * 读者档案业务对象 biz_reader
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Reader.class, reverseConvertGenerate = false)
public class ReaderBo extends BaseEntity {

    /**
     * 读者档案ID
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 关联C端账号ID（app_user，1:1）
     */
    @NotNull(message = "关联C端账号ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long userId;

    /**
     * 学号/校园卡号（实名唯一）
     */
    @NotBlank(message = "学号/校园卡号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String studentNo;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 院系
     */
    private String college;

    /**
     * 专业
     */
    private String major;

    /**
     * 当前信用分（0-100）
     */
    private Integer creditScore;

    /**
     * 守信(履约)次数
     */
    private Integer performCount;

    /**
     * 是否黑名单：0否 1是
     */
    private Integer blacklistFlag;

    /**
     * 黑名单暂停到期时间
     */
    private Date blacklistEndTime;

    /**
     * 状态：0正常 1受限 2停用
     */
    private Integer status;

}

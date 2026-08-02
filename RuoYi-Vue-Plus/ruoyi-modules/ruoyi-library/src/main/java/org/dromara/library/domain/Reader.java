package org.dromara.library.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.common.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 读者档案对象 biz_reader
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_reader")
public class Reader extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 读者档案ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联C端账号ID（app_user，1:1）
     */
    private Long userId;

    /**
     * 学号/校园卡号（实名唯一）
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}

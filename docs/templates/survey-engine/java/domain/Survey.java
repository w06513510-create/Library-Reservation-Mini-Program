package org.dromara.survey.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 问卷/测评/试卷主表 survey
 * <p>objective + anonymous 两个开关区分"考试/调查/问卷"三种用法。
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey")
public class Survey extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 标题 */
    private String title;

    /** 说明/须知 */
    private String description;

    /** 是否客观题有标准答案(0调查问卷 1考试测验) */
    private Integer objective;

    /** 是否匿名(0实名 1匿名) */
    private Integer anonymous;

    /** 是否允许重复作答(0否 1是) */
    private Integer allowRepeat;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 时长(分钟,0不限时) */
    private Integer duration;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 状态(0草稿 1进行中/已发布 2已结束/停用 3已归档) */
    private Integer status;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

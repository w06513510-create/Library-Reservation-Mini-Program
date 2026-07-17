package org.dromara.survey.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.survey.domain.Survey;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 问卷/试卷主表视图对象 survey
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = Survey.class)
public class SurveyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
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

    /** 创建时间 */
    private Date createTime;

}

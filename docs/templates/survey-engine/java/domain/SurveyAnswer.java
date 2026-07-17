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
 * 问卷/试卷作答明细 survey_answer（每人每题一行）
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_answer")
public class SurveyAnswer extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 作答会话(考试用;调查可空/0) */
    private Long recordId;

    /** 所属问卷/试卷 */
    private Long surveyId;

    /** 题目 */
    private Long questionId;

    /** 作答人(匿名为空) */
    private Long respondentId;

    /** 答案(选项key / 评分值 / 文本) */
    private String answerValue;

    /** 是否正确(客观题;0否 1是;可空) */
    private Integer isCorrect;

    /** 本题得分(客观题;可空) */
    private Integer score;

    /** 提交时间 */
    private Date submitTime;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

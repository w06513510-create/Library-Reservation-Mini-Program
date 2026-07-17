package org.dromara.survey.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.survey.domain.SurveyQuestion;

import java.io.Serial;
import java.io.Serializable;

/**
 * 问卷/试卷题目视图对象 survey_question
 * <p>下发给作答端时，考试场景应剔除 correctAnswer（别把答案发给考生）——在 service 里置 null 或用专门的"作答视图"。
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = SurveyQuestion.class)
public class SurveyQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属问卷/试卷 */
    private Long surveyId;

    /** 题干 */
    private String questionText;

    /** 题型(0单选 1多选 2判断 3填空 4评分) */
    private Integer questionType;

    /** 选项JSON */
    private String options;

    /** 标准答案(下发作答端时应置null) */
    private String correctAnswer;

    /** 本题分值 */
    private Integer score;

    /** 是否必答(0否 1是) */
    private Integer required;

    /** 题序 */
    private Integer orderNum;

}

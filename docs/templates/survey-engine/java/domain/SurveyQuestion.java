package org.dromara.survey.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 问卷/试卷题目 survey_question
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_question")
public class SurveyQuestion extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 所属问卷/试卷 */
    private Long surveyId;

    /** 题干 */
    private String questionText;

    /** 题型(0单选 1多选 2判断 3填空 4评分) */
    private Integer questionType;

    /** 选项JSON [{"key":"A","text":"..."}] */
    private String options;

    /** 标准答案(客观题:单选A;多选A,C;判断1/0;可空) */
    private String correctAnswer;

    /** 本题分值(objective=1时有意义) */
    private Integer score;

    /** 是否必答(0否 1是) */
    private Integer required;

    /** 题序 */
    private Integer orderNum;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

package org.dromara.survey.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题目 Excel 导入模板对象
 * <p>范式提炼自宿舍 ExamQuestionImportVo/SurveyQuestionImportVo：一列一属性，选项拆成 A/B/C/D 列，
 * 导入时由 service 拼成 options JSON、归一化 correctAnswer。
 * <p>题型/正确答案支持中文或字母，导入时统一解析（见 ISurveyService#importQuestions 说明）。
 *
 * @author ruoyi-template
 */
@Data
@ExcelIgnoreUnannotated
public class SurveyQuestionImportVo {

    /** 所属问卷/试卷ID */
    @ExcelProperty(value = "问卷ID")
    private Long surveyId;

    /** 题干 */
    @ExcelProperty(value = "题干")
    private String questionText;

    /** 题型(单选/多选/判断/填空/评分) */
    @ExcelProperty(value = "题型")
    private String questionType;

    /** 选项A */
    @ExcelProperty(value = "选项A")
    private String optionA;

    /** 选项B */
    @ExcelProperty(value = "选项B")
    private String optionB;

    /** 选项C */
    @ExcelProperty(value = "选项C")
    private String optionC;

    /** 选项D */
    @ExcelProperty(value = "选项D")
    private String optionD;

    /** 正确答案(单选填A;多选填A,C;判断填对/错;调查题留空) */
    @ExcelProperty(value = "正确答案")
    private String correctAnswer;

    /** 分值(考试题填;调查题留空) */
    @ExcelProperty(value = "分值")
    private Integer score;

    /** 题序 */
    @ExcelProperty(value = "题序")
    private Integer orderNum;

}

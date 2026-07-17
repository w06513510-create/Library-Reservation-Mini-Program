package org.dromara.survey.service;

import org.dromara.survey.domain.bo.SurveySubmitBo;
import org.dromara.survey.domain.vo.SurveyQuestionImportVo;

import java.util.List;
import java.util.Map;

/**
 * 问卷/测评引擎 Service
 *
 * @author ruoyi-template
 */
public interface ISurveyService {

    /**
     * 提交作答（引擎核心）。
     * <p>自动处理：匿名/实名、重复作答守卫、客观题自动评分（objective=1 时生成 survey_record + 总分/是否及格）。
     *
     * @return objective=1 返回作答会话 recordId（含得分，可据此查成绩）；objective=0（调查）返回 null
     */
    Long submit(SurveySubmitBo bo);

    /**
     * 问卷统计（无标准答案场景）：按题型聚合——
     * 单选/多选/判断=选项频次；评分=平均分；填空=文本列表。
     */
    List<Map<String, Object>> stats(Long surveyId);

    /**
     * Excel 批量导入题目。
     * <p>范式：题型/正确答案支持中文或字母，导入时统一解析；选项 A/B/C/D 拼成 options JSON；
     * 判断题强制选项"对/错(1/0)"；correctAnswer 归一化（多选去空格转大写排序）。
     *
     * @return 成功导入条数
     */
    int importQuestions(List<SurveyQuestionImportVo> list);

}

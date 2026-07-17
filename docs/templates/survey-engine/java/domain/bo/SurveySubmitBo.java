package org.dromara.survey.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 作答提交请求体：一次交卷 = 问卷ID + 若干题作答
 *
 * @author ruoyi-template
 */
@Data
public class SurveySubmitBo {

    /** 问卷/试卷ID */
    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

    /** 各题作答 */
    @NotEmpty(message = "作答不能为空")
    private List<Item> answers;

    /**
     * 单题作答项
     */
    @Data
    public static class Item {

        /** 题目ID */
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        /** 答案(选项key / 评分值 / 文本) */
        private String answerValue;

    }

}

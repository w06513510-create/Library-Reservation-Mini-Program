package org.dromara.survey.constant;

/**
 * 题型常量（统一超集，消除宿舍三套实现里"码 2 冲突"的问题）
 * <p>原实现：考试 2=判断、调查 2=评分，撞码。这里合成一套无歧义超集。
 *
 * @author ruoyi-template
 */
public interface QuestionType {

    /** 单选 */
    int SINGLE = 0;

    /** 多选 */
    int MULTI = 1;

    /** 判断 */
    int JUDGE = 2;

    /** 填空(主观,不自动评分) */
    int FILL = 3;

    /** 评分(主观,不自动评分,用于满意度打分) */
    int RATING = 4;

    /** 是否客观题(可自动判分)：单选/多选/判断 */
    static boolean isObjective(Integer type) {
        return type != null && (type == SINGLE || type == MULTI || type == JUDGE);
    }

}

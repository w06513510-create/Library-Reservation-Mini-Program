package org.dromara.survey.utils;

import org.dromara.survey.constant.QuestionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 客观题自动评分工具（模板核心）
 * <p>提炼自宿舍 ExamServiceImpl 的 isCorrect + normSet：
 * <ul>
 *   <li>单选/判断：去空白后 equalsIgnoreCase</li>
 *   <li>多选：逗号拆分→trim→大写→排序→重拼，做"集合相等"比较（忽略顺序/大小写/空白）</li>
 *   <li>填空/评分：主观题，不自动判分（返回 false，交由人工或仅做统计）</li>
 *   <li>空答案一律判错</li>
 * </ul>
 *
 * @author ruoyi-template
 */
public class SurveyScoreUtils {

    private SurveyScoreUtils() {
    }

    /**
     * 判断某题作答是否正确（仅客观题）。
     *
     * @param type    题型（{@link QuestionType}）
     * @param correct 标准答案（单选"A"；多选"A,C"；判断"1"/"0"）
     * @param answer  学生作答
     * @return 是否正确；主观题(填空/评分)恒返回 false
     */
    public static boolean isCorrect(Integer type, String correct, String answer) {
        if (!QuestionType.isObjective(type)) {
            return false;   // 主观题不自动判分
        }
        String c = correct == null ? "" : correct.trim();
        String s = answer == null ? "" : answer.trim();
        if (s.isEmpty()) {
            return false;   // 空答案判错
        }
        if (type == QuestionType.MULTI) {
            return normSet(c).equals(normSet(s));   // 多选：集合相等
        }
        return c.equalsIgnoreCase(s);               // 单选/判断：忽略大小写字符串相等
    }

    /**
     * 计算某题得分：正确得满分(allot)，错误得 0。
     *
     * @param type    题型
     * @param correct 标准答案
     * @param answer  作答
     * @param allot   本题应得分值
     * @return 实际得分
     */
    public static int scoreOne(Integer type, String correct, String answer, int allot) {
        return isCorrect(type, correct, answer) ? allot : 0;
    }

    /** 归一化多选答案："a, c" → "A,C"（拆分→trim→大写→排序→重拼） */
    private static String normSet(String s) {
        List<String> list = new ArrayList<>();
        for (String p : s.split(",")) {
            String t = p.trim().toUpperCase();
            if (!t.isEmpty()) {
                list.add(t);
            }
        }
        Collections.sort(list);
        return String.join(",", list);
    }

}

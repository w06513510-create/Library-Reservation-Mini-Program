package org.dromara.survey.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.survey.constant.QuestionType;
import org.dromara.survey.domain.Survey;
import org.dromara.survey.domain.SurveyAnswer;
import org.dromara.survey.domain.SurveyQuestion;
import org.dromara.survey.domain.SurveyRecord;
import org.dromara.survey.domain.bo.SurveySubmitBo;
import org.dromara.survey.domain.vo.SurveyQuestionImportVo;
import org.dromara.survey.mapper.SurveyAnswerMapper;
import org.dromara.survey.mapper.SurveyMapper;
import org.dromara.survey.mapper.SurveyQuestionMapper;
import org.dromara.survey.mapper.SurveyRecordMapper;
import org.dromara.survey.service.ISurveyService;
import org.dromara.survey.utils.SurveyScoreUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷/测评引擎 Service 实现
 * <p>统一了宿舍 SurveyServiceImpl(匿名+统计) 与 ExamServiceImpl(自动评分) 两套逻辑。
 *
 * @author ruoyi-template
 */
@RequiredArgsConstructor
@Service
public class SurveyServiceImpl implements ISurveyService {

    private final SurveyMapper baseMapper;
    private final SurveyQuestionMapper questionMapper;
    private final SurveyAnswerMapper answerMapper;
    private final SurveyRecordMapper recordMapper;

    private static final int STATUS_RUNNING = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(SurveySubmitBo bo) {
        Survey survey = baseMapper.selectById(bo.getSurveyId());
        if (survey == null) {
            throw new ServiceException("问卷不存在");
        }
        if (survey.getStatus() == null || survey.getStatus() != STATUS_RUNNING) {
            throw new ServiceException("问卷未开放作答");
        }
        boolean anonymous = survey.getAnonymous() != null && survey.getAnonymous() == 1;
        boolean objective = survey.getObjective() != null && survey.getObjective() == 1;
        Long respondentId = anonymous ? null : LoginHelper.getUserId();

        // 重复作答守卫：仅在"不允许重复 且 非匿名"时校验
        boolean allowRepeat = survey.getAllowRepeat() != null && survey.getAllowRepeat() == 1;
        if (!allowRepeat && !anonymous) {
            Long cnt = answerMapper.selectCount(Wrappers.<SurveyAnswer>lambdaQuery()
                .eq(SurveyAnswer::getSurveyId, survey.getId())
                .eq(SurveyAnswer::getRespondentId, respondentId));
            if (cnt != null && cnt > 0) {
                throw new ServiceException("您已提交过该问卷");
            }
        }

        // 题目索引（用于判分/取分值）
        Map<Long, SurveyQuestion> qMap = questionMapper
            .selectList(Wrappers.<SurveyQuestion>lambdaQuery().eq(SurveyQuestion::getSurveyId, survey.getId()))
            .stream().collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        // 考试型先建作答会话
        Long recordId = 0L;
        SurveyRecord record = null;
        if (objective) {
            record = new SurveyRecord();
            record.setSurveyId(survey.getId());
            record.setRespondentId(respondentId);
            record.setStartTime(new Date());
            record.setStatus(0);
            recordMapper.insert(record);
            recordId = record.getId();
        }

        // 落作答明细；客观题自动评分累计
        int total = 0;
        Date now = new Date();
        for (SurveySubmitBo.Item item : bo.getAnswers()) {
            SurveyQuestion q = qMap.get(item.getQuestionId());
            if (q == null) {
                continue;
            }
            SurveyAnswer a = new SurveyAnswer();
            a.setRecordId(recordId);
            a.setSurveyId(survey.getId());
            a.setQuestionId(q.getId());
            a.setRespondentId(respondentId);
            a.setAnswerValue(item.getAnswerValue());
            a.setSubmitTime(now);
            if (objective && QuestionType.isObjective(q.getQuestionType())) {
                int allot = q.getScore() == null ? 0 : q.getScore();
                boolean ok = SurveyScoreUtils.isCorrect(q.getQuestionType(), q.getCorrectAnswer(), item.getAnswerValue());
                int earned = ok ? allot : 0;
                a.setIsCorrect(ok ? 1 : 0);
                a.setScore(earned);
                total += earned;
            }
            answerMapper.insert(a);
        }

        // 回填会话成绩
        if (objective && record != null) {
            int pass = survey.getPassScore() != null ? survey.getPassScore() : 60;
            record.setSubmitTime(now);
            record.setScore(total);
            record.setIsPassed(total >= pass ? 1 : 0);
            record.setStatus(2);
            recordMapper.updateById(record);
            return record.getId();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> stats(Long surveyId) {
        List<SurveyQuestion> questions = questionMapper.selectList(
            Wrappers.<SurveyQuestion>lambdaQuery().eq(SurveyQuestion::getSurveyId, surveyId).orderByAsc(SurveyQuestion::getOrderNum));
        List<SurveyAnswer> answers = answerMapper.selectList(
            Wrappers.<SurveyAnswer>lambdaQuery().eq(SurveyAnswer::getSurveyId, surveyId));
        Map<Long, List<SurveyAnswer>> byQuestion = answers.stream()
            .collect(Collectors.groupingBy(SurveyAnswer::getQuestionId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (SurveyQuestion q : questions) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("questionId", q.getId());
            stat.put("questionText", q.getQuestionText());
            stat.put("questionType", q.getQuestionType());
            List<SurveyAnswer> list = byQuestion.getOrDefault(q.getId(), Collections.emptyList());
            Integer type = q.getQuestionType();
            if (type != null && type == QuestionType.RATING) {
                // 评分题：平均分
                double avg = list.stream()
                    .map(SurveyAnswer::getAnswerValue).filter(StringUtils::isNotBlank)
                    .mapToDouble(v -> { try { return Double.parseDouble(v.trim()); } catch (Exception e) { return 0; } })
                    .average().orElse(0);
                stat.put("average", Math.round(avg * 100.0) / 100.0);
            } else if (type != null && type == QuestionType.FILL) {
                // 填空题：文本列表
                stat.put("texts", list.stream().map(SurveyAnswer::getAnswerValue)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toList()));
            } else {
                // 单选/多选/判断：选项频次(多选按逗号拆)
                Map<String, Integer> freq = new LinkedHashMap<>();
                for (SurveyAnswer a : list) {
                    if (StringUtils.isBlank(a.getAnswerValue())) {
                        continue;
                    }
                    for (String opt : a.getAnswerValue().split(",")) {
                        String key = opt.trim();
                        if (!key.isEmpty()) {
                            freq.merge(key, 1, Integer::sum);
                        }
                    }
                }
                stat.put("optionCount", freq);
            }
            stat.put("total", list.size());
            result.add(stat);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importQuestions(List<SurveyQuestionImportVo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (SurveyQuestionImportVo vo : list) {
            if (StringUtils.isBlank(vo.getQuestionText())) {
                continue;
            }
            SurveyQuestion q = new SurveyQuestion();
            q.setSurveyId(vo.getSurveyId());
            q.setQuestionText(vo.getQuestionText().trim());
            int type = parseType(vo.getQuestionType());
            q.setQuestionType(type);
            q.setOptions(buildOptions(type, vo));
            q.setCorrectAnswer(normalizeAnswer(type, vo.getCorrectAnswer()));
            q.setScore(vo.getScore());
            q.setRequired(1);
            q.setOrderNum(vo.getOrderNum() == null ? n + 1 : vo.getOrderNum());
            questionMapper.insert(q);
            n++;
        }
        return n;
    }

    /** 题型解析：支持中文或数字 */
    private int parseType(String s) {
        String t = s == null ? "" : s.trim();
        if (t.contains("多选") || "1".equals(t)) return QuestionType.MULTI;
        if (t.contains("判断") || "2".equals(t)) return QuestionType.JUDGE;
        if (t.contains("填空") || "3".equals(t)) return QuestionType.FILL;
        if (t.contains("评分") || "4".equals(t)) return QuestionType.RATING;
        return QuestionType.SINGLE;
    }

    /** 选项 A/B/C/D 拼成 JSON；判断题固定"对/错"；填空/评分无选项 */
    private String buildOptions(int type, SurveyQuestionImportVo vo) {
        if (type == QuestionType.FILL || type == QuestionType.RATING) {
            return null;
        }
        List<Map<String, String>> opts = new ArrayList<>();
        if (type == QuestionType.JUDGE) {
            opts.add(Map.of("key", "1", "text", "对"));
            opts.add(Map.of("key", "0", "text", "错"));
        } else {
            addOpt(opts, "A", vo.getOptionA());
            addOpt(opts, "B", vo.getOptionB());
            addOpt(opts, "C", vo.getOptionC());
            addOpt(opts, "D", vo.getOptionD());
        }
        return JSONUtil.toJsonStr(opts);
    }

    private void addOpt(List<Map<String, String>> opts, String key, String text) {
        if (StringUtils.isNotBlank(text)) {
            opts.add(Map.of("key", key, "text", text.trim()));
        }
    }

    /** 归一化答案：判断题"对/正确/√/1"→"1"否则"0"；其它去空格转大写(多选"a, c"→"A,C") */
    private String normalizeAnswer(int type, String ans) {
        if (StringUtils.isBlank(ans)) {
            return null;
        }
        String a = ans.trim();
        if (type == QuestionType.JUDGE) {
            return (a.contains("对") || a.contains("正确") || a.contains("√") || "1".equals(a)) ? "1" : "0";
        }
        return a.toUpperCase().replace(" ", "");
    }

}

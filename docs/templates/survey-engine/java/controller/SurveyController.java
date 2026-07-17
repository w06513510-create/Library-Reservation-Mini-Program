package org.dromara.survey.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.dromara.survey.domain.bo.SurveySubmitBo;
import org.dromara.survey.domain.vo.SurveyQuestionImportVo;
import org.dromara.survey.service.ISurveyService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 问卷/测评 作答与统计 Controller（问卷/题目的 CRUD 用标准单表 Controller，另建即可，此处只放引擎接口）。
 *
 * @author ruoyi-template
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/survey")
public class SurveyController extends BaseController {

    private final ISurveyService surveyService;

    /** 提交作答 */
    @SaCheckPermission("survey:answer:add")
    @Log(title = "问卷作答", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/submit")
    public R<Long> submit(@Validated @RequestBody SurveySubmitBo bo) {
        // 考试型返回 recordId（可据此查成绩）；调查型返回 null
        return R.ok("提交成功", surveyService.submit(bo));
    }

    /** 问卷统计（选项频次/平均分/文本列表） */
    @SaCheckPermission("survey:stats:query")
    @GetMapping("/stats/{surveyId}")
    public R<List<Map<String, Object>>> stats(@NotNull(message = "问卷ID不能为空") @PathVariable Long surveyId) {
        return R.ok(surveyService.stats(surveyId));
    }

    /** 下载题目导入模板（导出空表头） */
    @SaCheckPermission("survey:question:import")
    @PostMapping("/question/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<SurveyQuestionImportVo>(), "题目导入模板", SurveyQuestionImportVo.class, response);
    }

    /** Excel 批量导入题目 */
    @SaCheckPermission("survey:question:import")
    @Log(title = "题目导入", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/question/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<SurveyQuestionImportVo> result = ExcelUtil.importExcel(file.getInputStream(), SurveyQuestionImportVo.class, true);
        int n = surveyService.importQuestions(result.getList());
        return R.ok("成功导入 " + n + " 道题目", null);
    }

}

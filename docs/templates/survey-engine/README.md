# 问卷 / 测评 / 考试 引擎 模板

> 提炼自宿舍 `ruoyi-dorm` 的三套近重复实现：`dorm_exam_*`（有标准答案 + 自动评分）、`dorm_survey_*`（匿名 + 无评分统计）、`dorm_questionnaire`（画像题库）。
> 宿舍里"题目 + 作答 + 自动评分 + Excel 导入"抄了 3 份——本模板**抽成一套可配置引擎**，用主表两个开关区分用途。

## 一套表覆盖三种用法（靠 survey 主表两个开关）

| 用途 | `objective` | `anonymous` | 效果 |
|---|---|---|---|
| 考试 / 测验 | `1` 有标准答案 | `0` 实名 | 客观题自动评分，生成 `survey_record` 会话（总分/是否及格） |
| 满意度 / 问卷调查 | `0` 无标准答案 | `1` 匿名 | 不记名、不评分，只做选项频次/平均分统计 |
| 实名调研 | `0` | `0` | 记名、不评分，重复作答守卫生效 |

另有 `allow_repeat`（是否允许重复作答）配合 `anonymous` 控制去重。

## 目录

```
survey-engine/
├── README.md
├── ddl/survey.sql       ← survey / survey_question / survey_answer (+ 可选 survey_record)
└── java/
    ├── constant/QuestionType.java          ← 0单选 1多选 2判断 3填空 4评分(消除原3套码2冲突)
    ├── utils/SurveyScoreUtils.java         ← 客观题自动评分核心(isCorrect/normSet/scoreOne)
    ├── domain/{Survey,SurveyQuestion,SurveyAnswer,SurveyRecord}.java
    ├── domain/bo/SurveySubmitBo.java       ← 交卷体(surveyId + answers[])
    ├── domain/vo/{SurveyVo,SurveyQuestionVo,SurveyQuestionImportVo}.java
    ├── mapper/{Survey,SurveyQuestion,SurveyAnswer,SurveyRecord}Mapper.java
    ├── service/ISurveyService.java
    ├── service/impl/SurveyServiceImpl.java ← submit(自动评分+匿名+去重) / stats / importQuestions
    └── controller/SurveyController.java    ← /submit /stats /question/import*
```

## 复制到哪、改什么

1. **建表**：执行 `ddl/survey.sql`。只做调查/问卷可不建 `survey_record`（并删 `SurveyRecord.java`/`SurveyRecordMapper.java` 及 impl 里相关分支）。表名前缀 `survey` 可换成你的业务（如 `club_vote`）。
2. **落 Java**：`java/` 复制到 `ruoyi-modules/ruoyi-<biz>/src/main/java/org/dromara/<biz>/` 对应包；全局替换包名 `org.dromara.survey`、路径 `/survey`、权限前缀 `survey:*`。
3. **问卷/题目的后台 CRUD**：本模板只给了"引擎接口"（作答/统计/导入）。问卷主表与题目的增删改查请用基座标准单表 Controller（照 `ruoyi-demo` 的 `TestDemoController` 生成即可），不重复造。
4. **考试防作弊**：下发题目给作答端时，务必把 `SurveyQuestionVo.correctAnswer` 置 null（别把答案发给考生）——在你的"取卷"接口里处理。

## 通用 vs 按业务改

| 部分 | 通用（直接用） | 按业务改 |
|---|---|---|
| `QuestionType` 超集枚举 | ✅ 0~4 | 如需"排序题/矩阵题"再扩码 |
| `SurveyScoreUtils` 客观题判分（单选/判断=忽略大小写相等；多选=集合相等；空答判错） | ✅ 直接复用 | 主观题(填空/评分)本就不自动判分 |
| `submit`：匿名/实名分流、重复守卫、objective 自动评分+会话成绩 | ✅ 引擎核心 | 及格分默认 60；考试次数限制 `max_attempts` 可自行加 |
| `stats`：评分题=平均分/填空=文本列表/其它=选项频次 | ✅ | 需要交叉分析再扩 |
| `importQuestions`：题型/答案支持中文或字母、A/B/C/D→options JSON、判断题固定"对/错"、答案归一化 | ✅ 范式 | 选项列数、维度/权重列(问卷画像)自行加 |
| options JSON 形状 `[{"key","text"}]` | ✅ | 画像题若要 `[{"text","value"}]` 另改 |

## Excel 模板导入范式（对齐基座 ExcelUtil）

- **下模板**：`ExcelUtil.exportExcel(new ArrayList<ImportVo>(), "题目导入模板", SurveyQuestionImportVo.class, response)` —— 导出空列表只出表头，即导入模板。
- **导入**：`ExcelUtil.importExcel(file.getInputStream(), SurveyQuestionImportVo.class, true)` 同步返回 `ExcelResult<Vo>`，`getList()` 拿全部行，service 逐行 `insert`（宿舍原实现即如此，不用自定义 Listener）。
- **导入 VO**：一列一属性 + `@ExcelProperty("中文表头")` + `@ExcelIgnoreUnannotated`；选项拆 A/B/C/D 列，导入时拼 options JSON；题型/正确答案写中文，导入时统一解析（`parseType`/`normalizeAnswer`）。
- 导入模板列：`问卷ID | 题干 | 题型(单选/多选/判断/填空/评分) | 选项A | 选项B | 选项C | 选项D | 正确答案(单选A;多选A,C;判断对/错;调查留空) | 分值 | 题序`。

## 自检要点

- `submit` 在 `@Transactional` 内：建会话、落明细、判分、回填成绩要么全成要么全回滚。
- 匿名时 `respondent_id` 置 null 且**跳过重复守卫**（匿名无法去重，符合原实现）。
- 客观题判分只对 `objective=1` 的问卷生效；`objective=0` 的调查即便有 correctAnswer 也不判分（`submit` 里双重判断 `objective && QuestionType.isObjective(type)`）。
- 题型码是三套原实现合成的**无歧义超集**（原考试 2=判断、调查 2=评分冲突，这里 2=判断、4=评分分开）。

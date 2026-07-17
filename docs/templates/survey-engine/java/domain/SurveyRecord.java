package org.dromara.survey.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 考试作答会话 survey_record（可选，仅 objective=1 的考试型用）
 *
 * @author ruoyi-template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("survey_record")
public class SurveyRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 所属试卷 */
    private Long surveyId;

    /** 作答人 */
    private Long respondentId;

    /** 开始时间 */
    private Date startTime;

    /** 交卷时间 */
    private Date submitTime;

    /** 得分 */
    private Integer score;

    /** 是否及格(0否 1是) */
    private Integer isPassed;

    /** 第几次作答 */
    private Integer attemptNo;

    /** 用时(秒) */
    private Integer durationUsed;

    /** 状态(0进行中 1已交卷 2已评分) */
    private Integer status;

    /** 逻辑删除标志(0存在 2删除) */
    @TableLogic
    private String delFlag;

}

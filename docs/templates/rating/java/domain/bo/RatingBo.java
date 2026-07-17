package org.dromara.biz.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.QueryGroup;

/**
 * 评价提交/查询请求体。
 * <p>安全要点：<b>评价方向与被评价人不由前端传</b>——前端只传 bizId + score + content，
 * 服务端据登录用户与业务单双方解析 evalRole / toUserId，防越权与刷评。
 *
 * @author ruoyi-template
 */
@Data
public class RatingBo {

    /** 业务类型(列表查询也用它过滤) */
    private String bizType;

    /** 被评业务主键 */
    @NotNull(message = "业务ID不能为空", groups = {AddGroup.class})
    private Long bizId;

    /** 评分(1-5星) */
    @NotNull(message = "评分不能为空", groups = {AddGroup.class})
    @Min(value = 1, message = "评分最低1星", groups = {AddGroup.class})
    @Max(value = 5, message = "评分最高5星", groups = {AddGroup.class})
    private Integer score;

    /** 评价内容(可选) */
    private String content;

    /** 评价方向(仅列表查询用作过滤条件) */
    private Integer evalRole;

}

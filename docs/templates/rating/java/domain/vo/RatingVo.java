package org.dromara.biz.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.biz.domain.Rating;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用评价视图对象 rating
 *
 * @author ruoyi-template
 */
@Data
@AutoMapper(target = Rating.class)
public class RatingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 业务类型 */
    private String bizType;

    /** 被评业务主键 */
    private Long bizId;

    /** 业务单号 */
    private String bizNo;

    /** 评价方向(1甲评乙 2乙评甲) */
    private Integer evalRole;

    /** 评价人 */
    private Long fromUserId;

    /** 被评价人 */
    private Long toUserId;

    /** 评分(1-5星) */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 是否系统默认好评 */
    private Integer isDefault;

    /** 评价时间 */
    private Date createTime;

}

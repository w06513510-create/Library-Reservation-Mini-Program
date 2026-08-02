package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Blacklist;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 黑名单业务对象 biz_blacklist
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Blacklist.class, reverseConvertGenerate = false)
public class BlacklistBo extends BaseEntity {

    private Long id;

    /** 读者ID（app_user） */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /** 拉黑原因 */
    private String reason;

    /** 暂停天数（手动加入时用） */
    private Integer days;

    /** 状态（查询） */
    private Integer status;

}

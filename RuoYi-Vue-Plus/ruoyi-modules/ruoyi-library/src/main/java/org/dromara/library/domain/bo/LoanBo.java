package org.dromara.library.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.library.domain.Loan;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 借阅单业务对象 biz_loan
 *
 * @author library
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Loan.class, reverseConvertGenerate = false)
public class LoanBo extends BaseEntity {

    private Long id;

    /** 读者ID（app_user） */
    @NotNull(message = "读者不能为空", groups = {AddGroup.class})
    private Long readerId;

    /** 馆藏册ID（借出） */
    @NotNull(message = "馆藏册不能为空", groups = {AddGroup.class})
    private Long itemId;

    /** 书目ID（查询） */
    private Long bookId;

    /** 状态（查询） */
    private Integer status;

}

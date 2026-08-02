package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.LoanBo;
import org.dromara.library.domain.vo.LoanVo;

import java.util.List;

/**
 * 借阅流通Service（借出/归还/续借/催还）
 *
 * @author library
 */
public interface ILoanService {

    LoanVo queryById(Long id);

    TableDataInfo<LoanVo> queryPageList(LoanBo bo, PageQuery pageQuery);

    List<LoanVo> queryList(LoanBo bo);

    /** 借出：校验黑名单/信用/可借上限 + 册可借，册→借出、可借数-1，建在借单 */
    Boolean borrow(LoanBo bo);

    /** 归还：在借/逾期→已还；有 hold 排队则该册转预约架并到书通知队首，否则回架、可借数+1 */
    Boolean returnBook(Long loanId);

    /** 续借：未逾期、未被催还、未超次数，延长应还日 */
    Boolean renew(Long loanId);

    /** 预约催还：对在借且被他人预约的书，标记催还 */
    Boolean recall(Long loanId);

}

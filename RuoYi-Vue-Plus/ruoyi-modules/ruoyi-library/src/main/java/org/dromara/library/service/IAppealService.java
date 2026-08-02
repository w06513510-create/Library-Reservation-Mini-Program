package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.AppealBo;
import org.dromara.library.domain.vo.AppealVo;

import java.util.List;

/**
 * 违约申诉Service
 *
 * @author library
 */
public interface IAppealService {

    AppealVo queryById(Long id);

    TableDataInfo<AppealVo> queryPageList(AppealBo bo, PageQuery pageQuery);

    List<AppealVo> queryList(AppealBo bo);

    /** 提交申诉（对有效违约） */
    Boolean submit(AppealBo bo);

    /** 审批：pass=true 通过则解除违约 + 冲正回补信用；false 驳回 */
    Boolean audit(Long id, boolean pass, String remark);

}

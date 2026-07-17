package org.dromara.biz.service;

import org.dromara.biz.domain.BizAuditLog;
import org.dromara.biz.domain.bo.BizAuditBo;
import org.dromara.biz.domain.vo.BizAuditVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 通用审核单 Service
 *
 * @author ruoyi-template
 */
public interface IBizAuditService {

    /** 分页查询审核单（后台审核列表） */
    TableDataInfo<BizAuditVo> queryPageList(BizAuditBo bo, PageQuery pageQuery);

    /** 详情 */
    BizAuditVo queryById(Long id);

    /** 提交申请（C端/后台发起，落 status=0 待审） */
    Long submit(BizAuditBo bo);

    /** 审核通过（status 0→1；触发回调 afterPass） */
    Boolean approve(Long id);

    /** 审核驳回（status 0→2，rejectReason 必填；触发回调 afterReject） */
    Boolean reject(Long id, String rejectReason);

    /** 分页查询某审核单的流转轨迹（仅多状态工单场景，需 biz_audit_log 表） */
    TableDataInfo<BizAuditLog> queryLogPage(Long auditId, PageQuery pageQuery);

}

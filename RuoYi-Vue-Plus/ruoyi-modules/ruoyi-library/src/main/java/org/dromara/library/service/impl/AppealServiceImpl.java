package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Appeal;
import org.dromara.library.domain.Violation;
import org.dromara.library.domain.bo.AppealBo;
import org.dromara.library.domain.vo.AppealVo;
import org.dromara.library.mapper.AppealMapper;
import org.dromara.library.mapper.ViolationMapper;
import org.dromara.library.service.IAppealService;
import org.dromara.library.service.ICreditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 违约申诉Service实现（通过→解除违约 + 冲正回补信用）
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class AppealServiceImpl implements IAppealService {

    private final AppealMapper baseMapper;
    private final ViolationMapper violationMapper;
    private final ICreditService creditService;

    @Override
    public AppealVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<AppealVo> queryPageList(AppealBo bo, PageQuery pageQuery) {
        Page<AppealVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<AppealVo> queryList(AppealBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Appeal> buildQueryWrapper(AppealBo bo) {
        LambdaQueryWrapper<Appeal> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Appeal::getReaderId, bo.getReaderId());
        lqw.eq(bo.getStatus() != null, Appeal::getStatus, bo.getStatus());
        lqw.orderByDesc(Appeal::getCreateTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(AppealBo bo) {
        Violation v = violationMapper.selectById(bo.getViolationId());
        if (v == null || v.getStatus() == null || v.getStatus() != 0) {
            throw new ServiceException("该违约不存在或已解除，无法申诉");
        }
        Long pending = baseMapper.selectCount(Wrappers.<Appeal>lambdaQuery()
            .eq(Appeal::getViolationId, bo.getViolationId()).eq(Appeal::getStatus, 0));
        if (pending != null && pending > 0) {
            throw new ServiceException("该违约已有待审申诉");
        }
        Appeal appeal = MapstructUtils.convert(bo, Appeal.class);
        appeal.setStatus(0);
        return baseMapper.insert(appeal) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean audit(Long id, boolean pass, String remark) {
        Appeal appeal = baseMapper.selectById(id);
        if (appeal == null) {
            throw new ServiceException("申诉不存在");
        }
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Appeal.class)
            .set(Appeal::getStatus, pass ? 1 : 2).set(Appeal::getAuditTime, new Date()).set(Appeal::getAuditRemark, remark)
            .eq(Appeal::getId, id).eq(Appeal::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("该申诉已审批");
        }
        if (pass) {
            Violation v = violationMapper.selectById(appeal.getViolationId());
            if (v != null && v.getStatus() != null && v.getStatus() == 0) {
                // 解除违约
                violationMapper.update(null, Wrappers.lambdaUpdate(Violation.class)
                    .set(Violation::getStatus, 1).eq(Violation::getId, v.getId()).eq(Violation::getStatus, 0));
                // 冲正回补信用（+原扣分）
                int deduct = v.getDeductScore() == null ? 0 : v.getDeductScore();
                if (deduct > 0) {
                    creditService.changeCredit(appeal.getReaderId(), deduct, 11, "申诉通过冲正", "violation", v.getId());
                }
            }
        }
        return true;
    }

}

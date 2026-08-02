package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.library.domain.PurchaseSuggest;
import org.dromara.library.domain.bo.PurchaseSuggestBo;
import org.dromara.library.domain.vo.PurchaseSuggestVo;
import org.dromara.library.mapper.PurchaseSuggestMapper;
import org.dromara.library.service.IPurchaseSuggestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 读者荐购Service业务层处理
 * 状态跃迁一律用 CAS（UPDATE ... WHERE id=? AND status=前置），影响行数=1 才继续，保证幂等/并发安全。
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class PurchaseSuggestServiceImpl implements IPurchaseSuggestService {

    private final PurchaseSuggestMapper baseMapper;

    @Override
    public PurchaseSuggestVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<PurchaseSuggestVo> queryPageList(PurchaseSuggestBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PurchaseSuggest> lqw = buildQueryWrapper(bo);
        Page<PurchaseSuggestVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<PurchaseSuggestVo> queryList(PurchaseSuggestBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<PurchaseSuggest> buildQueryWrapper(PurchaseSuggestBo bo) {
        LambdaQueryWrapper<PurchaseSuggest> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, PurchaseSuggest::getReaderId, bo.getReaderId());
        lqw.eq(bo.getStatus() != null, PurchaseSuggest::getStatus, bo.getStatus());
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), PurchaseSuggest::getTitle, bo.getTitle());
        lqw.orderByDesc(PurchaseSuggest::getCreateTime);
        return lqw;
    }

    @Override
    public Boolean insertByBo(PurchaseSuggestBo bo) {
        PurchaseSuggest add = MapstructUtils.convert(bo, PurchaseSuggest.class);
        if (add.getStatus() == null) {
            add.setStatus(0);
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(PurchaseSuggestBo bo) {
        PurchaseSuggest update = MapstructUtils.convert(bo, PurchaseSuggest.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean accept(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(PurchaseSuggest.class)
            .set(PurchaseSuggest::getStatus, 1)
            .set(PurchaseSuggest::getHandleBy, LoginHelper.getUserId())
            .set(PurchaseSuggest::getHandleTime, new Date())
            .eq(PurchaseSuggest::getId, id)
            .eq(PurchaseSuggest::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法受理（需为待受理）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String reason) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(PurchaseSuggest.class)
            .set(PurchaseSuggest::getStatus, 2)
            .set(PurchaseSuggest::getHandleBy, LoginHelper.getUserId())
            .set(PurchaseSuggest::getHandleTime, new Date())
            .set(PurchaseSuggest::getRejectReason, reason)
            .eq(PurchaseSuggest::getId, id)
            .eq(PurchaseSuggest::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("当前状态无法驳回（需为待受理）");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean purchased(Long id) {
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(PurchaseSuggest.class)
            .set(PurchaseSuggest::getStatus, 3)
            .set(PurchaseSuggest::getHandleTime, new Date())
            .eq(PurchaseSuggest::getId, id)
            .eq(PurchaseSuggest::getStatus, 1));
        if (rows != 1) {
            throw new ServiceException("当前状态无法标记已采购（需为已受理）");
        }
        return true;
    }

}

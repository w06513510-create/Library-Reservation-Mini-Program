package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Desk;
import org.dromara.library.domain.bo.DeskBo;
import org.dromara.library.domain.vo.DeskVo;
import org.dromara.library.mapper.DeskMapper;
import org.dromara.library.service.IDeskService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 桌子Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class DeskServiceImpl implements IDeskService {

    private final DeskMapper baseMapper;

    @Override
    public DeskVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<DeskVo> queryPageList(DeskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Desk> lqw = buildQueryWrapper(bo);
        Page<DeskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<DeskVo> queryList(DeskBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Desk> buildQueryWrapper(DeskBo bo) {
        LambdaQueryWrapper<Desk> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAreaId() != null, Desk::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getDeskNo()), Desk::getDeskNo, bo.getDeskNo());
        lqw.eq(bo.getStatus() != null, Desk::getStatus, bo.getStatus());
        lqw.orderByAsc(Desk::getSort);
        return lqw;
    }

    @Override
    public Boolean insertByBo(DeskBo bo) {
        Desk add = MapstructUtils.convert(bo, Desk.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(DeskBo bo) {
        Desk update = MapstructUtils.convert(bo, Desk.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

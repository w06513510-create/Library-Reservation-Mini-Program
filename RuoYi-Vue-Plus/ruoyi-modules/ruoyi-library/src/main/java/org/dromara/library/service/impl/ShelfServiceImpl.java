package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Shelf;
import org.dromara.library.domain.bo.ShelfBo;
import org.dromara.library.domain.vo.ShelfVo;
import org.dromara.library.mapper.ShelfMapper;
import org.dromara.library.service.IShelfService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 书架Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class ShelfServiceImpl implements IShelfService {

    private final ShelfMapper baseMapper;

    @Override
    public ShelfVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ShelfVo> queryPageList(ShelfBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Shelf> lqw = buildQueryWrapper(bo);
        Page<ShelfVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ShelfVo> queryList(ShelfBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Shelf> buildQueryWrapper(ShelfBo bo) {
        LambdaQueryWrapper<Shelf> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getLocationId() != null, Shelf::getLocationId, bo.getLocationId());
        lqw.like(StringUtils.isNotBlank(bo.getShelfNo()), Shelf::getShelfNo, bo.getShelfNo());
        lqw.eq(bo.getStatus() != null, Shelf::getStatus, bo.getStatus());
        lqw.orderByAsc(Shelf::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(ShelfBo bo) {
        Shelf add = MapstructUtils.convert(bo, Shelf.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(ShelfBo bo) {
        Shelf update = MapstructUtils.convert(bo, Shelf.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

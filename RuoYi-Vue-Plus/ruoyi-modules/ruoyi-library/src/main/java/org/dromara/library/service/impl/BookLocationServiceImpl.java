package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.BookLocation;
import org.dromara.library.domain.bo.BookLocationBo;
import org.dromara.library.domain.vo.BookLocationVo;
import org.dromara.library.mapper.BookLocationMapper;
import org.dromara.library.service.IBookLocationService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 藏地Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class BookLocationServiceImpl implements IBookLocationService {

    private final BookLocationMapper baseMapper;

    @Override
    public BookLocationVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BookLocationVo> queryPageList(BookLocationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BookLocation> lqw = buildQueryWrapper(bo);
        Page<BookLocationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BookLocationVo> queryList(BookLocationBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<BookLocation> buildQueryWrapper(BookLocationBo bo) {
        LambdaQueryWrapper<BookLocation> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getLocationName()), BookLocation::getLocationName, bo.getLocationName());
        lqw.eq(bo.getFloorId() != null, BookLocation::getFloorId, bo.getFloorId());
        lqw.eq(bo.getStatus() != null, BookLocation::getStatus, bo.getStatus());
        lqw.orderByAsc(BookLocation::getSort);
        return lqw;
    }

    @Override
    public Boolean insertByBo(BookLocationBo bo) {
        BookLocation add = MapstructUtils.convert(bo, BookLocation.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BookLocationBo bo) {
        BookLocation update = MapstructUtils.convert(bo, BookLocation.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

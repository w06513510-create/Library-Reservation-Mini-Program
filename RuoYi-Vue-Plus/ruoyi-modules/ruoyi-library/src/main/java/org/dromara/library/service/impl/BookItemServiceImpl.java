package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.BookItem;
import org.dromara.library.domain.bo.BookItemBo;
import org.dromara.library.domain.vo.BookItemVo;
import org.dromara.library.mapper.BookItemMapper;
import org.dromara.library.service.IBookItemService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 馆藏册（册/Item）Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class BookItemServiceImpl implements IBookItemService {

    private final BookItemMapper baseMapper;

    @Override
    public BookItemVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BookItemVo> queryPageList(BookItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BookItem> lqw = buildQueryWrapper(bo);
        Page<BookItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BookItemVo> queryList(BookItemBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<BookItem> buildQueryWrapper(BookItemBo bo) {
        LambdaQueryWrapper<BookItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getBookId() != null, BookItem::getBookId, bo.getBookId());
        lqw.like(StringUtils.isNotBlank(bo.getBarcode()), BookItem::getBarcode, bo.getBarcode());
        lqw.eq(bo.getLocationId() != null, BookItem::getLocationId, bo.getLocationId());
        lqw.eq(bo.getShelfId() != null, BookItem::getShelfId, bo.getShelfId());
        lqw.eq(bo.getStatus() != null, BookItem::getStatus, bo.getStatus());
        lqw.orderByAsc(BookItem::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(BookItemBo bo) {
        BookItem add = MapstructUtils.convert(bo, BookItem.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BookItemBo bo) {
        BookItem update = MapstructUtils.convert(bo, BookItem.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

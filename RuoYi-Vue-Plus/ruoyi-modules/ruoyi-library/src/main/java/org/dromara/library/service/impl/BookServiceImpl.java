package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Book;
import org.dromara.library.domain.bo.BookBo;
import org.dromara.library.domain.vo.BookVo;
import org.dromara.library.mapper.BookMapper;
import org.dromara.library.service.IBookService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 书目（种/Bib）Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements IBookService {

    private final BookMapper baseMapper;

    @Override
    public BookVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BookVo> queryPageList(BookBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Book> lqw = buildQueryWrapper(bo);
        Page<BookVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BookVo> queryList(BookBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Book> buildQueryWrapper(BookBo bo) {
        LambdaQueryWrapper<Book> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Book::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getAuthor()), Book::getAuthor, bo.getAuthor());
        lqw.like(StringUtils.isNotBlank(bo.getIsbn()), Book::getIsbn, bo.getIsbn());
        lqw.eq(bo.getStatus() != null, Book::getStatus, bo.getStatus());
        lqw.orderByAsc(Book::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(BookBo bo) {
        Book add = MapstructUtils.convert(bo, Book.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(BookBo bo) {
        Book update = MapstructUtils.convert(bo, Book.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

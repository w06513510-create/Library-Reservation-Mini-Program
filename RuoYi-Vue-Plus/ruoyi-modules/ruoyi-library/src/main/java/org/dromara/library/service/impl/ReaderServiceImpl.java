package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Reader;
import org.dromara.library.domain.bo.ReaderBo;
import org.dromara.library.domain.vo.ReaderVo;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.service.IReaderService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 读者档案Service业务层处理
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class ReaderServiceImpl implements IReaderService {

    private final ReaderMapper baseMapper;

    @Override
    public ReaderVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ReaderVo> queryPageList(ReaderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Reader> lqw = buildQueryWrapper(bo);
        Page<ReaderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<ReaderVo> queryList(ReaderBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Reader> buildQueryWrapper(ReaderBo bo) {
        LambdaQueryWrapper<Reader> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getStudentNo()), Reader::getStudentNo, bo.getStudentNo());
        lqw.like(StringUtils.isNotBlank(bo.getRealName()), Reader::getRealName, bo.getRealName());
        lqw.eq(bo.getBlacklistFlag() != null, Reader::getBlacklistFlag, bo.getBlacklistFlag());
        lqw.orderByAsc(Reader::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(ReaderBo bo) {
        Reader add = MapstructUtils.convert(bo, Reader.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(ReaderBo bo) {
        Reader update = MapstructUtils.convert(bo, Reader.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

}

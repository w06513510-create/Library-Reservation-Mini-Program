package org.dromara.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.Blacklist;
import org.dromara.library.domain.Reader;
import org.dromara.library.domain.bo.BlacklistBo;
import org.dromara.library.domain.vo.BlacklistVo;
import org.dromara.library.mapper.BlacklistMapper;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.service.IBlacklistService;
import org.dromara.library.service.ICreditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 黑名单Service实现
 *
 * @author library
 */
@RequiredArgsConstructor
@Service
public class BlacklistServiceImpl implements IBlacklistService {

    private final BlacklistMapper baseMapper;
    private final ReaderMapper readerMapper;
    private final ICreditService creditService;

    /** 解除黑名单后信用校准到的门槛分 */
    private static final int RECOVER_SCORE = 60;
    private static final long DAY_MS = 24L * 3600 * 1000;

    @Override
    public BlacklistVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BlacklistVo> queryPageList(BlacklistBo bo, PageQuery pageQuery) {
        Page<BlacklistVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<BlacklistVo> queryList(BlacklistBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Blacklist> buildQueryWrapper(BlacklistBo bo) {
        LambdaQueryWrapper<Blacklist> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getReaderId() != null, Blacklist::getReaderId, bo.getReaderId());
        lqw.eq(bo.getStatus() != null, Blacklist::getStatus, bo.getStatus());
        lqw.orderByDesc(Blacklist::getStartTime);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addToBlacklist(Long readerId, String reason, int days) {
        Long active = baseMapper.selectCount(Wrappers.<Blacklist>lambdaQuery()
            .eq(Blacklist::getReaderId, readerId).eq(Blacklist::getStatus, 0));
        if (active != null && active > 0) {
            return true; // 已在黑名单，幂等跳过
        }
        Date now = new Date();
        Date end = new Date(now.getTime() + (long) days * DAY_MS);
        Blacklist bl = new Blacklist();
        bl.setReaderId(readerId);
        bl.setReason(reason);
        bl.setStartTime(now);
        bl.setEndTime(end);
        bl.setStatus(0);
        baseMapper.insert(bl);
        readerMapper.update(null, Wrappers.lambdaUpdate(Reader.class)
            .set(Reader::getBlacklistFlag, 1).set(Reader::getBlacklistEndTime, end).set(Reader::getStatus, 1)
            .eq(Reader::getUserId, readerId));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addByBo(BlacklistBo bo) {
        int days = bo.getDays() != null ? bo.getDays() : 7;
        return addToBlacklist(bo.getReaderId(), bo.getReason() == null ? "管理员手动加入" : bo.getReason(), days);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean release(Long id, int releaseType) {
        Blacklist bl = baseMapper.selectById(id);
        if (bl == null) {
            throw new ServiceException("黑名单记录不存在");
        }
        int rows = baseMapper.update(null, Wrappers.lambdaUpdate(Blacklist.class)
            .set(Blacklist::getStatus, 1).set(Blacklist::getReleaseType, releaseType).set(Blacklist::getReleaseTime, new Date())
            .eq(Blacklist::getId, id).eq(Blacklist::getStatus, 0));
        if (rows != 1) {
            throw new ServiceException("该黑名单已解除");
        }
        // 恢复读者权限
        readerMapper.update(null, Wrappers.lambdaUpdate(Reader.class)
            .set(Reader::getBlacklistFlag, 0).set(Reader::getBlacklistEndTime, null).set(Reader::getStatus, 0)
            .eq(Reader::getUserId, bl.getReaderId()));
        // 信用校准到门槛分（赋值型，按未clamp的 raw_sum 计算）
        creditService.calibrateCredit(bl.getReaderId(), RECOVER_SCORE, "黑名单解除·校准门槛分", "blacklist", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoReleaseExpired() {
        List<Blacklist> expired = baseMapper.selectList(Wrappers.<Blacklist>lambdaQuery()
            .eq(Blacklist::getStatus, 0).lt(Blacklist::getEndTime, new Date()));
        int n = 0;
        for (Blacklist bl : expired) {
            try {
                release(bl.getId(), 1);
                n++;
            } catch (Exception ignore) {
            }
        }
        return n;
    }

}

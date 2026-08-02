package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.BlacklistBo;
import org.dromara.library.domain.vo.BlacklistVo;

import java.util.List;

/**
 * 黑名单Service（进/出黑名单 + 信用校准）
 *
 * @author library
 */
public interface IBlacklistService {

    BlacklistVo queryById(Long id);

    TableDataInfo<BlacklistVo> queryPageList(BlacklistBo bo, PageQuery pageQuery);

    List<BlacklistVo> queryList(BlacklistBo bo);

    /** 加入黑名单（已在黑名单则跳过）：暂停 days 天，读者 blacklist_flag=1 */
    Boolean addToBlacklist(Long readerId, String reason, int days);

    /** 手动加入黑名单 */
    Boolean addByBo(BlacklistBo bo);

    /** 解除黑名单：releaseType 1到期自动 2申诉通过 3手动；恢复读者权限并校准信用至门槛分 */
    Boolean release(Long id, int releaseType);

    /** 定时：扫描到期黑名单自动解除，返回解除条数 */
    int autoReleaseExpired();

}

package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.RoomReservationBo;
import org.dromara.library.domain.vo.RoomReservationVo;

import java.util.Collection;
import java.util.List;

/**
 * 研讨间预约Service接口（状态机）
 *
 * @author library
 */
public interface IRoomReservationService {

    /**
     * 查询研讨间预约
     */
    RoomReservationVo queryById(Long id);

    /**
     * 分页查询研讨间预约列表
     */
    TableDataInfo<RoomReservationVo> queryPageList(RoomReservationBo bo, PageQuery pageQuery);

    /**
     * 查询研讨间预约列表
     */
    List<RoomReservationVo> queryList(RoomReservationBo bo);

    /**
     * 新增研讨间预约（走 createReservation 逻辑）
     */
    Boolean insertByBo(RoomReservationBo bo);

    /**
     * 修改研讨间预约
     */
    Boolean updateByBo(RoomReservationBo bo);

    /**
     * 校验并批量删除研讨间预约
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 预约研讨间（悲观行锁 + 时段重叠校验 + 按需审批初始状态）
     */
    Boolean createReservation(RoomReservationBo bo);

    /**
     * 审批通过 0→1
     */
    Boolean approve(Long id);

    /**
     * 审批驳回 0→5
     */
    Boolean reject(Long id, String reason);

    /**
     * 签到 1→2
     */
    Boolean checkIn(Long id);

    /**
     * 完成 2→3
     */
    Boolean complete(Long id);

    /**
     * 取消 {0,1}→4
     */
    Boolean cancel(Long id);

}

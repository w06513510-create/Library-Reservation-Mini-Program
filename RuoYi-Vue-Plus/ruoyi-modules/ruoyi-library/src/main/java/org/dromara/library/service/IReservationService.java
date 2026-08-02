package org.dromara.library.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.bo.ReservationBo;
import org.dromara.library.domain.vo.ReservationVo;
import org.dromara.library.domain.vo.SeatStatusVo;

import java.util.Date;
import java.util.List;

/**
 * 座位预约状态机Service
 * 状态：0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约
 *
 * @author library
 */
public interface IReservationService {

    ReservationVo queryById(Long id);

    TableDataInfo<ReservationVo> queryPageList(ReservationBo bo, PageQuery pageQuery);

    List<ReservationVo> queryList(ReservationBo bo);

    /** 约座：校验黑名单/信用阈值 + 座位可用 + 座位资源不变式（同座同时段唯一有效占用），创建 status=0 */
    Boolean createReservation(ReservationBo bo);

    /** 签到 CAS 0→1 */
    Boolean checkIn(Long id);

    /** 暂离 CAS 1→2 */
    Boolean away(Long id);

    /** 返回落座 CAS 2→1 */
    Boolean back(Long id);

    /** 退座/签退 CAS {1,2}→3，履约加分 +1 */
    Boolean leave(Long id);

    /** 取消预约 CAS 0→4 */
    Boolean cancel(Long id);

    /** 续座：{1,2} 状态延长 endTime（校验后续时段空闲） */
    Boolean extend(Long id, Date newEndTime);

    /** 管理员强制释放 {0,1,2}→3 */
    Boolean forceRelease(Long id, String reason);

    /** 平面图选座：某楼层各座位坐标 + 所选时段占用状态（亮点①；时间用字符串比较，绕开时区偏移） */
    List<SeatStatusVo> seatStatus(Long floorId, String start, String end);

}

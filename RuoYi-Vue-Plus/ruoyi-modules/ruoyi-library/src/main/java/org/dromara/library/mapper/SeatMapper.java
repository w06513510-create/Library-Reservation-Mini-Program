package org.dromara.library.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.library.domain.Seat;
import org.dromara.library.domain.vo.SeatVo;

/**
 * 座位Mapper接口
 *
 * @author library
 */
public interface SeatMapper extends BaseMapperPlus<Seat, SeatVo> {

    /**
     * 悲观行锁：约座时先锁住该座位行，串行化同座并发预约，杜绝同座同时段被重复占用（座位资源不变式）
     */
    @Select("SELECT * FROM biz_seat WHERE id = #{id} FOR UPDATE")
    Seat selectForUpdate(@Param("id") Long id);

}

package org.dromara.library.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.library.domain.Room;
import org.dromara.library.domain.vo.RoomVo;

/**
 * 研讨间Mapper接口
 *
 * @author library
 */
public interface RoomMapper extends BaseMapperPlus<Room, RoomVo> {

    /**
     * 悲观行锁：预约研讨间时先锁住该研讨间行，串行化同研讨间并发预约，杜绝同研讨间同时段被重复占用
     */
    @Select("SELECT * FROM biz_room WHERE id = #{id} FOR UPDATE")
    Room selectForUpdate(@Param("id") Long id);

}

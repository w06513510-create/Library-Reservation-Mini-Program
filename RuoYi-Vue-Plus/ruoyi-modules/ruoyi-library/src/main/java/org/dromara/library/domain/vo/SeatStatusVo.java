package org.dromara.library.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 平面图选座 — 座位实时状态视图对象（亮点①）
 * 用于楼层平面图渲染：坐标 + 该时段占用与否。
 *
 * @author library
 */
@Data
public class SeatStatusVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 座位ID */
    private Long id;

    /** 座位编号 */
    private String seatNo;

    /** 区域ID */
    private Long areaId;

    /** 区域名称 */
    private String areaName;

    /** 座位类型：0普通 1靠窗 2沙发 3单间 */
    private Integer seatType;

    /** 有无插座：0无 1有 */
    private Integer hasPower;

    /** 平面图X坐标 */
    private Integer posX;

    /** 平面图Y坐标 */
    private Integer posY;

    /** 座位基础状态：0正常 1停用 */
    private Integer seatStatus;

    /** 所选时段是否被占用（待签到/使用中/暂离中） */
    private Boolean occupied;

}

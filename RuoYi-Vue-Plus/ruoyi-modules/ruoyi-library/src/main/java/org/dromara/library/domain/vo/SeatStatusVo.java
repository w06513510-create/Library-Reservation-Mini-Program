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

    /** 平面图X坐标（座位绝对坐标） */
    private Integer posX;

    /** 平面图Y坐标（座位绝对坐标） */
    private Integer posY;

    /** 座位基础状态：0正常 1停用 */
    private Integer seatStatus;

    /** 所选时段是否被占用（待签到/使用中/暂离中） */
    private Boolean occupied;

    // ===== 所属桌子（工位组）信息：供平面图「一桌展开多座」成组渲染 =====

    /** 所属桌子ID */
    private Long deskId;

    /** 桌号（如 D01） */
    private String deskNo;

    /** 桌子容量 */
    private Integer capacity;

    /** 桌形：0矩形 1圆 2吧台 */
    private Integer shape;

    /** 桌子平面图X坐标（左上角绝对坐标） */
    private Integer deskPosX;

    /** 桌子平面图Y坐标（左上角绝对坐标） */
    private Integer deskPosY;

    /** 桌子宽度（px） */
    private Integer deskWidth;

    /** 桌子高度（px） */
    private Integer deskHeight;

    /** 桌子旋转角度（度） */
    private Integer deskRotation;

    /** 座位相对桌子左上角X偏移（px） */
    private Integer offsetX;

    /** 座位相对桌子左上角Y偏移（px） */
    private Integer offsetY;

}

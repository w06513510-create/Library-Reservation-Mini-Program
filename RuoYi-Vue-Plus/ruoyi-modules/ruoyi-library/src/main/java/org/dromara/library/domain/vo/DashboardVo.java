package org.dromara.library.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 数据可视化大屏 概览指标（亮点③）
 *
 * @author library
 */
@Data
public class DashboardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // —— 座位/空间线 ——
    private Long seatTotal;
    private Long seatOccupied;
    private Long seatDisabled;
    private Long seatFree;
    private Long roomTotal;
    private Long reservationActive;

    // —— 图书/流通线 ——
    private Long bookTitles;
    private Long itemTotal;
    private Long itemAvail;
    private Long itemBorrowed;
    private Long itemOnHold;
    private Long itemWithdrawn;
    private Long loanOnLoan;
    private Long loanOverdue;
    private Long holdInTransit;
    private Long todayBorrow;
    private Long todayReturn;

    // —— 读者/信用线 ——
    private Long readerTotal;
    private Long blacklistCount;

    // —— 分布图（饼/柱） ——
    /** 座位状态占比：空闲/占用/停用 */
    private List<NameValueVo> seatStatus;
    /** 馆藏状态占比：可借/借出/在预约架/在编/遗失损坏/已注销 */
    private List<NameValueVo> itemStatus;
    /** 违约类型构成 */
    private List<NameValueVo> violationTypes;
    /** 信用分分布（分段人数） */
    private List<NameValueVo> creditDist;
    /** 座位预约状态构成 */
    private List<NameValueVo> reservationStatus;

}

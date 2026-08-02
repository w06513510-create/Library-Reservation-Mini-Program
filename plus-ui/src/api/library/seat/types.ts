export interface SeatVO {
  /** 座位ID */
  id: string | number;
  /** 所属区域ID */
  areaId: string | number;
  /** 座位编号 */
  seatNo: string;
  /** 座位类型：0普通 1靠窗 2沙发 3单间 */
  seatType: number;
  /** 有无插座：0无 1有 */
  hasPower: number;
  /** 平面图X坐标 */
  posX: number;
  /** 平面图Y坐标 */
  posY: number;
  /** 桌面二维码标识 */
  qrCode: string;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface SeatForm extends BaseEntity {
  id?: string | number;
  areaId?: string | number;
  seatNo?: string;
  seatType?: number;
  hasPower?: number;
  posX?: number;
  posY?: number;
  qrCode?: string;
  status?: number;
}

export interface SeatQuery extends PageQuery {
  areaId?: string | number;
  seatNo?: string;
  status?: number;
}

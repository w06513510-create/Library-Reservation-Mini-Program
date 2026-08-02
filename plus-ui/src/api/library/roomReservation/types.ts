export interface RoomReservationVO {
  /** 研讨间预约ID */
  id: string | number;
  /** 预约读者ID（app_user） */
  readerId: string | number;
  /** 研讨间ID */
  roomId: string | number;
  /** 预约日期 */
  reserveDate: string;
  /** 时段开始 */
  startTime: string;
  /** 时段结束 */
  endTime: string;
  /** 使用人数 */
  userCount: number;
  /** 状态：0待审批 1已通过待使用 2使用中 3已完成 4已取消 5已驳回 6已违约 */
  status: number;
  /** 签到时间 */
  checkInTime?: string;
  /** 审批人 */
  approveBy?: string | number;
  /** 审批时间 */
  approveTime?: string;
  /** 驳回原因 */
  rejectReason?: string;
}

export interface RoomReservationForm extends BaseEntity {
  id?: string | number;
  readerId?: string | number;
  roomId?: string | number;
  reserveDate?: string;
  startTime?: string;
  endTime?: string;
  userCount?: number;
  status?: number;
}

export interface RoomReservationQuery extends PageQuery {
  readerId?: string | number;
  roomId?: string | number;
  status?: number;
  reserveDate?: string;
}

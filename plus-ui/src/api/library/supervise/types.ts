export interface SuperviseVO {
  /** 监督ID */
  id: string | number;
  /** 被监督的座位预约单ID */
  reservationId: string | number;
  /** 座位ID */
  seatId: string | number;
  /** 举报读者ID（app_user） */
  reporterId: string | number;
  /** 举报时间 */
  reportTime: string;
  /** 落座截止时间 */
  deadline: string;
  /** 状态：0进行中 1已解除已落座 2超时释放 */
  status: number;
  /** 解除时间 */
  resolveTime: string;
  /** 创建时间 */
  createTime: string;
}

export interface SuperviseForm extends BaseEntity {
  id?: string | number;
  reservationId?: string | number;
  reporterId?: string | number;
}

export interface SuperviseQuery extends PageQuery {
  reservationId?: string | number;
  reporterId?: string | number;
  status?: number;
}

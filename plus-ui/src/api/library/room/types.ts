export interface RoomVO {
  /** 研讨间ID */
  id: string | number;
  /** 所属楼层ID */
  floorId: string | number;
  /** 研讨间名称/编号 */
  roomName: string;
  /** 容纳人数 */
  capacity: number;
  /** 预约最少人数 */
  minUsers: number;
  /** 是否需审批：0否 1是 */
  needApprove: number;
  /** 是否需签到：0否 1是 */
  needCheckin: number;
  /** 平面图X坐标 */
  posX: number;
  /** 平面图Y坐标 */
  posY: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface RoomForm extends BaseEntity {
  id?: string | number;
  floorId?: string | number;
  roomName?: string;
  capacity?: number;
  minUsers?: number;
  needApprove?: number;
  needCheckin?: number;
  posX?: number;
  posY?: number;
  status?: number;
}

export interface RoomQuery extends PageQuery {
  floorId?: string | number;
  roomName?: string;
  status?: number;
}

export interface DeskVO {
  /** 桌子ID */
  id: string | number;
  /** 所属区域ID */
  areaId: string | number;
  /** 桌号 */
  deskNo: string;
  /** 容量：1/2/4/6 */
  capacity: number;
  /** 桌形：0矩形 1圆 2吧台 */
  shape: number;
  /** X坐标 */
  posX: number;
  /** Y坐标 */
  posY: number;
  /** 宽度 */
  width: number;
  /** 高度 */
  height: number;
  /** 旋转角度 */
  rotation: number;
  /** 状态：0正常 1停用 */
  status: number;
  /** 排序 */
  sort: number;
}

export interface DeskForm extends BaseEntity {
  id?: string | number;
  areaId?: string | number;
  deskNo?: string;
  capacity?: number;
  shape?: number;
  posX?: number;
  posY?: number;
  width?: number;
  height?: number;
  rotation?: number;
  status?: number;
  sort?: number;
}

export interface DeskQuery extends PageQuery {
  areaId?: string | number;
  deskNo?: string;
  status?: number;
}

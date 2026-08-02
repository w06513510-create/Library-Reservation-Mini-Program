export interface AreaVO {
  /** 区域ID */
  id: string | number;
  /** 所属楼层ID */
  floorId: string | number;
  /** 区域名称 */
  areaName: string;
  /** 区域类型：0自习阅览 1研讨区 2其它 */
  areaType: number;
  /** 排序 */
  sort: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface AreaForm extends BaseEntity {
  id?: string | number;
  floorId?: string | number;
  areaName?: string;
  areaType?: number;
  sort?: number;
  status?: number;
}

export interface AreaQuery extends PageQuery {
  floorId?: string | number;
  areaName?: string;
  areaType?: number;
  status?: number;
}

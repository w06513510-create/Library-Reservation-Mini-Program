export interface LocationVO {
  /** 藏地ID */
  id: string | number;
  /** 藏地名称 */
  locationName: string;
  /** 所在楼层ID */
  floorId: string | number;
  /** 排序 */
  sort: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface LocationForm extends BaseEntity {
  id?: string | number;
  locationName?: string;
  floorId?: string | number;
  sort?: number;
  status?: number;
}

export interface LocationQuery extends PageQuery {
  locationName?: string;
  floorId?: string | number;
  status?: number;
}

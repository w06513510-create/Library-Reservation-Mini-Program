export interface FloorVO {
  /** 楼层ID */
  id: string | number;
  /** 所属场馆ID */
  venueId: string | number;
  /** 楼层名称 */
  floorName: string;
  /** 楼层号 */
  floorNo: number;
  /** 楼层平面图底图URL */
  floorPlanUrl: string;
  /** 排序 */
  sort: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface FloorForm extends BaseEntity {
  id?: string | number;
  venueId?: string | number;
  floorName?: string;
  floorNo?: number;
  floorPlanUrl?: string;
  sort?: number;
  status?: number;
}

export interface FloorQuery extends PageQuery {
  venueId?: string | number;
  floorName?: string;
  status?: number;
}

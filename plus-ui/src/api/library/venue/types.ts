export interface VenueVO {
  /** 场馆ID */
  id: string | number;
  /** 场馆名称 */
  venueName: string;
  /** 地址 */
  address: string;
  /** 开馆时间 */
  openTime: string;
  /** 闭馆时间 */
  closeTime: string;
  /** 排序 */
  sort: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface VenueForm extends BaseEntity {
  id?: string | number;
  venueName?: string;
  address?: string;
  openTime?: string;
  closeTime?: string;
  sort?: number;
  status?: number;
}

export interface VenueQuery extends PageQuery {
  venueName?: string;
  status?: number;
}

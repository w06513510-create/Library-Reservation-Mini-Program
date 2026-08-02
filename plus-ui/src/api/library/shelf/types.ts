export interface ShelfVO {
  /** 书架ID */
  id: string | number;
  /** 所属藏地ID */
  locationId: string | number;
  /** 架号 */
  shelfNo: string;
  /** 索书号起 */
  callNoStart: string;
  /** 索书号止 */
  callNoEnd: string;
  /** 平面图X坐标 */
  posX: number;
  /** 平面图Y坐标 */
  posY: number;
  /** 状态：0正常 1停用 */
  status: number;
}

export interface ShelfForm extends BaseEntity {
  id?: string | number;
  locationId?: string | number;
  shelfNo?: string;
  callNoStart?: string;
  callNoEnd?: string;
  posX?: number;
  posY?: number;
  status?: number;
}

export interface ShelfQuery extends PageQuery {
  locationId?: string | number;
  shelfNo?: string;
  status?: number;
}

export interface BookItemVO {
  /** 馆藏册ID */
  id: string | number;
  /** 所属书目ID */
  bookId: string | number;
  /** 条码 */
  barcode: string;
  /** 索书号 */
  callNo: string;
  /** 藏地ID */
  locationId: string | number;
  /** 书架ID */
  shelfId: string | number;
  /** 状态：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销 */
  status: number;
  /** 注销类型：1剔旧 2报损 3遗失核销 */
  withdrawType: number;
  /** 注销原因 */
  withdrawReason: string;
  /** 注销时间 */
  withdrawTime: string;
}

export interface BookItemForm extends BaseEntity {
  id?: string | number;
  bookId?: string | number;
  barcode?: string;
  callNo?: string;
  locationId?: string | number;
  shelfId?: string | number;
  status?: number;
  withdrawType?: number;
  withdrawReason?: string;
  withdrawTime?: string;
}

export interface BookItemQuery extends PageQuery {
  bookId?: string | number;
  barcode?: string;
  status?: number;
}

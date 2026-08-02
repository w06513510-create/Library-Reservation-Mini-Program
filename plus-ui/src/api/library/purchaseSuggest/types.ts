export interface PurchaseSuggestVO {
  /** 荐购ID */
  id: string | number;
  /** 荐购读者（app_user） */
  readerId: number;
  /** 书名 */
  title: string;
  /** 著者 */
  author: string;
  /** ISBN */
  isbn: string;
  /** 荐购理由 */
  reason: string;
  /** 状态：0待受理 1已受理转采购 2已驳回 3已采购 */
  status: number;
  /** 处理人（sys_user） */
  handleBy: number;
  /** 处理时间 */
  handleTime: string;
  /** 驳回原因 */
  rejectReason: string;
  /** 创建时间 */
  createTime: string;
}

export interface PurchaseSuggestForm extends BaseEntity {
  id?: string | number;
  readerId?: number;
  title?: string;
  author?: string;
  isbn?: string;
  reason?: string;
  status?: number;
}

export interface PurchaseSuggestQuery extends PageQuery {
  readerId?: number;
  title?: string;
  status?: number;
}

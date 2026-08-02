export interface BookVO {
  /** 书目ID */
  id: string | number;
  /** ISBN */
  isbn: string;
  /** 题名 */
  title: string;
  /** 著者 */
  author: string;
  /** 出版社 */
  publisher: string;
  /** 出版日期 */
  publishDate: string;
  /** 中图法分类号 */
  clcNo: string;
  /** 索书号 */
  callNo: string;
  /** 封面图URL */
  coverUrl: string;
  /** 内容简介 */
  summary: string;
  /** 定价 */
  price: number;
  /** 复本总数 */
  totalQty: number;
  /** 当前可借册数 */
  availQty: number;
  /** 状态：0在编 1已上架(可借) 2已下架 */
  status: number;
}

export interface BookForm extends BaseEntity {
  id?: string | number;
  isbn?: string;
  title?: string;
  author?: string;
  publisher?: string;
  publishDate?: string;
  clcNo?: string;
  callNo?: string;
  coverUrl?: string;
  summary?: string;
  price?: number;
  totalQty?: number;
  availQty?: number;
  status?: number;
}

export interface BookQuery extends PageQuery {
  title?: string;
  author?: string;
  isbn?: string;
  status?: number;
}

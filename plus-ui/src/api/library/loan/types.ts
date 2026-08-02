export interface LoanVO {
  id: string | number;
  readerId: string | number;
  itemId: string | number;
  bookId: string | number;
  borrowTime: string;
  dueTime: string;
  renewCount: number;
  returnTime?: string;
  status: number;
  overdueFlag?: number;
  recallFlag?: number;
}

export interface LoanForm extends BaseEntity {
  id?: string | number;
  readerId?: string | number;
  itemId?: string | number;
}

export interface LoanQuery extends PageQuery {
  readerId?: string | number;
  bookId?: string | number;
  status?: number;
}

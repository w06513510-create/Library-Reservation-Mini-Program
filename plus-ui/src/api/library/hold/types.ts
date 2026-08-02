export interface HoldVO {
  id: string | number;
  readerId: string | number;
  bookId: string | number;
  itemId?: string | number;
  queueNo: number;
  status: number;
  holdTime: string;
  readyTime?: string;
  holdDeadline?: string;
}

export interface HoldForm extends BaseEntity {
  id?: string | number;
  readerId?: string | number;
  bookId?: string | number;
}

export interface HoldQuery extends PageQuery {
  readerId?: string | number;
  bookId?: string | number;
  status?: number;
}

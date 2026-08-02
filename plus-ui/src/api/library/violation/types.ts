export interface ViolationVO {
  id: string | number;
  readerId: string | number;
  violationType: number;
  deductScore?: number;
  occurTime: string;
  source: number;
  status: number;
  remark?: string;
}

export interface ViolationForm extends BaseEntity {
  readerId?: string | number;
  violationType?: number;
  deductScore?: number;
}

export interface ViolationQuery extends PageQuery {
  readerId?: string | number;
  violationType?: number;
  status?: number;
}

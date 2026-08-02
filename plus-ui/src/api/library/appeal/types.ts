export interface AppealVO {
  id: string | number;
  violationId: string | number;
  readerId: string | number;
  reason: string;
  status: number;
  auditRemark?: string;
  auditTime?: string;
  createTime?: string;
}

export interface AppealForm extends BaseEntity {
  violationId?: string | number;
  readerId?: string | number;
  reason?: string;
}

export interface AppealQuery extends PageQuery {
  readerId?: string | number;
  status?: number;
}

export interface BlacklistVO {
  id: string | number;
  readerId: string | number;
  reason: string;
  startTime: string;
  endTime: string;
  status: number;
  createTime?: string;
}

export interface BlacklistForm extends BaseEntity {
  readerId?: string | number;
  reason?: string;
  days?: number;
}

export interface BlacklistQuery extends PageQuery {
  readerId?: string | number;
  status?: number;
}

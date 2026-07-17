export interface NotificationVO extends BaseEntity {
  id: number;
  receiverId: number;
  title: string;
  content: string;
  bizType: string;
  bizId: number;
  isRead: number;
  readTime: string;
}

export interface NotificationQuery extends PageQuery {
  receiverId?: number;
  bizType?: string;
  isRead?: number;
  title?: string;
}

export interface NotificationSendForm {
  receiverId: number | undefined;
  title: string;
  content: string;
  bizType: string;
  bizId: number | undefined;
}

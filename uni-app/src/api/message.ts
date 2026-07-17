import { request } from '../utils/request';

/** 站内系统通知（对应后端 AppNotificationVo） */
export interface NotificationVo {
  id: number;
  receiverId: number;
  title: string;
  content?: string;
  bizType?: string;
  bizId?: number;
  isRead: number;
  readTime?: string;
  createTime?: string;
}

/** 会话（对应后端 ConversationVo） */
export interface ConversationVo {
  peerId: number;
  peerNickname: string;
  peerAvatar?: string;
  lastContent?: string;
  lastTime?: string;
  unread: number;
}

/** 私信（对应后端 AppMessageVo） */
export interface MessageVo {
  id: number;
  fromId: number;
  toId: number;
  content: string;
  isRead: number;
  createTime?: string;
}

/* ============ 通知 ============ */

/** 我的通知分页 */
export function apiNoticeList(params: Record<string, any>) {
  return request<NotificationVo[]>({ url: '/app/notice/list', params });
}

/** 我的未读通知数 */
export function apiNoticeUnread() {
  return request<number>({ url: '/app/notice/unreadCount' });
}

/** 标记单条通知已读 */
export function apiNoticeRead(id: number) {
  return request<void>({ url: `/app/notice/read/${id}`, method: 'PUT' });
}

/** 全部通知已读 */
export function apiNoticeReadAll() {
  return request<void>({ url: '/app/notice/readAll', method: 'PUT' });
}

/* ============ 私信 ============ */

/** 我的会话列表 */
export function apiConversations() {
  return request<ConversationVo[]>({ url: '/app/message/conversations' });
}

/** 与某对端的消息分页（倒序：page1=最新一页） */
export function apiChat(params: Record<string, any>) {
  return request<MessageVo[]>({ url: '/app/message/chat', params });
}

/** 发送私信 */
export function apiSendMessage(toId: number, content: string) {
  return request<number>({ url: '/app/message/send', method: 'POST', data: { toId, content } });
}

/** 我的未读私信总数 */
export function apiMessageUnread() {
  return request<number>({ url: '/app/message/unreadCount' });
}

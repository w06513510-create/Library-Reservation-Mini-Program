import { request } from '../utils/request';
import type { AppUserVo } from './auth';

export type InteractionAction = 'favorite' | 'like' | 'follow';

/** 开关互动，返回 { active, count } */
export function apiToggle(action: InteractionAction, bizType: string, bizId: number) {
  return request<{ active: boolean; count: number }>({
    url: '/app/interaction/toggle',
    method: 'POST',
    data: { action, bizType, bizId }
  });
}

/** 我是否操作过 */
export function apiHas(action: InteractionAction, bizType: string, bizId: number) {
  return request<boolean>({ url: '/app/interaction/has', params: { action, bizType, bizId } });
}

/** 该对象计数 */
export function apiCount(action: InteractionAction, bizType: string, bizId: number) {
  return request<number>({ url: '/app/interaction/count', params: { action, bizType, bizId } });
}

/** 我的收藏/点赞/关注 对象ID 分页（返回 rows: bizId[]） */
export function apiMyBizIds(action: InteractionAction, bizType: string, pageNum = 1, pageSize = 20) {
  return request<number[]>({ url: '/app/interaction/my/page', params: { action, bizType, pageNum, pageSize } });
}

/** 我关注的人 */
export function apiFollowing(pageNum = 1, pageSize = 20) {
  return request<AppUserVo[]>({ url: '/app/interaction/following/page', params: { pageNum, pageSize } });
}

/** 关注我的人(粉丝) */
export function apiFollowers(pageNum = 1, pageSize = 20) {
  return request<AppUserVo[]>({ url: '/app/interaction/followers/page', params: { pageNum, pageSize } });
}

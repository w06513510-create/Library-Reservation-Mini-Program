import { request } from '../utils/request';

/** C端用户视图对象（与后端 AppUserVo 对应，已脱敏） */
export interface AppUserVo {
  id: number;
  phone: string;
  nickname: string;
  avatar?: string;
  gender?: number;
  status?: number;
  registerTime?: string;
  lastLoginTime?: string;
}

/** 登录：手机号 + 密码，返回 { token } */
export function apiLogin(phone: string, password: string) {
  return request<{ token: string }>({
    url: '/app/auth/login',
    method: 'POST',
    data: { phone, password },
    isToken: false
  });
}

/** 注册：手机号 + 密码(+ 可选昵称) */
export function apiRegister(phone: string, password: string, nickname?: string) {
  return request<void>({
    url: '/app/auth/register',
    method: 'POST',
    data: { phone, password, nickname },
    isToken: false
  });
}

/** 当前登录用户信息 */
export function apiGetInfo() {
  return request<AppUserVo>({ url: '/app/auth/getInfo' });
}

/** 退出登录 */
export function apiLogout() {
  return request<void>({ url: '/app/auth/logout', method: 'POST' });
}

/** 更新昵称 */
export function apiUpdateNickname(nickname: string) {
  return request<void>({ url: '/app/auth/nickname', method: 'PUT', params: { nickname } });
}

/** 更新头像（url 来自 upload.ts 上传返回） */
export function apiUpdateAvatar(url: string) {
  return request<void>({ url: '/app/auth/avatar', method: 'PUT', params: { url } });
}

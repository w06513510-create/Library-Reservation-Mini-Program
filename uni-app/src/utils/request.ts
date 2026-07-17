import { BASE_URL, CLIENT_ID, TOKEN_KEY } from '../config';

export function getToken(): string {
  return uni.getStorageSync(TOKEN_KEY) || '';
}
export function setToken(t: string) {
  uni.setStorageSync(TOKEN_KEY, t);
}
export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY);
}

export interface Resp<T = any> {
  code: number;
  msg: string;
  data?: T;
  rows?: T;
  total?: number;
}

interface Options {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: any;
  /** GET 查询参数（会拼到 url） */
  params?: Record<string, any>;
  /** 是否带 token，默认 true */
  isToken?: boolean;
  /** 静默模式：失败不弹 toast，默认 false */
  silent?: boolean;
}

// 401 重登防抖，避免并发请求同时触发多次跳转
let relogining = false;

function handle401() {
  clearToken();
  if (relogining) return;
  relogining = true;
  uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/login' });
    relogining = false;
  }, 800);
}

function toQuery(params?: Record<string, any>): string {
  if (!params) return '';
  const arr = Object.keys(params)
    .filter((k) => params[k] !== undefined && params[k] !== null && params[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`);
  return arr.length ? '?' + arr.join('&') : '';
}

/** 统一请求：注入 clientid + Bearer token；code===200 resolve，401 自动重登，其余弹 msg 并 reject。 */
export function request<T = any>(opts: Options): Promise<Resp<T>> {
  const header: Record<string, string> = {
    'Content-Type': 'application/json',
    'Content-Language': 'zh_CN',
    clientid: CLIENT_ID
  };
  if (opts.isToken !== false) {
    const token = getToken();
    if (token) header['Authorization'] = 'Bearer ' + token;
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + opts.url + toQuery(opts.params),
      method: opts.method || 'GET',
      data: opts.data || {},
      header,
      success: (res: any) => {
        const body = res.data as Resp<T>;
        if (res.statusCode === 200 && body && body.code === 200) {
          resolve(body);
        } else if (res.statusCode === 401 || (body && body.code === 401)) {
          handle401();
          reject(body || { code: 401, msg: '未登录' });
        } else {
          if (!opts.silent) {
            uni.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' });
          }
          reject(body);
        }
      },
      fail: (err) => {
        if (!opts.silent) {
          uni.showToast({ title: '网络异常', icon: 'none' });
        }
        reject(err);
      }
    });
  });
}

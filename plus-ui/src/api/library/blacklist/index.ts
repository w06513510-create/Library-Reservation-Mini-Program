import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BlacklistVO, BlacklistForm, BlacklistQuery } from '@/api/library/blacklist/types';

/** 查询黑名单列表 */
export const listBlacklist = (query?: BlacklistQuery): AxiosPromise<BlacklistVO[]> => {
  return request({ url: '/library/blacklist/list', method: 'get', params: query });
};

/** 加入黑名单 */
export const addBlacklist = (data: BlacklistForm) => {
  return request({ url: '/library/blacklist', method: 'post', data: data });
};

/** 解除黑名单 */
export const releaseBlacklist = (id: string | number) => {
  return request({ url: '/library/blacklist/release/' + id, method: 'put' });
};

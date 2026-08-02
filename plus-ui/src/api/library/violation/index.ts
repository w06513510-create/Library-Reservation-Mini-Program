import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ViolationVO, ViolationForm, ViolationQuery } from '@/api/library/violation/types';

/** 查询违约记录列表 */
export const listViolation = (query?: ViolationQuery): AxiosPromise<ViolationVO[]> => {
  return request({ url: '/library/violation/list', method: 'get', params: query });
};

/** 查询违约记录详细 */
export const getViolation = (id: string | number): AxiosPromise<ViolationVO> => {
  return request({ url: '/library/violation/' + id, method: 'get' });
};

/** 登记违约 */
export const addViolation = (data: ViolationForm) => {
  return request({ url: '/library/violation', method: 'post', data: data });
};

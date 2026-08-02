import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { HoldVO, HoldForm, HoldQuery } from '@/api/library/hold/types';

export const listHold = (query?: HoldQuery): AxiosPromise<HoldVO[]> => {
  return request({ url: '/library/hold/list', method: 'get', params: query });
};
export const getHold = (id: string | number): AxiosPromise<HoldVO> => {
  return request({ url: '/library/hold/' + id, method: 'get' });
};
/** 预约（排队） */
export const addHold = (data: HoldForm) => request({ url: '/library/hold', method: 'post', data });
export const pickupHold = (id: string | number) => request({ url: '/library/hold/pickup/' + id, method: 'put' });
export const cancelHold = (id: string | number) => request({ url: '/library/hold/cancel/' + id, method: 'put' });

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ReservationVO, ReservationForm, ReservationQuery } from '@/api/library/reservation/types';

/** 预约总览 */
export const listReservation = (query?: ReservationQuery): AxiosPromise<ReservationVO[]> => {
  return request({ url: '/library/reservation/list', method: 'get', params: query });
};

/** 预约详情 */
export const getReservation = (id: string | number): AxiosPromise<ReservationVO> => {
  return request({ url: '/library/reservation/' + id, method: 'get' });
};

/** 约座 */
export const addReservation = (data: ReservationForm) => {
  return request({ url: '/library/reservation', method: 'post', data: data });
};

/** 状态机动作：签到/暂离/返回/退座/取消/强制释放 */
export const checkInReservation = (id: string | number) => request({ url: '/library/reservation/checkIn/' + id, method: 'put' });
export const awayReservation = (id: string | number) => request({ url: '/library/reservation/away/' + id, method: 'put' });
export const backReservation = (id: string | number) => request({ url: '/library/reservation/back/' + id, method: 'put' });
export const leaveReservation = (id: string | number) => request({ url: '/library/reservation/leave/' + id, method: 'put' });
export const cancelReservation = (id: string | number) => request({ url: '/library/reservation/cancel/' + id, method: 'put' });
export const forceReleaseReservation = (id: string | number, reason?: string) =>
  request({ url: '/library/reservation/forceRelease/' + id, method: 'put', params: { reason } });
export const extendReservation = (id: string | number, newEndTime: string) =>
  request({ url: '/library/reservation/extend/' + id, method: 'put', params: { newEndTime } });

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { RoomReservationVO, RoomReservationForm, RoomReservationQuery } from '@/api/library/roomReservation/types';

/** 查询研讨间预约列表 */
export const listRoomReservation = (query?: RoomReservationQuery): AxiosPromise<RoomReservationVO[]> => {
  return request({
    url: '/library/roomReservation/list',
    method: 'get',
    params: query
  });
};

/** 查询研讨间预约详细 */
export const getRoomReservation = (id: string | number): AxiosPromise<RoomReservationVO> => {
  return request({
    url: '/library/roomReservation/' + id,
    method: 'get'
  });
};

/** 新增研讨间预约 */
export const addRoomReservation = (data: RoomReservationForm) => {
  return request({
    url: '/library/roomReservation',
    method: 'post',
    data: data
  });
};

/** 修改研讨间预约 */
export const updateRoomReservation = (data: RoomReservationForm) => {
  return request({
    url: '/library/roomReservation',
    method: 'put',
    data: data
  });
};

/** 删除研讨间预约 */
export const delRoomReservation = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/roomReservation/' + id,
    method: 'delete'
  });
};

/** 审批通过 0→1 */
export const approveRoomReservation = (id: string | number) => {
  return request({ url: '/library/roomReservation/approve/' + id, method: 'post' });
};

/** 审批驳回 0→5 */
export const rejectRoomReservation = (id: string | number, reason: string) => {
  return request({ url: '/library/roomReservation/reject/' + id, method: 'post', params: { reason } });
};

/** 签到 1→2 */
export const checkInRoomReservation = (id: string | number) => {
  return request({ url: '/library/roomReservation/checkIn/' + id, method: 'post' });
};

/** 完成 2→3 */
export const completeRoomReservation = (id: string | number) => {
  return request({ url: '/library/roomReservation/complete/' + id, method: 'post' });
};

/** 取消 {0,1}→4 */
export const cancelRoomReservation = (id: string | number) => {
  return request({ url: '/library/roomReservation/cancel/' + id, method: 'post' });
};

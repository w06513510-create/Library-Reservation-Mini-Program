import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SeatVO, SeatForm, SeatQuery } from '@/api/library/seat/types';

/** 查询座位列表 */
export const listSeat = (query?: SeatQuery): AxiosPromise<SeatVO[]> => {
  return request({
    url: '/library/seat/list',
    method: 'get',
    params: query
  });
};

/** 查询座位详细 */
export const getSeat = (id: string | number): AxiosPromise<SeatVO> => {
  return request({
    url: '/library/seat/' + id,
    method: 'get'
  });
};

/** 新增座位 */
export const addSeat = (data: SeatForm) => {
  return request({
    url: '/library/seat',
    method: 'post',
    data: data
  });
};

/** 修改座位 */
export const updateSeat = (data: SeatForm) => {
  return request({
    url: '/library/seat',
    method: 'put',
    data: data
  });
};

/** 删除座位 */
export const delSeat = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/seat/' + id,
    method: 'delete'
  });
};

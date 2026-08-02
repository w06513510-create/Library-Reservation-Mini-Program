import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { RoomVO, RoomForm, RoomQuery } from '@/api/library/room/types';

/** 查询研讨间列表 */
export const listRoom = (query?: RoomQuery): AxiosPromise<RoomVO[]> => {
  return request({
    url: '/library/room/list',
    method: 'get',
    params: query
  });
};

/** 查询研讨间详细 */
export const getRoom = (id: string | number): AxiosPromise<RoomVO> => {
  return request({
    url: '/library/room/' + id,
    method: 'get'
  });
};

/** 新增研讨间 */
export const addRoom = (data: RoomForm) => {
  return request({
    url: '/library/room',
    method: 'post',
    data: data
  });
};

/** 修改研讨间 */
export const updateRoom = (data: RoomForm) => {
  return request({
    url: '/library/room',
    method: 'put',
    data: data
  });
};

/** 删除研讨间 */
export const delRoom = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/room/' + id,
    method: 'delete'
  });
};

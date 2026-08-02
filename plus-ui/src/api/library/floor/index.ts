import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { FloorVO, FloorForm, FloorQuery } from '@/api/library/floor/types';

/** 查询楼层列表 */
export const listFloor = (query?: FloorQuery): AxiosPromise<FloorVO[]> => {
  return request({
    url: '/library/floor/list',
    method: 'get',
    params: query
  });
};

/** 查询楼层详细 */
export const getFloor = (id: string | number): AxiosPromise<FloorVO> => {
  return request({
    url: '/library/floor/' + id,
    method: 'get'
  });
};

/** 新增楼层 */
export const addFloor = (data: FloorForm) => {
  return request({
    url: '/library/floor',
    method: 'post',
    data: data
  });
};

/** 修改楼层 */
export const updateFloor = (data: FloorForm) => {
  return request({
    url: '/library/floor',
    method: 'put',
    data: data
  });
};

/** 删除楼层 */
export const delFloor = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/floor/' + id,
    method: 'delete'
  });
};

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { LocationVO, LocationForm, LocationQuery } from '@/api/library/location/types';

/** 查询藏地列表 */
export const listLocation = (query?: LocationQuery): AxiosPromise<LocationVO[]> => {
  return request({
    url: '/library/location/list',
    method: 'get',
    params: query
  });
};

/** 查询藏地详细 */
export const getLocation = (id: string | number): AxiosPromise<LocationVO> => {
  return request({
    url: '/library/location/' + id,
    method: 'get'
  });
};

/** 新增藏地 */
export const addLocation = (data: LocationForm) => {
  return request({
    url: '/library/location',
    method: 'post',
    data: data
  });
};

/** 修改藏地 */
export const updateLocation = (data: LocationForm) => {
  return request({
    url: '/library/location',
    method: 'put',
    data: data
  });
};

/** 删除藏地 */
export const delLocation = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/location/' + id,
    method: 'delete'
  });
};

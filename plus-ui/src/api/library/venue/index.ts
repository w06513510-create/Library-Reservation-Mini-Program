import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { VenueVO, VenueForm, VenueQuery } from '@/api/library/venue/types';

/** 查询场馆列表 */
export const listVenue = (query?: VenueQuery): AxiosPromise<VenueVO[]> => {
  return request({
    url: '/library/venue/list',
    method: 'get',
    params: query
  });
};

/** 查询场馆详细 */
export const getVenue = (id: string | number): AxiosPromise<VenueVO> => {
  return request({
    url: '/library/venue/' + id,
    method: 'get'
  });
};

/** 新增场馆 */
export const addVenue = (data: VenueForm) => {
  return request({
    url: '/library/venue',
    method: 'post',
    data: data
  });
};

/** 修改场馆 */
export const updateVenue = (data: VenueForm) => {
  return request({
    url: '/library/venue',
    method: 'put',
    data: data
  });
};

/** 删除场馆 */
export const delVenue = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/venue/' + id,
    method: 'delete'
  });
};

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AreaVO, AreaForm, AreaQuery } from '@/api/library/area/types';

/** 查询区域列表 */
export const listArea = (query?: AreaQuery): AxiosPromise<AreaVO[]> => {
  return request({
    url: '/library/area/list',
    method: 'get',
    params: query
  });
};

/** 查询区域详细 */
export const getArea = (id: string | number): AxiosPromise<AreaVO> => {
  return request({
    url: '/library/area/' + id,
    method: 'get'
  });
};

/** 新增区域 */
export const addArea = (data: AreaForm) => {
  return request({
    url: '/library/area',
    method: 'post',
    data: data
  });
};

/** 修改区域 */
export const updateArea = (data: AreaForm) => {
  return request({
    url: '/library/area',
    method: 'put',
    data: data
  });
};

/** 删除区域 */
export const delArea = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/area/' + id,
    method: 'delete'
  });
};

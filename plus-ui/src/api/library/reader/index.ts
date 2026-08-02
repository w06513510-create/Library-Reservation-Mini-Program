import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ReaderVO, ReaderForm, ReaderQuery } from '@/api/library/reader/types';

/** 查询读者档案列表 */
export const listReader = (query?: ReaderQuery): AxiosPromise<ReaderVO[]> => {
  return request({
    url: '/library/reader/list',
    method: 'get',
    params: query
  });
};

/** 查询读者档案详细 */
export const getReader = (id: string | number): AxiosPromise<ReaderVO> => {
  return request({
    url: '/library/reader/' + id,
    method: 'get'
  });
};

/** 新增读者档案 */
export const addReader = (data: ReaderForm) => {
  return request({
    url: '/library/reader',
    method: 'post',
    data: data
  });
};

/** 修改读者档案 */
export const updateReader = (data: ReaderForm) => {
  return request({
    url: '/library/reader',
    method: 'put',
    data: data
  });
};

/** 删除读者档案 */
export const delReader = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/reader/' + id,
    method: 'delete'
  });
};

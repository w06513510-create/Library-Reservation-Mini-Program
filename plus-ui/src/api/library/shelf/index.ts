import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ShelfVO, ShelfForm, ShelfQuery } from '@/api/library/shelf/types';

/** 查询书架列表 */
export const listShelf = (query?: ShelfQuery): AxiosPromise<ShelfVO[]> => {
  return request({
    url: '/library/shelf/list',
    method: 'get',
    params: query
  });
};

/** 查询书架详细 */
export const getShelf = (id: string | number): AxiosPromise<ShelfVO> => {
  return request({
    url: '/library/shelf/' + id,
    method: 'get'
  });
};

/** 新增书架 */
export const addShelf = (data: ShelfForm) => {
  return request({
    url: '/library/shelf',
    method: 'post',
    data: data
  });
};

/** 修改书架 */
export const updateShelf = (data: ShelfForm) => {
  return request({
    url: '/library/shelf',
    method: 'put',
    data: data
  });
};

/** 删除书架 */
export const delShelf = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/shelf/' + id,
    method: 'delete'
  });
};

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BookItemVO, BookItemForm, BookItemQuery } from '@/api/library/bookItem/types';

/** 查询馆藏册列表 */
export const listBookItem = (query?: BookItemQuery): AxiosPromise<BookItemVO[]> => {
  return request({
    url: '/library/bookItem/list',
    method: 'get',
    params: query
  });
};

/** 查询馆藏册详细 */
export const getBookItem = (id: string | number): AxiosPromise<BookItemVO> => {
  return request({
    url: '/library/bookItem/' + id,
    method: 'get'
  });
};

/** 新增馆藏册 */
export const addBookItem = (data: BookItemForm) => {
  return request({
    url: '/library/bookItem',
    method: 'post',
    data: data
  });
};

/** 修改馆藏册 */
export const updateBookItem = (data: BookItemForm) => {
  return request({
    url: '/library/bookItem',
    method: 'put',
    data: data
  });
};

/** 删除馆藏册 */
export const delBookItem = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/bookItem/' + id,
    method: 'delete'
  });
};

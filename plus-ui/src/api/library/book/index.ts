import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BookVO, BookForm, BookQuery } from '@/api/library/book/types';

/** 查询书目列表 */
export const listBook = (query?: BookQuery): AxiosPromise<BookVO[]> => {
  return request({
    url: '/library/book/list',
    method: 'get',
    params: query
  });
};

/** 查询书目详细 */
export const getBook = (id: string | number): AxiosPromise<BookVO> => {
  return request({
    url: '/library/book/' + id,
    method: 'get'
  });
};

/** 新增书目 */
export const addBook = (data: BookForm) => {
  return request({
    url: '/library/book',
    method: 'post',
    data: data
  });
};

/** 修改书目 */
export const updateBook = (data: BookForm) => {
  return request({
    url: '/library/book',
    method: 'put',
    data: data
  });
};

/** 删除书目 */
export const delBook = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/book/' + id,
    method: 'delete'
  });
};

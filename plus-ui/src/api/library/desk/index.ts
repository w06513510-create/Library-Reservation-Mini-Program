import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { DeskVO, DeskForm, DeskQuery } from '@/api/library/desk/types';

/** 查询桌子列表 */
export const listDesk = (query?: DeskQuery): AxiosPromise<DeskVO[]> => {
  return request({
    url: '/library/desk/list',
    method: 'get',
    params: query
  });
};

/** 查询桌子详细 */
export const getDesk = (id: string | number): AxiosPromise<DeskVO> => {
  return request({
    url: '/library/desk/' + id,
    method: 'get'
  });
};

/** 新增桌子 */
export const addDesk = (data: DeskForm) => {
  return request({
    url: '/library/desk',
    method: 'post',
    data: data
  });
};

/** 修改桌子 */
export const updateDesk = (data: DeskForm) => {
  return request({
    url: '/library/desk',
    method: 'put',
    data: data
  });
};

/** 删除桌子 */
export const delDesk = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/desk/' + id,
    method: 'delete'
  });
};

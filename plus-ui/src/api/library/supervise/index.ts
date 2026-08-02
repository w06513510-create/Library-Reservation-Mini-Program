import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { SuperviseVO, SuperviseForm, SuperviseQuery } from '@/api/library/supervise/types';

/** 查询占座监督列表 */
export const listSupervise = (query?: SuperviseQuery): AxiosPromise<SuperviseVO[]> => {
  return request({
    url: '/library/supervise/list',
    method: 'get',
    params: query
  });
};

/** 查询占座监督详细 */
export const getSupervise = (id: string | number): AxiosPromise<SuperviseVO> => {
  return request({
    url: '/library/supervise/' + id,
    method: 'get'
  });
};

/** 发起占座监督（举报某使用中座位无人落座） */
export const reportSupervise = (data: SuperviseForm) => {
  return request({
    url: '/library/supervise',
    method: 'post',
    data: data
  });
};

/** 标记已落座：手动解除监督（原用户已按时落座） */
export const reseatSupervise = (id: string | number) => {
  return request({
    url: '/library/supervise/reseat/' + id,
    method: 'post'
  });
};

/** 删除占座监督 */
export const delSupervise = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/supervise/' + id,
    method: 'delete'
  });
};

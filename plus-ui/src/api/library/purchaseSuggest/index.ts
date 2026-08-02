import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { PurchaseSuggestVO, PurchaseSuggestForm, PurchaseSuggestQuery } from '@/api/library/purchaseSuggest/types';

/** 查询读者荐购列表 */
export const listPurchaseSuggest = (query?: PurchaseSuggestQuery): AxiosPromise<PurchaseSuggestVO[]> => {
  return request({
    url: '/library/purchaseSuggest/list',
    method: 'get',
    params: query
  });
};

/** 查询读者荐购详细 */
export const getPurchaseSuggest = (id: string | number): AxiosPromise<PurchaseSuggestVO> => {
  return request({
    url: '/library/purchaseSuggest/' + id,
    method: 'get'
  });
};

/** 新增读者荐购 */
export const addPurchaseSuggest = (data: PurchaseSuggestForm) => {
  return request({
    url: '/library/purchaseSuggest',
    method: 'post',
    data: data
  });
};

/** 修改读者荐购 */
export const updatePurchaseSuggest = (data: PurchaseSuggestForm) => {
  return request({
    url: '/library/purchaseSuggest',
    method: 'put',
    data: data
  });
};

/** 删除读者荐购 */
export const delPurchaseSuggest = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/purchaseSuggest/' + id,
    method: 'delete'
  });
};

/** 受理转采购 0→1 */
export const acceptPurchaseSuggest = (id: string | number) => {
  return request({ url: '/library/purchaseSuggest/accept/' + id, method: 'post' });
};

/** 驳回 0→2 */
export const rejectPurchaseSuggest = (id: string | number, reason: string) => {
  return request({ url: '/library/purchaseSuggest/reject/' + id, method: 'post', params: { reason } });
};

/** 标记已采购 1→3 */
export const purchasedPurchaseSuggest = (id: string | number) => {
  return request({ url: '/library/purchaseSuggest/purchased/' + id, method: 'post' });
};

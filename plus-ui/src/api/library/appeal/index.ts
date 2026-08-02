import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AppealVO, AppealForm, AppealQuery } from '@/api/library/appeal/types';

/** 查询违约申诉列表 */
export const listAppeal = (query?: AppealQuery): AxiosPromise<AppealVO[]> => {
  return request({ url: '/library/appeal/list', method: 'get', params: query });
};

/** 提交申诉 */
export const addAppeal = (data: AppealForm) => {
  return request({ url: '/library/appeal', method: 'post', data: data });
};

/** 审批申诉：通过/驳回 */
export const auditAppeal = (id: string | number, pass: boolean, remark?: string) => {
  return request({ url: '/library/appeal/audit/' + id, method: 'put', params: { pass, remark } });
};

import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { LoanVO, LoanForm, LoanQuery } from '@/api/library/loan/types';

export const listLoan = (query?: LoanQuery): AxiosPromise<LoanVO[]> => {
  return request({ url: '/library/loan/list', method: 'get', params: query });
};
export const getLoan = (id: string | number): AxiosPromise<LoanVO> => {
  return request({ url: '/library/loan/' + id, method: 'get' });
};
/** 借出办理 */
export const borrowLoan = (data: LoanForm) => request({ url: '/library/loan/borrow', method: 'post', data });
export const returnLoan = (id: string | number) => request({ url: '/library/loan/return/' + id, method: 'put' });
export const renewLoan = (id: string | number) => request({ url: '/library/loan/renew/' + id, method: 'put' });
export const recallLoan = (id: string | number) => request({ url: '/library/loan/recall/' + id, method: 'put' });

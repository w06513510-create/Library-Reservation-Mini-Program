import request from '@/utils/request';
import { AxiosPromise } from 'axios';

/** 数据可视化大屏 概览指标（亮点③） */
export const getDashboardOverview = (): AxiosPromise<any> => {
  return request({ url: '/library/dashboard/overview', method: 'get' });
};

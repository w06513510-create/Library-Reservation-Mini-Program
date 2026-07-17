import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { BizDashboardVO } from '@/api/biz/dashboard/types';

/**
 * 看板聚合数据
 * 放到 plus-ui/src/api/biz/dashboard/index.ts（把 biz 换成你的业务）
 */
export const getDashboardData = (): AxiosPromise<BizDashboardVO> => {
  return request({
    url: '/biz/dashboard/overview',
    method: 'get'
  });
};

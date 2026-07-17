/**
 * 看板类型定义
 * 放到 plus-ui/src/api/biz/dashboard/types.ts
 * 后端下发的数值可能是字符串，统一用 number | string 并在渲染时 Number() 兜底。
 */
export interface BizDashboardOverview {
  total: number | string;
  today: number | string;
  week: number | string;
  completed: number | string;
  cancelled: number | string;
  totalAmount: number | string;
  activeUsers: number | string;
  completionRate: number | string;
}

export interface StatusItem {
  status: number | string;
  value: number | string;
}

export interface CategoryItem {
  category: number | string;
  value: number | string;
}

export interface TrendItem {
  date: string;
  count: number | string;
  amount: number | string;
}

export interface RankItem {
  name: string;
  value: number | string;
  count: number | string;
}

export interface BizDashboardVO {
  overview: BizDashboardOverview;
  statusDist: StatusItem[];
  categoryDist: CategoryItem[];
  trend: TrendItem[];
  rank: RankItem[];
}

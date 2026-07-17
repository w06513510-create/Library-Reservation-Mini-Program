import { ref } from 'vue';
import type { Ref } from 'vue';
import type { Resp } from '../utils/request';

export interface PageParams {
  pageNum: number;
  pageSize: number;
  [k: string]: any;
}

export interface UseListOptions<T> {
  /** 取一页数据；须返回后端 { rows, total } 结构 */
  fetch: (params: PageParams) => Promise<Resp<T[]>>;
  /** 每页条数，默认 10 */
  pageSize?: number;
  /** 附加筛选参数（状态/关键字等），每次请求合并 */
  extraParams?: () => Record<string, any>;
  /** 是否创建时立即加载首屏，默认 true */
  immediate?: boolean;
}

/**
 * 分页列表 composable：收敛各页 onReachBottom / onPullDownRefresh 的重复模板。
 * 页面里：onReachBottom(loadMore) + onPullDownRefresh(onRefresh)。
 */
export function useList<T = any>(options: UseListOptions<T>) {
  const { fetch, pageSize = 10, extraParams, immediate = true } = options;

  const list = ref([]) as Ref<T[]>;
  const pageNum = ref(1);
  const total = ref(0);
  const loading = ref(false);
  const finished = ref(false);
  const refreshing = ref(false);

  async function load(reset: boolean) {
    if (loading.value) return;
    if (reset) {
      pageNum.value = 1;
      finished.value = false;
    } else if (finished.value) {
      return;
    }
    loading.value = true;
    try {
      const params: PageParams = {
        pageNum: pageNum.value,
        pageSize,
        ...(extraParams ? extraParams() : {})
      };
      const res = await fetch(params);
      const rows = (res.rows as T[]) || [];
      total.value = res.total || 0;
      list.value = reset ? rows : list.value.concat(rows);
      if (list.value.length >= total.value || rows.length < pageSize) {
        finished.value = true;
      } else {
        pageNum.value += 1;
      }
    } finally {
      loading.value = false;
    }
  }

  /** 重置到第 1 页（切筛选/下拉刷新） */
  function reload() {
    return load(true);
  }
  /** 触底加载下一页 */
  function loadMore() {
    return load(false);
  }
  /** 下拉刷新（置 refreshing 标志） */
  async function onRefresh() {
    refreshing.value = true;
    try {
      await load(true);
    } finally {
      refreshing.value = false;
    }
  }

  if (immediate) {
    load(true);
  }

  return { list, pageNum, total, loading, finished, refreshing, reload, loadMore, onRefresh };
}

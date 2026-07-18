import { ref } from 'vue';
import { apiToggle, apiHas, apiCount, type InteractionAction } from '../api/interaction';

/**
 * 单个业务对象的互动状态（收藏/点赞/关注）——详情页用。
 * 列表页批量渲染请让后端在列表接口里用 IInteractionService.hasBatch/countBatch 直接下发。
 */
export function useInteraction(action: InteractionAction, bizType: string, bizId: number) {
  const active = ref(false);
  const count = ref(0);
  const loading = ref(false);

  /** 拉取初始状态（是否已操作 + 计数） */
  async function init() {
    try {
      const [h, c] = await Promise.all([apiHas(action, bizType, bizId), apiCount(action, bizType, bizId)]);
      active.value = !!h.data;
      count.value = c.data || 0;
    } catch (e) {
      // 忽略：未登录/网络异常由 request 统一处理
    }
  }

  /** 开关并同步最新状态与计数 */
  async function toggle() {
    if (loading.value) return;
    loading.value = true;
    try {
      const r = await apiToggle(action, bizType, bizId);
      active.value = !!r.data?.active;
      count.value = r.data?.count ?? count.value;
    } catch (e) {
      // 错误已由 request 统一提示
    } finally {
      loading.value = false;
    }
  }

  return { active, count, loading, init, toggle };
}

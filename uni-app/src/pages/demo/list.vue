<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看示例数据 ›</view>

    <template v-else>
      <view class="item" v-for="it in list" :key="it.id">
        <text class="title">{{ it.title }}</text>
        <view class="right">
          <text class="badge" :style="{ color: dictColor(AUDIT_STATUS, it.status) }">
            {{ dictLabel(AUDIT_STATUS, it.status) }}
          </text>
          <text class="fav" :class="{ on: favSet.has(it.id) }" @click="toggleFav(it.id)">
            {{ favSet.has(it.id) ? '♥' : '♡' }}
          </text>
        </view>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="finished && list.length" class="foot">没有更多了</view>
      <view v-else-if="!list.length" class="foot">暂无数据</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import { dictLabel, dictColor, AUDIT_STATUS } from '../../utils/dict';
import { apiToggle, apiMyBizIds } from '../../api/interaction';

interface DemoItem {
  id: number;
  title: string;
  status: number;
}

const authStore = useAuthStore();

const { list, loading, finished, reload, loadMore, onRefresh } = useList<DemoItem>({
  fetch: (params) => request<DemoItem[]>({ url: '/app/demo/page', params }),
  pageSize: 10,
  immediate: false
});

// 我收藏了哪些 demo 对象（bizType=demo, action=favorite）；演示通用互动模块
const favSet = ref<Set<number>>(new Set());
async function loadFav() {
  try {
    const r = await apiMyBizIds('favorite', 'demo', 1, 100);
    favSet.value = new Set(r.rows || []);
  } catch (e) {
    // 忽略：未登录/网络异常由 request 统一处理
  }
}
async function toggleFav(id: number) {
  const r = await apiToggle('favorite', 'demo', id).catch(() => null);
  if (!r) return;
  const s = new Set(favSet.value);
  if (r.data?.active) s.add(id);
  else s.delete(id);
  favSet.value = s;
}

onShow(() => {
  if (authStore.isLogin) {
    reload();
    loadFav();
  }
});
onReachBottom(() => {
  if (authStore.isLogin) loadMore();
});
onPullDownRefresh(async () => {
  if (authStore.isLogin) await onRefresh();
  uni.stopPullDownRefresh();
});

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' });
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx;
}
.tip {
  margin-top: 120rpx;
  text-align: center;
  color: #909399;
  font-size: 28rpx;
}
.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.04);
  .title {
    font-size: 30rpx;
    color: #303133;
  }
  .right {
    display: flex;
    align-items: center;
    gap: 24rpx;
  }
  .badge {
    font-size: 26rpx;
  }
  .fav {
    font-size: 40rpx;
    color: #c0c4cc;
    &.on {
      color: #f56c6c;
    }
  }
}
.foot {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>

<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看收藏 ›</view>

    <template v-else>
      <view class="item" v-for="id in list" :key="id">
        <text class="t">示例数据 #{{ id }}</text>
        <text class="unfav" @click="unfav(id)">取消收藏</text>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="finished && list.length" class="foot">没有更多了</view>
      <view v-else-if="!list.length" class="foot">还没有收藏（去"示例"页点 ♡ 收藏）</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import { apiToggle } from '../../api/interaction';

const authStore = useAuthStore();

// 我收藏的 demo 对象ID（bizType=demo, action=favorite）
const { list, loading, finished, reload, loadMore, onRefresh } = useList<number>({
  fetch: (params) => request<number[]>({ url: '/app/interaction/my/page', params: { ...params, action: 'favorite', bizType: 'demo' } }),
  pageSize: 10,
  immediate: false
});

onShow(() => {
  if (authStore.isLogin) reload();
});
onReachBottom(() => {
  if (authStore.isLogin) loadMore();
});
onPullDownRefresh(async () => {
  if (authStore.isLogin) await onRefresh();
  uni.stopPullDownRefresh();
});

async function unfav(id: number) {
  await apiToggle('favorite', 'demo', id).catch(() => {});
  reload();
}
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
  .t {
    font-size: 30rpx;
    color: #303133;
  }
  .unfav {
    font-size: 26rpx;
    color: #f56c6c;
  }
}
.foot {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>

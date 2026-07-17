<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看示例数据 ›</view>

    <template v-else>
      <view class="item" v-for="it in list" :key="it.id">
        <text class="title">{{ it.title }}</text>
        <text class="badge" :style="{ color: dictColor(AUDIT_STATUS, it.status) }">
          {{ dictLabel(AUDIT_STATUS, it.status) }}
        </text>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="finished && list.length" class="foot">没有更多了</view>
      <view v-else-if="!list.length" class="foot">暂无数据</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import { dictLabel, dictColor, AUDIT_STATUS } from '../../utils/dict';

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
  .badge {
    font-size: 26rpx;
  }
}
.foot {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>

<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看通知 ›</view>

    <template v-else>
      <view v-if="list.length" class="toolbar">
        <text class="read-all" @click="readAll">全部已读</text>
      </view>

      <view
        class="item"
        :class="{ 'item--unread': it.isRead === 0 }"
        v-for="it in list"
        :key="it.id"
        @click="onRead(it)"
      >
        <view class="row1">
          <text class="title">{{ it.title }}</text>
          <view v-if="it.isRead === 0" class="dot" />
        </view>
        <text v-if="it.content" class="content">{{ it.content }}</text>
        <text class="time">{{ it.createTime }}</text>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="finished && list.length" class="foot">没有更多了</view>
      <view v-else-if="!list.length" class="foot">暂无通知</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import { apiNoticeList, apiNoticeRead, apiNoticeReadAll, type NotificationVo } from '../../api/message';

const authStore = useAuthStore();

const { list, loading, finished, reload, loadMore, onRefresh } = useList<NotificationVo>({
  fetch: (params) => apiNoticeList(params),
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

async function onRead(it: NotificationVo) {
  if (it.isRead === 1) return;
  try {
    await apiNoticeRead(it.id);
    it.isRead = 1;
  } catch (e) {
    // 错误已统一提示
  }
}

async function readAll() {
  try {
    await apiNoticeReadAll();
    list.value.forEach((n) => (n.isRead = 1));
    uni.showToast({ title: '已全部已读', icon: 'success' });
  } catch (e) {
    // 错误已统一提示
  }
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
.toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 4rpx 8rpx 20rpx;
  .read-all {
    font-size: 26rpx;
    color: #409eff;
  }
}
.item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.04);
  &--unread .title {
    font-weight: 700;
  }
  .row1 {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .title {
      font-size: 30rpx;
      color: #303133;
    }
    .dot {
      width: 16rpx;
      height: 16rpx;
      border-radius: 50%;
      background: #f56c6c;
      flex-shrink: 0;
    }
  }
  .content {
    display: block;
    margin-top: 12rpx;
    font-size: 26rpx;
    color: #606266;
    line-height: 1.5;
  }
  .time {
    display: block;
    margin-top: 12rpx;
    font-size: 22rpx;
    color: #c0c4cc;
  }
}
.foot {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>

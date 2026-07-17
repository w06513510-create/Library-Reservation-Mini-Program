<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看会话 ›</view>

    <template v-else>
      <view class="item" v-for="c in list" :key="c.peerId" @click="openChat(c)">
        <image v-if="c.peerAvatar" class="avatar" :src="c.peerAvatar" mode="aspectFill" />
        <view v-else class="avatar avatar--empty">{{ (c.peerNickname || '?').charAt(0) }}</view>
        <view class="mid">
          <text class="name">{{ c.peerNickname }}</text>
          <text class="last">{{ c.lastContent }}</text>
        </view>
        <view class="right">
          <text class="time">{{ shortTime(c.lastTime) }}</text>
          <view v-if="c.unread > 0" class="badge">{{ c.unread > 99 ? '99+' : c.unread }}</view>
        </view>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="!list.length" class="foot">暂无会话</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { useAuthStore } from '../../store/auth';
import { apiConversations, type ConversationVo } from '../../api/message';

const authStore = useAuthStore();
const list = ref<ConversationVo[]>([]);
const loading = ref(false);

async function load() {
  if (!authStore.isLogin) return;
  loading.value = true;
  try {
    const res = await apiConversations();
    list.value = res.data || [];
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false;
  }
}

onShow(() => load());
onPullDownRefresh(async () => {
  await load();
  uni.stopPullDownRefresh();
});

function openChat(c: ConversationVo) {
  uni.navigateTo({
    url: `/pages/message/chat?peerId=${c.peerId}&peerNickname=${encodeURIComponent(c.peerNickname || '')}`
  });
}

function shortTime(t?: string): string {
  if (!t) return '';
  // 取 "MM-DD HH:mm" 简显
  const s = t.replace('T', ' ');
  return s.length >= 16 ? s.substring(5, 16) : s;
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' });
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
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
  background: #fff;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f0f2f5;
  .avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    flex-shrink: 0;
    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #409eff;
      color: #fff;
      font-size: 36rpx;
    }
  }
  .mid {
    flex: 1;
    margin-left: 24rpx;
    overflow: hidden;
    .name {
      display: block;
      font-size: 30rpx;
      color: #303133;
    }
    .last {
      display: block;
      margin-top: 10rpx;
      font-size: 26rpx;
      color: #909399;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  .right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
    .time {
      font-size: 22rpx;
      color: #c0c4cc;
    }
    .badge {
      margin-top: 12rpx;
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      border-radius: 16rpx;
      background: #f56c6c;
      color: #fff;
      font-size: 20rpx;
      line-height: 32rpx;
      text-align: center;
    }
  }
}
.foot {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 40rpx 0;
}
</style>

<template>
  <view class="home">
    <view class="banner">
      <view class="b-title">RuoYi Template</view>
      <view class="b-sub">C 端接入基座 · uni-app 骨架</view>
    </view>

    <view class="card">
      <view class="row" @click="goDemo">
        <text class="row-label">📄 分页示例（useList）</text>
        <text class="arrow">›</text>
      </view>
      <view class="row row--border" @click="goMine">
        <text class="row-label">👤 我的</text>
        <text class="arrow">›</text>
      </view>
      <view v-if="!authStore.isLogin" class="row row--border" @click="goLogin">
        <text class="row-label">🔑 登录 / 注册</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="hint">
      基座内置：手机号登录（app_user）、媒体上传（/resource/media/upload）、本地字典、分页 composable。
      复制本骨架起新项目，改 config.ts 即可对接后端。
    </view>
  </view>
</template>

<script setup lang="ts">
import { useAuthStore } from '../../store/auth';

const authStore = useAuthStore();

function goDemo() {
  uni.switchTab({ url: '/pages/demo/list' });
}
function goMine() {
  uni.switchTab({ url: '/pages/mine/mine' });
}
function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' });
}
</script>

<style lang="scss">
.home {
  min-height: 100vh;
}
.banner {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  padding: 80rpx 48rpx;
  .b-title {
    font-size: 48rpx;
    font-weight: 700;
  }
  .b-sub {
    margin-top: 16rpx;
    font-size: 26rpx;
    opacity: 0.9;
  }
}
.card {
  margin: 32rpx 24rpx 0;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  .row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    &--border {
      border-top: 1rpx solid #f0f2f5;
    }
    .row-label {
      font-size: 30rpx;
      color: #303133;
    }
    .arrow {
      font-size: 40rpx;
      color: #c0c4cc;
    }
  }
}
.hint {
  margin: 32rpx 32rpx;
  font-size: 24rpx;
  color: #909399;
  line-height: 1.7;
}
</style>

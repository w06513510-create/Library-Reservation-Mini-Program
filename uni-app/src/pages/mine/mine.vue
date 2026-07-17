<template>
  <view class="page">
    <view class="header">
      <template v-if="authStore.isLogin">
        <image v-if="user.avatar" class="avatar" :src="user.avatar" mode="aspectFill" @click="changeAvatar" />
        <view v-else class="avatar avatar--empty" @click="changeAvatar">＋</view>
        <view class="u-info">
          <view class="u-name">{{ authStore.nickname }}</view>
          <view class="u-phone">{{ user.phone || '' }}</view>
        </view>
      </template>
      <template v-else>
        <view class="avatar avatar--empty">👤</view>
        <view class="u-info">
          <view class="u-name" @click="goLogin">未登录，点击登录 ›</view>
        </view>
      </template>
    </view>

    <view v-if="authStore.isLogin" class="menu">
      <view class="menu-row" @click="changeAvatar">
        <text class="menu-label">🖼️ 更换头像</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-row menu-row--border" @click="editNickname">
        <text class="menu-label">✏️ 修改昵称</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <button v-if="authStore.isLogin" class="btn-logout" @click="doLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '../../store/auth';
import { chooseAndUploadImages } from '../../utils/upload';
import { apiUpdateAvatar, apiUpdateNickname } from '../../api/auth';

const authStore = useAuthStore();
const user = computed(() => authStore.user);

onShow(() => {
  if (authStore.isLogin) authStore.getInfo().catch(() => {});
});

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' });
}

async function changeAvatar() {
  if (!authStore.isLogin) return goLogin();
  const urls = await chooseAndUploadImages(1);
  if (!urls.length) return;
  try {
    await apiUpdateAvatar(urls[0]);
    await authStore.getInfo();
    uni.showToast({ title: '头像已更新', icon: 'success' });
  } catch (e) {
    // 错误已统一提示
  }
}

function editNickname() {
  uni.showModal({
    title: '修改昵称',
    editable: true,
    placeholderText: '请输入新昵称',
    content: user.value.nickname || '',
    success: async (r) => {
      if (r.confirm && r.content && r.content.trim()) {
        try {
          await apiUpdateNickname(r.content.trim());
          await authStore.getInfo();
          uni.showToast({ title: '昵称已更新', icon: 'success' });
        } catch (e) {
          // 错误已统一提示
        }
      }
    }
  });
}

function doLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (r) => {
      if (r.confirm) {
        authStore.logout();
        uni.reLaunch({ url: '/pages/login/login' });
      }
    }
  });
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
}
.header {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  padding: 56rpx 40rpx;
  color: #fff;
  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    flex-shrink: 0;
    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.25);
      font-size: 56rpx;
    }
  }
  .u-info {
    margin-left: 28rpx;
    flex: 1;
    .u-name {
      font-size: 38rpx;
      font-weight: 700;
    }
    .u-phone {
      margin-top: 12rpx;
      font-size: 26rpx;
      opacity: 0.9;
    }
  }
}
.menu {
  margin: 32rpx 24rpx 0;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  .menu-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    &--border {
      border-top: 1rpx solid #f0f2f5;
    }
    .menu-label {
      font-size: 30rpx;
      color: #303133;
    }
    .arrow {
      font-size: 40rpx;
      color: #c0c4cc;
    }
  }
}
.btn-logout {
  margin: 48rpx 24rpx 0;
  background: #fff;
  color: #f56c6c;
  font-size: 30rpx;
  border-radius: 44rpx;
  &::after {
    border: none;
  }
}
</style>

<template>
  <view class="page">
    <view class="head">
      <view class="avatar">{{ (profile?.realName || '读').slice(0, 1) }}</view>
      <view class="info" v-if="profile">
        <view class="name">{{ profile.realName }}</view>
        <view class="sub">{{ profile.studentNo }} · {{ profile.college || '—' }}</view>
      </view>
      <view class="info" v-else @click="goLogin">
        <view class="name">未登录</view>
        <view class="sub">点此登录 ›</view>
      </view>
      <view class="score" v-if="profile">
        <text class="score-n">{{ profile.creditScore }}</text>
        <text class="score-l">信用分</text>
      </view>
    </view>

    <view class="stat" v-if="profile">
      <view class="stat-c"><text class="v">{{ profile.performCount ?? 0 }}</text><text class="l">守信次数</text></view>
      <view class="stat-c"><text class="v" :style="{ color: profile.blacklistFlag === 1 ? '#cf5b4e' : '#4d8a5e' }">{{ profile.blacklistFlag === 1 ? '受限' : '正常' }}</text><text class="l">账号状态</text></view>
    </view>

    <view class="menu">
      <view class="mi" @click="go('/pages/seat/reservations')"><text>📋 我的预约</text><text class="ar">›</text></view>
      <view class="mi" @click="go('/pages/book/search')"><text>📚 我的借阅</text><text class="ar">›</text></view>
      <view class="mi" @click="go('/pages/reader/credit')"><text>⭐ 信用分与流水</text><text class="ar">›</text></view>
      <view class="mi" @click="go('/pages/reader/violations')"><text>⚠️ 违约与申诉</text><text class="ar">›</text></view>
      <view class="mi" @click="go('/pages/reader/rules')"><text>📖 规则说明</text><text class="ar">›</text></view>
    </view>

    <button v-if="profile" class="logout" @click="doLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { readerApi, type ReaderVo } from '../../api/library';
import { useAuthStore } from '../../store/auth';
import { getToken } from '../../utils/request';

const auth = useAuthStore();
const profile = ref<ReaderVo | null>(null);

async function load() {
  if (!getToken()) { profile.value = null; return; }
  try { profile.value = (await readerApi.profile()).data || null; } catch { profile.value = null; }
}

function go(url: string) {
  if (!getToken()) { goLogin(); return; }
  uni.navigateTo({ url });
}
function goLogin() { uni.navigateTo({ url: '/pages/login/login' }); }
function doLogout() {
  uni.showModal({
    title: '提示', content: '确认退出登录？', success: (r) => {
      if (!r.confirm) return;
      auth.logout();
      profile.value = null;
      uni.showToast({ title: '已退出', icon: 'none' });
    }
  });
}

onShow(load);
</script>

<style lang="scss">
.page { min-height: 100vh; }
.head {
  background: linear-gradient(135deg, #d9714e, #e5946f);
  color: #fff;
  padding: 60rpx 40rpx 48rpx;
  display: flex;
  align-items: center;
}
.avatar {
  width: 110rpx; height: 110rpx; border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  display: flex; align-items: center; justify-content: center;
  font-size: 48rpx; font-weight: 700;
}
.info { flex: 1; margin-left: 28rpx;
  .name { font-size: 38rpx; font-weight: 700; }
  .sub { font-size: 24rpx; opacity: 0.9; margin-top: 8rpx; }
}
.score { text-align: center;
  .score-n { display: block; font-size: 44rpx; font-weight: 700; }
  .score-l { font-size: 22rpx; opacity: 0.9; }
}
.stat {
  margin: -24rpx 24rpx 0;
  background: #fff;
  border-radius: 20rpx;
  display: flex;
  padding: 28rpx 0;
  box-shadow: 0 8rpx 24rpx rgba(160, 90, 60, 0.08);
  position: relative;
  z-index: 1;
  .stat-c { flex: 1; text-align: center; border-right: 1rpx solid #f0ece6; &:last-child { border: none; }
    .v { display: block; font-size: 36rpx; font-weight: 700; color: #3a332e; }
    .l { font-size: 22rpx; color: #9a938c; margin-top: 6rpx; }
  }
}
.menu {
  margin: 28rpx 24rpx 0;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  .mi {
    display: flex; align-items: center; justify-content: space-between;
    padding: 34rpx 32rpx;
    font-size: 30rpx; color: #3a332e;
    border-top: 1rpx solid #f5f1eb;
    &:first-child { border-top: none; }
    .ar { color: #c8c0b8; font-size: 40rpx; }
  }
}
.logout {
  margin: 40rpx 24rpx;
  background: #fff;
  color: #cf5b4e;
  font-size: 30rpx;
  border-radius: 16rpx;
  &::after { border: none; }
}
</style>

<template>
  <view class="home">
    <!-- 顶部信用卡片 -->
    <view class="hero">
      <view class="hero-head">
        <view>
          <view class="greet">{{ greet }}</view>
          <view class="name">{{ profile?.realName || '未登录读者' }}</view>
        </view>
        <view class="credit" v-if="profile">
          <view class="credit-num">{{ profile.creditScore }}</view>
          <view class="credit-label">信用分</view>
        </view>
      </view>
      <view class="hero-meta" v-if="profile">
        <text class="chip">{{ profile.studentNo }}</text>
        <text class="chip" v-if="profile.college">{{ profile.college }}</text>
        <text class="chip" v-if="profile.blacklistFlag === 1" style="background:#fbe4df;color:#cf5b4e">黑名单中</text>
      </view>
      <view class="hero-login" v-else @click="goLogin">
        <text>点此登录 · 演示账号 2021001 / admin123</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="grid">
      <view class="cell" @click="goSeat">
        <view class="cell-ic" style="background:#fdeee7">🪑</view>
        <text class="cell-t">选座预约</text>
      </view>
      <view class="cell" @click="goReservations">
        <view class="cell-ic" style="background:#eaf3ec">📋</view>
        <text class="cell-t">我的预约</text>
      </view>
      <view class="cell" @click="goBook">
        <view class="cell-ic" style="background:#eef0fa">🔍</view>
        <text class="cell-t">书目检索</text>
      </view>
      <view class="cell" @click="goCredit">
        <view class="cell-ic" style="background:#fbf1e2">⭐</view>
        <text class="cell-t">信用分</text>
      </view>
      <view class="cell" @click="goViolations">
        <view class="cell-ic" style="background:#faece9">⚠️</view>
        <text class="cell-t">违约申诉</text>
      </view>
      <view class="cell" @click="goRules">
        <view class="cell-ic" style="background:#eef0fa">📖</view>
        <text class="cell-t">规则说明</text>
      </view>
    </view>

    <view class="tips">
      <view class="tips-t">温馨提示</view>
      <view class="tips-c">按时到馆签到、离馆签退可累积守信次数并加信用分；爽约、暂离超时会扣分，信用分过低将暂停预约。</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { readerApi, type ReaderVo } from '../../api/library';
import { getToken } from '../../utils/request';

const profile = ref<ReaderVo | null>(null);
const greet = ref('你好');

function calcGreet() {
  const h = new Date().getHours();
  greet.value = h < 6 ? '夜深了' : h < 11 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好';
}

async function loadProfile() {
  if (!getToken()) {
    profile.value = null;
    return;
  }
  try {
    const res = await readerApi.profile();
    profile.value = res.data || null;
  } catch (e) {
    profile.value = null;
  }
}

onShow(() => {
  calcGreet();
  loadProfile();
});

function goLogin() { uni.navigateTo({ url: '/pages/login/login' }); }
function goSeat() { uni.switchTab({ url: '/pages/seat/select' }); }
function goBook() { uni.switchTab({ url: '/pages/book/search' }); }
function goReservations() { uni.navigateTo({ url: '/pages/seat/reservations' }); }
function goCredit() { uni.navigateTo({ url: '/pages/reader/credit' }); }
function goViolations() { uni.navigateTo({ url: '/pages/reader/violations' }); }
function goRules() { uni.navigateTo({ url: '/pages/reader/rules' }); }
</script>

<style lang="scss">
.home {
  min-height: 100vh;
  padding-bottom: 40rpx;
}
.hero {
  background: linear-gradient(135deg, #d9714e, #e5946f);
  color: #fff;
  padding: 56rpx 40rpx 48rpx;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}
.hero-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.greet { font-size: 26rpx; opacity: 0.9; }
.name { font-size: 44rpx; font-weight: 700; margin-top: 8rpx; }
.credit {
  text-align: center;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 20rpx;
  padding: 12rpx 28rpx;
  .credit-num { font-size: 48rpx; font-weight: 700; }
  .credit-label { font-size: 22rpx; opacity: 0.9; }
}
.hero-meta { margin-top: 28rpx; display: flex; flex-wrap: wrap; gap: 14rpx; }
.chip {
  font-size: 22rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 100rpx;
  padding: 6rpx 20rpx;
}
.hero-login {
  margin-top: 24rpx;
  font-size: 26rpx;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 100rpx;
  padding: 16rpx 28rpx;
  text-align: center;
}
.grid {
  margin: 32rpx 24rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx 0;
  display: flex;
  flex-wrap: wrap;
  box-shadow: 0 8rpx 24rpx rgba(160, 90, 60, 0.06);
}
.cell {
  width: 33.33%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  .cell-ic {
    width: 88rpx;
    height: 88rpx;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44rpx;
  }
  .cell-t { margin-top: 14rpx; font-size: 26rpx; color: #3a332e; }
}
.tips {
  margin: 32rpx 24rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(160, 90, 60, 0.06);
  .tips-t { font-size: 30rpx; font-weight: 700; color: #3a332e; }
  .tips-c { margin-top: 14rpx; font-size: 26rpx; color: #9a938c; line-height: 1.7; }
}
</style>

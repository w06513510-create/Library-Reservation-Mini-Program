<template>
  <view class="login">
    <view class="brand">
      <view class="brand-name">RuoYi Template</view>
      <view class="brand-slogan">C 端账号登录</view>
    </view>

    <view class="card">
      <view class="tabs">
        <view :class="['tab', mode === 'login' ? 'active' : '']" @click="switchMode('login')">登录</view>
        <view :class="['tab', mode === 'register' ? 'active' : '']" @click="switchMode('register')">注册</view>
      </view>

      <view class="field">
        <text class="label">手机号</text>
        <input class="input" type="number" maxlength="11" placeholder="请输入手机号" v-model="phone" />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input class="input" password placeholder="请输入密码" v-model="password" />
      </view>
      <view class="field" v-if="mode === 'register'">
        <text class="label">确认密码</text>
        <input class="input" password placeholder="请再次输入密码" v-model="confirmPassword" />
      </view>

      <button class="btn-primary" :loading="loading" :disabled="loading" @click="submit">
        {{ mode === 'login' ? '登 录' : '注 册' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '../../store/auth';

const authStore = useAuthStore();

const mode = ref<'login' | 'register'>('login');
const phone = ref('');
const password = ref('');
const confirmPassword = ref('');
const loading = ref(false);

function switchMode(m: 'login' | 'register') {
  if (loading.value) return;
  mode.value = m;
}

function validate(): boolean {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' });
    return false;
  }
  if (password.value.length < 6) {
    uni.showToast({ title: '密码至少 6 位', icon: 'none' });
    return false;
  }
  if (mode.value === 'register' && password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' });
    return false;
  }
  return true;
}

async function submit() {
  if (loading.value) return;
  if (!validate()) return;
  loading.value = true;
  uni.showLoading({ title: mode.value === 'login' ? '登录中' : '注册中', mask: true });
  try {
    if (mode.value === 'register') {
      await authStore.register(phone.value, password.value);
    }
    await authStore.login(phone.value, password.value);
    uni.hideLoading();
    uni.showToast({ title: mode.value === 'login' ? '登录成功' : '注册成功', icon: 'success' });
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 600);
  } catch (e) {
    uni.hideLoading();
    // 错误已由 request.ts 统一 toast
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss">
.login {
  min-height: 100vh;
  padding: 0 48rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 28%, #f5f6f8 58%);
}
.brand {
  padding-top: 140rpx;
  padding-bottom: 60rpx;
  text-align: center;
  color: #fff;
  .brand-name {
    font-size: 46rpx;
    font-weight: 700;
  }
  .brand-slogan {
    margin-top: 12rpx;
    font-size: 26rpx;
    opacity: 0.9;
  }
}
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx 40rpx;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
}
.tabs {
  display: flex;
  margin-bottom: 40rpx;
  .tab {
    flex: 1;
    text-align: center;
    font-size: 32rpx;
    color: #909399;
    padding-bottom: 16rpx;
    border-bottom: 4rpx solid transparent;
    &.active {
      color: #409eff;
      font-weight: 700;
      border-bottom-color: #409eff;
    }
  }
}
.field {
  margin-bottom: 32rpx;
  .label {
    display: block;
    font-size: 26rpx;
    color: #606266;
    margin-bottom: 12rpx;
  }
  .input {
    height: 88rpx;
    background: #f5f6f8;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 30rpx;
  }
}
.btn-primary {
  margin-top: 16rpx;
  background: #409eff;
  color: #fff;
  font-size: 32rpx;
  border-radius: 12rpx;
  &::after {
    border: none;
  }
}
</style>

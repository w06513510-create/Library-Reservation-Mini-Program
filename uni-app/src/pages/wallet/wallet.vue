<template>
  <view class="page">
    <view v-if="!authStore.isLogin" class="tip" @click="goLogin">请先登录后查看钱包 ›</view>

    <template v-else>
      <!-- 余额卡片 -->
      <view class="card">
        <text class="label">可用余额(元)</text>
        <text class="balance">{{ money(wallet.balance) }}</text>
        <view class="sub">
          <text>冻结 {{ money(wallet.frozen) }}</text>
          <text>累计充值 {{ money(wallet.totalRecharge) }}</text>
        </view>
      </view>

      <!-- 充值 -->
      <view class="recharge">
        <input
          class="input"
          type="digit"
          v-model="amount"
          placeholder="输入充值金额"
        />
        <button class="btn" :disabled="paying" @click="doRecharge">充值</button>
      </view>

      <!-- 对平自检 -->
      <view class="checkbar" @click="doCheck">
        <text>对平自检</text>
        <text v-if="diff !== null" :class="diff === 0 ? 'ok' : 'bad'">
          差额 {{ money(diff) }} {{ diff === 0 ? '(已对平)' : '(异常!)' }}
        </text>
        <text v-else class="muted">点击校验 ›</text>
      </view>

      <!-- 流水 -->
      <view class="flow-title">资金流水</view>
      <view class="item" v-for="it in list" :key="it.id">
        <view class="l">
          <text class="biz">{{ it.remark || it.bizType }}</text>
          <text class="time">{{ it.createTime }}</text>
        </view>
        <view class="r">
          <text :class="it.direction === 1 ? 'in' : 'out'">
            {{ it.direction === 1 ? '+' : '-' }}{{ money(it.amount) }}
          </text>
          <text class="after">余 {{ money(it.balanceAfter) }}</text>
        </view>
      </view>

      <view v-if="loading" class="foot">加载中…</view>
      <view v-else-if="finished && list.length" class="foot">没有更多了</view>
      <view v-else-if="!list.length" class="foot">暂无流水</view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { request } from '../../utils/request';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import {
  apiGetWallet,
  apiCheckInvariant,
  apiCreateRecharge,
  apiSimulatePaid,
  type AppFundFlowVo,
  type AppWalletVo
} from '../../api/wallet';

const authStore = useAuthStore();

const wallet = reactive<AppWalletVo>({ id: 0, userId: 0, balance: 0, frozen: 0, totalRecharge: 0 });
const amount = ref('');
const paying = ref(false);
const diff = ref<number | null>(null);

const { list, loading, finished, reload, loadMore, onRefresh } = useList<AppFundFlowVo>({
  fetch: (params) => request<AppFundFlowVo[]>({ url: '/app/wallet/flow/page', params }),
  pageSize: 10,
  immediate: false
});

function money(v: number | string | undefined | null): string {
  return Number(v || 0).toFixed(2);
}

async function refreshWallet() {
  const res = await apiGetWallet();
  Object.assign(wallet, res.data);
}

async function doRecharge() {
  const amt = Number(amount.value);
  if (!amt || amt <= 0) {
    uni.showToast({ title: '请输入正确金额', icon: 'none' });
    return;
  }
  paying.value = true;
  try {
    const res = await apiCreateRecharge(amt);
    const data = res.data!;
    if (data.alipayConfigured) {
      // 已配置支付宝：payForm 为支付表单 HTML，需在 H5/WebView 中渲染跳转支付，
      // 支付完成后调用 apiQueryRecharge(outTradeNo) 查单结算。此处模板仅提示。
      uni.showToast({ title: '请在支付宝完成支付后查单', icon: 'none' });
    } else {
      // 未配置支付宝：走模拟即时到账，便于验证 充值→对平
      await apiSimulatePaid(data.outTradeNo);
      uni.showToast({ title: '充值成功(模拟到账)', icon: 'success' });
    }
    amount.value = '';
    await refreshWallet();
    await reload();
    diff.value = null;
  } finally {
    paying.value = false;
  }
}

async function doCheck() {
  const res = await apiCheckInvariant();
  diff.value = Number(res.data || 0);
}

async function loadAll() {
  await refreshWallet();
  await reload();
}

onShow(() => {
  if (authStore.isLogin) loadAll();
});
onReachBottom(() => {
  if (authStore.isLogin) loadMore();
});
onPullDownRefresh(async () => {
  if (authStore.isLogin) await Promise.all([refreshWallet(), onRefresh()]);
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
.card {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 20rpx;
  padding: 40rpx;
  color: #fff;
  .label {
    font-size: 26rpx;
    opacity: 0.9;
  }
  .balance {
    display: block;
    font-size: 64rpx;
    font-weight: 700;
    margin: 12rpx 0;
  }
  .sub {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    opacity: 0.9;
  }
}
.recharge {
  display: flex;
  gap: 16rpx;
  margin: 24rpx 0;
  .input {
    flex: 1;
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
  }
  .btn {
    background: #409eff;
    color: #fff;
    font-size: 28rpx;
    border-radius: 12rpx;
  }
}
.checkbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  font-size: 28rpx;
  color: #303133;
  .ok {
    color: #67c23a;
  }
  .bad {
    color: #f56c6c;
  }
  .muted {
    color: #909399;
    font-size: 26rpx;
  }
}
.flow-title {
  font-size: 28rpx;
  color: #606266;
  margin: 8rpx 0 16rpx;
}
.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 16rpx;
  .l {
    display: flex;
    flex-direction: column;
    .biz {
      font-size: 28rpx;
      color: #303133;
    }
    .time {
      font-size: 22rpx;
      color: #909399;
      margin-top: 6rpx;
    }
  }
  .r {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    .in {
      font-size: 30rpx;
      color: #67c23a;
    }
    .out {
      font-size: 30rpx;
      color: #f56c6c;
    }
    .after {
      font-size: 22rpx;
      color: #909399;
      margin-top: 6rpx;
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

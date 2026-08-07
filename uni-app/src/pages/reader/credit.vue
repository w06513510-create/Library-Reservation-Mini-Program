<template>
  <view class="page">
    <view class="hero">
      <view class="hero-n">{{ profile?.creditScore ?? '—' }}</view>
      <view class="hero-l">当前信用分（满分 100）</view>
      <view class="hero-bar"><view class="hero-fill" :style="{ width: (profile?.creditScore ?? 0) + '%' }"></view></view>
    </view>

    <view class="sec-t">信用流水</view>
    <view class="logs">
      <view v-for="lg in logs" :key="lg.id" class="log">
        <view class="log-l">
          <view class="log-r">{{ lg.reasonDesc || reasonText(lg.reasonType) }}</view>
          <view class="log-t">{{ (lg.createTime || '').slice(0, 16) }}</view>
        </view>
        <view class="log-d" :class="lg.delta >= 0 ? 'up' : 'down'">{{ lg.delta >= 0 ? '+' + lg.delta : lg.delta }}</view>
        <view class="log-a">{{ lg.scoreAfter }}分</view>
      </view>
      <view v-if="loaded && logs.length === 0" class="empty">暂无信用流水</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { readerApi, type ReaderVo, type CreditLogVo } from '../../api/library';

const profile = ref<ReaderVo | null>(null);
const logs = ref<CreditLogVo[]>([]);
const loaded = ref(false);

const reasons: Record<number, string> = {
  1: '建档', 2: '座位爽约', 3: '暂离超时', 4: '监督未落座', 5: '未签退',
  6: '图书逾期', 7: '预约架超期', 8: '遗失损坏', 9: '按时履约', 10: '时间衰减', 11: '申诉冲正', 12: '黑名单校准'
};
function reasonText(t: number) { return reasons[t] || '信用变动'; }

async function load() {
  try {
    const [p, l] = await Promise.all([readerApi.profile(), readerApi.creditLogs()]);
    profile.value = p.data || null;
    logs.value = l.data || [];
  } catch { /* toast in request */ } finally { loaded.value = true; }
}

onShow(load);
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh(); });
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 40rpx; }
.hero {
  background: linear-gradient(135deg, #d9714e, #e5946f);
  color: #fff;
  padding: 60rpx 40rpx 48rpx;
  text-align: center;
  .hero-n { font-size: 96rpx; font-weight: 800; line-height: 1; }
  .hero-l { font-size: 26rpx; opacity: 0.92; margin-top: 16rpx; }
  .hero-bar { margin-top: 28rpx; height: 14rpx; background: rgba(255, 255, 255, 0.28); border-radius: 100rpx; overflow: hidden; }
  .hero-fill { height: 100%; background: #fff; border-radius: 100rpx; }
}
.sec-t { padding: 32rpx 32rpx 12rpx; font-size: 28rpx; font-weight: 700; color: #6b6259; }
.logs { margin: 0 24rpx; background: #fff; border-radius: 20rpx; overflow: hidden; }
.log {
  display: flex; align-items: center; padding: 28rpx 32rpx;
  border-top: 1rpx solid #f5f1eb;
  &:first-child { border-top: none; }
  .log-l { flex: 1;
    .log-r { font-size: 28rpx; color: #3a332e; }
    .log-t { font-size: 22rpx; color: #b3aaa1; margin-top: 8rpx; }
  }
  .log-d { font-size: 34rpx; font-weight: 700; width: 120rpx; text-align: right;
    &.up { color: #4d8a5e; } &.down { color: #cf5b4e; }
  }
  .log-a { width: 110rpx; text-align: right; font-size: 24rpx; color: #b3aaa1; }
}
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 80rpx 0; }
</style>

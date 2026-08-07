<template>
  <view class="page">
    <view class="tabs">
      <view
        v-for="t in tabs"
        :key="t.label"
        :class="['tab', t.val === status ? 'tab--on' : '']"
        @click="switchTab(t.val)"
      >{{ t.label }}</view>
    </view>

    <view class="list">
      <view v-for="r in list" :key="r.id" class="item">
        <view class="item-h">
          <text class="seat">座位 {{ r.seatNo || r.seatId }}</text>
          <text :class="['st', 'st--' + r.status]">{{ statusText(r.status) }}</text>
        </view>
        <view class="item-m">🗓 {{ dateOf(r.startTime) }}　⏰ {{ timeOf(r.startTime) }}–{{ timeOf(r.endTime) }}</view>
        <view class="acts" v-if="actionsOf(r.status).length">
          <button
            v-for="a in actionsOf(r.status)"
            :key="a.key"
            :class="['act', a.danger ? 'act--danger' : '']"
            @click="doAction(a.key, r)"
          >{{ a.label }}</button>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty">
        <view class="empty-ic">🪑</view>
        <view>暂无预约记录</view>
        <button class="empty-btn" @click="goSeat">去选座</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { useList } from '../../composables/useList';
import { seatApi, type ReservationVo } from '../../api/library';

const tabs = [
  { label: '全部', val: undefined as number | undefined },
  { label: '待签到', val: 0 },
  { label: '使用中', val: 1 },
  { label: '已完成', val: 3 }
];
const status = ref<number | undefined>(undefined);

const { list, loading, reload, loadMore, onRefresh } = useList<ReservationVo>({
  fetch: (p) => seatApi.myReservations(p),
  extraParams: () => (status.value === undefined ? {} : { status: status.value })
});

function switchTab(v: number | undefined) {
  if (status.value === v) return;
  status.value = v;
  reload();
}

const statusMap: Record<number, string> = { 0: '待签到', 1: '使用中', 2: '暂离中', 3: '已完成', 4: '已取消', 5: '已违约' };
function statusText(s: number) { return statusMap[s] || '—'; }
function dateOf(t?: string) { return (t || '').slice(0, 10); }
function timeOf(t?: string) { return (t || '').slice(11, 16); }

function actionsOf(s: number) {
  if (s === 0) return [{ key: 'checkIn', label: '签到' }, { key: 'cancel', label: '取消', danger: true }];
  if (s === 1) return [{ key: 'away', label: '暂离' }, { key: 'leave', label: '退座', danger: true }];
  if (s === 2) return [{ key: 'back', label: '返回' }, { key: 'leave', label: '退座', danger: true }];
  return [] as { key: string; label: string; danger?: boolean }[];
}

const fnMap: Record<string, (id: number) => Promise<any>> = {
  checkIn: seatApi.checkIn, away: seatApi.away, back: seatApi.back, leave: seatApi.leave, cancel: seatApi.cancel
};
const confirmable = ['leave', 'cancel'];

function doAction(key: string, r: ReservationVo) {
  const run = async () => {
    try {
      await fnMap[key](r.id);
      uni.showToast({ title: '操作成功', icon: 'success' });
      reload();
    } catch (e) { /* toast in request.ts */ }
  };
  if (confirmable.includes(key)) {
    uni.showModal({ title: '确认操作', content: key === 'leave' ? '确认退座/签退？' : '确认取消该预约？', success: (m) => m.confirm && run() });
  } else {
    run();
  }
}

function goSeat() { uni.switchTab({ url: '/pages/seat/select' }); }

onReachBottom(() => loadMore());
onPullDownRefresh(async () => { await onRefresh(); uni.stopPullDownRefresh(); });
</script>

<style lang="scss">
.page { min-height: 100vh; }
.tabs {
  display: flex;
  background: #fff;
  padding: 8rpx 16rpx;
  position: sticky;
  top: 0;
  z-index: 2;
}
.tab {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #6b6259;
  padding: 20rpx 0;
  &--on { color: #d9714e; font-weight: 700; border-bottom: 4rpx solid #d9714e; }
}
.list { padding: 24rpx; }
.item {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05);
}
.item-h { display: flex; align-items: center; justify-content: space-between; }
.seat { font-size: 32rpx; font-weight: 700; color: #3a332e; }
.st {
  font-size: 22rpx;
  padding: 6rpx 18rpx;
  border-radius: 100rpx;
  &--0 { background: #fbf1e2; color: #c98a2e; }
  &--1 { background: #eaf3ec; color: #4d8a5e; }
  &--2 { background: #eef0fa; color: #5b6bb0; }
  &--3 { background: #eee; color: #999; }
  &--4 { background: #eee; color: #999; }
  &--5 { background: #fbe4df; color: #cf5b4e; }
}
.item-m { margin-top: 16rpx; font-size: 26rpx; color: #9a938c; }
.acts { margin-top: 24rpx; display: flex; gap: 16rpx; justify-content: flex-end; }
.act {
  margin: 0;
  font-size: 26rpx;
  padding: 8rpx 32rpx;
  line-height: 1.9;
  background: #fdeee7;
  color: #d9714e;
  border-radius: 100rpx;
  &--danger { background: #fff; color: #9a938c; border: 2rpx solid #e6e0da; }
  &::after { border: none; }
}
.empty { text-align: center; padding: 100rpx 0; color: #b3aaa1; font-size: 26rpx;
  .empty-ic { font-size: 80rpx; }
  .empty-btn { margin: 32rpx auto 0; width: 220rpx; background: #d9714e; color: #fff; border-radius: 100rpx; font-size: 28rpx; &::after { border: none; } }
}
</style>

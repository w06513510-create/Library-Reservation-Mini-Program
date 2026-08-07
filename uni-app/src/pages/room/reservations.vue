<template>
  <view class="page">
    <view class="list">
      <view v-for="r in list" :key="r.id" class="item">
        <view class="item-h">
          <text class="rn">研讨间 #{{ r.roomId }}</text>
          <text :class="['st', 'st--' + r.status]">{{ statusText(r.status) }}</text>
        </view>
        <view class="item-m">🗓 {{ dateOf(r.startTime) }}　⏰ {{ timeOf(r.startTime) }}–{{ timeOf(r.endTime) }}　👥 {{ r.userCount || '-' }} 人</view>
        <view v-if="r.status === 5 && r.rejectReason" class="reject">驳回原因：{{ r.rejectReason }}</view>
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
        <view class="empty-ic">👥</view>
        <view>暂无研讨间预约</view>
        <button class="empty-btn" @click="goSelect">去预约</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import { useList } from '../../composables/useList';
import { roomApi, type RoomReservationVo } from '../../api/library';

const { list, loading, reload, loadMore, onRefresh } = useList<RoomReservationVo>({
  fetch: (p) => roomApi.myReservations(p)
});

const statusMap: Record<number, string> = { 0: '待审批', 1: '待使用', 2: '使用中', 3: '已完成', 4: '已取消', 5: '已驳回', 6: '已违约' };
function statusText(s: number) { return statusMap[s] || '—'; }
function dateOf(t?: string) { return (t || '').slice(0, 10); }
function timeOf(t?: string) { return (t || '').slice(11, 16); }

function actionsOf(s: number) {
  if (s === 0) return [{ key: 'cancel', label: '取消', danger: true }];
  if (s === 1) return [{ key: 'checkIn', label: '签到' }, { key: 'cancel', label: '取消', danger: true }];
  return [] as { key: string; label: string; danger?: boolean }[];
}

const fnMap: Record<string, (id: number) => Promise<any>> = { checkIn: roomApi.checkIn, cancel: roomApi.cancel };

function doAction(key: string, r: RoomReservationVo) {
  const run = async () => {
    try { await fnMap[key](r.id); uni.showToast({ title: '操作成功', icon: 'success' }); reload(); } catch (e) { /* toast */ }
  };
  if (key === 'cancel') {
    uni.showModal({ title: '确认操作', content: '确认取消该研讨间预约？', success: (m) => m.confirm && run() });
  } else {
    run();
  }
}

function goSelect() { uni.navigateTo({ url: '/pages/room/select' }); }

onReachBottom(() => loadMore());
onPullDownRefresh(async () => { await onRefresh(); uni.stopPullDownRefresh(); });
</script>

<style lang="scss">
.page { min-height: 100vh; }
.list { padding: 24rpx; }
.item { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.item-h { display: flex; align-items: center; justify-content: space-between; }
.rn { font-size: 32rpx; font-weight: 700; color: #3a332e; }
.st { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 100rpx;
  &--0 { background: #fbf1e2; color: #c98a2e; }
  &--1 { background: #eaf3ec; color: #4d8a5e; }
  &--2 { background: #eef0fa; color: #5b6bb0; }
  &--3 { background: #eee; color: #999; }
  &--4 { background: #eee; color: #999; }
  &--5 { background: #fbe4df; color: #cf5b4e; }
  &--6 { background: #fbe4df; color: #cf5b4e; }
}
.item-m { margin-top: 16rpx; font-size: 26rpx; color: #9a938c; }
.reject { margin-top: 10rpx; font-size: 24rpx; color: #cf5b4e; }
.acts { margin-top: 24rpx; display: flex; gap: 16rpx; justify-content: flex-end; }
.act { margin: 0; font-size: 26rpx; padding: 8rpx 32rpx; line-height: 1.9; background: #fdeee7; color: #d9714e; border-radius: 100rpx;
  &--danger { background: #fff; color: #9a938c; border: 2rpx solid #e6e0da; }
  &::after { border: none; }
}
.empty { text-align: center; padding: 100rpx 0; color: #b3aaa1; font-size: 26rpx;
  .empty-ic { font-size: 80rpx; }
  .empty-btn { margin: 32rpx auto 0; width: 220rpx; background: #d9714e; color: #fff; border-radius: 100rpx; font-size: 28rpx; &::after { border: none; } }
}
</style>

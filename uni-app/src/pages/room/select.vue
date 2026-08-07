<template>
  <view class="page">
    <!-- 日期 + 时段（中国时间；今天只放尚未开始的时段） -->
    <view class="bar">
      <picker mode="date" :value="date" :start="today" :end="maxDate" @change="onDate">
        <view class="picker">📅 {{ date }} <text class="caret">▾</text></view>
      </picker>
      <view class="slots">
        <view
          v-for="(s, i) in slots"
          :key="i"
          :class="['slot', i === slotIdx ? 'slot--on' : '', slotDisabled(i) ? 'slot--off' : '']"
          @click="pickSlot(i)"
        >{{ s.label }}</view>
      </view>
    </view>
    <view v-if="noBookableToday" class="note note--warn">今日已无可约时段，请选择明天或之后的日期</view>
    <view v-else class="note">选定日期与时段后，点研讨间「预约」。需审批的研讨间约后等管理员通过。</view>

    <!-- 研讨间列表 -->
    <view class="list">
      <view v-for="r in rooms" :key="r.id" class="room">
        <view class="room-l">
          <view class="room-n">{{ r.roomName }}</view>
          <view class="room-m">
            <text class="tag">可容 {{ r.capacity }} 人</text>
            <text class="tag">最少 {{ r.minUsers }} 人</text>
            <text v-if="r.needApprove === 1" class="tag tag--warn">需审批</text>
          </view>
        </view>
        <button class="room-btn" :disabled="noBookableToday" @click="doReserve(r)">预约</button>
      </view>
      <view v-if="loaded && rooms.length === 0" class="empty">暂无研讨间</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { roomApi, type RoomVo } from '../../api/library';
import { getToken } from '../../utils/request';
import { chinaToday, chinaDatePlus, chinaMinutesOfDay, hmToMinutes } from '../../utils/datetime';

const slots = [
  { label: '上午', start: '08:00:00', end: '12:00:00' },
  { label: '下午', start: '14:00:00', end: '18:00:00' },
  { label: '晚间', start: '18:30:00', end: '22:00:00' }
];
const today = chinaToday();
const maxDate = chinaDatePlus(2);
const date = ref(today);
const slotIdx = ref(0);
const rooms = ref<RoomVo[]>([]);
const loaded = ref(false);

function slotDisabled(i: number): boolean {
  if (date.value > today) return false;
  if (date.value < today) return true;
  return chinaMinutesOfDay() >= hmToMinutes(slots[i].start);
}
function firstEnabledSlot(): number {
  for (let i = 0; i < slots.length; i++) if (!slotDisabled(i)) return i;
  return -1;
}
const noBookableToday = computed(() => date.value === today && firstEnabledSlot() < 0);

async function loadRooms() {
  try { rooms.value = (await roomApi.list()).data || []; } catch { /* toast */ } finally { loaded.value = true; }
}
function pickSlot(i: number) {
  if (slotDisabled(i)) { uni.showToast({ title: '该时段已开始或已过，不可预约', icon: 'none' }); return; }
  slotIdx.value = i;
}
function onDate(e: any) {
  date.value = e.detail.value;
  if (slotDisabled(slotIdx.value)) { const f = firstEnabledSlot(); if (f >= 0) slotIdx.value = f; }
}

function doReserve(r: RoomVo) {
  if (!getToken()) { uni.navigateTo({ url: '/pages/login/login' }); return; }
  if (noBookableToday.value) return;
  uni.showModal({
    title: `预约 ${r.roomName}`,
    editable: true,
    placeholderText: `使用人数（${r.minUsers}-${r.capacity}）`,
    content: String(r.minUsers),
    success: async (m) => {
      if (!m.confirm) return;
      const n = parseInt((m.content || '').trim(), 10);
      if (!n || n < r.minUsers || n > r.capacity) {
        uni.showToast({ title: `人数需 ${r.minUsers}-${r.capacity} 人`, icon: 'none' });
        return;
      }
      const s = slots[slotIdx.value];
      try {
        await roomApi.reserve({
          roomId: r.id,
          reserveDate: `${date.value} 00:00:00`,
          startTime: `${date.value} ${s.start}`,
          endTime: `${date.value} ${s.end}`,
          userCount: n
        });
        uni.showToast({ title: r.needApprove === 1 ? '已提交，待审批' : '预约成功', icon: 'success' });
        setTimeout(() => uni.navigateTo({ url: '/pages/room/reservations' }), 700);
      } catch (e) { /* toast in request.ts */ }
    }
  });
}

onShow(loadRooms);
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 40rpx; }
.bar { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 24rpx; gap: 20rpx; background: #fff; }
.picker { font-size: 26rpx; color: #3a332e; background: #f5f3ee; border-radius: 12rpx; padding: 14rpx 22rpx; .caret { color: #b3aaa1; } }
.slots { display: flex; gap: 12rpx; }
.slot { font-size: 26rpx; padding: 14rpx 24rpx; border-radius: 12rpx; background: #f5f3ee; color: #6b6259;
  &--on { background: #fdeee7; color: #d9714e; font-weight: 700; }
  &--off { opacity: 0.38; text-decoration: line-through; }
}
.note { padding: 16rpx 28rpx; font-size: 22rpx; color: #b3aaa1; &--warn { color: #cf5b4e; } }
.list { padding: 0 24rpx; }
.room {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx;
  box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05);
}
.room-l { flex: 1; }
.room-n { font-size: 32rpx; font-weight: 700; color: #3a332e; }
.room-m { margin-top: 14rpx; display: flex; flex-wrap: wrap; gap: 12rpx; }
.tag { font-size: 22rpx; color: #6b6259; background: #f5f3ee; border-radius: 100rpx; padding: 6rpx 18rpx;
  &--warn { background: #fbf1e2; color: #c98a2e; }
}
.room-btn { margin: 0; width: 160rpx; background: #d9714e; color: #fff; font-size: 28rpx; border-radius: 100rpx;
  &[disabled] { background: #e6c9bd; } &::after { border: none; }
}
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 90rpx 0; }
</style>

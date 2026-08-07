<template>
  <view class="page">
    <!-- 楼层切换 -->
    <scroll-view scroll-x class="floors" :show-scrollbar="false">
      <view
        v-for="f in floors"
        :key="f.id"
        :class="['floor', f.id === floorId ? 'floor--on' : '']"
        @click="pickFloor(f.id)"
      >{{ f.floorName }}</view>
    </scroll-view>

    <!-- 日期 + 时段 -->
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

    <!-- 开放时段提示（中国时间；仅可约今日及未来、尚未开始的时段） -->
    <view v-if="noBookableToday" class="note note--warn">今日已无可约时段，请选择明天或之后的日期</view>
    <view v-else class="note">开放时段 08:00–22:00 · 仅可预约今日及未来、尚未开始的时段（北京时间）</view>

    <!-- 快速选座 -->
    <view v-if="!noBookableToday" class="quick" @click="quickReserve">⚡ 快速选座 · 自动分配一个空座</view>

    <!-- 图例 -->
    <view class="legend">
      <text class="lg"><text class="dot dot--free"></text>可选</text>
      <text class="lg"><text class="dot dot--sel"></text>已选</text>
      <text class="lg"><text class="dot dot--busy"></text>占用/停用</text>
    </view>

    <!-- 座位图（按桌分组） -->
    <view class="map" v-if="!loading">
      <view v-if="groups.length === 0" class="empty">该楼层暂无座位</view>
      <view v-for="g in groups" :key="g.key" class="desk">
        <view class="desk-h">
          <text class="desk-no">{{ g.title }}</text>
          <text class="desk-sub">{{ g.areaName }}</text>
        </view>
        <view class="seats">
          <view
            v-for="s in g.seats"
            :key="s.id"
            :class="['seat', seatCls(s)]"
            @click="tapSeat(s)"
          >{{ s.seatNo }}</view>
        </view>
      </view>
    </view>
    <view v-else class="empty">加载中…</view>

    <!-- 底部约座栏 -->
    <view class="footer">
      <view class="f-info">
        <text v-if="picked">已选 {{ picked.seatNo }} · {{ slots[slotIdx].label }}</text>
        <text v-else class="f-hint">请选择一个可用座位</text>
      </view>
      <button class="f-btn" :disabled="!picked || submitting" :loading="submitting" @click="doReserve">预约</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { seatApi, type FloorVo, type SeatStatusVo } from '../../api/library';
import { getToken } from '../../utils/request';
import { chinaToday, chinaDatePlus, chinaMinutesOfDay, hmToMinutes } from '../../utils/datetime';

const slots = [
  { label: '上午', start: '08:00:00', end: '12:00:00' },
  { label: '下午', start: '14:00:00', end: '18:00:00' },
  { label: '晚间', start: '18:30:00', end: '22:00:00' }
];

// 全部按中国时间(Asia/Shanghai)：可约今天起 3 天(今/明/后)；今天只放"尚未开始"的时段（起始须晚于当前）
const today = chinaToday();
const maxDate = chinaDatePlus(2);
const floors = ref<FloorVo[]>([]);
const floorId = ref<number | null>(null);
const date = ref(today);
const slotIdx = ref(0);
const seats = ref<SeatStatusVo[]>([]);
const picked = ref<SeatStatusVo | null>(null);
const loading = ref(false);
const submitting = ref(false);

const groups = computed(() => {
  const map = new Map<string, { key: string; title: string; areaName: string; seats: SeatStatusVo[] }>();
  for (const s of seats.value) {
    const key = s.deskId ? 'd' + s.deskId : 'loose';
    if (!map.has(key)) {
      map.set(key, {
        key,
        title: s.deskId ? '🪑 ' + (s.deskNo || '工位') : '散座',
        areaName: s.areaName || '',
        seats: []
      });
    }
    map.get(key)!.seats.push(s);
  }
  return Array.from(map.values());
});

/** 某时段在所选日期是否不可约：今天只允许"尚未开始"的时段；过去日期全禁（picker 已限下界） */
function slotDisabled(i: number): boolean {
  if (date.value > today) return false;
  if (date.value < today) return true;
  return chinaMinutesOfDay() >= hmToMinutes(slots[i].start);
}
/** 第一个可约时段下标，无则 -1 */
function firstEnabledSlot(): number {
  for (let i = 0; i < slots.length; i++) if (!slotDisabled(i)) return i;
  return -1;
}
/** 今日是否已无可约时段（晚间也已开始） */
const noBookableToday = computed(() => date.value === today && firstEnabledSlot() < 0);

function seatCls(s: SeatStatusVo) {
  if (picked.value && picked.value.id === s.id) return 'seat--sel';
  if (s.occupied || s.seatStatus !== 0) return 'seat--busy';
  return 'seat--free';
}

async function loadFloors() {
  const res = await seatApi.floors();
  floors.value = res.data || [];
  if (floors.value.length && floorId.value == null) {
    floorId.value = floors.value[0].id;
  }
  await loadSeats();
}

async function loadSeats() {
  if (floorId.value == null) return;
  // 今天：确保选中的是"尚未开始"的时段；已选时段过期则切到首个可约时段
  if (date.value === today && slotDisabled(slotIdx.value)) {
    const f = firstEnabledSlot();
    if (f < 0) { seats.value = []; picked.value = null; loading.value = false; return; } // 今日已无可约时段
    slotIdx.value = f;
  }
  loading.value = true;
  picked.value = null;
  try {
    const s = slots[slotIdx.value];
    const res = await seatApi.status(floorId.value, `${date.value} ${s.start}`, `${date.value} ${s.end}`);
    seats.value = res.data || [];
  } catch (e) {
    seats.value = [];
  } finally {
    loading.value = false;
  }
}

function pickFloor(id: number) { floorId.value = id; loadSeats(); }
function pickSlot(i: number) {
  if (slotDisabled(i)) { uni.showToast({ title: '该时段已开始或已过，不可预约', icon: 'none' }); return; }
  slotIdx.value = i;
  loadSeats();
}
function onDate(e: any) {
  date.value = e.detail.value;
  if (slotDisabled(slotIdx.value)) {
    const f = firstEnabledSlot();
    if (f >= 0) slotIdx.value = f;
  }
  loadSeats();
}

function tapSeat(s: SeatStatusVo) {
  if (s.occupied || s.seatStatus !== 0) {
    uni.showToast({ title: '该座位不可选', icon: 'none' });
    return;
  }
  picked.value = picked.value?.id === s.id ? null : s;
}

/** 快速选座：自动挑当前楼层/时段第一个空座并下单 */
function quickReserve() {
  if (noBookableToday.value) { uni.showToast({ title: '今日已无可约时段', icon: 'none' }); return; }
  const free = seats.value.find((s) => !s.occupied && s.seatStatus === 0);
  if (!free) { uni.showToast({ title: '该时段暂无空座', icon: 'none' }); return; }
  picked.value = free;
  doReserve();
}

async function doReserve() {
  if (!picked.value || submitting.value) return;
  if (!getToken()) {
    uni.navigateTo({ url: '/pages/login/login' });
    return;
  }
  const s = slots[slotIdx.value];
  uni.showModal({
    title: '确认预约',
    content: `${date.value} ${s.label}（${s.start.slice(0, 5)}-${s.end.slice(0, 5)}）\n座位 ${picked.value.seatNo}`,
    success: async (r) => {
      if (!r.confirm) return;
      submitting.value = true;
      try {
        await seatApi.reserve({
          seatId: picked.value!.id,
          reserveDate: `${date.value} 00:00:00`,
          startTime: `${date.value} ${s.start}`,
          endTime: `${date.value} ${s.end}`,
          source: 1
        });
        uni.showToast({ title: '预约成功', icon: 'success' });
        setTimeout(() => uni.navigateTo({ url: '/pages/seat/reservations' }), 700);
        loadSeats();
      } catch (e) {
        // request.ts 已 toast
      } finally {
        submitting.value = false;
      }
    }
  });
}

onShow(() => {
  if (floors.value.length === 0) loadFloors();
  else loadSeats();
});
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 140rpx; }
.floors {
  white-space: nowrap;
  background: #fff;
  padding: 20rpx 16rpx;
}
.floor {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin: 0 8rpx;
  font-size: 28rpx;
  color: #6b6259;
  background: #f5f3ee;
  border-radius: 100rpx;
  &--on { background: #d9714e; color: #fff; }
}
.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  gap: 20rpx;
}
.picker {
  font-size: 26rpx;
  color: #3a332e;
  background: #fff;
  border-radius: 12rpx;
  padding: 14rpx 22rpx;
  .caret { color: #b3aaa1; }
}
.slots { display: flex; gap: 12rpx; }
.slot {
  font-size: 26rpx;
  padding: 14rpx 24rpx;
  border-radius: 12rpx;
  background: #fff;
  color: #6b6259;
  &--on { background: #fdeee7; color: #d9714e; font-weight: 700; }
  &--off { opacity: 0.38; text-decoration: line-through; }
}
.note {
  padding: 4rpx 28rpx 12rpx;
  font-size: 22rpx;
  color: #b3aaa1;
  &--warn { color: #cf5b4e; }
}
.quick {
  margin: 4rpx 24rpx 16rpx;
  text-align: center;
  font-size: 26rpx;
  color: #d9714e;
  background: #fdeee7;
  border: 2rpx dashed #e6a488;
  border-radius: 12rpx;
  padding: 20rpx;
}
.legend {
  display: flex;
  gap: 32rpx;
  padding: 8rpx 28rpx 20rpx;
  .lg { font-size: 24rpx; color: #9a938c; display: flex; align-items: center; }
  .dot {
    width: 22rpx; height: 22rpx; border-radius: 6rpx; margin-right: 10rpx;
    &--free { background: #eaf3ec; border: 2rpx solid #5a9b6b; }
    &--sel { background: #d9714e; }
    &--busy { background: #e6e0da; }
  }
}
.map { padding: 0 24rpx; }
.desk {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.desk-h { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 18rpx; }
.desk-no { font-size: 28rpx; font-weight: 700; color: #3a332e; }
.desk-sub { font-size: 22rpx; color: #b3aaa1; }
.seats { display: flex; flex-wrap: wrap; gap: 16rpx; }
.seat {
  min-width: 120rpx;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 12rpx;
  font-size: 26rpx;
  &--free { background: #eaf3ec; color: #4d8a5e; border: 2rpx solid #bfe0c8; }
  &--sel { background: #d9714e; color: #fff; }
  &--busy { background: #efeae4; color: #b3aaa1; }
}
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 80rpx 0; }
.footer {
  position: fixed;
  left: 0; right: 0; bottom: var(--window-bottom); /* 抬到 tabBar 之上, 避免被底部 tabBar 遮挡拦截点击 */
  background: #fff;
  padding: 20rpx 28rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}
.f-info { flex: 1; font-size: 26rpx; color: #3a332e; }
.f-hint { color: #b3aaa1; }
.f-btn {
  width: 220rpx;
  background: #d9714e;
  color: #fff;
  font-size: 30rpx;
  border-radius: 100rpx;
  &[disabled] { background: #e6c9bd; }
  &::after { border: none; }
}
</style>

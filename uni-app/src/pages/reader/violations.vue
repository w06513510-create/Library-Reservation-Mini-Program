<template>
  <view class="page">
    <view class="tabs">
      <view :class="['tab', tab === 'v' ? 'tab--on' : '']" @click="tab = 'v'">违约记录</view>
      <view :class="['tab', tab === 'a' ? 'tab--on' : '']" @click="tab = 'a'">申诉记录</view>
    </view>

    <!-- 违约 -->
    <view v-if="tab === 'v'" class="list">
      <view v-for="v in violations" :key="v.id" class="item">
        <view class="item-h">
          <text class="vt">{{ vType(v.violationType) }}</text>
          <text :class="['st', v.status === 0 ? 'st--on' : 'st--off']">{{ v.status === 0 ? '有效' : '已解除' }}</text>
        </view>
        <view class="item-m">扣分 <text class="minus">-{{ v.deductScore ?? 0 }}</text>　·　{{ (v.occurTime || '').slice(0, 16) }}</view>
        <view class="acts" v-if="v.status === 0">
          <button class="act" @click="doAppeal(v.id)">发起申诉</button>
        </view>
      </view>
      <view v-if="loaded && violations.length === 0" class="empty">🎉 暂无违约记录，继续保持</view>
    </view>

    <!-- 申诉 -->
    <view v-else class="list">
      <view v-for="a in appeals" :key="a.id" class="item">
        <view class="item-h">
          <text class="vt">申诉 #{{ a.violationId }}</text>
          <text :class="['st', appealCls(a.status)]">{{ appealText(a.status) }}</text>
        </view>
        <view class="item-m" v-if="a.reason">理由：{{ a.reason }}</view>
        <view class="item-m" v-if="a.auditRemark">审批：{{ a.auditRemark }}</view>
      </view>
      <view v-if="loaded && appeals.length === 0" class="empty">暂无申诉记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { readerApi, type ViolationVo } from '../../api/library';

const tab = ref<'v' | 'a'>('v');
const violations = ref<ViolationVo[]>([]);
const appeals = ref<any[]>([]);
const loaded = ref(false);

const vt: Record<number, string> = { 1: '座位爽约', 2: '暂离超时', 3: '监督未落座', 4: '未签退', 5: '图书逾期', 6: '预约架超期', 7: '遗失损坏' };
function vType(t: number) { return vt[t] || '违约'; }
const at: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已驳回' };
function appealText(s: number) { return at[s] || '—'; }
function appealCls(s: number) { return s === 0 ? 'st--pend' : s === 1 ? 'st--off' : 'st--on'; }

async function load() {
  try {
    const [v, a] = await Promise.all([
      readerApi.violations({ pageNum: 1, pageSize: 50 }),
      readerApi.appeals({ pageNum: 1, pageSize: 50 })
    ]);
    violations.value = v.rows || [];
    appeals.value = a.rows || [];
  } catch { /* toast */ } finally { loaded.value = true; }
}

function doAppeal(violationId: number) {
  uni.showModal({
    title: '发起申诉', editable: true, placeholderText: '请填写申诉理由',
    success: async (r) => {
      if (!r.confirm) return;
      try {
        await readerApi.appeal(violationId, (r.content || '').trim());
        uni.showToast({ title: '申诉已提交', icon: 'success' });
        load();
      } catch { /* toast */ }
    }
  });
}

onShow(load);
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh(); });
</script>

<style lang="scss">
.page { min-height: 100vh; }
.tabs { display: flex; background: #fff; padding: 8rpx 16rpx; }
.tab { flex: 1; text-align: center; font-size: 28rpx; color: #6b6259; padding: 20rpx 0;
  &--on { color: #d9714e; font-weight: 700; border-bottom: 4rpx solid #d9714e; }
}
.list { padding: 24rpx; }
.item { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.item-h { display: flex; align-items: center; justify-content: space-between; }
.vt { font-size: 30rpx; font-weight: 700; color: #3a332e; }
.st { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 100rpx;
  &--on { background: #fbe4df; color: #cf5b4e; }
  &--off { background: #eaf3ec; color: #4d8a5e; }
  &--pend { background: #fbf1e2; color: #c98a2e; }
}
.item-m { margin-top: 16rpx; font-size: 26rpx; color: #9a938c; .minus { color: #cf5b4e; font-weight: 700; } }
.acts { margin-top: 22rpx; text-align: right; }
.act { display: inline-block; margin: 0; font-size: 26rpx; padding: 8rpx 36rpx; line-height: 1.9; background: #fdeee7; color: #d9714e; border-radius: 100rpx; &::after { border: none; } }
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 90rpx 0; }
</style>

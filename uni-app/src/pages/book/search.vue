<template>
  <view class="page">
    <view class="seg">
      <view :class="['s', tab === 'search' ? 's--on' : '']" @click="switchTab('search')">检索</view>
      <view :class="['s', tab === 'loans' ? 's--on' : '']" @click="switchTab('loans')">我的借阅</view>
      <view :class="['s', tab === 'holds' ? 's--on' : '']" @click="switchTab('holds')">我的预约</view>
    </view>

    <!-- 检索 -->
    <block v-if="tab === 'search'">
      <view class="searchbar">
        <input class="sinput" v-model="kw" placeholder="书名 / 作者 / ISBN" confirm-type="search" @confirm="doSearch" />
        <view class="sbtn" @click="doSearch">搜索</view>
      </view>
      <view class="list">
        <view v-for="b in books" :key="b.id" class="book">
          <image v-if="b.coverUrl" class="cover" :src="b.coverUrl" mode="aspectFill" />
          <view v-else class="cover cover--ph">📕</view>
          <view class="b-main">
            <view class="b-title">{{ b.title }}</view>
            <view class="b-sub">{{ b.author || '佚名' }}</view>
            <view class="b-meta">索书号 {{ b.callNo || '—' }}</view>
            <view class="b-foot">
              <text :class="['avail', (b.availQty ?? 0) > 0 ? 'ok' : 'no']">
                {{ (b.availQty ?? 0) > 0 ? '可借 ' + b.availQty + ' 册' : '暂无可借' }}
              </text>
              <button v-if="(b.availQty ?? 0) <= 0" class="bhold" @click="doHold(b)">预约</button>
            </view>
          </view>
        </view>
        <view v-if="searched && books.length === 0" class="empty">未找到相关书目</view>
      </view>
    </block>

    <!-- 我的借阅 -->
    <view v-else-if="tab === 'loans'" class="list">
      <view v-for="l in loans" :key="l.id" class="item">
        <view class="item-h"><text class="it">{{ l.bookTitle || '图书 #' + l.id }}</text><text :class="['st', l.status === 2 ? 'st--od' : 'st--on']">{{ loanText(l.status) }}</text></view>
        <view class="item-m">借出 {{ (l.borrowTime || '').slice(0, 10) }}　·　应还 {{ (l.dueTime || '').slice(0, 10) }}</view>
        <view class="acts" v-if="l.status === 0 || l.status === 2"><button class="act" @click="doRenew(l.id)">续借</button></view>
      </view>
      <view v-if="loaded && loans.length === 0" class="empty">暂无借阅记录</view>
    </view>

    <!-- 我的预约(hold) -->
    <view v-else class="list">
      <view v-for="h in holds" :key="h.id" class="item">
        <view class="item-h"><text class="it">{{ h.bookTitle || '图书 #' + h.bookId }}</text><text class="st st--wait">排队 {{ h.queueNo ?? '—' }}</text></view>
        <view class="acts"><button class="act act--ghost" @click="doCancelHold(h.id)">取消预约</button></view>
      </view>
      <view v-if="loaded && holds.length === 0" class="empty">暂无图书预约</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { bookApi, type BookVo, type LoanVo, type HoldVo } from '../../api/library';
import { getToken } from '../../utils/request';

const tab = ref<'search' | 'loans' | 'holds'>('search');
const kw = ref('');
const books = ref<BookVo[]>([]);
const loans = ref<LoanVo[]>([]);
const holds = ref<HoldVo[]>([]);
const searched = ref(false);
const loaded = ref(false);

async function doSearch() {
  try {
    const res = await bookApi.list({ pageNum: 1, pageSize: 50, title: kw.value.trim() || undefined });
    books.value = res.rows || [];
  } catch { /* toast */ } finally { searched.value = true; }
}

async function loadLoans() {
  loaded.value = false;
  try { loans.value = (await bookApi.loans({ pageNum: 1, pageSize: 50 })).rows || []; } catch { /* */ } finally { loaded.value = true; }
}
async function loadHolds() {
  loaded.value = false;
  try { holds.value = (await bookApi.holds({ pageNum: 1, pageSize: 50 })).rows || []; } catch { /* */ } finally { loaded.value = true; }
}

function switchTab(t: 'search' | 'loans' | 'holds') {
  tab.value = t;
  if (t === 'loans') loadLoans();
  else if (t === 'holds') loadHolds();
}

const loanMap: Record<number, string> = { 0: '在借', 1: '已还', 2: '逾期', 3: '预约架', 4: '已取' };
function loanText(s: number) { return loanMap[s] || '—'; }

function needLogin() { if (!getToken()) { uni.navigateTo({ url: '/pages/login/login' }); return true; } return false; }

function doHold(b: BookVo) {
  if (needLogin()) return;
  uni.showModal({ title: '图书预约', content: `预约《${b.title}》，到书后将通知你取书。`, success: async (r) => {
    if (!r.confirm) return;
    try { await bookApi.hold(b.id); uni.showToast({ title: '预约成功', icon: 'success' }); } catch { /* toast */ }
  } });
}
function doRenew(id: number) {
  uni.showModal({ title: '续借', content: '确认续借该图书？', success: async (r) => {
    if (!r.confirm) return;
    try { await bookApi.renew(id); uni.showToast({ title: '续借成功', icon: 'success' }); loadLoans(); } catch { /* toast */ }
  } });
}
function doCancelHold(id: number) {
  uni.showModal({ title: '取消预约', content: '确认取消该图书预约？', success: async (r) => {
    if (!r.confirm) return;
    try { await bookApi.cancelHold(id); uni.showToast({ title: '已取消', icon: 'none' }); loadHolds(); } catch { /* toast */ }
  } });
}

onShow(() => { if (tab.value === 'search' && !searched.value) doSearch(); });
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 40rpx; }
.seg { display: flex; background: #fff; padding: 8rpx 16rpx; position: sticky; top: 0; z-index: 2; }
.s { flex: 1; text-align: center; font-size: 28rpx; color: #6b6259; padding: 20rpx 0;
  &--on { color: #d9714e; font-weight: 700; border-bottom: 4rpx solid #d9714e; }
}
.searchbar { display: flex; gap: 16rpx; padding: 24rpx; }
.sinput { flex: 1; height: 76rpx; background: #fff; border-radius: 100rpx; padding: 0 28rpx; font-size: 28rpx; }
.sbtn { width: 130rpx; height: 76rpx; line-height: 76rpx; text-align: center; background: #d9714e; color: #fff; border-radius: 100rpx; font-size: 28rpx; }
.list { padding: 0 24rpx; }
.book { display: flex; background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.cover { width: 120rpx; height: 160rpx; border-radius: 12rpx; flex-shrink: 0; background: #f5f1eb;
  &--ph { display: flex; align-items: center; justify-content: center; font-size: 56rpx; }
}
.b-main { flex: 1; margin-left: 24rpx; display: flex; flex-direction: column; }
.b-title { font-size: 30rpx; font-weight: 700; color: #3a332e; }
.b-sub { font-size: 24rpx; color: #9a938c; margin-top: 8rpx; }
.b-meta { font-size: 22rpx; color: #b3aaa1; margin-top: 8rpx; }
.b-foot { margin-top: auto; display: flex; align-items: center; justify-content: space-between; padding-top: 12rpx; }
.avail { font-size: 24rpx; &.ok { color: #4d8a5e; } &.no { color: #cf5b4e; } }
.bhold { margin: 0; font-size: 24rpx; padding: 6rpx 30rpx; line-height: 1.9; background: #fdeee7; color: #d9714e; border-radius: 100rpx; &::after { border: none; } }
.item { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.item-h { display: flex; align-items: center; justify-content: space-between; }
.it { font-size: 30rpx; font-weight: 700; color: #3a332e; flex: 1; }
.st { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 100rpx; margin-left: 16rpx;
  &--on { background: #eaf3ec; color: #4d8a5e; }
  &--od { background: #fbe4df; color: #cf5b4e; }
  &--wait { background: #fbf1e2; color: #c98a2e; }
}
.item-m { margin-top: 16rpx; font-size: 26rpx; color: #9a938c; }
.acts { margin-top: 22rpx; text-align: right; }
.act { display: inline-block; margin: 0; font-size: 26rpx; padding: 8rpx 36rpx; line-height: 1.9; background: #fdeee7; color: #d9714e; border-radius: 100rpx; &::after { border: none; }
  &--ghost { background: #fff; color: #9a938c; border: 2rpx solid #e6e0da; }
}
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 90rpx 0; }
</style>

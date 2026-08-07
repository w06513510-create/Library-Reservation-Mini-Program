<template>
  <view class="page">
    <!-- 荐购表单 -->
    <view class="form">
      <view class="ft">我要荐购</view>
      <view class="field"><text class="lb">书名 *</text><input class="ipt" v-model="form.title" placeholder="必填" /></view>
      <view class="field"><text class="lb">著者</text><input class="ipt" v-model="form.author" placeholder="选填" /></view>
      <view class="field"><text class="lb">ISBN</text><input class="ipt" v-model="form.isbn" placeholder="选填" /></view>
      <view class="field"><text class="lb">理由</text><textarea class="ipt ipt--area" v-model="form.reason" placeholder="推荐理由（选填）" /></view>
      <button class="submit" :loading="submitting" :disabled="submitting" @click="submit">提交荐购</button>
    </view>

    <!-- 我的荐购 -->
    <view class="sec-t">我的荐购</view>
    <view class="list">
      <view v-for="s in list" :key="s.id" class="item">
        <view class="item-h">
          <text class="tt">{{ s.title }}</text>
          <text :class="['st', 'st--' + s.status]">{{ statusText(s.status) }}</text>
        </view>
        <view class="item-m" v-if="s.author || s.isbn">{{ s.author || '' }}<text v-if="s.isbn"> · {{ s.isbn }}</text></view>
        <view class="item-r" v-if="s.reason">{{ s.reason }}</view>
        <view class="item-j" v-if="s.status === 2 && s.rejectReason">驳回：{{ s.rejectReason }}</view>
      </view>
      <view v-if="loaded && list.length === 0" class="empty">还没有荐购记录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { readerApi, type PurchaseSuggestVo } from '../../api/library';

const form = reactive({ title: '', author: '', isbn: '', reason: '' });
const submitting = ref(false);
const list = ref<PurchaseSuggestVo[]>([]);
const loaded = ref(false);

const statusMap: Record<number, string> = { 0: '待受理', 1: '已受理', 2: '已驳回', 3: '已采购' };
function statusText(s: number) { return statusMap[s] || '—'; }

async function load() {
  try { list.value = (await readerApi.suggests({ pageNum: 1, pageSize: 50 })).rows || []; } catch { /* */ } finally { loaded.value = true; }
}

async function submit() {
  if (submitting.value) return;
  if (!form.title.trim()) { uni.showToast({ title: '请填写书名', icon: 'none' }); return; }
  submitting.value = true;
  try {
    await readerApi.suggest({ title: form.title.trim(), author: form.author.trim(), isbn: form.isbn.trim(), reason: form.reason.trim() });
    uni.showToast({ title: '荐购已提交', icon: 'success' });
    form.title = ''; form.author = ''; form.isbn = ''; form.reason = '';
    load();
  } catch (e) { /* toast */ } finally { submitting.value = false; }
}

onShow(load);
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh(); });
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 40rpx; }
.form { margin: 24rpx; background: #fff; border-radius: 20rpx; padding: 32rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.ft { font-size: 32rpx; font-weight: 700; color: #3a332e; margin-bottom: 24rpx; }
.field { margin-bottom: 24rpx; }
.lb { display: block; font-size: 26rpx; color: #6b6259; margin-bottom: 12rpx; }
.ipt { background: #f5f3ee; border-radius: 12rpx; padding: 20rpx 24rpx; font-size: 28rpx; width: 100%; box-sizing: border-box;
  &--area { height: 140rpx; }
}
.submit { margin-top: 8rpx; background: #d9714e; color: #fff; font-size: 30rpx; border-radius: 12rpx; &[disabled] { background: #e6c9bd; } &::after { border: none; } }
.sec-t { padding: 24rpx 32rpx 12rpx; font-size: 28rpx; font-weight: 700; color: #6b6259; }
.list { padding: 0 24rpx; }
.item { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 6rpx 18rpx rgba(160, 90, 60, 0.05); }
.item-h { display: flex; align-items: center; justify-content: space-between; }
.tt { font-size: 30rpx; font-weight: 700; color: #3a332e; flex: 1; }
.st { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 100rpx; margin-left: 16rpx;
  &--0 { background: #fbf1e2; color: #c98a2e; }
  &--1 { background: #eef0fa; color: #5b6bb0; }
  &--2 { background: #fbe4df; color: #cf5b4e; }
  &--3 { background: #eaf3ec; color: #4d8a5e; }
}
.item-m { margin-top: 12rpx; font-size: 24rpx; color: #9a938c; }
.item-r { margin-top: 12rpx; font-size: 26rpx; color: #6b6259; line-height: 1.6; }
.item-j { margin-top: 10rpx; font-size: 24rpx; color: #cf5b4e; }
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 80rpx 0; }
</style>

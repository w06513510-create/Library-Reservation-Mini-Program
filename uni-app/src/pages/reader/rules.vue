<template>
  <view class="page">
    <view class="intro">以下为图书馆座位预约与借阅的现行规则，请知悉后使用。</view>
    <view class="list">
      <view v-for="r in rules" :key="r.id" class="row">
        <view class="row-l">
          <view class="row-n">{{ r.ruleName || r.ruleKey }}</view>
          <view class="row-r" v-if="r.remark">{{ r.remark }}</view>
        </view>
        <view class="row-v">{{ r.ruleValue }}<text class="unit" v-if="r.unit"> {{ r.unit }}</text></view>
      </view>
      <view v-if="loaded && rules.length === 0" class="empty">暂无规则配置</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { readerApi, type RuleConfigVo } from '../../api/library';

const rules = ref<RuleConfigVo[]>([]);
const loaded = ref(false);

async function load() {
  try { rules.value = (await readerApi.rules()).data || []; } catch { /* toast */ } finally { loaded.value = true; }
}
onShow(load);
</script>

<style lang="scss">
.page { min-height: 100vh; padding-bottom: 40rpx; }
.intro { padding: 32rpx; font-size: 26rpx; color: #9a938c; line-height: 1.7; }
.list { margin: 0 24rpx; background: #fff; border-radius: 20rpx; overflow: hidden; }
.row {
  display: flex; align-items: center; padding: 28rpx 32rpx;
  border-top: 1rpx solid #f5f1eb;
  &:first-child { border-top: none; }
  .row-l { flex: 1;
    .row-n { font-size: 28rpx; color: #3a332e; }
    .row-r { font-size: 22rpx; color: #b3aaa1; margin-top: 8rpx; line-height: 1.5; }
  }
  .row-v { font-size: 30rpx; font-weight: 700; color: #d9714e; margin-left: 20rpx;
    .unit { font-size: 22rpx; color: #9a938c; font-weight: 400; }
  }
}
.empty { text-align: center; color: #b3aaa1; font-size: 26rpx; padding: 80rpx 0; }
</style>

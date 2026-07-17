<template>
  <view class="chat">
    <scroll-view class="scroll" scroll-y :scroll-into-view="anchor" :scroll-with-animation="false">
      <view v-if="!finished && ordered.length" class="load-earlier" @click="loadMore">加载更早消息</view>
      <view
        class="msg"
        :class="me === m.fromId ? 'msg--mine' : 'msg--peer'"
        v-for="m in ordered"
        :key="m.id"
        :id="'m' + m.id"
      >
        <view class="bubble">{{ m.content }}</view>
      </view>
      <view v-if="!ordered.length && !loading" class="empty">开始聊天吧</view>
      <view id="bottom" />
    </scroll-view>

    <view class="input-bar">
      <input class="ipt" v-model="draft" placeholder="输入消息…" confirm-type="send" @confirm="doSend" />
      <button class="btn-send" :disabled="!draft.trim()" @click="doSend">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import { useAuthStore } from '../../store/auth';
import { useList } from '../../composables/useList';
import { apiChat, apiSendMessage, type MessageVo } from '../../api/message';

const authStore = useAuthStore();
const me = computed(() => authStore.user.id);

const peerId = ref<number>(0);
const draft = ref('');
const anchor = ref('');

const { list, loading, finished, reload, loadMore } = useList<MessageVo>({
  fetch: (params) => apiChat({ ...params, peerId: peerId.value }),
  pageSize: 20,
  immediate: false
});

// 后端按时间倒序返回（page1=最新），展示时翻转为时间正序（旧在上、新在下）
const ordered = computed(() => [...list.value].reverse());

onLoad((opts: any) => {
  peerId.value = Number(opts?.peerId || 0);
  const name = opts?.peerNickname ? decodeURIComponent(opts.peerNickname) : '聊天';
  uni.setNavigationBarTitle({ title: name });
  reloadAndScroll();
});

// 触底=最底部（最新），这里不额外加载；加载更早由顶部按钮触发
onReachBottom(() => {});

async function reloadAndScroll() {
  await reload();
  scrollToBottom();
}

function scrollToBottom() {
  nextTick(() => {
    anchor.value = '';
    setTimeout(() => (anchor.value = 'bottom'), 50);
  });
}

async function doSend() {
  const content = draft.value.trim();
  if (!content) return;
  try {
    await apiSendMessage(peerId.value, content);
    draft.value = '';
    await reload();
    scrollToBottom();
  } catch (e) {
    // 错误已统一提示
  }
}
</script>

<style lang="scss">
.chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}
.scroll {
  flex: 1;
  padding: 24rpx;
  box-sizing: border-box;
}
.load-earlier {
  text-align: center;
  font-size: 24rpx;
  color: #909399;
  padding: 12rpx 0 24rpx;
}
.empty {
  text-align: center;
  color: #c0c4cc;
  font-size: 26rpx;
  padding-top: 120rpx;
}
.msg {
  display: flex;
  margin-bottom: 24rpx;
  .bubble {
    max-width: 70%;
    padding: 20rpx 24rpx;
    border-radius: 16rpx;
    font-size: 28rpx;
    line-height: 1.5;
    word-break: break-all;
  }
  &--mine {
    justify-content: flex-end;
    .bubble {
      background: #409eff;
      color: #fff;
    }
  }
  &--peer {
    justify-content: flex-start;
    .bubble {
      background: #fff;
      color: #303133;
    }
  }
}
.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #f0f2f5;
  .ipt {
    flex: 1;
    height: 72rpx;
    padding: 0 24rpx;
    background: #f5f6f8;
    border-radius: 36rpx;
    font-size: 28rpx;
  }
  .btn-send {
    margin-left: 20rpx;
    height: 72rpx;
    line-height: 72rpx;
    padding: 0 32rpx;
    background: #409eff;
    color: #fff;
    font-size: 28rpx;
    border-radius: 36rpx;
    &::after {
      border: none;
    }
    &[disabled] {
      background: #c0c4cc;
    }
  }
}
</style>

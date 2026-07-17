# uni-app 星级评分用法片段

`StarRate.vue` 是自包含组件，放到 `uni-app/src/components/StarRate.vue` 即可。下面是在页面里提交评价的片段。

## 1. api（放 `uni-app/src/api/rating.ts`）

```ts
import { request } from '../utils/request';

/** 提交评价：只传 bizType/bizId/score/content，方向由后端解析 */
export function apiAddRating(data: { bizType: string; bizId: number | string; score: number; content?: string }) {
  return request({ url: '/biz/rating', method: 'POST', data });
}

/** 查某业务某方向是否已评 */
export function apiGetRating(bizType: string, bizId: number | string, evalRole: number) {
  return request({ url: `/biz/rating/one/${bizType}/${bizId}/${evalRole}`, method: 'GET' });
}
```

> `request` 为基座 uni-app 已有的统一请求（自动带 Bearer + clientid，见 `uni-app/src/utils/request.ts`）。

## 2. 页面里使用（评价弹窗 / 内嵌表单）

```vue
<template>
  <view class="eval-box">
    <view class="row">
      <text class="label">评分</text>
      <StarRate v-model="score" :max="5" />
    </view>
    <textarea v-model="content" class="content" placeholder="说说本次体验（可选）" maxlength="500" />
    <button class="btn" :disabled="submitting" @tap="submit">提交评价</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import StarRate from '../../components/StarRate.vue';
import { apiAddRating } from '../../api/rating';

const props = defineProps<{ bizType: string; bizId: number | string }>();
const emit = defineEmits<{ (e: 'done'): void }>();

const score = ref(5);        // 默认 5 星
const content = ref('');
const submitting = ref(false);

const submit = async () => {
  if (score.value < 1) {
    uni.showToast({ title: '请先评分', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    await apiAddRating({ bizType: props.bizType, bizId: props.bizId, score: score.value, content: content.value });
    uni.showToast({ title: '评价成功' });
    emit('done');
  } finally {
    submitting.value = false;
  }
};
</script>
```

## 3. 只读展示（订单/工单详情里回显已评分）

```vue
<StarRate :model-value="order.score" readonly />
```

<!--
  uni-app 星级评分组件（自包含，无需 uni-ui）——契合基座 uni-app 的"纯 view/text + 自写 CSS"风格。
  放到 uni-app/src/components/StarRate.vue。两端(小程序/H5)通用。
  用法见同目录 usage-snippet.md。
-->
<template>
  <view class="star-rate">
    <text
      v-for="n in max"
      :key="n"
      class="star"
      :class="{ 'star--on': n <= current, 'star--readonly': readonly }"
      @tap="onTap(n)"
    >{{ n <= current ? '★' : '☆' }}</text>
    <text v-if="showText && current > 0" class="star-text">{{ texts[current - 1] }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = withDefaults(
  defineProps<{
    modelValue?: number;
    max?: number;
    readonly?: boolean;
    showText?: boolean;
    texts?: string[];
  }>(),
  {
    modelValue: 0,
    max: 5,
    readonly: false,
    showText: true,
    texts: () => ['很差', '较差', '一般', '满意', '非常满意']
  }
);

const emit = defineEmits<{ (e: 'update:modelValue', v: number): void }>();

const current = ref(props.modelValue);
watch(
  () => props.modelValue,
  (v) => (current.value = v)
);

const onTap = (n: number) => {
  if (props.readonly) return;
  current.value = n;
  emit('update:modelValue', n);
};
</script>

<style scoped>
.star-rate {
  display: flex;
  align-items: center;
}
.star {
  font-size: 44rpx;
  color: #dcdfe6;
  margin-right: 8rpx;
}
.star--on {
  color: #f7ba2a;
}
.star--readonly {
  /* 只读态不做点击反馈 */
}
.star-text {
  margin-left: 12rpx;
  font-size: 26rpx;
  color: #f7ba2a;
}
</style>

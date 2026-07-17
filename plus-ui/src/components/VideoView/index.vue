<!--
  VideoView · 详情/只读视频播放（与 ImagePreview 对称）
  ----------------------------------------------------------------
  业务字段（video）在本模板里直接存 URL，所以只需把 URL 交给原生 <video>：
      <VideoView :src="row.video" width="360" />
  兼容传入逗号串（取第一个）。为空则不渲染。
-->
<template>
  <div v-if="realSrc" class="component-video-view" :style="`width:${realWidth};height:${realHeight};`">
    <video :src="realSrc" :poster="poster" controls preload="metadata" class="video-el" />
  </div>
</template>

<script setup lang="ts">
import { propTypes } from '@/utils/propTypes';

const props = defineProps({
  // 视频 URL（本模板业务字段即 URL）
  src: propTypes.string.def(''),
  width: {
    type: [Number, String],
    default: '360px'
  },
  height: {
    type: [Number, String],
    default: 'auto'
  },
  // 封面图（可选）
  poster: propTypes.string.def('')
});

const realSrc = computed(() => (props.src ? props.src.split(',')[0] : ''));
const realWidth = computed(() => (typeof props.width === 'string' ? props.width : `${props.width}px`));
const realHeight = computed(() => (typeof props.height === 'string' ? props.height : `${props.height}px`));
</script>

<style lang="scss" scoped>
.component-video-view {
  .video-el {
    width: 100%;
    height: 100%;
    max-width: 100%;
    border-radius: 6px;
    background: #000;
    box-shadow: 0 0 5px 1px #ccc;
  }
}
</style>

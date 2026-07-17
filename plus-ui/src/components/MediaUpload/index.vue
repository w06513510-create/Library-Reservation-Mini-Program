<!--
  MediaUpload · 通用媒体上传（图片多选 + 视频单个，合一）
  ----------------------------------------------------------------
  · 后端走 /resource/media/upload（登录即可用，返回 { url, fileName, type }）。
  · 业务字段直接存 URL：images 逗号分隔多 URL、video 单 URL。
  · 对外两个 v-model —— 哪个绑了才渲染哪个区（可当纯图 / 纯视频 / 合一用）：
      <MediaUpload v-model:images="form.images" v-model:video="form.video" />
  · 读侧：图片用 <ImagePreview :src="row.images" />，视频用 <VideoView :src="row.video" />。
  详见 docs/specs/2026-07-17-media-upload-components-design.md
-->
<template>
  <div class="component-media-upload">
    <!-- 图片区 -->
    <div v-if="imageEnabled" class="media-block">
      <div class="media-label">图片</div>
      <el-upload
        ref="imageUploadRef"
        multiple
        :action="uploadUrl"
        list-type="picture-card"
        :headers="headers"
        :file-list="imageList"
        :limit="imageLimit"
        :accept="imageAccept"
        :disabled="disabled"
        :on-success="handleImageSuccess"
        :before-upload="handleImageBefore"
        :on-error="handleError"
        :on-exceed="handleImageExceed"
        :before-remove="handleImageRemove"
        :on-preview="handleImagePreview"
        :class="{ hide: imageList.length >= imageLimit || disabled }"
      >
        <el-icon class="upload-icon"><plus /></el-icon>
      </el-upload>
    </div>

    <!-- 视频区（恒单个） -->
    <div v-if="videoEnabled" class="media-block">
      <div class="media-label">视频</div>
      <div class="video-wrap">
        <div v-if="videoUrl" class="video-item">
          <video :src="videoUrl" controls class="video-player" />
          <el-icon v-if="!disabled" class="video-remove" title="移除视频" @click="removeVideo"><circle-close /></el-icon>
        </div>
        <el-upload
          v-else
          :action="uploadUrl"
          :headers="headers"
          :show-file-list="false"
          :accept="videoAccept"
          :disabled="disabled"
          :on-success="handleVideoSuccess"
          :before-upload="handleVideoBefore"
          :on-error="handleError"
        >
          <div class="video-add" :class="{ disabled }">
            <el-icon><plus /></el-icon>
          </div>
        </el-upload>
      </div>
    </div>

    <!-- 提示 -->
    <div v-if="isShowTip" class="el-upload__tip">
      <template v-if="imageEnabled"
        >图片 ≤ <b>{{ imageSize }}MB</b>（{{ imageFileType.join('/') }}）</template
      >
      <template v-if="imageEnabled && videoEnabled">，</template>
      <template v-if="videoEnabled"
        >视频 ≤ <b>{{ videoSize }}MB</b>（{{ videoFileType.join('/') }}）</template
      >
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="dialogVisible" title="预览" width="800px" append-to-body>
      <img :src="dialogUrl" style="display: block; max-width: 100%; margin: 0 auto" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { propTypes } from '@/utils/propTypes';
import { globalHeaders } from '@/utils/request';

const props = defineProps({
  // v-model:images —— 逗号分隔的图片 URL；不绑则不渲染图片区
  images: { type: String, default: undefined },
  // v-model:video —— 单个视频 URL；不绑则不渲染视频区
  video: { type: String, default: undefined },
  // 图片数量上限
  imageLimit: propTypes.number.def(9),
  // 图片大小上限(MB)
  imageSize: propTypes.number.def(10),
  // 视频大小上限(MB)
  videoSize: propTypes.number.def(100),
  // 图片允许扩展名
  imageFileType: propTypes.array.def(['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']),
  // 视频允许扩展名
  videoFileType: propTypes.array.def(['mp4', 'mov', 'm4v', 'webm']),
  isShowTip: propTypes.bool.def(true),
  disabled: propTypes.bool.def(false)
});

const emit = defineEmits(['update:images', 'update:video']);
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/resource/media/upload');
const headers = ref(globalHeaders());

// 父组件绑了对应 v-model 才显示该区（未绑时 prop 为 undefined）
const imageEnabled = computed(() => props.images !== undefined);
const videoEnabled = computed(() => props.video !== undefined);

const imageAccept = computed(() => props.imageFileType.map((t) => `.${t}`).join(','));
const videoAccept = computed(() => props.videoFileType.map((t) => `.${t}`).join(','));

// —— 图片回显（值本身即 URL，逗号拆开直接用）——
const imageList = ref<any[]>([]);
watch(
  () => props.images,
  (val?: string) => {
    imageList.value = (val ? val.split(',') : []).filter((u) => u).map((url) => ({ name: url, url }));
  },
  { immediate: true }
);

const videoUrl = computed(() => props.video || '');

// 多图并发上传的计数（对齐 stock ImageUpload 的做法）
const number = ref(0);
const uploadList = ref<any[]>([]);
const imageUploadRef = ref<any>();

const dialogVisible = ref(false);
const dialogUrl = ref('');

/** 扩展名 / MIME 双重校验 */
const checkType = (file: any, types: string[]) => {
  let ext = '';
  if (file.name.lastIndexOf('.') > -1) {
    ext = file.name.slice(file.name.lastIndexOf('.') + 1).toLowerCase();
  }
  return types.some((t) => (file.type && file.type.indexOf(t) > -1) || (ext && ext === t.toLowerCase()));
};

/** 对象列表 → 逗号 URL（跳过本地 blob 预览项） */
const listToUrls = (list: any[]) =>
  list
    .filter((f) => f.url && f.url.indexOf('blob:') !== 0)
    .map((f) => f.url)
    .join(',');

// ===== 图片 =====
const handleImageBefore = (file: any) => {
  if (!checkType(file, props.imageFileType)) {
    proxy?.$modal.msgError(`图片格式仅支持 ${props.imageFileType.join('/')}`);
    return false;
  }
  if (file.name.includes(',')) {
    proxy?.$modal.msgError('文件名不能包含英文逗号（逗号用作分隔符）');
    return false;
  }
  if (props.imageSize && file.size / 1024 / 1024 >= props.imageSize) {
    proxy?.$modal.msgError(`单张图片不能超过 ${props.imageSize}MB`);
    return false;
  }
  proxy?.$modal.loading('正在上传，请稍候...');
  number.value++;
};

const handleImageSuccess = (res: any, file: any) => {
  if (res.code === 200) {
    uploadList.value.push({ name: res.data.fileName, url: res.data.url });
    afterImageUpload();
  } else {
    number.value--;
    proxy?.$modal.closeLoading();
    proxy?.$modal.msgError(res.msg);
    imageUploadRef.value?.handleRemove(file);
    afterImageUpload();
  }
};

const afterImageUpload = () => {
  if (number.value > 0 && uploadList.value.length === number.value) {
    imageList.value = imageList.value.filter((f) => f.url !== undefined).concat(uploadList.value);
    uploadList.value = [];
    number.value = 0;
    emit('update:images', listToUrls(imageList.value));
    proxy?.$modal.closeLoading();
  }
};

const handleImageRemove = (file: any): boolean => {
  const idx = imageList.value.map((f) => f.url).indexOf(file.url);
  if (idx > -1) {
    imageList.value.splice(idx, 1);
    emit('update:images', listToUrls(imageList.value));
  }
  return false; // 由 file-list 受控，阻止 el-upload 默认移除
};

const handleImageExceed = () => proxy?.$modal.msgError(`最多上传 ${props.imageLimit} 张图片`);

const handleImagePreview = (file: any) => {
  dialogUrl.value = file.url;
  dialogVisible.value = true;
};

// ===== 视频 =====
const handleVideoBefore = (file: any) => {
  if (!checkType(file, props.videoFileType)) {
    proxy?.$modal.msgError(`视频格式仅支持 ${props.videoFileType.join('/')}`);
    return false;
  }
  if (props.videoSize && file.size / 1024 / 1024 >= props.videoSize) {
    proxy?.$modal.msgError(`视频不能超过 ${props.videoSize}MB`);
    return false;
  }
  proxy?.$modal.loading('正在上传视频，请稍候...');
};

const handleVideoSuccess = (res: any) => {
  proxy?.$modal.closeLoading();
  if (res.code === 200) {
    emit('update:video', res.data.url);
  } else {
    proxy?.$modal.msgError(res.msg);
  }
};

const removeVideo = () => emit('update:video', '');

const handleError = () => {
  proxy?.$modal.msgError('上传失败');
  proxy?.$modal.closeLoading();
};
</script>

<style lang="scss" scoped>
.component-media-upload {
  .media-block {
    margin-bottom: 12px; // 图片区与视频区之间留间距
  }
  .media-label {
    margin-bottom: 6px;
    font-size: 13px;
    color: var(--el-text-color-regular);
  }
  .upload-icon {
    font-size: 24px;
    color: #8c939d;
  }

  // 图片数量达上限 / 禁用时隐藏“加号”卡片
  :deep(.hide .el-upload--picture-card) {
    display: none;
  }

  // —— 视频区 ——
  .video-wrap {
    display: flex;
  }
  .video-item {
    position: relative;
    width: 220px;
  }
  .video-player {
    width: 220px;
    max-width: 100%;
    border-radius: 6px;
    background: #000;
  }
  .video-remove {
    position: absolute;
    top: -8px;
    right: -8px;
    font-size: 20px;
    color: var(--el-color-danger);
    background: #fff;
    border-radius: 50%;
    cursor: pointer;
  }
  .video-add {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 148px;
    height: 148px;
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    color: #8c939d;
    font-size: 24px;
    cursor: pointer;
    transition: border-color 0.2s;
    &:hover {
      border-color: var(--el-color-primary);
    }
    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}
</style>

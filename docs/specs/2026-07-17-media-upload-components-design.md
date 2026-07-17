# 媒体上传组件设计（MediaUpload + VideoView）

- 日期：2026-07-17
- 状态：已定稿，待实现
- 适用：基座 `ruoyi_template`（plus-ui），新项目复制自带

## 背景与决策

业务里普遍要传单/多图与视频（商品封面、相册、演示视频等）。基座已内置后端接口
`POST /resource/media/upload`（`@SaCheckLogin`，任何已登录用户含 C 端可用，无需
`system:oss:upload` 权限；按扩展名白名单校验，图 10MB / 视频 100MB；直传 MinIO/OSS）
**返回 `{ url, fileName, type }`**——即返回可访问 URL，不注册 `sys_oss`、不返回 ossId。

**已定约定（URL 约定）**：业务媒体字段**直接存 URL**（`images` 逗号分隔多 URL、`video`
单 URL）。因此不复用返回 ossId 的 stock `ImageUpload`/`FileUpload`，也不需要 `@Translation`
或 `listByIds` 做 ossId→url 解析——读侧字段本身即 URL。

## 组件

### 1. `MediaUpload`（表单侧唯一上传组件）
- 路径：`plus-ui/src/components/MediaUpload/index.vue`（`unplugin-vue-components` 自动全局注册，无需手动 import）。
- 对外：`v-model:images`（String，逗号 URL）、`v-model:video`（String，单 URL）。
  **哪个 v-model 绑了才渲染哪个区**——同一组件可当纯图 / 纯视频 / 图视频合一用。
- Props：`imageLimit`(默 9) · `imageSize`MB(默 10) · `videoSize`MB(默 100) · `disabled`(默 false) · `isShowTip`(默 true)。**视频恒为单个**（`video` 字段是单值），不设 videoLimit。
- 行为：
  - 两个区都 POST `${VITE_APP_BASE_API}/resource/media/upload`，请求头 `globalHeaders()`（带 Authorization + clientid）。
  - 成功回调取 `res.data.url`：图片 push 进列表 → `emit('update:images', 逗号拼接 url)`；视频替换单值 → `emit('update:video', url)`。
  - 图片区用 `el-upload` picture-card 多选预览；视频区上传后内联 `<video controls :src>` 预览。
  - 删除即从对应 model 移除（**不调 delOss**——URL 约定下不登记 sys_oss；MinIO 端残留由演示/运维清理，YAGNI）。
  - `before-upload` 校验：扩展名（图 jpg/jpeg/png/gif/webp/bmp、视频 mp4/mov/m4v/webm）、大小、文件名不含逗号（逗号是分隔符）。
- 回显：`watch` 两个 model，按逗号拆成预览列表（值已是 URL，直接用，无需查接口）。

### 2. `VideoView`（详情/只读播放，极薄）
- 路径：`plus-ui/src/components/VideoView/index.vue`。
- Props：`src`(String，视频 URL；容错：为空则不渲染) · `width` · `height` · `poster`。
- 渲染 `<video controls :src :poster :style>` + 加载失败兜底文案；圆角样式与 `ImagePreview` 呼应。

### 读侧组合（不新增图片组件）
- 图片：现成 `<ImagePreview :src="row.images" />`（本就按逗号拆成预览列表，URL 串正好）。
- 视频：`<VideoView :src="row.video" />`（或原生 `<video :src controls>`）。

## 数据契约（贯穿三端）
表单 `MediaUpload` 存 URL → 库里 `images`(逗号 URL)/`video`(单 URL) → 详情
`ImagePreview`/`VideoView` 直接显示。演示数据脚本也写 URL。图文对应由取图环节保证。

## 连带修正（既有产物按 ossId 写的，改为 URL 约定）
1. `Template/scripts/上传媒体到MinIO_图片视频.py`：写回 **URL**，去掉 `sys_oss` 登记段。
2. SOP `05` 与 `SOP/templates/README.md`：把"存 ossId / 逗号 ossId"表述改为 **URL 约定**。
3. SOP `03`：补一条"业务媒体统一走 `/resource/media/upload`、字段存 URL、组件用 `MediaUpload`/`VideoView`"。

## 验证（按 SOP §验证：Playwright + 查库）
需用户先起后端 + MinIO。助手起 vite → 在一个含图/视频字段的表单里用 `MediaUpload` 传
1 图 + 多图 + 1 视频 → 查库确认 `images`/`video` 落 URL → 详情页 `ImagePreview` 显示、
`VideoView` 播放。无后端时先做 vite 编译/类型检查兜底。

## 范围外（YAGNI）
- 不做封面自动截帧、不做单字段图视频混存、不做图片压缩（stock ImageUpload 才有，这里 URL 约定不涉及）。
- 后端零改动（`/resource/media/upload` 已存在）。

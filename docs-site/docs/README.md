# RuoYi 基座 · 通用能力文档

本站说明 **`ruoyi_template`**（RuoYi-Vue-Plus 5.6.2 复用基座）里已经沉淀好的**跨项目通用能力**——复制基座起新项目时，对照本站即可知道"哪些轮子已经造好、直接用、怎么用"，不必每次重抄。

- 仓库：`https://github.com/w06513510-create/ruoyi_template.git`
- 底座：后端 Spring Boot 3 + Java 17 + Sa-Token + MyBatis-Plus + MySQL 8 + Redis + MinIO；Web 端 plus-ui（Vite + Vue3 + TS + Element Plus）；移动端 uni-app（Vue3）。
- 由来：对 5 个毕设项目（校园社团 / 校园跑腿 / 校园二手 / 高校宿舍 / 宠物领养）横向调研，把**出现 ≥2 次、且解耦度高**的能力抽进基座。

!!! note "本站定位"
    本站是**说明/导航层**，做概括与"怎么用"。权威细节以仓库内文件为准：台账 `docs/通用能力清单.md`、设计稿 `docs/specs/*`、代码模板 `docs/templates/*`、各模块源码 `RuoYi-Vue-Plus/ruoyi-modules/ruoyi-*`。每页页尾 `## 源` 注明依据。

## 通用能力一览

| 能力 | 类型 | 位置 | 状态 | 详情 |
|---|---|---|---|---|
| ① C端接入基座 | 后端模块 + uni-app 骨架 | `ruoyi-modules/ruoyi-app` + `uni-app/` | ✅ 已验证 | [看这页](能力/01-C端接入基座.md) |
| ② 消息 / 通知中心 | 后端模块 + 前端页 | `ruoyi-modules/ruoyi-message` | ✅ 已验证 | [看这页](能力/02-消息通知中心.md) |
| ③④ 支付与钱包 | 后端模块 | `ruoyi-modules/ruoyi-pay` | ✅ 已验证 | [看这页](能力/03-支付与钱包.md) |
| 媒体上传组件 | 前端组件 + 后端接口 | `plus-ui` 组件 + `SysMediaController` | ✅ 已验证 | [看这页](能力/04-媒体上传.md) |
| ⑤~⑧ 脚手架 / 代码模板 | 代码模板 + 文档 | `docs/templates/` | ✅ | [看这页](能力/05-脚手架模板.md) |
| ⑨ 收藏/点赞/关注 | 后端模块 + 前端 | `ruoyi-modules/ruoyi-interaction` | ✅ | [看这页](能力/06-互动收藏点赞关注.md) |

先读 [总览与选型](能力/00-总览.md) 了解怎么挑、怎么落地。

## 我想做 X → 看哪页

| 我想… | 去 |
|---|---|
| 给新项目加**小程序/H5 的登录 + 请求 + 上传 + 分页** | [① C端接入基座](能力/01-C端接入基座.md) |
| 让业务在某个动作后**给用户发一条站内通知 / 做私信** | [② 消息/通知中心](能力/02-消息通知中心.md) |
| 做**充值 / 余额 / 扣款 / 冻结**，且要资金对得平 | [③④ 支付与钱包](能力/03-支付与钱包.md) |
| 表单里**传图/传视频**、详情里展示 | [媒体上传组件](能力/04-媒体上传.md) |
| 快速做**审核流 / 数据看板 / 问卷考试 / 评价打分** | [⑤~⑧ 脚手架模板](能力/05-脚手架模板.md) |
| 让用户**收藏/点赞/关注**某个对象或某个人 | [⑨ 收藏/点赞/关注](能力/06-互动收藏点赞关注.md) |
| 查术语（app_user / 对平不变式 / clientid …） | [术语与约定](glossary.md) |

## 源

- `docs/通用能力清单.md`（能力总台账，本站一览表与状态取自此）
- `docs/specs/2026-07-17-app-access-base-design.md`、`docs/specs/2026-07-18-message-center-design.md`、`docs/specs/2026-07-18-pay-wallet-design.md`、`docs/specs/2026-07-17-media-upload-components-design.md`

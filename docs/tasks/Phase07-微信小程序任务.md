# 任务 · Phase 07 微信小程序（uni-app 读者 C 端）

> 新会话接手本任务前，**先读** 同目录 [`项目进度与交接.md`](项目进度与交接.md)（项目现状/环境/踩坑）与 SOP `D:\codeSpace_shop\SOP\docs\07-小程序.md`（本篇执行手册，单一事实源）。
> 状态：**🟢 主体完成（2026-08-07，H5 已验）**——§二三缺口全部落地：app_user 种子补齐、后端 `/app/library/*` C 端接口、uni-app 9 读者页；H5+Playwright 逐页验证 + 查库对平 + 越权 403 均过。**仅剩 mp-weixin 微信开发者工具真机验收**（Playwright 测不了 mp-weixin）。下方缺口清单保留作实现留痕。约定/踩坑以 SOP 为准。

## 一、一句话 & 范围

用 **uniapp + Vue3 + Vite + TS + Pinia** 做微信小程序**读者 C 端**，**复用同一后端（8199）与数据库，不新建后端**，把读者高频移动场景搬上小程序。编译目标 mp-weixin 为主，H5 供开发期 Playwright 自测。

**范围红线**：只做**读者 C 端**（管理端/大屏留在 Web）；违规只走信用分+黑名单，**不涉及任何金钱/支付**（骨架里的 `wallet` 页与 Web 无关，本项目**不用**，可删或忽略）。

**读者端功能（建议 tabBar 4 项 + 普通页）**：
- tabBar：首页（个人信用分/快捷入口）/ 选座预约 / 我的（预约·借阅·信用）/ 消息。
- 普通页：平面图选座（复用 seatStatus 一桌多座）→ 下单；我的预约（签到/暂离/返回/退座/取消）；书目检索·寻书（按索书号/书架）；我的借阅·图书预约；信用分与信用流水；违约记录·申诉；规则说明（只读）。

## 二、现状（已就绪 / 缺口）——**务必据实，别重复造**

### ✅ 已就绪（从基座 Template 复制而来）
- **`uni-app/` 骨架**：`package.json`/`vite.config.ts`/`manifest.json`/`pages.json` + `src/`（`config.ts`、`utils/`(request/crypto/jsencrypt/dict)、`store/`(auth)、`composables/`(useList)、通用 api(auth/message/interaction/wallet)、通用页(login/index/message/mine/wallet/demo)）。**复用它，改 `config.ts` 已对接本项目**：`BASE_URL`(H5 走 `/dev-api` 代理→8199)、`CLIENT_ID='app00000000000000000000000000001'`、`TENANT_ID='000000'`、`ENCRYPT=false`（默认关）。
- **后端 C 端接入**（`ruoyi-modules/ruoyi-app`）：`AppAuthController`(`/app/auth/login`·`/register`·`/getInfo`·`/updateAvatar`·`/updateNickname`·`/logout`·`/wxLogin`)、`AppLoginHelper`（loginId 命名空间 `app_user:{id}`，与后台 `sys_user` 隔离）、`AppUser`/`IAppUserService`/`AppUserMapper`。表 `app_user`、`sys_client` 已建。
- **读者档案** `biz_reader` 已有 12 条（`user_id` 1001–1012，对应 张三…冯磊，含信用分）；库设计里 `biz_reader` 与 `app_user` 是 **1:1**（`biz_reader.user_id = app_user.id`）。

### ❌ 缺口（本任务要做的）
1. **⚠️ `app_user` 表为空**——12 个读者只在 `biz_reader`，**没有 C 端登录账号**。必须先**补种 `app_user`（id 对齐 `biz_reader.user_id` 1001–1012）**，读者才能登小程序。登录字段见 `AppLoginBody`（用户名/手机号 + 密码，密码走框架 BCrypt）。建议写 `sql/seed_app_user.sql`，并把它加入应用顺序（在 seed_support 之后即可）。
2. **后端无图书馆 C 端业务接口**——现有 `/library/*` 控制器都挂在后台 `sys_user` 权限点上（`@SaCheckPermission('library:*')`），**app_user 调不了**。需新增一组 **C 端读者接口**（建议 `ruoyi-app` 下 `/app/library/*` 或 `/app/reader/*`），用 `AppLoginHelper.getUserId()` 取当前读者 id（= readerId）复用现有 Service 逻辑：
   - 选座：楼层列表、`seatStatus`(按桌分组)、约座、我的预约列表、签到/暂离/返回/退座/取消（复用 `IReservationService` 的 CAS 动作，**readerId 强制取当前登录读者、禁止越权传别人**）。
   - 图书：书目检索/详情、按书架寻书、我的借阅、图书预约(hold)。
   - 信用：我的信用分 + 信用流水、我的违约、发起申诉。
   - 规则：`biz_rule_config` 只读展示（签到窗/暂离/就餐保留等，让读者知道规则）。
   > 复用 Web 的 Service（`ReservationServiceImpl`/`LoanServiceImpl`/`CreditServiceImpl`…），**只加 C 端 Controller 薄封装**，并发行锁/时段重叠/信用不变式一律走后端同一套，前端只展示与调用。
3. **前端无图书馆读者页**——现有 pages 是通用骨架(message/wallet/mine/demo)。需新建读者业务页（见 §一）+ `src/api/library.ts` 端点封装 + tabBar 配置(`pages.json`)。骨架的 wallet/demo 页本项目不用。

## 三、关键坑（SOP 07 已列，务必照做）

- **C 端认证走 `/app/auth/*`，不要用后台 `/auth/login`**；token 存 `auth` store/`uni.storage`。请求头必带 `Authorization: Bearer <token>` + `clientid`(= config `CLIENT_ID`) + `Content-Language: zh_CN`；`tenantId` 放登录 body(`000000`)。
- **业务侧取当前读者** 用 `AppLoginHelper.getUserId()`，**不要**取后台用户上下文；C 端接口不要挂 `sys_user` 权限点。
- **请求加密**：本项目 `ENCRYPT=false` 默认关，`/app/auth/*` 若带 `@ApiEncrypt` 需确认后端 `api-decrypt.enabled`；如需开加密，AES+RSA 工具骨架已有（`utils/crypto`、`jsencrypt`），RSA 配对别搞反、mp-weixin 响应头大小写不敏感遍历取 `encrypt-key`。
- **验证码默认关**（`captcha.enable=false`），H5/Playwright 可自动登录。
- **H5 代理**：uniapp `manifest.json` 的 `pathRewrite` 在 vite 下**无效**；已在 `vite.config.ts` 用 `server.proxy` + `rewrite` 剥 `/dev-api` 前缀（骨架已配，核对端口 8199）。
- **返回体**：列表 `{code,msg,rows,total}`；单体/动作 `{code,msg,data}`。兜底 `res.rows||[]`/`res.data||{}`/`??0`。
- **mp-weixin appid 坑**：微信开发者工具 2.02+ 会把 `dist/dev/mp-weixin/project.config.json` 的空 `appid` 自动填成你账号的真实 appid → 每页"网络异常"。修复：把 appid 改回 `""`、刷新；根治：`dist/dev/mp-weixin/` 加 `.gitignore`。
- **别克隆 `RuoYi-App`**（Vue2/非 Plus，认证不一致）；`<style lang="scss">` 需装 `sass`。

## 四、过关门禁（同 Web：Playwright + 查库 + 真机）

1. 后端用 `scripts/run-backend.bat` 起（动过后端 `run-backend.bat build` 重构建；**编译报错一次即停、列因交用户**）。
2. **H5 自测**：`uni-app` 下 `npm run dev:h5` + Playwright 逐页验证交互与接口，**查库校验**状态流转/信用不变式（如约座后 `biz_reservation` 落库、readerId 为当前登录读者、并发/时段重叠仍被后端拦）。
3. **mp-weixin 验收**：微信开发者工具真机/模拟器，勾"不校验合法域名"直连本地 8199（Playwright 测不了 mp-weixin）。
4. 一模块一 commit 即时 push；C 端通用能力（如微信授权）按铁律模板回写基座 `ruoyi_template`，业务留项目。

## 五、环境恢复（新会话第一步）

1. 起底座：`docker compose up -d`（Redis/MinIO/SnailJob）。
2. 起后端：`scripts/run-backend.bat`（约 20s，端口 **8199**）。
3. 起小程序 H5：`cd uni-app && npm i && npm run dev:h5`（首次装依赖；确认 `sass` 在）。
4. **先补 `app_user` 种子**（见 §二缺口 1）否则无法登录 C 端。
5. Web 端账号（后台，非 C 端）：`admin/admin123`；三角色 `liutong/caibian/xitong` 同密码。C 端读者账号待 §二 seed 后确定。
6. 库：本机 MySQL `library_reservation`（`root/123456@localhost:3306`）。

## 六、关联文档
- SOP 执行手册：`D:\codeSpace_shop\SOP\docs\07-小程序.md`（**照它做**）。
- C 端接入设计：`docs/specs/2026-07-17-app-access-base-design.md`。
- 需求/库设计：`项目文档站/docs/01-需求规格说明书.md`、`02-数据库设计说明书.md`（读者/信用/座位域）。
- 交付说明：`项目文档站/docs/03-交付说明.md`（§七 交付增强含桌子层/规则配置，C 端需与之一致）。
- 进度/环境/踩坑总入口：[`项目进度与交接.md`](项目进度与交接.md)。

> 里程碑：小程序验证通过即**全项目交付完成** 🎉（Web 00–06 + UI 换肤 + 小程序二期）。

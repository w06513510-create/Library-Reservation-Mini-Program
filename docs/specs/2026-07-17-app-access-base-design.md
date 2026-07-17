# C 端接入基座设计（ruoyi-app + uni-app 骨架）

- 日期：2026-07-17
- 状态：待用户过目
- 适用：基座 `ruoyi_template`（后端 RuoYi-Vue-Plus 5.6.2 + 新增 uni-app 小程序骨架）
- 来源：对 5 个毕设项目（社团/跑腿/二手/宿舍/宠物）横向调研得出——有小程序的 4 个项目都各写了一遍"C 端账号 + 登录 + 请求/上传/字典/分页"接入层。抽成基座后，新项目复制即用。

## 背景与关键事实

- 基座 `ruoyi-common-core/.../enums/UserType.java` **已内置 `APP_USER("app_user")`**，`getUserType()` 用 `StringUtils.contains` 匹配——框架**本就预期 loginId 带 `app_user` 命名空间**，无需多套 StpLogic。
- 基座已内置通用上传 `SysMediaController @ /resource/media/upload`（`@SaCheckLogin`，返回 `{url,fileName,type}`）。C 端上传**直接复用它**，不再造 `AppOssController`。
- 参考项目里：宠物/二手用 **app_user 独立表 + AppLoginHelper 命名空间**；跑腿则复用 sys_user+角色。基座取 **app_user 独立表**（C 端与后台职责/字段差异大，独立更干净，契合 `UserType.APP_USER`）。

## 已定决策（我的取舍，待你确认）

| 项 | 决策 | 理由 |
|---|---|---|
| 后端模块落法 | 新增独立模块 `ruoyi-app`（包 `org.dromara.app`） | 含建表实体/Controller，属业务型；后续 C 端业务模块只依赖 `ruoyi-app` 即可拿当前用户，避免各模块重复 copy `AppLoginHelper` |
| C 端账号模型 | `app_user` 独立表 | 不污染 sys_user，契合 `UserType.APP_USER` |
| Sa-Token 隔离 | 同一 `StpUtil` + loginId 前缀 `app_user:{id}` | 无需多 StpLogic，最小改动；`AppLoginHelper.getUserId()` 校前缀，后台 token 不能冒用 C 端接口 |
| 登录方式 | 手机号+密码（BCrypt）现做；openid（微信）**只留列+端点占位+TODO**，不引微信 SDK | YAGNI，openid 上线再补 |
| token 字段名 | 返回 `{ token }`（非 `access_token`） | app 体系专属，避免与 sys_user 混 |
| C 端上传 | 复用基座 `/resource/media/upload` | 已有同款返回，删掉各项目的 AppOss |
| 前端加密(AES/RSA) | 文件保留、**默认关**（`ENCRYPT=false`） | 需后端 api-decrypt 配套；传输层可上 HTTPS；对模板属过度设计 |
| C 端分页 | 提炼成 `useList` composable | 消除各页 onReachBottom/onPullDownRefresh 重复 |
| 登录策略接口 | **暂不引** `IAppLoginStrategy`，Controller 内 `login`/`wxLogin` 两方法并存 | YAGNI；第三种登录方式出现再抽策略 |

## 一、后端设计（ruoyi-app）

### 模块与依赖
- 新建 `Template/RuoYi-Vue-Plus/ruoyi-modules/ruoyi-app/`（pom 抄 ruoyi-demo）。
- 改 `ruoyi-modules/pom.xml`：`<modules>` 加 `ruoyi-app`。
- 改 `ruoyi-admin/pom.xml`：`<dependencies>` 加 `ruoyi-app`（照 ruoyi-demo 那段）。

包结构：
```
org.dromara.app
├── controller/AppAuthController.java
├── domain/AppUser.java            // extends TenantEntity
├── domain/bo/AppLoginBody.java    // {phone, password}
├── domain/bo/AppRegisterBody.java // {phone, password, nickname?}
├── domain/bo/WxLoginBody.java     // {code}  —— openid 预留
├── domain/vo/AppUserVo.java       // 下发用，剥离 password/openid/unionid
├── mapper/AppUserMapper.java
├── service/IAppUserService.java
├── service/impl/AppUserServiceImpl.java
└── utils/AppLoginHelper.java
```

### app_user 建表 DDL（只留通用字段）
```sql
drop table if exists app_user;
create table app_user (
  id              bigint        not null                  comment '用户ID',
  tenant_id       varchar(20)   default '000000'          comment '租户编号',
  phone           varchar(20)   not null                  comment '手机号(登录名)',
  password        varchar(100)                            comment '密码(BCrypt; 不下发)',
  openid          varchar(64)                             comment '微信openid(登录方式预留,可空)',
  unionid         varchar(64)                             comment '微信unionid(预留,可空)',
  nickname        varchar(50)                             comment '昵称',
  avatar          varchar(255)                            comment '头像(OSS url)',
  gender          tinyint       default 0                 comment '性别(0未知 1男 2女)',
  status          tinyint       default 0                 comment '账号状态(0正常 1受限 2封禁)',
  register_time   datetime                                comment '注册时间',
  last_login_time datetime                                comment '最后登录时间',
  create_dept     bigint                                  comment '创建部门',
  create_by       bigint                                  comment '创建者',
  create_time     datetime                                comment '创建时间',
  update_by       bigint                                  comment '更新者',
  update_time     datetime                                comment '更新时间',
  del_flag        char(1)       default '0'               comment '删除标志(0存在 2删除)',
  primary key (id),
  unique key uk_user_phone (phone, tenant_id),
  key idx_user_openid (openid)
) engine=innodb comment='C端用户(通用接入基座)';
```
剔除的业务列（业务模块自行加列/子表）：实名认证 `real_name/id_card/cert_status`、信用分 `credit_score`、业务归属 `org_id/dept_id/campus_id/college_id/major_id/grade/student_no/region_id`、多身份 `sender_status/volunteer_status`。

### AppLoginHelper（同 StpUtil + 命名空间）
```java
public class AppLoginHelper {
    public static final String PREFIX     = "app_user:";
    public static final String CLIENT_KEY = "clientid";   // 必须与框架 LoginHelper.CLIENT_KEY 同名
    public static final String USER_KEY   = "userId";
    public static final String TENANT_KEY = "tenantId";
    public static final String DEVICE     = "app";

    public static void login(Long userId, String clientId) {
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(DEVICE);
        model.setExtra(CLIENT_KEY, clientId);   // 关键：写入 clientid 过拦截器
        model.setExtra(USER_KEY, userId);
        model.setExtra(TENANT_KEY, "000000");
        StpUtil.login(PREFIX + userId, model);
    }
    public static Long getUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) throw new ServiceException("未登录或登录已过期", 401);
        String s = loginId.toString();
        if (!s.startsWith(PREFIX)) throw new ServiceException("仅限C端用户访问", 403);
        return Long.parseLong(s.substring(PREFIX.length()));
    }
}
```

### AppAuthController 接口
`@RestController @RequestMapping("/app/auth")`

| 方法 | 路径 | 鉴权 | 入参 | 返回 |
|---|---|---|---|---|
| POST | `/app/auth/login` | 放行 | body `{phone,password}` + header `clientid` | `R<{token}>` |
| POST | `/app/auth/register` | 放行 | body `{phone,password,nickname?}` | `R<Void>` |
| GET | `/app/auth/getInfo` | 拦截器 | — | `R<AppUserVo>` |
| POST | `/app/auth/logout` | 拦截器 | — | `R<Void>`（`StpUtil.logout()`） |
| PUT | `/app/auth/nickname` | 拦截器 | `@RequestParam nickname` | `R<Void>` |
| PUT | `/app/auth/avatar` | 拦截器 | `@RequestParam url`（来自 `/resource/media/upload`） | `R<Void>` |
| POST | `/app/auth/wxLogin` | 放行（预留，默认不实现，桩+TODO） | body `{code}` | `R<{token}>` |

### 安全放行（只改 yml，不改 Java）
框架 `SecurityConfig.SaInterceptor` 无需动。改 `ruoyi-admin/src/main/resources/application.yml` 的 `security.excludes`，只放行登录/注册：
```yaml
security:
  excludes:
    # ...原有...
    - /app/auth/login
    - /app/auth/register
    # - /app/auth/wxLogin   # 启用微信登录时再放行
```
**切勿放行整个 `/app/**`**；getInfo/logout 等走拦截器鉴权 + `AppLoginHelper` 前缀二次校验。

### clientid
拦截器只比对 `header.clientid == token.extra.clientid`，**不查 sys_client 表**。故 CLIENT_ID 只是前后端约定的固定串，**无需在 sys_client 建行**。前端每个请求（含上传）都带同一 `clientid` header。

## 二、前端 uni-app 骨架（从零新增）

> 基座当前无 uni-app。综合取：请求/字典取跑腿（最全），上传取宠物，auth store 取宠物（app_user 口径），分页提炼 composable。

### 目录树
```
Template/uni-app/
├── src/
│   ├── config.ts                 # BASE_URL / CLIENT_ID / TENANT_ID / TOKEN_KEY / ENCRYPT
│   ├── api/auth.ts               # login/register/getInfo/logout/updateNickname/updateAvatar
│   ├── store/auth.ts             # pinia: token/user + 动作
│   ├── utils/request.ts          # 统一请求(Bearer+clientid+401防抖+silent+可选加密)
│   ├── utils/upload.ts           # chooseAndUploadImages/Video → /resource/media/upload
│   ├── utils/dict.ts             # 本地字典 label/tag色 + dictText/dictColor/money
│   ├── utils/crypto.ts           # (可选)AES，ENCRYPT=false 时不引用
│   ├── utils/jsencrypt.ts        # (可选)RSA
│   └── composables/useList.ts    # 分页 composable
├── package.json / vite.config.ts / tsconfig.json / index.html / src/main.ts / src/App.vue / src/pages.json
```

### config.ts（按基座实际端口 8199）
```ts
let baseUrl = 'http://localhost:8199';   // 基座后端端口(Template 默认，见 SOP 01)
// #ifdef H5
baseUrl = '/dev-api';                    // H5 走 vite 代理避跨域
// #endif
export const BASE_URL = baseUrl;
export const CLIENT_ID = 'app00000000000000000000000000001'; // C端固定串，改需前后端同步
export const TENANT_ID = '000000';
export const TOKEN_KEY = 'app_token';
export const ENCRYPT   = false;          // 加密默认关(见"加密"说明)
```
vite proxy：`server.proxy['/dev-api'] = { target:'http://localhost:8199', changeOrigin:true, rewrite: p=>p.replace(/^\/dev-api/,'') }`。

### 关键 API 签名
- `request<T>({url,method?,data?,params?,isToken?=true,isEncrypt?=false,silent?=false})`：固定带 `clientid`；`isToken` 注入 `Bearer`；401 防抖（模块级锁，清 token→toast→`reLaunch('/pages/login/index')`，800ms 不重复跳）；`code===200` resolve `{code,msg,data,rows,total}`，其它 `!silent` 弹 msg 并 reject。
- `store/auth`：`state {token,user}`；`login(phone,password)`→读 `res.data.token`→setToken→getInfo；`register`/`getInfo`/`logout`；token 落 `uni.setStorageSync(TOKEN_KEY)` 冷启动恢复。**不预置 roles/permissions**。
- `upload.ts`：`uploadFile(filePath)`→POST `/resource/media/upload`（name='file'，带 Authorization+clientid）取 `data.url`；`chooseAndUploadImages(count=9)`、`chooseAndUploadVideo()`。
- `dict.ts`：`TAG_COLOR` 映射 + `dictText/dictColor/money`；基座只给引擎 + 一个示例字典，业务字典自填。
- `composables/useList<T>({fetch,pageSize?=10,extraParams?,immediate?=true})` → `{list,loading,finished,refreshing,total,pageNum,reload,loadMore,onRefresh}`；页面里 `onReachBottom(loadMore)` + `onPullDownRefresh(onRefresh)`。

### 加密说明
默认 `ENCRYPT=false`，`crypto.ts/jsencrypt.ts` 保留为可选，request.ts 用开关+动态引入包裹（避免未装依赖时编译报错）。启用需后端 `api-decrypt.enabled=true` + 同一对 RSA 密钥。

## 三、集成坑（实读三项目踩过）

1. **租户上下文**：登录在放行路径上，**无租户上下文**。按手机号/openid 查库、insert 用户全部包 `TenantHelper.ignore(() -> ...)`；登录成功把 `tenantId="000000"` 写 extra。
2. **clientid 一致性**：拦截器校验 `header.clientid == token.extra.clientid`，不一致抛 `-100`。前端**每个请求（含 uploadFile）都带 clientid**，值恒等于 CLIENT_ID。上传最易漏带。
3. **不改 Java 安全配置**，只在 yml `security.excludes` 加 login/register。
4. **token 字段**：后端 `R.ok(Map.of("token", StpUtil.getTokenValue()))`；前端读 `res.data.token`。
5. **getInfo 脱敏**：`password/openid/unionid` 置 null 或走 `AppUserVo`。
6. **复用 `/resource/media/upload`**：不在 ruoyi-app 再造上传接口。

## 四、YAGNI 边界（参考项目有，基座不做）
实名认证 / 信用分体系 / 钱包充值提现 / 多角色多身份 / 业务归属列 / 地址簿 / 消息通知（属 ② 独立模块）/ 举报纠纷 / 登录端加密（默认关）/ 微信 openid 换取逻辑（只留列+端点桩）/ 多租户按域名解析 / 短信验证码登录。基座只做"能登录、能拿当前 C 端用户、能传图、能分页"。

## 五、实现顺序（分支 feat/app-base，完成即推）
1. 后端 ruoyi-app 模块（pom 接线 → 实体/Helper/Controller/Mapper/BO/VO）+ app_user 建表 SQL。
2. yml security.excludes 放行 login/register。
3. 后端自检：`mvn compile`（或 IDE 编译）通过。
4. uni-app 骨架（config/request/upload/dict/auth store/useList + 最小 login 页 + me 页跑通登录/getInfo/上传头像）。
5. 验证：起后端 → uni-app H5（或小程序）注册→登录→getInfo→改头像（走 /resource/media/upload）→一个分页列表 demo。查库确认 app_user 落库、token 生效。
6. 验证通过 → `--no-ff` 合并 main（留回退点）。

## 验证方式（按 SOP）
需先起后端 + MySQL + MinIO。uni-app 以 H5 跑通：注册→登录（拿 token）→getInfo→上传头像（/resource/media/upload 返回 url，写回 avatar）→useList 分页 demo。无后端时先做 uni-app 构建/类型检查兜底 + 后端编译兜底。

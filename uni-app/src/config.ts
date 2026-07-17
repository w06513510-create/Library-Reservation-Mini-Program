// ============================================================
// 全局配置（每个新项目按需改这里）
// ============================================================

// 后端 BASE_URL：H5 走 vite 代理 /dev-api 避免跨域；小程序/App 直连后端地址。
// 目标端口须与基座 application.yml 的 server.port 一致（本模板默认 8199）。
let baseUrl = 'http://localhost:8199';
// #ifdef H5
baseUrl = '/dev-api';
// #endif
export const BASE_URL = baseUrl;

// C 端 clientid：前后端约定的固定串（框架安全拦截器只校验"请求头 clientid == 登录时写入 token 的 clientid"，
// 不查 sys_client 表）。改这里需与后端登录用值保持一致即可。
export const CLIENT_ID = 'app00000000000000000000000000001';

// 单租户默认租户号
export const TENANT_ID = '000000';

// 本地 token 存储键
export const TOKEN_KEY = 'app_token';

// 接口加密开关：默认关。开启需后端 api-decrypt + 前后端同一对 RSA 密钥（见 utils/crypto、jsencrypt）。
export const ENCRYPT = false;

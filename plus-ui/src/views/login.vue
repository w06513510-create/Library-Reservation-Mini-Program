<template>
  <div class="login">
    <!-- 左侧：暖阅读风品牌插画面板 -->
    <div class="login-hero">
      <div class="login-hero-inner">
        <svg class="login-illus" viewBox="0 0 440 320" fill="none" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="图书馆书架插画">
          <circle cx="220" cy="118" r="132" fill="#cc785c" opacity="0.10" />
          <circle cx="150" cy="86" r="72" fill="#e8a55a" opacity="0.12" />
          <rect x="70" y="236" width="300" height="12" rx="6" fill="#3d3d3a" />
          <rect x="78" y="248" width="284" height="6" rx="3" fill="#3d3d3a" opacity="0.25" />
          <g stroke="#2c2a27" stroke-opacity="0.14">
            <rect x="92" y="118" width="32" height="118" rx="4" fill="#cc785c" />
            <rect x="126" y="86" width="26" height="150" rx="4" fill="#e8a55a" />
            <rect x="154" y="132" width="30" height="104" rx="4" fill="#5db8a6" />
            <rect x="186" y="98" width="24" height="138" rx="4" fill="#2c2a27" />
            <rect x="212" y="116" width="30" height="120" rx="4" fill="#efe9de" />
            <rect x="244" y="90" width="28" height="146" rx="4" fill="#cc785c" />
            <rect x="274" y="126" width="32" height="110" rx="4" fill="#e8a55a" />
            <rect x="308" y="104" width="24" height="132" rx="4" fill="#5db8a6" />
          </g>
          <g fill="#faf9f5" opacity="0.5">
            <rect x="98" y="150" width="20" height="4" rx="2" />
            <rect x="131" y="118" width="16" height="4" rx="2" />
            <rect x="159" y="164" width="20" height="4" rx="2" />
            <rect x="190" y="130" width="16" height="4" rx="2" />
            <rect x="249" y="122" width="18" height="4" rx="2" />
            <rect x="279" y="158" width="22" height="4" rx="2" />
            <rect x="312" y="136" width="16" height="4" rx="2" />
          </g>
          <rect x="217" y="150" width="20" height="4" rx="2" fill="#2c2a27" opacity="0.4" />
          <g>
            <rect x="150" y="60" width="120" height="18" rx="5" fill="#faf9f5" stroke="#3d3d3a" stroke-opacity="0.2" />
            <rect x="150" y="60" width="120" height="6" rx="3" fill="#cc785c" opacity="0.85" />
            <rect x="248" y="60" width="10" height="34" fill="#cc785c" />
            <path d="M248 94 l5 -8 l5 8 z" fill="#a9583e" />
          </g>
          <circle cx="360" cy="70" r="4" fill="#e8a55a" />
          <circle cx="96" cy="70" r="3" fill="#cc785c" />
          <circle cx="386" cy="150" r="3" fill="#5db8a6" />
        </svg>
        <div class="login-hero-title">图书馆预约系统</div>
        <div class="login-hero-sub">座位 · 自习室预约 · 图书借阅流通 · 信用管理</div>
        <div class="login-hero-tags">
          <span>可视化选座</span><span>信用分体系</span><span>数据可视化大屏</span>
        </div>
        <ul class="login-hero-feats">
          <li>可视化平面图选座 / 寻书，一桌多座成组</li>
          <li>信用分体系 + SnailJob 定时任务自动处置</li>
          <li>座位 · 馆藏 · 信用 多维数据可视化大屏</li>
        </ul>
      </div>
      <div class="login-hero-foot">图书馆预约系统 · 毕业设计作品 © 2026</div>
    </div>
    <div class="login-right">
      <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <div class="title-box">
        <h3 class="title">{{ title }}</h3>
        <lang-select />
      </div>
      <p class="login-welcome">欢迎登录，请输入账号与密码</p>
      <!-- 去RuoYi化：租户选择器(多租户功能)，单租户项目用不到，已隐藏；需多租户改回 v-if="tenantEnabled" -->
      <el-form-item v-if="false" prop="tenantId">
        <el-select v-model="loginForm.tenantId" filterable :placeholder="proxy.$t('login.selectPlaceholder')" style="width: 100%">
          <el-option v-for="item in tenantList" :key="item.tenantId" :label="item.companyName" :value="item.tenantId"></el-option>
          <template #prefix><svg-icon icon-class="company" class="el-input__icon input-icon" /></template>
        </el-select>
      </el-form-item>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" size="large" auto-complete="off" :placeholder="proxy.$t('login.username')">
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          :placeholder="proxy.$t('login.password')"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="captchaEnabled" prop="code">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          :placeholder="proxy.$t('login.code')"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" class="login-code-img" @click="getCode" />
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin: 0 0 25px 0">{{ proxy.$t('login.rememberPassword') }}</el-checkbox>
      <!-- 去RuoYi化：第三方社交登录(微信/MaxKey/TopIam/Gitee/Github)RuoYi 自带，与业务无关，已隐藏 -->
      <el-form-item v-if="false" style="float: right">
        <el-button circle :title="proxy.$t('login.social.wechat')" @click="doSocialLogin('wechat')">
          <svg-icon icon-class="wechat" />
        </el-button>
        <el-button circle :title="proxy.$t('login.social.maxkey')" @click="doSocialLogin('maxkey')">
          <svg-icon icon-class="maxkey" />
        </el-button>
        <el-button circle :title="proxy.$t('login.social.topiam')" @click="doSocialLogin('topiam')">
          <svg-icon icon-class="topiam" />
        </el-button>
        <el-button circle :title="proxy.$t('login.social.gitee')" @click="doSocialLogin('gitee')">
          <svg-icon icon-class="gitee" />
        </el-button>
        <el-button circle :title="proxy.$t('login.social.github')" @click="doSocialLogin('github')">
          <svg-icon icon-class="github" />
        </el-button>
      </el-form-item>
      <el-form-item style="width: 100%">
        <el-button :loading="loading" size="large" type="primary" style="width: 100%" @click.prevent="handleLogin">
          <span v-if="!loading">{{ proxy.$t('login.login') }}</span>
          <span v-else>{{ proxy.$t('login.logging') }}</span>
        </el-button>
        <div v-if="register" style="float: right">
          <router-link class="link-type" :to="'/register'">{{ proxy.$t('login.switchRegisterPage') }}</router-link>
        </div>
      </el-form-item>
      </el-form>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2026 图书馆预约系统 · 毕业设计作品 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg, getTenantList } from '@/api/login';
import { authRouterUrl } from '@/api/system/social/auth';
import { useUserStore } from '@/store/modules/user';
import { LoginData, TenantVO } from '@/api/types';
import { to } from 'await-to-js';
import { HttpStatus } from '@/enums/RespEnum';
import { useI18n } from 'vue-i18n';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const title = import.meta.env.VITE_APP_TITLE;
const userStore = useUserStore();
const router = useRouter();
const { t } = useI18n();

const loginForm = ref<LoginData>({
  tenantId: '000000',
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
} as LoginData);

const loginRules: ElFormRules = {
  tenantId: [{ required: true, trigger: 'blur', message: t('login.rule.tenantId.required') }],
  username: [{ required: true, trigger: 'blur', message: t('login.rule.username.required') }],
  password: [{ required: true, trigger: 'blur', message: t('login.rule.password.required') }],
  code: [{ required: true, trigger: 'change', message: t('login.rule.code.required') }]
};

const codeUrl = ref('');
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(true);
// 租户开关
const tenantEnabled = ref(true);

// 注册开关
const register = ref(false);
const redirect = ref('/');
const loginRef = ref<ElFormInstance>();
// 租户列表
const tenantList = ref<TenantVO[]>([]);

watch(
  () => router.currentRoute.value,
  (newRoute: any) => {
    redirect.value = newRoute.query && newRoute.query.redirect && decodeURIComponent(newRoute.query.redirect);
  },
  { immediate: true }
);

const handleLogin = () => {
  loginRef.value?.validate(async (valid: boolean, fields: any) => {
    if (valid) {
      loading.value = true;
      // 勾选了需要记住密码设置在 localStorage 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        localStorage.setItem('tenantId', String(loginForm.value.tenantId));
        localStorage.setItem('username', String(loginForm.value.username));
        localStorage.setItem('password', String(loginForm.value.password));
        localStorage.setItem('rememberMe', String(loginForm.value.rememberMe));
      } else {
        // 否则移除
        localStorage.removeItem('tenantId');
        localStorage.removeItem('username');
        localStorage.removeItem('password');
        localStorage.removeItem('rememberMe');
      }
      // 调用action的登录方法
      const [err] = await to(userStore.login(loginForm.value));
      if (!err) {
        const redirectUrl = redirect.value || '/';
        await router.push(redirectUrl);
        loading.value = false;
      } else {
        loading.value = false;
        // 重新获取验证码
        if (captchaEnabled.value) {
          await getCode();
        }
      }
    } else {
      console.log('error submit!', fields);
    }
  });
};

/**
 * 获取验证码
 */
const getCode = async () => {
  const res = await getCodeImg();
  const { data } = res;
  captchaEnabled.value = data.captchaEnabled === undefined ? true : data.captchaEnabled;
  if (captchaEnabled.value) {
    // 刷新验证码时清空输入框
    loginForm.value.code = '';
    codeUrl.value = 'data:image/gif;base64,' + data.img;
    loginForm.value.uuid = data.uuid;
  }
};

const getLoginData = () => {
  const tenantId = localStorage.getItem('tenantId');
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
  const rememberMe = localStorage.getItem('rememberMe');
  loginForm.value = {
    tenantId: tenantId === null ? String(loginForm.value.tenantId) : tenantId,
    username: username === null ? String(loginForm.value.username) : username,
    password: password === null ? String(loginForm.value.password) : String(password),
    rememberMe: rememberMe === null ? false : Boolean(rememberMe)
  } as LoginData;
};

/**
 * 获取租户列表
 */
const initTenantList = async () => {
  const { data } = await getTenantList(false);
  tenantEnabled.value = data.tenantEnabled === undefined ? true : data.tenantEnabled;
  if (tenantEnabled.value) {
    tenantList.value = data.voList;
    if (tenantList.value != null && tenantList.value.length !== 0) {
      loginForm.value.tenantId = tenantList.value[0].tenantId;
    }
  }
};

/**
 * 第三方登录
 * @param type
 */
const doSocialLogin = (type: string) => {
  authRouterUrl(type, loginForm.value.tenantId).then((res: any) => {
    if (res.code === HttpStatus.SUCCESS) {
      // 获取授权地址跳转
      window.location.href = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

onMounted(() => {
  getCode();
  initTenantList();
  getLoginData();
});
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  align-items: stretch;
  height: 100%;
  position: relative;
  overflow: hidden;
  // 暖阅读风（Claude DESIGN.md）：奶油画布 + 珊瑚/琥珀柔光，取代蓝色科技图
  background:
    radial-gradient(920px 520px at 12% 16%, rgba(204, 120, 92, 0.18), transparent 60%),
    radial-gradient(780px 480px at 88% 84%, rgba(232, 165, 90, 0.16), transparent 62%),
    radial-gradient(640px 640px at 82% 10%, rgba(93, 184, 166, 0.08), transparent 60%),
    #faf9f5;
}

// 极淡暖色点阵纹理，增加编辑式质感
.login::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(20, 20, 19, 0.05) 1px, transparent 1px);
  background-size: 22px 22px;
  pointer-events: none;
}

// 左侧品牌插画面板
.login-hero {
  flex: 1.25;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px 64px;
  position: relative;
  z-index: 1;
  border-right: 1px solid #e6dfd8;
  background:
    radial-gradient(700px 520px at 28% 22%, rgba(204, 120, 92, 0.22), transparent 60%),
    radial-gradient(600px 480px at 82% 88%, rgba(232, 165, 90, 0.2), transparent 62%),
    linear-gradient(150deg, #f6eee5 0%, #efe6d9 100%);
}
.login-hero-inner {
  max-width: 460px;
  text-align: center;
}
.login-illus {
  width: 100%;
  max-width: 420px;
  height: auto;
  display: block;
  margin: 0 auto 14px;
}
.login-hero-title {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #252523;
}
.login-hero-sub {
  margin-top: 10px;
  font-size: 14px;
  color: #6c6a64;
  letter-spacing: 1px;
}
.login-hero-tags {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
.login-hero-tags span {
  padding: 5px 14px;
  font-size: 12px;
  border-radius: 999px;
  color: #a9583e;
  background: rgba(204, 120, 92, 0.12);
  border: 1px solid rgba(204, 120, 92, 0.25);
}
.login-hero-feats {
  list-style: none;
  margin: 26px auto 0;
  padding: 0;
  display: inline-block;
  text-align: left;
}
.login-hero-feats li {
  position: relative;
  padding-left: 20px;
  margin: 12px 0;
  font-size: 14px;
  color: #5a574f;
  line-height: 1.5;
}
.login-hero-feats li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 7px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cc785c;
}
.login-hero-foot {
  position: absolute;
  bottom: 28px;
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: #8e8b82;
}
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}
.login-welcome {
  margin: -14px 0 22px;
  text-align: center;
  font-size: 13px;
  color: #8e8b82;
  letter-spacing: 0.5px;
}

@media (max-width: 900px) {
  .login-hero {
    display: none;
  }
}

.title-box {
  display: flex;
  align-items: center;
  gap: 8px;

  .title {
    margin: 0px auto 26px auto;
    text-align: center;
    color: var(--el-text-color-primary);
    font-weight: 600;
    letter-spacing: 0.5px;
  }

  :deep(.lang-select--style) {
    line-height: 0;
    color: var(--el-text-color-secondary);
  }
}

.login-form {
  border-radius: var(--app-radius-lg);
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.5);
  width: min(420px, 90vw);
  padding: 32px 30px 12px 30px;
  z-index: 1;
  box-shadow: var(--app-shadow-lg);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }

  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-form :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.9);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(204, 120, 92, 0.22);
}

.login-form :deep(.el-button--primary) {
  border-radius: var(--app-radius-md);
  box-shadow: 0 8px 20px rgba(204, 120, 92, 0.28);
}

.login-form :deep(.el-button.is-circle) {
  background: rgba(20, 20, 19, 0.04);
  border: 1px solid rgba(20, 20, 19, 0.08);
  color: var(--el-text-color-regular);
}

.login-form :deep(.el-button.is-circle:hover) {
  background: rgba(204, 120, 92, 0.12);
  border-color: rgba(204, 120, 92, 0.28);
}

.login-code {
  width: calc(37% - 10px);
  height: 40px;
  float: right;
  margin-left: 10px;
  box-sizing: border-box;
  border-radius: var(--app-radius-sm);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--el-border-color-light);

  img {
    cursor: pointer;
    vertical-align: middle;
    display: block;
    width: 100%;
    height: 40px;
    object-fit: cover;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgba(60, 60, 58, 0.6);
  font-family: Arial, serif;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-code-img {
  height: 40px;
  padding-left: 0;
}

:global(html.dark) {
  .login {
    // 暗色：暖近黑画布 + 珊瑚/琥珀柔光
    background:
      radial-gradient(920px 520px at 12% 16%, rgba(204, 120, 92, 0.22), transparent 60%),
      radial-gradient(780px 480px at 88% 84%, rgba(232, 165, 90, 0.16), transparent 62%),
      #141312;
  }

  .login::before {
    background-image: radial-gradient(rgba(250, 249, 245, 0.05) 1px, transparent 1px);
  }

  .login-hero {
    border-right-color: rgba(230, 223, 216, 0.12);
    background:
      radial-gradient(700px 520px at 28% 22%, rgba(204, 120, 92, 0.26), transparent 60%),
      radial-gradient(600px 480px at 82% 88%, rgba(232, 165, 90, 0.18), transparent 62%),
      linear-gradient(150deg, #1b1a17 0%, #141312 100%);
  }
  .login-hero-title {
    color: #faf9f5;
  }
  .login-hero-sub {
    color: #a09d96;
  }
  .login-hero-feats li {
    color: #c9c5bd;
  }
  .login-hero-foot {
    color: #8e8b82;
  }

  .login-form {
    background: rgba(31, 30, 27, 0.92);
    border-color: rgba(230, 223, 216, 0.14);
  }

  .login-form :deep(.el-input__wrapper) {
    background-color: rgba(17, 24, 39, 0.7);
  }

  .login-form :deep(.el-button.is-circle) {
    background: rgba(148, 163, 184, 0.12);
    border-color: rgba(148, 163, 184, 0.25);
    color: #e5e7eb;
  }

  .el-login-footer {
    color: rgba(226, 232, 240, 0.65);
  }
}
</style>

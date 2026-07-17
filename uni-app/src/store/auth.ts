import { defineStore } from 'pinia';
import { setToken, clearToken, getToken } from '../utils/request';
import { apiLogin, apiRegister, apiGetInfo, apiLogout, type AppUserVo } from '../api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: {} as Partial<AppUserVo>
  }),
  getters: {
    isLogin: (s) => !!s.token,
    nickname: (s) => s.user.nickname || '未登录'
  },
  actions: {
    async login(phone: string, password: string) {
      const res = await apiLogin(phone, password);
      this.token = res.data!.token;
      setToken(this.token);
      await this.getInfo();
    },
    async register(phone: string, password: string, nickname?: string) {
      await apiRegister(phone, password, nickname);
    },
    async getInfo() {
      const res = await apiGetInfo();
      this.user = res.data || {};
      return this.user;
    },
    logout() {
      apiLogout().catch(() => {});
      this.token = '';
      this.user = {};
      clearToken();
    }
  }
});

<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app';
import { useAuthStore } from './store/auth';
import { getToken } from './utils/request';

onLaunch(() => {
  // 冷启动：若本地有 token，静默拉取用户信息（失败忽略，不打断进入首页）
  if (getToken()) {
    const authStore = useAuthStore();
    authStore.getInfo().catch(() => {});
  }
});
</script>

<style lang="scss">
page {
  background-color: #f5f6f8;
  color: #303133;
  font-size: 28rpx;
}
</style>

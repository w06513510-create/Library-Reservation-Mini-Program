import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5188,
    proxy: {
      // H5 开发走代理避免跨域；目标为基座后端端口(见 SOP 01 / application.yml server.port)
      '/dev-api': {
        target: 'http://localhost:8199',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/dev-api/, '')
      }
    }
  }
});

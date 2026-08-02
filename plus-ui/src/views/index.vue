<template>
  <div class="app-container lib-home">
    <!-- 欢迎横幅 -->
    <div class="hero">
      <div class="hero-text">
        <h1>{{ title }}</h1>
        <p>座位 / 自习室预约 · 图书借阅流通 · 信用管理 一体化管理平台</p>
        <div class="hero-tags">
          <span class="tag">可视化选座 · 寻书</span>
          <span class="tag">信用分体系 · 定时任务</span>
          <span class="tag">数据可视化大屏</span>
        </div>
      </div>
      <div class="hero-icon"><el-icon><Reading /></el-icon></div>
    </div>

    <!-- 三大亮点 -->
    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :sm="8" v-for="h in highlights" :key="h.title">
        <el-card shadow="hover" class="hl-card" :body-style="{ padding: '18px' }">
          <div class="hl-head">
            <el-icon class="hl-icon" :style="{ background: h.bg }"><component :is="h.icon" /></el-icon>
            <span class="hl-title">{{ h.title }}</span>
          </div>
          <div class="hl-desc">{{ h.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <div class="section-title">快捷入口</div>
    <el-row :gutter="16">
      <el-col :xs="12" :sm="6" :md="4" v-for="e in entries" :key="e.path">
        <div class="entry" @click="go(e.path)">
          <el-icon class="entry-icon" :style="{ color: e.color }"><component :is="e.icon" /></el-icon>
          <div class="entry-label">{{ e.label }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index" lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const title = import.meta.env.VITE_APP_TITLE || '图书馆预约系统';

const highlights = [
  { title: '可视化平面图选座 / 寻书', icon: 'Location', bg: '#e8f3ff', desc: '按坐标还原楼层平面，实时呈现座位空闲 / 占用；图书按索书号排架到书架，读者可视化寻书。' },
  { title: '信用分体系 + 定时任务', icon: 'TrendCharts', bg: '#e8fff3', desc: '爽约 / 逾期 / 超时自动扣分与黑名单联动；SnailJob 定时扫描自动释放、判定违约、恢复信用。' },
  { title: '数据可视化大屏', icon: 'DataLine', bg: '#fff4e8', desc: '座位占用、馆藏流通、违约构成、信用分布多维实时聚合，一屏总览运行态势。' }
];

const entries = [
  { label: '平面图选座', path: '/library/seatmap', icon: 'Location', color: '#3b82f6' },
  { label: '数据大屏', path: '/library/dashboard', icon: 'DataLine', color: '#22c55e' },
  { label: '座位预约', path: '/library/reservation', icon: 'Calendar', color: '#f59e0b' },
  { label: '研讨间预约', path: '/library/roomReservation', icon: 'OfficeBuilding', color: '#8b5cf6' },
  { label: '借阅管理', path: '/library/loan', icon: 'Reading', color: '#06b6d4' },
  { label: '图书档案', path: '/library/book', icon: 'Notebook', color: '#ef4444' }
];

const go = (path: string) => router.push(path).catch(() => {});
</script>

<style lang="scss" scoped>
.lib-home {
  padding: 16px;
}
.mt-16 {
  margin-top: 16px;
}
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26px 30px;
  border-radius: 14px;
  background: linear-gradient(120deg, #1d4ed8 0%, #2563eb 40%, #38bdf8 100%);
  color: #fff;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.25);
}
.hero-text h1 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 2px;
}
.hero-text p {
  margin: 0 0 14px;
  font-size: 15px;
  opacity: 0.92;
}
.hero-tags .tag {
  display: inline-block;
  margin-right: 10px;
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(2px);
}
.hero-icon {
  font-size: 92px;
  opacity: 0.28;
}
.hl-card {
  border-radius: 12px;
  margin-bottom: 16px;
  height: 100%;
}
.hl-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.hl-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  font-size: 22px;
  color: #2563eb;
}
.hl-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
}
.hl-desc {
  font-size: 13px;
  line-height: 1.7;
  color: #667085;
}
.section-title {
  margin: 22px 0 14px;
  font-size: 17px;
  font-weight: 700;
  color: #1f2d3d;
  padding-left: 10px;
  border-left: 4px solid #2563eb;
}
.entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 22px 10px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eef0f4;
  cursor: pointer;
  transition: all 0.2s;
}
.entry:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: #d6e4ff;
}
.entry-icon {
  font-size: 32px;
}
.entry-label {
  font-size: 14px;
  color: #344054;
  font-weight: 600;
}
</style>

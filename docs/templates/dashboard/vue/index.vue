<!--
  看板页模板（KPI 卡片 + 4 图：状态饼图 / 分类柱图 / 近7日趋势折线 / Top10 横向柱）
  提炼自跑腿 plus-ui/src/views/errand/dashboard/index.vue，echarts 用法对齐基座 monitor/cache/index.vue。
  放到 plus-ui/src/views/biz/dashboard/index.vue（把 biz 换成你的业务）。
  依赖：基座已装 echarts 6.0.0；echarts 'macarons' 主题为基座内置。
-->
<template>
  <div class="p-2">
    <!-- 工具条 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
      <span style="color: #909399; font-size: 13px">更新于 {{ updateTime }}</span>
      <el-button type="primary" icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <!-- KPI 卡片 -->
    <el-row :gutter="12">
      <el-col v-for="card in metricCards" :key="card.label" :xs="12" :sm="8" :md="6" :lg="3">
        <el-card shadow="hover" class="card-box">
          <div class="metric-value">{{ card.value }}</div>
          <div class="metric-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表：第一行 状态饼图 + 分类柱图 -->
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :md="8">
        <el-card shadow="hover">
          <template #header>状态分布</template>
          <div ref="statusChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :md="8">
        <el-card shadow="hover">
          <template #header>分类分布</template>
          <div ref="categoryChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :md="8">
        <el-card shadow="hover">
          <template #header>Top10 排行</template>
          <div ref="rankChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表：第二行 近7日趋势 -->
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :md="24">
        <el-card shadow="hover">
          <template #header>近7日趋势</template>
          <div ref="trendChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="BizDashboard" lang="ts">
import * as echarts from 'echarts';
import { getDashboardData } from '@/api/biz/dashboard';
import { BizDashboardVO } from '@/api/biz/dashboard/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const dashboard = ref<BizDashboardVO>();
const updateTime = ref('');

// 每个图一个 ref + 一个实例句柄
const statusChartRef = ref<HTMLElement>();
const categoryChartRef = ref<HTMLElement>();
const trendChartRef = ref<HTMLElement>();
const rankChartRef = ref<HTMLElement>();
let statusChart: echarts.ECharts | null = null;
let categoryChart: echarts.ECharts | null = null;
let trendChart: echarts.ECharts | null = null;
let rankChart: echarts.ECharts | null = null;

// 数值兜底（后端可能下发字符串）
const num = (v: unknown) => Number(v ?? 0) || 0;

// KPI 卡片（按业务改标签与字段）
const metricCards = computed(() => {
  const o = dashboard.value?.overview;
  return [
    { label: '总数', value: num(o?.total) },
    { label: '今日', value: num(o?.today) },
    { label: '近7日', value: num(o?.week) },
    { label: '已完成', value: num(o?.completed) },
    { label: '已取消', value: num(o?.cancelled) },
    { label: '金额(¥)', value: num(o?.totalAmount) },
    { label: '活跃用户', value: num(o?.activeUsers) },
    { label: '完成率(%)', value: num(o?.completionRate) }
  ];
});

// 状态码 -> 文案（建议改用 proxy.useDict 字典驱动）
const statusName = (code: unknown) => ({ 0: '待处理', 5: '已完成', 6: '已取消', 8: '已关闭' } as Record<string, string>)[String(code)] ?? String(code);

const initCharts = () => {
  if (!statusChart) statusChart = echarts.init(statusChartRef.value!, 'macarons');
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value!, 'macarons');
  if (!trendChart) trendChart = echarts.init(trendChartRef.value!, 'macarons');
  if (!rankChart) rankChart = echarts.init(rankChartRef.value!, 'macarons');
};

const renderStatus = () => {
  const list = (dashboard.value?.statusDist ?? []).map((i) => ({ name: statusName(i.status), value: num(i.value) }));
  statusChart?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { type: 'scroll', bottom: 0, left: 'center' },
    series: [
      {
        name: '状态',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        data: list
      }
    ]
  });
};

const renderCategory = () => {
  const list = dashboard.value?.categoryDist ?? [];
  categoryChart?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: list.map((i) => String(i.category)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '数量', type: 'bar', barMaxWidth: 40, data: list.map((i) => num(i.value)) }]
  });
};

const renderRank = () => {
  const list = [...(dashboard.value?.rank ?? [])].reverse(); // 横向柱：倒序让第一名在顶部
  rankChart?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '6%', bottom: '3%', top: '6%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: list.map((i) => i.name) },
    series: [{ name: '值', type: 'bar', barMaxWidth: 22, data: list.map((i) => num(i.value)) }]
  });
};

const renderTrend = () => {
  // 零填充最近 7 天，避免无数据的日期断线
  const days: string[] = [];
  const map = new Map<string, { count: number; amount: number }>();
  (dashboard.value?.trend ?? []).forEach((i) => map.set(i.date, { count: num(i.count), amount: num(i.amount) }));
  for (let d = 6; d >= 0; d--) {
    const dt = new Date();
    dt.setDate(dt.getDate() - d);
    const key = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
    days.push(key);
  }
  const counts = days.map((k) => map.get(k)?.count ?? 0);
  const amounts = days.map((k) => map.get(k)?.amount ?? 0);
  trendChart?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['数量', '金额(¥)'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: days },
    yAxis: [
      { type: 'value', name: '数量', minInterval: 1 },
      { type: 'value', name: '金额(¥)' }
    ],
    series: [
      { name: '数量', type: 'line', smooth: true, yAxisIndex: 0, areaStyle: { opacity: 0.15 }, data: counts },
      { name: '金额(¥)', type: 'line', smooth: true, yAxisIndex: 1, data: amounts }
    ]
  });
};

const renderAll = () => {
  renderStatus();
  renderCategory();
  renderRank();
  renderTrend();
};

const handleResize = () => {
  statusChart?.resize();
  categoryChart?.resize();
  trendChart?.resize();
  rankChart?.resize();
};

const loadData = async () => {
  proxy?.$modal.loading('正在加载看板数据，请稍候！');
  try {
    const res = await getDashboardData();
    dashboard.value = res.data;
    updateTime.value = new Date().toLocaleString();
    await nextTick();
    initCharts();
    renderAll();
  } finally {
    proxy?.$modal.closeLoading();
  }
};

onMounted(() => {
  loadData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  statusChart?.dispose();
  categoryChart?.dispose();
  trendChart?.dispose();
  rankChart?.dispose();
  statusChart = categoryChart = trendChart = rankChart = null;
});
</script>

<style scoped>
.card-box {
  margin-bottom: 12px;
  text-align: center;
}
.metric-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}
.metric-label {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.chart-box {
  width: 100%;
  height: 320px;
}
</style>

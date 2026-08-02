<template>
  <div class="lib-screen">
    <!-- 顶栏 -->
    <div class="screen-header">
      <div class="sh-side">馆藏 {{ d.itemTotal ?? 0 }} 册 · 读者 {{ d.readerTotal ?? 0 }} 人</div>
      <div class="sh-title">图书馆预约系统 · 数据可视化大屏</div>
      <div class="sh-side sh-right">
        <span class="clock">{{ clock }}</span>
        <el-button size="small" type="primary" plain icon="Refresh" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- KPI 卡片 -->
    <div class="kpi-row">
      <div class="kpi" v-for="k in kpis" :key="k.label">
        <div class="kpi-val" :style="{ color: k.color }">{{ k.value }}<span v-if="k.suffix" class="kpi-suffix">{{ k.suffix }}</span></div>
        <div class="kpi-label">{{ k.label }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="panel">
        <div class="panel-title">座位实时占用率</div>
        <div ref="gaugeRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">座位状态构成</div>
        <div ref="seatRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">馆藏状态占比</div>
        <div ref="itemRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">违约类型构成</div>
        <div ref="violRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">读者信用分分布</div>
        <div ref="creditRef" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">座位预约状态构成</div>
        <div ref="rsvRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup name="LibraryDashboard" lang="ts">
import * as echarts from 'echarts';
import { getDashboardOverview } from '@/api/library/dashboard';

const d = ref<any>({});
const clock = ref('');
const gaugeRef = ref<HTMLElement>();
const seatRef = ref<HTMLElement>();
const itemRef = ref<HTMLElement>();
const violRef = ref<HTMLElement>();
const creditRef = ref<HTMLElement>();
const rsvRef = ref<HTMLElement>();
const charts: echarts.ECharts[] = [];
let clockTimer: any = null;

const PALETTE = ['#22d3ee', '#3b82f6', '#34d399', '#fbbf24', '#f87171', '#a78bfa', '#f472b6'];

const kpis = computed(() => [
  { label: '座位总数', value: d.value.seatTotal ?? 0, color: '#22d3ee' },
  { label: '当前占用', value: d.value.seatOccupied ?? 0, color: '#f87171' },
  { label: '馆藏总册', value: d.value.itemTotal ?? 0, color: '#34d399' },
  { label: '可借册数', value: d.value.itemAvail ?? 0, color: '#22d3ee' },
  { label: '在借', value: d.value.loanOnLoan ?? 0, color: '#fbbf24' },
  { label: '逾期', value: d.value.loanOverdue ?? 0, color: '#f87171' },
  { label: '预约在途', value: d.value.holdInTransit ?? 0, color: '#a78bfa' },
  { label: '今日借出', value: d.value.todayBorrow ?? 0, color: '#34d399' },
  { label: '今日归还', value: d.value.todayReturn ?? 0, color: '#3b82f6' },
  { label: '读者总数', value: d.value.readerTotal ?? 0, color: '#22d3ee' },
  { label: '黑名单', value: d.value.blacklistCount ?? 0, color: '#f87171' },
  { label: '研讨间', value: d.value.roomTotal ?? 0, color: '#a78bfa' }
]);

const axisDark = {
  axisLine: { lineStyle: { color: '#2b4a6b' } },
  axisLabel: { color: '#9fc2e0' },
  splitLine: { lineStyle: { color: 'rgba(80,120,160,0.15)' } }
};

const pieOption = (data: any[]) => ({
  color: PALETTE,
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, textStyle: { color: '#9fc2e0' }, itemWidth: 10, itemHeight: 10 },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { color: '#cfe8ff', fontSize: 11 },
      labelLine: { lineStyle: { color: '#2b4a6b' } },
      data: (data || []).map((x) => ({ name: x.name, value: x.value }))
    }
  ]
});

const barOption = (data: any[], color: string) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 16, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: (data || []).map((x) => x.name), ...axisDark, axisLabel: { color: '#9fc2e0', interval: 0, rotate: (data || []).length > 5 ? 24 : 0, fontSize: 11 } },
  yAxis: { type: 'value', ...axisDark },
  series: [
    {
      type: 'bar',
      barWidth: '52%',
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color }, { offset: 1, color: 'rgba(34,211,238,0.15)' }]), borderRadius: [4, 4, 0, 0] },
      data: (data || []).map((x) => x.value)
    }
  ]
});

const gaugeOption = (percent: number) => ({
  series: [
    {
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      min: 0,
      max: 100,
      progress: { show: true, width: 16, itemStyle: { color: '#22d3ee' } },
      axisLine: { lineStyle: { width: 16, color: [[1, 'rgba(80,120,160,0.25)']] } },
      axisTick: { show: false },
      splitLine: { distance: -18, length: 8, lineStyle: { color: '#2b4a6b' } },
      axisLabel: { distance: -2, color: '#7fa8c8', fontSize: 10 },
      pointer: { itemStyle: { color: '#22d3ee' } },
      anchor: { show: true, itemStyle: { color: '#22d3ee' } },
      detail: { valueAnimation: true, formatter: '{value}%', color: '#e6f6ff', fontSize: 26, offsetCenter: [0, '38%'] },
      title: { color: '#9fc2e0', offsetCenter: [0, '68%'], fontSize: 12 },
      data: [{ value: percent, name: '占用率' }]
    }
  ]
});

const renderCharts = () => {
  const usable = Math.max(0, (d.value.seatTotal ?? 0) - (d.value.seatDisabled ?? 0));
  const percent = usable > 0 ? Math.round(((d.value.seatOccupied ?? 0) / usable) * 100) : 0;
  const defs: [HTMLElement | undefined, any][] = [
    [gaugeRef.value, gaugeOption(percent)],
    [seatRef.value, pieOption(d.value.seatStatus)],
    [itemRef.value, pieOption(d.value.itemStatus)],
    [violRef.value, barOption(d.value.violationTypes, '#f472b6')],
    [creditRef.value, barOption(d.value.creditDist, '#34d399')],
    [rsvRef.value, pieOption(d.value.reservationStatus)]
  ];
  defs.forEach(([el, opt], i) => {
    if (!el) return;
    let chart = charts[i];
    if (!chart) {
      chart = echarts.init(el);
      charts[i] = chart;
    }
    chart.setOption(opt);
  });
};

const load = async () => {
  const res: any = await getDashboardOverview();
  d.value = res.data || {};
  nextTick(renderCharts);
};

const tickClock = () => {
  const n = new Date();
  const p = (x: number) => String(x).padStart(2, '0');
  clock.value = `${n.getFullYear()}-${p(n.getMonth() + 1)}-${p(n.getDate())} ${p(n.getHours())}:${p(n.getMinutes())}:${p(n.getSeconds())}`;
};

const onResize = () => charts.forEach((c) => c && c.resize());

onMounted(() => {
  tickClock();
  clockTimer = setInterval(tickClock, 1000);
  load();
  window.addEventListener('resize', onResize);
});
onUnmounted(() => {
  clearInterval(clockTimer);
  window.removeEventListener('resize', onResize);
  charts.forEach((c) => c && c.dispose());
});
</script>

<style scoped>
.lib-screen {
  min-height: calc(100vh - 84px);
  padding: 16px;
  background: radial-gradient(1200px 600px at 50% -10%, #12395e 0%, #0a1b2e 55%, #071320 100%);
  color: #cfe8ff;
}
.screen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px 14px;
}
.sh-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(90deg, #22d3ee, #7dd3fc, #a78bfa);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 18px rgba(34, 211, 238, 0.25);
}
.sh-side {
  width: 320px;
  font-size: 13px;
  color: #8fb4d6;
}
.sh-right {
  text-align: right;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  align-items: center;
}
.clock {
  font-family: Consolas, monospace;
  color: #7dd3fc;
}
.kpi-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}
.kpi {
  background: linear-gradient(180deg, rgba(23, 62, 99, 0.55), rgba(12, 33, 55, 0.55));
  border: 1px solid rgba(56, 130, 180, 0.35);
  border-radius: 10px;
  padding: 14px 10px;
  text-align: center;
  box-shadow: inset 0 0 20px rgba(34, 211, 238, 0.06);
}
.kpi-val {
  font-size: 30px;
  font-weight: 800;
  font-family: Consolas, monospace;
  line-height: 1;
}
.kpi-suffix {
  font-size: 13px;
  margin-left: 2px;
}
.kpi-label {
  margin-top: 8px;
  font-size: 13px;
  color: #8fb4d6;
}
.chart-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.panel {
  background: linear-gradient(180deg, rgba(16, 45, 74, 0.5), rgba(10, 27, 46, 0.5));
  border: 1px solid rgba(56, 130, 180, 0.3);
  border-radius: 10px;
  padding: 8px 8px 4px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #bfe3ff;
  padding: 4px 6px 2px;
  border-left: 3px solid #22d3ee;
  margin-bottom: 4px;
}
.chart {
  width: 100%;
  height: 260px;
}
</style>

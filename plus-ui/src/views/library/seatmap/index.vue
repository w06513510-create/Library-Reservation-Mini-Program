<template>
  <div class="p-2">
    <!-- 控制栏 -->
    <el-card shadow="hover" class="mb-[10px]">
      <el-form :inline="true" class="seatmap-toolbar">
        <el-form-item label="场馆">
          <el-select v-model="venueId" placeholder="选择场馆" style="width: 160px" @change="onVenueChange">
            <el-option v-for="v in venueOptions" :key="v.id" :label="v.venueName" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="floorId" placeholder="选择楼层" style="width: 180px" @change="loadSeats">
            <el-option v-for="f in floorOptions" :key="f.id" :label="f.floorName" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="reserveDate" type="date" value-format="YYYY-MM-DD" :clearable="false" style="width: 150px" @change="loadSeats" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="slot" style="width: 170px" @change="loadSeats">
            <el-option v-for="s in slotOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="loadSeats">刷新座位</el-button>
        </el-form-item>
      </el-form>

      <!-- 图例 + 统计 -->
      <div class="seatmap-legend">
        <span class="lg"><i class="dot free"></i>空闲 {{ stat.free }}</span>
        <span class="lg"><i class="dot occupied"></i>已占用 {{ stat.occupied }}</span>
        <span class="lg"><i class="dot selected"></i>已选</span>
        <span class="lg"><i class="dot disabled"></i>停用 {{ stat.disabled }}</span>
        <span class="lg-tip">⚡=有插座　类型：普通/靠窗/沙发/单间　共 {{ seats.length }} 座</span>
      </div>
    </el-card>

    <!-- 平面图 -->
    <el-card shadow="hover">
      <div v-loading="loading" class="floor-wrap">
        <div v-if="!floorId" class="floor-empty">请选择场馆与楼层查看平面图</div>
        <div v-else-if="seats.length === 0" class="floor-empty">该楼层暂无座位数据（可到「座位管理」维护坐标）</div>
        <div v-else class="floor-plan" :style="planStyle">
          <el-tooltip v-for="s in seats" :key="s.id" placement="top" :show-after="120">
            <template #content>
              <div>座位 {{ s.seatNo }} · {{ areaMap[s.areaId] || '' }}</div>
              <div>{{ seatTypeText(s.seatType) }}{{ s.hasPower === 1 ? ' · 有插座' : '' }}</div>
              <div>状态：{{ seatStateText(s) }}</div>
            </template>
            <div
              class="seat"
              :class="seatClass(s)"
              :style="{ left: (s.posX || 0) + 'px', top: (s.posY || 0) + 'px' }"
              @click="clickSeat(s)"
            >
              <span class="seat-no">{{ s.seatNo }}</span>
              <span v-if="s.hasPower === 1" class="seat-power">⚡</span>
            </div>
          </el-tooltip>
        </div>
      </div>
    </el-card>

    <!-- 下单对话框 -->
    <el-dialog v-model="dialog.visible" title="预约选座" width="460px" append-to-body>
      <el-descriptions :column="1" border size="small" class="mb-[12px]">
        <el-descriptions-item label="座位">{{ current?.seatNo }}（{{ areaMap[current?.areaId] || '' }} · {{ seatTypeText(current?.seatType) }}{{ current?.hasPower === 1 ? ' · 有插座' : '' }}）</el-descriptions-item>
        <el-descriptions-item label="日期">{{ reserveDate }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ currentSlot?.label }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="buttonLoading" type="primary" @click="submit">确认预约</el-button>
        <el-button @click="dialog.visible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SeatMap" lang="ts">
import { getSeatStatus, addReservation } from '@/api/library/reservation';
import { listVenue } from '@/api/library/venue';
import { listFloor } from '@/api/library/floor';
import { listReader } from '@/api/library/reader';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const venueOptions = ref<any[]>([]);
const floorOptions = ref<any[]>([]);
const readerOptions = ref<any[]>([]);
const seats = ref<any[]>([]);
const areaMap = ref<Record<string, string>>({});

const venueId = ref<any>();
const floorId = ref<any>();
const reserveDate = ref<string>('');
const slot = ref('pm');
const loading = ref(false);
const buttonLoading = ref(false);

const slotOptions = [
  { value: 'am', label: '上午 08:00-12:00', start: '08:00:00', end: '12:00:00' },
  { value: 'pm', label: '下午 14:00-18:00', start: '14:00:00', end: '18:00:00' },
  { value: 'night', label: '晚上 18:00-22:00', start: '18:00:00', end: '22:00:00' }
];
const currentSlot = computed(() => slotOptions.find((s) => s.value === slot.value));

const selectedFloor = computed(() => floorOptions.value.find((f) => f.id === floorId.value));
const planStyle = computed(() => {
  const url = selectedFloor.value?.floorPlanUrl;
  return url ? { backgroundImage: `url(${url})`, backgroundSize: 'cover' } : {};
});

const stat = computed(() => {
  let free = 0, occupied = 0, disabled = 0;
  for (const s of seats.value) {
    if (s.seatStatus === 1) disabled++;
    else if (s.occupied) occupied++;
    else free++;
  }
  return { free, occupied, disabled };
});

const dialog = reactive<DialogOption>({ visible: false, title: '' });
const current = ref<any>();
const formRef = ref<ElFormInstance>();
const form = reactive<{ readerId?: any }>({ readerId: undefined });
const rules = { readerId: [{ required: true, message: '请选择读者', trigger: 'change' }] };

const seatTypeText = (t?: number) => (['普通', '靠窗', '沙发', '单间'][t ?? 0] || '普通');
const seatStateText = (s: any) => (s.seatStatus === 1 ? '停用' : s.occupied ? '已占用' : '空闲');
const seatClass = (s: any) => {
  if (s.seatStatus === 1) return 'disabled';
  if (s.occupied) return 'occupied';
  if (current.value && current.value.id === s.id) return 'selected';
  return 'free';
};

const initDate = () => {
  const d = new Date();
  reserveDate.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const onVenueChange = async () => {
  floorId.value = undefined;
  seats.value = [];
  const res = await listFloor({ pageNum: 1, pageSize: 999, venueId: venueId.value } as any);
  floorOptions.value = res.rows;
};

const loadSeats = async () => {
  if (!floorId.value) return;
  loading.value = true;
  const start = `${reserveDate.value} ${currentSlot.value?.start}`;
  const end = `${reserveDate.value} ${currentSlot.value?.end}`;
  try {
    const res: any = await getSeatStatus(floorId.value, start, end);
    seats.value = res.data || [];
    const m: Record<string, string> = {};
    seats.value.forEach((s) => (m[s.areaId] = s.areaName));
    areaMap.value = m;
  } finally {
    loading.value = false;
  }
};

const clickSeat = (s: any) => {
  if (s.seatStatus === 1) {
    proxy?.$modal.msgWarning('该座位已停用');
    return;
  }
  if (s.occupied) {
    proxy?.$modal.msgWarning('该座位在所选时段已被占用');
    return;
  }
  current.value = s;
  form.readerId = undefined;
  dialog.visible = true;
};

const submit = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    buttonLoading.value = true;
    const start = `${reserveDate.value} ${currentSlot.value?.start}`;
    const end = `${reserveDate.value} ${currentSlot.value?.end}`;
    try {
      await addReservation({ readerId: form.readerId, seatId: current.value.id, reserveDate: reserveDate.value, startTime: start, endTime: end, source: 1 } as any);
      proxy?.$modal.msgSuccess(`已为 ${current.value.seatNo} 预约成功`);
      dialog.visible = false;
      await loadSeats();
    } finally {
      buttonLoading.value = false;
    }
  });
};

onMounted(async () => {
  initDate();
  const [v, r] = await Promise.all([listVenue({ pageNum: 1, pageSize: 999 } as any), listReader({ pageNum: 1, pageSize: 999 } as any)]);
  venueOptions.value = v.rows;
  readerOptions.value = r.rows;
  if (venueOptions.value.length) {
    venueId.value = venueOptions.value[0].id;
    await onVenueChange();
    if (floorOptions.value.length) {
      floorId.value = floorOptions.value[0].id;
      await loadSeats();
    }
  }
});
</script>

<style scoped>
.seatmap-toolbar {
  margin-bottom: 4px;
}
.seatmap-legend {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
  color: #606266;
  padding-top: 6px;
  border-top: 1px dashed #ebeef5;
  flex-wrap: wrap;
}
.seatmap-legend .lg {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.seatmap-legend .lg-tip {
  margin-left: auto;
  color: #909399;
}
.dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 4px;
}
.dot.free {
  background: #e1f3d8;
  border: 1px solid #67c23a;
}
.dot.occupied {
  background: #fde2e2;
  border: 1px solid #f56c6c;
}
.dot.selected {
  background: #d9ecff;
  border: 1px solid #409eff;
}
.dot.disabled {
  background: #f4f4f5;
  border: 1px solid #c0c4cc;
}
.floor-wrap {
  min-height: 560px;
}
.floor-empty {
  height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 15px;
  background: repeating-linear-gradient(45deg, #fafafa, #fafafa 12px, #f5f5f5 12px, #f5f5f5 24px);
  border-radius: 8px;
}
.floor-plan {
  position: relative;
  width: 100%;
  min-width: 980px;
  height: 600px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  background-color: #fbfcfe;
  background-image: linear-gradient(#eef2f7 1px, transparent 1px), linear-gradient(90deg, #eef2f7 1px, transparent 1px);
  background-size: 40px 40px;
  overflow: auto;
}
.seat {
  position: absolute;
  width: 50px;
  height: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  user-select: none;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
  border: 1.5px solid transparent;
}
.seat .seat-no {
  line-height: 1;
}
.seat .seat-power {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 12px;
}
.seat.free {
  background: #e1f3d8;
  color: #4e9a20;
  border-color: #a4da89;
  cursor: pointer;
}
.seat.free:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(103, 194, 58, 0.35);
}
.seat.occupied {
  background: #fde2e2;
  color: #d9534f;
  border-color: #f0a3a3;
  cursor: not-allowed;
}
.seat.selected {
  background: #409eff;
  color: #fff;
  border-color: #66b1ff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.3);
  cursor: pointer;
}
.seat.disabled {
  background: #f4f4f5;
  color: #b4b7bd;
  border-color: #dcdfe6;
  cursor: not-allowed;
}
</style>

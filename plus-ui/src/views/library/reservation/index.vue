<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="读者ID" prop="readerId">
              <el-input v-model="queryParams.readerId" placeholder="读者ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="座位ID" prop="seatId">
              <el-input v-model="queryParams.seatId" placeholder="座位ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:reservation:add']" type="primary" plain icon="Plus" @click="handleAdd">约座</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="reservationList">
        <template #empty>
          <el-empty :image-size="90" description="暂无数据">
            <el-button v-hasPermi="['library:reservation:add']" type="primary" icon="Plus" @click="handleAdd">去约座</el-button>
            <el-button icon="Refresh" @click="handleQuery">刷新</el-button>
          </el-empty>
        </template>
        <el-table-column label="预约单ID" align="center" prop="id" width="180" />
        <el-table-column label="读者" align="center" width="170" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.readerName || scope.row.readerId }}</template>
        </el-table-column>
        <el-table-column label="座位" align="center" width="130">
          <template #default="scope">{{ scope.row.seatNo || scope.row.seatId }}</template>
        </el-table-column>
        <el-table-column label="日期" align="center" prop="reserveDate" width="110" />
        <el-table-column label="时段" align="center" width="300">
          <template #default="scope">{{ scope.row.startTime }} ~ {{ scope.row.endTime }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button v-hasPermi="['library:reservation:manage']" link type="primary" @click="doAction(scope.row, 'checkIn')">签到</el-button>
              <el-button v-hasPermi="['library:reservation:manage']" link type="warning" @click="doAction(scope.row, 'cancel')">取消</el-button>
            </template>
            <template v-else-if="scope.row.status === 1">
              <el-button v-hasPermi="['library:reservation:manage']" link type="primary" @click="doAction(scope.row, 'away')">暂离</el-button>
              <el-button v-hasPermi="['library:reservation:manage']" link type="success" @click="doAction(scope.row, 'leave')">退座</el-button>
            </template>
            <template v-else-if="scope.row.status === 2">
              <el-button v-hasPermi="['library:reservation:manage']" link type="primary" @click="doAction(scope.row, 'back')">返回</el-button>
              <el-button v-hasPermi="['library:reservation:manage']" link type="success" @click="doAction(scope.row, 'leave')">退座</el-button>
            </template>
            <el-button
              v-if="[0, 1, 2].includes(scope.row.status)"
              v-hasPermi="['library:reservation:manage']"
              link
              type="danger"
              @click="doAction(scope.row, 'forceRelease')"
              >强制释放</el-button
            >
            <span v-if="![0, 1, 2].includes(scope.row.status)">—</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 约座对话框 -->
    <el-dialog v-model="dialog.visible" title="约座" width="500px" append-to-body>
      <el-form ref="reservationFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位" prop="seatId">
          <el-select v-model="form.seatId" placeholder="选择座位" filterable style="width: 100%">
            <el-option v-for="s in seatOptions" :key="s.id" :label="s.seatNo" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="reserveDate">
          <el-date-picker v-model="form.reserveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段开始" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段结束" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="结束时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Reservation" lang="ts">
import {
  listReservation,
  addReservation,
  checkInReservation,
  awayReservation,
  backReservation,
  leaveReservation,
  cancelReservation,
  forceReleaseReservation
} from '@/api/library/reservation';
import { ReservationVO, ReservationQuery, ReservationForm } from '@/api/library/reservation/types';
import { listReader } from '@/api/library/reader';
import { listSeat } from '@/api/library/seat';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const reservationList = ref<ReservationVO[]>([]);
const readerOptions = ref<any[]>([]);
const seatOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '待签到' },
  { value: 1, label: '使用中' },
  { value: 2, label: '暂离中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' },
  { value: 5, label: '已违约' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'success', 'info', 'success', 'info', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const reservationFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: ReservationForm = {
  id: undefined,
  readerId: undefined,
  seatId: undefined,
  reserveDate: undefined,
  startTime: undefined,
  endTime: undefined,
  source: 1
};
const data = reactive<PageData<ReservationForm, ReservationQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, seatId: undefined, status: undefined },
  rules: {
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    seatId: [{ required: true, message: '请选择座位', trigger: 'change' }],
    reserveDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
    startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
    endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询预约列表 */
const getList = async () => {
  loading.value = true;
  const res = await listReservation(queryParams.value);
  reservationList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const loadOptions = async () => {
  const [r, s] = await Promise.all([listReader({ pageNum: 1, pageSize: 999 } as any), listSeat({ pageNum: 1, pageSize: 999 } as any)]);
  readerOptions.value = r.rows;
  seatOptions.value = s.rows;
};

const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  reservationFormRef.value?.resetFields();
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleAdd = () => {
  form.value = { ...initFormData };
  dialog.visible = true;
};

const submitForm = () => {
  reservationFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addReservation(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('约座成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 状态机动作 */
const actionMap: Record<string, { fn: (id: any) => any; label: string }> = {
  checkIn: { fn: checkInReservation, label: '签到' },
  away: { fn: awayReservation, label: '暂离' },
  back: { fn: backReservation, label: '返回落座' },
  leave: { fn: leaveReservation, label: '退座' },
  cancel: { fn: cancelReservation, label: '取消预约' },
  forceRelease: { fn: (id: any) => forceReleaseReservation(id, '管理员干预'), label: '强制释放' }
};
const doAction = async (row: ReservationVO, action: string) => {
  const a = actionMap[action];
  await proxy?.$modal.confirm(`确认对预约单 ${row.id} 执行「${a.label}」？`);
  await a.fn(row.id);
  proxy?.$modal.msgSuccess(a.label + '成功');
  await getList();
};

onMounted(() => {
  getList();
  loadOptions();
});
</script>

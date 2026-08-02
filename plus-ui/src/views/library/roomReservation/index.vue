<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="研讨间" prop="roomId">
              <el-select v-model="queryParams.roomId" placeholder="全部" clearable filterable style="width: 180px">
                <el-option v-for="r in roomOptions" :key="r.id" :label="r.roomName" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="预约日期" prop="reserveDate">
              <el-date-picker v-model="queryParams.reserveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" clearable />
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
            <el-button v-hasPermi="['library:roomReservation:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="roomReservationList">
        <el-table-column label="研讨间" align="center" width="160">
          <template #default="scope">{{ roomNameMap.get(scope.row.roomId) || scope.row.roomId }}</template>
        </el-table-column>
        <el-table-column label="读者" align="center" width="160">
          <template #default="scope">{{ readerNameMap.get(scope.row.readerId) || scope.row.readerId }}</template>
        </el-table-column>
        <el-table-column label="预约日期" align="center" prop="reserveDate" width="120" />
        <el-table-column label="时段" align="center" width="320">
          <template #default="scope">{{ scope.row.startTime }} ~ {{ scope.row.endTime }}</template>
        </el-table-column>
        <el-table-column label="使用人数" align="center" prop="userCount" width="90" />
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" align="center" width="140">
          <template #default="scope">{{ scope.row.approveBy || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button v-hasPermi="['library:roomReservation:approve']" link type="success" @click="doApprove(scope.row)">通过</el-button>
              <el-button v-hasPermi="['library:roomReservation:approve']" link type="danger" @click="doReject(scope.row)">驳回</el-button>
            </template>
            <template v-else-if="scope.row.status === 1">
              <el-button v-hasPermi="['library:roomReservation:edit']" link type="primary" @click="doCheckIn(scope.row)">签到</el-button>
              <el-button v-hasPermi="['library:roomReservation:edit']" link type="warning" @click="doCancel(scope.row)">取消</el-button>
            </template>
            <template v-else-if="scope.row.status === 2">
              <el-button v-hasPermi="['library:roomReservation:edit']" link type="success" @click="doComplete(scope.row)">完成</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新增研讨间预约对话框 -->
    <el-dialog v-model="dialog.visible" title="新增研讨间预约" width="500px" append-to-body>
      <el-form ref="roomReservationFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="研讨间" prop="roomId">
          <el-select v-model="form.roomId" placeholder="选择研讨间" filterable style="width: 100%">
            <el-option v-for="r in roomOptions" :key="r.id" :label="r.roomName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
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
        <el-form-item label="使用人数" prop="userCount">
          <el-input-number v-model="form.userCount" :min="1" controls-position="right" />
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

<script setup name="RoomReservation" lang="ts">
import {
  listRoomReservation,
  addRoomReservation,
  approveRoomReservation,
  rejectRoomReservation,
  checkInRoomReservation,
  completeRoomReservation,
  cancelRoomReservation
} from '@/api/library/roomReservation';
import { RoomReservationVO, RoomReservationQuery, RoomReservationForm } from '@/api/library/roomReservation/types';
import { listReader } from '@/api/library/reader';
import { listRoom } from '@/api/library/room';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const roomReservationList = ref<RoomReservationVO[]>([]);
const readerOptions = ref<any[]>([]);
const roomOptions = ref<any[]>([]);
const readerNameMap = ref<Map<any, string>>(new Map());
const roomNameMap = ref<Map<any, string>>(new Map());
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '待使用' },
  { value: 2, label: '使用中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' },
  { value: 5, label: '已驳回' },
  { value: 6, label: '已违约' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label ?? s;
const statusTag = (s: number) => (['warning', 'primary', 'success', 'info', 'info', 'danger', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const roomReservationFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: RoomReservationForm = {
  id: undefined,
  readerId: undefined,
  roomId: undefined,
  reserveDate: undefined,
  startTime: undefined,
  endTime: undefined,
  userCount: 1
};
const data = reactive<PageData<RoomReservationForm, RoomReservationQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, roomId: undefined, status: undefined, reserveDate: undefined },
  rules: {
    roomId: [{ required: true, message: '请选择研讨间', trigger: 'change' }],
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    reserveDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
    startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
    endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询研讨间预约列表 */
const getList = async () => {
  loading.value = true;
  const res = await listRoomReservation(queryParams.value);
  roomReservationList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 拉全量读者/研讨间，建 id→名称 映射（避免后端联表） */
const loadOptions = async () => {
  const [readers, rooms] = await Promise.all([
    listReader({ pageNum: 1, pageSize: 999 } as any),
    listRoom({ pageNum: 1, pageSize: 999 } as any)
  ]);
  readerOptions.value = readers.rows;
  roomOptions.value = rooms.rows;
  const rMap = new Map<any, string>();
  readers.rows.forEach((r: any) => {
    const name = (r.realName || '') + (r.studentNo ? '（' + r.studentNo + '）' : '');
    rMap.set(r.userId, name || r.userId);
  });
  readerNameMap.value = rMap;
  const roMap = new Map<any, string>();
  rooms.rows.forEach((r: any) => roMap.set(r.id, r.roomName));
  roomNameMap.value = roMap;
};

const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  roomReservationFormRef.value?.resetFields();
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
  roomReservationFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addRoomReservation(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('新增成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 审批通过 */
const doApprove = async (row: RoomReservationVO) => {
  await proxy?.$modal.confirm(`确认通过该研讨间预约？`);
  await approveRoomReservation(row.id);
  proxy?.$modal.msgSuccess('已通过');
  await getList();
};

/** 审批驳回：先收原因 */
const doReject = async (row: RoomReservationVO) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '驳回原因不能为空'
  });
  await rejectRoomReservation(row.id, value);
  proxy?.$modal.msgSuccess('已驳回');
  await getList();
};

/** 签到 */
const doCheckIn = async (row: RoomReservationVO) => {
  await proxy?.$modal.confirm(`确认为该预约签到？`);
  await checkInRoomReservation(row.id);
  proxy?.$modal.msgSuccess('签到成功');
  await getList();
};

/** 完成 */
const doComplete = async (row: RoomReservationVO) => {
  await proxy?.$modal.confirm(`确认结束该研讨间使用（完成）？`);
  await completeRoomReservation(row.id);
  proxy?.$modal.msgSuccess('已完成');
  await getList();
};

/** 取消 */
const doCancel = async (row: RoomReservationVO) => {
  await proxy?.$modal.confirm(`确认取消该研讨间预约？`);
  await cancelRoomReservation(row.id);
  proxy?.$modal.msgSuccess('已取消');
  await getList();
};

onMounted(() => {
  getList();
  loadOptions();
});
</script>

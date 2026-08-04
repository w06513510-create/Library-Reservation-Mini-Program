<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="被监督预约ID" prop="reservationId">
              <el-input v-model="queryParams.reservationId" placeholder="预约单ID" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:supervise:add']" type="primary" plain icon="Plus" @click="handleAdd">发起监督</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:supervise:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:supervise:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="superviseList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="被监督预约ID" align="center" prop="reservationId" width="170" />
        <el-table-column label="座位" align="center" width="130" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.seatNo || scope.row.seatId }}</template>
        </el-table-column>
        <el-table-column label="举报人" align="center" width="170" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.reporterName || scope.row.reporterId }}</template>
        </el-table-column>
        <el-table-column label="举报时间" align="center" prop="reportTime" width="170" />
        <el-table-column label="落座截止" align="center" prop="deadline" width="170" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="解除时间" align="center" prop="resolveTime" width="170" />
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              v-hasPermi="['library:supervise:edit']"
              link
              type="success"
              @click="handleReseat(scope.row)"
              >标记已落座(解除)</el-button
            >
            <el-button v-hasPermi="['library:supervise:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 发起监督对话框 -->
    <el-dialog v-model="dialog.visible" title="发起占座监督" width="500px" append-to-body>
      <el-form ref="superviseFormRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="被监督预约ID" prop="reservationId">
          <el-input-number v-model="form.reservationId" :min="1" :controls="false" placeholder="输入使用中的预约单ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="举报读者" prop="reporterId">
          <el-select v-model="form.reporterId" placeholder="选择举报读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
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

<script setup name="Supervise" lang="ts">
import { listSupervise, getSupervise, delSupervise, reportSupervise, reseatSupervise } from '@/api/library/supervise';
import { SuperviseVO, SuperviseQuery, SuperviseForm } from '@/api/library/supervise/types';
import { listReader } from '@/api/library/reader';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const superviseList = ref<SuperviseVO[]>([]);
const readerOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const multiple = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '进行中' },
  { value: 1, label: '已解除' },
  { value: 2, label: '超时释放' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'success', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const superviseFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: SuperviseForm = {
  id: undefined,
  reservationId: undefined,
  reporterId: undefined
};
const data = reactive<PageData<SuperviseForm, SuperviseQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    reservationId: undefined,
    reporterId: undefined,
    status: undefined
  },
  rules: {
    reservationId: [{ required: true, message: '请输入被监督预约单ID', trigger: 'blur' }],
    reporterId: [{ required: true, message: '请选择举报读者', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询占座监督列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSupervise(queryParams.value);
  superviseList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 加载读者下拉选项（发起监督对话框用） */
const loadReaders = async () => {
  const res = await listReader({ pageNum: 1, pageSize: 999 } as any);
  readerOptions.value = res.rows;
};

/** 取消按钮 */
const cancel = () => {
  form.value = { ...initFormData };
  superviseFormRef.value?.resetFields();
  dialog.visible = false;
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

/** 多选框选中数据 */
const handleSelectionChange = (selection: SuperviseVO[]) => {
  ids.value = selection.map((item) => item.id);
  multiple.value = !selection.length;
};

/** 发起监督按钮操作 */
const handleAdd = () => {
  form.value = { ...initFormData };
  dialog.visible = true;
};

/** 提交按钮 */
const submitForm = () => {
  superviseFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await reportSupervise(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('发起监督成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 标记已落座（解除监督） */
const handleReseat = async (row: SuperviseVO) => {
  await proxy?.$modal.confirm(`确认原用户已按时落座，解除对预约单 ${row.reservationId} 的监督？`);
  await reseatSupervise(row.id);
  proxy?.$modal.msgSuccess('已解除');
  await getList();
};

/** 删除按钮操作 */
const handleDelete = async (row?: SuperviseVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除占座监督编号为"' + _ids + '"的数据项？');
  await delSupervise(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/supervise/export',
    {
      ...queryParams.value
    },
    `supervise_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  loadReaders();
});
</script>

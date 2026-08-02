<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="读者ID" prop="readerId">
              <el-input v-model="queryParams.readerId" placeholder="读者ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="违约类型" prop="violationType">
              <el-select v-model="queryParams.violationType" placeholder="全部" clearable style="width: 160px">
                <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
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
            <el-button v-hasPermi="['library:violation:add']" type="primary" plain icon="Plus" @click="handleAdd">登记违约</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:violation:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="violationList">
        <el-table-column label="违约ID" align="center" prop="id" width="180" />
        <el-table-column label="读者ID" align="center" prop="readerId" width="160" />
        <el-table-column label="违约类型" align="center" width="130">
          <template #default="scope"><el-tag>{{ typeText(scope.row.violationType) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="扣分" align="center" prop="deductScore" width="80" />
        <el-table-column label="发生时间" align="center" prop="occurTime" width="170" />
        <el-table-column label="来源" align="center" width="110">
          <template #default="scope">{{ sourceText(scope.row.source) }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="120">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 登记违约对话框 -->
    <el-dialog v-model="dialog.visible" title="登记违约" width="500px" append-to-body>
      <el-form ref="violationFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="违约类型" prop="violationType">
          <el-select v-model="form.violationType" placeholder="选择违约类型" style="width: 100%">
            <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="扣分" prop="deductScore">
          <el-input-number v-model="form.deductScore" :min="0" placeholder="留空按规则自动扣分" style="width: 100%" />
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

<script setup name="Violation" lang="ts">
import { listViolation, addViolation } from '@/api/library/violation';
import { ViolationVO, ViolationQuery, ViolationForm } from '@/api/library/violation/types';
import { listReader } from '@/api/library/reader';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const violationList = ref<ViolationVO[]>([]);
const readerOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const typeOptions = [
  { value: 1, label: '座位爽约' },
  { value: 2, label: '暂离超时' },
  { value: 3, label: '监督未落座' },
  { value: 4, label: '未签退' },
  { value: 5, label: '图书逾期' },
  { value: 6, label: '预约架超期' },
  { value: 7, label: '遗失损坏' }
];
const typeText = (t: number) => typeOptions.find((o) => o.value === t)?.label || t;

const statusOptions = [
  { value: 0, label: '有效' },
  { value: 1, label: '已申诉解除' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'info'][s] || 'info');

const sourceText = (s: number) => (s === 1 ? '管理员登记' : '系统判定');

const queryFormRef = ref<ElFormInstance>();
const violationFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: ViolationForm = { readerId: undefined, violationType: undefined, deductScore: undefined };
const data = reactive<PageData<ViolationForm, ViolationQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, violationType: undefined, status: undefined },
  rules: {
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    violationType: [{ required: true, message: '请选择违约类型', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询违约记录列表 */
const getList = async () => {
  loading.value = true;
  const res = await listViolation(queryParams.value);
  violationList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const loadOptions = async () => {
  const r = await listReader({ pageNum: 1, pageSize: 999 } as any);
  readerOptions.value = r.rows;
};

const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  violationFormRef.value?.resetFields();
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
  violationFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addViolation(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('登记成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/violation/export',
    {
      ...queryParams.value
    },
    `violation_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  loadOptions();
});
</script>

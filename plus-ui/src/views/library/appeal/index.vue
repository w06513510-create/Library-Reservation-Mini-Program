<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="读者ID" prop="readerId">
              <el-input v-model="queryParams.readerId" placeholder="读者ID" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:appeal:add']" type="primary" plain icon="Plus" @click="handleAdd">提交申诉</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="appealList">
        <el-table-column label="申诉ID" align="center" prop="id" width="180" />
        <el-table-column label="违约记录ID" align="center" prop="violationId" width="180" />
        <el-table-column label="读者ID" align="center" prop="readerId" width="160" />
        <el-table-column label="申诉理由" align="center" prop="reason" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="审批意见" align="center" prop="auditRemark" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button v-hasPermi="['library:appeal:audit']" link type="success" @click="doAudit(scope.row, true)">通过</el-button>
              <el-button v-hasPermi="['library:appeal:audit']" link type="danger" @click="doAudit(scope.row, false)">驳回</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 提交申诉对话框 -->
    <el-dialog v-model="dialog.visible" title="提交申诉" width="500px" append-to-body>
      <el-form ref="appealFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="违约记录" prop="violationId">
          <el-select v-model="form.violationId" placeholder="选择违约记录" filterable style="width: 100%">
            <el-option v-for="v in violationOptions" :key="v.id" :label="'#' + v.id + ' 类型' + v.violationType" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入申诉理由" />
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

<script setup name="Appeal" lang="ts">
import { listAppeal, addAppeal, auditAppeal } from '@/api/library/appeal';
import { AppealVO, AppealQuery, AppealForm } from '@/api/library/appeal/types';
import { listViolation } from '@/api/library/violation';
import { listReader } from '@/api/library/reader';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const appealList = ref<AppealVO[]>([]);
const violationOptions = ref<any[]>([]);
const readerOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '待审' },
  { value: 1, label: '通过' },
  { value: 2, label: '驳回' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'success', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const appealFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: AppealForm = { violationId: undefined, readerId: undefined, reason: undefined };
const data = reactive<PageData<AppealForm, AppealQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, status: undefined },
  rules: {
    violationId: [{ required: true, message: '请选择违约记录', trigger: 'change' }],
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    reason: [{ required: true, message: '请输入申诉理由', trigger: 'blur' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询违约申诉列表 */
const getList = async () => {
  loading.value = true;
  const res = await listAppeal(queryParams.value);
  appealList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const loadOptions = async () => {
  const [v, r] = await Promise.all([listViolation({ pageNum: 1, pageSize: 999 } as any), listReader({ pageNum: 1, pageSize: 999 } as any)]);
  violationOptions.value = v.rows;
  readerOptions.value = r.rows;
};

const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  appealFormRef.value?.resetFields();
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
  appealFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addAppeal(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('提交成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 审批：通过/驳回 */
const doAudit = async (row: AppealVO, pass: boolean) => {
  const label = pass ? '通过' : '驳回';
  const remark = pass ? '审批通过' : '驳回';
  await proxy?.$modal.confirm(`确认对申诉 ${row.id} 执行「${label}」？`);
  await auditAppeal(row.id, pass, remark);
  proxy?.$modal.msgSuccess(label + '成功');
  await getList();
};

onMounted(() => {
  getList();
  loadOptions();
});
</script>

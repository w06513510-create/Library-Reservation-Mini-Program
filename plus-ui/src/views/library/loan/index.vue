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
            <el-button v-hasPermi="['library:loan:manage']" type="primary" plain icon="Plus" @click="handleBorrow">借出办理</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="loanList">
        <el-table-column label="借阅单ID" align="center" prop="id" width="180" />
        <el-table-column label="读者ID" align="center" prop="readerId" width="160" />
        <el-table-column label="馆藏册ID" align="center" prop="itemId" width="160" />
        <el-table-column label="借出时间" align="center" prop="borrowTime" width="160" />
        <el-table-column label="应还日" align="center" prop="dueTime" width="160" />
        <el-table-column label="续借" align="center" prop="renewCount" width="60" />
        <el-table-column label="催还" align="center" width="70">
          <template #default="scope"><el-tag v-if="scope.row.recallFlag === 1" type="danger">已催</el-tag><span v-else>—</span></template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
          <template #default="scope">
            <template v-if="scope.row.status === 0 || scope.row.status === 2">
              <el-button v-hasPermi="['library:loan:manage']" link type="success" @click="doAction(scope.row, 'return')">归还</el-button>
              <el-button v-hasPermi="['library:loan:manage']" link type="primary" @click="doAction(scope.row, 'renew')">续借</el-button>
              <el-button v-hasPermi="['library:loan:manage']" link type="warning" @click="doAction(scope.row, 'recall')">催还</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" title="借出办理" width="500px" append-to-body>
      <el-form ref="loanFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="馆藏册" prop="itemId">
          <el-select v-model="form.itemId" placeholder="选择馆藏册(条码)" filterable style="width: 100%">
            <el-option v-for="i in itemOptions" :key="i.id" :label="i.barcode" :value="i.id" />
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

<script setup name="Loan" lang="ts">
import { listLoan, borrowLoan, returnLoan, renewLoan, recallLoan } from '@/api/library/loan';
import { LoanVO, LoanQuery, LoanForm } from '@/api/library/loan/types';
import { listReader } from '@/api/library/reader';
import { listBookItem } from '@/api/library/bookItem';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const loanList = ref<LoanVO[]>([]);
const readerOptions = ref<any[]>([]);
const itemOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '在借' },
  { value: 1, label: '已还' },
  { value: 2, label: '逾期' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['success', 'info', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const loanFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: LoanForm = { id: undefined, readerId: undefined, itemId: undefined };
const data = reactive<PageData<LoanForm, LoanQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, status: undefined },
  rules: {
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    itemId: [{ required: true, message: '请选择馆藏册', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listLoan(queryParams.value);
  loanList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
const loadOptions = async () => {
  const [r, i] = await Promise.all([listReader({ pageNum: 1, pageSize: 999 } as any), listBookItem({ pageNum: 1, pageSize: 999 } as any)]);
  readerOptions.value = r.rows;
  itemOptions.value = i.rows;
};
const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  loanFormRef.value?.resetFields();
};
const handleQuery = () => { queryParams.value.pageNum = 1; getList(); };
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleBorrow = () => { form.value = { ...initFormData }; dialog.visible = true; };
const submitForm = () => {
  loanFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await borrowLoan(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('借出成功');
      dialog.visible = false;
      await getList();
    }
  });
};
const actionMap: Record<string, { fn: (id: any) => any; label: string }> = {
  return: { fn: returnLoan, label: '归还' },
  renew: { fn: renewLoan, label: '续借' },
  recall: { fn: recallLoan, label: '催还' }
};
const doAction = async (row: LoanVO, action: string) => {
  const a = actionMap[action];
  await proxy?.$modal.confirm(`确认对借阅单 ${row.id} 执行「${a.label}」？`);
  await a.fn(row.id);
  proxy?.$modal.msgSuccess(a.label + '成功');
  await getList();
};
onMounted(() => { getList(); loadOptions(); });
</script>

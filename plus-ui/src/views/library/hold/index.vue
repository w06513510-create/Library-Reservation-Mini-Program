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
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
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
            <el-button v-hasPermi="['library:hold:add']" type="primary" plain icon="Plus" @click="handleAdd">图书预约</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="holdList">
        <el-table-column label="预约ID" align="center" prop="id" width="180" />
        <el-table-column label="读者" align="center" width="170" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.readerName || scope.row.readerId }}</template>
        </el-table-column>
        <el-table-column label="书名" align="center" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.bookName || scope.row.bookId }}</template>
        </el-table-column>
        <el-table-column label="队列位次" align="center" prop="queueNo" width="90" />
        <el-table-column label="到书时间" align="center" prop="readyTime" width="160" />
        <el-table-column label="保留期截止" align="center" prop="holdDeadline" width="160" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button v-if="scope.row.status === 1" v-hasPermi="['library:hold:manage']" link type="success" @click="doAction(scope.row, 'pickup')">取书</el-button>
            <el-button v-if="scope.row.status === 0 || scope.row.status === 1" v-hasPermi="['library:hold:manage']" link type="warning" @click="doAction(scope.row, 'cancel')">取消</el-button>
            <span v-if="![0, 1].includes(scope.row.status)">—</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" title="图书预约" width="500px" append-to-body>
      <el-form ref="holdFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="书目" prop="bookId">
          <el-select v-model="form.bookId" placeholder="选择书目(题名)" filterable style="width: 100%">
            <el-option v-for="b in bookOptions" :key="b.id" :label="b.title" :value="b.id" />
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

<script setup name="Hold" lang="ts">
import { listHold, addHold, pickupHold, cancelHold } from '@/api/library/hold';
import { HoldVO, HoldQuery, HoldForm } from '@/api/library/hold/types';
import { listReader } from '@/api/library/reader';
import { listBook } from '@/api/library/book';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const holdList = ref<HoldVO[]>([]);
const readerOptions = ref<any[]>([]);
const bookOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '排队中' },
  { value: 1, label: '到书保留' },
  { value: 2, label: '已取书' },
  { value: 3, label: '已取消' },
  { value: 4, label: '过期释放' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'success', 'info', 'info', 'danger'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const holdFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: HoldForm = { id: undefined, readerId: undefined, bookId: undefined };
const data = reactive<PageData<HoldForm, HoldQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, status: undefined },
  rules: {
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    bookId: [{ required: true, message: '请选择书目', trigger: 'change' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listHold(queryParams.value);
  holdList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};
const loadOptions = async () => {
  const [r, b] = await Promise.all([listReader({ pageNum: 1, pageSize: 999 } as any), listBook({ pageNum: 1, pageSize: 999 } as any)]);
  readerOptions.value = r.rows;
  bookOptions.value = b.rows;
};
const cancel = () => {
  dialog.visible = false;
  form.value = { ...initFormData };
  holdFormRef.value?.resetFields();
};
const handleQuery = () => { queryParams.value.pageNum = 1; getList(); };
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleAdd = () => { form.value = { ...initFormData }; dialog.visible = true; };
const submitForm = () => {
  holdFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addHold(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('预约成功');
      dialog.visible = false;
      await getList();
    }
  });
};
const actionMap: Record<string, { fn: (id: any) => any; label: string }> = {
  pickup: { fn: pickupHold, label: '取书' },
  cancel: { fn: cancelHold, label: '取消预约' }
};
const doAction = async (row: HoldVO, action: string) => {
  const a = actionMap[action];
  await proxy?.$modal.confirm(`确认对预约 ${row.id} 执行「${a.label}」？`);
  await a.fn(row.id);
  proxy?.$modal.msgSuccess(a.label + '成功');
  await getList();
};
onMounted(() => { getList(); loadOptions(); });
</script>

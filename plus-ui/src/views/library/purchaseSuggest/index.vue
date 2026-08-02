<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="书名" prop="title">
              <el-input v-model="queryParams.title" placeholder="请输入书名" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:purchaseSuggest:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:purchaseSuggest:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:purchaseSuggest:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="purchaseSuggestList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="书名" align="center" prop="title" show-overflow-tooltip />
        <el-table-column label="著者" align="center" prop="author" show-overflow-tooltip />
        <el-table-column label="ISBN" align="center" prop="isbn" width="140" />
        <el-table-column label="荐购读者" align="center" width="120">
          <template #default="scope">{{ readerName(scope.row.readerId) }}</template>
        </el-table-column>
        <el-table-column label="荐购理由" align="center" prop="reason" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理时间" align="center" prop="handleTime" width="160" />
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <template v-if="scope.row.status === 0">
              <el-button v-hasPermi="['library:purchaseSuggest:handle']" link type="primary" @click="handleAccept(scope.row)">受理</el-button>
              <el-button v-hasPermi="['library:purchaseSuggest:handle']" link type="danger" @click="handleReject(scope.row)">驳回</el-button>
            </template>
            <template v-else-if="scope.row.status === 1">
              <el-button v-hasPermi="['library:purchaseSuggest:handle']" link type="success" @click="handlePurchased(scope.row)">标记已采购</el-button>
            </template>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新增荐购对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="purchaseSuggestFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="荐购读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="著者" prop="author">
          <el-input v-model="form.author" placeholder="请输入著者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="荐购理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入荐购理由" />
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

<script setup name="PurchaseSuggest" lang="ts">
import {
  listPurchaseSuggest,
  delPurchaseSuggest,
  addPurchaseSuggest,
  updatePurchaseSuggest,
  acceptPurchaseSuggest,
  rejectPurchaseSuggest,
  purchasedPurchaseSuggest
} from '@/api/library/purchaseSuggest';
import { PurchaseSuggestVO, PurchaseSuggestQuery, PurchaseSuggestForm } from '@/api/library/purchaseSuggest/types';
import { listReader } from '@/api/library/reader';
import { ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const purchaseSuggestList = ref<PurchaseSuggestVO[]>([]);
const readerMap = ref<Record<string | number, string>>({});
const readerOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const multiple = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '待受理' },
  { value: 1, label: '已受理' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已采购' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['warning', 'primary', 'danger', 'success'][s] || 'info');
const readerName = (id: string | number) => readerMap.value[id] || id;

const queryFormRef = ref<ElFormInstance>();
const purchaseSuggestFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: PurchaseSuggestForm = {
  id: undefined,
  readerId: undefined,
  title: undefined,
  author: undefined,
  isbn: undefined,
  reason: undefined
};
const data = reactive<PageData<PurchaseSuggestForm, PurchaseSuggestQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    status: undefined
  },
  rules: {
    readerId: [{ required: true, message: '请选择荐购读者', trigger: 'change' }],
    title: [{ required: true, message: '书名不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询荐购列表 */
const getList = async () => {
  loading.value = true;
  const res = await listPurchaseSuggest(queryParams.value);
  purchaseSuggestList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 加载读者选项 + id→姓名映射 */
const loadReaders = async () => {
  const res = await listReader({ pageNum: 1, pageSize: 999 } as any);
  readerOptions.value = res.rows;
  const map: Record<string | number, string> = {};
  res.rows.forEach((r: any) => {
    map[r.userId] = r.realName;
  });
  readerMap.value = map;
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  purchaseSuggestFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: PurchaseSuggestVO[]) => {
  ids.value = selection.map((item) => item.id);
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加荐购';
};

/** 提交按钮 */
const submitForm = () => {
  purchaseSuggestFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updatePurchaseSuggest(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addPurchaseSuggest(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: PurchaseSuggestVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除荐购编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delPurchaseSuggest(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 受理转采购 0→1 */
const handleAccept = async (row: PurchaseSuggestVO) => {
  await proxy?.$modal.confirm(`确认受理荐购「${row.title}」并转采购？`);
  await acceptPurchaseSuggest(row.id);
  proxy?.$modal.msgSuccess('受理成功');
  await getList();
};

/** 驳回 0→2（收集原因） */
const handleReject = async (row: PurchaseSuggestVO) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回荐购', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '驳回原因不能为空'
  });
  await rejectPurchaseSuggest(row.id, value);
  proxy?.$modal.msgSuccess('驳回成功');
  await getList();
};

/** 标记已采购 1→3 */
const handlePurchased = async (row: PurchaseSuggestVO) => {
  await proxy?.$modal.confirm(`确认将荐购「${row.title}」标记为已采购？`);
  await purchasedPurchaseSuggest(row.id);
  proxy?.$modal.msgSuccess('操作成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/purchaseSuggest/export',
    {
      ...queryParams.value
    },
    `purchaseSuggest_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
  loadReaders();
});
</script>

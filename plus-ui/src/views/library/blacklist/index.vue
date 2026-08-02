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
            <el-button v-hasPermi="['library:blacklist:add']" type="primary" plain icon="Plus" @click="handleAdd">加入黑名单</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="blacklistList">
        <el-table-column label="黑名单ID" align="center" prop="id" width="180" />
        <el-table-column label="读者ID" align="center" prop="readerId" width="160" />
        <el-table-column label="原因" align="center" prop="reason" min-width="200" show-overflow-tooltip />
        <el-table-column label="生效时间" align="center" prop="startTime" width="170" />
        <el-table-column label="到期时间" align="center" prop="endTime" width="170" />
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" v-hasPermi="['library:blacklist:manage']" link type="success" @click="doRelease(scope.row)">解除</el-button>
            <span v-else>—</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 加入黑名单对话框 -->
    <el-dialog v-model="dialog.visible" title="加入黑名单" width="500px" append-to-body>
      <el-form ref="blacklistFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" placeholder="选择读者" filterable style="width: 100%">
            <el-option v-for="r in readerOptions" :key="r.userId" :label="(r.studentNo || '') + ' ' + (r.realName || '')" :value="r.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" placeholder="请输入加入原因" />
        </el-form-item>
        <el-form-item label="暂停天数" prop="days">
          <el-input-number v-model="form.days" :min="1" style="width: 100%" />
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

<script setup name="Blacklist" lang="ts">
import { listBlacklist, addBlacklist, releaseBlacklist } from '@/api/library/blacklist';
import { BlacklistVO, BlacklistQuery, BlacklistForm } from '@/api/library/blacklist/types';
import { listReader } from '@/api/library/reader';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const blacklistList = ref<BlacklistVO[]>([]);
const readerOptions = ref<any[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const statusOptions = [
  { value: 0, label: '生效中' },
  { value: 1, label: '已解除' }
];
const statusText = (s: number) => statusOptions.find((o) => o.value === s)?.label || s;
const statusTag = (s: number) => (['danger', 'info'][s] || 'info');

const queryFormRef = ref<ElFormInstance>();
const blacklistFormRef = ref<ElFormInstance>();
const dialog = reactive<DialogOption>({ visible: false, title: '' });

const initFormData: BlacklistForm = { readerId: undefined, reason: undefined, days: 7 };
const data = reactive<PageData<BlacklistForm, BlacklistQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, readerId: undefined, status: undefined },
  rules: {
    readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
    reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

/** 查询黑名单列表 */
const getList = async () => {
  loading.value = true;
  const res = await listBlacklist(queryParams.value);
  blacklistList.value = res.rows;
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
  blacklistFormRef.value?.resetFields();
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
  blacklistFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      await addBlacklist(form.value).finally(() => (buttonLoading.value = false));
      proxy?.$modal.msgSuccess('加入成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 解除黑名单 */
const doRelease = async (row: BlacklistVO) => {
  await proxy?.$modal.confirm(`确认解除读者 ${row.readerId} 的黑名单？`);
  await releaseBlacklist(row.id);
  proxy?.$modal.msgSuccess('解除成功');
  await getList();
};

onMounted(() => {
  getList();
  loadOptions();
});
</script>

<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="学号/卡号" prop="studentNo">
              <el-input v-model="queryParams.studentNo" placeholder="请输入学号/校园卡号" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="黑名单" prop="blacklistFlag">
              <el-select v-model="queryParams.blacklistFlag" placeholder="全部" clearable style="width: 120px">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
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
            <el-button v-hasPermi="['library:reader:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:reader:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:reader:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:reader:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="readerList" @selection-change="handleSelectionChange">
        <template #empty>
          <el-empty :image-size="90" description="暂无数据">
            <el-button v-hasPermi="['library:reader:add']" type="primary" icon="Plus" @click="handleAdd">去新增</el-button>
            <el-button icon="Refresh" @click="handleQuery">刷新</el-button>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学号/卡号" align="center" prop="studentNo" />
        <el-table-column label="姓名" align="center" prop="realName" />
        <el-table-column label="院系" align="center" prop="college" show-overflow-tooltip />
        <el-table-column label="信用分" align="center" prop="creditScore" width="90" />
        <el-table-column label="黑名单" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.blacklistFlag === 1 ? 'danger' : 'success'">{{ scope.row.blacklistFlag === 1 ? '黑名单' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:reader:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:reader:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改读者档案对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="readerFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="C端账号ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" :controls="false" placeholder="请输入 app_user 账号ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学号/卡号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号/校园卡号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="院系" prop="college">
          <el-input v-model="form.college" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="信用分" prop="creditScore">
          <el-input-number v-model="form.creditScore" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="守信次数" prop="performCount">
          <el-input-number v-model="form.performCount" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="是否黑名单" prop="blacklistFlag">
          <el-radio-group v-model="form.blacklistFlag">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="黑名单到期" prop="blacklistEndTime">
          <el-date-picker
            v-model="form.blacklistEndTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择黑名单暂停到期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" :value="0" />
            <el-option label="受限" :value="1" />
            <el-option label="停用" :value="2" />
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

<script setup name="Reader" lang="ts">
import { listReader, getReader, delReader, addReader, updateReader } from '@/api/library/reader';
import { ReaderVO, ReaderQuery, ReaderForm } from '@/api/library/reader/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const statusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '受限' },
  { value: 2, label: '停用' }
];
const statusLabel = (val: number) => statusOptions.find((item) => item.value === val)?.label || val;
const statusTagType = (val: number) => {
  const map: Record<number, string> = { 0: 'success', 1: 'warning', 2: 'info' };
  return map[val] || 'info';
};

const readerList = ref<ReaderVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const readerFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: ReaderForm = {
  id: undefined,
  userId: undefined,
  studentNo: undefined,
  realName: undefined,
  college: undefined,
  major: undefined,
  creditScore: 100,
  performCount: 0,
  blacklistFlag: 0,
  blacklistEndTime: undefined,
  status: 0
};
const data = reactive<PageData<ReaderForm, ReaderQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentNo: undefined,
    realName: undefined,
    blacklistFlag: undefined
  },
  rules: {
    userId: [{ required: true, message: 'C端账号ID不能为空', trigger: 'blur' }],
    studentNo: [{ required: true, message: '学号/校园卡号不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询读者档案列表 */
const getList = async () => {
  loading.value = true;
  const res = await listReader(queryParams.value);
  readerList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

/** 取消按钮 */
const cancel = () => {
  reset();
  dialog.visible = false;
};

/** 表单重置 */
const reset = () => {
  form.value = { ...initFormData };
  readerFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: ReaderVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加读者档案';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: ReaderVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getReader(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改读者档案';
};

/** 提交按钮 */
const submitForm = () => {
  readerFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateReader(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addReader(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: ReaderVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除读者档案编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delReader(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/reader/export',
    {
      ...queryParams.value
    },
    `reader_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

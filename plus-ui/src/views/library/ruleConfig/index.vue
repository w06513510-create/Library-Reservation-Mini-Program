<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="规则分组" prop="ruleGroup">
              <el-select v-model="queryParams.ruleGroup" placeholder="全部" clearable style="width: 160px">
                <el-option v-for="item in ruleGroupOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="规则键" prop="ruleKey">
              <el-input v-model="queryParams.ruleKey" placeholder="请输入规则键" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:ruleConfig:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:ruleConfig:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:ruleConfig:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:ruleConfig:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="ruleConfigList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="规则分组" align="center" width="120">
          <template #default="scope">
            <el-tag :type="ruleGroupTagType(scope.row.ruleGroup)">{{ ruleGroupLabel(scope.row.ruleGroup) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="规则键" align="center" prop="ruleKey" show-overflow-tooltip />
        <el-table-column label="规则值" align="center" prop="ruleValue" show-overflow-tooltip />
        <el-table-column label="说明" align="center" prop="remark" show-overflow-tooltip />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:ruleConfig:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:ruleConfig:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改规则配置对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="ruleConfigFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="规则分组" prop="ruleGroup">
          <el-select v-model="form.ruleGroup" placeholder="请选择规则分组" style="width: 100%">
            <el-option v-for="item in ruleGroupOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则键" prop="ruleKey">
          <el-input v-model="form.ruleKey" placeholder="如 checkin_window_min" />
        </el-form-item>
        <el-form-item label="规则值" prop="ruleValue">
          <el-input v-model="form.ruleValue" placeholder="请输入规则值" />
        </el-form-item>
        <el-form-item label="说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入说明" />
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

<script setup name="RuleConfig" lang="ts">
import { listRuleConfig, getRuleConfig, delRuleConfig, addRuleConfig, updateRuleConfig } from '@/api/library/ruleConfig';
import { RuleConfigVO, RuleConfigQuery, RuleConfigForm } from '@/api/library/ruleConfig/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const ruleGroupOptions = [
  { value: 'seat', label: '座位' },
  { value: 'book', label: '图书' },
  { value: 'credit', label: '信用' },
  { value: 'task', label: '定时任务' }
];
const ruleGroupLabel = (val: string) => ruleGroupOptions.find((item) => item.value === val)?.label || val;
const ruleGroupTagType = (val: string) => {
  const map: Record<string, string> = { seat: 'primary', book: 'success', credit: 'warning', task: 'info' };
  return map[val] || 'info';
};

const ruleConfigList = ref<RuleConfigVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const ruleConfigFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: RuleConfigForm = {
  id: undefined,
  ruleGroup: undefined,
  ruleKey: undefined,
  ruleValue: undefined,
  remark: undefined
};
const data = reactive<PageData<RuleConfigForm, RuleConfigQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    ruleGroup: undefined,
    ruleKey: undefined
  },
  rules: {
    ruleGroup: [{ required: true, message: '规则分组不能为空', trigger: 'change' }],
    ruleKey: [{ required: true, message: '规则键不能为空', trigger: 'blur' }],
    ruleValue: [{ required: true, message: '规则值不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询规则配置列表 */
const getList = async () => {
  loading.value = true;
  const res = await listRuleConfig(queryParams.value);
  ruleConfigList.value = res.rows;
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
  ruleConfigFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: RuleConfigVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加规则配置';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: RuleConfigVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getRuleConfig(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改规则配置';
};

/** 提交按钮 */
const submitForm = () => {
  ruleConfigFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateRuleConfig(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addRuleConfig(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: RuleConfigVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除规则配置编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delRuleConfig(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/ruleConfig/export',
    {
      ...queryParams.value
    },
    `ruleConfig_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

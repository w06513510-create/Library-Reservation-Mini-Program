<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="场馆名称" prop="venueName">
              <el-input v-model="queryParams.venueName" placeholder="请输入场馆名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="正常" :value="0" />
                <el-option label="停用" :value="1" />
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
            <el-button v-hasPermi="['library:venue:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:venue:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:venue:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:venue:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="venueList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="场馆名称" align="center" prop="venueName" />
        <el-table-column label="地址" align="center" prop="address" show-overflow-tooltip />
        <el-table-column label="开馆时间" align="center" prop="openTime" width="100" />
        <el-table-column label="闭馆时间" align="center" prop="closeTime" width="100" />
        <el-table-column label="排序" align="center" prop="sort" width="80" />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:venue:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:venue:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改场馆对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="venueFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="场馆名称" prop="venueName">
          <el-input v-model="form.venueName" placeholder="请输入场馆名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="开馆时间" prop="openTime">
          <el-input v-model="form.openTime" placeholder="如 08:00" />
        </el-form-item>
        <el-form-item label="闭馆时间" prop="closeTime">
          <el-input v-model="form.closeTime" placeholder="如 22:00" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
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

<script setup name="Venue" lang="ts">
import { listVenue, getVenue, delVenue, addVenue, updateVenue } from '@/api/library/venue';
import { VenueVO, VenueQuery, VenueForm } from '@/api/library/venue/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const venueList = ref<VenueVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const venueFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: VenueForm = {
  id: undefined,
  venueName: undefined,
  address: undefined,
  openTime: undefined,
  closeTime: undefined,
  sort: 0,
  status: 0
};
const data = reactive<PageData<VenueForm, VenueQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    venueName: undefined,
    status: undefined
  },
  rules: {
    venueName: [{ required: true, message: '场馆名称不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询场馆列表 */
const getList = async () => {
  loading.value = true;
  const res = await listVenue(queryParams.value);
  venueList.value = res.rows;
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
  venueFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: VenueVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加场馆';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: VenueVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getVenue(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改场馆';
};

/** 提交按钮 */
const submitForm = () => {
  venueFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateVenue(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addVenue(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: VenueVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除场馆编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delVenue(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/venue/export',
    {
      ...queryParams.value
    },
    `venue_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

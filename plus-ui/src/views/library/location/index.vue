<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="藏地名称" prop="locationName">
              <el-input v-model="queryParams.locationName" placeholder="请输入藏地名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="所在楼层" prop="floorId">
              <el-select v-model="queryParams.floorId" placeholder="全部" clearable style="width: 180px">
                <el-option v-for="item in floorOptions" :key="item.id" :label="item.floorName" :value="item.id" />
              </el-select>
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
            <el-button v-hasPermi="['library:location:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:location:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:location:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:location:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="locationList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="藏地名称" align="center" prop="locationName" show-overflow-tooltip />
        <el-table-column label="所在楼层" align="center" prop="floorId" width="120" />
        <el-table-column label="排序" align="center" prop="sort" width="80" />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:location:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:location:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改藏地对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="locationFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="藏地名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="请输入藏地名称" />
        </el-form-item>
        <el-form-item label="所在楼层" prop="floorId">
          <el-select v-model="form.floorId" placeholder="请选择所在楼层" clearable style="width: 100%">
            <el-option v-for="item in floorOptions" :key="item.id" :label="item.floorName" :value="item.id" />
          </el-select>
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

<script setup name="Location" lang="ts">
import { listLocation, getLocation, delLocation, addLocation, updateLocation } from '@/api/library/location';
import { LocationVO, LocationQuery, LocationForm } from '@/api/library/location/types';
import { listFloor } from '@/api/library/floor';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const locationList = ref<LocationVO[]>([]);
const floorOptions = ref<{ id: string | number; floorName: string }[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const locationFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: LocationForm = {
  id: undefined,
  locationName: undefined,
  floorId: undefined,
  sort: 0,
  status: 0
};
const data = reactive<PageData<LocationForm, LocationQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    locationName: undefined,
    floorId: undefined,
    status: undefined
  },
  rules: {
    locationName: [{ required: true, message: '藏地名称不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询楼层下拉选项 */
const getFloorOptions = async () => {
  const res = await listFloor({ pageNum: 1, pageSize: 999 });
  floorOptions.value = res.rows;
};

/** 查询藏地列表 */
const getList = async () => {
  loading.value = true;
  const res = await listLocation(queryParams.value);
  locationList.value = res.rows;
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
  locationFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: LocationVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加藏地';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: LocationVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getLocation(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改藏地';
};

/** 提交按钮 */
const submitForm = () => {
  locationFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateLocation(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addLocation(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: LocationVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除藏地编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delLocation(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/location/export',
    {
      ...queryParams.value
    },
    `location_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getFloorOptions();
  getList();
});
</script>

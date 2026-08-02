<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属场馆" prop="venueId">
              <el-select v-model="queryParams.venueId" placeholder="全部" clearable style="width: 180px">
                <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="楼层名称" prop="floorName">
              <el-input v-model="queryParams.floorName" placeholder="请输入楼层名称" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:floor:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:floor:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:floor:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:floor:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="floorList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="所属场馆" align="center" prop="venueId" width="120" />
        <el-table-column label="楼层名称" align="center" prop="floorName" />
        <el-table-column label="楼层号" align="center" prop="floorNo" width="90" />
        <el-table-column label="平面图URL" align="center" prop="floorPlanUrl" show-overflow-tooltip />
        <el-table-column label="排序" align="center" prop="sort" width="80" />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:floor:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:floor:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改楼层对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="floorFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属场馆" prop="venueId">
          <el-select v-model="form.venueId" placeholder="请选择所属场馆" style="width: 100%">
            <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层名称" prop="floorName">
          <el-input v-model="form.floorName" placeholder="请输入楼层名称" />
        </el-form-item>
        <el-form-item label="楼层号" prop="floorNo">
          <el-input-number v-model="form.floorNo" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="平面图URL" prop="floorPlanUrl">
          <el-input v-model="form.floorPlanUrl" placeholder="请输入平面图URL" />
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

<script setup name="Floor" lang="ts">
import { listFloor, getFloor, delFloor, addFloor, updateFloor } from '@/api/library/floor';
import { FloorVO, FloorQuery, FloorForm } from '@/api/library/floor/types';
import { listVenue } from '@/api/library/venue';
import { VenueVO } from '@/api/library/venue/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const floorList = ref<FloorVO[]>([]);
const venueOptions = ref<VenueVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const floorFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: FloorForm = {
  id: undefined,
  venueId: undefined,
  floorName: undefined,
  floorNo: undefined,
  floorPlanUrl: undefined,
  sort: 0,
  status: 0
};
const data = reactive<PageData<FloorForm, FloorQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    venueId: undefined,
    floorName: undefined,
    status: undefined
  },
  rules: {
    venueId: [{ required: true, message: '所属场馆不能为空', trigger: 'change' }],
    floorName: [{ required: true, message: '楼层名称不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询场馆下拉选项 */
const getVenueOptions = async () => {
  const res = await listVenue({ pageNum: 1, pageSize: 999 });
  venueOptions.value = res.rows;
};

/** 查询楼层列表 */
const getList = async () => {
  loading.value = true;
  const res = await listFloor(queryParams.value);
  floorList.value = res.rows;
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
  floorFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: FloorVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加楼层';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: FloorVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getFloor(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改楼层';
};

/** 提交按钮 */
const submitForm = () => {
  floorFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateFloor(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addFloor(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: FloorVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除楼层编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delFloor(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/floor/export',
    {
      ...queryParams.value
    },
    `floor_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getVenueOptions();
  getList();
});
</script>

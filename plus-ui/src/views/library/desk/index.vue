<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属区域" prop="areaId">
              <el-select v-model="queryParams.areaId" placeholder="全部" clearable style="width: 180px">
                <el-option v-for="item in areaOptions" :key="item.id" :label="item.areaName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="桌号" prop="deskNo">
              <el-input v-model="queryParams.deskNo" placeholder="请输入桌号" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:desk:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:desk:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:desk:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:desk:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="deskList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="桌号" align="center" prop="deskNo" width="120" />
        <el-table-column label="所属区域" align="center" width="140">
          <template #default="scope">{{ areaNameText(scope.row.areaId) }}</template>
        </el-table-column>
        <el-table-column label="容量" align="center" prop="capacity" width="80" />
        <el-table-column label="桌形" align="center" width="100">
          <template #default="scope">{{ shapeText(scope.row.shape) }}</template>
        </el-table-column>
        <el-table-column label="坐标(X,Y)" align="center" width="120">
          <template #default="scope">{{ scope.row.posX }}, {{ scope.row.posY }}</template>
        </el-table-column>
        <el-table-column label="宽高(W×H)" align="center" width="120">
          <template #default="scope">{{ scope.row.width }} × {{ scope.row.height }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:desk:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:desk:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改桌子对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="deskFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属区域" prop="areaId">
          <el-select v-model="form.areaId" placeholder="请选择所属区域" style="width: 100%">
            <el-option v-for="item in areaOptions" :key="item.id" :label="item.areaName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="桌号" prop="deskNo">
          <el-input v-model="form.deskNo" placeholder="请输入桌号" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-select v-model="form.capacity" placeholder="请选择容量" style="width: 100%">
            <el-option v-for="item in capacityOptions" :key="item" :label="item + ' 人'" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="桌形" prop="shape">
          <el-select v-model="form.shape" placeholder="请选择桌形" style="width: 100%">
            <el-option v-for="item in shapeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="X坐标" prop="posX">
          <el-input-number v-model="form.posX" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="Y坐标" prop="posY">
          <el-input-number v-model="form.posY" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="宽度" prop="width">
          <el-input-number v-model="form.width" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="高度" prop="height">
          <el-input-number v-model="form.height" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="旋转角度" prop="rotation">
          <el-input-number v-model="form.rotation" :min="0" :max="360" controls-position="right" />
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

<script setup name="Desk" lang="ts">
import { listDesk, getDesk, delDesk, addDesk, updateDesk } from '@/api/library/desk';
import { DeskVO, DeskQuery, DeskForm } from '@/api/library/desk/types';
import { listArea } from '@/api/library/area';
import { AreaVO } from '@/api/library/area/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const deskList = ref<DeskVO[]>([]);
const areaOptions = ref<AreaVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

/** 容量枚举：1/2/4/6 */
const capacityOptions = [1, 2, 4, 6];

/** 桌形枚举：0矩形 1圆 2吧台 */
const shapeOptions = [
  { label: '矩形', value: 0 },
  { label: '圆', value: 1 },
  { label: '吧台', value: 2 }
];
const shapeText = (shape: number) => shapeOptions.find((item) => item.value === shape)?.label ?? shape;
const areaNameText = (areaId: string | number) => areaOptions.value.find((item) => item.id === areaId)?.areaName ?? areaId;

const queryFormRef = ref<ElFormInstance>();
const deskFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: DeskForm = {
  id: undefined,
  areaId: undefined,
  deskNo: undefined,
  capacity: 1,
  shape: 0,
  posX: 0,
  posY: 0,
  width: 0,
  height: 0,
  rotation: 0,
  status: 0,
  sort: 0
};
const data = reactive<PageData<DeskForm, DeskQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    areaId: undefined,
    deskNo: undefined,
    status: undefined
  },
  rules: {
    areaId: [{ required: true, message: '所属区域不能为空', trigger: 'change' }],
    deskNo: [{ required: true, message: '桌号不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询区域下拉选项 */
const getAreaOptions = async () => {
  const res = await listArea({ pageNum: 1, pageSize: 999 });
  areaOptions.value = res.rows;
};

/** 查询桌子列表 */
const getList = async () => {
  loading.value = true;
  const res = await listDesk(queryParams.value);
  deskList.value = res.rows;
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
  deskFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: DeskVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加桌子';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: DeskVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getDesk(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改桌子';
};

/** 提交按钮 */
const submitForm = () => {
  deskFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateDesk(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addDesk(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: DeskVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除桌子编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delDesk(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/desk/export',
    {
      ...queryParams.value
    },
    `desk_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getAreaOptions();
  getList();
});
</script>

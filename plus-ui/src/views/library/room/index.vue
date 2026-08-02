<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属楼层" prop="floorId">
              <el-select v-model="queryParams.floorId" placeholder="全部" clearable style="width: 200px">
                <el-option v-for="item in floorOptions" :key="item.id" :label="item.floorName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="研讨间名称" prop="roomName">
              <el-input v-model="queryParams.roomName" placeholder="请输入研讨间名称" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:room:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:room:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:room:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:room:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="roomList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="所属楼层ID" align="center" prop="floorId" width="110" />
        <el-table-column label="研讨间名称" align="center" prop="roomName" />
        <el-table-column label="容纳人数" align="center" prop="capacity" width="90" />
        <el-table-column label="最少人数" align="center" prop="minUsers" width="90" />
        <el-table-column label="需审批" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.needApprove === 1 ? 'warning' : 'info'">{{ scope.row.needApprove === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="需签到" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.needCheckin === 1 ? 'warning' : 'info'">{{ scope.row.needCheckin === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="X坐标" align="center" prop="posX" width="80" />
        <el-table-column label="Y坐标" align="center" prop="posY" width="80" />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:room:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:room:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改研讨间对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="roomFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属楼层" prop="floorId">
          <el-select v-model="form.floorId" placeholder="请选择所属楼层" style="width: 100%">
            <el-option v-for="item in floorOptions" :key="item.id" :label="item.floorName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="研讨间名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入研讨间名称/编号" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="最少人数" prop="minUsers">
          <el-input-number v-model="form.minUsers" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="需审批" prop="needApprove">
          <el-radio-group v-model="form.needApprove">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="需签到" prop="needCheckin">
          <el-radio-group v-model="form.needCheckin">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="X坐标" prop="posX">
          <el-input-number v-model="form.posX" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="Y坐标" prop="posY">
          <el-input-number v-model="form.posY" :min="0" controls-position="right" />
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

<script setup name="Room" lang="ts">
import { listRoom, getRoom, delRoom, addRoom, updateRoom } from '@/api/library/room';
import { RoomVO, RoomQuery, RoomForm } from '@/api/library/room/types';
import { listFloor } from '@/api/library/floor';
import { FloorVO } from '@/api/library/floor/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const roomList = ref<RoomVO[]>([]);
const floorOptions = ref<FloorVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const roomFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: RoomForm = {
  id: undefined,
  floorId: undefined,
  roomName: undefined,
  capacity: 0,
  minUsers: 1,
  needApprove: 0,
  needCheckin: 1,
  posX: 0,
  posY: 0,
  status: 0
};
const data = reactive<PageData<RoomForm, RoomQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    floorId: undefined,
    roomName: undefined,
    status: undefined
  },
  rules: {
    floorId: [{ required: true, message: '所属楼层不能为空', trigger: 'change' }],
    roomName: [{ required: true, message: '研讨间名称不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 加载所属楼层下拉选项 */
const loadFloorOptions = async () => {
  const res = await listFloor({ pageNum: 1, pageSize: 999 });
  floorOptions.value = res.rows;
};

/** 查询研讨间列表 */
const getList = async () => {
  loading.value = true;
  const res = await listRoom(queryParams.value);
  roomList.value = res.rows;
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
  roomFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: RoomVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加研讨间';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: RoomVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getRoom(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改研讨间';
};

/** 提交按钮 */
const submitForm = () => {
  roomFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateRoom(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addRoom(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: RoomVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除研讨间编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delRoom(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/room/export',
    {
      ...queryParams.value
    },
    `room_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  loadFloorOptions();
  getList();
});
</script>

<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属区域" prop="areaId">
              <el-select v-model="queryParams.areaId" placeholder="全部" clearable style="width: 200px">
                <el-option v-for="item in areaOptions" :key="item.id" :label="item.areaName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="座位编号" prop="seatNo">
              <el-input v-model="queryParams.seatNo" placeholder="请输入座位编号" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:seat:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:seat:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:seat:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:seat:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="seatList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="所属区域ID" align="center" prop="areaId" width="110" />
        <el-table-column label="座位编号" align="center" prop="seatNo" />
        <el-table-column label="座位类型" align="center" width="90">
          <template #default="scope">
            <el-tag>{{ seatTypeText(scope.row.seatType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有无插座" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.hasPower === 1 ? 'success' : 'info'">{{ scope.row.hasPower === 1 ? '有' : '无' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="X坐标" align="center" prop="posX" width="80" />
        <el-table-column label="Y坐标" align="center" prop="posY" width="80" />
        <el-table-column label="二维码" align="center" prop="qrCode" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">{{ scope.row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:seat:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:seat:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改座位对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="seatFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属区域" prop="areaId">
          <el-select v-model="form.areaId" placeholder="请选择所属区域" style="width: 100%">
            <el-option v-for="item in areaOptions" :key="item.id" :label="item.areaName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位编号" prop="seatNo">
          <el-input v-model="form.seatNo" placeholder="请输入座位编号" />
        </el-form-item>
        <el-form-item label="座位类型" prop="seatType">
          <el-select v-model="form.seatType" placeholder="请选择座位类型" style="width: 100%">
            <el-option label="普通" :value="0" />
            <el-option label="靠窗" :value="1" />
            <el-option label="沙发" :value="2" />
            <el-option label="单间" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="有无插座" prop="hasPower">
          <el-radio-group v-model="form.hasPower">
            <el-radio :value="0">无</el-radio>
            <el-radio :value="1">有</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="X坐标" prop="posX">
          <el-input-number v-model="form.posX" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="Y坐标" prop="posY">
          <el-input-number v-model="form.posY" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="二维码" prop="qrCode">
          <el-input v-model="form.qrCode" placeholder="请输入桌面二维码标识" />
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

<script setup name="Seat" lang="ts">
import { listSeat, getSeat, delSeat, addSeat, updateSeat } from '@/api/library/seat';
import { SeatVO, SeatQuery, SeatForm } from '@/api/library/seat/types';
import { listArea } from '@/api/library/area';
import { AreaVO } from '@/api/library/area/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const seatList = ref<SeatVO[]>([]);
const areaOptions = ref<AreaVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const seatFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: SeatForm = {
  id: undefined,
  areaId: undefined,
  seatNo: undefined,
  seatType: 0,
  hasPower: 0,
  posX: 0,
  posY: 0,
  qrCode: undefined,
  status: 0
};
const data = reactive<PageData<SeatForm, SeatQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    areaId: undefined,
    seatNo: undefined,
    status: undefined
  },
  rules: {
    areaId: [{ required: true, message: '所属区域不能为空', trigger: 'change' }],
    seatNo: [{ required: true, message: '座位编号不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

const seatTypeText = (val: number) => {
  return ['普通', '靠窗', '沙发', '单间'][val] ?? val;
};

/** 加载所属区域下拉选项 */
const loadAreaOptions = async () => {
  const res = await listArea({ pageNum: 1, pageSize: 999 });
  areaOptions.value = res.rows;
};

/** 查询座位列表 */
const getList = async () => {
  loading.value = true;
  const res = await listSeat(queryParams.value);
  seatList.value = res.rows;
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
  seatFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: SeatVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加座位';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: SeatVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getSeat(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改座位';
};

/** 提交按钮 */
const submitForm = () => {
  seatFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateSeat(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addSeat(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: SeatVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除座位编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delSeat(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/seat/export',
    {
      ...queryParams.value
    },
    `seat_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  loadAreaOptions();
  getList();
});
</script>

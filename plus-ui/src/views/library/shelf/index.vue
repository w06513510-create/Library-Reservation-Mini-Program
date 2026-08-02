<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属藏地" prop="locationId">
              <el-select v-model="queryParams.locationId" placeholder="全部" clearable style="width: 200px">
                <el-option v-for="item in locationOptions" :key="item.id" :label="item.locationName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="架号" prop="shelfNo">
              <el-input v-model="queryParams.shelfNo" placeholder="请输入架号" clearable @keyup.enter="handleQuery" />
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
            <el-button v-hasPermi="['library:shelf:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:shelf:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:shelf:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:shelf:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="shelfList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="所属藏地" align="center" prop="locationId" width="120" />
        <el-table-column label="架号" align="center" prop="shelfNo" width="100" />
        <el-table-column label="索书号起" align="center" prop="callNoStart" show-overflow-tooltip />
        <el-table-column label="索书号止" align="center" prop="callNoEnd" show-overflow-tooltip />
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
              <el-button v-hasPermi="['library:shelf:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:shelf:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改书架对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="shelfFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属藏地" prop="locationId">
          <el-select v-model="form.locationId" placeholder="请选择所属藏地" clearable style="width: 100%">
            <el-option v-for="item in locationOptions" :key="item.id" :label="item.locationName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="架号" prop="shelfNo">
          <el-input v-model="form.shelfNo" placeholder="如 A12" />
        </el-form-item>
        <el-form-item label="索书号起" prop="callNoStart">
          <el-input v-model="form.callNoStart" placeholder="排架区间起" />
        </el-form-item>
        <el-form-item label="索书号止" prop="callNoEnd">
          <el-input v-model="form.callNoEnd" placeholder="排架区间止" />
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

<script setup name="Shelf" lang="ts">
import { listShelf, getShelf, delShelf, addShelf, updateShelf } from '@/api/library/shelf';
import { ShelfVO, ShelfQuery, ShelfForm } from '@/api/library/shelf/types';
import { listLocation } from '@/api/library/location';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const shelfList = ref<ShelfVO[]>([]);
const locationOptions = ref<{ id: string | number; locationName: string }[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const shelfFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: ShelfForm = {
  id: undefined,
  locationId: undefined,
  shelfNo: undefined,
  callNoStart: undefined,
  callNoEnd: undefined,
  posX: 0,
  posY: 0,
  status: 0
};
const data = reactive<PageData<ShelfForm, ShelfQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    locationId: undefined,
    shelfNo: undefined,
    status: undefined
  },
  rules: {
    locationId: [{ required: true, message: '所属藏地不能为空', trigger: 'change' }],
    shelfNo: [{ required: true, message: '架号不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询藏地下拉选项 */
const getLocationOptions = async () => {
  const res = await listLocation({ pageNum: 1, pageSize: 999 });
  locationOptions.value = res.rows;
};

/** 查询书架列表 */
const getList = async () => {
  loading.value = true;
  const res = await listShelf(queryParams.value);
  shelfList.value = res.rows;
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
  shelfFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: ShelfVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加书架';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: ShelfVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getShelf(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改书架';
};

/** 提交按钮 */
const submitForm = () => {
  shelfFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateShelf(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addShelf(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: ShelfVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除书架编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delShelf(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/shelf/export',
    {
      ...queryParams.value
    },
    `shelf_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getLocationOptions();
  getList();
});
</script>

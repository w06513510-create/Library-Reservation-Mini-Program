<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="所属书目" prop="bookId">
              <el-select v-model="queryParams.bookId" placeholder="请选择书目" clearable filterable style="width: 200px">
                <el-option v-for="item in bookOptions" :key="item.id" :label="item.title" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="条码" prop="barcode">
              <el-input v-model="queryParams.barcode" placeholder="请输入条码" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
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
            <el-button v-hasPermi="['library:bookItem:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:bookItem:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:bookItem:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:bookItem:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="bookItemList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="所属书目ID" align="center" prop="bookId" width="100" />
        <el-table-column label="条码" align="center" prop="barcode" width="140" />
        <el-table-column label="索书号" align="center" prop="callNo" width="120" />
        <el-table-column label="藏地ID" align="center" prop="locationId" width="90" />
        <el-table-column label="书架ID" align="center" prop="shelfId" width="90" />
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusMap[scope.row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注销类型" align="center" width="100">
          <template #default="scope">
            <span>{{ scope.row.withdrawType ? withdrawTypeMap[scope.row.withdrawType] : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注销时间" align="center" prop="withdrawTime" width="160" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:bookItem:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:bookItem:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改馆藏册对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <el-form ref="bookItemFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属书目" prop="bookId">
          <el-select v-model="form.bookId" placeholder="请选择书目" filterable style="width: 100%">
            <el-option v-for="item in bookOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="条码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="请输入条码" />
        </el-form-item>
        <el-form-item label="索书号" prop="callNo">
          <el-input v-model="form.callNo" placeholder="请输入索书号" />
        </el-form-item>
        <el-form-item label="藏地" prop="locationId">
          <el-select v-model="form.locationId" placeholder="请选择藏地" clearable filterable style="width: 100%">
            <el-option v-for="item in locationOptions" :key="item.id" :label="item.locationName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="书架" prop="shelfId">
          <el-select v-model="form.shelfId" placeholder="请选择书架" clearable filterable style="width: 100%">
            <el-option v-for="item in shelfOptions" :key="item.id" :label="item.shelfNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </el-form-item>
        <el-form-item label="注销类型" prop="withdrawType">
          <el-select v-model="form.withdrawType" placeholder="请选择注销类型（status=已注销时填）" clearable style="width: 100%">
            <el-option v-for="(label, val) in withdrawTypeMap" :key="val" :label="label" :value="Number(val)" />
          </el-select>
        </el-form-item>
        <el-form-item label="注销原因" prop="withdrawReason">
          <el-input v-model="form.withdrawReason" placeholder="请输入注销原因" />
        </el-form-item>
        <el-form-item label="注销时间" prop="withdrawTime">
          <el-date-picker
            v-model="form.withdrawTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择注销时间"
            style="width: 100%"
          />
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

<script setup name="BookItem" lang="ts">
import { listBookItem, getBookItem, delBookItem, addBookItem, updateBookItem } from '@/api/library/bookItem';
import { BookItemVO, BookItemQuery, BookItemForm } from '@/api/library/bookItem/types';
import { listBook } from '@/api/library/book';
import { listShelf } from '@/api/library/shelf';
import { listLocation } from '@/api/library/location';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const bookItemList = ref<BookItemVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

// 外键下拉选项
const bookOptions = ref<any[]>([]);
const shelfOptions = ref<any[]>([]);
const locationOptions = ref<any[]>([]);

// 状态枚举：0在编 1可借在架 2借出 3在预约架 4遗失 5损坏 6已注销
const statusMap: Record<number, string> = {
  0: '在编',
  1: '可借在架',
  2: '借出',
  3: '在预约架',
  4: '遗失',
  5: '损坏',
  6: '已注销'
};
// 注销类型：1剔旧 2报损 3遗失核销
const withdrawTypeMap: Record<number, string> = {
  1: '剔旧',
  2: '报损',
  3: '遗失核销'
};
const statusTagType = (status: number) => {
  if (status === 1) return 'success';
  if (status === 2 || status === 3) return 'warning';
  if (status === 4 || status === 5 || status === 6) return 'danger';
  return 'info';
};

const queryFormRef = ref<ElFormInstance>();
const bookItemFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: BookItemForm = {
  id: undefined,
  bookId: undefined,
  barcode: undefined,
  callNo: undefined,
  locationId: undefined,
  shelfId: undefined,
  status: 0,
  withdrawType: undefined,
  withdrawReason: undefined,
  withdrawTime: undefined
};
const data = reactive<PageData<BookItemForm, BookItemQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bookId: undefined,
    barcode: undefined,
    status: undefined
  },
  rules: {
    bookId: [{ required: true, message: '所属书目不能为空', trigger: 'change' }],
    barcode: [{ required: true, message: '条码不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 加载外键下拉：书目 / 书架 / 藏地 */
const loadOptions = async () => {
  const [bookRes, shelfRes, locationRes] = await Promise.all([
    listBook({ pageNum: 1, pageSize: 999 } as any),
    listShelf({ pageNum: 1, pageSize: 999 } as any),
    listLocation({ pageNum: 1, pageSize: 999 } as any)
  ]);
  bookOptions.value = bookRes.rows || [];
  shelfOptions.value = shelfRes.rows || [];
  locationOptions.value = locationRes.rows || [];
};

/** 查询馆藏册列表 */
const getList = async () => {
  loading.value = true;
  const res = await listBookItem(queryParams.value);
  bookItemList.value = res.rows;
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
  bookItemFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: BookItemVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加馆藏册';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: BookItemVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getBookItem(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改馆藏册';
};

/** 提交按钮 */
const submitForm = () => {
  bookItemFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateBookItem(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBookItem(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: BookItemVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除馆藏册编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delBookItem(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/bookItem/export',
    {
      ...queryParams.value
    },
    `bookItem_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  loadOptions();
  getList();
});
</script>

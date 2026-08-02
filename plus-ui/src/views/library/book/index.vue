<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="题名" prop="title">
              <el-input v-model="queryParams.title" placeholder="请输入题名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="著者" prop="author">
              <el-input v-model="queryParams.author" placeholder="请输入著者" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="queryParams.isbn" placeholder="请输入ISBN" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
                <el-option label="在编" :value="0" />
                <el-option label="已上架(可借)" :value="1" />
                <el-option label="已下架" :value="2" />
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
            <el-button v-hasPermi="['library:book:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:book:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:book:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['library:book:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="bookList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="封面" align="center" width="70">
          <template #default="scope">
            <el-image
              v-if="scope.row.coverUrl"
              :src="scope.row.coverUrl"
              :preview-src-list="[scope.row.coverUrl]"
              :preview-teleported="true"
              fit="cover"
              style="width: 42px; height: 58px; border-radius: 3px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.18)"
            />
            <el-icon v-else style="color: #c0c4cc"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="题名" align="center" prop="title" show-overflow-tooltip />
        <el-table-column label="著者" align="center" prop="author" show-overflow-tooltip />
        <el-table-column label="ISBN" align="center" prop="isbn" width="140" />
        <el-table-column label="出版社" align="center" prop="publisher" show-overflow-tooltip />
        <el-table-column label="索书号" align="center" prop="callNo" width="120" />
        <el-table-column label="定价" align="center" prop="price" width="90" />
        <el-table-column label="复本总数" align="center" prop="totalQty" width="90" />
        <el-table-column label="可借册数" align="center" prop="availQty" width="90" />
        <el-table-column label="状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['library:book:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['library:book:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 添加或修改书目对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <el-form ref="bookFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="题名" prop="title">
          <el-input v-model="form.title" placeholder="请输入题名" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="著者" prop="author">
          <el-input v-model="form.author" placeholder="请输入著者" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="出版日期" prop="publishDate">
          <el-input v-model="form.publishDate" placeholder="如 2024-01" />
        </el-form-item>
        <el-form-item label="分类号" prop="clcNo">
          <el-input v-model="form.clcNo" placeholder="请输入中图法分类号" />
        </el-form-item>
        <el-form-item label="索书号" prop="callNo">
          <el-input v-model="form.callNo" placeholder="请输入索书号" />
        </el-form-item>
        <el-form-item label="封面图URL" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="内容简介" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入内容简介" />
        </el-form-item>
        <el-form-item label="定价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="复本总数" prop="totalQty">
          <el-input-number v-model="form.totalQty" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="可借册数" prop="availQty">
          <el-input-number v-model="form.availQty" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="在编" :value="0" />
            <el-option label="已上架(可借)" :value="1" />
            <el-option label="已下架" :value="2" />
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

<script setup name="Book" lang="ts">
import { listBook, getBook, delBook, addBook, updateBook } from '@/api/library/book';
import { BookVO, BookQuery, BookForm } from '@/api/library/book/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const bookList = ref<BookVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const bookFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const statusText = (status: number) => {
  return status === 0 ? '在编' : status === 1 ? '已上架(可借)' : '已下架';
};
const statusTagType = (status: number) => {
  return status === 1 ? 'success' : status === 0 ? 'info' : 'warning';
};

const initFormData: BookForm = {
  id: undefined,
  isbn: undefined,
  title: undefined,
  author: undefined,
  publisher: undefined,
  publishDate: undefined,
  clcNo: undefined,
  callNo: undefined,
  coverUrl: undefined,
  summary: undefined,
  price: 0,
  totalQty: 0,
  availQty: 0,
  status: 0
};
const data = reactive<PageData<BookForm, BookQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    author: undefined,
    isbn: undefined,
    status: undefined
  },
  rules: {
    title: [{ required: true, message: '题名不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询书目列表 */
const getList = async () => {
  loading.value = true;
  const res = await listBook(queryParams.value);
  bookList.value = res.rows;
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
  bookFormRef.value?.resetFields();
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
const handleSelectionChange = (selection: BookVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '添加书目';
};

/** 修改按钮操作 */
const handleUpdate = async (row?: BookVO) => {
  reset();
  const _id = row?.id || ids.value[0];
  const res = await getBook(_id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改书目';
};

/** 提交按钮 */
const submitForm = () => {
  bookFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      buttonLoading.value = true;
      if (form.value.id) {
        await updateBook(form.value).finally(() => (buttonLoading.value = false));
      } else {
        await addBook(form.value).finally(() => (buttonLoading.value = false));
      }
      proxy?.$modal.msgSuccess('操作成功');
      dialog.visible = false;
      await getList();
    }
  });
};

/** 删除按钮操作 */
const handleDelete = async (row?: BookVO) => {
  const _ids = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除书目编号为"' + _ids + '"的数据项？').finally(() => (loading.value = false));
  await delBook(_ids);
  proxy?.$modal.msgSuccess('删除成功');
  await getList();
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy?.download(
    'library/book/export',
    {
      ...queryParams.value
    },
    `book_${new Date().getTime()}.xlsx`
  );
};

onMounted(() => {
  getList();
});
</script>

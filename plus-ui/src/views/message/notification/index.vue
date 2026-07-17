<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="接收用户" prop="receiverId">
              <el-input v-model="queryParams.receiverId" placeholder="接收用户ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务类型" prop="bizType">
              <el-input v-model="queryParams.bizType" placeholder="请输入业务类型" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="是否已读" prop="isRead">
              <el-select v-model="queryParams.isRead" placeholder="全部" clearable style="width: 120px">
                <el-option label="未读" :value="0" />
                <el-option label="已读" :value="1" />
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
            <el-button v-hasPermi="['message:notification:send']" type="primary" plain icon="Promotion" @click="handleSend">发送通知</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="notificationList">
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="接收用户" align="center" prop="receiverId" width="100" />
        <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
        <el-table-column label="内容" align="center" prop="content" :show-overflow-tooltip="true" />
        <el-table-column label="业务类型" align="center" prop="bizType" width="120" />
        <el-table-column label="业务ID" align="center" prop="bizId" width="90" />
        <el-table-column label="是否已读" align="center" prop="isRead" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isRead === 1 ? 'success' : 'info'">{{ scope.row.isRead === 1 ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 发送通知对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <el-form ref="notificationFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="接收用户" prop="receiverId">
          <el-input v-model="form.receiverId" placeholder="接收用户ID(app_user)" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-input v-model="form.bizType" placeholder="可空，如 order/comment/system" />
        </el-form-item>
        <el-form-item label="业务ID" prop="bizId">
          <el-input v-model="form.bizId" placeholder="可空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Notification" lang="ts">
import { listNotification, sendNotification } from '@/api/message/notification';
import { NotificationVO, NotificationQuery, NotificationSendForm } from '@/api/message/notification/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const notificationList = ref<NotificationVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const notificationFormRef = ref<ElFormInstance>();

const dialog = reactive<DialogOption>({
  visible: false,
  title: ''
});

const initFormData: NotificationSendForm = {
  receiverId: undefined,
  title: '',
  content: '',
  bizType: '',
  bizId: undefined
};
const data = reactive<PageData<NotificationSendForm, NotificationQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    receiverId: undefined,
    bizType: '',
    isRead: undefined,
    title: ''
  },
  rules: {
    receiverId: [{ required: true, message: '接收用户不能为空', trigger: 'blur' }],
    title: [{ required: true, message: '标题不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询通知列表 */
const getList = async () => {
  loading.value = true;
  const res = await listNotification(queryParams.value);
  notificationList.value = res.rows;
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
  notificationFormRef.value?.resetFields();
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
/** 发送按钮操作 */
const handleSend = () => {
  reset();
  dialog.visible = true;
  dialog.title = '发送通知';
};
/** 提交按钮 */
const submitForm = () => {
  notificationFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      await sendNotification(form.value);
      proxy?.$modal.msgSuccess('发送成功');
      dialog.visible = false;
      await getList();
    }
  });
};

onMounted(() => {
  getList();
});
</script>

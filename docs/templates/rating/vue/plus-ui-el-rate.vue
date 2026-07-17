<!--
  plus-ui 评价片段（el-rate）——提炼自跑腿 order/my/index.vue 的评价弹窗 + 跑腿/宿舍的只读展示。
  这是"贴进你已有列表/详情页"的片段，不是独立页面。三处用法都在下面。
-->
<template>
  <!-- ① 列表行里的评价入口：已完成且未评→按钮；已评→标签 -->
  <template v-if="row.status === 5 && !row.evaluated">
    <el-button link type="primary" @click="handleEvaluate(row)">评价</el-button>
  </template>
  <el-tag v-else-if="row.evaluated" type="info">已评价</el-tag>

  <!-- ② 只读展示（详情页/管理列表）：disabled 星级 -->
  <el-rate :model-value="row.score" disabled />

  <!-- ③ 评价提交弹窗 -->
  <el-dialog v-model="evalDialog.visible" title="评价" width="480px" append-to-body>
    <el-form ref="evalFormRef" :model="evalForm" :rules="evalRules" label-width="80px">
      <el-form-item label="单号">
        <span>{{ evalForm.bizNo }}</span>
      </el-form-item>
      <el-form-item label="评分" prop="score">
        <el-rate v-model="evalForm.score" :max="5" show-text :texts="rateTexts" />
      </el-form-item>
      <el-form-item label="评价内容" prop="content">
        <el-input v-model="evalForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="说说本次体验（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button :loading="buttonLoading" type="primary" @click="submitEvaluate">提交评价</el-button>
        <el-button @click="evalDialog.visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
// 放到你的业务 api：plus-ui/src/api/biz/rating/index.ts
// export const addRating = (data) => request({ url: '/biz/rating', method: 'post', data });
import { addRating } from '@/api/biz/rating';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const buttonLoading = ref(false);
const rateTexts = ['很差', '较差', '一般', '满意', '非常满意'];

const evalFormRef = ref();
const evalDialog = reactive({ visible: false });
const evalForm = reactive<{ bizType: string; bizId?: number | string; bizNo?: string; score: number; content?: string }>({
  bizType: 'order', // 换成你的 biz_type
  bizId: undefined,
  bizNo: undefined,
  score: 5, // 默认 5 星
  content: undefined
});
const evalRules = {
  score: [{ required: true, type: 'number', min: 1, message: '请选择评分', trigger: 'change' }]
};

const handleEvaluate = (row: any) => {
  evalForm.bizId = row.id;
  evalForm.bizNo = row.orderNo;
  evalForm.score = 5;
  evalForm.content = undefined;
  evalDialog.visible = true;
  nextTick(() => evalFormRef.value?.clearValidate());
};

const submitEvaluate = () => {
  evalFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    buttonLoading.value = true;
    try {
      // 只传 bizType/bizId/score/content；评价方向、被评价人由后端解析
      await addRating({ bizType: evalForm.bizType, bizId: evalForm.bizId!, score: evalForm.score, content: evalForm.content });
      proxy?.$modal.msgSuccess('评价成功，感谢您的反馈');
      evalDialog.visible = false;
      // await getList();  // 刷新你的列表
    } finally {
      buttonLoading.value = false;
    }
  });
};
</script>

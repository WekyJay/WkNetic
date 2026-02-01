<template>
  <el-dialog
    v-model="visible"
    title="举报"
    width="500px"
    @close="handleClose"
  >
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      <template #title>
        举报须知
      </template>
      请如实提供举报信息，我们会认真审核。恶意举报将会受到相应处罚。
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="form.reason" placeholder="请选择举报原因" style="width: 100%">
          <el-option label="垃圾广告" value="SPAM" />
          <el-option label="色情低俗" value="PORNOGRAPHY" />
          <el-option label="违法违规" value="ILLEGAL" />
          <el-option label="人身攻击" value="HARASSMENT" />
          <el-option label="侵权内容" value="COPYRIGHT" />
          <el-option label="虚假信息" value="MISINFORMATION" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="详细描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          placeholder="请详细描述举报内容，帮助我们更好地处理（选填）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitReport" :loading="loading">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createReport } from '@/api/report'

interface Props {
  modelValue: boolean
  targetType: 'POST' | 'COMMENT'
  targetId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  reason: '',
  description: ''
})

const rules: FormRules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 提交举报
const submitReport = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await createReport({
      targetType: props.targetType,
      targetId: props.targetId,
      reason: form.reason,
      description: form.description || undefined
    })

    ElMessage.success('举报已提交，我们会尽快处理')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '举报提交失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  form.reason = ''
  form.description = ''
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
// 样式可以根据需要添加
</style>

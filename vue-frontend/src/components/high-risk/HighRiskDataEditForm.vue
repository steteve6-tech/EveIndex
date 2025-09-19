<template>
  <div class="high-risk-data-edit-form">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
      @finish="handleSubmit"
    >
      <!-- 基本信息 -->
      <a-divider orientation="left">基本信息</a-divider>
      
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="数据类型" name="dataType">
            <a-select
              v-model:value="formData.dataType"
              placeholder="选择数据类型"
              disabled
            >
              <a-select-option value="510k">510K设备</a-select-option>
              <a-select-option value="ce">CE认证</a-select-option>
              <a-select-option value="recall">召回记录</a-select-option>
              <a-select-option value="event">事件报告</a-select-option>
              <a-select-option value="registration">注册记录</a-select-option>
              <a-select-option value="guidance">指导文档</a-select-option>
              <a-select-option value="customs">海关案例</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="风险等级" name="riskLevel">
            <a-select
              v-model:value="formData.riskLevel"
              placeholder="选择风险等级"
            >
              <a-select-option value="HIGH">
                <a-tag color="#ff4d4f">高风险</a-tag>
              </a-select-option>
              <a-select-option value="MEDIUM">
                <a-tag color="#faad14">中风险</a-tag>
              </a-select-option>
              <a-select-option value="LOW">
                <a-tag color="#52c41a">低风险</a-tag>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备名称" name="deviceName">
            <a-input
              v-model:value="formData.deviceName"
              placeholder="输入设备名称"
              :maxlength="200"
              show-count
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="公司/申请人" name="applicant">
            <a-input
              v-model:value="formData.applicant"
              placeholder="输入公司或申请人名称"
              :maxlength="150"
              show-count
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 详细信息 -->
      <a-divider orientation="left">详细信息</a-divider>
      
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备类别" name="deviceClass">
            <a-input
              v-model:value="formData.deviceClass"
              placeholder="输入设备类别"
              :maxlength="100"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="专业描述" name="medicalSpecialtyDescription">
            <a-input
              v-model:value="formData.medicalSpecialtyDescription"
              placeholder="输入专业描述"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="申请编号" name="applicationNumber">
            <a-input
              v-model:value="formData.applicationNumber"
              placeholder="输入申请编号"
              :maxlength="50"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="申请日期" name="applicationDate">
            <a-date-picker
              v-model:value="formData.applicationDate"
              placeholder="选择申请日期"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 关键词管理 -->
      <a-divider orientation="left">关键词管理</a-divider>
      
      <a-form-item label="关键词" name="keywords">
        <div class="keywords-section">
          <div class="keywords-input">
            <a-input
              v-model:value="newKeyword"
              placeholder="输入关键词，按回车添加"
              @press-enter="addKeyword"
              :maxlength="50"
            >
              <template #addonAfter>
                <a-button type="primary" size="small" @click="addKeyword">
                  添加
                </a-button>
              </template>
            </a-input>
          </div>
          
          <div class="keywords-list">
            <a-tag
              v-for="keyword in formData.keywords"
              :key="keyword"
              closable
              @close="removeKeyword(keyword)"
              color="blue"
            >
              {{ keyword }}
            </a-tag>
            <span v-if="formData.keywords.length === 0" class="no-keywords">
              暂无关键词
            </span>
          </div>
        </div>
      </a-form-item>

      <!-- 风险分析 -->
      <a-divider orientation="left">风险分析</a-divider>
      
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="风险因素" name="riskFactors">
            <a-select
              v-model:value="formData.riskFactors"
              mode="multiple"
              placeholder="选择风险因素"
              :max-tag-count="3"
            >
              <a-select-option value="high_device_class">高设备类别</a-select-option>
              <a-select-option value="recall_history">召回历史</a-select-option>
              <a-select-option value="adverse_events">不良事件</a-select-option>
              <a-select-option value="regulatory_issues">监管问题</a-select-option>
              <a-select-option value="quality_issues">质量问题</a-select-option>
              <a-select-option value="safety_concerns">安全隐患</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        
        <a-col :span="12">
          <a-form-item label="风险评分" name="riskScore">
            <a-input-number
              v-model:value="formData.riskScore"
              placeholder="输入风险评分"
              :min="0"
              :max="100"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="风险描述" name="riskDescription">
        <a-textarea
          v-model:value="formData.riskDescription"
          placeholder="详细描述风险情况"
          :rows="4"
          :maxlength="1000"
          show-count
        />
      </a-form-item>

      <!-- 备注信息 -->
      <a-divider orientation="left">备注信息</a-divider>
      
      <a-form-item label="备注" name="remarks">
        <a-textarea
          v-model:value="formData.remarks"
          placeholder="输入备注信息"
          :rows="3"
          :maxlength="500"
          show-count
        />
      </a-form-item>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <a-space>
          <a-button type="primary" html-type="submit" :loading="submitLoading">
            保存
          </a-button>
          <a-button @click="handleCancel">
            取消
          </a-button>
        </a-space>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'

// 定义组件属性
interface Props {
  data: any
}

// 定义组件事件
interface Emits {
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref()

// 新关键词输入
const newKeyword = ref('')

// 提交状态
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  dataType: '',
  riskLevel: 'HIGH',
  deviceName: '',
  applicant: '',
  deviceClass: '',
  medicalSpecialtyDescription: '',
  applicationNumber: '',
  applicationDate: null,
  keywords: [] as string[],
  riskFactors: [],
  riskScore: 0,
  riskDescription: '',
  remarks: ''
})

// 表单验证规则
const formRules = {
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  applicant: [
    { required: true, message: '请输入公司或申请人名称', trigger: 'blur' }
  ],
  riskLevel: [
    { required: true, message: '请选择风险等级', trigger: 'change' }
  ],
  keywords: [
    { type: 'array', min: 1, message: '请至少添加一个关键词', trigger: 'change' }
  ]
}

// 添加关键词
const addKeyword = () => {
  const keyword = newKeyword.value.trim()
  if (!keyword) {
    message.warning('请输入关键词')
    return
  }
  
  if (formData.keywords.includes(keyword)) {
    message.warning('关键词已存在')
    return
  }
  
  if (formData.keywords.length >= 20) {
    message.warning('最多只能添加20个关键词')
    return
  }
  
  formData.keywords.push(keyword)
  newKeyword.value = ''
  message.success('关键词添加成功')
}

// 移除关键词
const removeKeyword = (keyword: string) => {
  const index = formData.keywords.indexOf(keyword)
  if (index > -1) {
    formData.keywords.splice(index, 1)
    message.success('关键词移除成功')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    // 处理日期格式
    const submitData = {
      ...formData,
      applicationDate: formData.applicationDate ? formData.applicationDate.format('YYYY-MM-DD') : null,
      keywords: JSON.stringify(formData.keywords)
    }
    
    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}

// 初始化表单数据
const initFormData = () => {
  if (props.data) {
    // 复制数据到表单
    Object.assign(formData, {
      ...props.data,
      keywords: props.data.keywords ? JSON.parse(props.data.keywords) : [],
      applicationDate: props.data.applicationDate ? dayjs(props.data.applicationDate) : null
    })
  }
}

// 监听数据变化
watch(() => props.data, () => {
  initFormData()
}, { immediate: true })

// 组件挂载时初始化
onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.high-risk-data-edit-form {
  .keywords-section {
    .keywords-input {
      margin-bottom: 16px;
    }
    
    .keywords-list {
      min-height: 40px;
      padding: 8px;
      border: 1px solid #d9d9d9;
      border-radius: 6px;
      background-color: #fafafa;
      
      .ant-tag {
        margin: 4px;
      }
      
      .no-keywords {
        color: #999;
        font-style: italic;
      }
    }
  }
  
  .form-actions {
    text-align: center;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>

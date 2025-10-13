<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑参数预设' : '创建参数预设'"
    width="900px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <!-- 基础信息 -->
      <a-divider orientation="left">基础信息</a-divider>

      <a-form-item label="预设名称" name="taskName" :rules="[{ required: true, message: '请输入预设名称' }]">
        <a-input
          v-model:value="formData.taskName"
          placeholder="例如：爬取Abbott公司510K数据"
        />
      </a-form-item>

      <a-form-item label="选择爬虫" name="crawlerName" :rules="[{ required: true, message: '请选择爬虫' }]">
        <a-select
          v-model:value="formData.crawlerName"
          placeholder="请选择爬虫"
          @change="handleCrawlerChange"
          :disabled="isEdit"
        >
          <a-select-option
            v-for="crawler in crawlers"
            :key="crawler.crawlerName"
            :value="crawler.crawlerName"
          >
            {{ crawler.description || crawler.crawlerName }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="任务描述">
        <a-textarea
          v-model:value="formData.description"
          :rows="2"
          placeholder="描述此预设的用途"
        />
      </a-form-item>

      <!-- 动态参数配置 -->
      <div v-if="currentSchema">
        <a-divider orientation="left">爬虫参数配置</a-divider>

        <!-- 渲染爬虫特定参数 -->
        <a-form-item
          v-for="field in currentSchema.fields"
          :key="field.fieldName"
          :label="field.fieldLabel"
          :required="field.required"
        >
          <!-- 关键词列表类型 -->
          <a-select
            v-if="field.fieldType === 'KEYWORD_LIST'"
            v-model:value="formData.params[field.fieldName]"
            mode="tags"
            :placeholder="field.placeholder || `请输入${field.fieldLabel}`"
            style="width: 100%"
          >
          </a-select>

          <!-- 文本类型 -->
          <a-input
            v-else-if="field.fieldType === 'TEXT'"
            v-model:value="formData.params[field.fieldName]"
            :placeholder="field.placeholder"
          />

          <!-- 数字类型 -->
          <a-input-number
            v-else-if="field.fieldType === 'NUMBER'"
            v-model:value="formData.params[field.fieldName]"
            :placeholder="field.placeholder"
            style="width: 100%"
          />

          <!-- 日期类型 -->
          <a-date-picker
            v-else-if="field.fieldType === 'DATE'"
            v-model:value="formData.params[field.fieldName]"
            style="width: 100%"
          />

          <!-- 字段描述 -->
          <div v-if="field.description" style="color: #999; font-size: 12px; margin-top: 4px">
            {{ field.description }}
          </div>
        </a-form-item>

        <!-- 通用参数 -->
        <a-divider orientation="left">通用参数</a-divider>

        <a-form-item
          v-for="field in currentSchema.commonFields"
          :key="field.fieldName"
          :label="field.fieldLabel"
        >
          <a-input-number
            v-model:value="formData.params[field.fieldName]"
            :placeholder="field.placeholder"
            style="width: 100%"
          />
          <div v-if="field.description" style="color: #999; font-size: 12px; margin-top: 4px">
            {{ field.description }}
          </div>
        </a-form-item>
      </div>

      <!-- 定时配置 -->
      <a-divider orientation="left">定时配置</a-divider>

      <a-form-item label="Cron表达式" name="cronExpression" :rules="[{ required: true, message: '请输入Cron表达式' }]">
        <a-input
          v-model:value="formData.cronExpression"
          placeholder="0 0 2 * * ?"
        >
          <template #suffix>
            <cron-helper @select="formData.cronExpression = $event" />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item label="启用任务">
        <a-switch v-model:checked="formData.enabled" />
        <span style="margin-left: 8px; color: #666">
          {{ formData.enabled ? '启用后将按Cron表达式自动执行' : '禁用后不会自动执行' }}
        </span>
      </a-form-item>

      <a-form-item label="优先级">
        <a-slider
          v-model:value="formData.priority"
          :min="1"
          :max="10"
          :marks="{ 1: '低', 5: '中', 10: '高' }"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import { getCrawlers, getCrawlerSchema, createPreset, updatePreset, validateParams } from '@/api/crawler';
import CronHelper from '@/components/crawler/CronHelper.vue';

const props = defineProps<{
  open: boolean;
  editData?: any;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
  success: [];
}>();

const visible = ref(false);
const isEdit = ref(false);
const formRef = ref();

const crawlers = ref<any[]>([]);
const currentSchema = ref<any>(null);

const formData = reactive({
  taskName: '',
  crawlerName: '',
  countryCode: '',
  description: '',
  params: {} as any,
  cronExpression: '',
  enabled: true,
  priority: 5,
  timeoutMinutes: 30,
  retryCount: 3
});

// 监听打开状态
watch(() => props.open, (val) => {
  visible.value = val;
  if (val) {
    loadCrawlers();
    if (props.editData) {
      isEdit.value = true;
      loadEditData();
    } else {
      isEdit.value = false;
      resetForm();
    }
  }
});

watch(visible, (val) => {
  emit('update:open', val);
});

// 加载爬虫列表
const loadCrawlers = async () => {
  try {
    const res = await getCrawlers();
    if (res.data.success) {
      crawlers.value = res.data.data || [];
    }
  } catch (error) {
    console.error('加载爬虫列表失败:', error);
  }
};

// 加载编辑数据
const loadEditData = () => {
  if (!props.editData) return;
  
  Object.assign(formData, {
    taskName: props.editData.taskName,
    crawlerName: props.editData.crawlerName,
    countryCode: props.editData.countryCode,
    description: props.editData.description,
    cronExpression: props.editData.cronExpression,
    enabled: props.editData.enabled,
    priority: props.editData.priority || 5,
    timeoutMinutes: props.editData.timeoutMinutes || 30,
    retryCount: props.editData.retryCount || 3
  });
  
  // 解析参数
  try {
    formData.params = JSON.parse(props.editData.parameters || '{}');
  } catch (e) {
    formData.params = {};
  }
  
  // 加载Schema
  handleCrawlerChange(formData.crawlerName);
};

// 爬虫变化时加载Schema
const handleCrawlerChange = async (crawlerName: string) => {
  if (!crawlerName) return;
  
  try {
    const res = await getCrawlerSchema(crawlerName);
    if (res.data.success) {
      currentSchema.value = res.data.data;
      
      // 找到对应的爬虫信息，设置countryCode
      const crawler = crawlers.value.find(c => c.crawlerName === crawlerName);
      if (crawler) {
        formData.countryCode = crawler.countryCode;
      }
      
      // 初始化参数对象
      if (!isEdit.value) {
        formData.params = {};
      }
    }
  } catch (error) {
    console.error('加载爬虫Schema失败:', error);
    message.error('加载爬虫参数配置失败');
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    
    // 验证参数
    const parametersJson = JSON.stringify(formData.params);
    const validRes = await validateParams(formData.crawlerName, formData.params);
    if (!validRes.data.valid) {
      message.error('参数验证失败，请检查必填项');
      return;
    }
    
    // 构建请求数据
    const requestData = {
      taskName: formData.taskName,
      crawlerName: formData.crawlerName,
      countryCode: formData.countryCode,
      parameters: parametersJson,
      cronExpression: formData.cronExpression,
      description: formData.description,
      enabled: formData.enabled,
      priority: formData.priority,
      timeoutMinutes: formData.timeoutMinutes,
      retryCount: formData.retryCount,
      createdBy: 'USER',
      updatedBy: 'USER'
    };
    
    let res;
    if (isEdit.value && props.editData?.id) {
      res = await updatePreset(props.editData.id, requestData);
    } else {
      res = await createPreset(requestData);
    }
    
    if (res.data.success) {
      message.success(isEdit.value ? '预设更新成功' : '预设创建成功');
      emit('success');
      handleCancel();
    } else {
      message.error(res.data.message || '操作失败');
    }
  } catch (error: any) {
    console.error('提交失败:', error);
    if (error.errorFields) {
      message.error('请填写所有必填项');
    } else {
      message.error('操作失败：' + (error.message || '未知错误'));
    }
  }
};

// 取消
const handleCancel = () => {
  visible.value = false;
  resetForm();
};

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    taskName: '',
    crawlerName: '',
    countryCode: '',
    description: '',
    params: {},
    cronExpression: '',
    enabled: true,
    priority: 5,
    timeoutMinutes: 30,
    retryCount: 3
  });
  currentSchema.value = null;
};
</script>

<style scoped>
:deep(.ant-divider-horizontal.ant-divider-with-text-left) {
  margin: 16px 0;
}
</style>


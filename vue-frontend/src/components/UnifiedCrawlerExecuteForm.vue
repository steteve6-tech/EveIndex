<template>
  <div class="unified-crawler-execute-form">
    <a-form
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
      ref="formRef"
    >
      <a-alert
        v-if="crawler"
        :message="`执行爬虫: ${crawler.displayName}`"
        :description="`国家: ${crawler.countryCode} | 类型: ${crawler.crawlerType}`"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-form-item label="执行模式">
        <a-radio-group v-model:value="formData.mode">
          <a-radio-button value="test">测试执行</a-radio-button>
          <a-radio-button value="full">完整执行</a-radio-button>
        </a-radio-group>
        <div class="mode-description">
          <p v-if="formData.mode === 'test'">测试模式：限制爬取数量，用于快速验证</p>
          <p v-else>完整模式：执行完整爬取任务</p>
        </div>
      </a-form-item>

      <a-form-item label="最大记录数" v-if="formData.mode === 'test'">
        <a-input-number 
          v-model:value="formData.maxRecords" 
          :min="1" 
          :max="100"
          placeholder="测试模式下的最大记录数"
        />
        <div class="field-description">
          测试模式下限制爬取的最大记录数（1-100）
        </div>
      </a-form-item>

      <a-form-item label="关键词（可选）">
        <a-textarea
          v-model:value="formData.keywords"
          placeholder="输入关键词，每行一个（可选）"
          :rows="4"
        />
        <div class="field-description">
          可选：输入关键词进行过滤爬取，每行一个关键词
        </div>
      </a-form-item>

      <a-form-item label="立即执行">
        <a-switch v-model:checked="formData.immediate" />
        <span style="margin-left: 8px;">{{ formData.immediate ? '立即执行' : '仅创建任务' }}</span>
      </a-form-item>
    </a-form>

    <a-divider />

    <div class="form-actions">
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit">
          {{ formData.immediate ? '立即执行' : '创建任务' }}
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import type { FormInstance } from 'ant-design-vue';

// Props
interface Props {
  crawler: any;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  submit: [data: any];
  cancel: [];
}>();

// 表单引用
const formRef = ref<FormInstance>();

// 响应式数据
const formData = reactive({
  mode: 'test',
  maxRecords: 10,
  keywords: '',
  immediate: true
});

// 方法
const handleSubmit = () => {
  const keywords = formData.keywords
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0);

  const submitData = {
    crawlerName: props.crawler?.crawlerName,
    mode: formData.mode,
    maxRecords: formData.mode === 'test' ? formData.maxRecords : undefined,
    keywords: keywords.length > 0 ? keywords : undefined,
    immediate: formData.immediate
  };

  emit('submit', submitData);
};

const handleCancel = () => {
  emit('cancel');
};

// 监听crawler变化，重置表单
watch(() => props.crawler, () => {
  formData.mode = 'test';
  formData.maxRecords = 10;
  formData.keywords = '';
  formData.immediate = true;
});
</script>

<style scoped>
.unified-crawler-execute-form {
  padding: 16px 0;
}

.mode-description {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f6f8fa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.mode-description p {
  margin: 0;
}

.field-description {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}
</style>


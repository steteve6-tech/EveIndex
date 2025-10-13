<template>
  <div class="unified-batch-execute-form">
    <a-form
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
      ref="formRef"
    >
      <a-alert
        :message="`批量执行 ${crawlers.length} 个爬虫`"
        :description="getCrawlersSummary()"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-form-item label="选中的爬虫">
        <a-tag
          v-for="crawler in selectedCrawlers"
          :key="crawler"
          color="blue"
          style="margin: 4px"
        >
          {{ crawler }}
        </a-tag>
      </a-form-item>

      <a-form-item label="执行模式">
        <a-radio-group v-model:value="formData.mode">
          <a-radio-button value="test">测试执行</a-radio-button>
          <a-radio-button value="full">完整执行</a-radio-button>
        </a-radio-group>
        <div class="mode-description">
          <p v-if="formData.mode === 'test'">测试模式：每个爬虫限制爬取数量</p>
          <p v-else>完整模式：执行完整爬取任务</p>
        </div>
      </a-form-item>

      <a-form-item label="最大记录数" v-if="formData.mode === 'test'">
        <a-input-number 
          v-model:value="formData.maxRecords" 
          :min="1" 
          :max="100"
          placeholder="每个爬虫的最大记录数"
        />
        <div class="field-description">
          测试模式下，每个爬虫限制爬取的最大记录数（1-100）
        </div>
      </a-form-item>

      <a-form-item label="执行方式">
        <a-radio-group v-model:value="formData.executeType">
          <a-radio-button value="parallel">并行执行</a-radio-button>
          <a-radio-button value="sequential">顺序执行</a-radio-button>
        </a-radio-group>
        <div class="mode-description">
          <p v-if="formData.executeType === 'parallel'">并行执行：同时启动所有爬虫，速度快</p>
          <p v-else>顺序执行：依次执行爬虫，更稳定</p>
        </div>
      </a-form-item>

      <a-form-item label="失败策略">
        <a-radio-group v-model:value="formData.failureStrategy">
          <a-radio-button value="continue">继续执行</a-radio-button>
          <a-radio-button value="stop">停止执行</a-radio-button>
        </a-radio-group>
        <div class="mode-description">
          <p v-if="formData.failureStrategy === 'continue'">某个爬虫失败后，继续执行其他爬虫</p>
          <p v-else>某个爬虫失败后，停止后续执行</p>
        </div>
      </a-form-item>

      <a-form-item label="执行间隔(秒)" v-if="formData.executeType === 'sequential'">
        <a-input-number 
          v-model:value="formData.interval" 
          :min="0" 
          :max="60"
          placeholder="爬虫之间的执行间隔"
        />
        <div class="field-description">
          顺序执行时，每个爬虫之间的等待时间（秒）
        </div>
      </a-form-item>
    </a-form>

    <a-divider />

    <div class="form-actions">
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="submitting">
          开始批量执行
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import type { FormInstance } from 'ant-design-vue';

// Props
interface Props {
  crawlers: string[];
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  submit: [data: any];
  cancel: [];
}>();

// 表单引用
const formRef = ref<FormInstance>();
const submitting = ref(false);

// 响应式数据
const formData = reactive({
  mode: 'test',
  maxRecords: 10,
  executeType: 'parallel',
  failureStrategy: 'continue',
  interval: 3
});

// 计算属性
const selectedCrawlers = computed(() => {
  return props.crawlers;
});

// 方法
const getCrawlersSummary = () => {
  const count = props.crawlers.length;
  if (count === 0) return '未选择爬虫';
  
  const summary = props.crawlers.slice(0, 3).join(', ');
  return count > 3 ? `${summary} 等 ${count} 个爬虫` : summary;
};

const handleSubmit = () => {
  submitting.value = true;
  
  const submitData = {
    crawlers: props.crawlers,
    mode: formData.mode,
    maxRecords: formData.mode === 'test' ? formData.maxRecords : undefined,
    executeType: formData.executeType,
    failureStrategy: formData.failureStrategy,
    interval: formData.executeType === 'sequential' ? formData.interval : 0
  };

  emit('submit', submitData);
  
  // 延迟重置提交状态
  setTimeout(() => {
    submitting.value = false;
  }, 1000);
};

const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.unified-batch-execute-form {
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


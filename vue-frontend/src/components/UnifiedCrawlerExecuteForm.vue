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

    </a-form>

    <!-- 实际执行参数显示区域 -->
    <a-divider>实际执行参数</a-divider>
    
    <div class="execution-params">
      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="执行模式">
          <a-tag :color="formData.mode === 'full' ? 'green' : 'blue'">
            {{ formData.mode === 'full' ? '完整执行' : '测试执行' }}
          </a-tag>
        </a-descriptions-item>
        
        <a-descriptions-item label="最大记录数" v-if="formData.mode === 'test'">
          {{ formData.maxRecords }}
        </a-descriptions-item>
        
        <a-descriptions-item label="最大记录数" v-else>
          <a-tag color="green">所有数据</a-tag>
        </a-descriptions-item>
        
        <a-descriptions-item label="关键词来源">
          <div v-if="loadingPreset">
            <a-spin size="small" />
            <span style="margin-left: 8px; color: #666; font-size: 12px;">加载中...</span>
          </div>
          <div v-else-if="presetData && presetData.parameters">
            <a-tag color="orange">预设配置</a-tag>
            <div style="margin-top: 4px;">
              <a-tag 
                v-for="(keyword, index) in getPresetKeywords()" 
                :key="index" 
                size="small" 
                color="blue"
                style="margin: 2px;"
              >
                {{ keyword }}
              </a-tag>
              <span v-if="getPresetKeywords().length === 0" style="color: #999; font-size: 12px;">
                无关键词配置
              </span>
            </div>
          </div>
          <div v-else>
            <a-tag color="gray">无预设配置</a-tag>
            <span style="margin-left: 8px; color: #666; font-size: 12px;">
              将爬取所有数据
            </span>
          </div>
        </a-descriptions-item>
        
        <a-descriptions-item label="执行方式">
          <a-tag color="green">立即执行</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <a-divider />

    <div class="form-actions">
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit">
          立即执行
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue';
import type { FormInstance } from 'ant-design-vue';
import { getPreset as getPresetApi } from '@/api/crawler';

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
  mode: 'full',  // 默认为完整执行模式，爬取所有数据
  maxRecords: 10
});

// 预设配置数据
const presetData = ref<any>(null);
const loadingPreset = ref(false);

// 方法
const handleSubmit = () => {
  const submitData = {
    crawlerName: props.crawler?.crawlerName,
    mode: formData.mode,
    maxRecords: formData.mode === 'test' ? formData.maxRecords : -1, // 完整模式使用-1表示爬取所有数据
    immediate: true  // 总是立即执行
  };

  emit('submit', submitData);
};

const handleCancel = () => {
  emit('cancel');
};

// 加载预设配置
const loadPresetConfig = async () => {
  if (!props.crawler?.crawlerName) return;
  
  loadingPreset.value = true;
  try {
    // 获取该爬虫的预设配置
    const response = await getPresetApi(props.crawler.crawlerName);
    if (response.success && response.data) {
      presetData.value = response.data;
    }
  } catch (error) {
    console.warn('加载预设配置失败:', error);
  } finally {
    loadingPreset.value = false;
  }
};

// 获取预设关键词
const getPresetKeywords = () => {
  if (!presetData.value || !presetData.value.parameters) {
    return [];
  }
  
  try {
    const params = JSON.parse(presetData.value.parameters);
    const keywords = [];
    
    // 从fieldKeywords中提取关键词
    if (params.fieldKeywords) {
      if (Array.isArray(params.fieldKeywords)) {
        keywords.push(...params.fieldKeywords);
      } else if (typeof params.fieldKeywords === 'object') {
        // 如果是对象，提取所有值
        Object.values(params.fieldKeywords).forEach(value => {
          if (Array.isArray(value)) {
            keywords.push(...value);
          } else if (typeof value === 'string' && value.trim()) {
            keywords.push(value.trim());
          }
        });
      }
    }
    
    // 从keywords中提取关键词
    if (params.keywords && Array.isArray(params.keywords)) {
      keywords.push(...params.keywords);
    }
    
    // 去重并过滤空值
    return [...new Set(keywords)].filter(keyword => keyword && keyword.trim());
  } catch (error) {
    console.warn('解析预设关键词失败:', error);
    return [];
  }
};

// 监听crawler变化，重置表单
watch(() => props.crawler, () => {
  formData.mode = 'full';  // 默认为完整执行模式
  formData.maxRecords = 10;
  loadPresetConfig(); // 加载预设配置
});

// 组件挂载时加载预设配置
onMounted(() => {
  loadPresetConfig();
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

.execution-params {
  margin: 16px 0;
}

.execution-params .ant-descriptions-item-label {
  font-weight: 500;
  color: #333;
}

.execution-params .ant-descriptions-item-content {
  color: #666;
}
</style>


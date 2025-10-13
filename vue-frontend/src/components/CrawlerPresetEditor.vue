<template>
  <div class="crawler-preset-editor">
    <!-- 加载状态 -->
    <a-spin :spinning="loading">
      <!-- 爬虫信息头部 -->
      <a-descriptions bordered :column="2" size="small" class="crawler-info">
        <a-descriptions-item label="爬虫名称">
          {{ crawlerName }}
        </a-descriptions-item>
        <a-descriptions-item label="国家">
          <a-tag :color="getCountryColor(schema?.countryCode)">
            {{ schema?.countryCode }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="类型">
          {{ schema?.crawlerType }}
        </a-descriptions-item>
        <a-descriptions-item label="描述">
          {{ schema?.description }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 参数编辑表单 -->
      <a-form :model="presetParams" layout="vertical" class="preset-form">
        <!-- 关键词字段区域 -->
        <div v-if="schema?.fields && schema.fields.length > 0" class="field-keywords-section">
          <a-divider orientation="left">关键词参数</a-divider>
          
          <a-row :gutter="16">
            <a-col :span="24" v-for="field in schema.fields" :key="field.fieldName">
              <a-form-item :label="field.displayName">
                <template #extra>
                  <span class="field-desc">{{ field.description }}</span>
                </template>
                <a-select
                  v-model:value="presetParams.fieldKeywords[field.fieldName]"
                  mode="tags"
                  :placeholder="field.placeholder || '输入关键词后按回车添加'"
                  style="width: 100%"
                  :max-tag-count="5"
                  :max-tag-text-length="20"
                >
                </a-select>
                <div class="keyword-count">
                  已添加 {{ (presetParams.fieldKeywords[field.fieldName] || []).length }} 个关键词
                </div>
              </a-form-item>
            </a-col>
          </a-row>
        </div>

        <!-- 通用参数区域 -->
        <a-divider orientation="left">通用参数</a-divider>
        
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="最大记录数" extra="-1表示爬取所有数据">
              <a-input-number 
                v-model:value="presetParams.maxRecords" 
                :min="-1"
                :step="100"
                style="width: 100%"
                placeholder="100"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="8">
            <a-form-item label="批次大小" extra="每次请求的数据量">
              <a-input-number 
                v-model:value="presetParams.batchSize" 
                :min="10"
                :max="1000"
                :step="10"
                style="width: 100%"
                placeholder="50"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="8">
            <a-form-item label="最近天数" extra="爬取最近N天的数据（可选）">
              <a-input-number 
                v-model:value="presetParams.recentDays" 
                :min="1"
                :max="365"
                style="width: 100%"
                placeholder="30"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 日期范围 -->
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" extra="格式：yyyyMMdd">
              <a-input 
                v-model:value="presetParams.dateFrom" 
                placeholder="20240101"
                :maxlength="8"
              />
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="结束日期" extra="格式：yyyyMMdd">
              <a-input 
                v-model:value="presetParams.dateTo" 
                placeholder="20241231"
                :maxlength="8"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- JSON预览区域 -->
        <a-divider orientation="left">参数预览</a-divider>
        
        <a-form-item>
          <a-textarea 
            :value="previewJson" 
            :rows="10"
            readonly
            class="json-preview"
          />
        </a-form-item>
      </a-form>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <a-space>
          <a-button @click="handleValidate" :loading="validating">
            <template #icon>
              <CheckCircleOutlined />
            </template>
            验证参数
          </a-button>
          
          <a-button type="primary" @click="handleSave" :loading="saving">
            <template #icon>
              <SaveOutlined />
            </template>
            保存预设
          </a-button>
          
          <a-button @click="handleCancel">
            取消
          </a-button>
        </a-space>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import { CheckCircleOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { getCrawlerPreset, updateCrawlerPreset, validateCrawlerPreset } from '../api/unifiedCrawler';

// Props
const props = defineProps<{
  crawlerName: string;
  initialParams?: any;
}>();

// Emits
const emit = defineEmits<{
  (e: 'save', params: any): void;
  (e: 'cancel'): void;
}>();

// 响应式数据
const loading = ref(false);
const saving = ref(false);
const validating = ref(false);
const schema = ref<any>(null);
const preset = ref<any>(null);

// 预设参数
const presetParams = ref({
  fieldKeywords: {} as Record<string, string[]>,
  maxRecords: 100,
  batchSize: 50,
  recentDays: null as number | null,
  dateFrom: '',
  dateTo: ''
});

// 计算属性：JSON预览
const previewJson = computed(() => {
  return JSON.stringify(presetParams.value, null, 2);
});

// 获取国家颜色
const getCountryColor = (countryCode: string) => {
  const colors: Record<string, string> = {
    'US': 'blue',
    'EU': 'green',
    'KR': 'orange',
    'CN': 'red',
    'JP': 'purple'
  };
  return colors[countryCode] || 'default';
};

// 加载预设和Schema
const loadPresetAndSchema = async () => {
  loading.value = true;
  try {
    console.log('加载预设和Schema:', props.crawlerName);
    const response = await getCrawlerPreset(props.crawlerName);
    console.log('预设响应:', response);
    
    if (response.success) {
      preset.value = response.preset;
      schema.value = response.schema;
      
      // 解析预设参数
      if (preset.value?.parameters) {
        const params = JSON.parse(preset.value.parameters);
        presetParams.value = {
          fieldKeywords: params.fieldKeywords || {},
          maxRecords: params.maxRecords || 100,
          batchSize: params.batchSize || 50,
          recentDays: params.recentDays || null,
          dateFrom: params.dateFrom || '',
          dateTo: params.dateTo || ''
        };
        
        // 确保所有字段都有空数组初始化
        if (schema.value?.fields) {
          for (const field of schema.value.fields) {
            if (!presetParams.value.fieldKeywords[field.fieldName]) {
              presetParams.value.fieldKeywords[field.fieldName] = [];
            }
          }
        }
      }
      
      console.log('预设参数:', presetParams.value);
    } else {
      message.error('加载预设失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载预设失败:', error);
    message.error('加载预设失败: ' + (error as any)?.message || '未知错误');
  } finally {
    loading.value = false;
  }
};

// 验证参数
const handleValidate = async () => {
  validating.value = true;
  try {
    const response = await validateCrawlerPreset(props.crawlerName, presetParams.value);
    
    if (response.success && response.valid) {
      message.success('参数验证通过');
    } else {
      message.warning('参数验证失败: ' + response.message);
    }
  } catch (error) {
    message.error('验证失败: ' + (error as any)?.message || '未知错误');
  } finally {
    validating.value = false;
  }
};

// 保存预设
const handleSave = async () => {
  saving.value = true;
  try {
    console.log('保存预设参数:', presetParams.value);
    const response = await updateCrawlerPreset(props.crawlerName, presetParams.value);
    
    if (response.success) {
      message.success('预设保存成功');
      emit('save', presetParams.value);
    } else {
      message.error('保存失败: ' + response.message);
    }
  } catch (error) {
    console.error('保存预设失败:', error);
    message.error('保存失败: ' + (error as any)?.message || '未知错误');
  } finally {
    saving.value = false;
  }
};

// 取消
const handleCancel = () => {
  emit('cancel');
};

// 初始化
onMounted(() => {
  if (props.initialParams) {
    presetParams.value = { ...props.initialParams };
  } else {
    loadPresetAndSchema();
  }
});

// 监听爬虫名称变化
watch(() => props.crawlerName, () => {
  loadPresetAndSchema();
});
</script>

<style scoped>
.crawler-preset-editor {
  padding: 16px 0;
}

.crawler-info {
  margin-bottom: 24px;
}

.preset-form {
  margin-top: 16px;
}

.field-keywords-section {
  margin-bottom: 16px;
}

.field-desc {
  color: #8c8c8c;
  font-size: 12px;
}

.keyword-count {
  margin-top: 4px;
  font-size: 12px;
  color: #1890ff;
}

.json-preview {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  background-color: #f5f5f5;
}

.action-buttons {
  margin-top: 24px;
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>

<template>
  <div class="unified-task-form">
    <a-form
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
      ref="formRef"
    >
      <!-- 基本信息 -->
      <a-divider orientation="left">基本信息</a-divider>
      
      <a-form-item label="任务名称" name="taskName">
        <a-input v-model:value="formData.taskName" placeholder="请输入任务名称" />
      </a-form-item>
      
      <a-form-item label="爬虫选择" name="crawlerName">
        <a-select 
          v-model:value="formData.crawlerName" 
          placeholder="请选择爬虫"
          show-search
          @change="onCrawlerChange"
        >
          <a-select-option 
            v-for="crawler in crawlers" 
            :key="crawler.crawlerName" 
            :value="crawler.crawlerName"
          >
            {{ crawler.displayName }} ({{ crawler.countryCode }})
          </a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="任务类型" name="taskType">
        <a-select v-model:value="formData.taskType" placeholder="请选择任务类型">
          <a-select-option value="KEYWORD_BATCH">关键词批量</a-select-option>
          <a-select-option value="DATE_RANGE">日期范围</a-select-option>
          <a-select-option value="FULL">全量爬取</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="任务描述" name="description">
        <a-textarea v-model:value="formData.description" placeholder="请输入任务描述" :rows="3" />
      </a-form-item>

      <!-- 参数配置 -->
      <a-divider orientation="left">参数配置</a-divider>
      
      <a-form-item label="参数版本">
        <a-radio-group v-model:value="formData.paramsVersion" @change="onParamsVersionChange">
          <a-radio-button value="v1">V1 模式</a-radio-button>
          <a-radio-button value="v2">V2 模式</a-radio-button>
        </a-radio-group>
        <div class="version-description">
          <p v-if="formData.paramsVersion === 'v1'">V1模式：使用传统的关键词列表方式</p>
          <p v-else>V2模式：支持多字段参数化配置，更灵活强大</p>
        </div>
      </a-form-item>

      <!-- V1模式参数 -->
      <template v-if="formData.paramsVersion === 'v1'">
        <a-form-item label="关键词列表" name="keywords">
          <a-textarea
            v-model:value="formData.keywords"
            placeholder="请输入关键词，每行一个，支持批量粘贴"
            :rows="6"
          />
          <div class="field-description">
            每行输入一个关键词，支持批量粘贴。例如：<br>
            关键词1<br>
            关键词2<br>
            关键词3
          </div>
        </a-form-item>
      </template>

      <!-- V2模式参数 -->
      <template v-else>
        <div v-if="currentSchema">
          <a-alert 
            :message="currentSchema.description" 
            type="info" 
            show-icon 
            style="margin-bottom: 16px"
          />
          
          <a-form-item
            v-for="field in currentSchema.fields"
            :key="field.name"
            :label="field.label"
            :name="field.name"
          >
            <!-- 文本输入 -->
            <a-input
              v-if="field.type === 'TEXT'"
              v-model:value="formData.parameters[field.name]"
              :placeholder="field.placeholder || `请输入${field.label}`"
            />

            <!-- 数字输入 -->
            <a-input-number
              v-else-if="field.type === 'NUMBER'"
              v-model:value="formData.parameters[field.name]"
              :placeholder="field.placeholder || `请输入${field.label}`"
              style="width: 100%"
            />

            <!-- 日期输入 -->
            <a-date-picker
              v-else-if="field.type === 'DATE'"
              v-model:value="formData.parameters[field.name]"
              :placeholder="field.placeholder || `请选择${field.label}`"
              style="width: 100%"
            />

            <!-- 布尔值 -->
            <a-switch
              v-else-if="field.type === 'BOOLEAN'"
              v-model:checked="formData.parameters[field.name]"
            />

            <!-- 下拉选择 -->
            <a-select
              v-else-if="field.type === 'SELECT'"
              v-model:value="formData.parameters[field.name]"
              :placeholder="field.placeholder || `请选择${field.label}`"
            >
              <a-select-option
                v-for="opt in field.options"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-select-option>
            </a-select>

            <!-- 关键词列表 -->
            <div v-else-if="field.type === 'KEYWORD_LIST'">
              <a-textarea
                v-model:value="formData.parameters[field.name]"
                :placeholder="field.placeholder || `请输入${field.label}，每行一个关键词，支持批量粘贴`"
                :rows="6"
              />
              <div class="field-description">
                {{ field.description || '每行输入一个关键词，支持批量粘贴' }}
              </div>
            </div>

            <!-- 字段描述 -->
            <div
              v-if="field.description && field.type !== 'KEYWORD_LIST'"
              class="field-description"
            >
              {{ field.description }}
            </div>
          </a-form-item>
        </div>
        <div v-else-if="formData.crawlerName">
          <a-spin tip="加载爬虫Schema中...">
            <div style="height: 100px;"></div>
          </a-spin>
        </div>
      </template>

      <!-- 调度配置 -->
      <a-divider orientation="left">调度配置</a-divider>
      
      <a-form-item label="启用状态">
        <a-switch v-model:checked="formData.enabled" />
        <span style="margin-left: 8px;">{{ formData.enabled ? '启用' : '禁用' }}</span>
      </a-form-item>
      
      <a-form-item label="Cron表达式" name="cronExpression" v-if="formData.enabled">
        <a-input v-model:value="formData.cronExpression" placeholder="请输入Cron表达式">
          <template #addonAfter>
            <a-button @click="showCronHelper">帮助</a-button>
          </template>
        </a-input>
        <div class="field-description">
          定时执行表达式，例如：0 0 2 * * ? 表示每天凌晨2点执行
        </div>
      </a-form-item>
      
      <a-form-item label="优先级" name="priority">
        <a-slider 
          v-model:value="formData.priority" 
          :min="1" 
          :max="10" 
          :marks="{ 1: '最低', 5: '中等', 10: '最高' }"
        />
      </a-form-item>

      <!-- 高级配置 -->
      <a-divider orientation="left">高级配置</a-divider>
      
      <a-form-item label="超时时间(分钟)">
        <a-input-number v-model:value="formData.timeoutMinutes" :min="1" :max="1440" />
      </a-form-item>
      
      <a-form-item label="重试次数">
        <a-input-number v-model:value="formData.retryCount" :min="0" :max="10" />
      </a-form-item>

      <!-- 参数预览 -->
      <a-divider orientation="left">参数预览</a-divider>
      
      <a-form-item label="JSON预览">
        <a-textarea 
          :value="JSON.stringify(buildParameters(), null, 2)" 
          :rows="8" 
          readonly 
          style="font-family: monospace; font-size: 12px"
        />
      </a-form-item>
    </a-form>

    <!-- Cron表达式帮助对话框 -->
    <a-modal v-model:open="cronHelperVisible" title="Cron表达式帮助" width="600px">
      <div class="cron-helper">
        <h4>常用Cron表达式示例：</h4>
        <ul>
          <li><code>0 0 2 * * ?</code> - 每天凌晨2点执行</li>
          <li><code>0 0 */6 * * ?</code> - 每6小时执行一次</li>
          <li><code>0 0 0 1 * ?</code> - 每月1号凌晨执行</li>
          <li><code>0 0 0 ? * MON</code> - 每周一凌晨执行</li>
          <li><code>0 0 9-17 * * MON-FRI</code> - 工作日9-17点每小时执行</li>
        </ul>
        
        <h4>Cron表达式格式：</h4>
        <p>秒 分 时 日 月 周</p>
        <p>字段说明：</p>
        <ul>
          <li>秒：0-59</li>
          <li>分：0-59</li>
          <li>时：0-23</li>
          <li>日：1-31</li>
          <li>月：1-12 或 JAN-DEC</li>
          <li>周：1-7 或 SUN-SAT（1=周日）</li>
        </ul>
        
        <h4>特殊字符：</h4>
        <ul>
          <li>* - 匹配任意值</li>
          <li>? - 不指定值（仅用于日和周字段）</li>
          <li>- - 范围，如 1-5</li>
          <li>, - 列举，如 1,3,5</li>
          <li>/ - 间隔，如 */5</li>
        </ul>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';

// Props
interface Props {
  crawlers: any[];
  initialData?: any;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  submit: [data: any];
}>();

// 表单引用
const formRef = ref<FormInstance>();

// 响应式数据
const formData = reactive({
  taskName: '',
  crawlerName: '',
  countryCode: '',
  taskType: 'KEYWORD_BATCH',
  description: '',
  paramsVersion: 'v2',
  keywords: '', // V1模式
  parameters: {} as Record<string, any>, // V2模式
  cronExpression: '0 0 2 * * ?',
  enabled: true,
  priority: 5,
  timeoutMinutes: 30,
  retryCount: 3
});

const currentSchema = ref<any>(null);
const cronHelperVisible = ref(false);

// 表单验证规则
const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  crawlerName: [
    { required: true, message: '请选择爬虫', trigger: 'change' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' }
  ]
};

// 计算属性
const selectedCrawler = computed(() => {
  return props.crawlers.find(c => c.crawlerName === formData.crawlerName);
});

// 方法
const onCrawlerChange = async (crawlerName: string) => {
  console.log('选择爬虫:', crawlerName);
  
  if (formData.paramsVersion === 'v2') {
    await loadCrawlerSchema(crawlerName);
  }
  
  // 设置国家代码
  if (selectedCrawler.value) {
    formData.countryCode = selectedCrawler.value.countryCode;
  }
};

const onParamsVersionChange = async () => {
  console.log('参数版本切换:', formData.paramsVersion);
  
  if (formData.paramsVersion === 'v2' && formData.crawlerName) {
    await loadCrawlerSchema(formData.crawlerName);
  }
  
  // 清空参数
  formData.parameters = {};
};

const loadCrawlerSchema = async (crawlerName: string) => {
  try {
    const response = await fetch(`/api/unified/schemas/${crawlerName}`);
    const result = await response.json();
    
    if (result.success) {
      currentSchema.value = result.data;
      
      // 初始化参数默认值
      if (currentSchema.value && currentSchema.value.fields) {
        currentSchema.value.fields.forEach((field: any) => {
          if (field.defaultValue !== undefined) {
            formData.parameters[field.name] = field.defaultValue;
          } else if (field.type === 'KEYWORD_LIST') {
            formData.parameters[field.name] = '';
          }
        });
      }
    } else {
      message.error('加载爬虫Schema失败: ' + result.message);
    }
  } catch (error) {
    console.error('加载爬虫Schema失败:', error);
    message.error('加载爬虫Schema失败');
  }
};

const buildParameters = () => {
  if (formData.paramsVersion === 'v1') {
    // V1参数格式
    const keywords = formData.keywords
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0);
    
    return {
      keywords,
      taskType: formData.taskType,
      timeoutMinutes: formData.timeoutMinutes,
      retryCount: formData.retryCount
    };
  } else {
    // V2参数格式
    const fieldKeywords: Record<string, string[]> = {};
    const otherParams: Record<string, any> = {};
    
    if (currentSchema.value && currentSchema.value.fields) {
      currentSchema.value.fields.forEach((field: any) => {
        const value = formData.parameters[field.name];
        if (value === undefined || value === null) return;
        
        if (field.type === 'KEYWORD_LIST') {
          // 处理关键词列表
          if (typeof value === 'string') {
            fieldKeywords[field.name] = value
              .split('\n')
              .map(line => line.trim())
              .filter(line => line.length > 0);
          }
        } else {
          otherParams[field.name] = value;
        }
      });
    }
    
    return {
      fieldKeywords,
      ...otherParams,
      taskType: formData.taskType,
      timeoutMinutes: formData.timeoutMinutes,
      retryCount: formData.retryCount
    };
  }
};

const showCronHelper = () => {
  cronHelperVisible.value = true;
};

const validateAndSubmit = async () => {
  try {
    await formRef.value?.validate();
    
    const taskData = {
      taskName: formData.taskName,
      crawlerName: formData.crawlerName,
      countryCode: formData.countryCode,
      taskType: formData.taskType,
      description: formData.description,
      paramsVersion: formData.paramsVersion,
      parameters: JSON.stringify(buildParameters()),
      keywords: formData.paramsVersion === 'v1' ? JSON.stringify(
        formData.keywords.split('\n').map(line => line.trim()).filter(line => line.length > 0)
      ) : null,
      cronExpression: formData.enabled ? formData.cronExpression : null,
      enabled: formData.enabled,
      priority: formData.priority,
      timeoutMinutes: formData.timeoutMinutes,
      retryCount: formData.retryCount
    };
    
    emit('submit', taskData);
    return true;
  } catch (error) {
    console.error('表单验证失败:', error);
    return false;
  }
};

// 暴露方法给父组件
defineExpose({
  validateAndSubmit
});

// 监听props变化
watch(() => props.initialData, (newData) => {
  if (newData) {
    Object.assign(formData, newData);
    if (newData.crawlerName && newData.paramsVersion === 'v2') {
      loadCrawlerSchema(newData.crawlerName);
    }
  }
}, { immediate: true });
</script>

<style scoped>
.unified-task-form {
  max-height: 600px;
  overflow-y: auto;
}

.version-description {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f6f8fa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.field-description {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

.cron-helper {
  font-size: 14px;
}

.cron-helper h4 {
  margin: 16px 0 8px 0;
  font-size: 14px;
  font-weight: 600;
}

.cron-helper ul {
  margin: 8px 0;
  padding-left: 20px;
}

.cron-helper li {
  margin: 4px 0;
}

.cron-helper code {
  background: #f6f8fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.cron-helper p {
  margin: 8px 0;
  font-family: monospace;
  background: #f6f8fa;
  padding: 8px;
  border-radius: 4px;
}
</style>

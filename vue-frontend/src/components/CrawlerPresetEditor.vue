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
              <a-form-item>
                <template #label>
                  <div class="field-label-with-action">
                    <span>{{ field.fieldLabel || field.displayName || field.fieldName }}</span>
                    <a-button 
                      type="link" 
                      size="small" 
                      @click="showBatchInputModal(field.fieldName)"
                    >
                      <template #icon><PlusCircleOutlined /></template>
                      批量输入
                    </a-button>
                  </div>
                </template>
                <template #extra>
                  <span class="field-desc">{{ field.description }}</span>
                </template>
                <a-select
                  v-model:value="presetParams.fieldKeywords[field.fieldName]"
                  mode="tags"
                  :placeholder="field.placeholder || '输入关键词后按回车添加，或点击右上角批量输入'"
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

        <!-- 定时任务配置 -->
        <a-divider orientation="left">定时任务配置</a-divider>
        
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item>
              <template #label>
                <span>创建定时任务</span>
              </template>
              <template #extra>
                <span class="field-desc">启用后，保存预设时会自动创建定时任务</span>
              </template>
              <a-switch v-model:checked="taskConfig.createTask" />
              <span style="margin-left: 8px;">{{ taskConfig.createTask ? '启用' : '禁用' }}</span>
            </a-form-item>
          </a-col>
        </a-row>

        <template v-if="taskConfig.createTask">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="任务名称" :required="taskConfig.createTask">
                <a-input 
                  v-model:value="taskConfig.taskName" 
                  placeholder="输入任务名称，留空则自动生成"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="任务状态">
                <a-switch v-model:checked="taskConfig.enabled" />
                <span style="margin-left: 8px;">{{ taskConfig.enabled ? '启用' : '禁用' }}</span>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="执行时间" :required="taskConfig.createTask && taskConfig.enabled">
                <a-input 
                  v-model:value="taskConfig.cronExpression" 
                  placeholder="点击右侧按钮选择或手动输入"
                  readonly
                >
                  <template #addonAfter>
                    <a-button type="primary" size="small" @click="openCronBuilder">
                      设置时间
                    </a-button>
                  </template>
                </a-input>
                <div class="field-desc">
                  {{ formatCronDescription(taskConfig.cronExpression) }}
                </div>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="任务描述">
                <a-textarea 
                  v-model:value="taskConfig.description" 
                  :rows="2"
                  placeholder="选填：任务的描述信息"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </template>

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

    <!-- Cron可视化构建器对话框 -->
    <a-modal
      v-model:open="cronBuilderVisible"
      title="设置执行时间"
      width="700px"
      @ok="applyCronBuilder"
      @cancel="cancelCronBuilder"
    >
      <a-tabs v-model:activeKey="cronBuilderTab">
        <!-- 快捷预设 -->
        <a-tab-pane key="preset" tab="快捷预设" force-render>
          <div class="cron-preset-list">
            <div 
              v-for="preset in cronPresets" 
              :key="preset.value" 
              :class="['cron-preset-card', { 'selected': tempCronExpression === preset.value }]"
              @click="tempCronExpression = preset.value"
            >
              <div class="preset-header">
                <span class="preset-label">{{ preset.label }}</span>
                <a-tag :color="preset.color || 'blue'">{{ preset.category }}</a-tag>
              </div>
              <div class="preset-description">{{ preset.description }}</div>
              <div class="preset-cron"><code>{{ preset.value }}</code></div>
            </div>
          </div>
        </a-tab-pane>

        <!-- 自定义设置 -->
        <a-tab-pane key="custom" tab="自定义设置" force-render>
          <a-form layout="vertical">
            <a-form-item label="执行频率">
              <a-select v-model:value="cronBuilder.frequency" @change="buildCronExpression">
                <a-select-option value="daily">每天</a-select-option>
                <a-select-option value="weekly">每周</a-select-option>
                <a-select-option value="monthly">每月</a-select-option>
                <a-select-option value="hourly">每小时</a-select-option>
                <a-select-option value="interval">自定义间隔</a-select-option>
              </a-select>
            </a-form-item>

            <!-- 每天 -->
            <template v-if="cronBuilder.frequency === 'daily'">
              <a-form-item label="执行时间">
                <a-time-picker 
                  v-model:value="cronBuilder.time" 
                  format="HH:mm"
                  @change="buildCronExpression"
                  style="width: 100%"
                  :minute-step="15"
                />
              </a-form-item>
            </template>

            <!-- 每周 -->
            <template v-if="cronBuilder.frequency === 'weekly'">
              <a-form-item label="星期">
                <a-select 
                  v-model:value="cronBuilder.dayOfWeek" 
                  mode="multiple" 
                  @change="buildCronExpression"
                  placeholder="选择星期"
                >
                  <a-select-option value="MON">周一</a-select-option>
                  <a-select-option value="TUE">周二</a-select-option>
                  <a-select-option value="WED">周三</a-select-option>
                  <a-select-option value="THU">周四</a-select-option>
                  <a-select-option value="FRI">周五</a-select-option>
                  <a-select-option value="SAT">周六</a-select-option>
                  <a-select-option value="SUN">周日</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="执行时间">
                <a-time-picker 
                  v-model:value="cronBuilder.time" 
                  format="HH:mm"
                  @change="buildCronExpression"
                  style="width: 100%"
                  :minute-step="15"
                />
              </a-form-item>
            </template>

            <!-- 每月 -->
            <template v-if="cronBuilder.frequency === 'monthly'">
              <a-form-item label="日期">
                <a-select 
                  v-model:value="cronBuilder.dayOfMonth" 
                  @change="buildCronExpression"
                  placeholder="选择日期"
                >
                  <a-select-option v-for="day in 31" :key="day" :value="day">
                    每月{{ day }}号
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="执行时间">
                <a-time-picker 
                  v-model:value="cronBuilder.time" 
                  format="HH:mm"
                  @change="buildCronExpression"
                  style="width: 100%"
                  :minute-step="15"
                />
              </a-form-item>
            </template>

            <!-- 每小时 -->
            <template v-if="cronBuilder.frequency === 'hourly'">
              <a-form-item label="分钟">
                <a-select 
                  v-model:value="cronBuilder.minute" 
                  @change="buildCronExpression"
                  placeholder="选择分钟"
                >
                  <a-select-option value="0">整点（00分）</a-select-option>
                  <a-select-option value="15">15分</a-select-option>
                  <a-select-option value="30">30分</a-select-option>
                  <a-select-option value="45">45分</a-select-option>
                </a-select>
              </a-form-item>
            </template>

            <!-- 自定义间隔 -->
            <template v-if="cronBuilder.frequency === 'interval'">
              <a-form-item label="间隔类型">
                <a-select v-model:value="cronBuilder.intervalType" @change="buildCronExpression">
                  <a-select-option value="hours">小时</a-select-option>
                  <a-select-option value="days">天</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="间隔数">
                <a-input-number 
                  v-model:value="cronBuilder.intervalValue" 
                  :min="1" 
                  :max="cronBuilder.intervalType === 'hours' ? 23 : 31"
                  @change="buildCronExpression"
                  style="width: 100%"
                />
              </a-form-item>
            </template>

            <!-- 预览 -->
            <a-alert
              type="info"
              show-icon
              style="margin-top: 16px"
            >
              <template #message>
                <div><strong>执行计划:</strong> {{ getCronDescription(tempCronExpression) }}</div>
                <div style="margin-top: 8px"><strong>Cron表达式:</strong> <code>{{ tempCronExpression }}</code></div>
              </template>
            </a-alert>
          </a-form>
        </a-tab-pane>

        <!-- 高级编辑 -->
        <a-tab-pane key="advanced" tab="高级编辑" force-render>
          <a-form layout="vertical">
            <a-form-item label="Cron表达式">
              <a-input v-model:value="tempCronExpression" placeholder="例如: 0 0 2 * * ?" />
              <div class="form-help-text">
                <div>格式说明: 秒 分 时 日 月 周</div>
                <div>示例: <code>0 0 2 * * ?</code> = 每天凌晨2点</div>
              </div>
            </a-form-item>

            <!-- 实时预览 -->
            <a-alert
              type="success"
              show-icon
              style="margin-top: 16px"
            >
              <template #message>
                <div><strong>执行计划:</strong> {{ getCronDescription(tempCronExpression) }}</div>
              </template>
            </a-alert>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-modal>

    <!-- 批量输入关键词模态框 -->
    <a-modal
      v-model:open="batchInputModalVisible"
      title="批量输入关键词"
      width="700"
      @ok="handleBatchInputOk"
      @cancel="handleBatchInputCancel"
    >
      <div class="batch-input-container">
        <a-alert
          message="批量输入说明"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #description>
            <p style="margin: 0">支持多种分隔符：逗号(,)、中文逗号(，)、分号(;)、换行符</p>
            <p style="margin: 8px 0 0 0">
              <strong>示例：</strong>
              <code style="background: #f0f0f0; padding: 2px 6px; border-radius: 3px">
                Skin, Analyzer, 3D, AI, Facial, Detector, Scanner
              </code>
            </p>
          </template>
        </a-alert>
        
        <div class="input-section">
          <label class="input-label">粘贴关键词（支持批量）：</label>
          <a-textarea
            v-model:value="batchInputText"
            :rows="8"
            placeholder="请输入关键词，支持逗号、分号、换行分隔&#10;&#10;示例：&#10;Skin, Analyzer, 3D, AI, Facial, Detector, Scanner, Spectra, Skin Analysis, Skin Scanner, skin imaging, Facial Imaging, pigmentation, skin elasticity"
            style="font-family: 'Courier New', monospace; font-size: 13px"
          />
        </div>

        <a-divider style="margin: 16px 0">解析预览</a-divider>

        <div class="preview-section">
          <div class="preview-header">
            <span class="preview-title">
              <strong>将解析为 {{ parsedKeywords.length }} 个关键词</strong>
            </span>
            <a-button type="link" size="small" @click="clearBatchInput" danger>
              <template #icon><DeleteOutlined /></template>
              清空
            </a-button>
          </div>
          <div class="preview-tags">
            <a-tag
              v-for="(keyword, index) in parsedKeywords"
              :key="index"
              color="processing"
              closable
              @close="removeParsedKeyword(index)"
              style="margin: 4px"
            >
              {{ keyword }}
            </a-tag>
            <a-empty 
              v-if="parsedKeywords.length === 0" 
              :image="false" 
              description="暂无关键词，请在上方输入框中输入" 
              style="margin: 20px 0; padding: 20px" 
            />
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, reactive } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { CheckCircleOutlined, SaveOutlined, PlusCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { getCrawlerPreset, saveCrawlerPreset, validateCrawlerPreset, createPreset } from '@/api/crawler';

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

// 批量输入相关
const batchInputModalVisible = ref(false);
const batchInputText = ref('');
const batchInputFieldName = ref('');

// Cron可视化构建器
const cronBuilderVisible = ref(false);
const cronBuilderTab = ref('preset');
const tempCronExpression = ref('');

// Cron构建器数据
const cronBuilder = reactive({
  frequency: 'daily',
  time: dayjs().hour(2).minute(0) as Dayjs,
  dayOfWeek: [] as string[],
  dayOfMonth: 1,
  minute: '0',
  intervalType: 'hours',
  intervalValue: 6
});

// Cron预设（分类整理）
const cronPresets = [
  // 每日任务
  { label: '每天凌晨2点', value: '0 0 2 * * ?', description: '推荐：适合深夜爬取，服务器负载低', category: '每日', color: 'blue' },
  { label: '每天凌晨3点', value: '0 0 3 * * ?', description: '日常定时爬取', category: '每日', color: 'blue' },
  { label: '每天早上6点', value: '0 0 6 * * ?', description: '清晨爬取最新数据', category: '每日', color: 'blue' },
  { label: '每天上午9点', value: '0 0 9 * * ?', description: '工作时间开始爬取', category: '每日', color: 'blue' },
  { label: '每天中午12点', value: '0 0 12 * * ?', description: '中午定时爬取', category: '每日', color: 'blue' },
  { label: '每天下午6点', value: '0 0 18 * * ?', description: '下班前爬取', category: '每日', color: 'blue' },
  
  // 高频任务
  { label: '每2小时一次', value: '0 0 */2 * * ?', description: '高频爬取，适合重要数据', category: '高频', color: 'orange' },
  { label: '每4小时一次', value: '0 0 */4 * * ?', description: '较高频率爬取', category: '高频', color: 'orange' },
  { label: '每6小时一次', value: '0 0 */6 * * ?', description: '中高频率爬取', category: '高频', color: 'orange' },
  { label: '每12小时一次', value: '0 0 */12 * * ?', description: '每天爬取2次', category: '高频', color: 'orange' },
  
  // 每周任务
  { label: '每周一上午9点', value: '0 0 9 ? * MON', description: '每周固定时间爬取', category: '每周', color: 'green' },
  { label: '工作日上午9点', value: '0 0 9 ? * MON-FRI', description: '周一至周五爬取', category: '每周', color: 'green' },
  { label: '周末上午10点', value: '0 0 10 ? * SAT,SUN', description: '周六、周日爬取', category: '每周', color: 'green' },
  
  // 每月任务
  { label: '每月1号凌晨2点', value: '0 0 2 1 * ?', description: '每月初爬取', category: '每月', color: 'purple' },
  { label: '每月15号凌晨2点', value: '0 0 2 15 * ?', description: '每月中旬爬取', category: '每月', color: 'purple' }
];

// 定时任务配置
const taskConfig = ref({
  createTask: false,
  taskName: '',
  enabled: true,
  cronExpression: '0 0 2 * * ?',
  description: ''
});

// 计算属性：JSON预览
const previewJson = computed(() => {
  return JSON.stringify(presetParams.value, null, 2);
});

// 计算属性：解析的关键词
const parsedKeywords = computed(() => {
  return parseKeywordString(batchInputText.value);
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
    
    // 1. 保存预设参数
    const response = await saveCrawlerPreset(props.crawlerName, presetParams.value);

    if (!response.success) {
      message.error('保存失败: ' + response.message);
      return;
    }

    message.success('预设保存成功');

    // 2. 如果启用了创建定时任务，则创建任务
    if (taskConfig.value.createTask) {
      await handleCreateTask();
    } else {
      emit('save', presetParams.value);
    }
  } catch (error) {
    console.error('保存预设失败:', error);
    message.error('保存失败: ' + (error as any)?.message || '未知错误');
  } finally {
    saving.value = false;
  }
};

// 创建定时任务
const handleCreateTask = async () => {
  try {
    // 验证必填字段
    if (taskConfig.value.enabled && !taskConfig.value.cronExpression) {
      message.error('启用定时任务时，Cron表达式不能为空');
      return;
    }

    // 构建任务名称（如果未填写则自动生成）
    let taskName = taskConfig.value.taskName;
    if (!taskName) {
      const countryNames: Record<string, string> = {
        'US': '美国', 'EU': '欧盟', 'KR': '韩国', 
        'CN': '中国', 'JP': '日本', 'TW': '台湾'
      };
      const typeNames: Record<string, string> = {
        'EVENT': '不良事件', '510K': '510K申请', 'RECALL': '召回数据',
        'REGISTRATION': '注册数据', 'GUIDANCE': '指导文档', 'CUSTOMS': '海关案例'
      };
      const countryName = countryNames[schema.value?.countryCode] || schema.value?.countryCode;
      const typeName = typeNames[schema.value?.crawlerType] || schema.value?.crawlerType;
      taskName = `${countryName}${typeName}定时任务`;
    }

    // 构建任务数据
    const taskData = {
      taskName: taskName,
      crawlerName: props.crawlerName,
      countryCode: schema.value?.countryCode,
      crawlerType: schema.value?.crawlerType,
      description: taskConfig.value.description || `自动创建的${taskName}`,
      paramsVersion: 'v2',
      parameters: JSON.stringify(presetParams.value),
      cronExpression: taskConfig.value.enabled ? taskConfig.value.cronExpression : null,
      enabled: taskConfig.value.enabled,
      priority: 5,
      timeoutMinutes: 30,
      retryCount: 3
    };

    console.log('创建定时任务:', taskData);

    const response = await createPreset(taskData);

    if (response.success) {
      message.success('定时任务创建成功');
      emit('save', presetParams.value);
    } else {
      message.error('创建定时任务失败: ' + response.message);
    }
  } catch (error) {
    console.error('创建定时任务失败:', error);
    message.error('创建定时任务失败: ' + (error as any)?.message || '未知错误');
  }
};

// 打开Cron构建器
const openCronBuilder = () => {
  tempCronExpression.value = taskConfig.value.cronExpression || '0 0 2 * * ?';
  cronBuilderTab.value = 'preset';
  cronBuilderVisible.value = true;
};

// 应用Cron设置
const applyCronBuilder = () => {
  if (tempCronExpression.value) {
    taskConfig.value.cronExpression = tempCronExpression.value;
    cronBuilderVisible.value = false;
    message.success('已设置执行时间');
  } else {
    message.warning('请选择或设置执行时间');
  }
};

// 取消Cron设置
const cancelCronBuilder = () => {
  cronBuilderVisible.value = false;
  tempCronExpression.value = '';
  // 重置构建器
  cronBuilder.frequency = 'daily';
  cronBuilder.time = dayjs().hour(2).minute(0);
  cronBuilder.dayOfWeek = [];
  cronBuilder.dayOfMonth = 1;
  cronBuilder.minute = '0';
  cronBuilder.intervalType = 'hours';
  cronBuilder.intervalValue = 6;
};

// 根据构建器参数生成Cron表达式
const buildCronExpression = () => {
  let cron = '';
  
  switch (cronBuilder.frequency) {
    case 'daily':
      // 每天指定时间
      const hour = cronBuilder.time.hour();
      const minute = cronBuilder.time.minute();
      cron = `0 ${minute} ${hour} * * ?`;
      break;
      
    case 'weekly':
      // 每周指定星期
      if (cronBuilder.dayOfWeek.length > 0) {
        const hour = cronBuilder.time.hour();
        const minute = cronBuilder.time.minute();
        const days = cronBuilder.dayOfWeek.join(',');
        cron = `0 ${minute} ${hour} ? * ${days}`;
      } else {
        cron = '0 0 9 ? * MON';
      }
      break;
      
    case 'monthly':
      // 每月指定日期
      const monthHour = cronBuilder.time.hour();
      const monthMinute = cronBuilder.time.minute();
      cron = `0 ${monthMinute} ${monthHour} ${cronBuilder.dayOfMonth} * ?`;
      break;
      
    case 'hourly':
      // 每小时
      cron = `0 ${cronBuilder.minute} * * * ?`;
      break;
      
    case 'interval':
      // 自定义间隔
      if (cronBuilder.intervalType === 'hours') {
        cron = `0 0 */${cronBuilder.intervalValue} * * ?`;
      } else {
        cron = `0 0 2 */${cronBuilder.intervalValue} * ?`;
      }
      break;
  }
  
  tempCronExpression.value = cron;
};

// 格式化Cron表达式为友好描述
const formatCronDescription = (cronExpression: string): string => {
  return getCronDescription(cronExpression);
};

// 获取Cron描述（详细版本）
const getCronDescription = (cron: string): string => {
  if (!cron) return '';
  
  try {
    const parts = cron.split(' ');
    if (parts.length < 6) return cron;
    
    const [second, minute, hour, dayOfMonth, month, dayOfWeek] = parts;
    
    let description = '';
    
    // 解析周
    if (dayOfWeek && dayOfWeek !== '?' && dayOfWeek !== '*') {
      const dayMap: Record<string, string> = {
        'MON': '周一', 'TUE': '周二', 'WED': '周三', 'THU': '周四',
        'FRI': '周五', 'SAT': '周六', 'SUN': '周日'
      };
      
      if (dayOfWeek.includes('-')) {
        description = `每周`;
      } else if (dayOfWeek.includes(',')) {
        const days = dayOfWeek.split(',').map(d => dayMap[d] || d).join('、');
        description = days;
      } else {
        description = dayMap[dayOfWeek] || dayOfWeek;
      }
    }
    // 解析月
    else if (dayOfMonth && dayOfMonth !== '?' && dayOfMonth !== '*') {
      if (dayOfMonth.includes('/')) {
        const interval = dayOfMonth.split('/')[1];
        description = `每${interval}天`;
      } else {
        description = `每月${dayOfMonth}号`;
      }
    }
    // 默认每天
    else {
      description = '每天';
    }

    // 解析小时
    let timeStr = '';
    if (hour.includes('*/')) {
      const interval = hour.split('*/')[1];
      return `每${interval}小时一次`;
    } else if (hour.includes(',')) {
      const hours = hour.split(',').map(h => {
        const hourNum = parseInt(h);
        return getTimeDescription(hourNum, 0);
      });
      timeStr = hours.join('、');
    } else if (hour !== '*') {
      const hourNum = parseInt(hour);
      const minuteNum = minute !== '*' ? parseInt(minute) : 0;
      timeStr = getTimeDescription(hourNum, minuteNum);
    }

    if (timeStr) {
      description += ' ' + timeStr;
    }

    return description || cron;
  } catch (error) {
    return cron;
  }
};

// 辅助函数：将小时数转换为时间描述
const getTimeDescription = (hour: number, minute: number = 0): string => {
  const minuteStr = minute > 0 ? `${minute}分` : '';

  if (hour === 0) return `午夜12点${minuteStr}`;
  if (hour < 6) return `凌晨${hour}点${minuteStr}`;
  if (hour < 9) return `早上${hour}点${minuteStr}`;
  if (hour < 12) return `上午${hour}点${minuteStr}`;
  if (hour === 12) return `中午12点${minuteStr}`;
  if (hour < 18) return `下午${hour - 12}点${minuteStr}`;
  if (hour < 22) return `晚上${hour - 12}点${minuteStr}`;
  return `晚上${hour - 12}点${minuteStr}`;
};

// 取消
const handleCancel = () => {
  emit('cancel');
};

// ==================== 批量输入方法 ====================

/**
 * 显示批量输入模态框
 */
const showBatchInputModal = (fieldName: string) => {
  batchInputFieldName.value = fieldName;
  
  // 如果字段已有关键词，预填充到文本框
  const existingKeywords = presetParams.value.fieldKeywords[fieldName] || [];
  if (existingKeywords.length > 0) {
    batchInputText.value = existingKeywords.join(', ');
  } else {
    batchInputText.value = '';
  }
  
  batchInputModalVisible.value = true;
};

/**
 * 解析关键词字符串为数组
 * 支持多种分隔符：逗号(,)、中文逗号(，)、分号(;)、换行符
 * 示例输入："Skin, Analyzer, 3D, AI, Facial, Detector, Scanner, Spectra, Skin Analysis, Skin Scanner, skin imaging, Facial Imaging, pigmentation, skin elasticity"
 * 解析结果：14个关键词的数组
 */
const parseKeywordString = (text: string): string[] => {
  if (!text || text.trim() === '') {
    return [];
  }
  
  // 使用正则表达式分割，支持多种分隔符
  const keywords = text
    .split(/[,，;；\n\r]+/)  // 支持逗号、中文逗号、分号、换行
    .map(k => k.trim())      // 去除前后空格
    .filter(k => k.length > 0)  // 过滤空字符串
    .filter((k, index, arr) => arr.indexOf(k) === index);  // 去重
  
  return keywords;
};

/**
 * 确认批量输入
 */
const handleBatchInputOk = () => {
  if (!batchInputFieldName.value) {
    message.warning('无效的操作');
    return;
  }
  
  const keywords = parseKeywordString(batchInputText.value);
  
  if (keywords.length === 0) {
    message.warning('请输入至少一个关键词');
    return;
  }
  
  // 更新字段关键词
  presetParams.value.fieldKeywords[batchInputFieldName.value] = keywords;
  
  message.success(`成功添加 ${keywords.length} 个关键词到 ${batchInputFieldName.value}`);
  
  // 关闭模态框并清空
  batchInputModalVisible.value = false;
  batchInputText.value = '';
  batchInputFieldName.value = '';
};

/**
 * 取消批量输入
 */
const handleBatchInputCancel = () => {
  batchInputModalVisible.value = false;
  batchInputText.value = '';
  batchInputFieldName.value = '';
};

/**
 * 清空批量输入
 */
const clearBatchInput = () => {
  batchInputText.value = '';
};

/**
 * 删除单个解析的关键词
 */
const removeParsedKeyword = (index: number) => {
  const keywords = parseKeywordString(batchInputText.value);
  keywords.splice(index, 1);
  batchInputText.value = keywords.join(', ');
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

/* 字段标签和批量输入按钮布局 */
.field-label-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

/* 批量输入模态框样式 */
.batch-input-container {
  padding: 8px 0;
}

.input-section {
  margin-bottom: 16px;
}

.input-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #262626;
}

.preview-section {
  max-height: 300px;
  overflow-y: auto;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preview-title {
  font-weight: 500;
  color: #262626;
}

.preview-tags {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  min-height: 80px;
  border: 1px dashed #d9d9d9;
}

/* Cron预设卡片列表 */
.cron-preset-list {
  max-height: 500px;
  overflow-y: auto;
}

.cron-preset-card {
  padding: 12px 16px;
  margin-bottom: 12px;
  border: 2px solid #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.cron-preset-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.cron-preset-card.selected {
  border-color: #1890ff;
  background: #e6f7ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.preset-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.preset-label {
  font-weight: 600;
  font-size: 15px;
  color: #262626;
}

.preset-description {
  font-size: 13px;
  color: #595959;
  margin-bottom: 8px;
  line-height: 1.5;
}

.preset-cron {
  font-size: 12px;
  color: #8c8c8c;
}

.preset-cron code {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  padding: 4px 8px;
}

.form-help-text {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.form-help-text code {
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}
</style>

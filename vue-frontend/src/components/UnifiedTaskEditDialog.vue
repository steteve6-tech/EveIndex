<template>
  <a-modal
    v-model:open="visible"
    title="编辑任务"
    width="1000px"
    :destroy-on-close="true"
    @ok="handleSave"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <a-form :model="formData" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <!-- 基本信息 -->
        <a-divider orientation="left">基本信息</a-divider>
        
        <a-form-item label="任务名称">
          <a-input v-model:value="formData.taskName" placeholder="输入任务名称" />
        </a-form-item>

        <a-form-item label="任务描述">
          <a-textarea v-model:value="formData.description" placeholder="输入任务描述" :rows="2" />
        </a-form-item>

        <a-form-item label="关联爬虫">
          <a-tag color="blue">{{ formData.crawlerName }}</a-tag>
          <span class="ml-2 text-gray-500">{{ formData.countryCode }} - {{ formData.crawlerType }}</span>
        </a-form-item>

        <a-form-item label="任务状态">
          <a-switch v-model:checked="formData.enabled">
            <template #checkedChildren>启用</template>
            <template #unCheckedChildren>禁用</template>
          </a-switch>
          <span class="ml-2 text-gray-500">{{ formData.enabled ? '任务将按计划自动执行' : '任务已禁用，不会自动执行' }}</span>
        </a-form-item>

        <!-- 定时任务设置 -->
        <a-divider orientation="left">⏰ 定时任务设置</a-divider>

        <a-form-item label="执行频率">
          <a-radio-group v-model:value="scheduleType" @change="handleScheduleTypeChange" button-style="solid">
            <a-radio-button value="daily">
              <CalendarOutlined /> 每天
            </a-radio-button>
            <a-radio-button value="weekly">
              <CalendarOutlined /> 每周
            </a-radio-button>
            <a-radio-button value="monthly">
              <CalendarOutlined /> 每月
            </a-radio-button>
            <a-radio-button value="interval">
              <ClockCircleOutlined /> 间隔
            </a-radio-button>
          </a-radio-group>
        </a-form-item>

        <!-- 每天执行 -->
        <a-form-item label="执行时间" v-if="scheduleType === 'daily'">
          <a-time-picker 
            v-model:value="dailyTime" 
            format="HH:mm" 
            :minute-step="5" 
            style="width: 200px"
            placeholder="选择时间"
            size="large"
          />
          <span class="ml-2 text-gray-500">
            <InfoCircleOutlined /> 每天在此时间自动执行爬虫任务
          </span>
          <div class="mt-2">
            <a-alert 
              :message="`将在每天 ${dailyTime ? dailyTime.format('HH:mm') : '--:--'} 自动执行`" 
              type="info" 
              show-icon 
            />
          </div>
        </a-form-item>

        <!-- 每周执行 -->
        <template v-if="scheduleType === 'weekly'">
          <a-form-item label="选择日期">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-select v-model:value="weeklyDay" style="width: 100%" size="large">
                  <a-select-option :value="1">
                    <CalendarOutlined /> 星期一
                  </a-select-option>
                  <a-select-option :value="2">
                    <CalendarOutlined /> 星期二
                  </a-select-option>
                  <a-select-option :value="3">
                    <CalendarOutlined /> 星期三
                  </a-select-option>
                  <a-select-option :value="4">
                    <CalendarOutlined /> 星期四
                  </a-select-option>
                  <a-select-option :value="5">
                    <CalendarOutlined /> 星期五
                  </a-select-option>
                  <a-select-option :value="6">
                    <CalendarOutlined /> 星期六
                  </a-select-option>
                  <a-select-option :value="7">
                    <CalendarOutlined /> 星期日
                  </a-select-option>
                </a-select>
              </a-col>
              <a-col :span="12">
                <a-time-picker 
                  v-model:value="weeklyTime" 
                  format="HH:mm" 
                  :minute-step="5" 
                  style="width: 100%"
                  placeholder="选择时间"
                  size="large"
                />
              </a-col>
            </a-row>
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 20, offset: 4 }">
            <a-alert 
              :message="`将在每周${getWeekDayName(weeklyDay)} ${weeklyTime ? weeklyTime.format('HH:mm') : '--:--'} 自动执行`" 
              type="info" 
              show-icon 
            />
          </a-form-item>
        </template>

        <!-- 每月执行 -->
        <template v-if="scheduleType === 'monthly'">
          <a-form-item label="选择日期">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-input-number 
                  v-model:value="monthlyDay" 
                  :min="1" 
                  :max="31" 
                  style="width: 100%"
                  size="large"
                  placeholder="日期"
                >
                  <template #addonAfter>日</template>
                </a-input-number>
              </a-col>
              <a-col :span="12">
                <a-time-picker 
                  v-model:value="monthlyTime" 
                  format="HH:mm" 
                  :minute-step="5" 
                  style="width: 100%"
                  placeholder="选择时间"
                  size="large"
                />
              </a-col>
            </a-row>
          </a-form-item>
          <a-form-item :wrapper-col="{ span: 20, offset: 4 }">
            <a-alert 
              :message="`将在每月 ${monthlyDay} 日 ${monthlyTime ? monthlyTime.format('HH:mm') : '--:--'} 自动执行`" 
              type="info" 
              show-icon 
            />
          </a-form-item>
        </template>

        <!-- 间隔执行 -->
        <a-form-item label="执行间隔" v-if="scheduleType === 'interval'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-input-number 
                v-model:value="intervalValue" 
                :min="5" 
                :max="1440" 
                style="width: 100%"
                size="large"
                placeholder="间隔时长"
              />
            </a-col>
            <a-col :span="12">
              <a-select v-model:value="intervalUnit" style="width: 100%" size="large">
                <a-select-option value="minutes">
                  <ClockCircleOutlined /> 分钟
                </a-select-option>
                <a-select-option value="hours">
                  <ClockCircleOutlined /> 小时
                </a-select-option>
              </a-select>
            </a-col>
          </a-row>
          <div class="mt-2">
            <a-alert 
              :message="`每 ${intervalValue} ${intervalUnit === 'hours' ? '小时' : '分钟'} 自动执行一次`" 
              type="info" 
              show-icon 
            />
            <a-alert 
              message="建议间隔不少于5分钟，避免频繁请求" 
              type="warning" 
              show-icon 
              class="mt-2"
            />
          </div>
        </a-form-item>

        <!-- 快速设置常用时间 -->
        <a-form-item label="快速设置" v-if="scheduleType !== 'interval'">
          <a-space wrap>
            <a-tag 
              v-for="preset in timePresets" 
              :key="preset.label" 
              @click="applyTimePreset(preset)" 
              style="cursor: pointer; padding: 4px 12px;"
              :color="isCurrentPreset(preset) ? 'blue' : 'default'"
            >
              {{ preset.label }}
            </a-tag>
          </a-space>
        </a-form-item>

        <!-- 执行计划预览 -->
        <a-form-item label="执行计划">
          <a-card size="small" style="background: #f6f8fa;">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div>
                <div style="font-size: 16px; font-weight: 500; color: #1890ff; margin-bottom: 4px;">
                  {{ cronDescription }}
                </div>
                <div style="font-size: 12px; color: rgba(0,0,0,0.45);">
                  Cron: <code style="background: #fff; padding: 2px 8px; border-radius: 3px;">{{ currentCron }}</code>
                  <a-tooltip title="复制Cron表达式">
                    <CopyOutlined @click="copyCron" style="cursor: pointer; margin-left: 8px;" />
                  </a-tooltip>
                </div>
              </div>
              <div v-if="formData.nextExecutionTime" style="text-align: right;">
                <div style="font-size: 12px; color: rgba(0,0,0,0.45);">下次执行</div>
                <div style="font-size: 14px; font-weight: 500; color: #52c41a;">
                  {{ formatDateTime(formData.nextExecutionTime) }}
                </div>
              </div>
            </div>
          </a-card>
        </a-form-item>

        <!-- 预设参数查看 -->
        <a-divider orientation="left">📋 预设参数</a-divider>

        <a-form-item label="参数配置" :wrapper-col="{ span: 20, offset: 0 }">
          <a-card size="small" v-if="presetParameters && Object.keys(presetParameters).length > 0">
            <a-descriptions :column="1" bordered size="small">
              <!-- 通用参数 -->
              <a-descriptions-item label="最大记录数" v-if="presetParameters.maxRecords">
                {{ presetParameters.maxRecords === -1 ? '不限制' : presetParameters.maxRecords }}
              </a-descriptions-item>
              <a-descriptions-item label="批次大小" v-if="presetParameters.batchSize">
                {{ presetParameters.batchSize }}
              </a-descriptions-item>
              <a-descriptions-item label="日期范围" v-if="presetParameters.dateFrom || presetParameters.dateTo">
                {{ presetParameters.dateFrom || '不限' }} ~ {{ presetParameters.dateTo || '不限' }}
              </a-descriptions-item>
              <a-descriptions-item label="最近天数" v-if="presetParameters.recentDays">
                最近 {{ presetParameters.recentDays }} 天
              </a-descriptions-item>

              <!-- 字段关键词 -->
              <a-descriptions-item label="搜索关键词" v-if="presetParameters.fieldKeywords">
                <div v-for="(keywords, field) in presetParameters.fieldKeywords" :key="field" class="mb-2">
                  <a-tag color="blue">{{ formatFieldName(field) }}</a-tag>
                  <div class="mt-1">
                    <a-tag v-for="(keyword, idx) in keywords" :key="idx" color="default" style="margin: 2px;">
                      {{ keyword }}
                    </a-tag>
                    <span v-if="keywords && keywords.length === 0" class="text-gray-400">未设置</span>
                  </div>
                </div>
              </a-descriptions-item>
            </a-descriptions>

            <a-button type="link" @click="editPresetParameters" class="mt-2">
              <template #icon><EditOutlined /></template>
              编辑预设参数
            </a-button>
          </a-card>

          <a-alert v-else message="该爬虫尚未配置预设参数" type="info" show-icon>
            <template #description>
              点击下方按钮配置爬虫的预设参数
            </template>
          </a-alert>

          <a-button type="primary" @click="editPresetParameters" class="mt-2" v-if="!presetParameters || Object.keys(presetParameters).length === 0">
            <template #icon><SettingOutlined /></template>
            配置预设参数
          </a-button>
        </a-form-item>

        <!-- 执行历史 -->
        <a-divider orientation="left">📊 执行统计</a-divider>

        <a-form-item label="执行统计" :wrapper-col="{ span: 20, offset: 0 }">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic title="总执行次数" :value="formData.executionCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="成功次数" :value="formData.successCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="失败次数" :value="formData.failureCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic 
                title="成功率" 
                :value="formData.successRate || 0" 
                suffix="%"
                :precision="1"
                :value-style="{ color: (formData.successRate || 0) >= 80 ? '#3f8600' : '#cf1322' }"
              />
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item label="最后执行" v-if="formData.lastExecutionTime">
          <div>
            <ClockCircleOutlined /> {{ formatDateTime(formData.lastExecutionTime) }}
            <a-tag :color="formData.lastExecutionStatus === 'SUCCESS' ? 'success' : 'error'" class="ml-2">
              {{ formData.lastExecutionStatus === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </div>
        </a-form-item>

        <a-form-item label="下次执行" v-if="formData.enabled">
          <div>
            <CalendarOutlined /> {{ formatDateTime(formData.nextExecutionTime) || '计算中...' }}
          </div>
        </a-form-item>
      </a-form>
    </a-spin>

    <template #footer>
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleSave" :loading="saving">保存</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { 
  CopyOutlined, 
  EditOutlined, 
  SettingOutlined,
  ClockCircleOutlined,
  CalendarOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue';
import { cronToReadable, generateCronExpression, getCronPresets } from '@/utils/cronUtils';
import { updateTask, getPreset } from '@/api/crawler';

interface TaskData {
  id?: number;
  taskName: string;
  description: string;
  crawlerName: string;
  countryCode: string;
  crawlerType: string;
  enabled: boolean;
  cronExpression: string;
  executionCount?: number;
  successCount?: number;
  failureCount?: number;
  successRate?: number;
  lastExecutionTime?: string;
  lastExecutionStatus?: string;
  nextExecutionTime?: string;
}

const props = defineProps<{
  modelValue: boolean;
  task: TaskData | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'saved'): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const loading = ref(false);
const saving = ref(false);
const formData = ref<TaskData>({
  taskName: '',
  description: '',
  crawlerName: '',
  countryCode: '',
  crawlerType: '',
  enabled: true,
  cronExpression: '0 0 2 * * ?'
});

// 定时任务配置
const scheduleType = ref<'daily' | 'weekly' | 'monthly' | 'interval' | 'custom'>('daily');
const dailyTime = ref<Dayjs>(dayjs().hour(2).minute(0));
const weeklyDay = ref(1);
const weeklyTime = ref<Dayjs>(dayjs().hour(2).minute(0));
const monthlyDay = ref(1);
const monthlyTime = ref<Dayjs>(dayjs().hour(2).minute(0));
const intervalValue = ref(60);
const intervalUnit = ref('minutes');
const customCron = ref('0 0 2 * * ?');

// 预设参数
const presetParameters = ref<any>(null);

// Cron预设模板
const cronPresets = getCronPresets();

// 时间快捷预设
const timePresets = [
  { label: '凌晨 02:00', hour: 2, minute: 0, description: '夜间执行，不影响白天使用' },
  { label: '早上 08:00', hour: 8, minute: 0, description: '工作日开始前执行' },
  { label: '中午 12:00', hour: 12, minute: 0, description: '午休时间执行' },
  { label: '下午 18:00', hour: 18, minute: 0, description: '下班时间执行' },
  { label: '晚上 22:00', hour: 22, minute: 0, description: '睡前执行' },
  { label: '上午 10:00', hour: 10, minute: 0, description: '上午工作时间' },
  { label: '下午 14:00', hour: 14, minute: 0, description: '下午工作时间' },
  { label: '下午 16:00', hour: 16, minute: 0, description: '下午茶时间' }
];

// 当前Cron表达式
const currentCron = computed(() => {
  switch (scheduleType.value) {
    case 'daily':
      return generateCronExpression({
        type: 'daily',
        time: dailyTime.value.format('HH:mm')
      });
    case 'weekly':
      return generateCronExpression({
        type: 'weekly',
        time: weeklyTime.value.format('HH:mm'),
        dayOfWeek: weeklyDay.value
      });
    case 'monthly':
      return generateCronExpression({
        type: 'monthly',
        time: monthlyTime.value.format('HH:mm'),
        dayOfMonth: monthlyDay.value
      });
    case 'interval':
      const minutes = intervalUnit.value === 'hours' ? intervalValue.value * 60 : intervalValue.value;
      return generateCronExpression({
        type: 'interval',
        intervalMinutes: minutes
      });
    case 'custom':
      return customCron.value;
    default:
      return '0 0 2 * * ?';
  }
});

// Cron表达式描述
const cronDescription = computed(() => {
  return cronToReadable(currentCron.value).description;
});

// 监听任务变化
watch(() => props.task, async (task) => {
  if (task) {
    formData.value = { ...task };
    parseExistingCron(task.cronExpression);
    await loadPresetParameters(task.crawlerName);
  }
}, { immediate: true });

// 解析现有的Cron表达式
function parseExistingCron(cron: string) {
  const readable = cronToReadable(cron);
  
  switch (readable.type) {
    case 'daily':
      scheduleType.value = 'daily';
      if (readable.details.time) {
        const [h, m] = readable.details.time.split(':');
        dailyTime.value = dayjs().hour(parseInt(h)).minute(parseInt(m));
      }
      break;
    case 'weekly':
      scheduleType.value = 'weekly';
      weeklyDay.value = readable.details.dayOfWeek || 1;
      if (readable.details.time) {
        const [h, m] = readable.details.time.split(':');
        weeklyTime.value = dayjs().hour(parseInt(h)).minute(parseInt(m));
      }
      break;
    case 'monthly':
      scheduleType.value = 'monthly';
      monthlyDay.value = readable.details.dayOfMonth || 1;
      if (readable.details.time) {
        const [h, m] = readable.details.time.split(':');
        monthlyTime.value = dayjs().hour(parseInt(h)).minute(parseInt(m));
      }
      break;
    case 'custom':
      if (readable.details.interval) {
        scheduleType.value = 'interval';
        if (readable.details.interval >= 60) {
          intervalValue.value = readable.details.interval / 60;
          intervalUnit.value = 'hours';
        } else {
          intervalValue.value = readable.details.interval;
          intervalUnit.value = 'minutes';
        }
      } else {
        scheduleType.value = 'custom';
        customCron.value = cron;
      }
      break;
  }
}

// 加载预设参数
async function loadPresetParameters(crawlerName: string) {
  try {
    loading.value = true;
    const response = await getPreset(crawlerName);
    if (response.data && response.data.parameters) {
      // 解析JSON字符串
      if (typeof response.data.parameters === 'string') {
        presetParameters.value = JSON.parse(response.data.parameters);
      } else {
        presetParameters.value = response.data.parameters;
      }
    }
  } catch (error: any) {
    console.error('加载预设参数失败:', error);
    presetParameters.value = null;
  } finally {
    loading.value = false;
  }
}

// 处理频率类型变化
function handleScheduleTypeChange() {
  // 可以在这里添加额外的逻辑
}

// 应用时间预设
function applyTimePreset(preset: any) {
  const time = dayjs().hour(preset.hour).minute(preset.minute);
  
  switch (scheduleType.value) {
    case 'daily':
      dailyTime.value = time;
      break;
    case 'weekly':
      weeklyTime.value = time;
      break;
    case 'monthly':
      monthlyTime.value = time;
      break;
  }
  
  message.success(`已设置为 ${preset.label}`);
}

// 检查是否是当前选中的预设
function isCurrentPreset(preset: any): boolean {
  let currentTime: Dayjs | null = null;
  
  switch (scheduleType.value) {
    case 'daily':
      currentTime = dailyTime.value;
      break;
    case 'weekly':
      currentTime = weeklyTime.value;
      break;
    case 'monthly':
      currentTime = monthlyTime.value;
      break;
  }
  
  if (!currentTime) return false;
  
  return currentTime.hour() === preset.hour && currentTime.minute() === preset.minute;
}

// 获取星期几的名称
function getWeekDayName(day: number): string {
  const days = ['', '一', '二', '三', '四', '五', '六', '日'];
  return days[day] || '一';
}

// 复制Cron表达式
function copyCron() {
  navigator.clipboard.writeText(currentCron.value);
  message.success('Cron表达式已复制到剪贴板');
}

// 编辑预设参数
function editPresetParameters() {
  // 触发打开预设编辑对话框
  message.info('即将打开预设参数编辑器');
  // 这里可以emit一个事件，让父组件打开预设编辑对话框
  emit('update:modelValue', false);
  // 需要父组件处理打开预设编辑对话框的逻辑
}

// 格式化字段名
function formatFieldName(field: string): string {
  const fieldNames: Record<string, string> = {
    'searchQueries': '搜索查询',
    'companyNames': '企业名称',
    'productNames': '产品名称',
    'modelNames': '型号名称',
    'brandNames': '品牌名称',
    'deviceClasses': '设备分类',
    'keywords': '关键词'
  };
  return fieldNames[field] || field;
}

// 格式化日期时间
function formatDateTime(dateTime: string | undefined): string {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
}

// 保存任务
async function handleSave() {
  try {
    saving.value = true;
    
    // 更新Cron表达式
    formData.value.cronExpression = currentCron.value;
    
    // 调用API更新任务
    await updateTask(formData.value.id!, {
      taskName: formData.value.taskName,
      description: formData.value.description,
      enabled: formData.value.enabled,
      cronExpression: formData.value.cronExpression
    });
    
    message.success('任务更新成功');
    emit('saved');
    visible.value = false;
  } catch (error: any) {
    console.error('保存任务失败:', error);
    message.error('保存任务失败: ' + (error.message || '未知错误'));
  } finally {
    saving.value = false;
  }
}

// 取消编辑
function handleCancel() {
  visible.value = false;
}
</script>

<style scoped>
.ml-2 {
  margin-left: 8px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

.mb-2 {
  margin-bottom: 8px;
}

.text-gray-500 {
  color: rgba(0, 0, 0, 0.45);
}

.text-gray-400 {
  color: rgba(0, 0, 0, 0.25);
}

/* 时间选择器样式 */
:deep(.ant-picker-large) {
  border-radius: 6px;
}

:deep(.ant-input-number-large) {
  border-radius: 6px;
}

:deep(.ant-select-large .ant-select-selector) {
  border-radius: 6px;
}

/* 快速设置标签样式 */
:deep(.ant-tag) {
  transition: all 0.3s ease;
}

:deep(.ant-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 执行计划卡片样式 */
code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}

/* 提示框样式 */
:deep(.ant-alert) {
  border-radius: 6px;
}

/* 分隔线样式 */
:deep(.ant-divider-with-text-left::before) {
  width: 5%;
}

:deep(.ant-divider-with-text-left::after) {
  width: 95%;
}
</style>


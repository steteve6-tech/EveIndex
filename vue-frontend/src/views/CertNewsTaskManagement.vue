<template>
  <div class="cert-news-task-management">
    <!-- AI判断待审核通知 -->
    <a-alert
      v-if="pendingJudgmentCount > 0"
      type="warning"
      closable
      show-icon
      style="margin-bottom: 16px"
    >
      <template #message>
        <span>
          您有 <strong>{{ pendingJudgmentCount }}</strong> 条AI判断待审核
        </span>
      </template>
      <template #description>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>需要确认的认证新闻AI判断结果</span>
          <a-button type="primary" size="small" @click="showAIJudgmentReview">
            立即审核
          </a-button>
        </div>
      </template>
    </a-alert>

    <a-card title="认证新闻爬虫定时任务管理" :bordered="false">
      <!-- 操作按钮 -->
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddDialog">
            <template #icon><PlusOutlined /></template>
            新建任务
          </a-button>
          <a-button @click="initDefaultTasks" :loading="initLoading">
            <template #icon><ThunderboltOutlined /></template>
            初始化默认任务
          </a-button>
          <a-button @click="loadTasks">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>

      <!-- 统计卡片 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="6">
          <a-statistic
            title="总任务数"
            :value="taskStats.total"
            :prefix="h(CalendarOutlined)"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="启用任务"
            :value="taskStats.enabled"
            :prefix="h(CheckCircleOutlined)"
            :value-style="{ color: '#3f8600' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="执行成功"
            :value="taskStats.success"
            :prefix="h(SmileOutlined)"
            :value-style="{ color: '#52c41a' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="执行失败"
            :value="taskStats.failed"
            :prefix="h(FrownOutlined)"
            :value-style="{ color: '#cf1322' }"
          />
        </a-col>
      </a-row>

      <!-- 任务列表 -->
      <a-table
        :columns="columns"
        :data-source="tasks"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 爬虫类型 -->
          <template v-if="column.key === 'crawlerType'">
            <a-tag :color="getCrawlerTypeColor(record.crawlerType)">
              {{ getCrawlerTypeName(record.crawlerType) }}
            </a-tag>
          </template>

          <!-- 启用状态 -->
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              v-model:checked="record.enabled"
              @change="toggleTaskStatus(record)"
              :loading="record.switching"
            />
          </template>

          <!-- Cron表达式 -->
          <template v-else-if="column.key === 'cronExpression'">
            <a-tooltip :title="record.cronExpression">
              <span style="color: #1890ff; font-weight: 500;">{{ getCronDescription(record.cronExpression) }}</span>
            </a-tooltip>
          </template>

          <!-- 上次执行状态 -->
          <template v-else-if="column.key === 'lastExecuteStatus'">
            <a-tag v-if="record.lastExecuteStatus" :color="getStatusColor(record.lastExecuteStatus)">
              {{ record.lastExecuteStatus === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
            <span v-else class="text-gray">-</span>
          </template>

          <!-- 执行时间 -->
          <template v-else-if="column.key === 'lastExecuteTime'">
            <div v-if="record.lastExecuteTime">
              {{ formatDateTime(record.lastExecuteTime) }}
            </div>
            <span v-else class="text-gray">-</span>
          </template>

          <!-- 下次执行时间 -->
          <template v-else-if="column.key === 'nextExecuteTime'">
            <div v-if="record.nextExecuteTime && record.enabled">
              {{ formatDateTime(record.nextExecuteTime) }}
            </div>
            <span v-else class="text-gray">-</span>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="executeTask(record)">
                <template #icon><PlayCircleOutlined /></template>
                立即执行
              </a-button>
              <a-button type="link" size="small" @click="showEditDialog(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="showTaskLogs(record)">
                <template #icon><FileTextOutlined /></template>
                日志
              </a-button>
              <a-popconfirm
                title="确定要删除这个任务吗？"
                @confirm="deleteTask(record.id)"
              >
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 任务编辑对话框 -->
    <a-modal
      v-model:open="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @ok="handleSave"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="任务名称" name="taskName">
          <a-input v-model:value="formData.taskName" placeholder="请输入任务名称" />
        </a-form-item>

        <a-form-item label="爬虫类型" name="crawlerType">
          <a-select v-model:value="formData.crawlerType" placeholder="请选择爬虫类型">
            <a-select-option value="SGS">SGS 认证机构</a-select-option>
            <a-select-option value="UL">UL Solutions</a-select-option>
            <a-select-option value="BEICE">北测检测</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="执行时间" name="cronExpression">
          <a-button block @click="showCronBuilderModal = true" style="text-align: left">
            <template #icon><ClockCircleOutlined /></template>
            <span v-if="formData.cronExpression">{{ getCronDescription(formData.cronExpression) }}</span>
            <span v-else style="color: #999">点击设置执行时间</span>
          </a-button>
          <div class="form-help-text">
            <div class="cron-preview">
              <code>{{ formData.cronExpression || '未设置' }}</code>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="搜索关键词">
          <a-input
            v-model:value="formData.keyword"
            placeholder="可选，留空则爬取最新新闻"
          />
        </a-form-item>

        <a-form-item label="最大记录数">
          <a-input-number
            v-model:value="formData.maxRecords"
            :min="10"
            :max="200"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="任务描述">
          <a-textarea
            v-model:value="formData.description"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </a-form-item>

        <a-form-item label="启用任务" name="enabled">
          <a-switch v-model:checked="formData.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 任务日志对话框 -->
    <a-modal
      v-model:open="logsDialogVisible"
      title="任务执行日志"
      width="1200px"
      :footer="null"
    >
      <!-- 日志统计 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :span="8">
          <a-card size="small">
            <a-statistic title="总执行次数" :value="logs.length" suffix="次" />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card size="small">
            <a-statistic
              title="成功次数"
              :value="logs.filter(l => l.status === 'SUCCESS').length"
              suffix="次"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card size="small">
            <a-statistic
              title="失败次数"
              :value="logs.filter(l => l.status === 'FAILED').length"
              suffix="次"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <a-table
        :columns="logColumns"
        :data-source="logs"
        :loading="logsLoading"
        :pagination="logsPagination"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>

          <!-- 时间 -->
          <template v-else-if="column.key === 'startTime'">
            {{ formatDateTime(record.startTime) }}
          </template>

          <!-- 耗时 -->
          <template v-else-if="column.key === 'duration'">
            <span v-if="record.endTime && record.startTime">
              {{ calculateDuration(record.startTime, record.endTime) }}
            </span>
            <span v-else>-</span>
          </template>

          <!-- 结果 -->
          <template v-else-if="column.key === 'message'">
            <div style="max-width: 400px">
              <div>{{ record.message }}</div>
              <div v-if="record.errorMessage" class="error-message">
                错误: {{ record.errorMessage }}
              </div>
            </div>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- Cron可视化构建器对话框 -->
    <a-modal
      v-model:open="showCronBuilderModal"
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

    <!-- AI判断审核弹窗 -->
    <AIJudgmentReviewPopup
      v-model:visible="aiJudgmentReviewVisible"
      module-type="CERT_NEWS"
      @confirmed="handleAIJudgmentConfirmed"
      @rejected="handleAIJudgmentRejected"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PlusOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  EditOutlined,
  FileTextOutlined,
  DeleteOutlined,
  ThunderboltOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  SmileOutlined,
  FrownOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import {
  getAllTasks,
  createTask,
  updateTask,
  deleteTask as deleteTaskApi,
  enableTask,
  disableTask,
  executeTask as executeTaskApi,
  getTaskLogs,
  type CertNewsTaskConfig,
  type CertNewsTaskLog
} from '../api/certNewsTask'
import {
  getPendingCount
} from '@/api/aiJudgment'
import AIJudgmentReviewPopup from '@/components/AIJudgmentReviewPopup.vue'

// AI判断待审核相关
const aiJudgmentReviewVisible = ref(false)
const pendingJudgmentCount = ref(0)

// 任务列表
const tasks = ref<CertNewsTaskConfig[]>([])
const loading = ref(false)
const initLoading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 统计数据
const taskStats = computed(() => ({
  total: tasks.value.length,
  enabled: tasks.value.filter(t => t.enabled).length,
  success: tasks.value.filter(t => t.lastExecuteStatus === 'SUCCESS').length,
  failed: tasks.value.filter(t => t.lastExecuteStatus === 'FAILED').length
}))

// 表格列配置
const columns = [
  { title: '任务名称', dataIndex: 'taskName', key: 'taskName', width: 200 },
  { title: '爬虫类型', dataIndex: 'crawlerType', key: 'crawlerType', width: 120 },
  { title: '执行时间', dataIndex: 'cronExpression', key: 'cronExpression', width: 180 },
  { title: '启用', dataIndex: 'enabled', key: 'enabled', width: 80 },
  { title: '上次执行', dataIndex: 'lastExecuteStatus', key: 'lastExecuteStatus', width: 100 },
  { title: '上次执行时间', dataIndex: 'lastExecuteTime', key: 'lastExecuteTime', width: 180 },
  { title: '下次执行时间', dataIndex: 'nextExecuteTime', key: 'nextExecuteTime', width: 180 },
  { title: '操作', key: 'action', width: 300, fixed: 'right' as const }
]

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive<CertNewsTaskConfig>({
  taskName: '',
  crawlerType: '',
  cronExpression: '',
  enabled: true,
  keyword: '',
  maxRecords: 50,
  description: ''
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  crawlerType: [{ required: true, message: '请选择爬虫类型', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

// 日志对话框
const logsDialogVisible = ref(false)
const logs = ref<CertNewsTaskLog[]>([])
const logsLoading = ref(false)
const logsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const logColumns = [
  { title: '执行时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '成功数', dataIndex: 'successCount', key: 'successCount', width: 80 },
  { title: '失败数', dataIndex: 'errorCount', key: 'errorCount', width: 80 },
  { title: '耗时', dataIndex: 'duration', key: 'duration', width: 100 },
  { title: '执行结果', dataIndex: 'message', key: 'message' }
]

// Cron构建器
const showCronBuilderModal = ref(false)
const cronBuilderTab = ref('preset')
const tempCronExpression = ref('')

// Cron构建器数据
const cronBuilder = reactive({
  frequency: 'daily',
  time: dayjs().hour(2).minute(0),
  dayOfWeek: [] as string[],
  dayOfMonth: 1,
  minute: '0',
  intervalType: 'hours',
  intervalValue: 6
})

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
]

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const res: any = await getAllTasks()
    if (res.success) {
      tasks.value = res.data || []
      pagination.total = res.total || 0
    } else {
      message.error(res.message || '加载任务列表失败')
    }
  } catch (error: any) {
    message.error('加载任务列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新建定时任务'
  Object.assign(formData, {
    taskName: '',
    crawlerType: '',
    cronExpression: '0 0 2 * * ?',
    enabled: true,
    keyword: '',
    maxRecords: 50,
    description: ''
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (record: CertNewsTaskConfig) => {
  dialogTitle.value = '编辑定时任务'
  Object.assign(formData, { ...record })
  dialogVisible.value = true
}

// 保存任务
const handleSave = async () => {
  try {
    await formRef.value.validate()

    const apiFunc = formData.id ? updateTask : createTask
    const params = formData.id
      ? [formData.id, formData]
      : [formData]

    const res: any = await apiFunc(...params as any)

    if (res.success) {
      message.success(res.message || '保存成功')
      dialogVisible.value = false
      loadTasks()
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
  }
}

// 取消
const handleCancel = () => {
  formRef.value.resetFields()
  dialogVisible.value = false
}

// 切换任务状态
const toggleTaskStatus = async (record: CertNewsTaskConfig) => {
  record.switching = true
  try {
    const apiFunc = record.enabled ? enableTask : disableTask
    const res: any = await apiFunc(record.id!)

    if (res.success) {
      message.success(record.enabled ? '任务已启用' : '任务已禁用')
      loadTasks()
    } else {
      record.enabled = !record.enabled
      message.error(res.message || '操作失败')
    }
  } catch (error: any) {
    record.enabled = !record.enabled
    message.error('操作失败: ' + (error.message || '未知错误'))
  } finally {
    record.switching = false
  }
}

// 立即执行任务
const executeTask = async (record: CertNewsTaskConfig) => {
  const hide = message.loading('正在执行任务...', 0)
  try {
    const res: any = await executeTaskApi(record.id!)
    hide()

    if (res.success) {
      message.success('任务执行完成')
      loadTasks()
    } else {
      message.error(res.message || '任务执行失败')
    }
  } catch (error: any) {
    hide()
    message.error('任务执行失败: ' + (error.message || '未知错误'))
  }
}

// 删除任务
const deleteTask = async (id: number) => {
  try {
    const res: any = await deleteTaskApi(id)
    if (res.success) {
      message.success('删除成功')
      loadTasks()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (error: any) {
    message.error('删除失败: ' + (error.message || '未知错误'))
  }
}

// 显示任务日志
const showTaskLogs = async (record: CertNewsTaskConfig) => {
  logsDialogVisible.value = true
  logsLoading.value = true
  try {
    const res: any = await getTaskLogs(record.id!)
    if (res.success) {
      logs.value = res.data || []
      logsPagination.total = res.total || 0
    }
  } catch (error: any) {
    message.error('加载日志失败: ' + (error.message || '未知错误'))
  } finally {
    logsLoading.value = false
  }
}

// 工具函数
const getCrawlerTypeName = (type: string) => {
  const map: Record<string, string> = {
    'SGS': 'SGS',
    'UL': 'UL Solutions',
    'BEICE': '北测'
  }
  return map[type] || type
}

const getCrawlerTypeColor = (type: string) => {
  const map: Record<string, string> = {
    'SGS': 'blue',
    'UL': 'green',
    'BEICE': 'orange'
  }
  return map[type] || 'default'
}

const getStatusColor = (status: string) => {
  return status === 'SUCCESS' ? 'success' : 'error'
}

const getCronDescription = (cron: string) => {
  if (!cron) return '未设置'

  // 常用的Cron表达式映射
  const commonDescriptions: Record<string, string> = {
    '0 0 0 * * ?': '每天午夜12点',
    '0 0 1 * * ?': '每天凌晨1点',
    '0 0 2 * * ?': '每天凌晨2点',
    '0 0 3 * * ?': '每天凌晨3点',
    '0 0 4 * * ?': '每天凌晨4点',
    '0 0 5 * * ?': '每天凌晨5点',
    '0 0 6 * * ?': '每天早上6点',
    '0 0 7 * * ?': '每天早上7点',
    '0 0 8 * * ?': '每天早上8点',
    '0 0 9 * * ?': '每天上午9点',
    '0 0 10 * * ?': '每天上午10点',
    '0 0 11 * * ?': '每天上午11点',
    '0 0 12 * * ?': '每天中午12点',
    '0 0 13 * * ?': '每天下午1点',
    '0 0 14 * * ?': '每天下午2点',
    '0 0 15 * * ?': '每天下午3点',
    '0 0 16 * * ?': '每天下午4点',
    '0 0 17 * * ?': '每天下午5点',
    '0 0 18 * * ?': '每天下午6点',
    '0 0 19 * * ?': '每天晚上7点',
    '0 0 20 * * ?': '每天晚上8点',
    '0 0 21 * * ?': '每天晚上9点',
    '0 0 22 * * ?': '每天晚上10点',
    '0 0 23 * * ?': '每天晚上11点',
    '0 0 */6 * * ?': '每6小时一次',
    '0 0 */12 * * ?': '每12小时一次',
    '0 0 9 ? * MON': '每周一上午9点',
    '0 0 9 ? * MON-FRI': '工作日上午9点',
    '0 0 2 1 * ?': '每月1号凌晨2点'
  }

  // 如果有预设描述，直接返回
  if (commonDescriptions[cron]) {
    return commonDescriptions[cron]
  }

  // 否则解析Cron表达式
  try {
    const parts = cron.trim().split(/\s+/)
    if (parts.length < 6) return cron

    const [second, minute, hour, dayOfMonth, month, dayOfWeek] = parts
    let description = ''

    // 解析星期
    if (dayOfWeek && dayOfWeek !== '?' && dayOfWeek !== '*') {
      const weekMap: Record<string, string> = {
        'SUN': '周日', 'MON': '周一', 'TUE': '周二', 'WED': '周三',
        'THU': '周四', 'FRI': '周五', 'SAT': '周六',
        '1': '周一', '2': '周二', '3': '周三', '4': '周四',
        '5': '周五', '6': '周六', '7': '周日'
      }

      if (dayOfWeek.includes('-')) {
        const [start, end] = dayOfWeek.split('-')
        description = `每${weekMap[start]}至${weekMap[end]}`
      } else {
        description = `每${weekMap[dayOfWeek]}`
      }
    }
    // 解析月份中的日期
    else if (dayOfMonth && dayOfMonth !== '?' && dayOfMonth !== '*') {
      if (dayOfMonth.includes('/')) {
        const interval = dayOfMonth.split('/')[1]
        description = `每${interval}天`
      } else {
        description = `每月${dayOfMonth}号`
      }
    }
    // 默认每天
    else {
      description = '每天'
    }

    // 解析小时
    let timeStr = ''
    if (hour.includes('*/')) {
      const interval = hour.split('/')[1]
      return `每${interval}小时一次`
    } else if (hour.includes(',')) {
      const hours = hour.split(',').map(h => {
        const hourNum = parseInt(h)
        return getTimeDescription(hourNum, 0)
      })
      timeStr = hours.join('、')
    } else if (hour !== '*') {
      const hourNum = parseInt(hour)
      const minuteNum = minute !== '*' ? parseInt(minute) : 0
      timeStr = getTimeDescription(hourNum, minuteNum)
    }

    if (timeStr) {
      description += ' ' + timeStr
    }

    return description || cron
  } catch (error) {
    return cron
  }
}

// 辅助函数：将小时数转换为时间描述
const getTimeDescription = (hour: number, minute: number = 0): string => {
  const minuteStr = minute > 0 ? `${minute}分` : ''

  if (hour === 0) return `午夜12点${minuteStr}`
  if (hour < 6) return `凌晨${hour}点${minuteStr}`
  if (hour < 9) return `早上${hour}点${minuteStr}`
  if (hour < 12) return `上午${hour}点${minuteStr}`
  if (hour === 12) return `中午12点${minuteStr}`
  if (hour < 18) return `下午${hour - 12}点${minuteStr}`
  if (hour < 22) return `晚上${hour - 12}点${minuteStr}`
  return `晚上${hour - 12}点${minuteStr}`
}

const formatDateTime = (datetime: string) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm:ss')
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTasks()
}

// 初始化默认任务
const initDefaultTasks = async () => {
  initLoading.value = true
  try {
    // 默认任务配置
    const defaultTasks = [
      {
        taskName: 'SGS每日爬取',
        crawlerType: 'SGS',
        cronExpression: '0 0 2 * * ?',
        enabled: true,
        keyword: '',
        maxRecords: 50,
        description: '每天凌晨2点自动爬取SGS认证新闻'
      },
      {
        taskName: 'UL每日爬取',
        crawlerType: 'UL',
        cronExpression: '0 0 3 * * ?',
        enabled: true,
        keyword: '',
        maxRecords: 50,
        description: '每天凌晨3点自动爬取UL认证新闻'
      },
      {
        taskName: '北测每日爬取',
        crawlerType: 'BEICE',
        cronExpression: '0 0 4 * * ?',
        enabled: true,
        keyword: '',
        maxRecords: 50,
        description: '每天凌晨4点自动爬取北测认证新闻'
      }
    ]

    let successCount = 0
    let errorCount = 0

    for (const task of defaultTasks) {
      try {
        // 检查是否已存在同名任务
        const exists = tasks.value.some(t => t.taskName === task.taskName)
        if (exists) {
          console.log(`任务 ${task.taskName} 已存在，跳过`)
          continue
        }

        const res: any = await createTask(task)
        // 注意：request 拦截器已经返回了 response.data，所以这里直接用 res.success
        if (res.success) {
          successCount++
        } else {
          errorCount++
          console.error(`创建任务 ${task.taskName} 失败: ${res.message}`)
        }
      } catch (error: any) {
        errorCount++
        console.error(`创建任务 ${task.taskName} 失败:`, error)
      }
    }

    if (successCount > 0) {
      message.success(`成功创建 ${successCount} 个默认任务`)
      loadTasks()
    } else if (errorCount > 0) {
      message.warning(`所有任务都已存在或创建失败`)
    } else {
      message.info('所有默认任务都已存在')
    }
  } catch (error: any) {
    message.error('初始化默认任务失败: ' + (error.message || '未知错误'))
  } finally {
    initLoading.value = false
  }
}

// 打开Cron构建器
const openCronBuilder = () => {
  // 初始化tempCronExpression
  tempCronExpression.value = formData.cronExpression || '0 0 2 * * ?'
  cronBuilderTab.value = 'preset'
  showCronBuilderModal.value = true
}

// 应用Cron设置
const applyCronBuilder = () => {
  if (tempCronExpression.value) {
    formData.cronExpression = tempCronExpression.value
    showCronBuilderModal.value = false
    message.success('已设置执行时间')
  } else {
    message.warning('请选择或设置执行时间')
  }
}

// 取消Cron设置
const cancelCronBuilder = () => {
  showCronBuilderModal.value = false
  tempCronExpression.value = ''
  // 重置构建器
  cronBuilder.frequency = 'daily'
  cronBuilder.time = dayjs().hour(2).minute(0)
  cronBuilder.dayOfWeek = []
  cronBuilder.dayOfMonth = 1
  cronBuilder.minute = '0'
  cronBuilder.intervalType = 'hours'
  cronBuilder.intervalValue = 6
}

// 根据构建器参数生成Cron表达式
const buildCronExpression = () => {
  let cron = ''
  
  switch (cronBuilder.frequency) {
    case 'daily':
      // 每天指定时间
      const hour = cronBuilder.time.hour()
      const minute = cronBuilder.time.minute()
      cron = `0 ${minute} ${hour} * * ?`
      break
      
    case 'weekly':
      // 每周指定星期
      if (cronBuilder.dayOfWeek.length > 0) {
        const hour = cronBuilder.time.hour()
        const minute = cronBuilder.time.minute()
        const days = cronBuilder.dayOfWeek.join(',')
        cron = `0 ${minute} ${hour} ? * ${days}`
      } else {
        cron = '0 0 9 ? * MON'
      }
      break
      
    case 'monthly':
      // 每月指定日期
      const monthHour = cronBuilder.time.hour()
      const monthMinute = cronBuilder.time.minute()
      cron = `0 ${monthMinute} ${monthHour} ${cronBuilder.dayOfMonth} * ?`
      break
      
    case 'hourly':
      // 每小时
      cron = `0 ${cronBuilder.minute} * * * ?`
      break
      
    case 'interval':
      // 自定义间隔
      if (cronBuilder.intervalType === 'hours') {
        cron = `0 0 */${cronBuilder.intervalValue} * * ?`
      } else {
        cron = `0 0 2 */${cronBuilder.intervalValue} * ?`
      }
      break
  }
  
  tempCronExpression.value = cron
}

// 计算执行耗时
const calculateDuration = (startTime: string, endTime: string): string => {
  try {
    const start = dayjs(startTime)
    const end = dayjs(endTime)
    const diffMs = end.diff(start)

    if (diffMs < 0) return '-'

    const seconds = Math.floor(diffMs / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)

    if (hours > 0) {
      return `${hours}小时${minutes % 60}分${seconds % 60}秒`
    } else if (minutes > 0) {
      return `${minutes}分${seconds % 60}秒`
    } else {
      return `${seconds}秒`
    }
  } catch (error) {
    return '-'
  }
}

// ==================== AI判断相关函数 ====================

/**
 * 加载AI判断待审核数据
 */
const loadAIJudgmentData = async () => {
  try {
    const countResponse = await getPendingCount('CERT_NEWS')
    if (countResponse.data.success) {
      const counts = countResponse.data.data
      pendingJudgmentCount.value = Object.values(counts).reduce((sum: number, count) => sum + (count as number), 0)
    }
  } catch (error) {
    console.error('加载AI判断数据失败:', error)
  }
}

/**
 * 显示AI判断审核弹窗
 */
const showAIJudgmentReview = () => {
  aiJudgmentReviewVisible.value = true
}

/**
 * AI判断确认后的回调
 */
const handleAIJudgmentConfirmed = async (count: number) => {
  message.success(`已确认 ${count} 条AI判断`)
  // 重新加载AI判断数据
  await loadAIJudgmentData()
}

/**
 * AI判断拒绝后的回调
 */
const handleAIJudgmentRejected = async (id: number) => {
  message.info('已拒绝该AI判断')
  // 重新加载AI判断数据
  await loadAIJudgmentData()
}

// ==================== 结束 AI判断相关函数 ====================

onMounted(() => {
  loadTasks()
  loadAIJudgmentData()
})
</script>

<style scoped lang="less">
.cert-news-task-management {
  padding: 20px;
}

.text-gray {
  color: #999;
}

.form-help-text {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.error-message {
  color: #ff4d4f;
  margin-top: 4px;
  font-size: 12px;
}

code {
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.cron-preview {
  margin-top: 4px;
  color: #1890ff;

  strong {
    color: #096dd9;
  }
  
  code {
    padding: 4px 8px;
    background: #e6f7ff;
    border: 1px solid #91d5ff;
    color: #0050b3;
    font-size: 13px;
  }
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

  &:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
  }

  &.selected {
    border-color: #1890ff;
    background: #e6f7ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  }
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

  code {
    background: #fafafa;
    border: 1px solid #e8e8e8;
    padding: 4px 8px;
  }
}
</style>

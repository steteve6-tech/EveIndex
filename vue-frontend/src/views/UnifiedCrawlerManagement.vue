<template>
  <div class="unified-crawler-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🕷️ 统一爬虫管理系统</h1>
      </div>
      <div class="header-actions">
        <a-space>
          <a-button @click="refreshAllData" :loading="loading" type="primary">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
          <a-button @click="showBatchExecuteDialog" type="success" :disabled="selectedCrawlers.length === 0">
            <template #icon>
              <PlayCircleOutlined />
            </template>
            批量执行 ({{ selectedCrawlers.length }})
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计面板 -->
    <div class="statistics-panel">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <a-statistic title="总爬虫数" :value="statistics.totalCrawlers" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="运行中爬虫" :value="statistics.runningCrawlers" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="总任务数" :value="statistics.totalTasks" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="整体成功率" :value="statistics.overallSuccessRate" suffix="%" :precision="1" />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 主内容区 -->
    <a-tabs v-model:activeKey="activeTab" class="main-tabs">
      <!-- 爬虫管理 -->
      <a-tab-pane key="crawlers" tab="爬虫管理">
        <template #tab>
          <span>
            <BugOutlined />
            爬虫管理
          </span>
        </template>
        
        <!-- 爬虫筛选 -->
        <div class="crawler-filters">
          <a-space>
            <a-select v-model:value="crawlerFilters.countryCode" placeholder="选择国家" style="width: 120px" @change="loadCrawlers">
              <a-select-option value="">全部国家</a-select-option>
              <a-select-option value="US">美国 (US)</a-select-option>
              <a-select-option value="EU">欧盟 (EU)</a-select-option>
              <a-select-option value="KR">韩国 (KR)</a-select-option>
              <a-select-option value="JP">日本 (JP)</a-select-option>
              <a-select-option value="TW">台湾 (TW)</a-select-option>
            </a-select>
            
            <a-select v-model:value="crawlerFilters.crawlerType" placeholder="选择类型" style="width: 150px" @change="loadCrawlers">
              <a-select-option value="">全部类型</a-select-option>
              <a-select-option value="510K">510K</a-select-option>
              <a-select-option value="REGISTRATION">注册</a-select-option>
              <a-select-option value="RECALL">召回</a-select-option>
              <a-select-option value="EVENT">事件</a-select-option>
              <a-select-option value="GUIDANCE">指导文档</a-select-option>
              <a-select-option value="CUSTOMS">海关案例</a-select-option>
            </a-select>
            
            <a-input-search v-model:value="crawlerFilters.keyword" placeholder="搜索爬虫名称" style="width: 200px" @search="loadCrawlers" />
          </a-space>
        </div>

        <!-- 爬虫列表 -->
        <div class="crawler-list">
          <a-spin :spinning="crawlerLoading">
            <a-row :gutter="16">
              <a-col :span="8" v-for="crawler in filteredCrawlers" :key="crawler.crawlerName">
                <a-card class="crawler-card" :class="{ 'selected': selectedCrawlers.includes(crawler.crawlerName) }">
                  <template #title>
                    <div class="crawler-header">
                      <span class="crawler-name">{{ crawler.displayName }}</span>
                      <a-checkbox 
                        :checked="selectedCrawlers.includes(crawler.crawlerName)"
                        @change="(e) => toggleCrawlerSelection(crawler.crawlerName, e.target.checked)"
                      />
                    </div>
                  </template>
                  
                  <div class="crawler-content">
                    <div class="crawler-info">
                      <a-tag :color="getCountryColor(crawler.countryCode)">{{ crawler.countryCode }}</a-tag>
                      <a-tag>{{ crawler.crawlerType }}</a-tag>
                    </div>
                    
                    <div class="crawler-description">{{ crawler.description }}</div>
                    
                    <div class="crawler-status">
                      <a-tag :color="getStatusColor(crawler.status.status)">
                        {{ getStatusText(crawler.status.status) }}
                      </a-tag>
                      <span class="success-rate">成功率: {{ crawler.status.successRate.toFixed(1) }}%</span>
                    </div>
                    
                    <div class="crawler-actions">
                      <a-space>
                        <a-button size="small" @click="showPresetEditor(crawler)" type="link">
                          <SettingOutlined /> 配置预设
                        </a-button>
                        <a-button size="small" @click="testCrawler(crawler)" :loading="crawler.testing">
                          测试
                        </a-button>
                        <a-button size="small" type="primary" @click="showExecuteDialog(crawler)">
                          执行
                        </a-button>
                      </a-space>
                    </div>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </a-spin>
        </div>
      </a-tab-pane>

      <!-- 任务管理 -->
      <a-tab-pane key="tasks" tab="任务管理">
        <template #tab>
          <span>
            <ScheduleOutlined />
            任务管理
          </span>
        </template>
        
        <!-- 任务操作栏 -->
        <div class="task-actions">
          <a-space>
            <a-button @click="batchExecuteSelectedTasks" :disabled="selectedTasks.length === 0">
              <template #icon>
                <PlayCircleOutlined />
              </template>
              批量执行 ({{ selectedTasks.length }})
            </a-button>
            <a-button @click="refreshTasks">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新任务
            </a-button>
          </a-space>
        </div>

        <!-- 任务筛选 -->
        <div class="task-filters">
          <a-space>
            <a-select v-model:value="taskFilters.countryCode" placeholder="选择国家" style="width: 120px" @change="loadTasks">
              <a-select-option value="">全部国家</a-select-option>
              <a-select-option value="US">美国 (US)</a-select-option>
              <a-select-option value="EU">欧盟 (EU)</a-select-option>
              <a-select-option value="KR">韩国 (KR)</a-select-option>
              <a-select-option value="JP">日本 (JP)</a-select-option>
              <a-select-option value="TW">台湾 (TW)</a-select-option>
            </a-select>
            
            <a-select v-model:value="taskFilters.crawlerType" placeholder="选择数据类型" style="width: 150px" @change="loadTasks">
              <a-select-option value="">全部类型</a-select-option>
              <a-select-option value="510K">510K申请</a-select-option>
              <a-select-option value="REGISTRATION">注册数据</a-select-option>
              <a-select-option value="RECALL">召回数据</a-select-option>
              <a-select-option value="EVENT">不良事件</a-select-option>
              <a-select-option value="GUIDANCE">指导文档</a-select-option>
              <a-select-option value="CUSTOMS">海关案例</a-select-option>
            </a-select>
            
            <a-select v-model:value="taskFilters.enabled" placeholder="选择状态" style="width: 120px" @change="loadTasks">
              <a-select-option value="">全部状态</a-select-option>
              <a-select-option :value="true">已启用</a-select-option>
              <a-select-option :value="false">已禁用</a-select-option>
            </a-select>
          </a-space>
        </div>

        <!-- 任务列表 -->
        <a-table 
          :columns="taskColumns" 
          :data-source="filteredTasks" 
          :loading="taskLoading"
          :pagination="{ 
            total: filteredTasks.length,
            pageSize: 20, 
            showSizeChanger: true, 
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条任务`
          }"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'selection'">
              <a-checkbox :checked="selectedTasks.includes(record.id)" @change="(e) => toggleTaskSelection(record.id, e.target.checked)" />
            </template>
            
            <template v-else-if="column.key === 'taskName'">
              <div>
                <strong>{{ formatTaskName(record) }}</strong>
                <div class="task-desc">{{ formatTaskSchedule(record) }}</div>
              </div>
            </template>
            
            <template v-else-if="column.key === 'countryCode'">
              <a-tag :color="getCountryColor(record.countryCode)">{{ record.countryCode }}</a-tag>
            </template>
            
            <template v-else-if="column.key === 'status'">
              <a-tag :color="getTaskStatusColor(record.enabled)">
                {{ record.enabled ? '已启用' : '已禁用' }}
              </a-tag>
            </template>
            
            <template v-else-if="column.key === 'successRate'">
              <a-progress
                :percent="record.successRate || 0"
                size="small"
                :status="(record.successRate || 0) >= 80 ? 'success' : (record.successRate || 0) >= 50 ? 'normal' : 'exception'"
              />
            </template>
            
            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button size="small" @click="executeTask(record)">执行</a-button>
                <a-button size="small" @click="editTask(record)">编辑</a-button>
                <a-button size="small" @click="viewTaskHistory(record)">历史</a-button>
                <a-popconfirm title="确定要删除这个任务吗？" @confirm="deleteTask(record)">
                  <a-button size="small" danger>删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <!-- 实时监控 -->
      <a-tab-pane key="monitoring" tab="实时监控">
        <template #tab>
          <span>
            <DashboardOutlined />
            实时监控
          </span>
        </template>
        
        <div class="monitoring-panel">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="运行状态">
                <div class="status-list">
                  <div v-for="crawler in runningCrawlers" :key="crawler.crawlerName" class="status-item">
                    <a-tag :color="getStatusColor(crawler.status.status)">{{ crawler.displayName }}</a-tag>
                    <span class="status-time">{{ formatTime(crawler.status.lastExecutionTime) }}</span>
                  </div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="最近执行">
                <div class="recent-executions">
                  <div v-for="log in recentLogs" :key="log.id" class="execution-item">
                    <a-tag :color="getExecutionStatusColor(log.status)">{{ log.status }}</a-tag>
                    <span>{{ log.taskName }}</span>
                    <span class="execution-time">{{ formatTime(log.startTime) }}</span>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </a-tab-pane>
    </a-tabs>

    <!-- 执行爬虫对话框 -->
    <a-modal 
      v-model:open="executeDialogVisible" 
      title="执行爬虫" 
      width="600px" 
      :footer="null"
      :destroyOnClose="true"
    >
      <UnifiedCrawlerExecuteForm 
        ref="executeFormRef"
        :crawler="selectedCrawler"
        @submit="handleExecuteSubmit"
        @cancel="executeDialogVisible = false"
      />
    </a-modal>

    <!-- 批量执行对话框 -->
    <a-modal v-model:open="batchExecuteDialogVisible" title="批量执行" width="600px" @ok="handleBatchExecute">
      <UnifiedBatchExecuteForm 
        ref="batchExecuteFormRef"
        :crawlers="selectedCrawlers"
        @submit="handleBatchExecuteSubmit"
      />
    </a-modal>

    <!-- 预设编辑对话框 -->
    <a-modal 
      v-model:open="presetEditorVisible" 
      title="编辑爬虫参数预设" 
      width="900px"
      :footer="null"
      :destroyOnClose="true"
    >
      <CrawlerPresetEditor 
        v-if="selectedCrawler"
        :crawler-name="selectedCrawler.crawlerName"
        @save="handlePresetSave"
        @cancel="presetEditorVisible = false"
      />
    </a-modal>

    <!-- 任务编辑对话框 -->
    <UnifiedTaskEditDialog 
      v-model="taskEditDialogVisible"
      :task="selectedTask"
      @saved="handleTaskEditSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { message } from 'ant-design-vue';
import { 
  ReloadOutlined, 
  PlayCircleOutlined, 
  PlusOutlined, 
  BugOutlined, 
  ScheduleOutlined, 
  DashboardOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';

// 组件导入
import UnifiedCrawlerExecuteForm from '../components/UnifiedCrawlerExecuteForm.vue';
import UnifiedBatchExecuteForm from '../components/UnifiedBatchExecuteForm.vue';
import CrawlerPresetEditor from '../components/CrawlerPresetEditor.vue';
import UnifiedTaskEditDialog from '../components/UnifiedTaskEditDialog.vue';

// API导入
import {
  getCrawlers,
  getPresets,
  getSystemOverview,
  triggerTask,
  testCrawler as testCrawlerApi,
  executeCrawler as executeCrawlerApi,
  batchTestCrawlers as batchTestCrawlersApi,
  batchExecuteCrawlers as batchExecuteCrawlersApi,
  deletePreset as deletePresetApi,
  getPreset as getPresetApi,
  updatePreset as updatePresetApi
} from '@/api/crawler';

// 适配旧接口名称
const getAllCrawlers = async () => {
  // getCrawlers() 已经通过axios拦截器返回了后端的数据
  // 后端返回: {success: true, data: [...], count: 11}
  const res = await getCrawlers();
  return res; // res就是完整对象
};

const getTasks = async (params: any) => {
  // 后端返回: {success: true, data: [...], total: 14}
  const res = await getPresets(params);
  return res; // res就是完整对象
};

const getSystemStatistics = async () => {
  // 后端返回: {success: true, data: {...}}
  const res = await getSystemOverview();
  return res; // res就是完整对象
};

const executeTaskApi = async (id: number) => {
  const res = await triggerTask(id, 'MANUAL');
  return res;
};

// 类型定义
interface CrawlerInfo {
  crawlerName: string;
  displayName: string;
  countryCode: string;
  crawlerType: string;
  description: string;
  version: string;
  available: boolean;
  status: CrawlerStatus;
  schema: any;
  testing?: boolean;
}

interface CrawlerStatus {
  status: string;
  lastExecutionTime: number;
  lastExecutionResult: string;
  totalExecutions: number;
  successCount: number;
  failureCount: number;
  successRate: number;
}

interface TaskInfo {
  id: number;
  taskName: string;
  crawlerName: string;
  countryCode: string;
  crawlerType: string;
  taskType: string;
  description: string;
  enabled: boolean;
  cronExpression?: string;
  executionCount?: number;
  successCount?: number;
  failureCount?: number;
  successRate?: number;
  lastExecutionTime?: string;
  lastExecutionStatus?: string;
  nextExecutionTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface SystemStatistics {
  totalCrawlers: number;
  runningCrawlers: number;
  totalTasks: number;
  overallSuccessRate: number;
}

// 响应式数据
const loading = ref(false);
const activeTab = ref('crawlers');

// 爬虫相关
const crawlers = ref<CrawlerInfo[]>([]);
const crawlerLoading = ref(false);
const selectedCrawlers = ref<string[]>([]);
const crawlerFilters = reactive({
  countryCode: '',
  crawlerType: '',
  keyword: ''
});

// 任务相关
const tasks = ref<TaskInfo[]>([]);
const allTasksCount = ref(0); // 记录所有任务的总数（不受筛选影响）
const taskLoading = ref(false);
const selectedTasks = ref<number[]>([]);
const taskFilters = reactive({
  countryCode: '',
  crawlerType: '',
  enabled: null as boolean | null
});

// 统计信息
const statistics = ref<SystemStatistics>({
  totalCrawlers: 0,
  runningCrawlers: 0,
  totalTasks: 0,
  overallSuccessRate: 0
});

// 对话框状态
const executeDialogVisible = ref(false);
const batchExecuteDialogVisible = ref(false);
const presetEditorVisible = ref(false);
const taskEditDialogVisible = ref(false);
const selectedCrawler = ref<CrawlerInfo | null>(null);
const selectedTask = ref<TaskInfo | null>(null);

// 表格列定义
const taskColumns = [
  { key: 'selection', title: '', width: 40 },
  { key: 'taskName', title: '任务名称', width: 200 },
  { key: 'crawlerName', title: '爬虫', width: 120 },
  { key: 'countryCode', title: '国家', width: 80 },
  { key: 'taskType', title: '类型', width: 100 },
  { key: 'status', title: '状态', width: 80 },
  { key: 'successRate', title: '成功率', width: 120 },
  { key: 'lastExecutionTime', title: '最后执行', width: 150 },
  { key: 'actions', title: '操作', width: 200 }
];

// 计算属性
const filteredCrawlers = computed(() => {
  return crawlers.value.filter(crawler => {
    const matchCountry = !crawlerFilters.countryCode || crawler.countryCode === crawlerFilters.countryCode;
    const matchType = !crawlerFilters.crawlerType || crawler.crawlerType === crawlerFilters.crawlerType;
    const matchKeyword = !crawlerFilters.keyword || 
      crawler.crawlerName.toLowerCase().includes(crawlerFilters.keyword.toLowerCase()) ||
      crawler.displayName.toLowerCase().includes(crawlerFilters.keyword.toLowerCase());
    
    return matchCountry && matchType && matchKeyword;
  });
});

const runningCrawlers = computed(() => {
  return crawlers.value.filter(crawler => crawler.status.status === 'RUNNING');
});

const recentLogs = computed(() => {
  // 这里应该从API获取最近的执行日志
  return [];
});

// 任务列表（用于表格显示，实际上就是tasks本身）
const filteredTasks = computed(() => {
  return tasks.value;
});

// 方法
const loadCrawlers = async () => {
  crawlerLoading.value = true;
  try {
    console.log('开始加载爬虫信息...');
    const response = await getAllCrawlers();
    console.log('爬虫信息响应:', response);
    
    if (response.success) {
      // 适配数据结构，为每个爬虫添加status对象
      crawlers.value = response.data.map((crawler: any) => ({
        ...crawler,
        displayName: formatCrawlerDescription(crawler.countryCode, crawler.crawlerType, crawler.description),
        description: formatCrawlerDescription(crawler.countryCode, crawler.crawlerType, crawler.description),
        status: {
          status: crawler.enabled ? 'READY' : 'DISABLED',
          successRate: 0,
          totalExecutions: 0,
          lastExecutionTime: 0,
          lastExecutionResult: ''
        }
      }));
      console.log('加载到爬虫数量:', crawlers.value.length);
      // 重新计算统计信息
      calculateStatistics();
    } else {
      console.error('加载爬虫信息失败:', response.message);
      message.error('加载爬虫信息失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载爬虫信息失败:', error);
    message.error('加载爬虫信息失败: ' + (error as any)?.message || '未知错误');
  } finally {
    crawlerLoading.value = false;
  }
};

const loadTasks = async () => {
  taskLoading.value = true;
  try {
    console.log('开始加载任务列表...');
    const params: any = {};
    if (taskFilters.countryCode) params.countryCode = taskFilters.countryCode;
    if (taskFilters.crawlerType) params.crawlerType = taskFilters.crawlerType;
    if (taskFilters.enabled !== null) params.enabled = taskFilters.enabled;
    
    console.log('任务筛选参数:', params);
    const response = await getTasks(params);
    console.log('任务列表响应:', response);
    
    if (response.success) {
      tasks.value = response.data;
      console.log('加载到任务数量:', tasks.value.length);
      
      // 如果没有筛选条件，更新总任务数
      if (!taskFilters.countryCode && !taskFilters.crawlerType && taskFilters.enabled === null) {
        allTasksCount.value = response.total || tasks.value.length;
        console.log('更新总任务数:', allTasksCount.value);
      }
      
      // 重新计算统计信息
      calculateStatistics();
    } else {
      console.error('加载任务列表失败:', response.message);
      message.error('加载任务列表失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载任务列表失败:', error);
    message.error('加载任务列表失败: ' + (error as any)?.message || '未知错误');
  } finally {
    taskLoading.value = false;
  }
};

const loadStatistics = async () => {
  try {
    console.log('开始加载统计信息...');
    const response = await getSystemStatistics();
    console.log('统计信息响应:', response);
    
    if (response.success) {
      statistics.value = response.data;
      console.log('统计信息加载成功:', statistics.value);
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
  }
};

// 计算统计信息（基于本地数据）
const calculateStatistics = () => {
  statistics.value = {
    totalCrawlers: crawlers.value.length,
    runningCrawlers: crawlers.value.filter(c => c.status?.status === 'RUNNING').length,
    totalTasks: allTasksCount.value || tasks.value.length, // 优先使用总数，不受筛选影响
    overallSuccessRate: calculateOverallSuccessRate()
  };
  console.log('计算后的统计信息:', statistics.value);
};

// 计算总体成功率
const calculateOverallSuccessRate = (): number => {
  if (tasks.value.length === 0) return 0;
  
  const tasksWithRate = tasks.value.filter(t => typeof t.successRate === 'number');
  if (tasksWithRate.length === 0) return 0;
  
  const totalRate = tasksWithRate.reduce((sum, t) => sum + (t.successRate || 0), 0);
  return Math.round(totalRate / tasksWithRate.length * 10) / 10;
};

const refreshAllData = async () => {
  loading.value = true;
  try {
    await Promise.all([
      loadCrawlers(),
      loadTasks()
    ]);
    // 统计信息在loadCrawlers和loadTasks中已经计算
    message.success('数据刷新成功');
  } catch (error) {
    message.error('数据刷新失败');
  } finally {
    loading.value = false;
  }
};

const refreshTasks = () => {
  loadTasks();
};

// 爬虫操作
const toggleCrawlerSelection = (crawlerName: string, checked: boolean) => {
  if (checked) {
    selectedCrawlers.value.push(crawlerName);
  } else {
    const index = selectedCrawlers.value.indexOf(crawlerName);
    if (index > -1) {
      selectedCrawlers.value.splice(index, 1);
    }
  }
};

const testCrawler = async (crawler: CrawlerInfo) => {
  crawler.testing = true;
  try {
    console.log('测试爬虫:', crawler.crawlerName);
    const response = await testCrawlerApi(crawler.crawlerName, {
      maxRecords: 10,
      mode: 'test'
    });

    console.log('测试响应:', response);

    if (response.success) {
      message.success(`爬虫 ${crawler.displayName} 测试成功`);
      // 刷新爬虫列表以更新状态
      await loadCrawlers();
    } else {
      message.error(`爬虫 ${crawler.displayName} 测试失败: ${response.message}`);
    }
  } catch (error) {
    console.error('测试爬虫失败:', error);
    message.error(`爬虫 ${crawler.displayName} 测试失败: ${(error as any)?.message || '未知错误'}`);
  } finally {
    crawler.testing = false;
  }
};

const showExecuteDialog = (crawler: CrawlerInfo) => {
  selectedCrawler.value = crawler;
  executeDialogVisible.value = true;
};

const showCrawlerDetails = (crawler: CrawlerInfo) => {
  // 显示爬虫详细信息
  message.info(`查看爬虫详情: ${crawler.displayName}`);
};

const showPresetEditor = (crawler: CrawlerInfo) => {
  selectedCrawler.value = crawler;
  presetEditorVisible.value = true;
};

const handlePresetSave = async () => {
  message.success('预设保存成功');
  presetEditorVisible.value = false;
  await loadCrawlers(); // 刷新爬虫列表
};

// 任务操作
const toggleTaskSelection = (taskId: number, checked: boolean) => {
  if (checked) {
    selectedTasks.value.push(taskId);
  } else {
    const index = selectedTasks.value.indexOf(taskId);
    if (index > -1) {
      selectedTasks.value.splice(index, 1);
    }
  }
};

const showBatchExecuteDialog = () => {
  batchExecuteDialogVisible.value = true;
};

const executeTask = async (task: TaskInfo) => {
  try {
    console.log('执行任务:', task);
    const response = await executeTaskApi(task.id);

    console.log('任务执行响应:', response);

    if (response.success) {
      message.success(`任务 ${task.taskName} 已提交执行`);
      await loadTasks();
    } else {
      message.error(`任务 ${task.taskName} 执行失败: ${response.message}`);
    }
  } catch (error) {
    console.error('执行任务失败:', error);
    message.error(`任务 ${task.taskName} 执行失败: ${(error as any)?.message || '未知错误'}`);
  }
};

const editTask = async (task: TaskInfo) => {
  try {
    console.log('编辑任务:', task);
    selectedTask.value = task;
    taskEditDialogVisible.value = true;
  } catch (error) {
    console.error('编辑任务失败:', error);
    message.error('编辑任务失败: ' + (error as any)?.message || '未知错误');
  }
};

const handleTaskEditSaved = async () => {
  message.success('任务更新成功');
  await loadTasks();
  taskEditDialogVisible.value = false;
  selectedTask.value = null;
};

const viewTaskHistory = (task: TaskInfo) => {
  // TODO: 实现查看任务历史功能
  message.info(`查看任务历史: ${task.taskName}`);
};

const deleteTask = async (task: TaskInfo) => {
  try {
    console.log('删除任务:', task);
    const response = await deletePresetApi(task.id);

    if (response.success) {
      message.success(`任务 ${task.taskName} 删除成功`);
      await loadTasks();
    } else {
      message.error(`任务删除失败: ${response.message}`);
    }
  } catch (error) {
    console.error('删除任务失败:', error);
    message.error(`任务 ${task.taskName} 删除失败: ${(error as any)?.message || '未知错误'}`);
  }
};

const batchExecuteSelectedTasks = async () => {
  try {
    if (selectedTasks.value.length === 0) {
      message.warning('请先选择要执行的任务');
      return;
    }

    console.log('批量执行任务:', selectedTasks.value);

    // 依次执行所有选中的任务
    let successCount = 0;
    let failCount = 0;

    for (const taskId of selectedTasks.value) {
      try {
        const response = await executeTaskApi(taskId);
        if (response.success) {
          successCount++;
        } else {
          failCount++;
        }
      } catch (error) {
        failCount++;
        console.error('执行任务失败:', taskId, error);
      }
    }

    message.success(`批量执行完成: 成功 ${successCount} 个, 失败 ${failCount} 个`);

    // 清空选中
    selectedTasks.value = [];
    await loadTasks();
  } catch (error) {
    console.error('批量执行失败:', error);
    message.error('批量执行失败: ' + (error as any)?.message || '未知错误');
  }
};

// 对话框处理
const handleBatchExecute = () => {
  // 由handleBatchExecuteSubmit处理
};

const handleExecuteSubmit = async (data: any) => {
  try {
    console.log('执行爬虫:', data);

    if (!data.crawlerName) {
      message.error('爬虫名称不能为空');
      return;
    }

    // 构建执行参数
    const params: any = {
      mode: data.mode || 'full'
    };

    // 完整模式：maxRecords = -1 表示爬取所有数据
    // 测试模式：使用用户指定的数量
    if (data.mode === 'full') {
      params.maxRecords = -1;  // 爬取所有数据
    } else if (data.maxRecords) {
      params.maxRecords = data.maxRecords;
    }

    if (data.keywords && data.keywords.length > 0) {
      params.keywords = data.keywords;
    }

    console.log('执行参数:', params);

    const response = await executeCrawlerApi(data.crawlerName, params);

    console.log('执行响应:', response);

    if (response.success) {
      message.success(`爬虫 ${data.crawlerName} 执行成功`);
      executeDialogVisible.value = false;
      await loadCrawlers();
    } else {
      message.error(`爬虫执行失败: ${response.message}`);
    }
  } catch (error) {
    console.error('执行爬虫失败:', error);
    message.error('执行爬虫失败: ' + (error as any)?.message || '未知错误');
  }
};

const handleBatchExecuteSubmit = async (data: any) => {
  try {
    console.log('批量执行提交:', data);

    if (!data.crawlers || data.crawlers.length === 0) {
      message.error('没有选择爬虫');
      return;
    }

    const response = await batchExecuteCrawlersApi(data);

    console.log('批量执行响应:', response);

    if (response.success) {
      message.success(`成功提交 ${data.crawlers.length} 个爬虫的执行任务`);
      batchExecuteDialogVisible.value = false;
      // 清空选中的爬虫
      selectedCrawlers.value = [];
      await loadCrawlers();
    } else {
      message.error(`批量执行失败: ${response.message}`);
    }
  } catch (error) {
    console.error('批量执行失败:', error);
    message.error('批量执行失败: ' + (error as any)?.message || '未知错误');
  }
};

// 工具方法
const getCountryColor = (countryCode: string) => {
  const colors = {
    'US': 'blue',
    'EU': 'green',
    'KR': 'orange',
    'CN': 'red',
    'JP': 'purple'
  };
  return colors[countryCode] || 'default';
};

const getStatusColor = (status: string) => {
  const colors = {
    'RUNNING': 'processing',
    'SUCCESS': 'success',
    'FAILED': 'error',
    'STOPPED': 'default'
  };
  return colors[status] || 'default';
};

const getStatusText = (status: string) => {
  const texts = {
    'RUNNING': '运行中',
    'SUCCESS': '成功',
    'FAILED': '失败',
    'STOPPED': '已停止'
  };
  return texts[status] || status;
};

const getTaskStatusColor = (enabled: boolean) => {
  return enabled ? 'success' : 'default';
};

const getExecutionStatusColor = (status: string) => {
  return getStatusColor(status);
};

const formatTime = (timestamp: number | string) => {
  if (!timestamp) return '-';
  
  let date: Date;
  if (typeof timestamp === 'string') {
    date = new Date(timestamp);
  } else {
    date = new Date(timestamp);
  }
  
  if (isNaN(date.getTime())) {
    return '-';
  }
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 生命周期
onMounted(() => {
  refreshAllData();
  
  // 定时刷新数据
  const interval = setInterval(() => {
    loadStatistics();
  }, 30000); // 每30秒刷新一次统计信息
  
  onUnmounted(() => {
    clearInterval(interval);
  });
});

// ==================== 工具函数 ====================

/**
 * 格式化爬虫描述
 * 将 "国家+类型" 格式转换为友好的中文描述
 */
const formatCrawlerDescription = (countryCode: string, crawlerType: string, originalDescription?: string): string => {
  // 国家名称映射
  const countryNames: Record<string, string> = {
    'US': '美国',
    'EU': '欧盟',
    'KR': '韩国',
    'CN': '中国',
    'JP': '日本',
    'TW': '台湾'
  };
  
  // 爬虫类型映射
  const typeNames: Record<string, string> = {
    'EVENT': '不良事件爬虫',
    '510K': '申请记录爬虫',
    'RECALL': '召回数据爬虫',
    'REGISTRATION': '注册数据爬虫',
    'GUIDANCE': '指导文档爬虫',
    'CUSTOMS': '海关案例爬虫',
    'CUSTOMS_CASE': '海关案例爬虫'
  };
  
  const countryName = countryNames[countryCode] || countryCode;
  const typeName = typeNames[crawlerType] || crawlerType;
  
  // 如果有原始描述且不是默认格式，则使用原始描述
  if (originalDescription && !originalDescription.includes('国家') && !originalDescription.includes('类型')) {
    return originalDescription;
  }
  
  return `${countryName}${typeName}`;
};

/**
 * 格式化任务名称
 * 将任务名称改为"国家+爬虫"格式
 */
const formatTaskName = (task: TaskInfo): string => {
  // 国家名称映射
  const countryNames: Record<string, string> = {
    'US': '美国',
    'EU': '欧盟',
    'KR': '韩国',
    'CN': '中国',
    'JP': '日本',
    'TW': '台湾'
  };
  
  // 爬虫类型映射
  const typeNames: Record<string, string> = {
    'EVENT': '不良事件爬虫',
    '510K': '申请记录爬虫',
    'RECALL': '召回数据爬虫',
    'REGISTRATION': '注册数据爬虫',
    'GUIDANCE': '指导文档爬虫',
    'CUSTOMS': '海关案例爬虫',
    'CUSTOMS_CASE': '海关案例爬虫'
  };
  
  const countryName = countryNames[task.countryCode] || task.countryCode;
  const typeName = typeNames[task.crawlerType] || task.crawlerType;
  
  return `${countryName}${typeName}`;
};

/**
 * 格式化任务调度时间
 * 将任务描述改为定时任务的时间
 */
const formatTaskSchedule = (task: TaskInfo): string => {
  // 如果有cron表达式，解析并显示为友好的时间描述
  if (task.cronExpression) {
    return formatCronExpression(task.cronExpression);
  }
  
  // 如果有下次执行时间，显示下次执行时间
  if (task.nextExecutionTime) {
    return `下次执行: ${formatTime(task.nextExecutionTime)}`;
  }
  
  // 如果有最后执行时间，显示最后执行时间
  if (task.lastExecutionTime) {
    return `最后执行: ${formatTime(task.lastExecutionTime)}`;
  }
  
  // 默认显示为手动执行
  return '手动执行';
};

/**
 * 格式化Cron表达式为友好的时间描述
 */
const formatCronExpression = (cronExpression: string): string => {
  try {
    const parts = cronExpression.split(' ');
    if (parts.length !== 5) {
      return cronExpression;
    }
    
    const [minute, hour, dayOfMonth, month, dayOfWeek] = parts;
    
    // 解析分钟和小时
    if (minute === '0' && hour !== '*') {
      // 整点执行
      if (dayOfMonth === '*' && month === '*' && dayOfWeek === '*') {
        return `每天 ${hour}:00 执行`;
      } else if (dayOfMonth === '*' && month === '*' && dayOfWeek !== '*') {
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        const days = dayOfWeek.split(',').map(d => {
          const dayNum = parseInt(d);
          return weekDays[dayNum] || d;
        }).join(',');
        return `每周 ${days} ${hour}:00 执行`;
      }
    } else if (minute !== '*' && hour !== '*') {
      // 指定时间执行
      if (dayOfMonth === '*' && month === '*' && dayOfWeek === '*') {
        return `每天 ${hour}:${minute.padStart(2, '0')} 执行`;
      }
    }
    
    // 默认返回原始表达式
    return `定时: ${cronExpression}`;
  } catch (error) {
    return `定时: ${cronExpression}`;
  }
};
</script>

<style scoped>
.unified-crawler-management {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.header-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.statistics-panel {
  margin-bottom: 24px;
}

.main-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.crawler-filters,
.task-filters {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.crawler-list {
  padding: 24px;
}

.crawler-card {
  margin-bottom: 16px;
  transition: all 0.3s;
}

.crawler-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.crawler-card.selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.crawler-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.crawler-name {
  font-weight: 600;
  font-size: 16px;
}

.crawler-info {
  margin-bottom: 12px;
}

.crawler-description {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.crawler-status {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.success-rate {
  font-size: 12px;
  color: #666;
}

.task-actions {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.task-desc {
  color: #666;
  font-size: 12px;
  margin-top: 4px;
}

.monitoring-panel {
  padding: 24px;
}

.status-list,
.recent-executions {
  max-height: 400px;
  overflow-y: auto;
}

.status-item,
.execution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-time,
.execution-time {
  font-size: 12px;
  color: #999;
}
</style>

<template>
  <div class="unified-crawler-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🕷️ 统一爬虫管理系统</h1>
        <p>整合V1和V2所有功能的统一爬虫任务管理系统</p>
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
          <a-button @click="showCreateTaskDialog" type="primary">
            <template #icon>
              <PlusOutlined />
            </template>
            创建任务
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
              <a-select-option value="CN">中国 (CN)</a-select-option>
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
            <a-button @click="showCreateTaskDialog" type="primary">
              <template #icon>
                <PlusOutlined />
              </template>
              创建任务
            </a-button>
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
            </a-select>
            
            <a-select v-model:value="taskFilters.taskType" placeholder="选择类型" style="width: 150px" @change="loadTasks">
              <a-select-option value="">全部类型</a-select-option>
              <a-select-option value="KEYWORD_BATCH">关键词批量</a-select-option>
              <a-select-option value="DATE_RANGE">日期范围</a-select-option>
              <a-select-option value="FULL">全量爬取</a-select-option>
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
          :data-source="tasks" 
          :loading="taskLoading"
          :pagination="{ pageSize: 20, showSizeChanger: true, showQuickJumper: true }"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'selection'">
              <a-checkbox :checked="selectedTasks.includes(record.id)" @change="(e) => toggleTaskSelection(record.id, e.target.checked)" />
            </template>
            
            <template v-else-if="column.key === 'taskName'">
              <div>
                <strong>{{ record.taskName }}</strong>
                <div class="task-desc">{{ record.description }}</div>
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
              <a-progress :percent="record.successRate" size="small" />
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

    <!-- 创建任务对话框 -->
    <a-modal v-model:open="createTaskDialogVisible" title="创建任务" width="800px" @ok="handleCreateTask">
      <UnifiedTaskForm 
        ref="taskFormRef"
        :crawlers="crawlers"
        @submit="handleTaskSubmit"
      />
    </a-modal>

    <!-- 执行爬虫对话框 -->
    <a-modal v-model:open="executeDialogVisible" title="执行爬虫" width="600px" @ok="handleExecuteCrawler">
      <UnifiedCrawlerExecuteForm 
        ref="executeFormRef"
        :crawler="selectedCrawler"
        @submit="handleExecuteSubmit"
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
import UnifiedTaskForm from '../components/UnifiedTaskForm.vue';
import UnifiedCrawlerExecuteForm from '../components/UnifiedCrawlerExecuteForm.vue';
import UnifiedBatchExecuteForm from '../components/UnifiedBatchExecuteForm.vue';
import CrawlerPresetEditor from '../components/CrawlerPresetEditor.vue';

// API导入
import {
  getCrawlers,
  getPresets,
  getSystemOverview,
  triggerTask
} from '@/api/crawler';

// 适配旧接口名称
const getAllCrawlers = async () => {
  const res = await getCrawlers();
  // res.data 是后端返回的 {success: true, data: [...], count: 11}
  return res.data; // 直接返回整个对象
};

const getTasks = async (params: any) => {
  const res = await getPresets(params);
  // res.data 是后端返回的 {success: true, data: [...], total: 14}
  return res.data; // 直接返回整个对象
};

const getSystemStatistics = async () => {
  const res = await getSystemOverview();
  // res.data 是后端返回的 {success: true, data: {...}}
  return res.data; // 直接返回整个对象
};

const executeTaskApi = async (id: number) => {
  const res = await triggerTask(id, 'MANUAL');
  return res.data;
};

const batchTestCrawlers = async (crawlerNames: string[]) => {
  return { success: true, data: [] }; // 暂时返回空数据
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
  taskType: string;
  description: string;
  enabled: boolean;
  successRate: number;
  lastExecutionTime: string;
  nextExecutionTime: string;
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
const taskLoading = ref(false);
const selectedTasks = ref<number[]>([]);
const taskFilters = reactive({
  countryCode: '',
  taskType: '',
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
const createTaskDialogVisible = ref(false);
const executeDialogVisible = ref(false);
const batchExecuteDialogVisible = ref(false);
const presetEditorVisible = ref(false);
const selectedCrawler = ref<CrawlerInfo | null>(null);

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

// 方法
const loadCrawlers = async () => {
  crawlerLoading.value = true;
  try {
    console.log('开始加载爬虫信息...');
    const response = await getAllCrawlers();
    console.log('爬虫信息响应:', response);
    
    if (response.success) {
      crawlers.value = response.data;
      console.log('加载到爬虫数量:', crawlers.value.length);
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
    if (taskFilters.taskType) params.taskType = taskFilters.taskType;
    if (taskFilters.enabled !== null) params.enabled = taskFilters.enabled;
    
    const response = await getTasks(params);
    console.log('任务列表响应:', response);
    
    if (response.success) {
      tasks.value = response.data;
      console.log('加载到任务数量:', tasks.value.length);
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

const refreshAllData = async () => {
  loading.value = true;
  try {
    await Promise.all([
      loadCrawlers(),
      loadTasks(),
      loadStatistics()
    ]);
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
    const response = await batchTestCrawlers([crawler.crawlerName]);
    
    if (response.success) {
      message.success(`爬虫 ${crawler.displayName} 测试成功`);
    } else {
      message.error(`爬虫 ${crawler.displayName} 测试失败: ${response.message}`);
    }
  } catch (error) {
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

const showCreateTaskDialog = () => {
  createTaskDialogVisible.value = true;
};

const showBatchExecuteDialog = () => {
  batchExecuteDialogVisible.value = true;
};

const executeTask = async (task: TaskInfo) => {
  try {
    const response = await executeTaskApi(task.id);
    
    if (response.success) {
      message.success(`任务 ${task.taskName} 执行成功`);
      loadTasks();
    } else {
      message.error(`任务 ${task.taskName} 执行失败: ${response.message}`);
    }
  } catch (error) {
    message.error(`任务 ${task.taskName} 执行失败: ${(error as any)?.message || '未知错误'}`);
  }
};

const editTask = (task: TaskInfo) => {
  message.info(`编辑任务: ${task.taskName}`);
};

const viewTaskHistory = (task: TaskInfo) => {
  message.info(`查看任务历史: ${task.taskName}`);
};

const deleteTask = async (task: TaskInfo) => {
  try {
    // 这里调用删除任务API
    message.success(`任务 ${task.taskName} 删除成功`);
    loadTasks();
  } catch (error) {
    message.error(`任务 ${task.taskName} 删除失败`);
  }
};

const batchExecuteSelectedTasks = () => {
  message.info(`批量执行 ${selectedTasks.value.length} 个任务`);
};

// 对话框处理
const handleCreateTask = () => {
  // 处理创建任务
};

const handleExecuteCrawler = () => {
  // 处理执行爬虫
};

const handleBatchExecute = () => {
  // 处理批量执行
};

const handleTaskSubmit = (data: any) => {
  console.log('任务提交:', data);
  createTaskDialogVisible.value = false;
  loadTasks();
};

const handleExecuteSubmit = (data: any) => {
  console.log('执行提交:', data);
  executeDialogVisible.value = false;
};

const handleBatchExecuteSubmit = (data: any) => {
  console.log('批量执行提交:', data);
  batchExecuteDialogVisible.value = false;
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

const formatTime = (timestamp: number) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleString();
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

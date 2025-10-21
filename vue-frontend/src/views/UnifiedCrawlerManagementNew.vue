<template>
  <div class="unified-crawler-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🕷️ 统一爬虫管理平台</h1>
        <p class="subtitle">智能化医疗器械数据爬取与调度系统</p>
      </div>
      <div class="header-actions">
        <a-space size="middle">
          <a-button type="primary" @click="refreshAllData" :loading="refreshing">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <a-button @click="showHistoryDrawer">
            <template #icon><HistoryOutlined /></template>
            执行历史
          </a-button>
          <a-dropdown>
            <template #overlay>
              <a-menu @click="handleBatchAction">
                <a-menu-item key="batchTest">
                  <ExperimentOutlined /> 批量测试选中
                </a-menu-item>
                <a-menu-item key="batchExecute">
                  <PlayCircleOutlined /> 批量执行选中
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="selectAll">
                  <CheckOutlined /> 全选
                </a-menu-item>
                <a-menu-item key="clearSelection">
                  <CloseOutlined /> 清空选择
                </a-menu-item>
              </a-menu>
            </template>
            <a-button>
              批量操作 <DownOutlined />
            </a-button>
          </a-dropdown>
        </a-space>
      </div>
    </div>

    <!-- 统计概览 -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="总爬虫数"
            :value="statistics.totalCrawlers"
            :prefix="h(RobotOutlined)"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="运行中任务"
            :value="statistics.runningTasks"
            :prefix="h(SyncOutlined)"
            :value-style="{ color: '#3f8600' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="今日执行次数"
            :value="statistics.todayExecutions"
            :prefix="h(ThunderboltOutlined)"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic
            title="成功率"
            :value="statistics.successRate"
            suffix="%"
            :prefix="h(TrophyOutlined)"
            :value-style="{ color: '#cf1322' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 国家分组标签 -->
    <a-tabs v-model:activeKey="activeCountry" class="country-tabs">
      <a-tab-pane key="ALL" tab="全部爬虫">
        <template #tab>
          <span><GlobalOutlined /> 全部 ({{ allCrawlers.length }})</span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="US" tab="美国">
        <template #tab>
          <span>🇺🇸 美国 ({{ usCrawlers.length }})</span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="EU" tab="欧盟">
        <template #tab>
          <span>🇪🇺 欧盟 ({{ euCrawlers.length }})</span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="KR" tab="韩国">
        <template #tab>
          <span>🇰🇷 韩国 ({{ krCrawlers.length }})</span>
        </template>
      </a-tab-pane>
      <a-tab-pane key="CN" tab="中国">
        <template #tab>
          <span>🇨🇳 中国 ({{ cnCrawlers.length }})</span>
        </template>
      </a-tab-pane>
    </a-tabs>

    <!-- 爬虫卡片列表 -->
    <a-spin :spinning="loading">
      <div class="crawler-cards-container">
        <a-empty v-if="filteredCrawlers.length === 0" description="暂无爬虫数据" />
        
        <div v-else class="crawler-cards">
          <div
            v-for="crawler in filteredCrawlers"
            :key="crawler.crawlerName"
            class="crawler-card"
            :class="{ 'card-disabled': !crawler.enabled, 'card-selected': crawler.selected }"
          >
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="header-left">
                <a-checkbox
                  v-model:checked="crawler.selected"
                  @change="onCrawlerSelect(crawler)"
                />
                <div class="crawler-info">
                  <h3 class="crawler-name">{{ crawler.crawlerName }}</h3>
                  <div class="crawler-tags">
                    <a-tag :color="getCountryColor(crawler.countryCode)">
                      {{ getCountryName(crawler.countryCode) }}
                    </a-tag>
                    <a-tag :color="getTypeColor(crawler.crawlerType)">
                      {{ crawler.crawlerType }}
                    </a-tag>
                    <a-tag v-if="crawler.version" color="default">
                      v{{ crawler.version }}
                    </a-tag>
                  </div>
                </div>
              </div>
              <a-switch
                v-model:checked="crawler.enabled"
                @change="toggleCrawler(crawler)"
                :loading="crawler.toggling"
              >
                <template #checkedChildren>启用</template>
                <template #unCheckedChildren>停用</template>
              </a-switch>
            </div>

            <!-- 爬虫描述 -->
            <p class="crawler-description">{{ crawler.description }}</p>

            <!-- 参数配置区域 -->
            <a-collapse v-model:activeKey="crawler.expandedPanels" ghost>
              <a-collapse-panel key="params" header="参数配置">
                <div class="params-config">
                  <!-- 字段关键词配置 -->
                  <div v-if="crawler.schemaFields && crawler.schemaFields.length > 0" class="field-keywords-section">
                    <div class="section-header">
                      <span class="section-title">🎯 字段关键词配置</span>
                      <a-tooltip title="每个字段独立配置关键词，执行时会遍历所有字段和关键词组合">
                        <QuestionCircleOutlined class="help-icon" />
                      </a-tooltip>
                    </div>
                    
                    <div v-for="field in crawler.schemaFields" :key="field.name" class="field-item">
                      <div class="field-label">
                        <span class="label-text">{{ field.label }}</span>
                        <span v-if="field.required" class="required-mark">*</span>
                        <a-tooltip v-if="field.description" :title="field.description">
                          <InfoCircleOutlined class="info-icon" />
                        </a-tooltip>
                        <a-button
                          type="link"
                          size="small"
                          @click="showBatchInputModal(crawler, field.name)"
                          style="margin-left: auto"
                        >
                          <template #icon><PlusCircleOutlined /></template>
                          批量输入
                        </a-button>
                      </div>
                      <a-select
                        v-model:value="crawler.fieldKeywords[field.name]"
                        mode="tags"
                        :placeholder="field.placeholder || `输入${field.label}，按回车添加`"
                        style="width: 100%"
                        :max-tag-count="3"
                      >
                        <template #tagRender="{ label, closable, onClose }">
                          <a-tag
                            :closable="closable"
                            @close="onClose"
                            color="processing"
                            style="margin: 2px"
                          >
                            {{ label }}
                          </a-tag>
                        </template>
                      </a-select>
                      <div v-if="crawler.fieldKeywords[field.name]?.length" class="keyword-badge">
                        <a-badge
                          :count="crawler.fieldKeywords[field.name].length"
                          :number-style="{ backgroundColor: '#52c41a' }"
                        >
                          <span class="badge-text">已配置关键词</span>
                        </a-badge>
                      </div>
                    </div>
                  </div>

                  <!-- 通用参数 -->
                  <a-divider orientation="left">
                    <SettingOutlined /> 通用参数
                  </a-divider>
                  
                  <a-row :gutter="[16, 16]">
                    <a-col :span="12">
                      <div class="param-item">
                        <label class="param-label">开始日期:</label>
                        <a-date-picker
                          v-model:value="crawler.params.dateFrom"
                          style="width: 100%"
                          format="YYYY-MM-DD"
                          placeholder="选择开始日期"
                        />
                      </div>
                    </a-col>
                    <a-col :span="12">
                      <div class="param-item">
                        <label class="param-label">结束日期:</label>
                        <a-date-picker
                          v-model:value="crawler.params.dateTo"
                          style="width: 100%"
                          format="YYYY-MM-DD"
                          placeholder="选择结束日期"
                        />
                      </div>
                    </a-col>
                    <a-col :span="12">
                      <div class="param-item">
                        <label class="param-label">最大记录数:</label>
                        <a-input-number
                          v-model:value="crawler.params.maxRecords"
                          :min="-1"
                          placeholder="-1表示全部"
                          style="width: 100%"
                        >
                          <template #addonAfter>
                            <a-tooltip title="-1表示爬取所有数据">
                              <InfoCircleOutlined />
                            </a-tooltip>
                          </template>
                        </a-input-number>
                      </div>
                    </a-col>
                    <a-col :span="12">
                      <div class="param-item">
                        <label class="param-label">批次大小:</label>
                        <a-input-number
                          v-model:value="crawler.params.batchSize"
                          :min="1"
                          :max="1000"
                          placeholder="批次保存大小"
                          style="width: 100%"
                        />
                      </div>
                    </a-col>
                  </a-row>
                </div>
              </a-collapse-panel>
            </a-collapse>

            <!-- 操作按钮区 -->
            <div class="card-actions">
              <a-space>
                <a-button
                  @click="savePreset(crawler)"
                  :loading="crawler.saving"
                  size="large"
                >
                  <template #icon><SaveOutlined /></template>
                  保存预设
                </a-button>
                <a-button
                  @click="testCrawler(crawler)"
                  :loading="crawler.testing"
                  size="large"
                >
                  <template #icon><ExperimentOutlined /></template>
                  测试 (10条)
                </a-button>
                <a-button
                  type="primary"
                  @click="executeCrawler(crawler)"
                  :loading="crawler.executing"
                  :disabled="!crawler.enabled"
                  size="large"
                >
                  <template #icon><PlayCircleOutlined /></template>
                  立即执行
                </a-button>
                <a-button
                  @click="viewCrawlerStats(crawler)"
                  size="large"
                >
                  <template #icon><LineChartOutlined /></template>
                  统计
                </a-button>
              </a-space>
            </div>

            <!-- 执行进度 -->
            <div v-if="crawler.executing || crawler.testing" class="execution-progress">
              <a-progress
                :percent="crawler.progress"
                :status="crawler.progressStatus"
                :stroke-color="{
                  '0%': '#108ee9',
                  '100%': '#87d068',
                }"
              />
              <div class="progress-info">
                <span class="progress-text">{{ crawler.statusText }}</span>
                <span class="progress-time">{{ crawler.executionTime }}s</span>
              </div>
            </div>

            <!-- 最近执行结果 -->
            <transition name="fade">
              <a-alert
                v-if="crawler.lastResult"
                :type="crawler.lastResult.success ? 'success' : 'error'"
                :message="crawler.lastResult.message"
                closable
                @close="crawler.lastResult = null"
                class="result-alert"
                show-icon
              >
                <template #description v-if="crawler.lastResult.success">
                  <div class="result-details">
                    <div class="result-item">
                      <CheckCircleOutlined class="icon-success" />
                      <span>保存: <strong>{{ crawler.lastResult.savedCount || 0 }}</strong> 条</span>
                    </div>
                    <div class="result-item">
                      <CloseCircleOutlined class="icon-skip" />
                      <span>跳过: <strong>{{ crawler.lastResult.skippedCount || 0 }}</strong> 条</span>
                    </div>
                    <div class="result-item">
                      <ClockCircleOutlined class="icon-time" />
                      <span>耗时: <strong>{{ crawler.lastResult.durationSeconds || 0 }}</strong> 秒</span>
                    </div>
                  </div>
                </template>
              </a-alert>
            </transition>

            <!-- 最后执行信息 -->
            <div v-if="crawler.lastExecution" class="last-execution">
              <span class="execution-label">最后执行:</span>
              <span class="execution-time">{{ formatDateTime(crawler.lastExecution.time) }}</span>
              <a-tag :color="crawler.lastExecution.success ? 'success' : 'error'">
                {{ crawler.lastExecution.success ? '成功' : '失败' }}
              </a-tag>
            </div>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 执行历史抽屉 -->
    <a-drawer
      v-model:open="historyDrawerVisible"
      title="执行历史记录"
      width="1000"
      placement="right"
    >
      <div class="history-container">
        <!-- 筛选条件 -->
        <div class="history-filters">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-select
                v-model:value="historyFilters.crawlerName"
                placeholder="选择爬虫"
                allowClear
                style="width: 100%"
              >
                <a-select-option value="">全部爬虫</a-select-option>
                <a-select-option
                  v-for="crawler in allCrawlers"
                  :key="crawler.crawlerName"
                  :value="crawler.crawlerName"
                >
                  {{ crawler.crawlerName }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="8">
              <a-select
                v-model:value="historyFilters.status"
                placeholder="执行状态"
                allowClear
                style="width: 100%"
              >
                <a-select-option value="">全部状态</a-select-option>
                <a-select-option value="SUCCESS">成功</a-select-option>
                <a-select-option value="FAILED">失败</a-select-option>
                <a-select-option value="RUNNING">运行中</a-select-option>
              </a-select>
            </a-col>
            <a-col :span="8">
              <a-button type="primary" @click="loadExecutionHistory" block>
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
            </a-col>
          </a-row>
        </div>

        <!-- 历史记录列表 -->
        <a-table
          :columns="historyColumns"
          :data-source="executionHistory"
          :loading="historyLoading"
          :pagination="historyPagination"
          @change="handleHistoryTableChange"
          size="small"
          class="history-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'crawlerName'">
              <a-tag :color="getCountryColor(record.countryCode)">
                {{ record.crawlerName }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'result'">
              <div class="result-summary">
                <span class="result-text">保存: {{ record.savedCount || 0 }}</span>
                <a-divider type="vertical" />
                <span class="result-text">跳过: {{ record.skippedCount || 0 }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'duration'">
              <span>{{ record.durationSeconds || 0 }}s</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" size="small" @click="viewHistoryDetail(record)">
                详情
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </a-drawer>

    <!-- 统计详情模态框 -->
    <a-modal
      v-model:open="statsModalVisible"
      :title="`${currentCrawler?.crawlerName} - 统计信息`"
      width="800"
      :footer="null"
    >
      <div v-if="crawlerStats" class="crawler-stats">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic
              title="总执行次数"
              :value="crawlerStats.totalExecutions"
              :prefix="h(ThunderboltOutlined)"
            />
          </a-col>
          <a-col :span="8">
            <a-statistic
              title="成功次数"
              :value="crawlerStats.successCount"
              :value-style="{ color: '#3f8600' }"
              :prefix="h(CheckCircleOutlined)"
            />
          </a-col>
          <a-col :span="8">
            <a-statistic
              title="失败次数"
              :value="crawlerStats.failureCount"
              :value-style="{ color: '#cf1322' }"
              :prefix="h(CloseCircleOutlined)"
            />
          </a-col>
        </a-row>
        <a-divider />
        <a-row :gutter="16">
          <a-col :span="12">
            <a-statistic
              title="累计爬取数据"
              :value="crawlerStats.totalCrawled"
              suffix="条"
            />
          </a-col>
          <a-col :span="12">
            <a-statistic
              title="累计保存数据"
              :value="crawlerStats.totalSaved"
              suffix="条"
            />
          </a-col>
        </a-row>
      </div>
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
            <p style="margin: 8px 0 0 0">示例：<code>Skin, Analyzer, 3D, AI, Facial</code></p>
          </template>
        </a-alert>
        
        <div class="input-section">
          <label class="input-label">粘贴关键词（支持批量）:</label>
          <a-textarea
            v-model:value="batchInputText"
            :rows="8"
            placeholder="请输入关键词，支持逗号、分号、换行分隔
示例：
Skin, Analyzer, 3D, AI, Facial, Detector, Scanner, Spectra, Skin Analysis, Skin Scanner, skin imaging, Facial Imaging, pigmentation, skin elasticity"
            style="font-family: monospace"
          />
        </div>

        <a-divider>解析预览</a-divider>

        <div class="preview-section">
          <div class="preview-header">
            <span class="preview-title">将解析为 {{ parsedKeywords.length }} 个关键词：</span>
            <a-button type="link" size="small" @click="clearBatchInput">
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
            <a-empty v-if="parsedKeywords.length === 0" :image="false" description="暂无关键词" style="margin: 20px 0" />
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue';
import { message, notification } from 'ant-design-vue';
import {
  ReloadOutlined,
  HistoryOutlined,
  ExperimentOutlined,
  PlayCircleOutlined,
  DownOutlined,
  CheckOutlined,
  CloseOutlined,
  RobotOutlined,
  SyncOutlined,
  ThunderboltOutlined,
  TrophyOutlined,
  GlobalOutlined,
  SaveOutlined,
  LineChartOutlined,
  QuestionCircleOutlined,
  InfoCircleOutlined,
  SettingOutlined,
  SearchOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  PlusCircleOutlined,
} from '@ant-design/icons-vue';
import {
  getCrawlers,
  getSystemOverview,
  saveCrawlerPreset,
  testCrawler as apiTestCrawler,
  executeCrawler as apiExecuteCrawler,
  enableCrawler,
  disableCrawler,
  getExecutionHistory,
  batchTestCrawlers,
  batchExecuteCrawlers,
} from '@/api/crawler';
import dayjs from 'dayjs';

// ==================== 数据定义 ====================

interface CrawlerInfo {
  crawlerName: string;
  countryCode: string;
  crawlerType: string;
  description: string;
  version?: string;
  enabled: boolean;
  available: boolean;
  selected: boolean;
  schemaFields?: any[];
  fieldKeywords: Record<string, string[]>;
  params: {
    dateFrom: any;
    dateTo: any;
    maxRecords: number;
    batchSize: number;
  };
  expandedPanels: string[];
  executing: boolean;
  testing: boolean;
  saving: boolean;
  toggling: boolean;
  progress: number;
  progressStatus: string;
  statusText: string;
  executionTime: number;
  lastResult: any;
  lastExecution: any;
}

interface Statistics {
  totalCrawlers: number;
  runningTasks: number;
  todayExecutions: number;
  successRate: number;
}

// ==================== 响应式数据 ====================

const loading = ref(false);
const refreshing = ref(false);
const activeCountry = ref('ALL');
const allCrawlers = ref<CrawlerInfo[]>([]);
const historyDrawerVisible = ref(false);
const statsModalVisible = ref(false);
const currentCrawler = ref<CrawlerInfo | null>(null);
const crawlerStats = ref<any>(null);

const statistics = ref<Statistics>({
  totalCrawlers: 0,
  runningTasks: 0,
  todayExecutions: 0,
  successRate: 0,
});

// 执行历史相关
const executionHistory = ref<any[]>([]);
const historyLoading = ref(false);
const historyFilters = ref({
  crawlerName: '',
  status: '',
});
const historyPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
});

// 批量输入相关
const batchInputModalVisible = ref(false);
const batchInputText = ref('');
const batchInputCrawler = ref<CrawlerInfo | null>(null);
const batchInputFieldName = ref('');
const parsedKeywords = computed(() => {
  return parseKeywordString(batchInputText.value);
});

const historyColumns = [
  { title: '爬虫名称', dataIndex: 'crawlerName', key: 'crawlerName', width: 150 },
  { title: '执行状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '执行结果', key: 'result', width: 150 },
  { title: '耗时', key: 'duration', width: 80 },
  { title: '触发方式', dataIndex: 'triggeredBy', key: 'triggeredBy', width: 100 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' },
];

// ==================== 计算属性 ====================

const filteredCrawlers = computed(() => {
  if (activeCountry.value === 'ALL') {
    return allCrawlers.value;
  }
  return allCrawlers.value.filter(c => c.countryCode === activeCountry.value);
});

const usCrawlers = computed(() => 
  allCrawlers.value.filter(c => c.countryCode === 'US')
);

const euCrawlers = computed(() => 
  allCrawlers.value.filter(c => c.countryCode === 'EU')
);

const krCrawlers = computed(() => 
  allCrawlers.value.filter(c => c.countryCode === 'KR')
);

const cnCrawlers = computed(() => 
  allCrawlers.value.filter(c => c.countryCode === 'CN')
);

const selectedCrawlers = computed(() => 
  allCrawlers.value.filter(c => c.selected)
);

// ==================== 生命周期 ====================

onMounted(() => {
  loadAllData();
  startAutoRefresh();
});

// ==================== 核心方法 ====================

/**
 * 加载所有数据
 */
const loadAllData = async () => {
  loading.value = true;
  try {
    await Promise.all([
      loadCrawlers(),
      loadStatistics(),
    ]);
  } catch (error: any) {
    console.error('加载数据失败:', error);
    message.error('加载数据失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

/**
 * 加载爬虫列表
 */
const loadCrawlers = async () => {
  try {
    const response = await getCrawlers();
    if (response.data.success) {
      const crawlersData = response.data.data || [];
      
      allCrawlers.value = crawlersData.map((c: any) => ({
        crawlerName: c.crawlerName,
        countryCode: c.countryCode,
        crawlerType: c.crawlerType,
        description: c.description,
        version: c.version,
        enabled: c.enabled !== false,
        available: c.available !== false,
        selected: false,
        schemaFields: c.schemaFields || [],
        fieldKeywords: initFieldKeywords(c.schemaFields),
        params: {
          dateFrom: null,
          dateTo: null,
          maxRecords: -1,
          batchSize: 100,
        },
        expandedPanels: [],
        executing: false,
        testing: false,
        saving: false,
        toggling: false,
        progress: 0,
        progressStatus: 'active',
        statusText: '',
        executionTime: 0,
        lastResult: null,
        lastExecution: null,
      }));
      
      console.log(`成功加载 ${allCrawlers.value.length} 个爬虫`);
    }
  } catch (error: any) {
    console.error('加载爬虫列表失败:', error);
    throw error;
  }
};

/**
 * 初始化字段关键词
 */
const initFieldKeywords = (schemaFields: any[]) => {
  const keywords: Record<string, string[]> = {};
  if (schemaFields && schemaFields.length > 0) {
    schemaFields.forEach(field => {
      keywords[field.name] = [];
    });
  }
  return keywords;
};

/**
 * 加载统计信息
 */
const loadStatistics = async () => {
  try {
    const response = await getSystemOverview();
    if (response.data.success) {
      const data = response.data.data || {};
      statistics.value = {
        totalCrawlers: data.totalCrawlers || 0,
        runningTasks: data.runningTasks || 0,
        todayExecutions: data.todayExecutions || 0,
        successRate: data.successRate || 0,
      };
    }
  } catch (error: any) {
    console.error('加载统计信息失败:', error);
  }
};

/**
 * 刷新所有数据
 */
const refreshAllData = async () => {
  refreshing.value = true;
  try {
    await loadAllData();
    message.success('刷新成功');
  } finally {
    refreshing.value = false;
  }
};

/**
 * 保存预设
 */
const savePreset = async (crawler: CrawlerInfo) => {
  crawler.saving = true;
  try {
    // 构建预设数据
    const presetData: any = {
      fieldKeywords: {},
      maxRecords: crawler.params.maxRecords,
      batchSize: crawler.params.batchSize,
    };
    
    // 添加字段关键词
    Object.keys(crawler.fieldKeywords).forEach(field => {
      if (crawler.fieldKeywords[field] && crawler.fieldKeywords[field].length > 0) {
        presetData.fieldKeywords[field] = crawler.fieldKeywords[field];
      }
    });
    
    // 添加日期参数
    if (crawler.params.dateFrom) {
      presetData.dateFrom = dayjs(crawler.params.dateFrom).format('YYYYMMDD');
    }
    if (crawler.params.dateTo) {
      presetData.dateTo = dayjs(crawler.params.dateTo).format('YYYYMMDD');
    }
    
    const response = await saveCrawlerPreset(crawler.crawlerName, presetData);
    
    if (response.data.success) {
      message.success('预设保存成功');
    } else {
      message.error('预设保存失败: ' + response.data.message);
    }
  } catch (error: any) {
    console.error('保存预设失败:', error);
    message.error('保存预设失败: ' + error.message);
  } finally {
    crawler.saving = false;
  }
};

/**
 * 测试爬虫
 */
const testCrawler = async (crawler: CrawlerInfo) => {
  crawler.testing = true;
  crawler.progress = 0;
  crawler.progressStatus = 'active';
  crawler.statusText = '正在测试...';
  crawler.executionTime = 0;
  
  const startTime = Date.now();
  const progressTimer = setInterval(() => {
    crawler.executionTime = Math.floor((Date.now() - startTime) / 1000);
    if (crawler.progress < 90) {
      crawler.progress += 5;
    }
  }, 500);
  
  try {
    const params = {
      maxRecords: 10,
      mode: 'test',
    };
    
    const response = await apiTestCrawler(crawler.crawlerName, params);
    
    clearInterval(progressTimer);
    crawler.progress = 100;
    crawler.progressStatus = response.data.success ? 'success' : 'exception';
    
    const result = response.data.data || {};
    crawler.lastResult = {
      success: response.data.success,
      message: response.data.message,
      savedCount: result.savedCount || 0,
      skippedCount: result.skippedCount || 0,
      durationSeconds: result.durationSeconds || crawler.executionTime,
    };
    
    crawler.lastExecution = {
      time: new Date(),
      success: response.data.success,
    };
    
    if (response.data.success) {
      notification.success({
        message: '测试完成',
        description: `${crawler.crawlerName} 测试成功`,
        duration: 3,
      });
    } else {
      notification.error({
        message: '测试失败',
        description: response.data.message,
        duration: 5,
      });
    }
  } catch (error: any) {
    clearInterval(progressTimer);
    crawler.progress = 100;
    crawler.progressStatus = 'exception';
    crawler.lastResult = {
      success: false,
      message: '测试失败: ' + error.message,
    };
    message.error('测试失败: ' + error.message);
  } finally {
    crawler.testing = false;
    setTimeout(() => {
      crawler.progress = 0;
      crawler.statusText = '';
    }, 2000);
  }
};

/**
 * 执行爬虫
 */
const executeCrawler = async (crawler: CrawlerInfo) => {
  if (!crawler.enabled) {
    message.warning('该爬虫已停用，请先启用');
    return;
  }
  
  crawler.executing = true;
  crawler.progress = 0;
  crawler.progressStatus = 'active';
  crawler.statusText = '正在执行...';
  crawler.executionTime = 0;
  
  const startTime = Date.now();
  const progressTimer = setInterval(() => {
    crawler.executionTime = Math.floor((Date.now() - startTime) / 1000);
    if (crawler.progress < 90) {
      crawler.progress += 3;
    }
  }, 1000);
  
  try {
    // 构建执行参数
    const params: any = {
      fieldKeywords: {},
      maxRecords: crawler.params.maxRecords,
      batchSize: crawler.params.batchSize,
    };
    
    // 添加字段关键词
    Object.keys(crawler.fieldKeywords).forEach(field => {
      if (crawler.fieldKeywords[field] && crawler.fieldKeywords[field].length > 0) {
        params.fieldKeywords[field] = crawler.fieldKeywords[field];
      }
    });
    
    // 添加日期参数
    if (crawler.params.dateFrom) {
      params.dateFrom = dayjs(crawler.params.dateFrom).format('YYYYMMDD');
    }
    if (crawler.params.dateTo) {
      params.dateTo = dayjs(crawler.params.dateTo).format('YYYYMMDD');
    }
    
    const response = await apiExecuteCrawler(crawler.crawlerName, params);
    
    clearInterval(progressTimer);
    crawler.progress = 100;
    crawler.progressStatus = response.data.success ? 'success' : 'exception';
    
    const result = response.data.data || {};
    crawler.lastResult = {
      success: response.data.success,
      message: response.data.message,
      savedCount: result.savedCount || 0,
      skippedCount: result.skippedCount || 0,
      failedCount: result.failedCount || 0,
      durationSeconds: result.durationSeconds || crawler.executionTime,
    };
    
    crawler.lastExecution = {
      time: new Date(),
      success: response.data.success,
    };
    
    if (response.data.success) {
      notification.success({
        message: '执行完成',
        description: `${crawler.crawlerName} 执行成功，保存 ${result.savedCount || 0} 条数据`,
        duration: 5,
      });
      // 刷新统计信息
      loadStatistics();
    } else {
      notification.error({
        message: '执行失败',
        description: response.data.message,
        duration: 5,
      });
    }
  } catch (error: any) {
    clearInterval(progressTimer);
    crawler.progress = 100;
    crawler.progressStatus = 'exception';
    crawler.lastResult = {
      success: false,
      message: '执行失败: ' + error.message,
    };
    message.error('执行失败: ' + error.message);
  } finally {
    crawler.executing = false;
    setTimeout(() => {
      crawler.progress = 0;
      crawler.statusText = '';
    }, 3000);
  }
};

/**
 * 切换爬虫启用状态
 */
const toggleCrawler = async (crawler: CrawlerInfo) => {
  crawler.toggling = true;
  try {
    const apiCall = crawler.enabled ? enableCrawler : disableCrawler;
    const response = await apiCall(crawler.crawlerName);
    
    if (response.data.success) {
      message.success(`爬虫已${crawler.enabled ? '启用' : '停用'}`);
    } else {
      // 回滚状态
      crawler.enabled = !crawler.enabled;
      message.error(response.data.message);
    }
  } catch (error: any) {
    // 回滚状态
    crawler.enabled = !crawler.enabled;
    message.error('操作失败: ' + error.message);
  } finally {
    crawler.toggling = false;
  }
};

/**
 * 爬虫选择
 */
const onCrawlerSelect = (crawler: CrawlerInfo) => {
  console.log('选中爬虫:', crawler.crawlerName, crawler.selected);
};

/**
 * 批量操作
 */
const handleBatchAction = async ({ key }: { key: string }) => {
  const selected = selectedCrawlers.value;
  
  if (selected.length === 0) {
    message.warning('请先选择要操作的爬虫');
    return;
  }
  
  switch (key) {
    case 'batchTest':
      await batchTest(selected);
      break;
    case 'batchExecute':
      await batchExecute(selected);
      break;
    case 'selectAll':
      allCrawlers.value.forEach(c => c.selected = true);
      break;
    case 'clearSelection':
      allCrawlers.value.forEach(c => c.selected = false);
      break;
  }
};

/**
 * 批量测试
 */
const batchTest = async (crawlers: CrawlerInfo[]) => {
  const hide = message.loading(`正在批量测试 ${crawlers.length} 个爬虫...`, 0);
  
  try {
    const crawlerNames = crawlers.map(c => c.crawlerName);
    const response = await batchTestCrawlers(crawlerNames);
    
    hide();
    
    if (response.data.success) {
      const results = response.data.data || [];
      const successCount = results.filter((r: any) => r.success).length;
      
      notification.success({
        message: '批量测试完成',
        description: `成功: ${successCount}/${results.length}`,
        duration: 5,
      });
      
      // 刷新数据
      await loadAllData();
    } else {
      message.error('批量测试失败: ' + response.data.message);
    }
  } catch (error: any) {
    hide();
    message.error('批量测试失败: ' + error.message);
  }
};

/**
 * 批量执行
 */
const batchExecute = async (crawlers: CrawlerInfo[]) => {
  const hide = message.loading(`正在批量执行 ${crawlers.length} 个爬虫...`, 0);
  
  try {
    const crawlerNames = crawlers.map(c => c.crawlerName);
    const response = await batchExecuteCrawlers({
      crawlers: crawlerNames,
      mode: 'full',
    });
    
    hide();
    
    if (response.data.success) {
      const results = response.data.data || [];
      const successCount = results.filter((r: any) => r.success).length;
      
      notification.success({
        message: '批量执行完成',
        description: `成功: ${successCount}/${results.length}`,
        duration: 5,
      });
      
      // 刷新数据
      await loadAllData();
    } else {
      message.error('批量执行失败: ' + response.data.message);
    }
  } catch (error: any) {
    hide();
    message.error('批量执行失败: ' + error.message);
  }
};

/**
 * 显示执行历史
 */
const showHistoryDrawer = () => {
  historyDrawerVisible.value = true;
  loadExecutionHistory();
};

/**
 * 加载执行历史
 */
const loadExecutionHistory = async () => {
  historyLoading.value = true;
  try {
    const params = {
      crawlerName: historyFilters.value.crawlerName || undefined,
      status: historyFilters.value.status || undefined,
      page: historyPagination.value.current - 1,
      size: historyPagination.value.pageSize,
    };
    
    const response = await getExecutionHistory(params);
    
    if (response.data.success) {
      executionHistory.value = response.data.data || [];
      historyPagination.value.total = response.data.total || 0;
    }
  } catch (error: any) {
    console.error('加载执行历史失败:', error);
    message.error('加载执行历史失败');
  } finally {
    historyLoading.value = false;
  }
};

/**
 * 历史记录表格变化
 */
const handleHistoryTableChange = (pagination: any) => {
  historyPagination.value.current = pagination.current;
  historyPagination.value.pageSize = pagination.pageSize;
  loadExecutionHistory();
};

/**
 * 查看历史详情
 */
const viewHistoryDetail = (record: any) => {
  notification.info({
    message: '执行详情',
    description: record.resultMessage || '暂无详细信息',
    duration: 10,
  });
};

/**
 * 查看爬虫统计
 */
const viewCrawlerStats = (crawler: CrawlerInfo) => {
  currentCrawler.value = crawler;
  
  // 模拟统计数据（实际应该从API获取）
  crawlerStats.value = {
    totalExecutions: Math.floor(Math.random() * 100),
    successCount: Math.floor(Math.random() * 80),
    failureCount: Math.floor(Math.random() * 20),
    totalCrawled: Math.floor(Math.random() * 10000),
    totalSaved: Math.floor(Math.random() * 8000),
  };
  
  statsModalVisible.value = true;
};

/**
 * 自动刷新
 */
const startAutoRefresh = () => {
  setInterval(() => {
    loadStatistics();
  }, 30000); // 每30秒刷新一次统计
};

// ==================== 工具方法 ====================

/**
 * 获取国家颜色
 */
const getCountryColor = (countryCode: string) => {
  const colors: Record<string, string> = {
    US: 'blue',
    EU: 'green',
    KR: 'orange',
    CN: 'red',
  };
  return colors[countryCode] || 'default';
};

/**
 * 获取国家名称
 */
const getCountryName = (countryCode: string) => {
  const names: Record<string, string> = {
    US: '美国',
    EU: '欧盟',
    KR: '韩国',
    CN: '中国',
  };
  return names[countryCode] || countryCode;
};

/**
 * 获取类型颜色
 */
const getTypeColor = (crawlerType: string) => {
  const colors: Record<string, string> = {
    '510K': 'cyan',
    'RECALL': 'volcano',
    'EVENT': 'magenta',
    'REGISTRATION': 'purple',
    'GUIDANCE': 'geekblue',
    'CUSTOMS': 'gold',
  };
  return colors[crawlerType] || 'default';
};

/**
 * 获取状态颜色
 */
const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    SUCCESS: 'success',
    FAILED: 'error',
    RUNNING: 'processing',
  };
  return colors[status] || 'default';
};

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    SUCCESS: '成功',
    FAILED: '失败',
    RUNNING: '运行中',
  };
  return texts[status] || status;
};

/**
 * 格式化日期时间
 */
const formatDateTime = (date: Date | string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
};

// ==================== 批量输入方法 ====================

/**
 * 显示批量输入模态框
 */
const showBatchInputModal = (crawler: CrawlerInfo, fieldName: string) => {
  batchInputCrawler.value = crawler;
  batchInputFieldName.value = fieldName;
  
  // 如果字段已有关键词，预填充到文本框
  const existingKeywords = crawler.fieldKeywords[fieldName] || [];
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
  if (!batchInputCrawler.value || !batchInputFieldName.value) {
    message.warning('无效的操作');
    return;
  }
  
  const keywords = parseKeywordString(batchInputText.value);
  
  if (keywords.length === 0) {
    message.warning('请输入至少一个关键词');
    return;
  }
  
  // 更新爬虫的字段关键词
  batchInputCrawler.value.fieldKeywords[batchInputFieldName.value] = keywords;
  
  message.success(`成功添加 ${keywords.length} 个关键词`);
  
  // 关闭模态框
  batchInputModalVisible.value = false;
  batchInputText.value = '';
  batchInputCrawler.value = null;
  batchInputFieldName.value = '';
};

/**
 * 取消批量输入
 */
const handleBatchInputCancel = () => {
  batchInputModalVisible.value = false;
  batchInputText.value = '';
  batchInputCrawler.value = null;
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
</script>

<style scoped>
/* ==================== 主容器 ==================== */
.unified-crawler-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

/* ==================== 页面头部 ==================== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: white;
}

.header-content .subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

/* ==================== 统计卡片 ==================== */
.statistics-row {
  margin-bottom: 24px;
}

.statistics-row .stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.statistics-row .stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* ==================== 国家标签页 ==================== */
.country-tabs {
  background: white;
  padding: 16px 16px 0;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* ==================== 爬虫卡片容器 ==================== */
.crawler-cards-container {
  min-height: 400px;
}

.crawler-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
  padding: 4px;
}

/* ==================== 爬虫卡片 ==================== */
.crawler-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.crawler-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.crawler-card.card-selected {
  border-color: #1890ff;
  background: #f0f8ff;
}

.crawler-card.card-disabled {
  opacity: 0.6;
  background: #fafafa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header .header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.card-header .crawler-info {
  flex: 1;
}

.crawler-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.crawler-tags {
  margin-top: 8px;
  display: flex;
  gap: 6px;
}

.crawler-description {
  color: #8c8c8c;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

/* ==================== 参数配置 ==================== */
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title {
  font-weight: 600;
  font-size: 15px;
  color: #262626;
}

.help-icon {
  color: #8c8c8c;
  cursor: help;
}

.field-keywords-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.field-item {
  margin-bottom: 16px;
}

.field-item:last-child {
  margin-bottom: 0;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  justify-content: space-between;
}

.label-text {
  font-weight: 500;
  color: #595959;
}

.required-mark {
  color: #ff4d4f;
  font-weight: bold;
}

.info-icon {
  color: #8c8c8c;
  cursor: help;
}

.keyword-badge {
  margin-top: 8px;
}

.badge-text {
  font-size: 12px;
  color: #8c8c8c;
}

.param-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #595959;
}

/* ==================== 操作按钮 ==================== */
.card-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* ==================== 执行进度 ==================== */
.execution-progress {
  margin-top: 16px;
  padding: 12px;
  background: #f6f6f6;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 13px;
}

.progress-text {
  color: #595959;
}

.progress-time {
  color: #8c8c8c;
  font-weight: 600;
}

/* ==================== 执行结果 ==================== */
.result-alert {
  margin-top: 16px;
  border-radius: 8px;
}

.result-details {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.icon-success {
  color: #52c41a;
}

.icon-skip {
  color: #faad14;
}

.icon-time {
  color: #1890ff;
}

.result-item strong {
  font-weight: 600;
  color: #262626;
}

/* ==================== 最后执行信息 ==================== */
.last-execution {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.execution-label {
  color: #8c8c8c;
}

.execution-time {
  color: #595959;
}

/* ==================== 执行历史 ==================== */
.history-filters {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.history-table ::v-deep(.ant-table) {
  font-size: 13px;
}

.result-summary {
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-text {
  font-size: 12px;
}

/* ==================== 统计信息 ==================== */
.crawler-stats {
  padding: 16px 0;
}

/* ==================== 批量输入 ==================== */
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

/* ==================== 动画效果 ==================== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ==================== 响应式布局 ==================== */
@media (max-width: 1400px) {
  .crawler-cards {
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  }
}

@media (max-width: 768px) {
  .crawler-cards {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .statistics-row ::v-deep(.ant-col) {
    margin-bottom: 12px;
  }
}
</style>


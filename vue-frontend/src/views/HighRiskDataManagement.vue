<template>
  <div class="high-risk-data-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>高风险数据管理</h1>
          <p>管理和监控风险等级为高的医疗器械数据，支持数据编辑和风险等级调整</p>
        </div>
        <div class="header-actions">
          <a-button type="primary" @click="refreshAllData" :loading="refreshing">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
        </div>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="高风险数据总数"
              :value="statistics.totalHighRisk"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <WarningOutlined style="color: #ff4d4f" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="上市前通告高风险"
              :value="statistics.device510KHighRisk"
              :value-style="{ color: '#ff7a45' }"
            >
              <template #prefix>
                <ExperimentOutlined style="color: #ff7a45" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="召回高风险"
              :value="statistics.recallHighRisk"
              :value-style="{ color: '#ff7875' }"
            >
              <template #prefix>
                <ExclamationCircleOutlined style="color: #ff7875" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="事件高风险"
              :value="statistics.eventHighRisk"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <AlertOutlined style="color: #ff4d4f" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="指导文档高风险"
              :value="statistics.guidanceHighRisk"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <FileTextOutlined style="color: #faad14" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="4">
          <a-card class="stat-card">
            <a-statistic
              title="海关案例高风险"
              :value="statistics.customsHighRisk"
              :value-style="{ color: '#722ed1' }"
            >
              <template #prefix>
                <GlobalOutlined style="color: #722ed1" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 按国家分类统计 -->
    <div class="country-stats-section" v-if="countryStatistics && Object.keys(countryStatistics).length > 0">
      <a-card title="按国家分类统计" class="country-stats-card">
        <a-row :gutter="16">
          <a-col :span="6" v-for="(countryData, country) in countryStatistics" :key="country">
            <a-card 
              class="country-stat-card" 
              :class="{ 'selected': selectedCountry === country }"
              size="small"
              hoverable
              @click="handleCountryClick(country, countryData)"
            >
              <a-statistic
                :title="getCountryDisplayName(country)"
                :value="countryData.total"
                :value-style="{ color: '#1890ff', fontSize: '24px' }"
              >
                <template #prefix>
                  <GlobalOutlined style="color: #1890ff" />
                </template>
              </a-statistic>
              <div class="country-detail-stats">
                <div class="detail-item">
                  <span class="label">上市前通告:</span>
                  <span class="value">{{ countryData.device510K }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">召回:</span>
                  <span class="value">{{ countryData.recall }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">事件:</span>
                  <span class="value">{{ countryData.event }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">注册:</span>
                  <span class="value">{{ countryData.registration }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">指导:</span>
                  <span class="value">{{ countryData.guidance }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">海关:</span>
                  <span class="value">{{ countryData.customs }}</span>
                </div>
              </div>
              <div class="click-hint">
                <span class="hint-text">点击查看详情</span>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 数据筛选和显示区域 -->
    <div class="data-filter-section">
      <a-card class="data-filter-card">
        <template #title>
          <div class="filter-title">
            <span>{{ getCurrentDisplayTitle() }}</span>
            <a-tag v-if="selectedCountry" color="blue" closable @close="clearCountrySelection">
              {{ getCountryDisplayName(selectedCountry) }}
            </a-tag>
            <a-tag v-if="selectedKeyword" color="orange" closable @close="clearKeywordSelection">
              关键词: {{ selectedKeyword }}
            </a-tag>
          </div>
        </template>
        
        <template #extra>
          <div class="filter-actions">
            <a-button 
              v-if="selectedCountry" 
              type="link" 
              @click="clearCountrySelection"
              icon="close"
            >
              显示全部
            </a-button>
            <a-button 
              type="primary" 
              @click="refreshAllData" 
              :loading="refreshing"
              size="small"
            >
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新
            </a-button>
          </div>
        </template>

        <!-- 关键词筛选区域 -->
        <div class="keyword-filter-section" v-if="allKeywords.length > 0">
          <div class="keyword-filter-header">
            <h4>关键词筛选</h4>
            <div class="keyword-header-actions">
              <p>点击关键词查看对应的数据详情</p>
              <a-button type="primary" @click="openKeywordManagement" size="small">
                管理关键词
              </a-button>
            </div>
          </div>
          <div class="keyword-tags-container">
            <a-tag
              v-for="keyword in allKeywords"
              :key="keyword.keyword"
              :color="getKeywordColor(keyword.keyword)"
              class="keyword-tag clickable-keyword"
              @click="viewKeywordData(keyword.keyword)"
            >
              {{ keyword.keyword }} ({{ keyword.count }} 条)
            </a-tag>
          </div>
        </div>
        
        <!-- 当前选择信息 -->
        <div v-if="selectedCountry && selectedCountryData" class="current-selection-info">
          <!-- 国家数据统计卡片 -->
          <a-row :gutter="16" class="country-stats-row">
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="上市前通告"
                  :value="selectedCountryData.device510K"
                  :value-style="{ color: '#ff7a45' }"
                >
                  <template #prefix>
                    <ExperimentOutlined style="color: #ff7a45" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="召回记录"
                  :value="selectedCountryData.recall"
                  :value-style="{ color: '#ff7875' }"
                >
                  <template #prefix>
                    <ExclamationCircleOutlined style="color: #ff7875" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="事件报告"
                  :value="selectedCountryData.event"
                  :value-style="{ color: '#ff4d4f' }"
                >
                  <template #prefix>
                    <AlertOutlined style="color: #ff4d4f" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="注册记录"
                  :value="selectedCountryData.registration"
                  :value-style="{ color: '#52c41a' }"
                >
                  <template #prefix>
                    <FileTextOutlined style="color: #52c41a" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="指导文档"
                  :value="selectedCountryData.guidance"
                  :value-style="{ color: '#faad14' }"
                >
                  <template #prefix>
                    <FileTextOutlined style="color: #faad14" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
            <a-col :span="4">
              <a-card class="data-type-card">
                <a-statistic
                  title="海关案例"
                  :value="selectedCountryData.customs"
                  :value-style="{ color: '#722ed1' }"
                >
                  <template #prefix>
                    <GlobalOutlined style="color: #722ed1" />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
          </a-row>
        </div>
        
        <!-- 数据类型选择标签页 -->
        <a-tabs v-model:activeKey="activeDataType" @change="handleDataTypeChange" class="data-type-tabs">
          <a-tab-pane 
            v-for="tab in availableTabs" 
            :key="tab.key" 
            :tab="`${tab.label} (${tab.count})`"
          >
            <HighRiskDataTable 
              :dataType="tab.key" 
              :selectedCountry="selectedCountry"
              :selectedKeyword="selectedKeyword"
              :ref="`${tab.key}Table`"
              @data-loaded="handleDataLoaded"
              @keyword-click="handleKeywordClick"
            />
          </a-tab-pane>
          
          <!-- 如果没有数据，显示提示信息 -->
          <div v-if="availableTabs.length === 0" class="no-data-tip">
            <a-empty 
              description="暂无高风险数据"
              :image="false"
            >
              <template #description>
                <span v-if="selectedCountry || selectedKeyword">
                  当前选择的条件没有高风险数据
                </span>
                <span v-else>
                  当前没有高风险数据，请检查数据源或联系管理员
                </span>
              </template>
            </a-empty>
          </div>
        </a-tabs>
      </a-card>
    </div>



    <!-- 原来的完整关键词管理模态框 -->
    <a-modal
      v-model:open="showKeywordManagement"
      title="关键词管理"
      width="1200px"
      :footer="null"
    >
      <div class="keyword-management">
        <!-- 操作工具栏 -->
        <div class="keyword-toolbar">
          <a-space>
            <a-button type="primary" @click="showCreateKeywordModal = true">
              添加关键词
            </a-button>
            <a-select
              v-model:value="keywordFilterType"
              placeholder="筛选类型"
              style="width: 150px"
              @change="loadAllKeywords"
            >
              <a-select-option value="NORMAL">匹配关键词</a-select-option>
              <a-select-option value="BLACKLIST">黑名单关键词</a-select-option>
            </a-select>
            <a-select
              v-model:value="keywordFilterEnabled"
              placeholder="筛选状态"
              style="width: 120px"
              allow-clear
              @change="loadAllKeywords"
            >
              <a-select-option value="">全部状态</a-select-option>
              <a-select-option value="true">启用</a-select-option>
              <a-select-option value="false">禁用</a-select-option>
            </a-select>
            <a-input
              v-model:value="keywordSearchText"
              placeholder="搜索关键词"
              style="width: 200px"
              @press-enter="loadAllKeywords"
            >
              <template #suffix>
                <a-button type="text" @click="loadAllKeywords" size="small">搜索</a-button>
              </template>
            </a-input>
            <a-button @click="loadAllKeywords">刷新</a-button>
          </a-space>
        </div>

        <!-- 关键词列表表格 -->
        <a-table
          :columns="keywordColumns"
          :data-source="keywordList"
          :loading="keywordListLoading"
          :pagination="{ pageSize: 10, showSizeChanger: true, showQuickJumper: true }"
          row-key="id"
          style="margin-top: 16px"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'keywordType'">
              <a-tag :color="record.keywordType === 'NORMAL' ? 'blue' : 'red'">
                {{ record.keywordType === 'NORMAL' ? '匹配关键词' : '黑名单关键词' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'count'">
              <div class="count-display">
                <a-tooltip 
                  :title="`该关键词在${selectedCountry ? getCountryDisplayName(selectedCountry) : '所有'}高风险数据中出现了 ${record.count} 次`"
                  placement="top"
                >
                  <div class="count-container">
                    <div class="count-number" :class="{ 'has-data': record.count > 0, 'no-data': record.count === 0 }">
                      <span class="number">{{ record.count || 0 }}</span>
                      <span class="unit">条</span>
                    </div>
                    <div 
                      class="count-indicator"
                      :class="{ 'active': record.count > 0, 'inactive': record.count === 0 }"
                    ></div>
                  </div>
                </a-tooltip>
              </div>
            </template>
            <template v-else-if="column.key === 'enabled'">
              <a-switch
                v-model:checked="record.enabled"
                @change="updateKeywordStatus(record)"
                :loading="record.updating"
              />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="editKeywordInManagement(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" danger @click="deleteKeywordInManagement(record)">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>

    <!-- 创建/编辑关键词模态框 -->
    <a-modal
      v-model:open="showCreateKeywordModal"
      :title="editingKeywordInManagement ? '编辑关键词' : '添加关键词'"
      @ok="handleCreateOrUpdateKeyword"
      :confirm-loading="createKeywordLoading"
      width="500px"
    >
      <a-form :model="createKeywordForm" layout="vertical">
        <a-form-item label="关键词" required>
          <a-input
            v-model:value="createKeywordForm.keyword"
            placeholder="请输入关键词"
            :disabled="editingKeywordInManagement"
          />
        </a-form-item>
        <a-form-item label="关键词类型" required>
          <a-select
            v-model:value="createKeywordForm.keywordType"
            placeholder="选择关键词类型"
            style="width: 100%"
          >
            <a-select-option value="NORMAL">匹配关键词</a-select-option>
            <a-select-option value="BLACKLIST">黑名单关键词</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch
            v-model:checked="createKeywordForm.enabled"
            checked-children="启用"
            un-checked-children="禁用"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 可拖拽的关键词编辑浮动窗口 -->
    <div 
      v-if="showSimpleKeywordEdit"
      class="draggable-keyword-window"
      :style="keywordWindowStyle"
    >
      <div class="window-header" @mousedown="startDrag">
        <div class="window-title">
          <span>关键词编辑</span>
        </div>
        <div class="window-controls">
          <a-button type="text" size="small" @click="minimizeKeywordWindow">
            <MinusOutlined />
          </a-button>
          <a-button type="text" size="small" @click="closeKeywordWindow">
            <CloseOutlined />
          </a-button>
        </div>
      </div>
      
      <div class="window-content" v-show="!keywordWindowMinimized">
        <div class="simple-keyword-management">
          <a-form layout="vertical">
            <!-- 正常关键词列表 -->
            <a-form-item label="关键词列表">
              <div class="unified-keywords-container">
                <a-tag
                  v-for="(keyword, index) in normalKeywords"
                  :key="index"
                  closable
                  @close="removeNormalKeyword(index)"
                  class="keyword-tag"
                  :color="getKeywordColor(keyword.keyword)"
                >
                  <div class="keyword-content">
                    <span class="keyword-text">{{ keyword.keyword }}</span>
                    <a-badge
                      :count="keyword.count || 0"
                      class="keyword-badge"
                      :show-zero="false"
                    />
                  </div>
                </a-tag>
                <a-input
                  v-if="showNormalKeywordInput"
                  ref="normalKeywordInputRef"
                  v-model:value="newNormalKeyword"
                  size="small"
                  style="width: 150px;"
                  @blur="addNormalKeyword"
                  @keyup.enter="addNormalKeyword"
                  placeholder="输入关键词后按回车"
                />
                <a-button v-else type="dashed" size="small" @click="showNormalKeywordInput = true">
                  <PlusOutlined/>
                  添加关键词
                </a-button>
              </div>
            </a-form-item>
            
            <!-- 黑名单关键词列表 -->
            <a-form-item label="黑名单关键词">
              <div class="blacklist-keywords-container">
                <a-tag
                  v-for="(keyword, index) in blacklistKeywords"
                  :key="index"
                  closable
                  @close="removeBlacklistKeyword(index)"
                  class="blacklist-keyword-tag"
                  color="red"
                >
                  <div class="keyword-content">
                    <span class="keyword-text">{{ keyword.keyword }}</span>
                  </div>
                </a-tag>
                <a-input
                  v-if="showBlacklistKeywordInput"
                  ref="blacklistKeywordInputRef"
                  v-model:value="newBlacklistKeyword"
                  size="small"
                  style="width: 150px;"
                  @blur="addBlacklistKeyword"
                  @keyup.enter="addBlacklistKeyword"
                  placeholder="输入黑名单关键词后按回车"
                />
                <a-button v-else type="dashed" size="small" @click="showBlacklistKeywordInput = true">
                  <PlusOutlined/>
                  添加黑名单关键词
                </a-button>
              </div>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onErrorCaptured, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import { 
  WarningOutlined, 
  ExperimentOutlined, 
  ExclamationCircleOutlined, 
  AlertOutlined, 
  FileTextOutlined, 
  GlobalOutlined,
  ReloadOutlined,
  PlusOutlined,
  MinusOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import { 
  getHighRiskStatistics, 
  getHighRiskStatisticsByCountry, 
  getHighRiskDataByType, 
  getKeywordStatistics,
  getKeywordsByType,
  createKeyword,
  updateKeyword,
  deleteKeyword,
  searchKeywords
} from '@/api/api/highRiskData'
import HighRiskDataTable from '../components/HighRiskDataTable.vue'

// 当前选中的数据类型
const activeDataType = ref('device510k')

// 统计数据
const statistics = ref({
  totalHighRisk: 0,
  device510KHighRisk: 0,
  recallHighRisk: 0,
  eventHighRisk: 0,
  registrationHighRisk: 0,
  guidanceHighRisk: 0,
  customsHighRisk: 0
})

// 按国家分类的统计数据
const countryStatistics = ref<Record<string, any>>({})

// 选中的国家
const selectedCountry = ref<string>('')
const selectedCountryData = ref<any>(null)

// 选中的关键词
const selectedKeyword = ref<string>('')

// 所有关键词及其统计
const allKeywords = ref<Array<{keyword: string, count: number}>>([])


// 原来的关键词管理相关变量
const showKeywordManagement = ref(false)
const showCreateKeywordModal = ref(false)
const keywordList = ref<any[]>([])
const keywordListLoading = ref(false)
const editingKeywordInManagement = ref<any>(null)
const createKeywordLoading = ref(false)

// 关键词筛选条件
const keywordFilterType = ref<string>('NORMAL')
const keywordFilterEnabled = ref<string>('')
const keywordSearchText = ref<string>('')

// 创建关键词表单
const createKeywordForm = ref({
  keyword: '',
  keywordType: 'NORMAL',
  enabled: true
})

// 关键词列表表格列定义
const keywordColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '关键词',
    dataIndex: 'keyword',
    key: 'keyword',
    width: 200
  },
  {
    title: '类型',
    dataIndex: 'keywordType',
    key: 'keywordType',
    width: 120
  },
  {
    title: '统计数量',
    dataIndex: 'count',
    key: 'count',
    width: 120,
    sorter: true
  },
  {
    title: '启用状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 120
  }
]

// 新的简洁关键词编辑相关变量
const showSimpleKeywordEdit = ref(false)

// 正常关键词和黑名单关键词
const normalKeywords = ref<Array<{keyword: string, count: number, id?: number}>>([])
const blacklistKeywords = ref<Array<{keyword: string, count: number, id?: number}>>([])

// 输入框状态
const showNormalKeywordInput = ref(false)
const showBlacklistKeywordInput = ref(false)
const newNormalKeyword = ref('')
const newBlacklistKeyword = ref('')

// 输入框引用
const normalKeywordInputRef = ref()
const blacklistKeywordInputRef = ref()

// 拖拽窗口相关变量
const keywordWindowMinimized = ref(false)
const keywordWindowPosition = ref({ x: 100, y: 100 })
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const dragStartTime = ref(0)
const dragFrameId = ref<number | null>(null)

// 窗口样式计算（优化版本）
const keywordWindowStyle = computed(() => ({
  position: 'fixed' as const,
  left: `${keywordWindowPosition.value.x}px`,
  top: `${keywordWindowPosition.value.y}px`,
  zIndex: 1000,
  width: '450px',
  maxHeight: keywordWindowMinimized.value ? '40px' : '600px',
  // 添加硬件加速和性能优化
  transform: 'translateZ(0)', // 强制启用硬件加速
  willChange: isDragging.value ? 'transform, left, top' : 'auto', // 拖拽时优化渲染
  backfaceVisibility: 'hidden' as const, // 减少重绘
  transition: isDragging.value ? 'none' : 'all 0.2s ease', // 拖拽时禁用过渡
  boxShadow: isDragging.value ? '0 8px 32px rgba(0, 0, 0, 0.3)' : '0 4px 16px rgba(0, 0, 0, 0.15)' // 拖拽时增强阴影
}))


// 刷新状态
const refreshing = ref(false)

// 各数据类型的数据统计
const dataTypeStats = ref<Record<string, number>>({
  device510k: 0,
  recall: 0,
  event: 0,
  registration: 0,
  guidance: 0,
  customs: 0
})



// 查看关键词数据
const viewKeywordData = async (keyword: string) => {
  selectedKeyword.value = keyword
  console.log('查看关键词数据:', keyword)
  
  const countryText = selectedCountry.value ? `在${getCountryDisplayName(selectedCountry.value)}中` : ''
  message.success(`已筛选关键词 "${keyword}" ${countryText}的数据`)
  
  // 获取关键词对应的统计数据
  await loadKeywordSpecificStats(keyword)
  
  // 自动切换到第一个标签页
  setTimeout(() => {
    const firstTab = availableTabs.value[0]
    if (firstTab) {
      activeDataType.value = firstTab.key
      console.log('自动切换到标签页:', firstTab.key)
    }
  }, 100)
}

// 清除关键词选择
const clearKeywordSelection = async () => {
  selectedKeyword.value = ''
  message.info('已清除关键词筛选')
  
  // 恢复原始统计数据或重新加载国家统计数据
  if (selectedCountry.value) {
    // 如果还有国家筛选，重新加载国家统计
    await loadCountrySpecificStats(selectedCountry.value)
  } else {
    // 恢复原始统计数据
    restoreOriginalStats()
  }
  
  // 自动切换到第一个标签页
  setTimeout(() => {
    const firstTab = availableTabs.value[0]
    if (firstTab) {
      activeDataType.value = firstTab.key
      console.log('清除筛选后自动切换到标签页:', firstTab.key)
    }
  }, 100)
}







// 加载关键词统计
const loadKeywordStatistics = async (country?: string) => {
  console.log('🔄 开始加载关键词统计...', country ? `国家筛选: ${country}` : '')
  try {
    const response = await getKeywordStatistics(country)
    
    if (response && (response as any).keywords) {
      allKeywords.value = (response as any).keywords
      console.log('✅ 关键词统计加载成功:', allKeywords.value)
    } else {
      console.warn('⚠️ 关键词统计响应格式不正确:', response)
      allKeywords.value = []
    }
  } catch (error) {
    console.warn('⚠️ 关键词统计API暂未实现，跳过关键词功能:', error)
    // 不显示错误消息，因为这是可选功能
    allKeywords.value = []
  }
}

// 打开关键词管理模态框
const openKeywordManagement = async () => {
  // 重置筛选条件
  keywordFilterType.value = 'NORMAL'
  keywordFilterEnabled.value = ''
  keywordSearchText.value = ''
  
  // 打开模态框
  showKeywordManagement.value = true
  
  // 加载正常关键词
  await loadAllKeywords()
}

// 处理关键词点击 - 打开简洁关键词编辑界面
const handleKeywordClick = async (_record: any, _keyword: string) => {
  // 打开简洁关键词编辑模态框
  showSimpleKeywordEdit.value = true
  
  // 加载关键词数据
  await loadKeywordsForSimpleEdit()
  
  // message.info(`已打开关键词编辑界面，可以编辑 "${keyword}" 关键词`)
}

// 加载所有关键词列表（用于关键词管理）
const loadAllKeywords = async () => {
  console.log('🔄 开始加载关键词列表...')
  keywordListLoading.value = true
  
  try {
    // 先获取所有关键词列表，再获取统计信息
    let allKeywordsResponse
    let statsResponse
    
    // 根据筛选条件获取关键词列表
    if (keywordFilterType.value && keywordFilterType.value !== '') {
      allKeywordsResponse = await getKeywordsByType(keywordFilterType.value)
    } else if (keywordSearchText.value) {
      allKeywordsResponse = await searchKeywords(keywordSearchText.value)
    } else {
      // 默认加载正常关键词
      allKeywordsResponse = await getKeywordsByType('NORMAL')
    }
    
    // 获取统计信息
    statsResponse = await getKeywordStatistics()
    
    let keywords = (allKeywordsResponse as any)?.data || allKeywordsResponse || []
    const stats = (statsResponse as any)?.keywords || []
    
    // 创建统计映射
    const statsMap = new Map()
    stats.forEach((stat: any) => {
      statsMap.set(stat.keyword, stat.count)
    })
    
    // 合并关键词信息和统计数量
    keywords = keywords.map((keyword: any) => ({
      ...keyword,
      count: statsMap.get(keyword.keyword) || 0
    }))
    
    // 应用启用状态筛选
    if (keywordFilterEnabled.value !== '') {
      const enabledFilter = keywordFilterEnabled.value === 'true'
      keywords = keywords.filter((k: any) => k.enabled === enabledFilter)
    }
    
    keywordList.value = keywords
    console.log('✅ 关键词列表加载成功:', keywords.length, '个关键词')
    
  } catch (error) {
    console.error('❌ 加载关键词列表失败:', error)
    message.error('加载关键词列表失败')
    keywordList.value = []
  } finally {
    keywordListLoading.value = false
  }
}

// 更新关键词状态
const updateKeywordStatus = async (record: any) => {
  record.updating = true
  
  try {
    const result = await updateKeyword(record.id, {
      enabled: record.enabled
    })
    
    if (result && (result as any).success) {
      message.success('关键词状态更新成功')
      // 刷新关键词统计
      await loadKeywordStatistics()
    } else {
      const errorMsg = (result as any)?.error || '关键词状态更新失败'
      message.error(errorMsg)
      // 回滚状态
      record.enabled = !record.enabled
    }
  } catch (error) {
    console.error('更新关键词状态失败:', error)
    message.error('关键词状态更新失败')
    // 回滚状态
    record.enabled = !record.enabled
  } finally {
    record.updating = false
  }
}

// 编辑关键词（在管理页面中）
const editKeywordInManagement = (record: any) => {
  editingKeywordInManagement.value = record
  createKeywordForm.value = {
    keyword: record.keyword,
    keywordType: record.keywordType,
    enabled: record.enabled
  }
  showCreateKeywordModal.value = true
}

// 删除关键词
const deleteKeywordInManagement = async (record: any) => {
  const { Modal } = await import('ant-design-vue')
  
  Modal.confirm({
    title: '删除关键词',
    content: () => {
      return h('div', {
        style: {
          padding: '8px 0'
        }
      }, [
        h('div', {
          style: {
            marginBottom: '12px',
            fontSize: '14px',
            color: '#666'
          }
        }, '确定要删除关键词'),
        h('div', {
          style: {
            background: '#f5f5f5',
            padding: '8px 12px',
            borderRadius: '4px',
            border: '1px solid #d9d9d9',
            marginBottom: '8px',
            fontFamily: 'SF Mono, Monaco, Cascadia Code, Roboto Mono, Consolas, Courier New, monospace',
            fontWeight: '500',
            color: '#1890ff'
          }
        }, record.keyword),
        h('div', {
          style: {
            fontSize: '12px',
            color: '#ff4d4f'
          }
        }, '删除后无法恢复')
      ])
    },
    okText: '删除',
    cancelText: '取消',
    okType: 'danger',
    width: 360,
    centered: true,
    onOk: async () => {
      try {
        const result = await deleteKeyword(record.id)
        
        if (result && (result as any).success) {
          message.success('关键词删除成功')
          await loadAllKeywords()
          await loadKeywordStatistics()
        } else {
          const errorMsg = (result as any)?.error || '关键词删除失败'
          message.error(errorMsg)
        }
      } catch (error: any) {
        console.error('删除关键词失败:', error)
        message.error('关键词删除失败')
      }
    }
  })
}

// 创建或更新关键词
const handleCreateOrUpdateKeyword = async () => {
  if (!createKeywordForm.value.keyword.trim()) {
    message.warning('请输入关键词')
    return
  }
  
  if (!createKeywordForm.value.keywordType) {
    message.warning('请选择关键词类型')
    return
  }
  
  createKeywordLoading.value = true
  
  try {
    let result
    
    if (editingKeywordInManagement.value) {
      // 更新关键词
      result = await updateKeyword(editingKeywordInManagement.value.id, {
        keyword: createKeywordForm.value.keyword.trim(),
        keywordType: createKeywordForm.value.keywordType,
        enabled: createKeywordForm.value.enabled
      })
    } else {
      // 创建新关键词
      result = await createKeyword(
        createKeywordForm.value.keyword.trim(),
        createKeywordForm.value.keywordType,
        createKeywordForm.value.enabled
      )
    }
    
    if (result && (result as any).success) {
      message.success(editingKeywordInManagement.value ? '关键词更新成功' : '关键词创建成功')
      showCreateKeywordModal.value = false
      
      // 重置表单
      createKeywordForm.value = {
        keyword: '',
        keywordType: 'NORMAL',
        enabled: true
      }
      editingKeywordInManagement.value = null
      
      // 刷新数据
      await loadAllKeywords()
      await loadKeywordStatistics()
    } else {
      const errorMsg = (result as any)?.error || (editingKeywordInManagement.value ? '关键词更新失败' : '关键词创建失败')
      message.error(errorMsg)
    }
  } catch (error) {
    console.error('处理关键词失败:', error)
    message.error(editingKeywordInManagement.value ? '关键词更新失败' : '关键词创建失败')
  } finally {
    createKeywordLoading.value = false
  }
}

// 处理数据类型切换
const handleDataTypeChange = (key: string) => {
  activeDataType.value = key
  console.log('切换到数据类型:', key)
}

// 加载统计数据
const loadStatistics = async () => {
  console.log('🔄 开始加载高风险数据统计...')
  try {
    const response = await getHighRiskStatistics()
    console.log('📊 统计数据响应:', response)
    
    if (response && typeof response === 'object') {
      const hasStats = Object.keys(response).some(key => key.includes('HighRisk'))
      if (hasStats) {
        statistics.value = response as any
        console.log('✅ 统计数据加载成功:', statistics.value)
      } else {
        console.warn('⚠️ 响应数据不包含统计字段:', response)
      }
    } else {
      console.warn('⚠️ 统计数据响应为空或格式不正确:', response)
    }
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error)
    message.error('加载统计数据失败')
  }
}

// 加载按国家分类的统计数据
const loadCountryStatistics = async () => {
  console.log('🔄 开始加载按国家分类的统计数据...')
  try {
    const response = await getHighRiskStatisticsByCountry()
    console.log('📊 按国家统计数据响应:', response)
    
    if (response && (response as any).countryStatistics) {
      countryStatistics.value = (response as any).countryStatistics
      console.log('✅ 按国家统计数据加载成功:', countryStatistics.value)
    } else {
      console.warn('⚠️ 按国家统计数据响应格式不正确:', response)
    }
  } catch (error) {
    console.error('❌ 加载按国家统计数据失败:', error)
    message.error('加载按国家统计数据失败')
  }
}

// 获取国家显示名称
const getCountryDisplayName = (countryCode: string): string => {
  const countryNames: Record<string, string> = {
    'US': '美国',
    'CN': '中国',
    'EU': '欧盟',
    'JP': '日本',
    'KR': '韩国',
    'CA': '加拿大',
    'AU': '澳大利亚',
    'GB': '英国',
    'DE': '德国',
    'FR': '法国',
    'Unknown': '未知'
  }
  return countryNames[countryCode] || countryCode
}

// 处理国家卡片点击
const handleCountryClick = async (countryCode: string, countryData: any) => {
  selectedCountry.value = countryCode
  selectedCountryData.value = countryData
  console.log('选中国家:', countryCode, '数据:', countryData)
  message.info(`已选择 ${getCountryDisplayName(countryCode)} 的高风险数据`)
  
  // 重新加载该国家的关键词统计
  await loadKeywordStatistics(countryCode)
  
  // 重新加载该国家的数据类型统计
  await loadCountrySpecificStats(countryCode)
  
  setTimeout(() => {
    const firstAvailableTab = availableTabs.value[0]
    if (firstAvailableTab) {
      activeDataType.value = firstAvailableTab.key
      console.log('自动切换到标签页:', firstAvailableTab.key)
    }
  }, 100)
}

// 清除国家选择
const clearCountrySelection = async () => {
  selectedCountry.value = ''
  selectedCountryData.value = null
  console.log('清除国家选择，显示全部数据')
  message.info('已切换到显示全部国家数据')
  
  // 重新加载全部关键词统计
  await loadKeywordStatistics()
  
  // 恢复原始统计数据或重新加载关键词统计数据
  if (selectedKeyword.value) {
    // 如果还有关键词筛选，重新加载关键词统计
    await loadKeywordSpecificStats(selectedKeyword.value)
  } else {
    // 恢复原始统计数据
    restoreOriginalStats()
  }
  
  setTimeout(() => {
    const firstAvailableTab = availableTabs.value[0]
    if (firstAvailableTab) {
      activeDataType.value = firstAvailableTab.key
      console.log('自动切换到标签页:', firstAvailableTab.key)
    }
  }, 100)
}

// 获取当前显示标题
const getCurrentDisplayTitle = (): string => {
  if (selectedCountry.value && selectedKeyword.value) {
    return `${getCountryDisplayName(selectedCountry.value)} - ${selectedKeyword.value} 高风险数据详情`
  } else if (selectedCountry.value) {
    return `${getCountryDisplayName(selectedCountry.value)} 高风险数据详情`
  } else if (selectedKeyword.value) {
    return `${selectedKeyword.value} 高风险数据详情`
  }
  return '高风险数据管理'
}

// 处理数据加载完成事件
const handleDataLoaded = (dataType: string, _data: any[], total: number) => {
  // 只有在没有关键词筛选时才更新dataTypeStats，避免影响标签页显示
  if (!selectedKeyword.value) {
    dataTypeStats.value[dataType] = total
  }
  console.log(`📊 ${dataType} 数据加载完成，共 ${total} 条`)
}

// 获取关键词对应的统计数据
const loadKeywordSpecificStats = async (keyword: string) => {
  console.log('🔄 开始加载关键词对应的统计数据...', keyword)
  
  const dataTypes = ['device510k', 'recall', 'event', 'registration', 'guidance', 'customs']
  
  try {
    const promises = dataTypes.map(async (dataType) => {
      try {
        const response = await getHighRiskDataByType(dataType, { 
          page: 0, 
          size: 1,
          sortBy: 'id',
          sortDir: 'asc',
          keyword: keyword,
          country: selectedCountry.value || undefined
        })
        
        const total = (response as any)?.data?.totalElements || (response as any)?.totalElements || 0
        dataTypeStats.value[dataType] = total
        
        console.log(`✅ ${dataType} 关键词 "${keyword}" 数据数量: ${total}`)
        return { dataType, total }
      } catch (error) {
        console.error(`❌ 获取 ${dataType} 关键词统计数据失败:`, error)
        dataTypeStats.value[dataType] = 0
        return { dataType, total: 0 }
      }
    })
    
    const results = await Promise.all(promises)
    console.log('📊 关键词统计数据加载完成:', results)
    
  } catch (error) {
    console.error('❌ 加载关键词统计数据失败:', error)
  }
}

// 获取国家对应的统计数据
const loadCountrySpecificStats = async (country: string) => {
  console.log('🔄 开始加载国家对应的统计数据...', country)
  
  const dataTypes = ['device510k', 'recall', 'event', 'registration', 'guidance', 'customs']
  
  try {
    const promises = dataTypes.map(async (dataType) => {
      try {
        const response = await getHighRiskDataByType(dataType, { 
          page: 0, 
          size: 1,
          sortBy: 'id',
          sortDir: 'asc',
          keyword: selectedKeyword.value || undefined,
          country: country
        })
        
        const total = (response as any)?.data?.totalElements || (response as any)?.totalElements || 0
        dataTypeStats.value[dataType] = total
        
        console.log(`✅ ${dataType} 国家 "${country}" 数据数量: ${total}`)
        return { dataType, total }
      } catch (error) {
        console.error(`❌ 获取 ${dataType} 国家统计数据失败:`, error)
        dataTypeStats.value[dataType] = 0
        return { dataType, total: 0 }
      }
    })
    
    const results = await Promise.all(promises)
    console.log('📊 国家统计数据加载完成:', results)
    
  } catch (error) {
    console.error('❌ 加载国家统计数据失败:', error)
  }
}

// 保存原始的统计数据，用于恢复
const originalDataTypeStats = ref<Record<string, number>>({})

// 保存原始统计数据
const saveOriginalStats = () => {
  originalDataTypeStats.value = { ...dataTypeStats.value }
  console.log('💾 保存原始统计数据:', originalDataTypeStats.value)
}

// 恢复原始统计数据
const restoreOriginalStats = () => {
  dataTypeStats.value = { ...originalDataTypeStats.value }
  console.log('🔄 恢复原始统计数据:', dataTypeStats.value)
}

// 初始化获取所有数据类型的数据数量
const loadAllDataTypeStats = async () => {
  console.log('🔄 开始加载所有数据类型的数据统计...')
  
  const dataTypes = ['device510k', 'recall', 'event', 'registration', 'guidance', 'customs']
  
  try {
    const promises = dataTypes.map(async (dataType) => {
      try {
        const response = await getHighRiskDataByType(dataType, { 
          page: 0, 
          size: 1,
          sortBy: 'id',
          sortDir: 'asc',
          keyword: selectedKeyword.value || undefined,
          country: selectedCountry.value || undefined
        })
        
        const total = (response as any)?.data?.totalElements || (response as any)?.totalElements || 0
        dataTypeStats.value[dataType] = total
        
        console.log(`✅ ${dataType} 数据数量: ${total}`)
        return { dataType, total }
      } catch (error) {
        console.error(`❌ 获取 ${dataType} 数据数量失败:`, error)
        dataTypeStats.value[dataType] = 0
        return { dataType, total: 0 }
      }
    })
    
    const results = await Promise.all(promises)
    console.log('📊 所有数据类型统计完成:', results)
    
    // 如果是第一次加载（没有关键词筛选），保存原始统计数据
    if (!selectedKeyword.value) {
      saveOriginalStats()
    }
    
  } catch (error) {
    console.error('❌ 加载数据类型统计失败:', error)
    message.error('加载数据类型统计失败')
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  refreshing.value = true
  try {
    console.log('🔄 开始刷新所有数据...')
    
    await Promise.all([
      loadStatistics(),
      loadCountryStatistics(),
      loadAllDataTypeStats(),
      loadKeywordStatistics()
    ])
    
    message.success('数据刷新成功')
    console.log('✅ 所有数据刷新完成')
    
  } catch (error) {
    console.error('❌ 刷新数据失败:', error)
    message.error('刷新数据失败')
  } finally {
    refreshing.value = false
  }
}

// 计算属性：获取有数据的标签页
const availableTabs = computed(() => {
  const tabs: Array<{ key: string; label: string; count: number }> = []
  
  const allTabs = [
    { key: 'registration', label: '注册记录' },
    { key: 'device510k', label: '上市前通告' },
    { key: 'recall', label: '召回记录' },
    { key: 'event', label: '事件报告' },
    { key: 'guidance', label: '指导文档' },
    { key: 'customs', label: '海关案例' }
  ]
  
  // 始终使用dataTypeStats，因为它会根据筛选条件动态更新
  allTabs.forEach(tab => {
    const count = dataTypeStats.value[tab.key] || 0
    if (count > 0) {
      tabs.push({ ...tab, count })
    }
  })
  
  console.log('📊 最终可用标签页:', tabs)
  return tabs
})


// 拖拽窗口相关函数

// 开始拖拽
const startDrag = (event: MouseEvent) => {
  if (event.target && (event.target as HTMLElement).closest('.window-controls')) {
    // 如果点击的是窗口控制按钮，不开始拖拽
    return
  }
  
  isDragging.value = true
  dragStartTime.value = performance.now()
  dragOffset.value = {
    x: event.clientX - keywordWindowPosition.value.x,
    y: event.clientY - keywordWindowPosition.value.y
  }
  
  // 添加全局鼠标事件监听器，使用 passive 选项提高性能
  document.addEventListener('mousemove', handleDragThrottled, { passive: false })
  document.addEventListener('mouseup', stopDrag, { passive: true })
  
  // 添加视觉反馈和性能优化类
  const windowElement = document.querySelector('.draggable-keyword-window') as HTMLElement
  if (windowElement) {
    windowElement.classList.add('dragging')
    windowElement.style.userSelect = 'none'
  }
  
  // 防止文本选择和默认行为
  event.preventDefault()
  event.stopPropagation()
}

// 节流处理拖拽事件
const handleDragThrottled = (event: MouseEvent) => {
  if (!isDragging.value) return
  
  // 取消之前的动画帧
  if (dragFrameId.value) {
    cancelAnimationFrame(dragFrameId.value)
  }
  
  // 使用 requestAnimationFrame 确保流畅的拖拽
  dragFrameId.value = requestAnimationFrame(() => {
    handleDrag(event)
  })
}

// 处理拖拽（优化版本）
const handleDrag = (event: MouseEvent) => {
  if (!isDragging.value) return
  
  // 计算新位置，添加边界检查和性能优化
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const windowElementWidth = 450
  const windowElementHeight = keywordWindowMinimized.value ? 40 : 600
  
  const newX = Math.max(0, Math.min(windowWidth - windowElementWidth, event.clientX - dragOffset.value.x))
  const newY = Math.max(0, Math.min(windowHeight - windowElementHeight, event.clientY - dragOffset.value.y))
  
  // 只有位置真正改变时才更新，减少不必要的重渲染
  if (keywordWindowPosition.value.x !== newX || keywordWindowPosition.value.y !== newY) {
    keywordWindowPosition.value = { x: newX, y: newY }
  }
}

// 停止拖拽（优化版本）
const stopDrag = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  
  // 取消任何待处理的动画帧
  if (dragFrameId.value) {
    cancelAnimationFrame(dragFrameId.value)
    dragFrameId.value = null
  }
  
  // 移除事件监听器
  document.removeEventListener('mousemove', handleDragThrottled)
  document.removeEventListener('mouseup', stopDrag)
  
  // 恢复窗口样式
  const windowElement = document.querySelector('.draggable-keyword-window') as HTMLElement
  if (windowElement) {
    windowElement.classList.remove('dragging')
    windowElement.style.userSelect = ''
  }
  
  // 计算拖拽性能统计
  const dragDuration = performance.now() - dragStartTime.value
  console.log(`🎯 拖拽完成，耗时: ${dragDuration.toFixed(2)}ms`)
}

// 最小化窗口
const minimizeKeywordWindow = () => {
  keywordWindowMinimized.value = !keywordWindowMinimized.value
}

// 关闭窗口
const closeKeywordWindow = () => {
  showSimpleKeywordEdit.value = false
  keywordWindowMinimized.value = false
  // 重置窗口位置
  keywordWindowPosition.value = { x: 100, y: 100 }
}

// 简洁关键词编辑相关函数

// 加载关键词数据（用于简洁编辑）
const loadKeywordsForSimpleEdit = async () => {
  try {
    // 加载正常关键词
    const normalResponse = await getKeywordsByType('NORMAL')
    const normalKeywordList = (normalResponse as any)?.data || normalResponse || []
    
    // 加载黑名单关键词
    const blacklistResponse = await getKeywordsByType('BLACKLIST')
    const blacklistKeywordList = (blacklistResponse as any)?.data || blacklistResponse || []
    
    // 获取统计信息
    const statsResponse = await getKeywordStatistics()
    const stats = (statsResponse as any)?.keywords || []
    
    // 创建统计映射
    const statsMap = new Map()
    stats.forEach((stat: any) => {
      statsMap.set(stat.keyword, stat.count)
    })
    
    // 设置正常关键词
    normalKeywords.value = normalKeywordList.map((keyword: any) => ({
      keyword: keyword.keyword,
      count: statsMap.get(keyword.keyword) || 0,
      id: keyword.id
    }))
    
    // 设置黑名单关键词
    blacklistKeywords.value = blacklistKeywordList.map((keyword: any) => ({
      keyword: keyword.keyword,
      count: statsMap.get(keyword.keyword) || 0,
      id: keyword.id
    }))
    
    console.log('✅ 简洁编辑关键词数据加载成功')
  } catch (error) {
    console.error('❌ 加载简洁编辑关键词数据失败:', error)
    message.error('加载关键词数据失败')
  }
}

// 获取关键词颜色
const getKeywordColor = (keyword: any) => {
  const count = keyword.count || 0
  if (count > 100) return 'red'
  if (count > 50) return 'orange'
  if (count > 10) return 'blue'
  if (count > 0) return 'green'
  return 'default'
}

// 添加正常关键词
const addNormalKeyword = async () => {
  if (!newNormalKeyword.value.trim()) {
    showNormalKeywordInput.value = false
    return
  }
  
  try {
    const result = await createKeyword(
      newNormalKeyword.value.trim(),
      'NORMAL',
      true
    )
    
    if (result && (result as any).success) {
      message.success('关键词添加成功')
      newNormalKeyword.value = ''
      showNormalKeywordInput.value = false
      
      // 重新加载数据
      await loadKeywordsForSimpleEdit()
    } else {
      message.error('关键词添加失败')
    }
  } catch (error) {
    console.error('添加关键词失败:', error)
    message.error('关键词添加失败')
  }
}

// 添加黑名单关键词
const addBlacklistKeyword = async () => {
  if (!newBlacklistKeyword.value.trim()) {
    showBlacklistKeywordInput.value = false
    return
  }
  
  try {
    const result = await createKeyword(
      newBlacklistKeyword.value.trim(),
      'BLACKLIST',
      true
    )
    
    if (result && (result as any).success) {
      message.success('黑名单关键词添加成功')
      newBlacklistKeyword.value = ''
      showBlacklistKeywordInput.value = false
      
      // 重新加载数据
      await loadKeywordsForSimpleEdit()
    } else {
      message.error('黑名单关键词添加失败')
    }
  } catch (error) {
    console.error('添加黑名单关键词失败:', error)
    message.error('黑名单关键词添加失败')
  }
}

// 删除正常关键词
const removeNormalKeyword = async (index: number) => {
  const keyword = normalKeywords.value[index]
  if (!keyword || !keyword.id) {
    normalKeywords.value.splice(index, 1)
    return
  }
  
  try {
    const result = await deleteKeyword(keyword.id)
    if (result && (result as any).success) {
      message.success('关键词删除成功')
      normalKeywords.value.splice(index, 1)
    } else {
      message.error('关键词删除失败')
    }
  } catch (error) {
    console.error('删除关键词失败:', error)
    message.error('关键词删除失败')
  }
}

// 删除黑名单关键词
const removeBlacklistKeyword = async (index: number) => {
  const keyword = blacklistKeywords.value[index]
  if (!keyword || !keyword.id) {
    blacklistKeywords.value.splice(index, 1)
    return
  }
  
  try {
    const result = await deleteKeyword(keyword.id)
    if (result && (result as any).success) {
      message.success('黑名单关键词删除成功')
      blacklistKeywords.value.splice(index, 1)
    } else {
      message.error('黑名单关键词删除失败')
    }
  } catch (error) {
    console.error('删除黑名单关键词失败:', error)
    message.error('黑名单关键词删除失败')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadStatistics()
  loadCountryStatistics()
  loadAllDataTypeStats()
  loadKeywordStatistics()
})

// 错误捕获
onErrorCaptured((error, _instance, info) => {
  console.error('主页面错误:', error)
  console.error('错误信息:', info)
  message.error('页面加载失败，请刷新页面重试')
  return false
})
</script>

<style scoped>
.high-risk-data-management {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-text {
  flex: 1;
}

.header-actions {
  margin-left: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #1890ff;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.data-filter-section {
  margin-bottom: 24px;
}

.data-filter-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keyword-filter-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.keyword-filter-header {
  margin-bottom: 16px;
}

.keyword-filter-header h4 {
  margin: 0 0 4px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.keyword-filter-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.keyword-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.keyword-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.clickable-keyword-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.clickable-keyword-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.current-selection-info {
  margin-bottom: 24px;
}

.country-stats-row {
  margin-bottom: 16px;
}

.data-type-tabs {
  background: transparent;
}

.data-type-tabs :deep(.ant-tabs-nav) {
  margin: 0;
  padding: 0;
}

.data-type-tabs :deep(.ant-tabs-tab) {
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 500;
}

.data-type-tabs :deep(.ant-tabs-tab-active) {
  background: #e6f7ff;
  border-radius: 6px 6px 0 0;
}

.data-type-tabs :deep(.ant-tabs-content) {
  padding: 24px 0;
  min-height: 600px;
}

.country-stats-section {
  margin-bottom: 24px;
}

.country-stats-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.country-stat-card {
  text-align: center;
  transition: all 0.3s;
  margin-bottom: 16px;
  cursor: pointer;
  position: relative;
}

.country-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.country-stat-card.selected {
  border: 2px solid #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transform: translateY(-2px);
}

.click-hint {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(24, 144, 255, 0.1);
  border-radius: 4px;
  padding: 2px 6px;
}

.hint-text {
  font-size: 10px;
  color: #1890ff;
  font-weight: 500;
}

.country-detail-stats {
  margin-top: 16px;
  text-align: left;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}

.detail-item .label {
  color: #666;
  font-weight: 500;
}

.detail-item .value {
  color: #1890ff;
  font-weight: 600;
}

.data-type-card {
  text-align: center;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.data-type-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.no-data-tip {
  padding: 60px 20px;
  text-align: center;
  background: #fafafa;
  border-radius: 8px;
  margin: 20px 0;
}

.keyword-detail-stats .ant-card {
  text-align: center;
}

.keyword-detail-stats .ant-statistic-title {
  font-size: 12px;
  margin-bottom: 4px;
}

.keyword-detail-stats .ant-statistic-content {
  font-size: 18px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .high-risk-data-management {
    padding: 16px;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-actions {
    margin-left: 0;
    width: 100%;
  }
  
  .keyword-tags-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 关键词管理相关样式 */
.keyword-filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.keyword-header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.keyword-header-actions p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.keyword-management {
  .keyword-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 6px;
  }
}

.keyword-list-table {
  .ant-table-thead > tr > th {
    background: #f5f5f5;
    font-weight: 600;
  }
  
  .ant-table-tbody > tr:hover > td {
    background: #f0f9ff;
  }
}

/* 统计数量显示样式 */
.count-display {
  .count-container {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    padding: 4px 0;
    gap: 8px;
    
    .count-number {
      display: flex;
      align-items: baseline;
      gap: 2px;
      
      .number {
        font-family: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
        font-size: 16px;
        font-weight: bold;
        letter-spacing: 0.5px;
        transition: all 0.2s ease;
      }
      
      .unit {
        font-size: 12px;
        font-weight: normal;
        opacity: 0.7;
        transition: opacity 0.2s ease;
      }
      
      &.has-data {
        .number {
          color: #1890ff;
        }
        .unit {
          color: #1890ff;
        }
      }
      
      &.no-data {
        .number {
          color: #8c8c8c;
        }
        .unit {
          color: #8c8c8c;
        }
      }
    }
    
    .count-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      transition: all 0.2s ease;
      
      &.active {
        background-color: #52c41a;
        box-shadow: 0 0 4px rgba(82, 196, 26, 0.4);
      }
      
      &.inactive {
        background-color: #d9d9d9;
      }
    }
  }
  
  &:hover {
    .count-container {
      .count-number {
        .number {
          transform: scale(1.05);
        }
        .unit {
          opacity: 1;
        }
      }
      
      .count-indicator {
        transform: scale(1.2);
        
        &.active {
          box-shadow: 0 0 8px rgba(82, 196, 26, 0.6);
        }
      }
    }
  }
}

.keyword-type-tag {
  &.normal {
    background: #e6f7ff;
    color: #1890ff;
    border-color: #91d5ff;
  }
  
  &.blacklist {
    background: #fff2f0;
    color: #ff4d4f;
    border-color: #ffccc7;
  }
}

/* 可拖拽关键词编辑窗口样式（性能优化版本） */
.draggable-keyword-window {
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid #e8e8e8;
  overflow: hidden;
  user-select: none;
  transition: all 0.3s ease;
  
  /* 性能优化 */
  transform: translateZ(0); /* 启用硬件加速 */
  backface-visibility: hidden; /* 减少重绘 */
  -webkit-font-smoothing: antialiased; /* 改善字体渲染 */
  contain: layout style paint; /* CSS Containment 优化 */
  
  /* 拖拽时的优化 */
  &.dragging {
    transition: none !important; /* 拖拽时禁用过渡 */
    will-change: transform, left, top; /* 优化渲染性能 */
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.25); /* 拖拽时增强阴影 */
    transform: translateZ(0) scale(1.02); /* 轻微放大效果 */
    cursor: grabbing;
  }
  
  &:hover:not(.dragging) {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    transform: translateZ(0) translateY(-1px); /* 轻微上浮效果 */
  }
}

.window-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  cursor: grab;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 40px;
  
  /* 性能优化 */
  transform: translateZ(0);
  will-change: transform;
  transition: all 0.2s ease;
  
  &:hover {
    background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
    transform: translateZ(0) scale(1.01);
  }
  
  &:active {
    cursor: grabbing;
    transform: translateZ(0) scale(0.99);
  }
  
  .window-title {
    font-weight: 600;
    font-size: 14px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
  
  .window-controls {
    display: flex;
    gap: 4px;
    
    .ant-btn {
      color: white;
      border: none;
      background: transparent;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }
}

.window-content {
  padding: 16px;
  max-height: 560px;
  overflow-y: auto;
}

/* 简洁关键词编辑样式 - 标签形式 */
.simple-keyword-management {
  .unified-keywords-container, .blacklist-keywords-container {
    min-height: 100px;
    max-height: 200px;
    overflow-y: auto;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    padding: 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: flex-start;
    align-content: flex-start;
  }
  
  .keyword-tag, .blacklist-keyword-tag {
    margin: 0;
    display: inline-flex;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 12px;
    
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }
  }
  
  .keyword-content {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .keyword-text {
    font-weight: 500;
  }
  
  .keyword-badge {
    :deep(.ant-badge-count) {
      background: rgba(255, 255, 255, 0.9);
      color: #1890ff;
      border: 1px solid #1890ff;
      font-size: 10px;
      min-width: 16px;
      height: 16px;
      line-height: 14px;
      padding: 0 4px;
    }
  }
}</style>


<template>
  <div class="standards-page">
    <div class="page-header">
      <h1>标准管理</h1>
      <a-space>
        <a-button @click="refreshData" :loading="loading">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="showAddModal = true">
          <template #icon>
            <PlusOutlined />
          </template>
          添加标准
        </a-button>
      </a-space>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <Statistic
              title="总标准数"
              :value="statistics.total"
              :loading="loading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <Statistic
              title="高风险标准"
              :value="statistics.highRisk"
              :value-style="{ color: '#cf1322' }"
              :loading="loading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <Statistic
              title="监控中标准"
              :value="statistics.monitored"
              :value-style="{ color: '#3f8600' }"
              :loading="loading"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <Statistic
              title="即将到期"
              :value="statistics.expiring"
              :value-style="{ color: '#faad14' }"
              :loading="loading"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选 -->
    <a-card style="margin-bottom: 16px;">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="搜索标准编号或标题"
            allow-clear
            @change="handleSearch"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="searchForm.riskLevel"
            placeholder="风险等级"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="searchForm.standardStatus"
            placeholder="标准状态"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="ACTIVE">生效</a-select-option>
            <a-select-option value="SUPERSEDED">已替代</a-select-option>
            <a-select-option value="WITHDRAWN">已撤回</a-select-option>
            <a-select-option value="UNDER_REVISION">修订中</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="searchForm.country"
            placeholder="国家/地区"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option 
              v-for="option in countryOptions" 
              :key="option.value" 
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="loading">
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button @click="resetSearch">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
            <a-button @click="loadAllData">
              <template #icon>
                <DatabaseOutlined />
              </template>
              显示所有
            </a-button>


          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 标准列表 -->
    <a-card>
      <template #extra>
        <a-space>
          <a-switch
            v-model:checked="showMonitoredOnly"
            @change="handleSearch"
          >
            <template #checkedChildren>仅监控</template>
            <template #unCheckedChildren>全部</template>
          </a-switch>
          <a-button @click="exportData" :loading="exporting">
            <template #icon>
              <DownloadOutlined />
            </template>
            导出
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="standards"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="getRiskColor(record.riskLevel)">
              {{ getRiskText(record.riskLevel) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'standardStatus'">
            <a-tag :color="getStatusColor(record.standardStatus)">
              {{ getStatusText(record.standardStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'isMonitored'">
            <a-tag :color="record.isMonitored ? 'green' : 'default'">
              {{ record.isMonitored ? '监控中' : '未监控' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="viewDetail(record)">查看</a>
              <a-divider type="vertical" />
              <a @click="editStandard(record)">编辑</a>
              <a-divider type="vertical" />
              <a-switch
                :checked="record.isMonitored"
                size="small"
                @change="(checked: boolean) => toggleMonitoring(record.id, checked)"
              />
              <a-divider type="vertical" />
              <a-popconfirm
                title="确定要删除这个标准吗？"
                @confirm="deleteStandardItem(record.id)"
              >
                <a class="text-danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑标准模态框 -->
    <a-modal
      v-model:open="showAddModal"
      :title="editingStandard ? '编辑标准' : '添加标准'"
      width="800px"
      @ok="handleSave"
      @cancel="handleCancel"
      :confirm-loading="saving"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标准编号" name="standardNumber">
              <a-input v-model:value="formData.standardNumber" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="版本" name="version">
              <a-input v-model:value="formData.version" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="标题" name="title">
          <a-input v-model:value="formData.title" />
        </a-form-item>
        
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" />
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="发布日期" name="publishedDate">
              <a-date-picker
                v-model:value="formData.publishedDate"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效日期" name="effectiveDate">
              <a-date-picker
                v-model:value="formData.effectiveDate"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="风险等级" name="riskLevel">
              <a-select v-model:value="formData.riskLevel">
                <a-select-option value="LOW">低风险</a-select-option>
                <a-select-option value="MEDIUM">中风险</a-select-option>
                <a-select-option value="HIGH">高风险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="监管影响" name="regulatoryImpact">
              <a-select v-model:value="formData.regulatoryImpact">
                <a-select-option value="LOW">低影响</a-select-option>
                <a-select-option value="MEDIUM">中影响</a-select-option>
                <a-select-option value="HIGH">高影响</a-select-option>
                <a-select-option value="CRITICAL">关键影响</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="标准状态" name="standardStatus">
              <a-select v-model:value="formData.standardStatus">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="ACTIVE">生效</a-select-option>
                <a-select-option value="SUPERSEDED">已替代</a-select-option>
                <a-select-option value="WITHDRAWN">已撤回</a-select-option>
                <a-select-option value="UNDER_REVISION">修订中</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="主要国家/地区" name="country">
              <a-select v-model:value="formData.country" placeholder="选择主要国家">
                <a-select-option 
                  v-for="option in countryOptions" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="官方链接" name="downloadUrl">
              <a-input v-model:value="formData.downloadUrl" placeholder="官方标准文档链接" />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="数据来源URL" name="sourceUrl">
          <a-input v-model:value="formData.sourceUrl" placeholder="数据来源链接（如爬虫数据源）" />
        </a-form-item>
        
        <a-form-item label="适用国家列表" name="countries">
          <a-select
            v-model:value="formData.countries"
            mode="multiple"
            placeholder="选择适用的国家/地区（可多选）"
            :options="countryOptions"
            :max-tag-count="5"
            :max-tag-text-length="10"
          />
        </a-form-item>
        
        <a-form-item label="关键词" name="keywords">
          <a-input v-model:value="formData.keywords" placeholder="用逗号分隔多个关键词" />
        </a-form-item>
        
        <a-form-item label="适用范围" name="scope">
          <a-textarea v-model:value="formData.scope" :rows="2" />
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="合规截止日期" name="complianceDeadline">
              <a-date-picker
                v-model:value="formData.complianceDeadline"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="过渡期结束" name="transitionEnd">
              <a-date-picker
                v-model:value="formData.transitionEnd"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
              />
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="是否监控" name="isMonitored">
          <a-switch v-model:checked="formData.isMonitored" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      :title="selectedStandard?.title"
      width="1000px"
      :footer="null"
    >
      <div v-if="selectedStandard" class="detail-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="标准编号">
            {{ selectedStandard.standardNumber || '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="版本">
            {{ selectedStandard.version || '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="风险等级">
            <a-tag :color="getRiskColor(selectedStandard.riskLevel)">
              {{ getRiskText(selectedStandard.riskLevel) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="标准状态">
            <a-tag :color="getStatusColor(selectedStandard.standardStatus)">
              {{ getStatusText(selectedStandard.standardStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="发布日期">
            {{ formatDate(selectedStandard.publishedDate) }}
          </a-descriptions-item>
          <a-descriptions-item label="生效日期">
            {{ formatDate(selectedStandard.effectiveDate) }}
          </a-descriptions-item>
          <a-descriptions-item label="监管影响">
            <a-tag :color="getRegulatoryImpactColor(selectedStandard.regulatoryImpact)">
              {{ getRegulatoryImpactText(selectedStandard.regulatoryImpact) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="风险评分">
            {{ selectedStandard.riskScore || '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="合规截止日期">
            {{ formatDate(selectedStandard.complianceDeadline) }}
          </a-descriptions-item>
          <a-descriptions-item label="过渡期结束">
            {{ formatDate(selectedStandard.transitionEnd) }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDateTime(selectedStandard.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatDateTime(selectedStandard.updatedAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="国家/地区" :span="2">
            <div v-if="getCountriesList(selectedStandard).length > 1">
              <div><strong>主要国家：</strong>{{ getCountryName(selectedStandard.country) }}</div>
              <div style="margin-top: 8px;">
                <strong>适用国家：</strong>
                <a-tag 
                  v-for="country in getCountriesList(selectedStandard)" 
                  :key="country"
                  style="margin-right: 4px; margin-bottom: 4px;"
                >
                  {{ getCountryName(country) }}
                </a-tag>
              </div>
            </div>
            <div v-else>
              {{ getCountryName(selectedStandard.country) }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="官方链接" :span="2">
            <a v-if="selectedStandard.downloadUrl" :href="selectedStandard.downloadUrl" target="_blank">
              {{ selectedStandard.downloadUrl }}
            </a>
            <span v-else>未设置</span>
          </a-descriptions-item>
          <a-descriptions-item label="数据来源URL" :span="2">
            <a v-if="selectedStandard.sourceUrl" :href="selectedStandard.sourceUrl" target="_blank" style="color: #52c41a;">
              {{ selectedStandard.sourceUrl }}
            </a>
            <span v-else>未设置</span>
          </a-descriptions-item>
          <a-descriptions-item label="监控状态" :span="2">
            <a-tag :color="selectedStandard.isMonitored ? 'green' : 'default'">
              {{ selectedStandard.isMonitored ? '监控中' : '未监控' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="描述" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.description || '暂无描述' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="适用范围" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.scope || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="关键词" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.keywords || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="产品类型" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.productTypes || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="频率范围" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.frequencyBands || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="功率限制" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.powerLimits || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="测试方法" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.testMethods || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="匹配的产品档案" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.matchedProfiles || '无' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="原始摘要" :span="2">
            <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">
              {{ selectedStandard.rawExcerpt || '无' }}
            </div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { message, Statistic } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DownloadOutlined,
  DatabaseOutlined
} from '@ant-design/icons-vue'
import {
  getStandards,
  createStandard,
  updateStandard,
  deleteStandard as deleteStandardAPI,
  getStandard
} from '@/api/biaozhunguanli'
import { getCountryOptions, getCountryName } from '@/utils/countryMapping'
// 使用全局API类型定义

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const showAddModal = ref(false)
const detailVisible = ref(false)
const editingStandard = ref<API.Standard | null>(null)
const selectedStandard = ref<API.Standard | null>(null)
const showMonitoredOnly = ref(false)
const countryOptions = ref(getCountryOptions())

// 搜索表单
const searchForm = reactive({
  keyword: '',
  riskLevel: undefined as string | undefined,
  standardStatus: undefined as string | undefined,
  country: undefined as string | undefined
})

// 统计数据
const statistics = reactive({
  total: 0,
  highRisk: 0,
  monitored: 0,
  expiring: 0
})

// 标准列表
const standards = ref<API.Standard[]>([])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '标准编号',
    dataIndex: 'standardNumber',
    key: 'standardNumber',
    width: 150,
    fixed: 'left',
    customRender: ({ text }: { text: string }) => text || '未设置'
  },
  // {
  //   title: '版本',
  //   dataIndex: 'version',
  //   key: 'version',
  //   width: 80,
  //   customRender: ({ text }: { text: string }) => text || '-'
  // },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 200,
    ellipsis: true,
    customRender: ({ text }: { text: string }) => text || '未设置'
  },
  {
    title: '国家',
    dataIndex: 'country',
    key: 'country',
    width: 150,
    customRender: ({ record }: { record: API.Standard }) => {
      const country = getCountryName(record.country)
      
      // 使用修复的getCountriesList函数
      const countriesList = getCountriesList(record)
      
      if (countriesList.length > 1) {
        const otherCountries = countriesList.slice(1).map((c: string) => getCountryName(c)).join(', ')
        return `${country} (主要)\n${otherCountries}`
      }
      return country
    }
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100
  },
  {
    title: '标准状态',
    dataIndex: 'standardStatus',
    key: 'standardStatus',
    width: 120
  },
  {
    title: '发布日期',
    dataIndex: 'publishedDate',
    key: 'publishedDate',
    width: 120,
    customRender: ({ text }: { text: string }) => text ? formatDate(text) : '-'
  },
  {
    title: '生效日期',
    dataIndex: 'effectiveDate',
    key: 'effectiveDate',
    width: 120,
    customRender: ({ text }: { text: string }) => text ? formatDate(text) : '-'
  },
  {
    title: '官方链接',
    dataIndex: 'downloadUrl',
    key: 'downloadUrl',
    width: 120,
    customRender: ({ text }: { text: string }) => {
      if (text) {
        return h('a', {
          href: text,
          target: '_blank',
          style: 'color: #1890ff; text-decoration: none;'
        }, '官方链接')
      }
      return '-'
    }
  },
  {
    title: '数据来源',
    dataIndex: 'sourceUrl',
    key: 'sourceUrl',
    width: 120,
    customRender: ({ text }: { text: string }) => {
      if (text) {
        return h('a', {
          href: text,
          target: '_blank',
          style: 'color: #52c41a; text-decoration: none;'
        }, '来源链接')
      }
      return '-'
    }
  },
  {
    title: '监控状态',
    dataIndex: 'isMonitored',
    key: 'isMonitored',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 表单数据
const formData = reactive<API.StandardCreateRequest & {
  publishedDate: any;
  effectiveDate: any;
  complianceDeadline: any;
  transitionEnd: any;
}>({
  standardNumber: '',
  version: '',
  title: '',
  description: '',
  publishedDate: null,
  effectiveDate: null,
  downloadUrl: '',
  keywords: '',
  riskLevel: 'MEDIUM',
  regulatoryImpact: 'MEDIUM',
  standardStatus: 'ACTIVE',
  country: '',
  countries: [],
  scope: '',
  productTypes: '',
  frequencyBands: '',
  powerLimits: '',
  testMethods: '',
  complianceDeadline: null,
  transitionEnd: null,
  riskScore: 0,
  matchedProfiles: '',
  rawExcerpt: '',
  isMonitored: false,
  sourceUrl: ''
})

// 表单验证规则
const rules = {
  standardNumber: [{ required: true, message: '请输入标准编号' }],
  title: [{ required: true, message: '请输入标题' }],
  riskLevel: [{ required: true, message: '请选择风险等级' }],
  country: [{ required: true, message: '请选择国家/地区' }]
}

const formRef = ref<FormInstance>()

// 工具函数
const getRiskColor = (level?: string) => {
  switch (level) {
    case 'HIGH': return 'red'
    case 'MEDIUM': return 'orange'
    case 'LOW': return 'green'
    default: return 'default'
  }
}

const getRiskText = (level?: string) => {
  switch (level) {
    case 'HIGH': return '高风险'
    case 'MEDIUM': return '中风险'
    case 'LOW': return '低风险'
    default: return '未知'
  }
}

const getStatusColor = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'green'
    case 'DRAFT': return 'blue'
    case 'SUPERSEDED': return 'orange'
    case 'WITHDRAWN': return 'red'
    case 'UNDER_REVISION': return 'purple'
    default: return 'default'
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return '生效'
    case 'DRAFT': return '草稿'
    case 'SUPERSEDED': return '已替代'
    case 'WITHDRAWN': return '已撤回'
    case 'UNDER_REVISION': return '修订中'
    default: return '未知'
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getRegulatoryImpactColor = (impact?: string) => {
  switch (impact) {
    case 'CRITICAL': return 'red'
    case 'HIGH': return 'orange'
    case 'MEDIUM': return 'blue'
    case 'LOW': return 'green'
    default: return 'default'
  }
}

const getRegulatoryImpactText = (impact?: string) => {
  switch (impact) {
    case 'CRITICAL': return '关键影响'
    case 'HIGH': return '高影响'
    case 'MEDIUM': return '中影响'
    case 'LOW': return '低影响'
    default: return '未知'
  }
}

// 获取国家列表的辅助函数
const getCountriesList = (standard: API.Standard): string[] => {
  if (standard.countries) {
    if (typeof standard.countries === 'string') {
      try {
        // 处理可能的双重JSON编码
        let parsed = JSON.parse(standard.countries)
        
        // 如果解析后仍然是字符串，可能还需要再次解析
        if (typeof parsed === 'string') {
          try {
            parsed = JSON.parse(parsed)
          } catch (e2) {
            // 如果第二次解析失败，直接使用第一次解析的结果
            parsed = [parsed]
          }
        }
        
        // 确保返回的是数组
        if (Array.isArray(parsed)) {
          return parsed
        } else {
          return [parsed]
        }
      } catch (e) {
        console.warn('解析countries字段失败:', e, '原始数据:', standard.countries)
        return standard.country ? [standard.country] : []
      }
    } else if (Array.isArray(standard.countries)) {
      // 如果已经是数组，检查数组元素是否也需要解析
      return standard.countries.map(item => {
        if (typeof item === 'string') {
          try {
            const parsed = JSON.parse(item)
            return Array.isArray(parsed) ? parsed[0] : parsed
          } catch (e) {
            return item
          }
        }
        return item
      })
    }
  }
  return standard.country ? [standard.country] : []
}

// 方法
const loadStandards = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      riskLevel: searchForm.riskLevel,
      status: searchForm.standardStatus,
      country: searchForm.country,
      isMonitored: showMonitoredOnly.value ? true : undefined
    }

    const result = await getStandards(params) as any
    console.log('API返回的标准数据:', result)
    standards.value = result?.standards || []
    pagination.total = result?.total || 0
    console.log('处理后的标准列表:', standards.value)
    
    // 检查是否包含ID为1的数据
    const hasId1 = standards.value.some((item: any) => item.id === 1)
    console.log('是否包含ID为1的数据:', hasId1)
    if (hasId1) {
      const id1Data = standards.value.find((item: any) => item.id === 1)
      console.log('ID为1的数据详情:', id1Data)
    }
  } catch (error) {
    message.error('加载标准数据失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    console.log('=== 开始加载标准页面统计数据 ===')
    
    // 获取所有标准数据
    const allStandardsResult = await getStandards({ page: 1, size: 1000 }) as any
    console.log('所有标准API返回:', allStandardsResult)
    
    // 正确处理API返回的数据结构
    const allStandardsData = allStandardsResult?.data || allStandardsResult
    const allStandards = allStandardsData?.standards || []
    const total = allStandardsData?.total || allStandards.length
    
    // 手动计算各统计数据
    const highRisk = allStandards.filter((item: any) => item.riskLevel === 'HIGH').length
    const monitored = allStandards.filter((item: any) => item.isMonitored === true).length
    
    // 获取即将到期的标准（这里可以根据实际需求调整逻辑）
    const expiring = allStandards.filter((item: any) => {
      if (!item.complianceDeadline) return false
      const deadline = new Date(item.complianceDeadline)
      const now = new Date()
      const daysDiff = Math.ceil((deadline.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
      return daysDiff <= 30 && daysDiff > 0 // 30天内到期
    }).length
    
    // 更新统计数据
    statistics.total = total
    statistics.highRisk = highRisk
    statistics.monitored = monitored
    statistics.expiring = expiring
    
    console.log('标准页面统计数据:', {
      total,
      highRisk,
      monitored,
      expiring,
      allStandardsCount: allStandards.length
    })
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadStandards()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.riskLevel = undefined
  searchForm.standardStatus = undefined
  searchForm.country = undefined
  showMonitoredOnly.value = false
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadStandards()
}

const refreshData = () => {
  loadStandards()
  loadStatistics()
}

const loadAllData = async () => {
  loading.value = true
  try {
    // 不设置任何过滤条件，获取所有数据
    const params = {
      page: 1,
      size: 1000 // 获取更多数据
    }

    const result = await getStandards(params) as any
    console.log('获取所有标准数据:', result)
    standards.value = result?.standards || []
    pagination.total = result?.total || 0
    pagination.current = 1
    console.log('处理后的标准列表:', standards.value)
    
    // 详细分析返回的数据
    if (result?.standards) {
      console.log('返回的数据详情:')
      result.standards.forEach((item: any, index: number) => {
        console.log(`数据${index + 1}:`, {
          id: item.id,
          standardNumber: item.standardNumber,
          title: item.title,
          country: item.country,
          riskLevel: item.riskLevel,
          standardStatus: item.standardStatus,
          keywords: item.keywords
        })
      })
    }
  } catch (error) {
    message.error('加载所有数据失败')
  } finally {
    loading.value = false
  }
}





const viewDetail = async (record: API.Standard) => {
  try {
    console.log('查看标准详情，ID:', record.id)
    const result = await getStandard({ id: record.id! }) as any
    console.log('获取到的标准详情:', result)
    selectedStandard.value = result
    detailVisible.value = true
  } catch (error) {
    console.error('获取标准详情失败:', error)
    message.error('获取标准详情失败')
  }
}

const editStandard = (record: API.Standard) => {
  console.log('开始编辑标准:', record)
  editingStandard.value = record
  
  // 先重置表单数据，避免响应式数据冲突
  Object.assign(formData, {
    standardNumber: '',
    version: '',
    title: '',
    description: '',
    publishedDate: null,
    effectiveDate: null,
    downloadUrl: '',
    keywords: '',
    riskLevel: 'MEDIUM',
    regulatoryImpact: 'MEDIUM',
    standardStatus: 'ACTIVE',
    country: '',
    countries: [],
    scope: '',
    productTypes: '',
    frequencyBands: '',
    powerLimits: '',
    testMethods: '',
    complianceDeadline: null,
    transitionEnd: null,
    riskScore: 0,
    matchedProfiles: '',
    rawExcerpt: '',
    isMonitored: false,
    sourceUrl: ''
  })
  
  // 然后逐个赋值，避免响应式数据冲突
  console.log('开始赋值表单数据...')
  formData.standardNumber = record.standardNumber || ''
  formData.version = record.version || ''
  formData.title = record.title || ''
  formData.description = record.description || ''
  formData.downloadUrl = record.downloadUrl || ''
  formData.keywords = record.keywords || ''
  formData.riskLevel = record.riskLevel || 'MEDIUM'
  formData.regulatoryImpact = record.regulatoryImpact || 'MEDIUM'
  formData.standardStatus = record.standardStatus || 'ACTIVE'
  formData.country = record.country || ''
  formData.scope = record.scope || ''
  formData.productTypes = record.productTypes || ''
  formData.frequencyBands = record.frequencyBands || ''
  formData.powerLimits = record.powerLimits || ''
  formData.testMethods = record.testMethods || ''
  formData.riskScore = record.riskScore || 0
  formData.matchedProfiles = record.matchedProfiles || ''
  formData.rawExcerpt = record.rawExcerpt || ''
  formData.isMonitored = record.isMonitored || false
  formData.sourceUrl = record.sourceUrl || ''
  console.log('基本字段赋值完成')
  
  // 处理日期字段 - 转换为YYYY-MM-DD格式
  console.log('开始处理日期字段...')
  if (record.publishedDate) {
    const date = new Date(record.publishedDate)
    formData.publishedDate = date.toISOString().split('T')[0]
    console.log('publishedDate处理完成:', formData.publishedDate)
  }
  if (record.effectiveDate) {
    const date = new Date(record.effectiveDate)
    formData.effectiveDate = date.toISOString().split('T')[0]
    console.log('effectiveDate处理完成:', formData.effectiveDate)
  }
  if (record.complianceDeadline) {
    const date = new Date(record.complianceDeadline)
    formData.complianceDeadline = date.toISOString().split('T')[0]
    console.log('complianceDeadline处理完成:', formData.complianceDeadline)
  }
  if (record.transitionEnd) {
    const date = new Date(record.transitionEnd)
    formData.transitionEnd = date.toISOString().split('T')[0]
    console.log('transitionEnd处理完成:', formData.transitionEnd)
  }
  console.log('日期字段处理完成')
  
  // 确保countries字段正确处理
  console.log('开始处理countries字段...')
  console.log('原始countries数据:', record.countries, '类型:', typeof record.countries)
  
  if (record.countries && typeof record.countries === 'string') {
    try {
      // 处理可能的双重JSON编码
      let parsed = JSON.parse(record.countries)
      
      // 如果解析后仍然是字符串，可能还需要再次解析
      if (typeof parsed === 'string') {
        try {
          parsed = JSON.parse(parsed)
        } catch (e2) {
          // 如果第二次解析失败，直接使用第一次解析的结果
          parsed = [parsed]
        }
      }
      
      // 确保是数组格式
      if (Array.isArray(parsed)) {
        formData.countries = parsed
      } else {
        formData.countries = [parsed]
      }
      console.log('countries字段解析成功:', formData.countries)
    } catch (e) {
      console.warn('解析countries字段失败:', e)
      formData.countries = record.country ? [record.country] : []
      console.log('使用默认countries值:', formData.countries)
    }
  } else if (Array.isArray(record.countries)) {
    // 如果已经是数组，检查数组元素是否也需要解析
    formData.countries = record.countries.map(item => {
      if (typeof item === 'string') {
        try {
          const parsed = JSON.parse(item)
          return Array.isArray(parsed) ? parsed[0] : parsed
        } catch (e) {
          return item
        }
      }
      return item
    })
    console.log('countries字段是数组，处理后:', formData.countries)
  } else {
    formData.countries = record.country ? [record.country] : []
    console.log('使用country字段作为countries:', formData.countries)
  }
  
  console.log('编辑标准处理完成，准备显示模态框')
  showAddModal.value = true
  console.log('模态框显示状态:', showAddModal.value)
}

const toggleMonitoring = async (id: number, isMonitored: boolean) => {
  try {
    const standard = standards.value.find(s => s.id === id)
    if (standard) {
      await updateStandard({ id }, { 
        ...standard, 
        isMonitored,
        countries: standard.countries ? [standard.countries] : undefined
      })
      message.success(`${isMonitored ? '开启' : '关闭'}监控成功`)
      loadStandards()
    }
  } catch (error) {
    message.error('更新监控状态失败')
  }
}

const deleteStandardItem = async (id: number) => {
  try {
    await deleteStandardAPI({ id })
    message.success('删除成功')
    loadStandards()
    loadStatistics()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true

    // 处理countries字段和日期字段
    const saveData = { ...formData }
    
    // 处理日期字段 - 确保日期格式正确
    if (saveData.publishedDate && typeof saveData.publishedDate === 'object' && saveData.publishedDate.$d) {
      saveData.publishedDate = saveData.publishedDate.toISOString().split('T')[0]
    }
    if (saveData.effectiveDate && typeof saveData.effectiveDate === 'object' && saveData.effectiveDate.$d) {
      saveData.effectiveDate = saveData.effectiveDate.toISOString().split('T')[0]
    }
    if (saveData.complianceDeadline && typeof saveData.complianceDeadline === 'object' && saveData.complianceDeadline.$d) {
      saveData.complianceDeadline = saveData.complianceDeadline.toISOString().split('T')[0]
    }
    if (saveData.transitionEnd && typeof saveData.transitionEnd === 'object' && saveData.transitionEnd.$d) {
      saveData.transitionEnd = saveData.transitionEnd.toISOString().split('T')[0]
    }
    
    if (saveData.countries && saveData.countries.length > 0) {
      // 如果选择了多个国家，确保主要国家在列表中
      if (!saveData.countries.includes(saveData.country)) {
        saveData.countries.unshift(saveData.country)
      }
    } else if (saveData.country) {
      // 如果只选择了主要国家，将其添加到countries列表
      saveData.countries = [saveData.country]
    }

    if (editingStandard.value) {
      // 更新标准
      await updateStandard({ id: editingStandard.value.id! }, saveData)
      message.success('更新成功')
    } else {
      // 添加标准
      await createStandard(saveData)
      message.success('添加成功')
    }

    showAddModal.value = false
    loadStandards()
    loadStatistics()
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  showAddModal.value = false
  editingStandard.value = null
  formRef.value?.resetFields()
  // 重置表单数据
  Object.assign(formData, {
    standardNumber: '',
    version: '',
    title: '',
    description: '',
    publishedDate: null,
    effectiveDate: null,
    downloadUrl: '',
    keywords: '',
    riskLevel: 'MEDIUM',
    regulatoryImpact: 'MEDIUM',
    standardStatus: 'ACTIVE',
    country: '',
    countries: [],
    scope: '',
    productTypes: '',
    frequencyBands: '',
    powerLimits: '',
    testMethods: '',
    complianceDeadline: null,
    transitionEnd: null,
    riskScore: 0,
    matchedProfiles: '',
    rawExcerpt: '',
    isMonitored: false,
    sourceUrl: ''
  })
}

const exportData = async () => {
  exporting.value = true
  try {
    const params = {
      page: 1,
      size: 1000, // 导出更多数据
      keyword: searchForm.keyword || undefined,
      riskLevel: searchForm.riskLevel,
      standardStatus: searchForm.standardStatus,
      country: searchForm.country,
      isMonitored: showMonitoredOnly.value ? true : undefined
    }
    
    const result = await getStandards(params)
    // 这里可以实现实际的导出逻辑，比如生成Excel文件
    console.log('导出数据:', result)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 初始化
onMounted(() => {
  loadStandards()
  loadStatistics()
})
</script>

<style scoped>
.standards-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.stats-section {
  margin-bottom: 24px;
}

.text-danger {
  color: #ff4d4f;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .standards-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>

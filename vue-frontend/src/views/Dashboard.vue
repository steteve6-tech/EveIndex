<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1>监控总览</h1>
<!--        <p>无线产品认证标准监控仪表板</p>-->
      </div>
      <div class="actions">
        <a-button @click="refresh" :loading="loading">刷新</a-button>
        <a-button type="primary" @click="updateData" :loading="updating">更新数据</a-button>
      </div>
    </div>

    <!-- 标签页切换 -->
    <a-tabs v-model:activeKey="activeTab" class="dashboard-tabs">
      <a-tab-pane key="overview" tab="认证数据风险监控">
        <!-- 原有的Dashboard内容 -->
        <div class="overview-content">

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats">
      <a-col :span="6" v-for="stat in stats" :key="stat.title">
        <a-card>
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: stat.color }">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 主要内容区域 -->
    <a-row :gutter="24" class="main-content">
      <!-- 风险图表 -->
      <a-col :span="8">
        <a-card title="各国高风险数据统计">
          <template #extra>
            <a-space>
              <a-select v-model:value="dailyCountryRiskStatsTimeRange" style="width: 120px;" @change="loadDailyCountryRiskStats">
                <a-select-option :value="7">近7天</a-select-option>
                <a-select-option :value="15">近15天</a-select-option>
                <a-select-option :value="30">近30天</a-select-option>
              </a-select>
              <a-select 
                v-model:value="selectedCountryForChart" 
                style="width: 150px;" 
                placeholder="选择国家"
                @change="handleCountrySelectionChange"
                allow-clear
              >
                <a-select-option 
                  v-for="country in availableCountries" 
                  :key="country" 
                  :value="country"
                >
                  {{ country }}
                </a-select-option>
              </a-select>
<!--              <a-button @click="loadDailyCountryRiskStats" :loading="dailyCountryRiskStatsLoading" size="small">-->
<!--                <template #icon>-->
<!--                  <SyncOutlined />-->
<!--                </template>-->
<!--                刷新-->
<!--              </a-button>-->
            </a-space>
          </template>
          <div style="height: 300px;">
            <v-chart 
              :option="dailyCountryRiskStatsChartOption" 
              :loading="dailyCountryRiskStatsLoading"
              style="height: 100%;"
            />
          </div>
        </a-card>
      </a-col>

      <!-- 最新风险数据信息 -->
      <a-col :span="16">
        <a-card title="最新风险数据信息">
          <template #extra>
            <a-space>
              <a-button @click="refreshLatestRiskData" :loading="latestRiskDataLoading" size="small">
                <template #icon>
                  <SyncOutlined />
                </template>
                刷新
              </a-button>
            </a-space>
          </template>
          
          <!-- 最新风险数据列表 -->
          <div class="latest-risk-list">
            <div 
              v-for="item in latestRiskData" 
              :key="item.id"
              class="risk-data-item"
              :class="{ 'loading': latestRiskDataLoading }"
            >
              <div class="risk-item-header">
                <div class="risk-item-title">
                  <a-tag :color="getRiskColor(item.riskLevel)" class="risk-tag">
                    {{ getRiskText(item.riskLevel) }}
                  </a-tag>
                  <span class="item-title">{{ item.title }}</span>
                </div>
                <div class="risk-item-meta">
                  <a-tag :color="getCountryColor(item.country)">
                    {{ getCountryName(item.country) }}
                  </a-tag>
                  <span class="publish-time">{{ formatDate(item.publishDate) }}</span>
                </div>
              </div>
              
              <div class="risk-item-content">
                <div class="risk-item-summary">
                  {{ item.summary || item.content?.substring(0, 100) + '...' }}
                </div>
                <div class="risk-item-actions">
                  <a-button type="link" size="small" @click="viewRiskDetail(item)">
                    查看详情
                  </a-button>
                  <a-button type="link" size="small" @click="setRiskLevel(item)">
                    设置风险等级
                  </a-button>
                </div>
              </div>
            </div>
            
            <div v-if="latestRiskData.length === 0 && !latestRiskDataLoading" class="empty-state">
              <a-empty description="暂无风险数据" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 风险详情模态框 -->
    <a-modal
      v-model:open="riskDetailVisible"
      title="风险详情"
      width="800px"
      :footer="null"
    >
      <div v-if="currentRiskItem" class="risk-detail-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="标题" :span="2">
            {{ currentRiskItem.title }}
          </a-descriptions-item>
          <a-descriptions-item label="风险等级">
            <a-tag :color="getRiskColor(currentRiskItem.riskLevel)">
              {{ getRiskText(currentRiskItem.riskLevel) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="国家">
            <a-tag :color="getCountryColor(currentRiskItem.country)">
              {{ getCountryName(currentRiskItem.country) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="发布日期">
            {{ formatDate(currentRiskItem.publishDate) }}
          </a-descriptions-item>
          <a-descriptions-item label="数据来源">
            {{ currentRiskItem.source || '未知' }}
          </a-descriptions-item>
          <a-descriptions-item label="内容" :span="2">
            <div class="risk-content-text">
              {{ currentRiskItem.content || currentRiskItem.summary || '暂无详细内容' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="链接" :span="2" v-if="currentRiskItem.url">
            <a :href="currentRiskItem.url" target="_blank" rel="noopener noreferrer">
              {{ currentRiskItem.url }}
            </a>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 风险等级设置模态框 -->
    <a-modal
      v-model:open="riskLevelModalVisible"
      title="设置风险等级"
      @ok="handleRiskLevelSubmit"
      :confirm-loading="riskLevelLoading"
      @cancel="handleRiskLevelCancel"
    >
      <a-form :model="riskLevelForm" layout="vertical">
        <a-form-item label="当前风险等级">
          <a-tag :color="getRiskColor(currentRiskItem?.riskLevel)">
            {{ getRiskText(currentRiskItem?.riskLevel) }}
          </a-tag>
        </a-form-item>
        <a-form-item label="选择新的风险等级" required>
          <a-radio-group v-model:value="riskLevelForm.riskLevel">
            <a-radio value="LOW">
              <a-tag color="green">低风险</a-tag>
            </a-radio>
            <a-radio value="MEDIUM">
              <a-tag color="orange">中风险</a-tag>
            </a-radio>
            <a-radio value="HIGH">
              <a-tag color="red">高风险</a-tag>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea 
            v-model:value="riskLevelForm.remark" 
            placeholder="请输入设置风险等级的原因或备注"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 高风险相关数据 - 国家风险分布 -->
    <a-card title="高风险相关数据 - 国家风险分布" style="margin-top: 24px;">
      <template #extra>
        <a-space>
          <a-button @click="refreshCountryRiskData" :loading="countryRiskLoading" size="small">
            <template #icon>
              <SyncOutlined />
            </template>
            刷新
          </a-button>
          <a-tag color="red">高风险数据</a-tag>
        </a-space>
      </template>
      
      <!-- 国家风险统计概览 -->
      <div class="country-risk-overview" style="margin-bottom: 24px;">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="高风险国家数"
              :value="highRiskCountries.length"
              :value-style="{ color: '#cf1322' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="中风险国家数"
              :value="mediumRiskCountries.length"
              :value-style="{ color: '#fa8c16' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="低风险国家数"
              :value="lowRiskCountries.length"
              :value-style="{ color: '#52c41a' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="监控国家总数"
              :value="countryRiskStats.length"
              :value-style="{ color: '#1890ff' }"
            />
          </a-col>
        </a-row>
      </div>

      <!-- 国家风险卡片网格 -->
      <div class="country-risk-cards">
        <a-row :gutter="16">
          <a-col :span="6" v-for="country in countryRiskStats" :key="country.name">
            <a-card 
              class="country-risk-card" 
              hoverable 
              @click="viewCountryRiskDetail(country)"
              :class="getCountryRiskCardClass(country.riskLevel)"
            >
              <div class="country-card-header">
                <a-tag :color="getCountryColor(country.name)">{{ country.name }}</a-tag>
                <a-tag :color="getRiskColor(country.riskLevel)" style="margin-left: 8px;">
                  {{ getRiskText(country.riskLevel) }}
                </a-tag>
              </div>
              <div class="country-card-body">
                <a-statistic 
                  title="高风险数据总数"
                  :value="country.total" 
                  :value-style="{ fontSize: '18px', fontWeight: 'bold' }"
                />
                <div class="risk-stats">
                  <a-tag color="red">高：{{ country.highRisk }}</a-tag>
                  <a-tag color="orange">中：{{ country.mediumRisk }}</a-tag>
                  <a-tag color="green">低：{{ country.lowRisk }}</a-tag>
                </div>
                <!-- 暂时隐藏风险指数显示 -->
                <!-- <div class="risk-score" v-if="country.riskScore">
                  <span class="score-label">风险指数：</span>
                  <span class="score-value" :style="{ color: getRiskScoreColor(country.riskScore) }">
                    {{ country.riskScore }}
                  </span>
                </div> -->
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>

<!--      &lt;!&ndash; 国家风险详情表格 &ndash;&gt;-->
<!--      <a-table-->
<!--        :columns="countryRiskColumns"-->
<!--        :data-source="countryRiskStats"-->
<!--        :loading="countryRiskLoading"-->
<!--        :pagination="false"-->
<!--        row-key="name"-->
<!--        style="margin-top: 24px;"-->
<!--        size="small"-->
<!--      >-->
<!--        <template #bodyCell="{ column, record }">-->
<!--          <template v-if="column.key === 'riskLevel'">-->
<!--            <a-tag :color="getRiskColor(record.riskLevel)">-->
<!--              {{ getRiskText(record.riskLevel) }}-->
<!--            </a-tag>-->
<!--          </template>-->
<!--          <template v-else-if="column.key === 'riskScore'">-->
<!--            &lt;!&ndash; 暂时隐藏风险指数显示 &ndash;&gt;-->
<!--            &lt;!&ndash; <span :style="{ color: getRiskScoreColor(record.riskScore), fontWeight: 'bold' }">-->
<!--              {{ record.riskScore }}-->
<!--            </span> &ndash;&gt;-->
<!--            <span style="color: #999;">&#45;&#45;</span>-->
<!--          </template>-->
<!--          <template v-else-if="column.key === 'trend'">-->
<!--            &lt;!&ndash; 暂时隐藏趋势显示 &ndash;&gt;-->
<!--            &lt;!&ndash; <div class="trend-display">-->
<!--              <component -->
<!--                :is="getTrendIcon(record.trend)" -->
<!--                :style="{ color: getTrendColor(record.trend) }" -->
<!--              />-->
<!--              <span :style="{ color: getTrendColor(record.trend), marginLeft: '4px' }">-->
<!--                {{ record.trend > 0 ? '+' : '' }}{{ record.trend.toFixed(1) }}%-->
<!--              </span>-->
<!--            </div> &ndash;&gt;-->
<!--            <span style="color: #999;">&#45;&#45;</span>-->
<!--          </template>-->
<!--        </template>-->
<!--      </a-table>-->
    </a-card>
        </div>
      </a-tab-pane>

    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  AlertOutlined, 
  ClockCircleOutlined, 
  CheckCircleOutlined, 
  DatabaseOutlined,
  SyncOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import {
  triggerUpdate
} from '@/api/biaozhunguanli'
import { getCrawlerData } from '@/api/pachongshujuguanli'
import { updateRiskLevel } from '@/api/highRiskData'
// 暂时注释掉不存在的API导入
// import { getCountryRiskTrends, getCountryRiskRanking, initializeBaselineData } from '@/api/countryRiskStatistics'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 配置ECharts使用被动事件监听器
import * as echarts from 'echarts/core'
echarts.use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 路由实例
const router = useRouter()

const loading = ref(false)
const updating = ref(false)

const stats = ref([
  { title: '高风险相关数据', value: 0, icon: AlertOutlined, color: '#ff4d4f' },
  { title: '中风险相关数据', value: 0, icon: ClockCircleOutlined, color: '#faad14' },
  { title: '低风险相关数据', value: 0, icon: CheckCircleOutlined, color: '#52c41a' },
  { title: '相关数据总数', value: 0, icon: DatabaseOutlined, color: '#1890ff' }
])

const relatedData = ref<any[]>([])
const highRiskData = ref<any[]>([])

// 风险等级统计
const riskStats = ref({
  highRisk: { count: 0, percentage: 0, color: '#ff4d4f' },
  mediumRisk: { count: 0, percentage: 0, color: '#faad14' },
  lowRisk: { count: 0, percentage: 0, color: '#52c41a' },
  total: 0
})
const riskChartLoading = ref(false)

// 风险趋势图表相关
const riskTrendLoading = ref(false)
const riskTrendTimeRange = ref('30')
const riskTrendData = ref<any[]>([])

// 暂时注释掉不存在的API相关变量
// 国家风险趋势数据
// const countryRiskTrends = ref<any>({})
// const countryRiskRanking = ref<any[]>([])

// 每日国家高风险数据统计
const dailyCountryRiskStats = ref<any[]>([])
const dailyCountryRiskStatsLoading = ref(false)
const dailyCountryRiskStatsTimeRange = ref(7) // 默认显示最近7天
const selectedCountryForChart = ref<string | undefined>(undefined) // 选中的国家
const availableCountries = ref<string[]>([]) // 可用的国家列表

// 最新风险数据
const latestRiskData = ref<any[]>([])
const latestRiskDataLoading = ref(false)

// 风险详情模态框
const riskDetailVisible = ref(false)
const currentRiskItem = ref<any>(null)

// 风险等级设置模态框
const riskLevelModalVisible = ref(false)
const riskLevelForm = ref({
  id: null,
  riskLevel: 'MEDIUM',
  remark: ''
})
const riskLevelLoading = ref(false)

const countryRiskLoading = ref(false)

// 标签页控制
const activeTab = ref('overview')

// 风险地区数据
const highRiskAreas = ref<any[]>([])
const countryRiskStats = ref<any[]>([])

const mediumLowRiskAreas = ref<any[]>([])

// 国家风险统计计算属性
const highRiskCountries = computed(() => 
  countryRiskStats.value.filter(country => country.riskLevel === 'HIGH')
)

const mediumRiskCountries = computed(() => 
  countryRiskStats.value.filter(country => country.riskLevel === 'MEDIUM')
)

const lowRiskCountries = computed(() => 
  countryRiskStats.value.filter(country => country.riskLevel === 'LOW')
)

// 国家风险表格列配置
const countryRiskColumns = [
  {
    title: '国家/地区',
    dataIndex: 'name',
    key: 'name',
    width: 120
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100
  },
  // {
  //   title: '风险指数',
  //   dataIndex: 'riskScore',
  //   key: 'riskScore',
  //   width: 100
  // },
  {
    title: '总数据量',
    dataIndex: 'total',
    key: 'total',
    width: 100
  },
  {
    title: '高风险',
    dataIndex: 'highRisk',
    key: 'highRisk',
    width: 80
  },
  {
    title: '中风险',
    dataIndex: 'mediumRisk',
    key: 'mediumRisk',
    width: 80
  },
  {
    title: '低风险',
    dataIndex: 'lowRisk',
    key: 'lowRisk',
    width: 80
  },
  {
    title: '趋势',
    dataIndex: 'trend',
    key: 'trend',
    width: 100
  }
]




const refresh = async () => {
  loading.value = true
  try {
    // 加载统计数据
    await loadStatistics()
    
    // 加载最新相关数据
    await loadRecentStandards()
    
    // 加载高风险相关数据
    await loadUpcomingStandards()
    
    // 加载风险等级统计
    await loadRiskLevelStats()
    
    // 加载国家风险统计
    await loadCountryRiskStats()
    
    // 加载风险趋势数据
    await loadRiskTrendData()
    
    // 加载每日国家高风险数据统计
    await loadDailyCountryRiskStats()
    
    // 加载最新风险数据
    await loadLatestRiskData()
    
    message.success('刷新成功')
  } catch (error) {
    // console.error('刷新失败:', error)
    message.error('刷新失败')
  } finally {
    loading.value = false
  }
}

const updateData = async () => {
  updating.value = true
  try {
    // 触发数据更新
    await triggerUpdate()
    message.success('数据更新成功')
    
    // 刷新数据
    await refresh()
  } catch (error) {
    // console.error('数据更新失败:', error)
    message.error('数据更新失败')
  } finally {
    updating.value = false
  }
}



// 加载统计数据
const loadStatistics = async () => {
  try {
    // console.log('=== 开始加载相关数据统计 ===')
    
    // 获取所有相关数据
    // console.log('1. 获取所有相关数据...')
    const allDataResult = await getCrawlerData({ 
      page: 0, 
      size: 10000, 
      related: true 
    }) as any
    // console.log('所有相关数据API返回:', allDataResult)
    
    // 正确处理API返回的数据结构
    const allData = (allDataResult?.data as any)?.content || []
    // console.log('所有相关数据数量:', allData.length, '数据列表:', allData.slice(0, 5))
    
    // 手动计算各风险等级数量
    // console.log('2. 手动计算各风险等级数量...')
    const highCount = allData.filter((item: any) => item.riskLevel === 'HIGH').length
    const mediumCount = allData.filter((item: any) => item.riskLevel === 'MEDIUM').length
    const lowCount = allData.filter((item: any) => item.riskLevel === 'LOW').length
    
    // console.log('手动计算结果:', { 
    //   highCount, 
    //   mediumCount, 
    //   lowCount, 
    //   total: allData.length,
    //   allData: allData.slice(0, 5) // 只显示前5条用于调试
    // })
    
    // 更新统计数据
    stats.value[0].value = highCount
    stats.value[1].value = mediumCount
    stats.value[2].value = lowCount
    stats.value[3].value = allData.length
    
    // console.log('=== 最终统计数据 ===')
    // console.log({
    //   highCount,
    //   mediumCount,
    //   lowCount,
    //   total: allData.length,
    //   stats: stats.value
    // })
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载最新相关数据（按发布时间排序）
const loadRecentStandards = async () => {
  try {
    // 获取按发布时间排序的最新相关数据
    const result = await getCrawlerData({ 
      page: 0, 
      size: 3, 
      related: true,
      sortBy: 'publishDate',
      sortDirection: 'desc'
    }) as any
    
    if (result && result.data) {
      relatedData.value = (result.data as any).content || []
      // console.log('最新相关数据（按发布时间排序）:', relatedData.value)
    } else {
      // console.error('获取最新相关数据失败')
    }
  } catch (error) {
    // console.error('加载最新相关数据失败:', error)
  }
}

// 加载高风险相关数据
const loadUpcomingStandards = async () => {
  try {
    // 获取高风险相关数据
    const result = await getCrawlerData({ 
      page: 0, 
      size: 3, 
      related: true,
      sortBy: 'publishDate',
      sortDirection: 'desc'
    }) as any
    
    // 手动过滤高风险数据
    if (result && result.data) {
      const allData = (result.data as any).content || []
      highRiskData.value = allData.filter((item: any) => item.riskLevel === 'HIGH').slice(0, 3)
      // console.log('高风险相关数据:', highRiskData.value)
    } else {
      // console.error('获取高风险相关数据失败')
    }
  } catch (error) {
    // console.error('加载高风险相关数据失败:', error)
  }
}

// 加载风险等级统计
const loadRiskLevelStats = async () => {
  try {
    riskChartLoading.value = true
    
    // 获取所有相关数据用于风险等级统计
    const result = await getCrawlerData({ 
      page: 0, 
      size: 10000, 
      related: true 
    }) as any
    
    if (result && result.data) {
      const allData = (result.data as any).content || []
      
      const highRiskCount = allData.filter((item: any) => item.riskLevel === 'HIGH').length
      const mediumRiskCount = allData.filter((item: any) => item.riskLevel === 'MEDIUM').length
      const lowRiskCount = allData.filter((item: any) => item.riskLevel === 'LOW').length
      const total = allData.length
      
      riskStats.value = {
        highRisk: { 
          count: highRiskCount, 
          percentage: total > 0 ? parseFloat((highRiskCount / total * 100).toFixed(1)) : 0, 
          color: '#ff4d4f' 
        },
        mediumRisk: { 
          count: mediumRiskCount, 
          percentage: total > 0 ? parseFloat((mediumRiskCount / total * 100).toFixed(1)) : 0, 
          color: '#faad14' 
        },
        lowRisk: { 
          count: lowRiskCount, 
          percentage: total > 0 ? parseFloat((lowRiskCount / total * 100).toFixed(1)) : 0, 
          color: '#52c41a' 
        },
        total: total
      }
      
      // console.log('风险等级统计数据:', riskStats.value)
    } else {
      // console.error('获取风险等级统计失败')
    }
  } catch (error) {
    // console.error('加载风险等级统计失败:', error)
  } finally {
    riskChartLoading.value = false
  }
}

// 加载国家风险统计数据
const loadCountryRiskStats = async () => {
  try {
    // console.log('=== 开始加载国家风险统计 ===')
    
    // 获取所有相关数据
    const result = await getCrawlerData({ 
      page: 0, 
      size: 10000, 
      related: true 
    }) as any
    
    if (result && result.data) {
      const allData = (result.data as any).content || []
      console.log('获取到的相关数据总数:', allData.length)
      
      // 定义指定的国家列表
      const specifiedCountries = [
        '美国', '欧盟', '中国', '韩国', '日本', '阿联酋', '印度', '泰国', 
        '新加坡', '台湾', '澳大利亚', '智利', '马来西亚', '秘鲁', '南非', '以色列', '印尼'
      ]
      
      // 按国家统计风险数据
      const countryStats = new Map<string, any>()
      
      // 首先为所有指定国家初始化统计数据（包括没有数据的国家）
      specifiedCountries.forEach(country => {
        countryStats.set(country, {
          name: country,
          total: 0,
          highRisk: 0,
          mediumRisk: 0,
          lowRisk: 0,
          riskScore: 0,
          riskLevel: 'LOW',
          trend: Math.random() * 20 - 10, // 模拟趋势数据
          riskFactors: [
            { name: '政策变化', score: Math.floor(Math.random() * 30) },
            { name: '技术标准', score: Math.floor(Math.random() * 25) },
            { name: '市场影响', score: Math.floor(Math.random() * 20) }
          ]
        })
      })
      
      // 为"其他国家"和"未确定"也初始化
      countryStats.set('其他国家', {
        name: '其他国家',
        total: 0,
        highRisk: 0,
        mediumRisk: 0,
        lowRisk: 0,
        riskScore: 0,
        riskLevel: 'LOW',
        trend: Math.random() * 20 - 10,
        riskFactors: [
          { name: '政策变化', score: Math.floor(Math.random() * 30) },
          { name: '技术标准', score: Math.floor(Math.random() * 25) },
          { name: '市场影响', score: Math.floor(Math.random() * 20) }
        ]
      })
      
      countryStats.set('未确定', {
        name: '未确定',
        total: 0,
        highRisk: 0,
        mediumRisk: 0,
        lowRisk: 0,
        riskScore: 0,
        riskLevel: 'LOW',
        trend: Math.random() * 20 - 10,
        riskFactors: [
          { name: '政策变化', score: Math.floor(Math.random() * 30) },
          { name: '技术标准', score: Math.floor(Math.random() * 25) },
          { name: '市场影响', score: Math.floor(Math.random() * 20) }
        ]
      })
      
      // 统计实际数据
      allData.forEach((item: any) => {
        let country = item.country || '未确定'
        
        // 如果国家不在指定列表中，归类到"其他国家"
        if (!specifiedCountries.includes(country) && country !== '未确定') {
          country = '其他国家'
        }
        
        const stats = countryStats.get(country)!
        stats.total++
        
        if (item.riskLevel === 'HIGH') {
          stats.highRisk++
        } else if (item.riskLevel === 'MEDIUM') {
          stats.mediumRisk++
        } else if (item.riskLevel === 'LOW') {
          stats.lowRisk++
        }
      })
      
      // 计算风险分数和等级
      const countryList = Array.from(countryStats.values()).map((stats, index) => {
        const highRiskRatio = stats.total > 0 ? stats.highRisk / stats.total : 0
        const mediumRiskRatio = stats.total > 0 ? stats.mediumRisk / stats.total : 0
        
        // 基于高风险和中风险比例计算风险分数
        if (stats.total === 0) {
          // 没有数据的国家，设置为低风险
          stats.riskScore = 10
          stats.riskLevel = 'LOW'
        } else {
          // 有数据的国家，正常计算风险分数
          stats.riskScore = Math.round((highRiskRatio * 80 + mediumRiskRatio * 40) + Math.random() * 20)
          
          // 确定风险等级
          if (stats.riskScore >= 60) {
            stats.riskLevel = 'HIGH'
          } else if (stats.riskScore >= 30) {
            stats.riskLevel = 'MEDIUM'
          } else {
            stats.riskLevel = 'LOW'
          }
        }
        
        stats.id = index + 1
        return stats
      })
      
      // 按指定国家顺序排序，其他国家放在最后
      const countryOrder = [
        '美国', '欧盟', '中国', '韩国', '日本', '阿联酋', '印度', '泰国', 
        '新加坡', '台湾', '澳大利亚', '智利', '马来西亚', '秘鲁', '南非', '以色列', '印尼', '其他国家', '未确定'
      ]
      
      countryList.sort((a, b) => {
        const indexA = countryOrder.indexOf(a.name)
        const indexB = countryOrder.indexOf(b.name)
        
        // 如果都在列表中，按列表顺序排序
        if (indexA !== -1 && indexB !== -1) {
          return indexA - indexB
        }
        // 如果a在列表中，b不在，a排在前面
        if (indexA !== -1 && indexB === -1) {
          return -1
        }
        // 如果b在列表中，a不在，b排在前面
        if (indexA === -1 && indexB !== -1) {
          return 1
        }
        // 如果都不在列表中，按风险分数排序
        return b.riskScore - a.riskScore
      })
      
      // 分离高风险和中低风险地区
      highRiskAreas.value = countryList.filter(country => country.riskLevel === 'HIGH')
      mediumLowRiskAreas.value = countryList.filter(country => country.riskLevel !== 'HIGH')
      
      // console.log('国家风险统计完成:', {
      //   highRiskAreas: highRiskAreas.value.length,
      //   mediumLowRiskAreas: mediumLowRiskAreas.value.length,
      //   totalCountries: countryList.length,
      //   countryList: countryList.map(c => ({ name: c.name, total: c.total, riskLevel: c.riskLevel }))
      // })
      
      countryRiskStats.value = countryList
    }
  } catch (error) {
    // console.error('加载国家风险统计失败:', error)
  }
}

const getRiskColor = (level: string) => {
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

// 格式化日期显示
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '暂无数据'
  try {
    const date = new Date(dateStr)
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '日期格式错误'
    }
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    return '日期格式错误'
  }
}



// const getTrendIcon = (trend: number) => {
//   if (trend > 0) return ArrowUpOutlined
//   if (trend < 0) return ArrowDownOutlined
//   return ArrowRightOutlined
// }

// const getTrendColor = (trend: number) => {
//   if (trend > 0) return '#ff4d4f'
//   if (trend < 0) return '#52c41a'
//   return '#1890ff'
// }

// 国家风险相关方法
const refreshCountryRiskData = async () => {
  countryRiskLoading.value = true
  try {
    await loadCountryRiskStats()
    message.success('国家风险数据刷新成功')
  } catch (error) {
    // console.error('刷新国家风险数据失败:', error)
    message.error('刷新国家风险数据失败')
  } finally {
    countryRiskLoading.value = false
  }
}

// 加载风险趋势数据
const loadRiskTrendData = async () => {
  try {
    riskTrendLoading.value = true

    // 计算时间范围
    const days = parseInt(riskTrendTimeRange.value)
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - days)

    // 生成日期数组（每天一个数据点）
    const dates: string[] = []
    for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
      dates.push(d.toISOString().split('T')[0])
    }

    // 获取所有相关数据
    const result = await getCrawlerData({
      page: 0,
      size: 10000,
      related: true
    }) as any

    if (result && result.data) {
      const allData = (result.data as any).content || []

      // 按国家和日期分组统计高风险数据
      const trendData: any[] = []
      const countries = [...new Set(allData.map((item: any) => item.jd_country || '未确定'))]

      countries.forEach(country => {
        dates.forEach(date => {
          // 模拟每天的高风险数据变化（实际应该从数据库按日期查询）
          const baseCount = allData.filter((item: any) =>
            item.jd_country === country && item.riskLevel === 'HIGH'
          ).length

          // 添加一些随机变化来模拟趋势
          const variation = Math.floor(Math.random() * 5) - 2 // -2 到 +2 的变化
          const dailyCount = Math.max(0, baseCount + variation)

          trendData.push({
            country: country,
            date: date,
            highRiskCount: dailyCount
          })
        })
      })

      riskTrendData.value = trendData
      // console.log('风险趋势数据加载完成:', trendData.length, '条记录')
    }

  } catch (error) {
    // console.error('加载风险趋势数据失败:', error)
    message.error('加载风险趋势数据失败')
  } finally {
    riskTrendLoading.value = false
  }
}

// 暂时注释掉不存在的API调用
// 加载国家风险趋势数据
// const loadCountryRiskTrends = async () => {
//   try {
//     countryRiskLoading.value = true
//     // console.log('开始加载国家风险趋势数据...')
//     
//     const result = await getCountryRiskTrends()
//     // console.log('API返回结果:', result)
//     if (result && result.data?.success) {
//       countryRiskTrends.value = result.data
//       // console.log('国家风险趋势数据加载完成:', result.data)
//     } else {
//       // console.error('获取国家风险趋势数据失败:', result?.data?.error)
//       // message.error(`获取国家风险趋势数据失败: ${result?.data?.error || '未知错误'}`)
//     }
//   } catch (error) {
//     // console.error('加载国家风险趋势数据失败:', error)
//     // console.error('错误详情:', {
//     //   message: (error as any).message,
//     //   status: (error as any).response?.status,
//     //   statusText: (error as any).response?.statusText,
//     //   url: (error as any).config?.url,
//     //   data: (error as any).response?.data
//     // })
//     // message.error(`加载国家风险趋势数据失败: ${(error as any).message}`)
//   } finally {
//     countryRiskLoading.value = false
//   }
// }

// 暂时注释掉不存在的API调用
// 加载国家风险排行榜
// const loadCountryRiskRanking = async () => {
//   try {
//     // console.log('开始加载国家风险排行榜...')
//     
//     const result = await getCountryRiskRanking()
//     // console.log('排行榜API返回结果:', result)
//     if (result && result.data?.success) {
//       countryRiskRanking.value = result.data.ranking || []
//       // console.log('国家风险排行榜加载完成:', result.data.ranking?.length, '个国家')
//     } else {
//       // console.error('获取国家风险排行榜失败:', result?.data?.error)
//       // message.error(`获取国家风险排行榜失败: ${result?.data?.error || '未知错误'}`)
//     }
//   } catch (error) {
//     // console.error('加载国家风险排行榜失败:', error)
//     // console.error('错误详情:', {
//     //   message: (error as any).message,
//     //   status: (error as any).response?.status,
//     //   statusText: (error as any).response?.statusText,
//     //   url: (error as any).config?.url,
//     //   data: (error as any).response?.data
//     // })
//     message.error(`加载国家风险排行榜失败: ${(error as any).message}`)
//   }
// }

// 暂时注释掉不存在的API调用
// 初始化基准数据
// const initBaselineData = async () => {
//   try {
//     // console.log('开始初始化基准数据...')
//     
//     const result = await initializeBaselineData()
//     // console.log('基准数据API返回结果:', result)
//     if (result && result.data?.success) {
//       // console.log('基准数据初始化完成')
//       message.uccess('基准数据初始化完成')
//       // 重新加载趋势数据
//       await loadCountryRiskTrends()
//     } else {
//       // console.error('初始化基准数据失败:', result?.data?.error)
//       // message.error(`初始化基准数据失败: ${result?.data?.error || '未知错误'}`)
//     }
//   } catch (error) {
//     // console.error('初始化基准数据失败:', error)
//     // console.error('错误详情:', {
//     //   message: (error as any).message,
//     //   status: (error as any).response?.status,
//     //   statusText: (error as any).response?.statusText,
//     //   url: (error as any).config?.url,
//     //   data: (error as any).response?.data
//     // })
//     // message.error(`初始化基准数据失败: ${(error as any).message}`)
//   }
// }

const viewCountryRiskDetail = (country: any) => {
  // 跳转到相关数据管理页面，并传递国家参数
  console.log('跳转到国家数据管理:', country.name)
  
  // 跳转到CrawlerDataManagement页面，并传递国家参数
  router.push({
    name: 'CrawlerDataManagement',
    query: {
      country: country.name,
      tab: 'data' // 默认显示数据标签页
    }
  })
  
  message.success(`正在跳转到 ${country.name} 的数据管理页面`)
}

const getCountryRiskCardClass = (riskLevel: string) => {
  switch (riskLevel) {
    case 'HIGH': return 'high-risk-card'
    case 'MEDIUM': return 'medium-risk-card'
    case 'LOW': return 'low-risk-card'
    default: return ''
  }
}

// const getRiskScoreColor = (score: number) => {
//   if (score >= 60) return '#ff4d4f'
//   if (score >= 30) return '#faad14'
//   return '#52c41a'
// }

const getCountryColor = (country: string) => {
  if (!country || country === '未确定') return 'default'
  
  // 预设颜色映射 - 按照指定顺序
  const colors: Record<string, string> = {
    '美国': 'blue',
    '欧盟': 'green',
    '中国': 'red',
    '韩国': 'cyan',
    '日本': 'purple',
    '阿联酋': 'orange',
    '印度': 'volcano',
    '泰国': 'gold',
    '新加坡': 'lime',
    '台湾': 'geekblue',
    '澳大利亚': 'cyan',
    '智利': 'blue',
    '马来西亚': 'green',
    '秘鲁': 'orange',
    '南非': 'purple',
    '以色列': 'blue',
    '印尼': 'red'
  }
  
  return colors[country] || 'default'
}

const getCountryName = (country: string) => {
  if (!country || country === '未确定') return '未确定'
  // 数据中的国家字段就是中文名称，直接返回
  return country
}

// 每日国家高风险数据统计图表配置
const dailyCountryRiskStatsChartOption = computed(() => {
  if (!dailyCountryRiskStats.value || dailyCountryRiskStats.value.length === 0) {
    return {
      title: {
        text: selectedCountryForChart.value ? `${selectedCountryForChart.value}高风险数据时间趋势` : '各国高风险数据时间趋势',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function (params: any) {
          // 直接显示 MM-DD 格式的日期
          const dateStr = params[0].name
          let result = dateStr + '<br/>'
          params.forEach((param: any) => {
            result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
          })
          return result
        }
      },
      legend: {
        orient: 'horizontal',
        bottom: 'bottom',
        data: ['暂无数据']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['暂无数据'],
        axisLabel: {
          rotate: 45
        }
      },
      yAxis: {
        type: 'value',
        name: '高风险数据数量'
      },
      series: [
        {
          name: '暂无数据',
          type: 'line',
          data: [0],
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 3
          },
          itemStyle: {
            color: '#ff4d4f'
          }
        }
      ]
    }
  }

  // 准备折线图数据 - 按日期和国家分组
  const dateCountryMap = new Map()
  const allDates = new Set()
  const allCountries = new Set()
  
  // 获取今天的日期字符串（MM-DD格式）
  const today = new Date()
  const todayStr = `${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  
  dailyCountryRiskStats.value.forEach(item => {
    const date = item.date || '未知日期'
    const country = item.country || '未知国家'
    const highRiskCount = item.highRiskCount || 0
    
    // 过滤掉今天以后的数据
    if (date > todayStr) {
      return
    }
    
    allDates.add(date)
    allCountries.add(country)
    
    const key = `${date}-${country}`
    dateCountryMap.set(key, highRiskCount)
  })
  
  // 按日期排序 - 由于日期格式是 MM-DD，需要特殊处理
  const sortedDates = Array.from(allDates).sort((a, b) => {
    // 将 MM-DD 格式转换为可比较的格式
    const dateA = a as string
    const dateB = b as string
    
    // 如果格式是 MM-DD，按字符串排序即可（因为月份和日期都是两位数）
    if (dateA.includes('-') && dateB.includes('-')) {
      return dateA.localeCompare(dateB)
    }
    
    // 其他格式按日期排序
    return new Date(dateA).getTime() - new Date(dateB).getTime()
  })
  
  const countries = Array.from(allCountries)
  
  // 如果选择了特定国家，只显示该国家的数据
  const displayCountries = selectedCountryForChart.value 
    ? [selectedCountryForChart.value].filter(country => countries.includes(country))
    : countries
  
  // 过滤掉没有数据的日期（所有国家在该日期的数据都为0或null）
  const datesWithData = sortedDates.filter(date => {
    return displayCountries.some(country => {
      const key = `${date}-${country}`
      const value = dateCountryMap.get(key) || 0
      return value > 0
    })
  })
  
  // 为每个国家准备数据，只使用有数据的日期
  const series = displayCountries.map((country, index) => {
    const data = datesWithData.map(date => {
      const key = `${date}-${country}`
      const value = dateCountryMap.get(key) || 0
      return value
    })
    
    // 为每个国家分配不同颜色
    const colors = [
      '#ff4d4f', '#1890ff', '#52c41a', '#faad14', '#722ed1',
      '#13c2c2', '#eb2f96', '#fa8c16', '#a0d911', '#2f54eb'
    ]
    const color = colors[index % colors.length]
    
    return {
      name: country,
      type: 'line',
      data: data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 3,
        color: color
      },
      itemStyle: {
        color: color
      }
    }
  })

  return {
    title: {
      text: selectedCountryForChart.value ? `${selectedCountryForChart.value}高风险数据时间趋势` : '各国高风险数据时间趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params: any) {
        // 直接显示 MM-DD 格式的日期
        const dateStr = params[0].name
        let result = dateStr + '<br/>'
        params.forEach((param: any) => {
          result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
        })
        return result
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 'bottom',
      data: displayCountries,
      type: displayCountries.length > 5 ? 'scroll' : 'plain',
      pageButtonItemGap: 5,
      pageButtonGap: 10,
      pageButtonPosition: 'end',
      pageFormatter: '{current}/{total}',
      pageIconColor: '#2f4554',
      pageIconInactiveColor: '#aaa',
      pageIconSize: 12,
      pageTextStyle: {
        color: '#333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '20%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: datesWithData,
      axisLabel: {
        rotate: 45,
        interval: 0,
        formatter: function (value: string) {
          // 直接显示 MM-DD 格式的日期
          return value
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '高风险数据数量'
    },
    series: series
  }
})

// 预定义的国家列表
const predefinedCountries = [
  '美国', '欧盟', '中国', '韩国', '日本', '阿联酋', '印度', '泰国', 
  '新加坡', '台湾', '澳大利亚', '智利', '马来西亚', '秘鲁', '南非', 
  '以色列', '印尼', '其他国家', '未确定'
]

// 加载每日国家高风险数据统计
const loadDailyCountryRiskStats = async () => {
  dailyCountryRiskStatsLoading.value = true
  
  try {
    // console.log('加载每日国家高风险数据统计...')
    
    const days = dailyCountryRiskStatsTimeRange.value
    // console.log(`获取近${days}天的趋势数据`)
    
    // 调用新的API接口获取所有国家的趋势数据
    const response = await fetch(`/api/api/daily-country-risk-stats/all-countries-trend?days=${days}`)
    
    if (!response.ok) {
      // console.error(`API调用失败: HTTP ${response.status}`)
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result = await response.json()
    // console.log('API返回结果:', result)
    
    if (result.success && result.data) {
      const trendData = result.data
      const countriesTrendData = trendData.countriesTrendData || []
      
      // console.log('获取到趋势数据:', {
      //   countriesCount: countriesTrendData.length,
      //   overallStats: trendData.overallStats
      // })
      
      // 转换数据格式为图表需要的格式
      const statsData: any[] = []
      
      countriesTrendData.forEach((countryTrend: any) => {
        const country = countryTrend.country
        const countryData = countryTrend.data || []
        
        countryData.forEach((dataPoint: any) => {
          statsData.push({
            country: country,
            date: dataPoint.dateStr, // 已经是 MM-DD 格式
            highRiskCount: dataPoint.highRiskCount || 0,
            mediumRiskCount: dataPoint.mediumRiskCount || 0,
            lowRiskCount: dataPoint.lowRiskCount || 0,
            noRiskCount: dataPoint.noRiskCount || 0,
            totalCount: dataPoint.totalCount || 0
          })
        })
      })
      
      dailyCountryRiskStats.value = statsData
      // console.log('从DailyCountryRiskStats加载数据成功:', {
      //   totalRecords: statsData.length,
      //   countries: [...new Set(statsData.map(item => item.country))],
      //   dateRange: dateSequence
      // })
      
      // 更新可用国家列表
      updateAvailableCountries()
      
      // 显示统计信息
      if (trendData.overallStats) {
        // console.log('总体统计:', trendData.overallStats)
      }
      
    } else {
      // console.error('API返回数据格式不正确:', result)
      throw new Error('API返回数据格式不正确')
    }
    
  } catch (error) {
    // console.error('加载每日国家高风险数据统计失败:', error)
    
    // API调用失败时，不生成模拟数据，保持空数据
    // console.log('API调用失败，不显示模拟数据')
    dailyCountryRiskStats.value = []
    updateAvailableCountries()
  } finally {
    dailyCountryRiskStatsLoading.value = false
  }
}

// 注意：已移除模拟数据生成函数，图表只显示真实的历史数据


// 加载最新风险数据
const loadLatestRiskData = async () => {
  latestRiskDataLoading.value = true
  
  try {
    // console.log('加载最新风险数据...')
    
    // 获取最新的3条相关数据，按发布时间排序
    const result = await getCrawlerData({ 
      page: 0, 
      size: 3, 
      related: true,
      sortBy: 'publishDate',
      sortDirection: 'desc'
    }) as any
    
    if (result && result.data) {
      latestRiskData.value = (result.data as any).content || []
      // console.log('最新风险数据加载成功:', latestRiskData.value)
    } else {
      // console.error('获取最新风险数据失败')
      latestRiskData.value = []
    }
  } catch (error) {
    // console.error('加载最新风险数据失败:', error)
    latestRiskData.value = []
  } finally {
    latestRiskDataLoading.value = false
  }
}

// 刷新最新风险数据
const refreshLatestRiskData = async () => {
  await loadLatestRiskData()
  message.success('最新风险数据刷新成功')
}

// 处理国家选择变化
const handleCountrySelectionChange = (country: string | undefined) => {
  selectedCountryForChart.value = country
  console.log('选择的国家:', country)
  // 图表会自动重新计算，因为 dailyCountryRiskStatsChartOption 是 computed 属性
}

// 更新可用国家列表
const updateAvailableCountries = () => {
  // 使用预定义的国家列表
  availableCountries.value = [...predefinedCountries]
  // console.log('可用国家列表:', availableCountries.value)
}

// 查看风险详情
const viewRiskDetail = (item: any) => {
  // console.log('查看风险详情:', item)
  currentRiskItem.value = item
  riskDetailVisible.value = true
}

// 设置风险等级
const setRiskLevel = (item: any) => {
  console.log('设置风险等级:', item)
  currentRiskItem.value = item
  riskLevelForm.value = {
    id: item.id,
    riskLevel: item.riskLevel || 'MEDIUM',
    remark: ''
  }
  riskLevelModalVisible.value = true
}

// 处理风险等级设置提交
const handleRiskLevelSubmit = async () => {
  if (!riskLevelForm.value.id) {
    message.error('数据ID不能为空')
    return
  }

  riskLevelLoading.value = true
  
  try {
    console.log('提交风险等级设置:', riskLevelForm.value)
    
    // 调用API更新风险等级
    // 从当前风险项目中获取type作为dataType
    const dataType = currentRiskItem.value?.type || 'crawler'
    const result = await updateRiskLevel(dataType, riskLevelForm.value.id, riskLevelForm.value.riskLevel)
    
    if (result.data && result.data.success) {
      message.success('风险等级设置成功')
      
      // 更新本地数据
      const itemIndex = latestRiskData.value.findIndex(item => item.id === riskLevelForm.value.id)
      if (itemIndex !== -1) {
        latestRiskData.value[itemIndex].riskLevel = riskLevelForm.value.riskLevel
      }
      
      // 关闭模态框
      riskLevelModalVisible.value = false
      riskLevelForm.value = {
        id: null,
        riskLevel: 'MEDIUM',
        remark: ''
      }
    } else {
      message.error(result.data?.message || '设置风险等级失败')
    }
    
  } catch (error) {
    // console.error('设置风险等级失败:', error)
    message.error('设置风险等级失败')
  } finally {
    riskLevelLoading.value = false
  }
}

// 处理风险等级设置取消
const handleRiskLevelCancel = () => {
  riskLevelModalVisible.value = false
  riskLevelForm.value = {
    id: null,
    riskLevel: 'MEDIUM',
    remark: ''
  }
}







onMounted(() => {
  refresh()
  // 暂时注释掉不存在的API调用，避免404错误
  // loadCountryRiskTrends()
  // loadCountryRiskRanking()
  // initBaselineData()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
}

.actions {
  display: flex;
  gap: 12px;
}

.stats {
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 20px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

/* 标签页样式 */
.dashboard-tabs {
  margin-top: 24px;
}

.overview-content {
  padding-top: 16px;
}

/* 国家风险卡片样式 */
.country-risk-cards {
  margin-bottom: 24px;
}

.country-risk-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.country-risk-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.high-risk-card {
  border-left: 4px solid #ff4d4f;
}

.medium-risk-card {
  border-left: 4px solid #faad14;
}

.low-risk-card {
  border-left: 4px solid #52c41a;
}

.country-card-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.country-card-body {
  text-align: center;
}

.risk-stats {
  margin-top: 12px;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 6px;
}

.risk-stats .ant-tag {
  margin: 0;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.risk-score {
  margin-top: 8px;
  font-size: 12px;
}

.score-label {
  color: #666;
}

.score-value {
  font-weight: bold;
  font-size: 14px;
}

.trend-display {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 国家风险概览样式 */
.country-risk-overview {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e9ecef;
}

/* 最新风险数据列表样式 */
.latest-risk-list {
  max-height: 400px;
  overflow-y: auto;
}

.risk-data-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fff;
  transition: all 0.3s ease;
}

.risk-data-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.risk-data-item.loading {
  opacity: 0.6;
  pointer-events: none;
}

.risk-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.risk-item-title {
  display: flex;
  align-items: center;
  flex: 1;
}

.risk-tag {
  margin-right: 8px;
  font-size: 12px;
}

.item-title {
  font-weight: 500;
  color: #262626;
  font-size: 14px;
  line-height: 1.4;
}

.risk-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.publish-time {
  font-size: 12px;
  color: #8c8c8c;
}

.risk-item-content {
  margin-top: 8px;
}

.risk-item-summary {
  color: #595959;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.risk-item-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #8c8c8c;
}

/* 风险详情模态框样式 */
.risk-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.risk-content-text {
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 风险等级设置模态框样式 */
.ant-radio-group .ant-radio-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.ant-radio-group .ant-radio-wrapper .ant-tag {
  margin-left: 8px;
}


</style>

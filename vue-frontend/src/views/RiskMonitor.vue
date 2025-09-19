能够<template>
  <div class="risk-monitor-container">
    <!-- 标题与控制区 -->
    <div class="header">
      <h1 class="title">地区风险监控面板</h1>
      <div class="controls">
        <a-date-picker
            v-model:value="selectedDate"
            format="YYYY-MM-DD"
            @change="handleDateChange"
        />
        <a-select
            v-model:value="timeRange"
            style="margin-left: 16px"
            @change="handleTimeRangeChange"
        >
          <a-select-option value="day">今日</a-select-option>
          <a-select-option value="week">本周</a-select-option>
          <a-select-option value="month">本月</a-select-option>
        </a-select>
        <a-button type="primary" style="margin-left: 16px" @click="refreshData" :loading="isLoading">
          <template #icon>
            <SyncOutlined />
          </template>
          刷新数据
        </a-button>
      </div>
    </div>


    <!-- 按国家分类统计 -->
    <div class="country-stats-section" v-if="countryStatistics && Object.keys(countryStatistics).length > 0">
      <a-card title="按国家分类统计" class="country-stats-card">
        <a-row :gutter="16">
          <a-col :span="6" v-for="(countryData, country) in countryStatistics" :key="country">
            <a-card class="country-stat-card" size="small">
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
                  <span class="label">510K:</span>
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
            </a-card>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 主内容区 - 左右分栏 -->
    <div class="main-content">
      <!-- 左侧：高风险地区 -->
      <div class="risk-panel high-risk-panel">
        <div class="panel-header">
          <ExclamationCircleOutlined />
          <h2 class="panel-title">高风险地区</h2>
          <a-select
              v-model:value="highRiskSort"
              class="sort-select"
              @change="sortHighRiskAreas"
          >
            <a-select-option value="riskDesc">高风险数据量 ↓</a-select-option>
            <a-select-option value="riskAsc">高风险数据量 ↑</a-select-option>
            <a-select-option value="name">名称排序</a-select-option>
          </a-select>
        </div>

        <!-- 高风险地区图表 -->
        <div class="chart-container">
          <a-card>
            <div class="chart-title">高风险地区分布</div>
            <div class="chart-wrapper">
              <v-chart 
                :option="highRiskChartOption" 
                style="height: 200px; width: 100%;" 
                autoresize
              />
            </div>
          </a-card>
        </div>

        <!-- 高风险地区列表 -->
        <div class="region-list">
          <a-card
              class="region-card"
              v-for="(region, index) in highRiskRegions"
              :key="index"
              hoverable
              @click="showRegionDetail(region)"
          >
            <div class="region-header">
              <div class="region-name">{{ region.name }}</div>
              <div class="risk-level" :style="{ backgroundColor: getRiskColor(region.riskLevel) }">
                {{ region.riskLevel }}
              </div>
            </div>
            <div class="region-stats">
              <div class="region-stat-item">
                <div class="stat-item-label">高风险数据量</div>
                <div class="stat-item-value">{{ region.highRiskCount || 0 }}</div>
              </div>
              <div class="region-stat-item">
                <div class="stat-item-label">总数据量</div>
                <div class="stat-item-value">{{ region.totalCount || 0 }}</div>
              </div>
              <div class="region-stat-item">
                <div class="stat-item-label">主要风险</div>
                <div class="stat-item-value">{{ region.mainRisk }}</div>
              </div>
            </div>
            <div class="region-progress">
              <a-progress
                  :percent="region.totalCount > 0 ? Math.round((region.highRiskCount || 0) / region.totalCount * 100) : 0"
                  :status="(region.highRiskCount || 0) > 0 ? 'exception' : 'success'"
              />
            </div>
            <div class="region-update-time">
              最后更新: {{ formatDate(region.updateTime) }}
            </div>
          </a-card>
        </div>
      </div>

      <!-- 右侧：低风险地区 -->
      <div class="risk-panel low-risk-panel">
        <div class="panel-header">
          <CheckCircleOutlined />
          <h2 class="panel-title">低风险地区</h2>
          <a-select
              v-model:value="lowRiskSort"
              class="sort-select"
              @change="sortLowRiskAreas"
          >
            <a-select-option value="riskDesc">高风险数据量 ↓</a-select-option>
            <a-select-option value="riskAsc">高风险数据量 ↑</a-select-option>
            <a-select-option value="name">名称排序</a-select-option>
          </a-select>
        </div>

        <!-- 低风险地区图表 -->
        <div class="chart-container">
          <a-card>
            <div class="chart-title">低风险地区分布</div>
            <div class="chart-wrapper">
              <v-chart 
                :option="lowRiskChartOption" 
                style="height: 200px; width: 100%;" 
                autoresize
              />
            </div>
          </a-card>
        </div>

        <!-- 低风险地区列表 -->
        <div class="region-list">
          <a-card
              class="region-card"
              v-for="(region, index) in lowRiskRegions"
              :key="index"
              hoverable
              @click="showRegionDetail(region)"
          >
            <div class="region-header">
              <div class="region-name">{{ region.name }}</div>
              <div class="risk-level" :style="{ backgroundColor: getRiskColor(region.riskLevel) }">
                {{ region.riskLevel }}
              </div>
            </div>
            <div class="region-stats">
              <div class="region-stat-item">
                <div class="stat-item-label">高风险数据量</div>
                <div class="stat-item-value">{{ region.highRiskCount || 0 }}</div>
              </div>
              <div class="region-stat-item">
                <div class="stat-item-label">总数据量</div>
                <div class="stat-item-value">{{ region.totalCount || 0 }}</div>
              </div>
              <div class="region-stat-item">
                <div class="stat-item-label">主要风险</div>
                <div class="stat-item-value">{{ region.mainRisk || '无显著风险' }}</div>
              </div>
            </div>
            <div class="region-progress">
              <a-progress
                  :percent="region.totalCount > 0 ? Math.round((region.highRiskCount || 0) / region.totalCount * 100) : 0"
                  :status="(region.highRiskCount || 0) > 0 ? 'exception' : 'success'"
              />
            </div>
            <div class="region-update-time">
              最后更新: {{ formatDate(region.updateTime) }}
            </div>
          </a-card>
        </div>
      </div>
    </div>

    <!-- 地区详情模态框 -->
    <a-modal
        v-model:open="detailVisible"
        :title="currentRegion ? currentRegion.name + ' 风险详情' : '地区风险详情'"
        :width="700"
        @cancel="handleCancel"
    >
      <div v-if="currentRegion" class="region-detail">
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">地区名称:</span>
              <span class="info-value">{{ currentRegion.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">风险等级:</span>
              <span class="info-value" :style="{ color: getRiskColor(currentRegion.riskLevel) }">
                {{ currentRegion.riskLevel }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">高风险数据量:</span>
              <span class="info-value">{{ currentRegion.highRiskCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">总数据量:</span>
              <span class="info-value">{{ currentRegion.totalCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上次评估时间:</span>
              <span class="info-value">{{ formatDate(currentRegion.updateTime) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">风险构成</h3>
          <div class="risk-composition">
            <v-chart 
              :option="riskCompositionChartOption" 
              style="height: 240px; width: 100%;" 
              autoresize
            />
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">风险详情</h3>
          <a-descriptions column="1" bordered>
            <a-descriptions-item label="主要风险点">
              <div v-for="(risk, i) in currentRegion.riskDetails" :key="i" class="risk-point">
                <a-badge :status="risk.severity === '高' ? 'error' : risk.severity === '中' ? 'warning' : 'success'" />
                {{ risk.description }}
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="影响范围">
              {{ currentRegion.impactRange }}
            </a-descriptions-item>
            <a-descriptions-item label="趋势预测">
              <a-tag :color="currentRegion.trend === '上升' ? 'red' : currentRegion.trend === '下降' ? 'green' : 'blue'">
                {{ currentRegion.trend }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="建议措施">
              <div v-for="(measure, i) in currentRegion.recommendations" :key="i" class="recommendation-item">
                {{ i + 1 }}. {{ measure }}
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import {
  SyncOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  GlobalOutlined
} from '@ant-design/icons-vue'
import { getHighRiskStatisticsByCountry } from '@/api/highRiskData'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 状态管理
const selectedDate = ref(dayjs())
const timeRange = ref('week')
const highRiskSort = ref('riskDesc')
const lowRiskSort = ref('riskAsc')
const detailVisible = ref(false)
const currentRegion = ref<any>(null)
const isLoading = ref(false)

// 统计数据
const highRiskStats = reactive({
  total: 0,
  increaseRate: 0
})

const mediumRiskStats = reactive({
  total: 0,
  increaseRate: 0
})

const lowRiskStats = reactive({
  total: 0,
  increaseRate: 0
})

const totalRegions = ref(0)
const totalRegionsChange = ref(0)

// 地区数据
const highRiskRegions = ref<any[]>([])
const lowRiskRegions = ref<any[]>([])

// 按国家分类的统计数据
const countryStatistics = ref<Record<string, any>>({})


// 从后端获取国家统计数据
const fetchCountryStatistics = async () => {
  try {
    console.log('🔄 开始加载按国家分类的统计数据...')
    const response = await getHighRiskStatisticsByCountry()
    console.log('📊 按国家统计数据响应:', response)
    
    if (response && (response as any).countryStatistics) {
      console.log('✅ 按国家统计数据加载成功:', (response as any).countryStatistics)
      return response
    } else {
      console.warn('⚠️ 按国家统计数据响应格式不正确:', response)
      return null
    }
  } catch (error) {
    console.error('❌ 加载按国家统计数据失败:', error)
    return null
  }
}



// 处理后端数据，转换为前端需要的格式
const processBackendData = (backendData: any) => {
  if (!backendData) return { highRiskAreas: [], lowRiskAreas: [], mediumRiskCount: 0, totalRegions: 0, totalRegionsChange: 0 }
  
  const highRiskAreas: any[] = []
  const lowRiskAreas: any[] = []
  let mediumRiskCount = 0
  
  console.log('处理后端数据:', backendData)
  
  // 从countryStatistics中提取各国的高风险数据量
  if (backendData.countryStatistics) {
    const countryStats = backendData.countryStatistics
    
    // 将国家统计数据转换为地区数据，按高风险数据量排序
    const countriesWithHighRisk = Object.entries(countryStats)
      .map(([countryCode, stats]: [string, any]) => ({
        countryCode,
        countryName: getCountryDisplayName(countryCode),
        highRiskCount: stats.highRisk || 0,
        totalCount: stats.total || 0,
        stats
      }))
      .filter(country => country.highRiskCount > 0)
      .sort((a, b) => b.highRiskCount - a.highRiskCount) // 按高风险数据量降序排序
    
    // 将高风险数据量最多的国家作为高风险地区
    countriesWithHighRisk.forEach((country) => {
      const riskScore = calculateRiskScoreFromStats(country.stats)
      const riskLevel = determineRiskLevelFromScore(riskScore)
      
      if (riskLevel === '高风险' || country.highRiskCount >= 10) {
        highRiskAreas.push({
          name: country.countryName,
          countryCode: country.countryCode,
          riskLevel: '高风险',
          riskScore: riskScore,
          highRiskCount: country.highRiskCount,
          totalCount: country.totalCount,
          mainRisk: getMainRiskTypeFromStats(country.stats),
          impactRange: getImpactRangeFromStats(country.stats),
          updateTime: new Date(),
          trend: '上升',
          riskDetails: generateRiskDetailsFromStats(country.stats),
          recommendations: generateRecommendationsFromStats(country.countryName, country.stats)
        })
      } else if (riskLevel === '低风险' || country.highRiskCount < 5) {
        lowRiskAreas.push({
          name: country.countryName,
          countryCode: country.countryCode,
          riskLevel: '低风险',
          riskScore: riskScore,
          highRiskCount: country.highRiskCount,
          totalCount: country.totalCount,
          mainRisk: getMainRiskTypeFromStats(country.stats) || '无显著风险',
          impactRange: getImpactRangeFromStats(country.stats) || '极小',
          updateTime: new Date(),
          trend: '稳定',
          riskDetails: generateRiskDetailsFromStats(country.stats),
          recommendations: generateRecommendationsFromStats(country.countryName, country.stats)
        })
      } else {
        mediumRiskCount++
      }
    })
    
    // 只使用真实数据，不添加默认数据
  } else {
    // 如果没有统计数据，返回空数据
    console.warn('没有获取到国家统计数据')
  }
  
  return {
    highRiskAreas,
    lowRiskAreas,
    mediumRiskCount,
    totalRegions: highRiskAreas.length + lowRiskAreas.length + mediumRiskCount,
    totalRegionsChange: 2
  }
}

// 从后端统计数据计算风险分数
const calculateRiskScoreFromStats = (stats: any) => {
  if (!stats) return 0
  
  // 基于高风险、中风险、低风险、无风险的数量计算风险分数
  const highRisk = stats.highRisk || 0
  const mediumRisk = stats.mediumRisk || 0
  const lowRisk = stats.lowRisk || 0
  const noRisk = stats.noRisk || 0
  const total = stats.total || (highRisk + mediumRisk + lowRisk + noRisk)
  
  if (total === 0) return 0
  
  // 风险分数计算：高风险权重最高
  const riskScore = Math.round((highRisk * 100 + mediumRisk * 60 + lowRisk * 20 + noRisk * 0) / total)
  return Math.min(100, Math.max(0, riskScore))
}

// 根据风险分数确定风险等级
const determineRiskLevelFromScore = (score: number) => {
  if (score >= 70) return '高风险'
  if (score >= 40) return '中风险'
  if (score >= 10) return '低风险'
  return '无风险'
}

// 从统计数据获取主要风险类型
const getMainRiskTypeFromStats = (stats: any) => {
  if (!stats) return '无显著风险'
  
  const highRisk = stats.highRisk || 0
  const mediumRisk = stats.mediumRisk || 0
  const lowRisk = stats.lowRisk || 0
  
  if (highRisk > 0) return '高风险数据'
  if (mediumRisk > 0) return '中风险数据'
  if (lowRisk > 0) return '低风险数据'
  return '无显著风险'
}

// 从统计数据获取影响范围
const getImpactRangeFromStats = (stats: any) => {
  if (!stats) return '极小'
  
  const total = stats.total || 0
  if (total >= 1000) return '极大'
  if (total >= 500) return '大'
  if (total >= 100) return '中等'
  if (total >= 10) return '小'
  return '极小'
}

// 从统计数据生成风险详情
const generateRiskDetailsFromStats = (stats: any) => {
  if (!stats) return []
  
  const details = []
  if (stats.highRisk > 0) {
    details.push({
      description: `高风险数据 ${stats.highRisk} 条`,
      severity: '高'
    })
  }
  if (stats.mediumRisk > 0) {
    details.push({
      description: `中风险数据 ${stats.mediumRisk} 条`,
      severity: '中'
    })
  }
  if (stats.lowRisk > 0) {
    details.push({
      description: `低风险数据 ${stats.lowRisk} 条`,
      severity: '低'
    })
  }
  return details
}

// 从统计数据生成建议措施
const generateRecommendationsFromStats = (country: string, stats: any) => {
  if (!stats) return []
  
  const recommendations = []
  const highRisk = stats.highRisk || 0
  const total = stats.total || 0
  
  if (highRisk > 0) {
    recommendations.push(`加强${country}高风险数据的监控和预警`)
    recommendations.push(`建立${country}风险数据快速响应机制`)
  }
  
  if (total > 100) {
    recommendations.push(`优化${country}数据处理流程，提高效率`)
  }
  
  if (recommendations.length === 0) {
    recommendations.push(`继续监控${country}数据变化趋势`)
  }
  
  return recommendations
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


// 初始化数据
const initData = async () => {
  isLoading.value = true
  
  try {
    // 从后端获取数据
    const countryStats = await fetchCountryStatistics()
    
    if (countryStats) {
      const processedData = processBackendData(countryStats)
      
      highRiskRegions.value = [...processedData.highRiskAreas]
      lowRiskRegions.value = [...processedData.lowRiskAreas]
      mediumRiskStats.total = processedData.mediumRiskCount
      highRiskStats.total = processedData.highRiskAreas.length
      lowRiskStats.total = processedData.lowRiskAreas.length
      totalRegions.value = processedData.totalRegions
      totalRegionsChange.value = processedData.totalRegionsChange
      
      // 设置按国家分类的统计数据
      if (countryStats && (countryStats as any).countryStatistics) {
        countryStatistics.value = (countryStats as any).countryStatistics
      }
      
      // 计算真实的趋势数据
      if ((countryStats as any).yesterdaySummary && (countryStats as any).todaySummary) {
        // 计算高风险数据变化率
        const yesterdayHighRisk = Object.values((countryStats as any).yesterdaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.highRisk || 0), 0)
        const todayHighRisk = Object.values((countryStats as any).todaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.highRisk || 0), 0)
        
        highRiskStats.increaseRate = yesterdayHighRisk > 0 ? 
          Math.round(((todayHighRisk - yesterdayHighRisk) / yesterdayHighRisk) * 100) : 0
        
        // 计算中风险数据变化率
        const yesterdayMediumRisk = Object.values((countryStats as any).yesterdaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.mediumRisk || 0), 0)
        const todayMediumRisk = Object.values((countryStats as any).todaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.mediumRisk || 0), 0)
        
        mediumRiskStats.increaseRate = yesterdayMediumRisk > 0 ? 
          Math.round(((todayMediumRisk - yesterdayMediumRisk) / yesterdayMediumRisk) * 100) : 0
        
        // 计算低风险数据变化率
        const yesterdayLowRisk = Object.values((countryStats as any).yesterdaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.lowRisk || 0), 0)
        const todayLowRisk = Object.values((countryStats as any).todaySummary.countryStats || {})
          .reduce((sum: number, stats: any) => sum + (stats.lowRisk || 0), 0)
        
        lowRiskStats.increaseRate = yesterdayLowRisk > 0 ? 
          Math.round(((todayLowRisk - yesterdayLowRisk) / yesterdayLowRisk) * 100) : 0
      } else {
        // 如果没有历史数据，使用默认值
        highRiskStats.increaseRate = 0
        mediumRiskStats.increaseRate = 0
        lowRiskStats.increaseRate = 0
      }
      
    } else {
      // 如果API调用失败，清空数据
      console.warn('API调用失败，清空数据')
      highRiskRegions.value = []
      lowRiskRegions.value = []
      mediumRiskStats.total = 0
      mediumRiskStats.increaseRate = 0
      highRiskStats.total = 0
      highRiskStats.increaseRate = 0
      lowRiskStats.total = 0
      lowRiskStats.increaseRate = 0
      totalRegions.value = 0
      totalRegionsChange.value = 0
    }
    
    
  } catch (error) {
    console.error('初始化数据失败:', error)
    // 发生错误时清空数据
    highRiskRegions.value = []
    lowRiskRegions.value = []
    mediumRiskStats.total = 0
    mediumRiskStats.increaseRate = 0
    highRiskStats.total = 0
    highRiskStats.increaseRate = 0
    lowRiskStats.total = 0
    lowRiskStats.increaseRate = 0
    totalRegions.value = 0
    totalRegionsChange.value = 0
  } finally {
    isLoading.value = false
  }
}


// 格式化日期
const formatDate = (date: Date) => {
  if (!date) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 获取风险等级颜色
const getRiskColor = (level: string) => {
  switch (level) {
    case '高风险':
      return '#f5222d'
    case '中风险':
      return '#faad14'
    case '低风险':
      return '#52c41a'
    default:
      return '#1890ff'
  }
}

// 排序高风险地区
const sortHighRiskAreas = () => {
  highRiskRegions.value.sort((a, b) => {
    if (highRiskSort.value === 'riskDesc') {
      // 按高风险数据量降序排序
      return (b.highRiskCount || 0) - (a.highRiskCount || 0)
    } else if (highRiskSort.value === 'riskAsc') {
      // 按高风险数据量升序排序
      return (a.highRiskCount || 0) - (b.highRiskCount || 0)
    } else if (highRiskSort.value === 'name') {
      return a.name.localeCompare(b.name)
    }
    return 0
  })
}

// 排序低风险地区
const sortLowRiskAreas = () => {
  lowRiskRegions.value.sort((a, b) => {
    if (lowRiskSort.value === 'riskDesc') {
      // 按高风险数据量降序排序
      return (b.highRiskCount || 0) - (a.highRiskCount || 0)
    } else if (lowRiskSort.value === 'riskAsc') {
      // 按高风险数据量升序排序
      return (a.highRiskCount || 0) - (b.highRiskCount || 0)
    } else if (lowRiskSort.value === 'name') {
      return a.name.localeCompare(b.name)
    }
    return 0
  })
}

// 显示地区详情
const showRegionDetail = (region: any) => {
  currentRegion.value = { 
    ...region,
    stats: region.stats || {
      highRisk: region.highRiskCount || 0,
      mediumRisk: 0,
      lowRisk: 0,
      noRisk: (region.totalCount || 0) - (region.highRiskCount || 0),
      total: region.totalCount || 0
    }
  }
  detailVisible.value = true
}

// 处理取消
const handleCancel = () => {
  detailVisible.value = false
  currentRegion.value = null
}

// 刷新数据
const refreshData = async () => {
  // 清空当前数据，显示加载状态
  highRiskRegions.value = []
  lowRiskRegions.value = []

  // 重新初始化数据
  await initData()
}

// 处理日期变更
const handleDateChange = (date: any) => {
  console.log('Selected date:', date)
}

// 处理时间范围变更
const handleTimeRangeChange = (range: string) => {
  console.log('Time range:', range)
}

// 图表配置
const highRiskChartOption = computed(() => {
  const maxHighRiskCount = Math.max(...highRiskRegions.value.map(r => r.highRiskCount || 0), 1)
  
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const data = params[0]
        const region = highRiskRegions.value[data.dataIndex]
        return `${data.name}<br/>高风险数据量: ${region.highRiskCount || 0}<br/>总数据量: ${region.totalCount || 0}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: highRiskRegions.value.map(r => r.name),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      max: maxHighRiskCount,
      name: '高风险数据量'
    },
    series: [
      {
        name: '高风险数据量',
        type: 'bar',
        data: highRiskRegions.value.map(r => r.highRiskCount || 0),
        itemStyle: {
          color: '#f5222d'
        }
      }
    ]
  }
})

const lowRiskChartOption = computed(() => {
  const maxHighRiskCount = Math.max(...lowRiskRegions.value.map(r => r.highRiskCount || 0), 1)
  
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const data = params[0]
        const region = lowRiskRegions.value[data.dataIndex]
        return `${data.name}<br/>高风险数据量: ${region.highRiskCount || 0}<br/>总数据量: ${region.totalCount || 0}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: lowRiskRegions.value.map(r => r.name),
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      max: maxHighRiskCount,
      name: '高风险数据量'
    },
    series: [
      {
        name: '高风险数据量',
        type: 'bar',
        data: lowRiskRegions.value.map(r => r.highRiskCount || 0),
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
})

const riskCompositionChartOption = computed(() => {
  if (!currentRegion.value) return {}

  // 基于真实数据生成风险构成
  const stats = currentRegion.value.stats
  if (!stats) return {}

  const riskComposition = {
    '高风险数据': stats.highRisk || 0,
    '中风险数据': stats.mediumRisk || 0,
    '低风险数据': stats.lowRisk || 0,
    '无风险数据': stats.noRisk || 0
  }

  // 过滤掉为0的数据
  const filteredComposition = Object.entries(riskComposition)
    .filter(([_, value]) => value > 0)
    .reduce((acc, [key, value]) => {
      acc[key] = value
      return acc
    }, {} as Record<string, number>)

  if (Object.keys(filteredComposition).length === 0) {
    return {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '风险构成',
          type: 'pie',
          radius: '50%',
          data: [{ name: '暂无数据', value: 1 }],
          itemStyle: {
            color: '#d9d9d9'
          }
        }
      ]
    }
  }

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '风险构成',
        type: 'pie',
        radius: '50%',
        data: Object.entries(filteredComposition).map(([name, value]) => ({
          name,
          value
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

// 监听数据变化重新排序
watch(highRiskSort, sortHighRiskAreas)
watch(lowRiskSort, sortLowRiskAreas)

// 组件挂载时初始化
onMounted(() => {
  initData()
})
</script>

<style scoped>
.risk-monitor-container {
  padding: 1.25rem;
  min-height: 100%;
  width: 100%;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  margin: 0;
  color: #1f2329;
  font-size: 24px;
  font-weight: 600;
}

.controls {
  display: flex;
  align-items: center;
}

.stats-container {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.stat-label {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.stat-trend .anticon {
  margin-right: 4px;
}

.trend-text {
  margin-left: 4px;
  color: rgba(0, 0, 0, 0.5);
}

.high-risk-stat .stat-value {
  color: #f5222d;
}

.medium-risk-stat .stat-value {
  color: #faad14;
}

.low-risk-stat .stat-value {
  color: #52c41a;
}

.total-stat .stat-value {
  color: #1890ff;
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
}

.country-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

.main-content {
  display: flex;
  gap: 24px;
  height: calc(100% - 180px);
}

.risk-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.high-risk-panel {
  border-left: 3px solid #f5222d;
  padding-left: 20px;
}

.low-risk-panel {
  border-left: 3px solid #52c41a;
  padding-left: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-header .anticon {
  color: #f5222d;
  margin-right: 8px;
}

.low-risk-panel .panel-header .anticon {
  color: #52c41a;
}

.panel-title {
  display: flex;
  align-items: center;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sort-select {
  width: 160px;
}

.chart-container {
  margin-bottom: 24px;
}

.chart-title {
  font-size: 16px;
  margin-bottom: 16px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.chart-wrapper {
  height: 200px;
  width: 100%;
  min-height: 200px;
}

.region-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  max-height: calc(100vh - 450px);
  padding-right: 8px;
}

.region-card {
  transition: all 0.2s ease;
}

.region-card:hover {
  transform: translateX(5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.region-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.region-name {
  font-size: 16px;
  font-weight: 500;
}

.risk-level {
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.region-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.region-stat-item {
  flex: 1;
  text-align: center;
}

.stat-item-label {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  margin-bottom: 4px;
  display: block;
}

.stat-item-value {
  font-size: 14px;
  font-weight: 500;
}

.region-progress {
  margin-bottom: 8px;
}

.region-update-time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  text-align: right;
}

.region-detail {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  margin-bottom: 16px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-label {
  flex: 0 0 100px;
  color: rgba(0, 0, 0, 0.65);
}

.info-value {
  flex: 1;
  font-weight: 500;
}

.risk-composition {
  height: 240px;
  margin-bottom: 16px;
}

.risk-point {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.risk-point .ant-badge {
  margin-right: 8px;
}

.recommendation-item {
  margin-bottom: 8px;
  padding-left: 8px;
  position: relative;
}

.recommendation-item:before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #1890ff;
}

/* 滚动条样式优化 */
.region-list::-webkit-scrollbar {
  width: 6px;
}

.region-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.region-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.region-list::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .high-risk-panel, .low-risk-panel {
    padding-left: 12px;
  }

  .stats-container {
    flex-direction: column;
  }

  .stat-card {
    width: 100%;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .controls {
    width: 100%;
    flex-wrap: wrap;
  }

  .a-date-picker, .a-select {
    width: 100% !important;
    margin-left: 0 !important;
    margin-bottom: 12px !important;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

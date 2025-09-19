<template>
  <div class="us-high-risk-data">
    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="高风险设备总数"
              :value="statistics.totalHighRisk"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <WarningOutlined style="color: #ff4d4f" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card">
            <a-statistic
              title="510K高风险"
              :value="statistics.device510KHighRisk"
              :value-style="{ color: '#ff7a45' }"
            >
              <template #prefix>
                <ExperimentOutlined style="color: #ff7a45" />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
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
        <a-col :span="6">
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
      </a-row>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <a-card title="搜索筛选" :bordered="false">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="数据类型">
            <a-select
              v-model:value="searchForm.dataType"
              placeholder="选择数据类型"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="510k">510K设备</a-select-option>
              <a-select-option value="recall">召回记录</a-select-option>
              <a-select-option value="event">事件报告</a-select-option>
              <a-select-option value="registration">注册记录</a-select-option>
              <a-select-option value="guidance">指导文档</a-select-option>
              <a-select-option value="customs">海关案例</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="关键词">
            <a-input
              v-model:value="searchForm.keywords"
              placeholder="输入关键词搜索"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          
          <a-form-item label="时间范围">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              style="width: 240px"
              :placeholder="['开始日期', '结束日期']"
            />
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" @click="handleSearch" :loading="searchLoading">
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button style="margin-left: 8px" @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 数据表格区域 -->
    <div class="table-section">
      <a-card title="高风险数据列表" :bordered="false">
        <div class="table-actions">
          <a-space>
            <a-button type="primary" @click="handleBatchEdit" :disabled="!selectedRowKeys.length">
              <template #icon>
                <EditOutlined />
              </template>
              批量编辑
            </a-button>
            <a-button @click="handleExport" :loading="exportLoading">
              <template #icon>
                <DownloadOutlined />
              </template>
              导出数据
            </a-button>
          </a-space>
          
          <span class="selected-count">
            已选择 {{ selectedRowKeys.length }} 项
          </span>
        </div>
        
        <a-table
          :columns="columns"
          :data-source="tableData"
          :loading="tableLoading"
          :pagination="pagination"
          :row-selection="rowSelection"
          @change="handleTableChange"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'riskLevel'">
              <a-tag :color="getRiskLevelColor(record.riskLevel)">
                {{ getRiskLevelText(record.riskLevel) }}
              </a-tag>
            </template>
            
            <template v-else-if="column.key === 'keywords'">
              <div class="keywords-display">
                <a-tag
                  v-for="keyword in parseKeywords(record.keywords)"
                  :key="keyword"
                  size="small"
                  color="blue"
                >
                  {{ keyword }}
                </a-tag>
              </div>
            </template>
            
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleView(record)">
                  查看
                </a-button>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" @click="handleRiskAdjust(record)">
                  风险调整
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑高风险数据"
      width="800px"
      :footer="null"
      @cancel="handleEditCancel"
    >
      <HighRiskDataEditForm
        v-if="editModalVisible"
        :data="currentEditData"
        @submit="handleEditSubmit"
        @cancel="handleEditCancel"
      />
    </a-modal>

    <!-- 风险调整弹窗 -->
    <a-modal
      v-model:open="riskAdjustModalVisible"
      title="风险等级调整"
      width="500px"
      @ok="handleRiskAdjustSubmit"
      @cancel="handleRiskAdjustCancel"
    >
      <a-form :model="riskAdjustForm" layout="vertical">
        <a-form-item label="当前风险等级">
          <a-tag :color="getRiskLevelColor(currentEditData?.riskLevel)">
            {{ getRiskLevelText(currentEditData?.riskLevel) }}
          </a-tag>
        </a-form-item>
        
        <a-form-item label="调整后风险等级" required>
          <a-radio-group v-model:value="riskAdjustForm.newRiskLevel">
            <a-radio value="HIGH">高风险</a-radio>
            <a-radio value="MEDIUM">中风险</a-radio>
            <a-radio value="LOW">低风险</a-radio>
          </a-radio-group>
        </a-form-item>
        
        <a-form-item label="调整原因" required>
          <a-textarea
            v-model:value="riskAdjustForm.reason"
            placeholder="请输入风险等级调整的原因"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  WarningOutlined,
  ExperimentOutlined,
  ExclamationCircleOutlined,
  AlertOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import HighRiskDataEditForm from './HighRiskDataEditForm.vue'
import { getHighRiskData, updateRiskLevel } from '@/api/highRiskData'

// 数据统计
const statistics = reactive({
  totalHighRisk: 0,
  device510KHighRisk: 0,
  recallHighRisk: 0,
  eventHighRisk: 0
})

// 搜索表单
const searchForm = reactive({
  dataType: '',
  keywords: '',
  dateRange: []
})

// 表格相关
const tableData = ref([])
const tableLoading = ref(false)
const selectedRowKeys = ref([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 弹窗控制
const editModalVisible = ref(false)
const riskAdjustModalVisible = ref(false)
const currentEditData = ref(null)
const searchLoading = ref(false)
const exportLoading = ref(false)

// 风险调整表单
const riskAdjustForm = reactive({
  newRiskLevel: 'MEDIUM',
  reason: ''
})

// 表格列定义
const columns = [
  {
    title: '数据类型',
    dataIndex: 'dataType',
    key: 'dataType',
    width: 100,
    render: (text: string) => {
      const typeMap = {
        '510k': '510K设备',
        'recall': '召回记录',
        'event': '事件报告',
        'registration': '注册记录',
        'guidance': '指导文档',
        'customs': '海关案例'
      }
      return typeMap[text] || text
    }
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 200
  },
  {
    title: '公司/申请人',
    dataIndex: 'applicant',
    key: 'applicant',
    width: 150
  },
  {
    title: '关键词',
    dataIndex: 'keywords',
    key: 'keywords',
    width: 200
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 120
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  }
}))

// 获取风险等级颜色
const getRiskLevelColor = (level: string) => {
  const colorMap = {
    'HIGH': '#ff4d4f',
    'MEDIUM': '#faad14',
    'LOW': '#52c41a'
  }
  return colorMap[level] || '#faad14'
}

// 获取风险等级文本
const getRiskLevelText = (level: string) => {
  const textMap = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险'
  }
  return textMap[level] || '中风险'
}

// 解析关键词
const parseKeywords = (keywords: string) => {
  if (!keywords) return []
  try {
    return JSON.parse(keywords)
  } catch {
    return []
  }
}

// 搜索数据
const handleSearch = async () => {
  searchLoading.value = true
  try {
    const params = {
      countryCode: 'US',
      riskLevel: 'HIGH',
      dataType: searchForm.dataType,
      keywords: searchForm.keywords,
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.dateFrom = searchForm.dateRange[0].format('YYYY-MM-DD')
      params.dateTo = searchForm.dateRange[1].format('YYYY-MM-DD')
    }
    
    const result = await getHighRiskData(params)
    if (result.success) {
      tableData.value = result.data.records || []
      pagination.total = result.data.totalElements || 0
      message.success(`搜索完成，找到 ${pagination.total} 条记录`)
    }
  } catch (error) {
    message.error('搜索失败: ' + error.message)
  } finally {
    searchLoading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.dataType = ''
  searchForm.keywords = ''
  searchForm.dateRange = []
  pagination.current = 1
  handleSearch()
}

// 表格变化处理
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  handleSearch()
}

// 查看数据
const handleView = (record: any) => {
  currentEditData.value = record
  editModalVisible.value = true
}

// 编辑数据
const handleEdit = (record: any) => {
  currentEditData.value = record
  editModalVisible.value = true
}

// 批量编辑
const handleBatchEdit = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要编辑的数据')
    return
  }
  message.info(`批量编辑 ${selectedRowKeys.value.length} 条数据`)
}

// 导出数据
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 实现导出逻辑
    await new Promise(resolve => setTimeout(resolve, 2000))
    message.success('数据导出成功')
  } catch (error) {
    message.error('数据导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 风险调整
const handleRiskAdjust = (record: any) => {
  currentEditData.value = record
  riskAdjustForm.newRiskLevel = record.riskLevel
  riskAdjustForm.reason = ''
  riskAdjustModalVisible.value = true
}

// 风险调整提交
const handleRiskAdjustSubmit = async () => {
  if (!riskAdjustForm.reason.trim()) {
    message.warning('请输入调整原因')
    return
  }
  
  try {
    // 从当前编辑数据中获取dataType，如果没有则默认使用'510k'
    const dataType = currentEditData.value.dataType || currentEditData.value.type || '510k'
    await updateRiskLevel(dataType, currentEditData.value.id, riskAdjustForm.newRiskLevel)
    message.success('风险等级调整成功')
    riskAdjustModalVisible.value = false
    handleSearch() // 刷新数据
  } catch (error) {
    message.error('风险等级调整失败: ' + error.message)
  }
}

// 风险调整取消
const handleRiskAdjustCancel = () => {
  riskAdjustModalVisible.value = false
}

// 编辑提交
const handleEditSubmit = async (data: any) => {
  try {
    // 实现编辑提交逻辑
    message.success('数据更新成功')
    editModalVisible.value = false
    handleSearch() // 刷新数据
  } catch (error) {
    message.error('数据更新失败: ' + error.message)
  }
}

// 编辑取消
const handleEditCancel = () => {
  editModalVisible.value = false
  currentEditData.value = null
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const result = await getHighRiskData({ countryCode: 'US', riskLevel: 'HIGH' })
    if (result.success) {
      // 这里需要根据实际API返回的数据结构进行调整
      statistics.totalHighRisk = result.data.totalElements || 0
      // 其他统计数据也需要根据实际API调整
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadStatistics()
  handleSearch()
})
</script>

<style scoped>
.us-high-risk-data {
  .stats-section {
    margin-bottom: 24px;
  }

  .stat-card {
    text-align: center;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .search-section {
    margin-bottom: 24px;
  }

  .table-section {
    .table-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .selected-count {
      color: #666;
      font-size: 14px;
    }
  }

  .keywords-display {
    .ant-tag {
      margin: 2px;
    }
  }
}
</style>

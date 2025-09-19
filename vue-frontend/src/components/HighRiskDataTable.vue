<template>
  <div class="high-risk-data-table">
    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <a-card title="搜索筛选" :bordered="false" style="margin-bottom: 16px;">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="关键词">
            <a-input
              v-model:value="searchForm.keywords"
              placeholder="输入关键词搜索"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          
          <a-form-item label="风险等级">
            <a-select
              v-model:value="searchForm.riskLevel"
              placeholder="选择风险等级"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="HIGH">高风险</a-select-option>
              <a-select-option value="MEDIUM">中风险</a-select-option>
              <a-select-option value="LOW">低风险</a-select-option>
              <a-select-option value="NONE">无风险</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="时间排序">
            <a-select
              v-model:value="sortConfig.sortDir"
              placeholder="选择排序方式"
              style="width: 120px"
              @change="handleSortChange"
            >
              <a-select-option value="desc">
                <span>⬇️ 降序</span>
              </a-select-option>
              <a-select-option value="asc">
                <span>⬆️ 升序</span>
              </a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item>
            <a-button type="primary" @click="handleSearch" :loading="loading">
              搜索
            </a-button>
            <a-button style="margin-left: 8px" @click="handleReset">
              重置
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 批量操作工具栏 -->
    <div class="toolbar-section" v-if="selectedRowKeys.length > 0">
      <a-card :bordered="false" style="margin-bottom: 16px;">
        <a-space>
          <span>已选择 {{ selectedRowKeys.length }} 项</span>
          <a-select
            v-model:value="batchRiskLevel"
            placeholder="选择风险等级"
            style="width: 150px"
          >
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
            <a-select-option value="NONE">无风险</a-select-option>
          </a-select>
          <a-button 
            type="primary" 
            @click="handleBatchUpdate"
            :loading="batchUpdating"
            :disabled="!batchRiskLevel"
          >
            批量更新
          </a-button>
          <a-button @click="handleClearSelection">
            清除选择
          </a-button>
        </a-space>
      </a-card>
    </div>


    <!-- 数据表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="handleTableChange"
        row-key="id"
        size="middle"
      >
        <!-- 自定义空状态 -->
        <template #emptyText>
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <div class="empty-title">暂无数据</div>
            <div class="empty-description">
              <span v-if="hasActiveFilters">
                当前筛选条件下没有找到相关数据，请尝试调整搜索条件
              </span>
              <span v-else>
                该数据类型下暂无高风险数据
              </span>
            </div>
          </div>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-select
              v-model:value="record.riskLevel"
              style="width: 100px"
              @change="(value) => handleRiskLevelChange(record.id, value)"
              :loading="record.updating"
            >
              <a-select-option value="HIGH">
                <a-tag color="#ff4d4f">高风险</a-tag>
              </a-select-option>
              <a-select-option value="MEDIUM">
                <a-tag color="#faad14">中风险</a-tag>
              </a-select-option>
              <a-select-option value="LOW">
                <a-tag color="#52c41a">低风险</a-tag>
              </a-select-option>
              <a-select-option value="NONE">
                <a-tag color="#d9d9d9">无风险</a-tag>
              </a-select-option>
            </a-select>
          </template>
          
          <template v-else-if="column.key === 'matchedKeywords'">
            <div v-if="record.matchedKeywords && Array.isArray(record.matchedKeywords)">
              <a-tag 
                v-for="keyword in record.matchedKeywords" 
                :key="keyword" 
                color="blue" 
                style="margin: 2px; cursor: pointer;"
                @click="handleKeywordClick(record, keyword)"
                class="clickable-keyword-tag"
              >
                {{ keyword }}
              </a-tag>
            </div>
            <span v-else>-</span>
          </template>
          
          <template v-else-if="column.key === 'matchedFields'">
            <div v-if="record.matchedFields && Array.isArray(record.matchedFields)">
              <a-tag v-for="field in record.matchedFields" :key="field" color="green" style="margin: 2px;">
                {{ field }}
              </a-tag>
            </div>
            <span v-else>-</span>
          </template>
          
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDetail(record)">
                查看详情
              </a-button>
              <a-button 
                v-if="shouldShowGenerateButton()"
                type="link" 
                size="small" 
                @click="handleGenerateCompetitorInfo(record)"
              >
                生成竞品信息
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 生成竞品信息模态框 -->
    <a-modal
      v-model:open="generateModalVisible"
      :title="`生成竞品信息 - ${currentRecord?.deviceName || currentRecord?.productDescription || ''}`"
      width="800px"
      @ok="handleConfirmGenerate"
      :confirm-loading="generateLoading"
      ok-text="确认生成"
      cancel-text="取消"
    >
      <div v-if="currentRecord" class="generate-form">
        <a-alert
          message="生成说明"
          description="系统将根据当前高风险数据生成竞品信息，请确认以下信息是否正确，可以进行编辑调整。"
          type="info"
          show-icon
          style="margin-bottom: 24px"
        />
        
        <a-form :model="generateForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
          <a-form-item label="设备名称" required>
            <a-input v-model:value="generateForm.productName" placeholder="请输入设备名称" />
          </a-form-item>
          
          <a-form-item label="申请人名称">
            <a-input v-model:value="generateForm.applicantName" placeholder="请输入申请人名称" />
          </a-form-item>
          
          <a-form-item label="品牌名称">
            <a-input v-model:value="generateForm.brandName" placeholder="请输入品牌名称" />
          </a-form-item>
          
          <a-form-item label="设备代码">
            <a-input v-model:value="generateForm.deviceCode" placeholder="请输入设备代码" />
          </a-form-item>
          
          <a-form-item label="设备等级">
            <a-select v-model:value="generateForm.deviceClass" placeholder="请选择设备等级">
              <a-select-option value="Class I">Class I</a-select-option>
              <a-select-option value="Class II">Class II</a-select-option>
              <a-select-option value="Class III">Class III</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="设备描述">
            <a-textarea v-model:value="generateForm.deviceDescription" placeholder="请输入设备描述" :rows="4" />
          </a-form-item>
          
          <a-form-item label="数据来源">
            <a-input v-model:value="generateForm.dataSource" disabled />
          </a-form-item>
          
          <a-form-item label="原始数据ID">
            <a-input v-model:value="generateForm.sourceDataId" disabled />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="数据详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered>
        <!-- 优先显示jd_country字段 -->
        <a-descriptions-item 
          v-if="currentRecord"
          label="来源国家"
          :span="1"
        >
          <div style="display: flex; align-items: center; gap: 8px;">
            <a-tag :color="getCountryColor(getJdCountryValue(currentRecord))" style="font-weight: bold;">
              {{ getCountryDisplayName(getJdCountryValue(currentRecord)) }}
            </a-tag>

          </div>
        </a-descriptions-item>
        
        <!-- 显示其他字段 -->
        <a-descriptions-item 
          v-for="(value, key) in filteredRecord" 
          :key="key" 
          :label="getColumnLabel(key)"
          :span="isWideField(key) ? 2 : 1"
        >
          <template v-if="key === 'riskLevel'">
            <a-tag :color="getRiskLevelColor(value)">
              {{ getRiskLevelLabel(value) }}
            </a-tag>
          </template>
          <template v-else-if="key === 'keywords' && value">
            <div v-if="typeof value === 'string'">
              <a-tag v-for="keyword in parseKeywords(value)" :key="keyword" color="blue" style="margin: 2px;">
                {{ keyword }}
              </a-tag>
            </div>
            <div v-else>
              {{ value }}
            </div>
          </template>
          <template v-else-if="isDateField(key) && value">
            {{ formatDate(value) }}
          </template>
          <template v-else-if="isUrlField(key) && value">
            <a :href="value" target="_blank" rel="noopener noreferrer">
              {{ value }}
            </a>
          </template>
          <template v-else>
            {{ formatFieldValue(value) }}
          </template>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { 
  getHighRiskDataByType, 
  updateRiskLevel, 
  batchUpdateRiskLevel,
  RISK_LEVEL_MAP,
  RISK_LEVEL_COLOR_MAP
} from '@/api/highRiskData'
import { generateProductFromHighRiskData, checkProductExists } from '@/api/api/product'

// Props
interface Props {
  dataType: string
  selectedCountry?: string
  selectedKeyword?: string
}

const props = defineProps<Props>()

// 定义组件事件
const emit = defineEmits<{
  'data-loaded': [dataType: string, data: any[], total: number]
  'keyword-click': [record: any, keyword: string]
  'competitor-generated': [product: any]
}>()

// 暴露方法给父组件
defineExpose({
  getCurrentData: () => tableData.value
})

// 响应式数据
const loading = ref(false)
const batchUpdating = ref(false)
const tableData = ref<any[]>([])
const selectedRowKeys = ref<number[]>([])
const batchRiskLevel = ref<string>('')
const detailModalVisible = ref(false)
const generateModalVisible = ref(false)
const generateLoading = ref(false)
const currentRecord = ref<any>({})

// 生成竞品信息表单
const generateForm = ref({
  productName: '',
  applicantName: '',
  brandName: '',
  deviceCode: '',
  deviceClass: 'Class II',
  deviceDescription: '',
  dataSource: '',
  sourceDataId: null as number | null
})

// 搜索表单
const searchForm = ref({
  keywords: '',
  riskLevel: ''
})

// 排序配置
const sortConfig = ref({
  sortBy: 'id', // 默认按ID排序
  sortDir: 'desc' // 默认降序
})

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = computed(() => {
  const riskLevelColumn = {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 120
  }
  
  const actionColumn = {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right'
  }

  // 根据数据类型添加特定列
  switch (props.dataType) {
    case 'device510k':
      return [
        riskLevelColumn,
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName' },
        { title: '申请人', dataIndex: 'applicant', key: 'applicant' },
        { title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived' },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    case 'recall':
      return [
        riskLevelColumn,
        { title: '产品描述', dataIndex: 'productDescription', key: 'productDescription' },
        { title: '召回公司', dataIndex: 'recallingFirm', key: 'recallingFirm' },
        { title: '事件日期', dataIndex: 'eventDatePosted', key: 'eventDatePosted' },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    case 'event':
      return [
        riskLevelColumn,
        { title: '品牌名称', dataIndex: 'brandName', key: 'brandName' },
        { title: '制造商', dataIndex: 'manufacturerName', key: 'manufacturerName' },
        { title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived' },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    case 'registration':
      return [
        riskLevelColumn,
        {
          title: '设备名称',
          dataIndex: 'deviceName',
          key: 'deviceName',
          ellipsis: true,
          customRender: ({text}: { text: any }) => {
            return text || '-'
          }
        },
        {
          title: '制造商',
          dataIndex: 'manufacturerName',
          key: 'manufacturerName',
          ellipsis: true,
          customRender: ({text}: { text: any }) => {
            return text || '-'
          }
        },
        {
          title: '创建日期',
          dataIndex: 'createdDate',
          key: 'createdDate',
          width: 120,
          customRender: ({text}: { text: any }) => {
            return text || '-'
          }
        },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    case 'guidance':
      return [
        riskLevelColumn,
        { title: '文档标题', dataIndex: 'title', key: 'title' },
        { title: '文档类型', dataIndex: 'topic', key: 'topic' },
        { title: '发布日期', dataIndex: 'publicationDate', key: 'publicationDate' },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    case 'customs':
      return [
        riskLevelColumn,
        { title: '案例编号', dataIndex: 'caseNumber', key: 'caseNumber' },
        { title: '案例标题', dataIndex: 'rulingResult', key: 'rulingResult' },
        { title: 'HS编码', dataIndex: 'hsCodeUsed', key: 'hsCodeUsed' },
        { title: '处理日期', dataIndex: 'caseDate', key: 'caseDate' },
        { title: '匹配关键词', key: 'matchedKeywords', width: 200 },
        { title: '匹配字段', key: 'matchedFields', width: 150 },
        actionColumn
      ]
    default:
      return [riskLevelColumn, actionColumn]
  }
})

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
  }
}))

// 检查是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return !!(
    (searchForm.value.keywords && searchForm.value.keywords.trim()) ||
    searchForm.value.riskLevel ||
    props.selectedKeyword ||
    props.selectedCountry ||
    sortConfig.value.sortDir !== 'desc' // 包含非默认排序
  )
})

// 过滤后的记录（排除国家相关字段，因为我们单独显示）
const filteredRecord = computed(() => {
  if (!currentRecord.value) return {}
  const filtered = { ...currentRecord.value }
  
  // 排除所有可能的国家字段，因为我们单独显示
  const countryFields = [
    'jdCountry', 'jd_country', 'JdCountry', 
    'countryCode', 'country', 
    'manufacturerCountry', 'manufacturerCountryCode'
  ]
  
  countryFields.forEach(field => {
    delete filtered[field]
  })
  
  return filtered
})

// 处理关键词点击
const handleKeywordClick = (record: any, keyword: string) => {
  emit('keyword-click', record, keyword)
}

// 获取数据类型对应的时间字段
const getTimeFieldForDataType = (dataType: string): string => {
  const timeFieldMap: Record<string, string> = {
    'device510k': 'dateReceived', // 上市前通告使用接收日期 (实体字段：dateReceived)
    'recall': 'eventDatePosted', // 召回记录使用事件发布日期 (实体字段：eventDatePosted)
    'event': 'dateReceived', // 事件报告使用接收日期 (实体字段：dateReceived)
    'registration': 'createdDate', // 注册记录使用创建日期 (实体字段：createdDate)
    'guidance': 'publicationDate', // 指导文档使用发布日期 (实体字段：publicationDate)
    'customs': 'caseDate' // 海关案例使用案例日期 (实体字段：caseDate)
  }
  
  return timeFieldMap[dataType] || 'id' // 如果没有找到对应字段，默认使用id
}

// 方法
const loadData = async () => {
  loading.value = true
  console.log(`🔄 开始加载 ${props.dataType} 类型的数据...`)
  
  try {
    // 获取当前数据类型对应的时间字段
    const timeField = getTimeFieldForDataType(props.dataType)
    
    const params: any = {
      page: pagination.value.current - 1,
      size: pagination.value.pageSize,
      sortBy: timeField, // 使用时间字段排序
      sortDir: sortConfig.value.sortDir // 使用当前排序方向
    }
    
    console.log(`📊 排序配置: 数据类型=${props.dataType}, 字段=${timeField}, 方向=${sortConfig.value.sortDir === 'desc' ? '降序' : '升序'}`)
    
    // 如果有选中的国家，添加国家筛选参数
    if (props.selectedCountry) {
      params.country = props.selectedCountry
    }
    
    // 如果有选中的关键词，添加关键词筛选参数
    if (props.selectedKeyword) {
      params.keyword = props.selectedKeyword
    }
    
    // 添加搜索表单中的关键词搜索
    if (searchForm.value.keywords && searchForm.value.keywords.trim()) {
      params.searchKeyword = searchForm.value.keywords.trim()
    }
    
    // 添加搜索表单中的风险等级筛选
    if (searchForm.value.riskLevel) {
      params.riskLevel = searchForm.value.riskLevel
    }
    
    const response = await getHighRiskDataByType(props.dataType, params)
    
    console.log(`📊 ${props.dataType} 数据响应:`, response)
    
    if (response && response.content && Array.isArray(response.content)) {
      tableData.value = response.content
      pagination.value.total = response.totalElements || 0
      console.log(`✅ 数据加载成功: ${tableData.value.length} 条记录，总数: ${pagination.value.total}`)
      
      // 检查是否有搜索条件但结果为0
      const hasSearchConditions = (params.searchKeyword && params.searchKeyword.trim()) || 
                                  params.keyword || 
                                  params.country || 
                                  params.riskLevel
      
      if (hasSearchConditions && tableData.value.length === 0) {
        let searchInfo = []
        if (params.searchKeyword) searchInfo.push(`关键词"${params.searchKeyword}"`)
        if (params.keyword) searchInfo.push(`筛选关键词"${params.keyword}"`)
        if (params.country) searchInfo.push(`国家"${params.country}"`)
        if (params.riskLevel) searchInfo.push(`风险等级"${params.riskLevel}"`)
        
        const searchText = searchInfo.join('、')
        message.info(`未找到符合${searchText}的数据`)
        console.log(`🔍 搜索结果为空: ${searchText}`)
      }
      
      // 触发数据加载完成事件
      emit('data-loaded', props.dataType, tableData.value, pagination.value.total)
    } else {
      console.warn(`⚠️ 响应数据为空或格式不正确:`, response)
      tableData.value = []
      pagination.value.total = 0
      
      // 触发数据加载完成事件（空数据）
      emit('data-loaded', props.dataType, [], 0)
    }
  } catch (error) {
    console.error(`❌ 加载 ${props.dataType} 数据失败:`, error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  console.log('🔍 执行关键词搜索:', searchForm.value)
  
  // 检查是否输入了搜索条件
  if (!searchForm.value.keywords?.trim() && !searchForm.value.riskLevel) {
    message.warning('请输入关键词或选择风险等级进行搜索')
    return
  }
  
  pagination.value.current = 1
  loadData()
}

const handleReset = () => {
  console.log('🔄 重置搜索条件')
  
  const hasSearchConditions = searchForm.value.keywords?.trim() || searchForm.value.riskLevel
  const hadSortChange = sortConfig.value.sortDir !== 'desc'
  
  searchForm.value = {
    keywords: '',
    riskLevel: ''
  }
  
  // 重置排序为默认降序
  sortConfig.value.sortDir = 'desc'
  
  pagination.value.current = 1
  
  if (hasSearchConditions || hadSortChange) {
    message.success('搜索条件已重置')
  }
  
  loadData()
}

// 处理排序变化
const handleSortChange = () => {
  console.log('📊 排序方式变化:', sortConfig.value.sortDir)
  pagination.value.current = 1 // 重置到第一页
  loadData()
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const handleRiskLevelChange = async (id: number, newRiskLevel: string) => {
  try {
    // 设置更新状态
    const record = tableData.value.find(item => item.id === id)
    if (record) {
      record.updating = true
    }
    
    await updateRiskLevel(props.dataType, id, newRiskLevel)
    message.success('风险等级更新成功')
    
    // 清除更新状态
    if (record) {
      record.updating = false
    }
  } catch (error) {
    console.error('更新风险等级失败:', error)
    message.error('更新风险等级失败')
    
    // 恢复原值
    const record = tableData.value.find(item => item.id === id)
    if (record) {
      record.riskLevel = record.originalRiskLevel
      record.updating = false
    }
  }
}

const handleBatchUpdate = async () => {
  if (!batchRiskLevel.value) {
    message.warning('请选择要更新的风险等级')
    return
  }
  
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要更新的数据')
    return
  }
  
  Modal.confirm({
    title: '确认批量更新',
    content: `确定要将选中的 ${selectedRowKeys.value.length} 条数据的风险等级更新为 ${getRiskLevelLabel(batchRiskLevel.value)} 吗？`,
    onOk: async () => {
      batchUpdating.value = true
      try {
        console.log('🔄 开始批量更新，IDs:', selectedRowKeys.value, '目标风险等级:', batchRiskLevel.value)
        
        const response = await batchUpdateRiskLevel(selectedRowKeys.value, batchRiskLevel.value)
        
        console.log('📊 批量更新响应:', response)
        
        if (response) {
          const updatedCount = response.updatedCount || 0
          const totalCount = response.totalCount || selectedRowKeys.value.length
          const errors = response.errors || []
          
          if (updatedCount > 0) {
            message.success(`批量更新成功，共更新 ${updatedCount} 条数据${totalCount > updatedCount ? `，失败 ${totalCount - updatedCount} 条` : ''}`)
            selectedRowKeys.value = []
            batchRiskLevel.value = ''
            loadData()
          } else {
            message.error(`批量更新失败，没有数据被更新${errors.length > 0 ? '：' + errors.slice(0, 3).join(', ') : ''}`)
          }
          
          if (errors.length > 0) {
            console.warn('⚠️ 批量更新部分失败:', errors)
          }
        } else {
          message.error('批量更新失败：服务器返回空响应')
        }
      } catch (error) {
        console.error('💥 批量更新失败:', error)
        
        let errorMessage = '批量更新失败'
        if (error.response?.data?.error) {
          errorMessage = error.response.data.error
        } else if (error.message) {
          errorMessage = `批量更新失败：${error.message}`
        }
        
        message.error(errorMessage)
      } finally {
        batchUpdating.value = false
      }
    }
  })
}

const handleClearSelection = () => {
  selectedRowKeys.value = []
  batchRiskLevel.value = ''
}

const handleViewDetail = (record: any) => {
  console.log('🔍 查看详情 - 完整记录数据:', record)
  console.log('🗂️ 记录中的所有键:', Object.keys(record))
  
  // 显示所有字段的名称和值
  console.log('📋 所有字段详情:')
  Object.keys(record).forEach(key => {
    console.log(`   ${key}: ${record[key]}`)
  })
  
  // 检查所有可能的国家字段
  const countryFields = Object.keys(record).filter(key => 
    key.toLowerCase().includes('country') || 
    key.toLowerCase().includes('jd') ||
    key.toLowerCase().includes('nation') ||
    key.toLowerCase().includes('region')
  )
  console.log('🗺️ 包含country/jd/nation/region的字段:', countryFields)
  countryFields.forEach(field => {
    console.log(`   ${field}: ${record[field]}`)
  })
  
  currentRecord.value = record
  detailModalVisible.value = true
}

// 判断是否显示生成竞品信息按钮（只在510K和注册记录中显示）
const shouldShowGenerateButton = () => {
  return props.dataType === 'device510k' || props.dataType === 'registration'
}

// 处理生成竞品信息
const handleGenerateCompetitorInfo = async (record: any) => {
  currentRecord.value = record
  
  // 先检查是否已经生成过竞品信息
  try {
    const checkResponse = await checkProductExists(props.dataType, record.id)
    if (checkResponse.data?.exists) {
      message.warning('该数据已生成过竞品信息，请勿重复生成')
      return
    }
  } catch (error) {
    console.error('检查竞品信息是否存在失败:', error)
  }
  
  // 初始化表单数据
  generateForm.value = {
    productName: getProductName(record),
    applicantName: getApplicantName(record),
    brandName: getBrandName(record),
    deviceCode: getDeviceCode(record),
    deviceClass: getDeviceClass(record),
    deviceDescription: getDeviceDescription(record),
    dataSource: props.dataType,
    sourceDataId: record.id
  }
  
  generateModalVisible.value = true
}

// 确认生成竞品信息
const handleConfirmGenerate = async () => {
  if (!generateForm.value.productName?.trim()) {
    message.warning('请输入设备名称')
    return
  }
  
  generateLoading.value = true
  
  try {
    const response = await generateProductFromHighRiskData(generateForm.value)
    
    if (response.success) {
      message.success('竞品信息生成成功！')
      generateModalVisible.value = false
      
      // 可以触发一个事件通知父组件
      emit('competitor-generated', response.data)
    } else {
      message.error(response.message || '生成竞品信息失败')
    }
  } catch (error: any) {
    console.error('生成竞品信息失败:', error)
    message.error('生成竞品信息失败：' + (error.message || '未知错误'))
  } finally {
    generateLoading.value = false
  }
}

// 根据数据类型和记录获取产品名称
const getProductName = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.deviceName || record.deviceGeneralName || ''
    case 'recall':
      return record.productDescription || record.deviceName || ''
    case 'event':
      return record.brandName || record.genericName || record.deviceName || ''
    case 'registration':
      return record.deviceName || record.proprietaryName || ''
    case 'guidance':
      return record.title || ''
    case 'customs':
      return record.rulingResult || record.caseNumber || ''
    default:
      return ''
  }
}

// 根据数据类型和记录获取申请人名称
const getApplicantName = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.applicant || record.contact || ''
    case 'recall':
      return record.recallingFirm || ''
    case 'event':
      return record.manufacturerName || ''
    case 'registration':
      return record.manufacturerName || record.ownerFirmName || ''
    case 'guidance':
      return 'FDA' // 指导文档通常是FDA发布的
    case 'customs':
      return '海关' // 海关案例
    default:
      return ''
  }
}

// 根据数据类型和记录获取品牌名称
const getBrandName = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.tradeName || record.brandName || ''
    case 'recall':
      return record.brandName || ''
    case 'event':
      return record.brandName || ''
    case 'registration':
      return record.brandName || record.proprietaryName || ''
    case 'guidance':
      return 'FDA指导文档'
    case 'customs':
      return '海关案例'
    default:
      return ''
  }
}

// 根据数据类型和记录获取设备代码
const getDeviceCode = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.kNumber || record.productCode || ''
    case 'recall':
      return record.productResNumber || record.cfresId || ''
    case 'event':
      return record.reportNumber || ''
    case 'registration':
      return record.registrationNumber || record.feiNumber || ''
    case 'guidance':
      return record.guidanceNumber || ''
    case 'customs':
      return record.caseNumber || record.hsCodeUsed || ''
    default:
      return ''
  }
}

// 根据数据类型和记录获取设备等级
const getDeviceClass = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.deviceClass || 'Class II'
    case 'registration':
      return record.deviceClass || record.riskClass || 'Class II'
    default:
      return 'Class II'
  }
}

// 根据数据类型和记录获取设备描述
const getDeviceDescription = (record: any): string => {
  switch (props.dataType) {
    case 'device510k':
      return record.statementOrSummary || record.decisionDescription || record.deviceName || ''
    case 'recall':
      return record.reasonForRecall || record.rootCauseDescription || record.productDescription || ''
    case 'event':
      return record.eventDescription || record.deviceProblem || ''
    case 'registration':
      return record.deviceNames || record.establishmentType || ''
    case 'guidance':
      return record.summary || record.title || ''
    case 'customs':
      return record.caseDescription || record.rulingResult || ''
    default:
      return ''
  }
}

// 暂时注释掉生成竞品信息功能
/*
const handleGenerateCompetitorInfo = (record: any) => {
  // 生成竞品信息并确认是否发送到竞品信息
  const { Modal } = require('ant-design-vue')
  
  Modal.confirm({
    title: '生成竞品信息',
    content: `确定要为这条数据生成竞品信息并发送到竞品信息模块吗？\n\n数据类型: ${props.dataType}\n记录ID: ${record.id}`,
    okText: '确定发送',
    cancelText: '取消',
    onOk() {
      // 这里调用生成竞品信息的API
      generateAndSendCompetitorInfo(record)
    },
    onCancel() {
      console.log('用户取消生成竞品信息')
    }
  })
}

const generateAndSendCompetitorInfo = async (record: any) => {
  try {
    // 这里应该调用后端API生成竞品信息
    message.info('正在生成竞品信息...')
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    message.success('竞品信息生成成功并已发送到竞品信息模块！')
    
  } catch (error: any) {
    console.error('生成竞品信息失败:', error)
    message.error('生成竞品信息失败：' + error.message)
  }
}
*/

const getColumnLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    // 通用字段
    id: 'ID',
    riskLevel: '风险等级',
    keywords: '关键词',
    dataSource: '数据来源',
    jdCountry: '来源国家',
    crawlTime: '爬取时间',
    dataStatus: '数据状态',
    createTime: '创建时间',
    updateTime: '更新时间',
    
    // 510K设备字段
    deviceName: '设备名称',
    deviceGeneralName: '通用名称',
    deviceClass: '设备类别',
    decisionResult: '决策结果',
    regulationNumber: '法规编号',
    applicant: '申请人',
    countryCode: '国家代码',
    dateReceived: '接收日期',
    decisionDate: '决策日期',
    kNumber: 'K编号',
    productCode: '产品代码',
    contactPerson: '联系人',
    address: '地址',
    address1: '地址1',
    address2: '地址2',
    city: '城市',
    state: '州/省',
    postalCode: '邮政编码',
    zipCode: '邮编',
    clearanceType: '许可类型',
    contact: '联系方式',
    advisoryCommittee: '咨询委员会',
    advisoryCommitteeDescription: '咨询委员会描述',
    decisionCode: '决策代码',
    decisionDescription: '决策描述',
    expeditedReviewFlag: '快速审查标志',
    openfda: 'OpenFDA数据',
    reviewAdvisoryCommittee: '审查咨询委员会',
    statementOrSummary: '声明或摘要',
    thirdPartyFlag: '第三方标志',
    meta: '元数据',
    deviceUrl: '设备URL',
    
    // 召回记录字段
    cfresId: '召回事件ID',
    productResNumber: '产品召回编号',
    resEventNumber: '召回事件编号',
    eventDateInitiated: '事件发起日期',
    eventDatePosted: '事件发布日期',
    eventDateTerminated: '事件终止日期',
    recallStatus: '召回状态',
    productDescription: '产品描述',
    codeInfo: '代码信息',
    kNumbers: 'K编号列表',
    recallingFirm: '召回公司',
    recallingFirmAddress: '召回公司地址',
    reasonForRecall: '召回原因',
    rootCauseDescription: '根本原因描述',
    action: '处理措施',
    productQuantity: '产品数量',
    distributionPattern: '分销模式',
    medicalSpecialty: '医疗专业',
    
    // 事件报告字段
    reportNumber: '报告编号',
    eventType: '事件类型',
    typeOfReport: '报告类型',
    dateOfEvent: '事件日期',
    dateReport: '报告日期',
    sourceType: '来源类型',
    reportSourceCode: '报告来源代码',
    brandName: '品牌名称',
    modelNumber: '型号',
    genericName: '通用名称',
    manufacturerName: '制造商名称',
    manufacturerCity: '制造商城市',
    manufacturerState: '制造商州/省',
    manufacturerCountry: '制造商国家',
    
    // 注册记录字段
    registrationNumber: '注册编号',
    feiNumber: 'FEI编号',
    proprietaryName: '专有名称',
    deviceClass: '设备类别',
    riskClass: '风险等级',
    statusCode: '状态代码',
    createdDate: '创建日期',
    regExpiryYear: '注册到期年份',
    establishmentType: '机构类型',
    manufacturerFullAddress: '制造商完整地址',
    manufacturerCountryCode: '制造商国家代码',
    usAgentBusinessName: '美国代理业务名称',
    usAgentContactInfo: '美国代理联系信息',
    ownerFirmName: '所有者公司名称',
    ownerFullAddress: '所有者完整地址',
    deviceNames: '设备名称列表',
    deviceClasses: '设备类别列表',
    regulationNumbers: '法规编号列表',
    
    // 指导文档字段
    title: '标题',
    publicationDate: '发布日期',
    topic: '话题/主题',
    guidanceStatus: '指导状态',
    documentUrl: '文档URL',
    sourceUrl: '来源URL',
    
    // 海关案例字段
    caseNumber: '案例编号',
    caseDate: '案例日期',
    hsCodeUsed: '使用的HS编码',
    rulingResult: '裁定结果',
    violationType: '违规类型',
    penaltyAmount: '处罚金额',
    caseType: '案例类型',
    caseDescription: '案例描述',
    
    // 其他常见字段
    description: '描述',
    content: '内容',
    remarks: '备注',
    notes: '注释'
  }
  return labelMap[key] || key
}

const getRiskLevelLabel = (riskLevel: string) => {
  return RISK_LEVEL_MAP[riskLevel as keyof typeof RISK_LEVEL_MAP] || riskLevel
}

const getRiskLevelColor = (riskLevel: string) => {
  return RISK_LEVEL_COLOR_MAP[riskLevel as keyof typeof RISK_LEVEL_COLOR_MAP] || '#d9d9d9'
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
    'IT': '意大利',
    'ES': '西班牙',
    'NL': '荷兰',
    'BE': '比利时',
    'SE': '瑞典',
    'NO': '挪威',
    'DK': '丹麦',
    'FI': '芬兰',
    'CH': '瑞士',
    'AT': '奥地利',
    'Unknown': '未知',
    'NULL': '未知',
    '': '未知'
  }
  return countryNames[countryCode] || countryCode || '未知'
}

// 获取国家标签颜色
const getCountryColor = (countryCode: string): string => {
  const countryColors: Record<string, string> = {
    'US': 'blue',
    'CN': 'red',
    'EU': 'purple',
    'JP': 'orange',
    'KR': 'cyan',
    'CA': 'green',
    'AU': 'gold',
    'GB': 'geekblue',
    'DE': 'lime',
    'FR': 'magenta',
    'Unknown': 'default',
    'NULL': 'default',
    '': 'default'
  }
  return countryColors[countryCode] || 'volcano'
}

// 智能获取jd_country字段的值（考虑多种可能的字段名）
const getJdCountryValue = (record: any): string => {
  if (!record) return ''
  
  // 按优先级检查可能的字段名
  const possibleFields = [
    'jdCountry',
    'jd_country', 
    'JdCountry',
    'countryCode',
    'country',
    'manufacturerCountry',
    'manufacturerCountryCode'
  ]
  
  for (const field of possibleFields) {
    if (record[field] !== undefined && record[field] !== null && record[field] !== '') {
      console.log(`✅ 找到国家字段: ${field} = ${record[field]}`)
      return record[field]
    }
  }
  
  console.log('❌ 未找到有效的国家字段')
  return 'Unknown'
}

// 获取使用的字段名（用于调试显示）
const getJdCountryFieldName = (record: any): string => {
  if (!record) return 'none'
  
  const possibleFields = [
    'jdCountry',
    'jd_country', 
    'JdCountry',
    'countryCode',
    'country',
    'manufacturerCountry',
    'manufacturerCountryCode'
  ]
  
  for (const field of possibleFields) {
    if (record[field] !== undefined && record[field] !== null && record[field] !== '') {
      return field
    }
  }
  
  return 'not found'
}

// 辅助函数：判断是否为宽字段
const isWideField = (key: string) => {
  const wideFields = [
    'description', 'content', 'statementOrSummary', 'decisionDescription',
    'advisoryCommitteeDescription', 'reasonForRecall', 'rootCauseDescription',
    'action', 'distributionPattern', 'rulingResult', 'caseDescription',
    'manufacturerFullAddress', 'ownerFullAddress', 'recallingFirmAddress',
    'proprietaryName', 'productDescription', 'mdrTextDescription', 'mdrTextAction',
    'riskDescription', 'measuresDescription', 'productProblemsList', 'remedialActionList',
    'openfda', 'title', 'summary', 'relatedDocuments', 'attachments', 'metadata',
    'hsCodeUsed'
  ]
  return wideFields.includes(key)
}

// 辅助函数：判断是否为日期字段
const isDateField = (key: string) => {
  const dateFields = [
    'dateReceived', 'decisionDate', 'eventDateInitiated', 'eventDatePosted',
    'eventDateTerminated', 'dateOfEvent', 'dateReport', 'publicationDate',
    'caseDate', 'crawlTime', 'createTime', 'updateTime', 'createdDate',
    'createdTime', 'updatedTime', 'publishDate', 'expireDate', 'dateAdded',
    'dateReportToFda'
  ]
  return dateFields.includes(key)
}

// 辅助函数：判断是否为URL字段
const isUrlField = (key: string) => {
  const urlFields = ['deviceUrl', 'documentUrl', 'sourceUrl', 'caseNumber', 'detailUrl', 'imageUrl']
  return urlFields.includes(key)
}

// 辅助函数：格式化日期
const formatDate = (value: any) => {
  if (!value) return '-'
  try {
    if (typeof value === 'string') {
      // 如果是字符串，尝试解析
      const date = new Date(value)
      if (!isNaN(date.getTime())) {
        return date.toLocaleDateString('zh-CN')
      }
    }
    return value
  } catch (error) {
    return value
  }
}

// 辅助函数：解析关键词
const parseKeywords = (value: string) => {
  if (!value) return []
  try {
    // 尝试解析JSON数组
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return parsed
    }
  } catch (error) {
    // 如果不是JSON，尝试按逗号分割
    return value.split(',').map(k => k.trim()).filter(k => k)
  }
  return [value]
}

// 辅助函数：格式化字段值
const formatFieldValue = (value: any) => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value, null, 2)
    } catch (error) {
      return String(value)
    }
  }
  return String(value)
}

// 监听数据类型变化
watch(() => props.dataType, () => {
  pagination.value.current = 1
  selectedRowKeys.value = []
  batchRiskLevel.value = ''
  
  // 重置排序为默认降序
  sortConfig.value.sortDir = 'desc'
  
  console.log(`🔄 数据类型变化为: ${props.dataType}，重置排序为降序`)
  loadData()
})

// 监听国家变化，重新加载数据
watch(() => props.selectedCountry, (newCountry, oldCountry) => {
  if (newCountry !== oldCountry) {
    console.log(`🔄 国家变化: ${oldCountry} -> ${newCountry}，重新加载数据`)
    pagination.value.current = 1 // 重置到第一页
    loadData()
  }
}, { immediate: false })

// 监听关键词变化，重新加载数据
watch(() => props.selectedKeyword, (newKeyword, oldKeyword) => {
  if (newKeyword !== oldKeyword) {
    console.log(`🔄 关键词变化: ${oldKeyword} -> ${newKeyword}，重新加载数据`)
    pagination.value.current = 1 // 重置到第一页
    loadData()
  }
}, { immediate: false })

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.high-risk-data-table {
  padding: 0;
}

.search-section {
  margin-bottom: 16px;
}

.toolbar-section {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f5f5f5;
}

.clickable-keyword-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.clickable-keyword-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 空状态样式 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-title {
  font-size: 16px;
  font-weight: 500;
  color: #666;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: #999;
  line-height: 1.5;
}
</style>

<template>
  <div class="competitor-info">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🏆 竞品信息管理</h1>
        <p>管理和分析竞争对手的医疗器械认证信息，支持数据推送、统计分析和风险监控</p>
      </div>
      <div class="header-actions">
        <a-space>
          <a-button @click="refreshData" :loading="refreshing" type="primary">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
          <a-button @click="showPushDataModal" type="primary">
            <template #icon>
              <UploadOutlined />
            </template>
            推送数据
          </a-button>
          <a-button @click="showGenerateFromDataModal" type="primary" ghost>
            <template #icon>
              <PlusOutlined />
            </template>
            从数据生成
          </a-button>
          <a-button @click="clearAllData" danger>
            <template #icon>
              <DeleteOutlined />
            </template>
            清空数据
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="总记录数"
              :value="statistics.totalRecords"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <DatabaseOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="活跃竞品"
              :value="statistics.activeCompetitors"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <TrophyOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="本月新增"
              :value="statistics.monthlyNew"
              :value-style="{ color: '#fa8c16' }"
            >
              <template #prefix>
                <RiseOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="风险提醒"
              :value="statistics.riskAlerts"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <WarningOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据列表 -->
    <div class="data-section">
      <a-card title="竞品信息列表" :bordered="false">
        <template #extra>
          <a-space>
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索产品名称、品牌、申请人或设备代码"
              style="width: 300px"
              @search="handleSearch"
              allow-clear
            />
            <a-select
              v-model:value="filterStatus"
              placeholder="筛选设备等级"
              style="width: 120px"
              @change="handleFilterChange"
              allow-clear
            >
              <a-select-option value="Class I">Class I</a-select-option>
              <a-select-option value="Class II">Class II</a-select-option>
              <a-select-option value="Class III">Class III</a-select-option>
            </a-select>
          </a-space>
        </template>

        <div v-if="loading" class="loading-container">
          <a-spin size="large" />
          <p>加载中...</p>
        </div>

        <div v-else-if="competitorList.length === 0" class="empty-container">
          <a-empty description="暂无竞品数据">
            <a-button type="primary" @click="showPushDataModal">
              推送第一批数据
            </a-button>
          </a-empty>
        </div>

        <div v-else>
          <a-table
            :columns="columns"
            :data-source="competitorList"
            :pagination="pagination"
            :loading="loading"
            row-key="id"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'isActive'">
                <a-tag :color="getActiveStatusColor(record.isActive)">
                  {{ getActiveStatusText(record.isActive) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'deviceClass'">
                <a-tag :color="getDeviceClassColor(record.deviceClass)">
                  {{ record.deviceClass || '-' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'createTime'">
                {{ formatDateTime(record.createTime) }}
              </template>
              <template v-else-if="column.key === 'dataSource'">
                <a-tag color="blue">{{ record.dataSource || '-' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'actions'">
                <a-space>
                  <a-button size="small" @click="viewDetails(record)">
                    <template #icon>
                      <EyeOutlined />
                    </template>
                    详情
                  </a-button>
                  <a-button size="small" @click="editRecord(record)">
                    <template #icon>
                      <EditOutlined />
                    </template>
                    编辑
                  </a-button>
                  <a-button size="small" danger @click="deleteRecord(record)">
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </a-card>
    </div>

    <!-- 推送数据模态框 -->
    <a-modal
      v-model:open="pushDataModalVisible"
      title="推送竞品数据"
      width="800px"
      @ok="handlePushData"
      :confirm-loading="pushDataLoading"
    >
      <div class="push-data-content">
        <a-alert
          message="数据推送说明"
          description="请按照JSON格式输入竞品数据，支持批量推送。数据将自动解析并存储到竞品信息库中。"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />
        
        <a-form :model="pushDataForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
          <a-form-item label="数据格式" required>
            <a-select v-model:value="pushDataForm.format" placeholder="选择数据格式">
              <a-select-option value="json">JSON格式</a-select-option>
              <a-select-option value="csv">CSV格式</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="数据内容" required>
            <a-textarea
              v-model:value="pushDataForm.data"
              placeholder="请输入竞品数据，支持JSON数组或CSV格式"
              :rows="10"
              style="font-family: monospace;"
            />
          </a-form-item>
          <a-form-item label="数据预览">
            <div class="data-preview">
              <a-tag v-if="parsedData.length > 0" color="green">
                解析成功: {{ parsedData.length }} 条记录
              </a-tag>
              <a-tag v-else color="red">
                数据格式错误或为空
              </a-tag>
            </div>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      :title="`产品详情 - ${selectedRecord?.productName || ''}`"
      width="1000px"
      :footer="null"
    >
      <div v-if="selectedRecord" class="detail-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="产品名称">
            {{ selectedRecord.productName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="品牌">
            {{ selectedRecord.brand || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="申请人名称">
            {{ selectedRecord.applicantName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="品牌名称">
            {{ selectedRecord.brandName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="型号">
            {{ selectedRecord.model || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="设备代码">
            {{ selectedRecord.deviceCode || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="产品类型">
            {{ selectedRecord.productType || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="设备等级">
            <a-tag :color="getDeviceClassColor(selectedRecord.deviceClass)">
              {{ selectedRecord.deviceClass || '-' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="数据来源">
            <a-tag color="blue">{{ selectedRecord.dataSource || '-' }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="原始数据ID">
            {{ selectedRecord.sourceDataId || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getActiveStatusColor(selectedRecord.isActive)">
              {{ getActiveStatusText(selectedRecord.isActive) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDateTime(selectedRecord.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatDateTime(selectedRecord.updateTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="设备描述" :span="2">
            <div style="max-height: 100px; overflow-y: auto;">
              {{ selectedRecord.deviceDescription || '无描述' }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">
            <div style="max-height: 100px; overflow-y: auto;">
              {{ selectedRecord.remarks || '无备注' }}
            </div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 编辑模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="`编辑产品信息 - ${editForm.productName || ''}`"
      width="800px"
      @ok="handleEditSave"
      :confirm-loading="editLoading"
    >
      <a-form
        :model="editForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
        :rules="editRules"
        ref="editFormRef"
      >
        <a-form-item label="产品名称" name="productName">
          <a-input v-model:value="editForm.productName" placeholder="请输入产品名称" />
        </a-form-item>
        <a-form-item label="品牌" name="brand">
          <a-input v-model:value="editForm.brand" placeholder="请输入品牌名称" />
        </a-form-item>
        <a-form-item label="申请人名称" name="applicantName">
          <a-input v-model:value="editForm.applicantName" placeholder="请输入申请人名称" />
        </a-form-item>
        <a-form-item label="品牌名称" name="brandName">
          <a-input v-model:value="editForm.brandName" placeholder="请输入品牌名称" />
        </a-form-item>
        <a-form-item label="型号" name="model">
          <a-input v-model:value="editForm.model" placeholder="请输入产品型号" />
        </a-form-item>
        <a-form-item label="设备代码" name="deviceCode">
          <a-input v-model:value="editForm.deviceCode" placeholder="请输入设备代码" />
        </a-form-item>
        <a-form-item label="产品类型" name="productType">
          <a-select v-model:value="editForm.productType" placeholder="请选择产品类型">
            <a-select-option value="医疗器械">医疗器械</a-select-option>
            <a-select-option value="诊断设备">诊断设备</a-select-option>
            <a-select-option value="治疗设备">治疗设备</a-select-option>
            <a-select-option value="监护设备">监护设备</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="设备等级" name="deviceClass">
          <a-select v-model:value="editForm.deviceClass" placeholder="请选择设备等级">
            <a-select-option value="Class I">Class I</a-select-option>
            <a-select-option value="Class II">Class II</a-select-option>
            <a-select-option value="Class III">Class III</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="数据来源" name="dataSource">
          <a-select v-model:value="editForm.dataSource" placeholder="请选择数据来源">
            <a-select-option value="device510k">510K设备</a-select-option>
            <a-select-option value="registration">设备注册</a-select-option>
            <a-select-option value="recall">召回记录</a-select-option>
            <a-select-option value="event">事件报告</a-select-option>
            <a-select-option value="guidance">指导文档</a-select-option>
            <a-select-option value="customs">海关案例</a-select-option>
            <a-select-option value="manual">手动录入</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="isActive">
          <a-select v-model:value="editForm.isActive" placeholder="请选择状态">
            <a-select-option :value="1">活跃</a-select-option>
            <a-select-option :value="0">非活跃</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="设备描述" name="deviceDescription">
          <a-textarea v-model:value="editForm.deviceDescription" placeholder="请输入设备描述" :rows="4" />
        </a-form-item>
        <a-form-item label="备注" name="remarks">
          <a-textarea v-model:value="editForm.remarks" placeholder="请输入备注信息" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 从数据生成竞品信息模态框 -->
    <a-modal
      v-model:open="generateFromDataModalVisible"
      title="从高风险数据生成竞品信息"
      width="1200px"
      :confirm-loading="generateLoading"
      @ok="generateCompetitorData"
      @cancel="closeGenerateModal"
    >
      <div class="generate-from-data-modal">
        <!-- 数据源选择 -->
        <div class="data-source-selection" style="margin-bottom: 20px;">
          <a-radio-group v-model:value="sourceDataType" @change="loadSourceData">
            <a-radio-button value="device510k">510K设备数据</a-radio-button>
            <a-radio-button value="registration">设备注册记录</a-radio-button>
          </a-radio-group>
          <a-button @click="loadSourceData" :loading="sourceDataLoading" style="margin-left: 10px;">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
        </div>

        <!-- 数据列表 -->
        <div class="source-data-table">
          <a-table
            :columns="sourceDataColumns"
            :data-source="sourceDataList"
            :loading="sourceDataLoading"
            :pagination="{ pageSize: 10, showSizeChanger: true }"
            row-key="id"
            :scroll="{ x: 800, y: 400 }"
            :row-selection="{
              selectedRowKeys: selectedSourceData.map(item => item.id),
              onChange: handleSourceDataSelection,
              type: 'checkbox'
            }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'riskLevel'">
                <a-tag :color="getRiskLevelColor(record.riskLevel)">
                  {{ getRiskLevelText(record.riskLevel) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'dataSource'">
                <a-tag color="blue">{{ record.dataSource }}</a-tag>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 选择提示 -->
        <div class="selection-info" style="margin-top: 16px;">
          <a-alert
            :message="`已选择 ${selectedSourceData.length} 条数据用于生成竞品信息`"
            type="info"
            show-icon
            v-if="selectedSourceData.length > 0"
          />
          <a-alert
            message="请选择要生成竞品信息的数据记录"
            type="warning"
            show-icon
            v-else
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  ReloadOutlined,
  UploadOutlined,
  DeleteOutlined,
  DatabaseOutlined,
  TrophyOutlined,
  RiseOutlined,
  WarningOutlined,
  EyeOutlined,
  EditOutlined,
  PlusOutlined
} from '@ant-design/icons-vue';
import {
  getCompetitorStatistics,
  getCompetitorList,
  pushDataToCompetitorInfo,
  clearCompetitorData,
  updateCompetitorInfo,
  deleteCompetitorInfo
} from '@/api/competitorInfo';
import { getHighRiskDataByType } from '@/api/highRiskData';
import { 
  getProductList, 
  getProductStatistics, 
  deleteProduct, 
  updateProduct 
} from '@/api/api/product';

// 响应式数据
const loading = ref(false);
const refreshing = ref(false);
const pushDataLoading = ref(false);
const editLoading = ref(false);
const searchKeyword = ref('');
const filterStatus = ref('');
const pushDataModalVisible = ref(false);
const detailModalVisible = ref(false);
const editModalVisible = ref(false);
const generateFromDataModalVisible = ref(false);
const selectedRecord = ref<any>(null);
const editFormRef = ref();

// 统计数据
const statistics = ref({
  totalRecords: 0,
  activeCompetitors: 0,
  monthlyNew: 0,
  riskAlerts: 0
});

// 竞品列表
const competitorList = ref<any[]>([]);

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
});

// 推送数据表单
const pushDataForm = ref({
  format: 'json',
  data: ''
});

// 编辑表单（基于Product实体）
const editForm = ref({
  id: null,
  productName: '',
  brand: '',
  applicantName: '',
  brandName: '',
  model: '',
  deviceCode: '',
  productType: '',
  deviceClass: '',
  deviceDescription: '',
  dataSource: '',
  sourceDataId: null as number | null,
  isActive: 1,
  remarks: ''
});

// 编辑表单验证规则
const editRules = {
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  brand: [
    { required: true, message: '请输入品牌名称', trigger: 'blur' }
  ],
  applicantName: [
    { required: true, message: '请输入申请人名称', trigger: 'blur' }
  ],
  deviceCode: [
    { required: true, message: '请输入设备代码', trigger: 'blur' }
  ],
  dataSource: [
    { required: true, message: '请选择数据来源', trigger: 'change' }
  ],
  isActive: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 从高风险数据生成竞品数据相关状态
const sourceDataList = ref<any[]>([]);
const selectedSourceData = ref<any[]>([]);
const sourceDataLoading = ref(false);
const generateLoading = ref(false);
const sourceDataType = ref('device510k'); // 默认选择510K设备
const sourceDataColumns = ref([
  {
    title: '选择',
    key: 'selection',
    width: 60,
    type: 'selection'
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 200,
    ellipsis: true
  },
  {
    title: '申请人/制造商',
    dataIndex: 'applicant',
    key: 'applicant',
    width: 150,
    ellipsis: true
  },
  {
    title: '品牌名称',
    dataIndex: 'tradeName',
    key: 'tradeName',
    width: 150,
    ellipsis: true
  },
  {
    title: '设备代码',
    dataIndex: 'kNumber',
    key: 'kNumber',
    width: 120
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100
  },
  {
    title: '数据来源',
    dataIndex: 'dataSource',
    key: 'dataSource',
    width: 100
  }
]);

// 表格列配置（基于Product实体）
const columns = [
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
    ellipsis: true
  },
  {
    title: '品牌',
    dataIndex: 'brand',
    key: 'brand',
    width: 120,
    ellipsis: true
  },
  {
    title: '申请人',
    dataIndex: 'applicantName',
    key: 'applicantName',
    width: 150,
    ellipsis: true
  },
  {
    title: '型号',
    dataIndex: 'model',
    key: 'model',
    width: 120
  },
  {
    title: '设备代码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 120
  },
  {
    title: '产品类型',
    dataIndex: 'productType',
    key: 'productType',
    width: 120
  },
  {
    title: '设备等级',
    dataIndex: 'deviceClass',
    key: 'deviceClass',
    width: 100
  },
  {
    title: '数据来源',
    dataIndex: 'dataSource',
    key: 'dataSource',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'isActive',
    key: 'isActive',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right'
  }
];

// 计算属性
const parsedData = computed(() => {
  if (!pushDataForm.value.data) return [];
  
  try {
    if (pushDataForm.value.format === 'json') {
      const data = JSON.parse(pushDataForm.value.data);
      return Array.isArray(data) ? data : [data];
    } else {
      // CSV解析逻辑（简化版）
      const lines = pushDataForm.value.data.split('\n');
      const headers = lines[0].split(',');
      return lines.slice(1).map(line => {
        const values = line.split(',');
        const obj: any = {};
        headers.forEach((header, index) => {
          obj[header.trim()] = values[index]?.trim() || '';
        });
        return obj;
      });
    }
  } catch (error) {
    return [];
  }
});

// 方法
const refreshData = async () => {
  refreshing.value = true;
  try {
    await Promise.all([loadStatistics(), loadCompetitorList()]);
    message.success('数据刷新成功');
  } catch (error: any) {
    console.error('刷新数据失败:', error);
    message.error('刷新数据失败: ' + error.message);
  } finally {
    refreshing.value = false;
  }
};

const loadStatistics = async () => {
  try {
    // 优先使用Product API获取统计数据
    const response = await getProductStatistics();
    if (response.success) {
      statistics.value = response.data;
    } else {
      // 如果Product API失败，尝试使用原有API
      const fallbackResponse = await getCompetitorStatistics();
      if (fallbackResponse.success) {
        statistics.value = fallbackResponse.data;
      }
    }
  } catch (error: any) {
    console.error('加载统计数据失败:', error);
    // 尝试使用原有API作为备选
    try {
      const fallbackResponse = await getCompetitorStatistics();
      if (fallbackResponse.success) {
        statistics.value = fallbackResponse.data;
      }
    } catch (fallbackError) {
      console.error('备选统计数据API也失败:', fallbackError);
    }
  }
};

const loadCompetitorList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.value.current - 1, // Product API使用0基索引
      size: pagination.value.pageSize,
      keyword: searchKeyword.value || undefined,
      deviceClass: filterStatus.value || undefined
    };
    
    console.log('加载产品列表，参数:', params);
    
    // 使用Product API获取产品列表
    const response = await getProductList(params);
    
    if (response.success) {
      // 直接使用Product API返回的数据，不进行格式转换
      competitorList.value = response.data.list || [];
      pagination.value.total = response.data.total || 0;
      
      console.log('产品列表加载成功:', competitorList.value.length, '条记录');
    } else {
      message.error(response.message || '加载产品列表失败');
      competitorList.value = [];
      pagination.value.total = 0;
    }
  } catch (error: any) {
    console.error('加载产品列表失败:', error);
    message.error('加载产品列表失败: ' + error.message);
    competitorList.value = [];
    pagination.value.total = 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.current = 1;
  loadCompetitorList();
};

const handleFilterChange = () => {
  pagination.value.current = 1;
  loadCompetitorList();
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadCompetitorList();
};

const showPushDataModal = () => {
  pushDataForm.value = {
    format: 'json',
    data: ''
  };
  pushDataModalVisible.value = true;
};

const handlePushData = async () => {
  if (!pushDataForm.value.data.trim()) {
    message.warning('请输入数据内容');
    return;
  }
  
  if (parsedData.value.length === 0) {
    message.error('数据格式错误，请检查输入内容');
    return;
  }
  
  pushDataLoading.value = true;
  try {
    const response = await pushDataToCompetitorInfo(parsedData.value);
    if (response.success) {
      message.success(`成功推送 ${parsedData.value.length} 条竞品数据`);
      pushDataModalVisible.value = false;
      await refreshData();
    } else {
      message.error(response.message || '推送数据失败');
    }
  } catch (error: any) {
    console.error('推送数据失败:', error);
    message.error('推送数据失败: ' + error.message);
  } finally {
    pushDataLoading.value = false;
  }
};

const clearAllData = () => {
  Modal.confirm({
    title: '确认清空',
    content: '确定要清空所有竞品数据吗？此操作不可恢复！',
    okText: '清空',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await clearCompetitorData();
        if (response.success) {
          message.success('所有竞品数据已清空');
          await refreshData();
        } else {
          message.error(response.message || '清空数据失败');
        }
      } catch (error: any) {
        console.error('清空数据失败:', error);
        message.error('清空数据失败: ' + error.message);
      }
    }
  });
};

const viewDetails = (record: any) => {
  selectedRecord.value = record;
  detailModalVisible.value = true;
};

const editRecord = (record: any) => {
  // 填充编辑表单（基于Product实体）
  editForm.value = {
    id: record.id,
    productName: record.productName || '',
    brand: record.brand || '',
    applicantName: record.applicantName || '',
    brandName: record.brandName || '',
    model: record.model || '',
    deviceCode: record.deviceCode || '',
    productType: record.productType || '',
    deviceClass: record.deviceClass || '',
    deviceDescription: record.deviceDescription || '',
    dataSource: record.dataSource || '',
    sourceDataId: record.sourceDataId || null,
    isActive: record.isActive !== undefined ? record.isActive : 1,
    remarks: record.remarks || ''
  };
  editModalVisible.value = true;
};

const handleEditSave = async () => {
  try {
    await editFormRef.value.validate();
    
    editLoading.value = true;
    
    // 直接使用Product格式的数据
    const productData = {
      id: editForm.value.id,
      productName: editForm.value.productName,
      brand: editForm.value.brand,
      applicantName: editForm.value.applicantName,
      brandName: editForm.value.brandName,
      model: editForm.value.model,
      deviceCode: editForm.value.deviceCode,
      productType: editForm.value.productType,
      deviceClass: editForm.value.deviceClass,
      deviceDescription: editForm.value.deviceDescription,
      dataSource: editForm.value.dataSource,
      sourceDataId: editForm.value.sourceDataId,
      isActive: editForm.value.isActive,
      remarks: editForm.value.remarks
    };
    
    console.log('保存产品数据:', productData);
    
    // 使用Product API更新
    const response = await updateProduct(editForm.value.id, productData);
    
    if (response.success) {
      message.success('产品信息更新成功');
      editModalVisible.value = false;
      await loadCompetitorList();
    } else {
      message.error(response.message || '更新失败');
    }
  } catch (error: any) {
    if (error.errorFields) {
      message.error('请检查表单输入');
    } else {
      console.error('更新产品信息失败:', error);
      message.error('更新产品信息失败: ' + error.message);
    }
  } finally {
    editLoading.value = false;
  }
};

const deleteRecord = (record: any) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除产品 "${record.productName} - ${record.brand}" 吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        console.log('删除产品:', record.id);
        const response = await deleteProduct(record.id);
        
        if (response.success) {
          message.success('产品删除成功');
          await loadCompetitorList();
        } else {
          message.error(response.message || '删除失败');
        }
      } catch (error: any) {
        console.error('删除产品信息失败:', error);
        message.error('删除产品信息失败: ' + error.message);
      }
    }
  });
};

// Product实体的状态处理函数
const getActiveStatusColor = (isActive: number) => {
  return isActive === 1 ? 'green' : 'red';
};

const getActiveStatusText = (isActive: number) => {
  return isActive === 1 ? '活跃' : '非活跃';
};

const getDeviceClassColor = (deviceClass: string) => {
  switch (deviceClass) {
    case 'Class I':
      return 'green';
    case 'Class II':
      return 'orange';
    case 'Class III':
      return 'red';
    default:
      return 'default';
  }
};

const getRiskColor = (riskLevel: string) => {
  switch (riskLevel) {
    case 'low':
      return 'green';
    case 'medium':
      return 'orange';
    case 'high':
      return 'red';
    default:
      return 'default';
  }
};

const getRiskText = (riskLevel: string) => {
  switch (riskLevel) {
    case 'low':
      return '低风险';
    case 'medium':
      return '中风险';
    case 'high':
      return '高风险';
    default:
      return '未知';
  }
};

const formatDate = (date: string) => {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD');
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-';
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

// 从数据生成竞品信息相关函数
const showGenerateFromDataModal = () => {
  generateFromDataModalVisible.value = true;
  loadSourceData();
};

const closeGenerateModal = () => {
  generateFromDataModalVisible.value = false;
  selectedSourceData.value = [];
  sourceDataList.value = [];
};

const loadSourceData = async () => {
  sourceDataLoading.value = true;
  try {
    const response = await getHighRiskDataByType(sourceDataType.value, {
      page: 0,
      size: 100,
      sortBy: 'id',
      sortDir: 'desc'
    });
    
    if (response && response.data && response.data.content) {
      sourceDataList.value = response.data.content;
      console.log(`加载${sourceDataType.value}数据成功:`, sourceDataList.value.length, '条记录');
    } else {
      console.warn('响应数据格式不正确:', response);
      sourceDataList.value = [];
    }
  } catch (error) {
    console.error('加载源数据失败:', error);
    message.error('加载源数据失败');
    sourceDataList.value = [];
  } finally {
    sourceDataLoading.value = false;
  }
};

const handleSourceDataSelection = (_selectedRowKeys: any[], selectedRows: any[]) => {
  selectedSourceData.value = selectedRows;
  console.log('选中的数据:', selectedSourceData.value);
};

const generateCompetitorData = async () => {
  if (selectedSourceData.value.length === 0) {
    message.warning('请选择要生成竞品信息的数据记录');
    return;
  }

  generateLoading.value = true;
  try {
    // 转换数据格式
    const competitorDataList = selectedSourceData.value.map(sourceData => {
      const competitorData: any = {
        deviceName: sourceData.deviceName || '',
        manufacturerBrand: sourceData.applicant || sourceData.manufacturerName || '',
        deviceCode: sourceData.kNumber || sourceData.registrationNumber || '',
        usageScope: sourceData.deviceClass || '',
        deviceDescription: sourceData.deviceName || '',
        dataSource: sourceDataType.value === 'device510k' ? '510K设备数据' : '设备注册记录',
        certificationType: sourceDataType.value === 'device510k' ? '510K' : '注册',
        status: 'active',
        riskLevel: sourceData.riskLevel?.toLowerCase() || 'medium',
        certificationDate: sourceData.dateReceived || sourceData.createdDate || null,
        expiryDate: null,
        remarks: `从${sourceDataType.value === 'device510k' ? '510K设备数据' : '设备注册记录'}生成，原始ID: ${sourceData.id}`
      };

      // 如果是510K数据，添加额外信息
      if (sourceDataType.value === 'device510k') {
        competitorData.deviceCode = sourceData.kNumber || '';
        competitorData.manufacturerBrand = sourceData.applicant || sourceData.tradeName || '';
      }

      // 如果是注册数据，添加额外信息
      if (sourceDataType.value === 'registration') {
        competitorData.deviceCode = sourceData.registrationNumber || '';
        competitorData.manufacturerBrand = sourceData.manufacturerName || '';
      }

      return competitorData;
    });

    console.log('生成的竞品数据:', competitorDataList);

    // 批量保存竞品数据
    let successCount = 0;
    let failCount = 0;

    for (const competitorData of competitorDataList) {
      try {
        const response = await pushDataToCompetitorInfo([competitorData]);
        
        if (response.success) {
          successCount++;
        } else {
          failCount++;
          console.error('保存失败:', competitorData.deviceName, response.message);
        }
      } catch (error) {
        failCount++;
        console.error('保存失败:', competitorData.deviceName, error);
      }
    }

    // 显示结果
    if (successCount > 0) {
      message.success(`成功生成 ${successCount} 条竞品信息`);
      if (failCount > 0) {
        message.warning(`${failCount} 条数据生成失败`);
      }
      
      // 关闭模态框并刷新数据
      closeGenerateModal();
      await loadCompetitorList();
    } else {
      message.error('所有数据生成失败，请检查数据格式');
    }

  } catch (error: any) {
    console.error('生成竞品数据失败:', error);
    message.error('生成竞品数据失败: ' + error.message);
  } finally {
    generateLoading.value = false;
  }
};

const getRiskLevelColor = (riskLevel: string) => {
  switch (riskLevel?.toLowerCase()) {
    case 'high':
      return 'red';
    case 'medium':
      return 'orange';
    case 'low':
      return 'green';
    default:
      return 'default';
  }
};

const getRiskLevelText = (riskLevel: string) => {
  switch (riskLevel?.toLowerCase()) {
    case 'high':
      return '高风险';
    case 'medium':
      return '中风险';
    case 'low':
      return '低风险';
    default:
      return '未知';
  }
};

// 生命周期
onMounted(() => {
  console.log('竞品信息管理页面初始化');
  refreshData();
});
</script>

<style scoped>
.competitor-info {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.header-content p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.stats-section {
  margin-bottom: 24px;
}

.data-section {
  width: 100%;
}

.loading-container {
  text-align: center;
  padding: 40px 0;
}

.empty-container {
  text-align: center;
  padding: 40px 0;
}

.push-data-content {
  padding: 16px 0;
}

.data-preview {
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  min-height: 32px;
  display: flex;
  align-items: center;
}

.detail-content {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .competitor-info {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .stats-section .ant-col {
    margin-bottom: 16px;
  }
}

/* 生成竞品数据模态框样式 */
.generate-from-data-modal {
  .data-source-selection {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    background: #fafafa;
    border-radius: 6px;
    margin-bottom: 16px;
  }

  .source-data-table {
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    overflow: hidden;
  }

  .selection-info {
    .ant-alert {
      margin-bottom: 0;
    }
  }
}
</style>

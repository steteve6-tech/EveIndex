<template>
  <a-modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    width="90%"
    :footer="null"
    @cancel="handleClose"
    class="ai-judgment-dialog"
  >
    <!-- 设备数据模块：显示统计信息 -->
    <div v-if="moduleType === 'DEVICE_DATA'" class="statistics-section">
      <a-alert
        message="数据统计"
        type="info"
        :closable="false"
        show-icon
        class="statistics-alert"
      >
        <template #description>
          <div class="statistics-content">
            <div class="stat-item">
              <span class="stat-label">黑名单过滤数据：</span>
              <span class="stat-value highlight-danger">{{ statistics.filteredByBlacklistCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">高风险数据：</span>
              <span class="stat-value highlight-warning">{{ statistics.highRiskCount }}</span>
            </div>
            <div class="stat-item" v-if="statistics.newBlacklistKeywords.length > 0">
              <span class="stat-label">新增黑名单关键词：</span>
              <a-tag
                v-for="keyword in statistics.newBlacklistKeywords"
                :key="keyword"
                color="red"
                size="small"
                class="keyword-tag"
              >
                {{ keyword }}
              </a-tag>
            </div>
          </div>
        </template>
      </a-alert>
    </div>

    <!-- 待审核列表 -->
    <div class="pending-list-section">
      <div class="list-header">
        <div class="header-left">
          <a-button
            type="primary"
            :disabled="selectedJudgments.length === 0"
            @click="handleBatchConfirm"
            :loading="batchConfirming"
          >
            批量确认 ({{ selectedJudgments.length }})
          </a-button>
          <a-button
            @click="handleRefresh"
            :loading="loading"
          >
            刷新
          </a-button>
        </div>
        <div class="header-right">
          <span class="total-count">待审核: {{ totalCount }} 条</span>
        </div>
      </div>

      <!-- 空状态提示 -->
      <a-empty
        v-if="!loading && pendingList.length === 0"
        description="暂无待审核的AI判断数据"
        style="margin: 40px 0"
      >
        <template #image>
          <CheckCircleOutlined style="font-size: 64px; color: #52c41a;" />
        </template>
        <p style="color: #52c41a; margin-top: 16px;">
          ✅ 所有AI判断已处理完成！
        </p>
      </a-empty>

      <a-table
        v-else
        :data-source="pendingList"
        :loading="loading"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :columns="tableColumns"
        :scroll="{ y: 500 }"
        row-key="id"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'entityType'">
            <a-tag :color="getEntityTypeColor(record.entityType)" size="small">
              {{ getEntityTypeName(record.entityType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'suggestedRiskLevel'">
            <a-tag :color="getRiskLevelColor(record.suggestedRiskLevel)" size="small">
              {{ getRiskLevelName(record.suggestedRiskLevel) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'suggestedRemark'">
            <div class="remark-cell">{{ record.suggestedRemark || '-' }}</div>
          </template>

          <template v-else-if="column.key === 'filteredByBlacklist'">
            <a-tag v-if="record.filteredByBlacklist" color="red" size="small">
              已过滤
            </a-tag>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.key === 'blacklistKeywords'">
            <div v-if="record.blacklistKeywords && record.blacklistKeywords.length > 0">
              <a-tag
                v-for="(keyword, index) in record.blacklistKeywords"
                :key="index"
                color="red"
                size="small"
                class="keyword-tag-small"
              >
                {{ keyword }}
              </a-tag>
            </div>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.key === 'createdTime'">
            {{ formatDateTime(record.createdTime) }}
          </template>

          <template v-else-if="column.key === 'expireTime'">
            <span :class="{ 'expire-warning': isExpiringSoon(record.expireTime) }">
              {{ formatDateTime(record.expireTime) }}
            </span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="primary"
                size="small"
                @click="handleConfirmSingle(record)"
                :loading="confirmingIds.has(record.id)"
              >
                确认
              </a-button>
              <a-button
                danger
                size="small"
                @click="handleReject(record)"
                :loading="rejectingIds.has(record.id)"
              >
                拒绝
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { CheckCircleOutlined } from '@ant-design/icons-vue';
import {
  getPendingList,
  getDeviceDataStatistics,
  confirmJudgment,
  batchConfirmJudgments,
  rejectJudgment,
  type AIPendingJudgment,
  type DeviceDataStatistics
} from '@/api/aiJudgment';
import type { TableColumnsType } from 'ant-design-vue';

// Props
const props = defineProps<{
  visible: boolean;
  moduleType: 'DEVICE_DATA' | 'CERT_NEWS';
}>();

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirmed', count: number): void;
  (e: 'rejected', id: number): void;
}>();

// 响应式数据
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

const dialogTitle = computed(() => {
  return props.moduleType === 'DEVICE_DATA'
    ? 'AI判断审核 - 设备数据模块'
    : 'AI判断审核 - 医疗认证模块';
});

const loading = ref(false);
const batchConfirming = ref(false);
const confirmingIds = ref(new Set<number>());
const rejectingIds = ref(new Set<number>());

const pendingList = ref<AIPendingJudgment[]>([]);
const selectedRowKeys = ref<number[]>([]);
const selectedJudgments = ref<AIPendingJudgment[]>([]);
const totalCount = ref(0);

const statistics = ref<DeviceDataStatistics>({
  filteredByBlacklistCount: 0,
  highRiskCount: 0,
  newBlacklistKeywords: []
});

// 表格列配置
const baseColumns: TableColumnsType = [
  { title: '数据类型', key: 'entityType', dataIndex: 'entityType', width: 120 },
  { title: '建议风险等级', key: 'suggestedRiskLevel', dataIndex: 'suggestedRiskLevel', width: 120 },
  { title: 'AI判断备注', key: 'suggestedRemark', dataIndex: 'suggestedRemark', width: 200 },
];

const deviceDataColumns: TableColumnsType = [
  { title: '黑名单过滤', key: 'filteredByBlacklist', dataIndex: 'filteredByBlacklist', width: 100 },
  { title: '黑名单关键词', key: 'blacklistKeywords', dataIndex: 'blacklistKeywords', width: 180 },
];

const timeColumns: TableColumnsType = [
  { title: '创建时间', key: 'createdTime', dataIndex: 'createdTime', width: 160 },
  { title: '过期时间', key: 'expireTime', dataIndex: 'expireTime', width: 160 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
];

const tableColumns = computed(() => {
  if (props.moduleType === 'DEVICE_DATA') {
    return [...baseColumns, ...deviceDataColumns, ...timeColumns];
  } else {
    return [...baseColumns, ...timeColumns];
  }
});

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadData();
  }
});

// 加载数据
async function loadData() {
  loading.value = true;
  try {
    // 加载待审核列表
    await loadPendingList();

    // 如果是设备数据模块，加载统计信息
    if (props.moduleType === 'DEVICE_DATA') {
      await loadStatistics();
    }
  } catch (error: any) {
    console.error('加载数据失败:', error);
    message.error('加载数据失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
}

// 加载待审核列表
async function loadPendingList() {
  try {
    const response = await getPendingList(props.moduleType, 'PENDING');
    if (response.data.success) {
      pendingList.value = response.data.data;
      totalCount.value = response.data.total;
    }
  } catch (error) {
    console.error('加载待审核列表失败:', error);
    throw error;
  }
}

// 加载统计信息（仅设备数据模块）
async function loadStatistics() {
  try {
    const response = await getDeviceDataStatistics();
    if (response.data.success) {
      statistics.value = response.data.data;
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
  }
}

// 刷新数据
function handleRefresh() {
  loadData();
}

// 表格选择变化
function onSelectChange(selectedKeys: number[]) {
  selectedRowKeys.value = selectedKeys;
  selectedJudgments.value = pendingList.value.filter(item => selectedKeys.includes(item.id));
}

// 确认单个判断
async function handleConfirmSingle(record: AIPendingJudgment) {
  Modal.confirm({
    title: '确认操作',
    content: `确认执行AI判断结果吗？\n数据类型: ${getEntityTypeName(record.entityType)}\n建议风险等级: ${getRiskLevelName(record.suggestedRiskLevel)}`,
    onOk: async () => {
      confirmingIds.value.add(record.id);

      try {
        const response = await confirmJudgment(record.id, 'user');

        if (response.data.success) {
          message.success('确认成功');
          emit('confirmed', 1);
          await loadData(); // 重新加载数据
        } else {
          message.error('确认失败: ' + response.data.message);
        }
      } catch (error: any) {
        console.error('确认失败:', error);
        message.error('确认失败: ' + (error.message || '未知错误'));
      } finally {
        confirmingIds.value.delete(record.id);
      }
    }
  });
}

// 批量确认
async function handleBatchConfirm() {
  if (selectedJudgments.value.length === 0) {
    message.warning('请先选择要确认的数据');
    return;
  }

  Modal.confirm({
    title: '批量确认',
    content: `确认批量执行 ${selectedJudgments.value.length} 条AI判断结果吗？`,
    onOk: async () => {
      batchConfirming.value = true;

      try {
        const ids = selectedJudgments.value.map(j => j.id);
        const response = await batchConfirmJudgments(ids, 'user');

        if (response.data.success) {
          message.success(`成功确认 ${selectedJudgments.value.length} 条数据`);
          emit('confirmed', selectedJudgments.value.length);
          selectedJudgments.value = [];
          selectedRowKeys.value = [];
          await loadData(); // 重新加载数据
        } else {
          message.error('批量确认失败: ' + response.data.message);
        }
      } catch (error: any) {
        console.error('批量确认失败:', error);
        message.error('批量确认失败: ' + (error.message || '未知错误'));
      } finally {
        batchConfirming.value = false;
      }
    }
  });
}

// 拒绝判断
async function handleReject(record: AIPendingJudgment) {
  Modal.confirm({
    title: '拒绝确认',
    content: '确认拒绝此AI判断吗？拒绝后将不会应用该判断结果。',
    onOk: async () => {
      rejectingIds.value.add(record.id);

      try {
        const response = await rejectJudgment(record.id, 'user');

        if (response.data.success) {
          message.success('已拒绝');
          emit('rejected', record.id);
          await loadData(); // 重新加载数据
        } else {
          message.error('拒绝失败: ' + response.data.message);
        }
      } catch (error: any) {
        console.error('拒绝失败:', error);
        message.error('拒绝失败: ' + (error.message || '未知错误'));
      } finally {
        rejectingIds.value.delete(record.id);
      }
    }
  });
}

// 关闭弹窗
function handleClose() {
  dialogVisible.value = false;
}

// 工具函数
function getEntityTypeName(type: string): string {
  const typeMap: Record<string, string> = {
    'Application': '510K申请',
    'Recall': '召回记录',
    'Event': '不良事件',
    'Registration': '注册记录',
    'Document': '指导文件',
    'Customs': '海关案例'
  };
  return typeMap[type] || type;
}

function getEntityTypeColor(type: string): string {
  const colorMap: Record<string, string> = {
    'Application': 'blue',
    'Recall': 'red',
    'Event': 'orange',
    'Registration': 'green',
    'Document': 'purple',
    'Customs': 'cyan'
  };
  return colorMap[type] || '';
}

function getRiskLevelName(level: string): string {
  const levelMap: Record<string, string> = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险'
  };
  return levelMap[level] || level;
}

function getRiskLevelColor(level: string): string {
  const colorMap: Record<string, string> = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'green'
  };
  return colorMap[level] || '';
}

function formatDateTime(dateStr: string): string {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function isExpiringSoon(expireTime: string): boolean {
  if (!expireTime) return false;
  const expire = new Date(expireTime);
  const now = new Date();
  const daysUntilExpire = (expire.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
  return daysUntilExpire <= 3; // 3天内过期显示警告
}

// 组件挂载时加载数据
onMounted(() => {
  if (props.visible) {
    loadData();
  }
});
</script>

<style scoped>
.statistics-section {
  margin-bottom: 20px;
}

.statistics-alert {
  background-color: #f4f4f5;
  border: 1px solid #e9e9eb;
}

.statistics-content {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-weight: 500;
  color: #606266;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
}

.stat-value.highlight-danger {
  color: #f56c6c;
}

.stat-value.highlight-warning {
  color: #e6a23c;
}

.keyword-tag {
  margin-right: 5px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.header-left {
  display: flex;
  gap: 10px;
}

.total-count {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.remark-cell {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.keyword-tag-small {
  margin-right: 3px;
  margin-bottom: 3px;
}

.expire-warning {
  color: #f56c6c;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>

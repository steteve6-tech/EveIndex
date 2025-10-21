import axios from 'axios';

const API_BASE_URL = '/api/ai-judgment';
const NEW_DATA_API_BASE_URL = '/api/new-data';

// ==================== AI判断相关接口 ====================

export interface AIPendingJudgment {
  id: number;
  moduleType: string;
  entityType: string;
  entityId: number;
  judgeResult: any;
  suggestedRiskLevel: string;
  suggestedRemark: string;
  blacklistKeywords: string[] | null;
  filteredByBlacklist: boolean;
  status: string;
  createdTime: string;
  expireTime: string;
  confirmedBy?: string;
  confirmedTime?: string;
  rejectedBy?: string;
  rejectedTime?: string;
}

export interface DeviceDataStatistics {
  filteredByBlacklistCount: number;
  highRiskCount: number;
  newBlacklistKeywords: string[];
}

export interface PendingListResponse {
  success: boolean;
  data: AIPendingJudgment[];
  total: number;
}

export interface CountResponse {
  success: boolean;
  data: {
    [key: string]: number;
  };
}

export interface StatisticsResponse {
  success: boolean;
  data: DeviceDataStatistics;
}

/**
 * 获取待审核AI判断列表
 */
export function getPendingList(moduleType: string, status?: string) {
  return axios.get<PendingListResponse>(`${API_BASE_URL}/pending`, {
    params: { moduleType, status }
  });
}

/**
 * 获取待审核AI判断数量
 */
export function getPendingCount(moduleType: string) {
  return axios.get<CountResponse>(`${API_BASE_URL}/pending/count`, {
    params: { moduleType }
  });
}

/**
 * 获取设备数据统计信息（黑名单过滤、高风险、新增关键词）
 */
export function getDeviceDataStatistics() {
  return axios.get<StatisticsResponse>(`${API_BASE_URL}/pending/statistics/device-data`);
}

/**
 * 获取单个判断详情
 */
export function getJudgmentDetails(id: number) {
  return axios.get(`${API_BASE_URL}/pending/${id}`);
}

/**
 * 确认单个AI判断
 */
export function confirmJudgment(id: number, confirmedBy?: string) {
  return axios.post(`${API_BASE_URL}/confirm/${id}`, null, {
    params: { confirmedBy }
  });
}

/**
 * 批量确认AI判断
 */
export function batchConfirmJudgments(ids: number[], confirmedBy?: string) {
  return axios.post(`${API_BASE_URL}/batch-confirm`, {
    ids,
    confirmedBy
  });
}

/**
 * 拒绝AI判断
 */
export function rejectJudgment(id: number, rejectedBy?: string) {
  return axios.post(`${API_BASE_URL}/reject/${id}`, null, {
    params: { rejectedBy }
  });
}

/**
 * 清理过期记录
 */
export function cleanupExpired() {
  return axios.post(`${API_BASE_URL}/cleanup-expired`);
}

// ==================== 新增数据相关接口 ====================

export interface NewDataCountResponse {
  success: boolean;
  data: {
    [key: string]: number;
  };
}

/**
 * 获取新增数据数量（所有类型）
 */
export function getNewDataCount(moduleType: string) {
  return axios.get<NewDataCountResponse>(`${NEW_DATA_API_BASE_URL}/count`, {
    params: { moduleType }
  });
}

/**
 * 获取指定类型新增数据数量
 */
export function getNewDataCountByType(entityType: string) {
  return axios.get(`${NEW_DATA_API_BASE_URL}/count/${entityType}`);
}

/**
 * 获取新增数据列表（分页）
 */
export function getNewDataList(entityType: string, page: number = 0, size: number = 20) {
  return axios.get(`${NEW_DATA_API_BASE_URL}/list`, {
    params: { entityType, page, size }
  });
}

/**
 * 批量将所有数据设置为普通数据（非新增）
 */
export function batchSetAllAsNormal() {
  return axios.post(`${NEW_DATA_API_BASE_URL}/batch-set-all-normal`);
}

/**
 * 自动标记已查看（用于页面加载时）
 */
export function autoMarkViewed(moduleType: string) {
  return axios.post(`${NEW_DATA_API_BASE_URL}/auto-mark-viewed`, null, {
    params: { moduleType }
  });
}

/**
 * 标记数据为已查看
 */
export function markDataAsViewed(entityType: string, ids: number[]) {
  return axios.post(`${NEW_DATA_API_BASE_URL}/mark-viewed`, {
    entityType,
    ids
  });
}

/**
 * 取消新增标记
 */
export function clearNewFlag(entityType: string, ids: number[]) {
  return axios.post(`${NEW_DATA_API_BASE_URL}/clear-new-flag`, {
    entityType,
    ids
  });
}

/**
 * 清理已查看的新增数据
 */
export function cleanupViewedData(daysToKeep: number = 7) {
  return axios.post(`${NEW_DATA_API_BASE_URL}/cleanup-viewed`, null, {
    params: { daysToKeep }
  });
}

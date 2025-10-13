// @ts-ignore
/* eslint-disable */
import request from "@/request";

// 数据类型映射
export const DATA_TYPE_MAP = {
  device510k: '510K设备',
  recall: '召回记录',
  event: '事件报告',
  registration: '注册记录',
  guidance: '指导文档',
  customs: '海关案例'
}

// 风险等级映射
export const RISK_LEVEL_MAP = {
  HIGH: '高风险',
  MEDIUM: '中风险',
  LOW: '低风险',
  NONE: '无风险'
}

// 风险等级颜色映射
export const RISK_LEVEL_COLOR_MAP = {
  HIGH: '#ff4d4f',
  MEDIUM: '#faad14',
  LOW: '#52c41a',
  NONE: '#d9d9d9'
}

// 获取高风险数据统计
export async function getHighRiskStatistics() {
  return request('/high-risk-data/statistics', {
    method: 'GET'
  })
}

// 按国家获取高风险数据统计
export async function getHighRiskStatisticsByCountry() {
  return request('/high-risk-data/statistics/by-country', {
    method: 'GET'
  })
}

// 获取关键词统计
export async function getKeywordStatistics(country?: string) {
  const params: any = {}
  if (country) {
    params.country = country
  }
  
  return request('/high-risk-data/keywords/statistics', {
    method: 'GET',
    params
  })
}

// 按类型获取高风险数据
export async function getHighRiskDataByType(
  dataType: string,
  params?: {
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
    country?: string
    keyword?: string
  }
) {
  return request(`/high-risk-data/${dataType}`, {
    method: 'GET',
    params
  })
}

// 更新风险等级
export async function updateRiskLevel(dataType: string, id: number, riskLevel: string) {
  return request(`/high-risk-data/${dataType}/${id}/risk-level`, {
    method: 'PUT',
    data: { riskLevel }
  })
}

// 批量更新风险等级
export async function batchUpdateRiskLevel(ids: number[], riskLevel: string) {
  return request('/high-risk-data/batch/risk-level', {
    method: 'PUT',
    data: { ids, riskLevel }
  })
}

// 更新关键词
export async function updateKeywords(dataType: string, id: number, oldKeyword: string, newKeyword: string) {
  return request(`/high-risk-data/${dataType}/${id}/keywords`, {
    method: 'PUT',
    data: { oldKeyword, newKeyword }
  })
}

// ==================== DeviceMatchKeywords 管理API ====================

// 获取所有关键词
export async function getAllKeywords() {
  return request('/high-risk-data/keywords', {
    method: 'GET'
  })
}

// 根据类型获取关键词
export async function getKeywordsByType(keywordType: string) {
  return request('/high-risk-data/keywords/by-type', {
    method: 'GET',
    params: { keywordType }
  })
}

// 创建关键词
export async function createKeyword(keyword: string, keywordType: string, enabled: boolean = true) {
  return request('/high-risk-data/keywords', {
    method: 'POST',
    data: { keyword, keywordType, enabled }
  })
}

// 更新关键词
export async function updateKeyword(id: number, data: { keyword?: string; keywordType?: string; enabled?: boolean }) {
  return request(`/high-risk-data/keywords/${id}`, {
    method: 'PUT',
    data
  })
}

// 删除关键词
export async function deleteKeyword(id: number) {
  return request(`/high-risk-data/keywords/${id}`, {
    method: 'DELETE'
  })
}

// 搜索关键词
export async function searchKeywords(keyword: string) {
  return request('/high-risk-data/keywords/search', {
    method: 'GET',
    params: { keyword }
  })
}

// 更新数据备注
export async function updateDataRemarks(id: number, remarks: string) {
  return request(`/high-risk-data/remarks/${id}`, {
    method: 'PUT',
    data: { remarks }
  })
}
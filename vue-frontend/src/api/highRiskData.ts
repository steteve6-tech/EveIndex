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
  return request('/api/high-risk-data/statistics', {
    method: 'GET'
  })
}

// 按国家获取高风险数据统计
export async function getHighRiskStatisticsByCountry() {
  return request('/api/high-risk-data/statistics/by-country', {
    method: 'GET'
  })
}

// 通用获取高风险数据函数
export async function getHighRiskData(params?: {
  countryCode?: string
  riskLevel?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}) {
  return request('/api/high-risk-data/search', {
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
  }
) {
  return request(`/api/high-risk-data/${dataType}`, {
    method: 'GET',
    params
  })
}

// 更新风险等级
export async function updateRiskLevel(dataType: string, id: number, riskLevel: string) {
  return request(`/api/high-risk-data/${dataType}/${id}/risk-level`, {
    method: 'PUT',
    data: { riskLevel }
  })
}

// 批量更新风险等级
export async function batchUpdateRiskLevel(ids: number[], riskLevel: string) {
  return request('/api/high-risk-data/batch/risk-level', {
    method: 'PUT',
    data: { ids, riskLevel }
  })
}

import request from '@/request'

// 定时爬取配置接口类型定义
export interface ScheduledCrawlerConfig {
  id?: number
  moduleName: string
  crawlerName: string
  countryCode?: string
  enabled: boolean
  cronExpression: string
  description?: string
  crawlParams?: string
  lastExecutionTime?: string
  nextExecutionTime?: string
  lastExecutionStatus?: string
  lastExecutionResult?: string
  executionCount?: number
  successCount?: number
  failureCount?: number
  createdAt?: string
  updatedAt?: string
  deleted?: number
  updating?: boolean
  triggering?: boolean
}

// API响应接口
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  error?: string
  message?: string
  timestamp?: string
  total?: number
  page?: number
  size?: number
  totalPages?: number
}

/**
 * 获取所有定时爬取配置
 */
export async function getScheduledCrawlerConfigs(): Promise<ApiResponse<ScheduledCrawlerConfig[]>> {
  return request({
    url: '/scheduled-crawlers',
    method: 'GET'
  })
}

/**
 * 分页获取定时爬取配置
 */
export async function getScheduledCrawlerConfigsPage(params: {
  page?: number
  size?: number
  sort?: string
}): Promise<ApiResponse<{
  content: ScheduledCrawlerConfig[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}>> {
  return request({
    url: '/scheduled-crawlers/page',
    method: 'GET',
    params
  })
}

/**
 * 根据模块获取配置
 */
export async function getScheduledCrawlerConfigsByModule(moduleName: string): Promise<ApiResponse<ScheduledCrawlerConfig[]>> {
  return request({
    url: `/scheduled-crawlers/module/${moduleName}`,
    method: 'GET'
  })
}

/**
 * 创建或更新配置
 */
export async function saveScheduledCrawlerConfig(config: ScheduledCrawlerConfig): Promise<ApiResponse<ScheduledCrawlerConfig>> {
  return request({
    url: '/scheduled-crawlers',
    method: 'POST',
    data: config
  })
}

/**
 * 更新配置
 */
export async function updateScheduledCrawlerConfig(config: ScheduledCrawlerConfig): Promise<ApiResponse<ScheduledCrawlerConfig>> {
  return request({
    url: '/scheduled-crawlers',
    method: 'PUT',
    data: config
  })
}

/**
 * 删除配置
 */
export async function deleteScheduledCrawlerConfig(id: number): Promise<ApiResponse> {
  return request({
    url: `/scheduled-crawlers/${id}`,
    method: 'DELETE'
  })
}

/**
 * 启用/禁用配置
 */
export async function toggleScheduledCrawlerConfig(id: number, enabled: boolean): Promise<ApiResponse> {
  return request({
    url: `/scheduled-crawlers/${id}/toggle`,
    method: 'PUT',
    params: { enabled }
  })
}

/**
 * 创建默认配置
 */
export async function createDefaultScheduledCrawlerConfigs(): Promise<ApiResponse> {
  return request({
    url: '/scheduled-crawlers/default',
    method: 'POST'
  })
}


/**
 * 手动触发CertNewsData爬虫
 */
export async function triggerCertNewsDataCrawler(crawlerName: string): Promise<ApiResponse<{
  success: boolean
  message: string
  executionTime: string
}>> {
  return request({
    url: `/scheduled-crawlers/trigger/certnewsdata/${crawlerName}`,
    method: 'POST'
  })
}

/**
 * 手动触发设备数据爬虫
 */
export async function triggerDeviceDataCrawler(
  crawlerName: string, 
  countryCode: string, 
  moduleName: string
): Promise<ApiResponse<{
  success: boolean
  message: string
  executionTime: string
}>> {
  return request({
    url: `/scheduled-crawlers/trigger/devicedata/${crawlerName}`,
    method: 'POST',
    params: { countryCode, moduleName }
  })
}

/**
 * 获取CertNewsData爬虫状态
 */
export async function getCertNewsDataCrawlerStatus(): Promise<ApiResponse<Record<string, {
  enabled: boolean
  lastExecutionTime?: string
  lastExecutionStatus?: string
  lastExecutionResult?: string
  nextExecutionTime?: string
  executionCount: number
  successCount: number
  failureCount: number
}>>> {
  return request({
    url: '/scheduled-crawlers/status/certnewsdata',
    method: 'GET'
  })
}

/**
 * 获取设备数据爬虫状态
 */
export async function getDeviceDataCrawlerStatus(): Promise<ApiResponse<Record<string, Record<string, {
  enabled: boolean
  lastExecutionTime?: string
  lastExecutionStatus?: string
  lastExecutionResult?: string
  nextExecutionTime?: string
  executionCount: number
  successCount: number
  failureCount: number
}>>>> {
  return request({
    url: '/scheduled-crawlers/status/devicedata',
    method: 'GET'
  })
}

/**
 * 获取所有爬虫状态
 */
export async function getAllCrawlerStatus(): Promise<ApiResponse<{
  certnewsdata: Record<string, any>
  devicedata: Record<string, any>
  statistics: Record<string, any>
}>> {
  return request({
    url: '/scheduled-crawlers/status/all',
    method: 'GET'
  })
}

/**
 * 获取定时爬取状态（统一接口）
 */
export async function getScheduledCrawlerStatus(): Promise<ApiResponse> {
  return getAllCrawlerStatus()
}

// 导出所有接口
export default {
  getScheduledCrawlerConfigs,
  getScheduledCrawlerConfigsPage,
  getScheduledCrawlerConfigsByModule,
  saveScheduledCrawlerConfig,
  updateScheduledCrawlerConfig,
  deleteScheduledCrawlerConfig,
  toggleScheduledCrawlerConfig,
  createDefaultScheduledCrawlerConfigs,
  // getScheduledCrawlerStatistics,
  triggerCertNewsDataCrawler,
  triggerDeviceDataCrawler,
  getCertNewsDataCrawlerStatus,
  getDeviceDataCrawlerStatus,
  getAllCrawlerStatus,
  getScheduledCrawlerStatus
}

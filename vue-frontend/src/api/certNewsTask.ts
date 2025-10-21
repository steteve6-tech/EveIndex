import request from '../request'

export interface CertNewsTaskConfig {
  id?: number
  taskName: string
  crawlerType: string
  cronExpression: string
  enabled: boolean
  keyword?: string
  maxRecords?: number
  description?: string
  lastExecuteTime?: string
  nextExecuteTime?: string
  lastExecuteStatus?: string
  lastExecuteMessage?: string
  createdAt?: string
  updatedAt?: string
}

export interface CertNewsTaskLog {
  id: number
  taskId: number
  taskName: string
  crawlerType: string
  startTime: string
  endTime: string
  status: string
  successCount: number
  errorCount: number
  message: string
  errorMessage?: string
  createdAt: string
}

/**
 * 获取所有任务配置
 */
export const getAllTasks = () => {
  return request.get('/api/cert-news-tasks')
}

/**
 * 获取启用的任务
 */
export const getEnabledTasks = () => {
  return request.get('/api/cert-news-tasks/enabled')
}

/**
 * 根据ID获取任务
 */
export const getTaskById = (id: number) => {
  return request.get(`/api/cert-news-tasks/${id}`)
}

/**
 * 创建任务
 */
export const createTask = (data: CertNewsTaskConfig) => {
  return request.post('/api/cert-news-tasks', data)
}

/**
 * 更新任务
 */
export const updateTask = (id: number, data: CertNewsTaskConfig) => {
  return request.put(`/api/cert-news-tasks/${id}`, data)
}

/**
 * 删除任务
 */
export const deleteTask = (id: number) => {
  return request.delete(`/api/cert-news-tasks/${id}`)
}

/**
 * 启用任务
 */
export const enableTask = (id: number) => {
  return request.post(`/api/cert-news-tasks/${id}/enable`)
}

/**
 * 禁用任务
 */
export const disableTask = (id: number) => {
  return request.post(`/api/cert-news-tasks/${id}/disable`)
}

/**
 * 手动执行任务
 */
export const executeTask = (id: number) => {
  return request.post(`/api/cert-news-tasks/${id}/execute`)
}

/**
 * 获取任务执行日志
 */
export const getTaskLogs = (id: number) => {
  return request.get(`/api/cert-news-tasks/${id}/logs`)
}

/**
 * 获取所有任务日志
 */
export const getAllLogs = () => {
  return request.get('/api/cert-news-tasks/logs/all')
}

/**
 * 获取最近的日志
 */
export const getRecentLogs = (limit: number = 10) => {
  return request.get(`/api/cert-news-tasks/logs/recent?limit=${limit}`)
}

/**
 * 获取爬虫类型列表
 */
export const getCrawlerTypes = () => {
  return request.get('/api/cert-news-tasks/crawler-types')
}

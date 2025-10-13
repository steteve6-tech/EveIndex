import request from '../request';

/**
 * 爬虫信息接口
 */
export interface CrawlerInfo {
  crawlerName: string;
  displayName: string;
  countryCode: string;
  crawlerType: string;
  description: string;
  version: string;
  available: boolean;
  status: CrawlerStatus;
  schema: any;
  testing?: boolean;
}

/**
 * 爬虫状态接口
 */
export interface CrawlerStatus {
  status: string;
  lastExecutionTime: number;
  lastExecutionResult: string;
  totalExecutions: number;
  successCount: number;
  failureCount: number;
  successRate: number;
}

/**
 * 任务信息接口
 */
export interface TaskInfo {
  id: number;
  taskName: string;
  crawlerName: string;
  countryCode: string;
  taskType: string;
  description: string;
  enabled: boolean;
  successRate: number;
  lastExecutionTime: string;
  nextExecutionTime: string;
}

/**
 * 系统统计信息接口
 */
export interface SystemStatistics {
  totalCrawlers: number;
  runningCrawlers: number;
  totalTasks: number;
  overallSuccessRate: number;
}

/**
 * 任务配置请求接口
 */
export interface TaskConfigRequest {
  taskName: string;
  crawlerName: string;
  countryCode: string;
  taskType: string;
  paramsVersion: string;
  parameters: string;
  cronExpression?: string;
  description: string;
  enabled: boolean;
  priority: number;
  timeoutMinutes?: number;
  retryCount?: number;
}

/**
 * 批量执行请求接口
 */
export interface BatchExecuteRequest {
  crawlers: string[];
  mode: string;
  maxRecords?: number;
  executeType: string;
  failureStrategy: string;
  interval?: number;
}

/**
 * 获取所有爬虫信息
 */
export const getAllCrawlers = () => {
  return request.get('/unified/crawlers');
};

/**
 * 获取指定爬虫信息
 */
export const getCrawler = (crawlerName: string) => {
  return request.get(`/unified/crawlers/${crawlerName}`);
};

/**
 * 获取爬虫状态
 */
export const getCrawlerStatus = (crawlerName: string) => {
  return request.get(`/unified/crawlers/${crawlerName}/status`);
};

/**
 * 批量执行爬虫
 */
export const batchExecuteCrawlers = (data: BatchExecuteRequest) => {
  return request.post('/unified/crawlers/batch-execute', data);
};

/**
 * 批量测试爬虫
 */
export const batchTestCrawlers = (crawlerNames: string[]) => {
  return request.post('/unified/crawlers/batch-test', crawlerNames);
};

/**
 * 获取所有爬虫Schema
 */
export const getAllSchemas = () => {
  return request.get('/unified/schemas');
};

/**
 * 获取指定爬虫的Schema
 */
export const getCrawlerSchema = (crawlerName: string) => {
  return request.get(`/unified/schemas/${crawlerName}`);
};

/**
 * 创建任务
 */
export const createTask = (data: TaskConfigRequest) => {
  return request.post('/unified/tasks', data);
};

/**
 * 获取任务列表
 */
export const getTasks = (params?: {
  countryCode?: string;
  crawlerName?: string;
  taskType?: string;
  enabled?: boolean;
  page?: number;
  size?: number;
}) => {
  return request.get('/unified/tasks', { params });
};

/**
 * 执行任务
 */
export const executeTask = (taskId: number) => {
  return request.post(`/unified/tasks/${taskId}/execute`);
};

/**
 * 获取任务执行历史
 */
export const getTaskHistory = (taskId: number) => {
  return request.get(`/unified/tasks/${taskId}/history`);
};

/**
 * 获取系统统计信息
 */
export const getSystemStatistics = () => {
  return request.get('/unified/statistics');
};

/**
 * 获取爬虫参数预设
 */
export const getCrawlerPreset = (crawlerName: string) => {
  return request.get(`/unified/crawlers/${crawlerName}/preset`);
};

/**
 * 更新爬虫参数预设
 */
export const updateCrawlerPreset = (crawlerName: string, parameters: any) => {
  return request.put(`/unified/crawlers/${crawlerName}/preset`, { parameters });
};

/**
 * 验证预设参数
 */
export const validateCrawlerPreset = (crawlerName: string, parameters: any) => {
  return request.post(`/unified/crawlers/${crawlerName}/preset/validate`, parameters);
};
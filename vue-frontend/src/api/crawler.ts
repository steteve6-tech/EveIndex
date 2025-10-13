import request from '@/request';

// ==================== 爬虫管理 ====================

/**
 * 获取所有爬虫信息
 */
export const getCrawlers = () => {
  return request.get('/api/unified/crawlers');
};

/**
 * 获取爬虫Schema
 */
export const getCrawlerSchema = (name: string) => {
  return request.get(`/api/unified/schemas/${name}`);
};

/**
 * 获取所有Schema
 */
export const getAllSchemas = () => {
  return request.get('/api/unified/schemas');
};

/**
 * 启用爬虫
 */
export const enableCrawler = (name: string) => {
  return request.put(`/api/unified/crawlers/${name}/enable`);
};

/**
 * 停用爬虫
 */
export const disableCrawler = (name: string) => {
  return request.put(`/api/unified/crawlers/${name}/disable`);
};

// ==================== 参数预设管理 ====================

/**
 * 创建参数预设
 */
export const createPreset = (data: any) => {
  return request.post('/api/unified/presets', data);
};

/**
 * 查询预设列表
 */
export const getPresets = (params?: any) => {
  return request.get('/api/unified/presets', { params });
};

/**
 * 获取预设详情
 */
export const getPreset = (id: number) => {
  return request.get(`/api/unified/presets/${id}`);
};

/**
 * 更新参数预设
 */
export const updatePreset = (id: number, data: any) => {
  return request.put(`/api/unified/presets/${id}`, data);
};

/**
 * 复制参数预设
 */
export const copyPreset = (id: number, newName: string) => {
  return request.post(`/api/unified/presets/${id}/copy`, { newName });
};

/**
 * 删除参数预设
 */
export const deletePreset = (id: number) => {
  return request.delete(`/api/unified/presets/${id}`);
};

/**
 * 验证参数
 */
export const validateParams = (crawlerName: string, params: any) => {
  return request.post('/api/unified/presets/validate', { 
    crawlerName, 
    params 
  });
};

// ==================== 任务执行 ====================

/**
 * 手动触发任务
 */
export const triggerTask = (id: number, triggeredBy?: string) => {
  return request.post(`/api/unified/tasks/${id}/trigger`, null, {
    params: { triggeredBy: triggeredBy || 'MANUAL' }
  });
};

/**
 * 暂停任务
 */
export const pauseTask = (id: number) => {
  return request.post(`/api/unified/tasks/${id}/pause`);
};

/**
 * 恢复任务
 */
export const resumeTask = (id: number) => {
  return request.post(`/api/unified/tasks/${id}/resume`);
};

// ==================== 监控查询 ====================

/**
 * 获取运行中任务
 */
export const getRunningTasks = () => {
  return request.get('/api/unified/monitor/running');
};

/**
 * 获取执行历史
 */
export const getExecutionHistory = (params?: any) => {
  return request.get('/api/unified/monitor/history', { params });
};

/**
 * 获取任务统计
 */
export const getTaskStatistics = (taskId: number) => {
  return request.get(`/api/unified/monitor/statistics/${taskId}`);
};

/**
 * 获取系统总览
 */
export const getSystemOverview = () => {
  return request.get('/api/unified/monitor/overview');
};


import request from '@/request';

// ==================== 爬虫管理 ====================

/**
 * 获取所有爬虫信息
 */
export const getCrawlers = () => {
  return request.get('/unified/crawlers');
};

/**
 * 获取爬虫Schema
 */
export const getCrawlerSchema = (name: string) => {
  return request.get(`/unified/schemas/${name}`);
};

/**
 * 获取所有Schema
 */
export const getAllSchemas = () => {
  return request.get('/unified/schemas');
};

/**
 * 启用爬虫
 */
export const enableCrawler = (name: string) => {
  return request.put(`/unified/crawlers/${name}/enable`);
};

/**
 * 停用爬虫
 */
export const disableCrawler = (name: string) => {
  return request.put(`/unified/crawlers/${name}/disable`);
};

// ==================== 参数预设管理 ====================

/**
 * 创建参数预设
 */
export const createPreset = (data: any) => {
  return request.post('/unified/presets', data);
};

/**
 * 查询预设列表
 */
export const getPresets = (params?: any) => {
  return request.get('/unified/presets', { params });
};

/**
 * 获取预设详情
 */
export const getPreset = (id: number) => {
  return request.get(`/unified/presets/${id}`);
};

/**
 * 更新参数预设
 */
export const updatePreset = (id: number, data: any) => {
  return request.put(`/unified/presets/${id}`, data);
};

/**
 * 复制参数预设
 */
export const copyPreset = (id: number, newName: string) => {
  return request.post(`/unified/presets/${id}/copy`, { newName });
};

/**
 * 删除参数预设
 */
export const deletePreset = (id: number) => {
  return request.delete(`/unified/presets/${id}`);
};

/**
 * 更新任务信息
 */
export const updateTask = (id: number, data: any) => {
  return request.put(`/unified/presets/${id}`, data);
};

/**
 * 验证参数
 */
export const validateParams = (crawlerName: string, params: any) => {
  return request.post('/unified/presets/validate', {
    crawlerName, 
    params 
  });
};

// ==================== 任务执行 ====================

/**
 * 手动触发任务
 */
export const triggerTask = (id: number, triggeredBy?: string) => {
  return request.post(`/unified/tasks/${id}/trigger`, null, {
    params: { triggeredBy: triggeredBy || 'MANUAL' }
  });
};

/**
 * 暂停任务
 */
export const pauseTask = (id: number) => {
  return request.post(`/unified/tasks/${id}/pause`);
};

/**
 * 恢复任务
 */
export const resumeTask = (id: number) => {
  return request.post(`/unified/tasks/${id}/resume`);
};

// ==================== 监控查询 ====================

/**
 * 获取运行中任务
 */
export const getRunningTasks = () => {
  return request.get('/unified/monitor/running');
};

/**
 * 获取执行历史
 */
export const getExecutionHistory = (params?: any) => {
  return request.get('/unified/monitor/history', { params });
};

/**
 * 获取任务统计
 */
export const getTaskStatistics = (taskId: number) => {
  return request.get(`/unified/monitor/statistics/${taskId}`);
};

/**
 * 获取系统总览
 */
export const getSystemOverview = () => {
  return request.get('/unified/monitor/overview');
};

// ==================== 爬虫预设编辑器 ====================

/**
 * 获取爬虫预设（用于预设编辑器）
 */
export const getCrawlerPreset = (crawlerName: string) => {
  return request.get(`/unified/crawlers/${crawlerName}/preset`);
};

/**
 * 保存爬虫预设（用于预设编辑器）
 */
export const saveCrawlerPreset = (crawlerName: string, presetData: any) => {
  return request.post(`/unified/crawlers/${crawlerName}/preset`, presetData);
};

/**
 * 验证爬虫预设参数
 */
export const validateCrawlerPreset = (crawlerName: string, params: any) => {
  return request.post('/unified/presets/validate', {
    crawlerName,
    params
  });
};

// ==================== 爬虫测试和执行 ====================

/**
 * 测试爬虫
 */
export const testCrawler = (crawlerName: string, params?: any) => {
  return request.post(`/unified/crawlers/${crawlerName}/test`, params || {});
};

/**
 * 执行爬虫
 */
export const executeCrawler = (crawlerName: string, params: any) => {
  return request.post(`/unified/crawlers/${crawlerName}/execute`, params);
};

/**
 * 批量测试爬虫
 */
export const batchTestCrawlers = (crawlerNames: string[]) => {
  return request.post('/unified/crawlers/batch-test', { crawlerNames });
};

/**
 * 批量执行爬虫
 */
export const batchExecuteCrawlers = (data: any) => {
  return request.post('/unified/crawlers/batch-execute', data);
};


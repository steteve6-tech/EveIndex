import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface SystemConfig {
  id?: number;
  configCategory: string;
  configKey: string;
  configValue: string;
  description?: string;
  valueType: string;
  enabled?: boolean;
  createTime?: string;
  updateTime?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  total?: number;
}

/**
 * 获取所有配置
 */
export const getAllConfigs = async (): Promise<SystemConfig[]> => {
  const response = await axios.get<ApiResponse<SystemConfig[]>>(`${API_BASE_URL}/api/system/config/all`);
  return response.data.data || [];
};

/**
 * 获取所有配置（按分类分组）
 */
export const getAllConfigsGrouped = async (): Promise<Record<string, SystemConfig[]>> => {
  const response = await axios.get<ApiResponse<Record<string, SystemConfig[]>>>(`${API_BASE_URL}/api/system/config/grouped`);
  return response.data.data || {};
};

/**
 * 根据分类获取配置
 */
export const getConfigsByCategory = async (category: string): Promise<SystemConfig[]> => {
  const response = await axios.get<ApiResponse<SystemConfig[]>>(`${API_BASE_URL}/api/system/config/category/${category}`);
  return response.data.data || [];
};

/**
 * 根据配置键获取配置值
 */
export const getConfigByKey = async (configKey: string): Promise<string | null> => {
  const response = await axios.get<ApiResponse<{ configValue: string }>>(`${API_BASE_URL}/api/system/config/key/${configKey}`);
  return response.data.data?.configValue || null;
};

/**
 * 保存或更新单个配置
 */
export const saveConfig = async (config: SystemConfig): Promise<SystemConfig> => {
  const response = await axios.post<ApiResponse<SystemConfig>>(`${API_BASE_URL}/api/system/config/save`, config);
  return response.data.data!;
};

/**
 * 批量保存或更新配置
 */
export const batchSaveConfigs = async (configs: SystemConfig[]): Promise<void> => {
  await axios.post(`${API_BASE_URL}/api/system/config/batch-save`, configs);
};

/**
 * 更新配置值
 */
export const updateConfigValue = async (configKey: string, configValue: string): Promise<void> => {
  await axios.put(`${API_BASE_URL}/api/system/config/update/${configKey}`, { configValue });
};

/**
 * 删除配置
 */
export const deleteConfig = async (id: number): Promise<void> => {
  await axios.delete(`${API_BASE_URL}/api/system/config/delete/${id}`);
};

/**
 * 初始化默认配置
 */
export const initDefaultConfigs = async (): Promise<void> => {
  await axios.post(`${API_BASE_URL}/api/system/config/init-defaults`);
};

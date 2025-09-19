import request, { aiRequest } from '@/request';

/** 初始化默认关键词 初始化系统默认的关键词列表 POST /api/cert-keywords/initialize */
export async function initializeKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/initialize", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取所有关键词 获取所有启用的关键词列表 GET api/api/cert-keywords/list */
export async function getAllKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/list", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取启用的关键词 获取所有启用的关键词字符串列表 GET /api/cert-keywords/enabled */
export async function getEnabledKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/enabled", {
    method: "GET",
    ...(options || {}),
  });
}

/** 添加关键词 添加新的关键词 POST /api/cert-keywords/add */
export async function addKeyword(
  params: {
    keyword: string;
    description?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/add", {
    method: "POST",
    params,
    ...(options || {}),
  });
}

/** 删除关键词 删除指定的关键词 DELETE /api/cert-keywords/delete */
export async function deleteKeyword(
  params: {
    keyword: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/delete", {
    method: "DELETE",
    params,
    ...(options || {}),
  });
}

/** 更新关键词 更新关键词信息 PUT /api/cert-keywords/update */
export async function updateKeyword(
  params: {
    id: number;
    keyword: string;
    description?: string;
    enabled?: boolean;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/update", {
    method: "PUT",
    params,
    ...(options || {}),
  });
}

/** 检查文本关键词 检查文本是否包含任何关键词 POST /api/cert-keywords/check */
export async function checkKeywords(
  params: {
    text: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("api/api/cert-keywords/check", {
    method: "POST",
    params,
    ...(options || {}),
  });
}

/** 获取关键词匹配数量 获取关键词及其匹配数量 GET api/api/cert-keywords/with-match-counts */
export async function getKeywordsWithMatchCounts(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/with-match-counts", {
    method: "GET",
    ...(options || {}),
  });
}

/** 删除0匹配关键词 删除所有匹配数量为0的关键词 DELETE /api/cert-keywords/delete-zero-match */
export async function deleteZeroMatchKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("api/api/cert-keywords/delete-zero-match", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 自动处理相关状态 根据关键词自动设置数据的相关状态 POST /api/crawler-data/auto-process-related */
export async function autoProcessRelated(
  params?: {
    keywords?: string[];
  },
  options?: { [key: string]: any }
) {
  return aiRequest<Record<string, any>>("/api/crawler-data/auto-process-related", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 根据数据源自动处理相关状态 根据关键词自动设置指定数据源的相关状态 POST /api/crawler-data/auto-process-related-by-source */
export async function autoProcessRelatedBySource(
  params: {
    sourceName: string;
  },
  options?: { [key: string]: any }
) {
  return aiRequest<Record<string, any>>("/api/crawler-data/auto-process-related-by-source", {
    method: "POST",
    params,
    ...(options || {}),
  });
}

/** 保存关键词列表 批量保存关键词列表，会先清空现有关键词 POST /keywords/save-list */
export async function saveKeywordList(
  params: {
    keywords: string[];
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/keywords/save-list", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 获取文件关键词 从CertNewsKeywords.txt文件获取关键词列表 GET /api/crawler-data/keywords/file */
export async function getFileKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/crawler-data/keywords/file", {
    method: "GET",
    ...(options || {}),
  });
}

/** 保存关键词到文件 将关键词列表保存到CertNewsKeywords.txt文件 POST /api/crawler-data/keywords/file */
export async function saveKeywordsToFile(
  keywords: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/crawler-data/keywords/file", {
    method: "POST",
    data: keywords,
    ...(options || {}),
  });
}

/** 迁移本地关键词到文件 将localStorage中的关键词迁移到CertNewsKeywords.txt文件 POST /api/crawler-data/keywords/migrate-from-local */
export async function migrateKeywordsFromLocalStorage(
  localKeywords: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/crawler-data/keywords/migrate-from-local", {
    method: "POST",
    data: localKeywords,
    ...(options || {}),
  });
}

// ==================== 设备匹配关键词管理 API ====================

/** 获取统一关键词配置 */
export async function getUnifiedKeywordConfig(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/api/device-match-keywords/unified-config", {
    method: "GET",
    ...(options || {}),
  });
}

/** 保存统一关键词配置 */
export async function saveUnifiedKeywordConfig(
  params: {
    normalKeywords: string[];
    blacklistKeywords: string[];
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/api/device-match-keywords/unified-config", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

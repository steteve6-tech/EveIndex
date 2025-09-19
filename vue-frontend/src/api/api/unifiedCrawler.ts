// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 执行统一爬虫 POST /unified-crawler/execute */
export async function executeUnifiedCrawler(
  params?: {
    crawlerType?: string;
    options?: any;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/execute", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 获取爬虫执行历史 GET /unified-crawler/history */
export async function getExecutionHistory(
  params?: {
    page?: number;
    size?: number;
    crawlerType?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/history", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 清除执行历史 DELETE /unified-crawler/history */
export async function clearExecutionHistory(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/history", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 执行统一爬取 POST /unified-crawler/execute */
export async function executeUnifiedCrawl(
  params?: {
    crawlerType?: string;
    options?: any;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/execute", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 使用配置执行统一爬取 POST /unified-crawler/execute-with-config */
export async function executeUnifiedCrawlWithConfig(
  params?: {
    config?: any;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/execute-with-config", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 执行快速测试 POST /unified-crawler/quick-test */
export async function executeQuickTest(
  params?: {
    crawlerType?: string;
    limit?: number;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/quick-test", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 测试特定爬虫 POST /unified-crawler/test */
export async function testSpecificCrawler(
  crawlerType: string,
  keyword: string,
  limit: number = 5,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/test", {
    method: "POST",
    data: {
      crawlerType,
      keyword,
      limit
    },
    ...(options || {}),
  });
}

/** 获取可用爬虫列表 GET /unified-crawler/available */
export async function getAvailableCrawlers(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/available", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取默认配置 GET /unified-crawler/config/default */
export async function getDefaultConfig(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/config/default", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取快速测试配置 GET /unified-crawler/config/quick-test */
export async function getQuickTestConfig(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/config/quick-test", {
    method: "GET",
    ...(options || {}),
  });
}

/** 验证关键词文件 POST /unified-crawler/validate-keywords */
export async function validateKeywordsFile(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/validate-keywords", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取系统状态 GET /unified-crawler/status */
export async function getSystemStatus(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unified-crawler/status", {
    method: "GET",
    ...(options || {}),
  });
}

/** 统一爬取配置类型 */
export interface UnifiedCrawlConfig {
  crawlerType?: string;
  keywords?: string[];
  limit?: number;
  options?: any;
}
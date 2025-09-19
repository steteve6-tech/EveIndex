// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 执行所有爬虫 POST /crawler/execute-all */
export async function executeAllCrawlers(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/execute-all", {
    method: "POST",
    ...(options || {}),
  });
}

/** 执行SGS爬虫 POST /crawler/execute-sgs */
export async function executeSgsCrawler(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/execute-sgs", {
    method: "POST",
    ...(options || {}),
  });
}

/** 执行UL爬虫 POST /crawler/execute-ul */
export async function executeULCrawler(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/execute-ul", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取所有爬虫状态 GET /crawler/states */
export async function getAllCrawlerStates(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/states", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取爬虫统计信息 GET /crawler/statistics */
export async function getCrawlerStatistics(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取系统健康状态 GET /crawler/health */
export async function getSystemHealth(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** 重置爬虫状态 POST /crawler/reset */
export async function resetCrawlerState(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/reset", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取支持的爬虫列表 GET /crawler/supported */
export async function getSupportedCrawlers(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/supported", {
    method: "GET",
    ...(options || {}),
  });
}

/** 设置爬虫启用状态 POST /crawler/enable */
export async function setCrawlerEnabled(
  crawlerType: string,
  enabled: boolean,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(`/crawler/enable/${crawlerType}`, {
    method: "POST",
    params: { enabled },
    ...(options || {}),
  });
}

/** 手动执行爬取 POST /crawler/manual-execute */
export async function executeManualCrawl(
  params?: {
    crawlerType?: string;
    options?: any;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler/manual-execute", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}
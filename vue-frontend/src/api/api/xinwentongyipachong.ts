// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取API配置 获取统一爬虫API的配置信息 GET /api/news-unicrawl/config */
export async function getConfig(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/news-unicrawl/config", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取可用爬虫列表 获取所有可用的爬虫及其状态信息 GET /api/news-unicrawl/crawlers */
export async function getAvailableCrawlers(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/news-unicrawl/crawlers", {
    method: "GET",
    ...(options || {}),
  });
}

/** 执行所有爬虫 并发执行所有可用的爬虫 POST /api/news-unicrawl/execute-all */
export async function executeAllCrawlers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeAllCrawlersParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/news-unicrawl/execute-all", {
    method: "POST",
    params: {
      // countPerCrawler has a default value: 50
      countPerCrawler: "50",
      ...params,
    },
    ...(options || {}),
  });
}

/** 执行指定爬虫 执行指定名称的爬虫 POST /api/news-unicrawl/execute/${param0} */
export async function executeSpecificCrawler(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeSpecificCrawlerParams,
  options?: { [key: string]: any }
) {
  const { crawlerName: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/api/news-unicrawl/execute/${param0}`, {
    method: "POST",
    params: {
      // count has a default value: 50
      count: "50",
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 执行SGS爬虫（过滤条件） 使用过滤条件执行SGS爬虫 POST /api/news-unicrawl/execute/sgs/filters */
export async function executeSgsCrawlerWithFilters(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeSgsCrawlerWithFiltersParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/news-unicrawl/execute/sgs/filters",
    {
      method: "POST",
      params: {
        // count has a default value: 50
        count: "50",

        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 获取SGS过滤选项 获取SGS爬虫可用的过滤选项 GET /api/news-unicrawl/sgs/filter-options */
export async function getSgsFilterOptions(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/news-unicrawl/sgs/filter-options", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取爬虫状态 获取所有爬虫的状态和数据库统计信息 GET /api/news-unicrawl/status */
export async function getCrawlerStatus(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/news-unicrawl/status", {
    method: "GET",
    ...(options || {}),
  });
}

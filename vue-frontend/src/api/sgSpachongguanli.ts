// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 批量执行SGS爬虫 执行多个关键词的SGS爬虫任务 POST /sgs-crawler/batch-execute */
export async function batchExecuteSgsCrawler(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.batchExecuteSgsCrawlerParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/sgs-crawler/batch-execute", {
    method: "POST",
    params: {
      // countPerKeyword has a default value: 5
      countPerKeyword: "5",
      ...params,
    },
    ...(options || {}),
  });
}

/** 诊断SGS网站网络连接 诊断SGS网站的网络连接问题，包括DNS解析、TCP连接和HTTP请求 GET /sgs-crawler/diagnose-network */
export async function diagnoseSgsNetwork(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/sgs-crawler/diagnose-network", {
    method: "GET",
    ...(options || {}),
  });
}

/** 诊断指定URL的网络连接 诊断指定URL的网络连接问题 GET /sgs-crawler/diagnose-url */
export async function diagnoseUrl(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.diagnoseUrlParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/sgs-crawler/diagnose-url", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 执行SGS爬虫并保存到数据库 调用SGS爬虫抓取最新数据并保存到数据库 POST /sgs-crawler/execute */
export async function executeSgsCrawler(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeSgsCrawlerParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/sgs-crawler/execute", {
    method: "POST",
    params: {
      // count has a default value: 10
      count: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 执行SGS爬虫（关键词搜索）并保存到数据库 根据关键词调用SGS爬虫抓取数据并保存到数据库 POST /sgs-crawler/execute-with-keyword */
export async function executeSgsCrawlerWithKeyword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeSgsCrawlerWithKeywordParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/sgs-crawler/execute-with-keyword", {
    method: "POST",
    params: {
      // count has a default value: 10
      count: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取SGS爬虫状态 获取SGS爬虫的可用性、配置信息和数据库统计 GET /sgs-crawler/status */
export async function getSgsCrawlerStatus(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/sgs-crawler/status", {
    method: "GET",
    ...(options || {}),
  });
}

/** 测试SGS爬虫连接 测试SGS爬虫的连接性和API可用性 GET /sgs-crawler/test-connection */
export async function testSgsCrawlerConnection(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/sgs-crawler/test-connection", {
    method: "GET",
    ...(options || {}),
  });
}

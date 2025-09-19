// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** BTI基础搜索 使用默认参数搜索BTI数据 POST /api/eu-crawler/bti/basic */
export async function basicBtiSearch(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.basicBTISearchParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/eu-crawler/bti/basic", {
    method: "POST",
    params: {
      // maxPages has a default value: 3
      maxPages: "3",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取BTI爬虫配置 获取BTI爬虫的参数配置模板 GET /api/eu-crawler/bti/config */
export async function getBtiConfig(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/eu-crawler/bti/config", {
    method: "GET",
    ...(options || {}),
  });
}

/** BTI参数化搜索 按多种参数搜索欧盟绑定关税信息数据 POST /api/eu-crawler/bti/search */
export async function searchBti(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchBTIParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/eu-crawler/bti/search", {
    method: "POST",
    params: {
      // keywordMatchRule has a default value: OR
      keywordMatchRule: "OR",

      // maxPages has a default value: 5
      maxPages: "5",
      ...params,
    },
    ...(options || {}),
  });
}

/** 测试BTI爬虫连接 测试BTI爬虫是否能正常连接 GET /api/eu-crawler/bti/test */
export async function testBtiConnection(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/eu-crawler/bti/test", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取EU注册信息爬虫配置 获取EU注册信息爬虫的参数配置模板 GET /api/eu-crawler/registration/config */
export async function getRegistrationConfig(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/eu-crawler/registration/config", {
    method: "GET",
    ...(options || {}),
  });
}

/** EU注册信息关键词爬取 基于关键词列表爬取EUDAMED设备注册信息 POST /api/eu-crawler/registration/keywords */
export async function crawlRegistrationWithKeywords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.crawlRegistrationWithKeywordsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/eu-crawler/registration/keywords", {
    method: "POST",
    params: {
      // maxRecords has a default value: 100
      maxRecords: "100",
      // batchSize has a default value: 50
      batchSize: "50",

      ...params,
    },
    ...(options || {}),
  });
}

/** EU注册信息参数化搜索 按Trade name、制造商名称、风险等级等参数搜索EUDAMED设备注册信息 POST /api/eu-crawler/registration/search */
export async function searchRegistration(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchRegistrationParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/eu-crawler/registration/search", {
    method: "POST",
    params: {
      // maxRecords has a default value: 100
      maxRecords: "100",
      // batchSize has a default value: 50
      batchSize: "50",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取EU爬虫状态 获取所有EU爬虫的运行状态 GET /api/eu-crawler/status */
export async function getEuStatus(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/eu-crawler/status", {
    method: "GET",
    ...(options || {}),
  });
}

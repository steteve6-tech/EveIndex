// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取爬虫参数配置模板 获取指定爬虫的参数配置模板 GET /api/us-crawler/config/${param0} */
export async function getCrawlerConfig(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCrawlerConfigParams,
  options?: { [key: string]: any }
) {
  const { crawlerType: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/api/us-crawler/config/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 健康检查 检查美国爬虫服务是否正常运行 GET /api/us-crawler/health */
export async function healthCheck2(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** CustomsCaseCrawler参数化搜索 按HS编码、最大记录数、批次大小、开始日期等参数搜索海关案例数据 POST /api/us-crawler/search/customs-case */
export async function searchCustomsCase(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchCustomsCaseParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/customs-case", {
    method: "POST",
    params: {
      // maxRecords has a default value: 10
      maxRecords: "10",
      // batchSize has a default value: 10
      batchSize: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** D_510K参数化搜索 按设备名称、申请人、决策日期等参数搜索FDA 510K数据 POST /api/us-crawler/search/d510k */
export async function searchD510K(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchD510KParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/d510k", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** D_event参数化搜索 按品牌名称、制造商、型号、报告日期等参数搜索FDA MAUDE数据 POST /api/us-crawler/search/devent */
export async function searchDEvent(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchDEventParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/devent", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** D_recall参数化搜索 按产品名称、召回原因、召回公司、召回日期等参数搜索FDA召回数据，支持关键词列表 POST /api/us-crawler/search/drecall */
export async function searchDRecall(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchDRecallParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/drecall", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** D_registration专门搜索 按设备名称、专有名称、制造商名称等参数搜索FDA注册数据 POST /api/us-crawler/search/dregistration */
export async function searchDRegistration(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchDRegistrationParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/dregistration", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** GuidanceCrawler参数化搜索 按最大记录数参数搜索FDA指导文档数据 POST /api/us-crawler/search/guidance */
export async function searchGuidance(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchGuidanceParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/guidance", {
    method: "POST",
    params: {
      // maxRecords has a default value: 10
      maxRecords: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** unicrawl参数化搜索 按关键词列表、日期范围等参数搜索所有爬虫数据 POST /api/us-crawler/search/unicrawl */
export async function searchUnicrawl(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUnicrawlParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/unicrawl", {
    method: "POST",
    params: {
      // totalCount has a default value: 50
      totalCount: "50",

      ...params,
    },
    ...(options || {}),
  });
}

/** US_510K参数化搜索 按设备名称、申请人、trade_name、日期范围等参数搜索FDA 510K数据 POST /api/us-crawler/search/us510k */
export async function searchUs510K(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUS510KParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/us510k", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** US_event参数化搜索 按设备名称、制造商、产品问题、报告日期等参数搜索FDA设备不良事件数据 POST /api/us-crawler/search/usevent */
export async function searchUsEvent(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUSEventParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/usevent", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** US_recall_api参数化搜索 按召回公司、brand name、产品描述、召回日期等参数搜索FDA召回数据，支持关键词列表 POST /api/us-crawler/search/usrecall */
export async function searchUsRecall(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUSRecallParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/us-crawler/search/usrecall", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 测试CustomsCaseCrawler爬虫 测试海关案例数据爬虫 POST /api/us-crawler/test/customs-case */
export async function testCustomsCase(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/customs-case", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试D_510K爬虫 测试FDA 510K设备审批数据爬虫 POST /api/us-crawler/test/d510k */
export async function testD510K(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/d510k", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试D_event爬虫 测试FDA设备不良事件数据爬虫 POST /api/us-crawler/test/devent */
export async function testDEvent(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/devent", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试D_recall爬虫 测试FDA设备召回数据爬虫 POST /api/us-crawler/test/drecall */
export async function testDRecall(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/drecall", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试GuidanceCrawler爬虫 测试FDA指导文档爬虫 POST /api/us-crawler/test/guidance */
export async function testGuidance(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/guidance", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试unicrawl爬虫 测试统一爬虫 POST /api/us-crawler/test/unicrawl */
export async function testUnicrawl(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/unicrawl", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试US_510K爬虫 测试新的FDA 510K设备数据爬虫 POST /api/us-crawler/test/us510k */
export async function testUs510K(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/us510k", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试US_event爬虫 测试新的FDA设备不良事件数据爬虫 POST /api/us-crawler/test/usevent */
export async function testUsEvent(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/usevent", {
    method: "POST",
    ...(options || {}),
  });
}

/** 测试US_recall_api爬虫 测试新的FDA设备召回数据爬虫 POST /api/us-crawler/test/usrecall */
export async function testUsRecall(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/us-crawler/test/usrecall", {
    method: "POST",
    ...(options || {}),
  });
}

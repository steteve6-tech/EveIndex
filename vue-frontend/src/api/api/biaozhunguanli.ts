// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取标准列表 分页获取标准列表，支持关键词搜索和筛选 GET /standards */
export async function getStandards(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStandardsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards", {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取标准详情 根据ID获取标准详细信息 GET /standards/${param0} */
export async function getStandard(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStandardParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/standards/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新标准 更新指定ID的标准信息 PUT /standards/${param0} */
export async function updateStandard(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateStandardParams,
  body: API.StandardUpdateRequest,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<API.Standard>(`/standards/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 删除标准 逻辑删除指定ID的标准 DELETE /standards/${param0} */
export async function deleteStandard(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteStandardParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/standards/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新监控状态 更新标准的监控状态 PUT /standards/${param0}/monitoring */
export async function updateMonitoringStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateMonitoringStatusParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/standards/${param0}/monitoring`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 根据多个国家获取标准 根据多个国家获取标准列表 POST /standards/countries */
export async function getStandardsByCountries(
  body: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards/countries", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 根据国家获取标准 根据国家获取标准列表 GET /standards/country/${param0} */
export async function getStandardsByCountry(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStandardsByCountryParams,
  options?: { [key: string]: any }
) {
  const { country: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/standards/country/${param0}`, {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // limit has a default value: 20
      limit: "20",
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 查询爬虫数据 根据关键词配置查询相关的爬虫数据 GET /standards/crawler-data */
export async function queryCrawlerData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryCrawlerDataParams,
  options?: { [key: string]: any }
) {
  return request<API.CrawlerDataSearchResult>("/standards/crawler-data", {
    method: "GET",
    params: {
      ...params,
      request: undefined,
      ...params["request"],
    },
    ...(options || {}),
  });
}

/** 查询高优先级爬虫数据 根据高优先级监测项查询爬虫数据 GET /standards/crawler-data/high-priority */
export async function queryHighPriorityCrawlerData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryHighPriorityCrawlerDataParams,
  options?: { [key: string]: any }
) {
  return request<API.CrawlerDataSearchResult>(
    "/standards/crawler-data/high-priority",
    {
      method: "GET",
      params: {
        ...params,
        request: undefined,
        ...params["request"],
      },
      ...(options || {}),
    }
  );
}

/** 根据市场查询爬虫数据 根据市场关键词查询相关的爬虫数据 GET /standards/crawler-data/market/${param0} */
export async function queryCrawlerDataByMarket(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryCrawlerDataByMarketParams,
  options?: { [key: string]: any }
) {
  const { marketCode: param0, ...queryParams } = params;
  return request<API.CrawlerDataSearchResult>(
    `/standards/crawler-data/market/${param0}`,
    {
      method: "GET",
      params: {
        ...queryParams,
        request: undefined,
        ...queryParams["request"],
      },
      ...(options || {}),
    }
  );
}

/** 创建新标准 创建新的认证标准 POST /standards/create */
export async function createStandard(
  body: API.StandardCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.Standard>("/standards/create", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取即将到期的标准 获取即将到期的标准 GET /standards/expiring */
export async function getExpiringStandards(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/expiring", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取高风险标准 获取所有高风险等级的标准 GET /standards/high-risk */
export async function getHighRiskStandards(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/high-risk", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取关键词配置 获取当前的关键词配置信息 GET /standards/keywords */
export async function getKeywordConfig(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/keywords", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取按发布时间排序的最新标准 获取按发布时间排序的最新标准列表 GET /standards/latest-by-published */
export async function getLatestStandardsByPublishedDate(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLatestStandardsByPublishedDateParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards/latest-by-published", {
    method: "GET",
    params: {
      // limit has a default value: 3
      limit: "3",
      ...params,
    },
    ...(options || {}),
  });
}

/** 分页查询标准（管理版本） 根据条件分页查询标准列表 GET /standards/management */
export async function getStandardsManagement(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStandardsManagementParams,
  options?: { [key: string]: any }
) {
  return request<API.StandardSearchResult>("/standards/management", {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // size has a default value: 20
      size: "20",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取监控中的标准 获取所有监控中的标准 GET /standards/monitored */
export async function getMonitoredStandards(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/monitored", {
    method: "GET",
    ...(options || {}),
  });
}

/** 根据编号获取标准 根据标准编号获取标准信息 GET /standards/number/${param0} */
export async function getStandardByNumber(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStandardByNumberParams,
  options?: { [key: string]: any }
) {
  const { standardNumber: param0, ...queryParams } = params;
  return request<API.Standard>(`/standards/number/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 获取最近更新的标准 获取最近更新的标准列表 GET /standards/recent-updates */
export async function getRecentlyUpdatedStandards(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getRecentlyUpdatedStandardsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards/recent-updates", {
    method: "GET",
    params: {
      // limit has a default value: 10
      limit: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取风险等级统计 获取各风险等级的标准数量统计 GET /standards/risk-level-stats */
export async function getRiskLevelStats(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/risk-level-stats", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取风险统计 获取风险等级统计信息 GET /standards/risk-statistics */
export async function getRiskStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/risk-statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取标准统计信息 获取标准的统计信息 GET /standards/statistics */
export async function getStandardStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取国家统计信息 获取各国家的标准统计信息 GET /standards/statistics/countries */
export async function getCountryStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/statistics/countries", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取即将生效的标准 获取即将生效的标准列表 GET /standards/upcoming */
export async function getUpcomingStandards(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUpcomingStandardsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards/upcoming", {
    method: "GET",
    params: {
      // days has a default value: 365
      days: "365",
      // page has a default value: 1
      page: "1",
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取按生效时间排序的即将生效标准 获取未来生效的即将生效标准列表 GET /standards/upcoming-by-effective */
export async function getUpcomingStandardsByEffectiveDate(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUpcomingStandardsByEffectiveDateParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/standards/upcoming-by-effective", {
    method: "GET",
    params: {
      // limit has a default value: 3
      limit: "3",
      ...params,
    },
    ...(options || {}),
  });
}

/** 触发数据更新 手动触发标准数据更新 POST /standards/update */
export async function triggerUpdate(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/standards/update", {
    method: "POST",
    ...(options || {}),
  });
}

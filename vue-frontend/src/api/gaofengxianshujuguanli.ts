// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取指定类型的高风险数据 根据数据类型获取风险等级为HIGH的数据 GET /high-risk-data/${param0} */
export async function getHighRiskDataByType(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getHighRiskDataByTypeParams,
  options?: { [key: string]: any }
) {
  const { dataType: param0, ...queryParams } = params;
  return request<API.PageMapStringObject>(`/high-risk-data/${param0}`, {
    method: "GET",
    params: {
      // size has a default value: 20
      size: "20",
      // sortBy has a default value: id
      sortBy: "id",
      // sortDir has a default value: desc
      sortDir: "desc",
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 获取高风险数据详情 获取指定高风险数据的详细信息 GET /high-risk-data/${param0}/${param1} */
export async function getHighRiskDataDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getHighRiskDataDetailParams,
  options?: { [key: string]: any }
) {
  const { dataType: param0, id: param1, ...queryParams } = params;
  return request<Record<string, any>>(`/high-risk-data/${param0}/${param1}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新数据风险等级 更新指定数据的风险等级 PUT /high-risk-data/${param0}/${param1}/risk-level */
export async function updateRiskLevel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateRiskLevelParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  const { dataType: param0, id: param1, ...queryParams } = params;
  return request<Record<string, any>>(
    `/high-risk-data/${param0}/${param1}/risk-level`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      params: { ...queryParams },
      data: body,
      ...(options || {}),
    }
  );
}

/** 批量更新风险等级 批量更新多个数据的风险等级 PUT /high-risk-data/batch/risk-level */
export async function batchUpdateRiskLevel(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/high-risk-data/batch/risk-level", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 搜索高风险数据 根据条件搜索高风险数据 POST /high-risk-data/search */
export async function searchHighRiskData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchHighRiskDataParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<API.PageMapStringObject>("/high-risk-data/search", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    params: {
      // size has a default value: 20
      size: "20",
      ...params,
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取高风险数据统计 获取各类型高风险数据的数量统计 GET /high-risk-data/statistics */
export async function getHighRiskStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/high-risk-data/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取按国家分类的高风险数据统计 获取各国家各类型高风险数据的数量统计 GET /high-risk-data/statistics/by-country */
export async function getHighRiskStatisticsByCountry(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/high-risk-data/statistics/by-country", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取按国家分类的带趋势的高风险数据统计 获取各国家各类型高风险数据的数量统计，包含各国今天相对于昨天的变化趋势 GET /high-risk-data/statistics/by-country/with-trend */
export async function getHighRiskStatisticsByCountryWithTrend(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/high-risk-data/statistics/by-country/with-trend",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 获取带趋势的高风险数据统计 获取各类型高风险数据的数量统计，包含今天相对于昨天的变化趋势 GET /high-risk-data/statistics/with-trend */
export async function getHighRiskStatisticsWithTrend(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/high-risk-data/statistics/with-trend", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取高风险数据变化趋势 获取今天的高风险数据相对于昨天的变化趋势 GET /high-risk-data/trend */
export async function getHighRiskTrend(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/high-risk-data/trend", {
    method: "GET",
    ...(options || {}),
  });
}

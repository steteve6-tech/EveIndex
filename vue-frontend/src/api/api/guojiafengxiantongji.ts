// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 初始化基准数据 初始化昨天的基准数据，为趋势图表提供起始点 POST /country-risk/init-baseline */
export async function initializeBaselineData(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/country-risk/init-baseline", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取国家风险排行榜 获取最近7天的国家风险排行榜 GET /country-risk/ranking */
export async function getCountryRiskRanking(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/country-risk/ranking", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取指定日期的国家风险统计 获取指定日期的国家风险统计数据 GET /country-risk/stats/${param0} */
export async function getCountryRiskStatsByDate(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryRiskStatsByDateParams,
  options?: { [key: string]: any }
) {
  const { date: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/country-risk/stats/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 获取国家风险趋势 获取每个国家每天的高风险数据数量变化趋势 GET /country-risk/trends */
export async function getCountryRiskTrends(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/country-risk/trends", {
    method: "GET",
    ...(options || {}),
  });
}

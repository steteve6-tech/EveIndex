// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取国家风险趋势 GET /country-risk-statistics/trends */
export async function getCountryRiskTrends(
  params?: {
    country?: string;
    startDate?: string;
    endDate?: string;
    period?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/country-risk-statistics/trends", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取国家风险排名 GET /country-risk-statistics/ranking */
export async function getCountryRiskRanking(
  params?: {
    period?: string;
    limit?: number;
    sortBy?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/country-risk-statistics/ranking", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 初始化基线数据 POST /country-risk-statistics/initialize-baseline */
export async function initializeBaselineData(
  params?: {
    startDate?: string;
    endDate?: string;
    countries?: string[];
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/country-risk-statistics/initialize-baseline", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 获取国家风险分布 GET /country-risk-statistics/distribution */
export async function getCountryRiskDistribution(
  params?: {
    period?: string;
    riskLevel?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/country-risk-statistics/distribution", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取风险统计概览 GET /country-risk-statistics/overview */
export async function getRiskStatisticsOverview(
  params?: {
    period?: string;
    countries?: string[];
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/country-risk-statistics/overview", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

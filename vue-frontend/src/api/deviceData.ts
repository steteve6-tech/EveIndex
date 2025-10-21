// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取设备召回记录 GET /device-data/query/recall-records */
export async function getDeviceRecallRecords(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/recall-records", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取设备510K记录 GET /device-data/query/510k */
export async function getDevice510KRecords(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/510k", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取设备事件报告 GET /device-data/query/event-reports */
export async function getDeviceEventReports(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/event-reports", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取设备注册记录 GET /device-data/query/registration-records */
export async function getDeviceRegistrationRecords(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/registration-records", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取指导文档 GET /device-data/query/guidance-documents */
export async function getGuidanceDocuments(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/guidance-documents", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取海关案例 GET /device-data/query/customs-cases */
export async function getCustomsCases(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    countryCode?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/query/customs-cases", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 根据关键词搜索设备数据 POST /device-data/query/search-by-keywords */
export async function searchDeviceDataByKeywords(
  keywords: string[],
  page?: number,
  size?: number,
  entityTypes?: string[],
  riskLevel?: string,
  country?: string,
  blacklistKeywords?: string[],
  searchMode?: string,
  options?: { [key: string]: any }
) {
  const requestBody = {
    keywords,
    blacklistKeywords: blacklistKeywords || []
  };

  const params: any = {};
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;
  if (entityTypes && entityTypes.length > 0) params.entityTypes = entityTypes.join(',');
  if (riskLevel) params.riskLevel = riskLevel;
  if (country) params.country = country;
  if (searchMode) params.searchMode = searchMode;

  return request<Record<string, any>>("/device-data/query/search-by-keywords", {
    method: "POST",
    data: requestBody,
    params,
    ...(options || {}),
  });
}

/** 获取设备数据概览统计 GET /device-data/statistics/overview */
export async function getDeviceDataOverview(
  params?: {
    country?: string;
    period?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/statistics/overview", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取各国设备数据统计 GET /device-data/statistics/by-country */
export async function getDeviceDataByCountry(
  params?: {
    period?: string;
    dataType?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/statistics/by-country", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取按风险等级统计的设备数据 GET /device-data/statistics/by-risk-level */
export async function getDeviceDataByRiskLevel(
  params?: {
    country?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/statistics/by-risk-level", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取高风险数据统计 GET /device-data/statistics/high-risk */
export async function getHighRiskStatistics(
  params?: {},
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/statistics/high-risk", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 获取竞争对手信息列表 GET /competitor-info */
export async function getCompetitorInfoList(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    country?: string;
    industry?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取竞争对手详情 GET /competitor-info/${param0} */
export async function getCompetitorInfo(
  params: {
    id: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/competitor-info/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 创建竞争对手信息 POST /competitor-info */
export async function createCompetitorInfo(
  params: {
    name: string;
    country: string;
    industry: string;
    description?: string;
    website?: string;
    contactInfo?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 更新竞争对手信息 PUT /competitor-info/${param0} */
export async function updateCompetitorInfo(
  params: {
    id: number;
    name?: string;
    country?: string;
    industry?: string;
    description?: string;
    website?: string;
    contactInfo?: string;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, ...bodyParams } = params;
  return request<Record<string, any>>(`/competitor-info/${param0}`, {
    method: "PUT",
    data: bodyParams,
    ...(options || {}),
  });
}

/** 删除竞争对手信息 DELETE /competitor-info/${param0} */
export async function deleteCompetitorInfo(
  params: {
    id: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/competitor-info/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 获取竞争对手统计信息 GET /competitor-info/statistics */
export async function getCompetitorStatistics(
  params?: {
    period?: string;
    industry?: string;
    country?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info/statistics", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 获取竞争对手列表 GET /competitor-info/list */
export async function getCompetitorList(
  params?: {
    page?: number;
    size?: number;
    keyword?: string;
    country?: string;
    industry?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info/list", {
    method: "GET",
    params,
    ...(options || {}),
  });
}

/** 推送数据到竞争对手信息 POST /competitor-info/push-data */
export async function pushDataToCompetitorInfo(
  params?: {
    data?: any;
    source?: string;
  },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info/push-data", {
    method: "POST",
    data: params,
    ...(options || {}),
  });
}

/** 清空竞争对手数据 DELETE /competitor-info/clear */
export async function clearCompetitorData(
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/competitor-info/clear", {
    method: "DELETE",
    ...(options || {}),
  });
}
// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 清空所有竞品数据 清空数据库中的所有竞品信息数据 DELETE /api/competitor-info/clear-all */
export async function clearAllCompetitorData(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/competitor-info/clear-all", {
    method: "DELETE",
    ...(options || {}),
  });
}

/** 创建竞品信息 创建新的竞品信息记录 POST /api/competitor-info/create */
export async function createCompetitorInfo(
  body: API.CompetitorInfo,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/competitor-info/create", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除竞品信息 删除指定ID的竞品信息 DELETE /api/competitor-info/delete/${param0} */
export async function deleteCompetitorInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteCompetitorInfoParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/api/competitor-info/delete/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 获取竞品信息详情 根据ID获取竞品信息的详细信息 GET /api/competitor-info/detail/${param0} */
export async function getCompetitorInfoDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompetitorInfoDetailParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/api/competitor-info/detail/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 健康检查 检查竞品信息服务是否正常运行 GET /api/competitor-info/health */
export async function healthCheck4(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/competitor-info/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取竞品信息列表 分页查询竞品信息列表，支持关键词搜索和状态筛选 GET /api/competitor-info/list */
export async function getCompetitorList(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCompetitorListParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/competitor-info/list", {
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

/** 批量推送竞品数据 批量推送竞品数据到数据库 POST /api/competitor-info/push-data */
export async function pushDataToCompetitorInfo(
  body: API.CompetitorInfo[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/competitor-info/push-data", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取竞品信息统计数据 获取竞品信息的总数、活跃数量、本月新增、风险提醒等统计数据 GET /api/competitor-info/statistics */
export async function getStatistics2(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/competitor-info/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 更新竞品信息 更新指定ID的竞品信息 PUT /api/competitor-info/update/${param0} */
export async function updateCompetitorInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateCompetitorInfoParams,
  body: API.CompetitorInfo,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/api/competitor-info/update/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

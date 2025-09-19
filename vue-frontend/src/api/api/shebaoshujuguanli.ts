// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 批量更新多个实体的风险等级和关键词 批量更新指定类型的多个实体的风险等级和关键词 PUT /device-data/batch-update/${param0} */
export async function batchUpdateRiskLevelAndKeywords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.batchUpdateRiskLevelAndKeywordsParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  const { entityType: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/device-data/batch-update/${param0}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  });
}

/** 获取海关案例 分页获取海关案例 GET /device-data/customs-cases */
export async function getCustomsCases(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCustomsCasesParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/customs-cases", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取510K设备记录 分页获取510K设备记录 GET /device-data/device-510k */
export async function getDevice510KRecords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDevice510KRecordsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/device-510k", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取事件报告 分页获取事件报告 GET /device-data/event-reports */
export async function getDeviceEventReports(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDeviceEventReportsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/event-reports", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取指导文档 分页获取指导文档 GET /device-data/guidance-documents */
export async function getGuidanceDocuments(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getGuidanceDocumentsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/guidance-documents", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取设备数据总览统计 获取各种设备数据的统计信息 GET /device-data/overview-statistics */
export async function getDeviceDataOverview(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/device-data/overview-statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取召回记录 分页获取召回记录 GET /device-data/recall-records */
export async function getDeviceRecallRecords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDeviceRecallRecordsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/recall-records", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    ...(options || {}),
  });
}

/** 获取注册记录 分页获取注册记录 GET /device-data/registration-records */
export async function getDeviceRegistrationRecords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getDeviceRegistrationRecordsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/registration-records", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 重置所有数据为中等风险 将所有设备数据的风险等级重置为MEDIUM POST /device-data/reset-all-to-medium-risk */
export async function resetAllDataToMediumRisk(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/device-data/reset-all-to-medium-risk", {
    method: "POST",
    ...(options || {}),
  });
}

/** 根据关键词搜索设备数据 支持搜索多个实体类型，返回匹配的设备数据，支持黑名单关键词过滤 POST /device-data/search-by-keywords */
export async function searchDeviceDataByKeywords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchDeviceDataByKeywordsParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/device-data/search-by-keywords", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    params: {
      // size has a default value: 10
      size: "10",

      ...params,
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取各国设备数据统计 按国家统计各种设备数据的数量 GET /device-data/statistics-by-country */
export async function getDeviceDataByCountry(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/device-data/statistics-by-country", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取支持的实体类型列表 返回系统支持的所有实体类型 GET /device-data/supported-entity-types */
export async function getSupportedEntityTypes(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/device-data/supported-entity-types", {
    method: "GET",
    ...(options || {}),
  });
}

/** 更新单个实体的风险等级和关键词 更新指定实体的风险等级和关键词 PUT /device-data/update/${param0}/${param1} */
export async function updateEntityRiskLevelAndKeywords(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateEntityRiskLevelAndKeywordsParams,
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  const { entityType: param0, id: param1, ...queryParams } = params;
  return request<Record<string, any>>(
    `/device-data/update/${param0}/${param1}`,
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

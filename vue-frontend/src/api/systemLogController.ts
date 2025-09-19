// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 此处后端没有提供注释 GET /system-logs */
export async function getLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLogsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/system-logs", {
    method: "GET",
    params: {
      // page has a default value: 1
      page: "1",
      // limit has a default value: 20
      limit: "20",
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /system-logs/cleanup */
export async function cleanupOldLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.cleanupOldLogsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/system-logs/cleanup", {
    method: "POST",
    params: {
      // daysToKeep has a default value: 30
      daysToKeep: "30",
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /system-logs/errors */
export async function getErrorLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getErrorLogsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/system-logs/errors", {
    method: "GET",
    params: {
      // limit has a default value: 20
      limit: "20",
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /system-logs/log */
export async function logMessage(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/system-logs/log", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /system-logs/recent */
export async function getRecentLogs(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getRecentLogsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/system-logs/recent", {
    method: "GET",
    params: {
      // limit has a default value: 10
      limit: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /system-logs/statistics */
export async function getLogStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/system-logs/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

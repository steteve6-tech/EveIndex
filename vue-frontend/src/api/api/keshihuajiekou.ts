// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 生成数据源分布饼图 生成数据源分布的饼图并返回文件路径 POST /visualization/charts/source-pie */
export async function generateSourcePieChart(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/visualization/charts/source-pie", {
    method: "POST",
    ...(options || {}),
  });
}

/** 生成数据状态分布柱状图 生成数据状态分布的柱状图并返回文件路径 POST /visualization/charts/status-bar */
export async function generateStatusBarChart(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/visualization/charts/status-bar", {
    method: "POST",
    ...(options || {}),
  });
}

/** 生成时间趋势图 生成指定天数的数据趋势图 POST /visualization/charts/trend */
export async function generateTrendChart(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.generateTrendChartParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/visualization/charts/trend", {
    method: "POST",
    params: {
      // days has a default value: 30
      days: "30",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取仪表板数据 获取包含统计信息和图表路径的仪表板数据 GET /visualization/dashboard */
export async function getDashboardData(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/visualization/dashboard", {
    method: "GET",
    ...(options || {}),
  });
}

/** 导出所有数据到Excel 将所有爬虫数据导出到Excel文件 POST /visualization/export/all */
export async function exportAllDataToExcel(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/visualization/export/all", {
    method: "POST",
    ...(options || {}),
  });
}

/** 根据数据源导出数据到Excel 根据数据源名称导出数据到Excel文件 POST /visualization/export/source/${param0} */
export async function exportDataBySourceToExcel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.exportDataBySourceToExcelParams,
  options?: { [key: string]: any }
) {
  const { sourceName: param0, ...queryParams } = params;
  return request<Record<string, any>>(
    `/visualization/export/source/${param0}`,
    {
      method: "POST",
      params: { ...queryParams },
      ...(options || {}),
    }
  );
}

/** 根据状态导出数据到Excel 根据数据状态导出数据到Excel文件 POST /visualization/export/status/${param0} */
export async function exportDataByStatusToExcel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.exportDataByStatusToExcelParams,
  options?: { [key: string]: any }
) {
  const { status: param0, ...queryParams } = params;
  return request<Record<string, any>>(
    `/visualization/export/status/${param0}`,
    {
      method: "POST",
      params: { ...queryParams },
      ...(options || {}),
    }
  );
}

/** 获取统计信息 获取数据统计信息，包括总数、各数据源统计、各状态统计等 GET /visualization/statistics */
export async function getStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/visualization/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取趋势数据 获取指定天数的数据趋势信息 GET /visualization/trend */
export async function getTrendData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTrendDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/visualization/trend", {
    method: "GET",
    params: {
      // days has a default value: 30
      days: "30",
      ...params,
    },
    ...(options || {}),
  });
}

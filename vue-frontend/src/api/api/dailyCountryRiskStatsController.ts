// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/all-countries-trend */
export async function getAllCountriesTrend(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAllCountriesTrendParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/all-countries-trend",
    {
      method: "GET",
      params: {
        // days has a default value: 7
        days: "7",
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/by-date */
export async function getStatsByDate(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStatsByDateParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/api/daily-country-risk-stats/by-date", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/calculate */
export async function calculateStats(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.calculateStatsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/calculate",
    {
      method: "POST",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/calculate-yesterday */
export async function calculateYesterdayStats(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/calculate-yesterday",
    {
      method: "POST",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/check-today-changed */
export async function checkTodayDataChanged(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/check-today-changed",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/cleanup-non-predefined-countries */
export async function cleanupNonPredefinedCountries(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/cleanup-non-predefined-countries",
    {
      method: "POST",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/countries */
export async function getAllCountries(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/countries",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-name-mapping */
export async function getCountryNameMapping(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-name-mapping",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-risk-change */
export async function getCountryRiskChangeData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryRiskChangeDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-risk-change",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-risk-distribution */
export async function getCountryRiskLevelDistribution(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryRiskLevelDistributionParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-risk-distribution",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-stats */
export async function getCountryStatsByDateRange(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryStatsByDateRangeParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-stats",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-sum */
export async function getCountrySumByDateRange(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountrySumByDateRangeParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-sum",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-trend */
export async function getCountryTrend(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryTrendParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-trend",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/country-trend-summary */
export async function getCountryRiskTrendSummary(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCountryRiskTrendSummaryParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/country-trend-summary",
    {
      method: "GET",
      params: {
        // days has a default value: 7
        days: "7",
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/current-countries */
export async function getCurrentCountriesInDatabase(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/current-countries",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/filtered-smart-chart-data */
export async function getFilteredSmartChartData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getFilteredSmartChartDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/filtered-smart-chart-data",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/latest */
export async function getLatestStats(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/api/daily-country-risk-stats/latest", {
    method: "GET",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/multi-country-comparison */
export async function getMultiCountryComparisonData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMultiCountryComparisonDataParams,
  body: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/multi-country-comparison",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      params: {
        ...params,
      },
      data: body,
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/predefined-countries */
export async function getPredefinedCountries(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/predefined-countries",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/predefined-country-filtered-smart-chart-data */
export async function getPredefinedCountryFilteredSmartChartData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPredefinedCountryFilteredSmartChartDataParams,
  body: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/predefined-country-filtered-smart-chart-data",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      params: {
        ...params,
      },
      data: body,
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/predefined-country-trend */
export async function getPredefinedCountryTrendData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPredefinedCountryTrendDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/predefined-country-trend",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/recent-7days-trend */
export async function getRecent7DaysTrend(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/recent-7days-trend",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/reset-yesterday-today-data */
export async function resetYesterdayAndTodayData(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/reset-yesterday-today-data",
    {
      method: "POST",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/set-date-same-as-previous */
export async function setDateDataSameAsPreviousDay(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.setDateDataSameAsPreviousDayParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/set-date-same-as-previous",
    {
      method: "POST",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/set-today-same-as-yesterday */
export async function setTodayDataSameAsYesterday(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/set-today-same-as-yesterday",
    {
      method: "POST",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/smart-chart-data */
export async function getSmartChartData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSmartChartDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/smart-chart-data",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/smart-country-trend */
export async function getSmartCountryTrendData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSmartCountryTrendDataParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/smart-country-trend",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/trend-data */
export async function getTrendData1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTrendData1Params,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/trend-data",
    {
      method: "GET",
      params: {
        // days has a default value: 7
        days: "7",
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/daily-country-risk-stats/update-countries-to-predefined */
export async function updateCountriesToPredefinedList(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/update-countries-to-predefined",
    {
      method: "POST",
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/daily-country-risk-stats/yesterday-today-summary */
export async function getYesterdayAndTodayDataSummary(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>(
    "/api/daily-country-risk-stats/yesterday-today-summary",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

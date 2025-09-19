// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 执行关键词匹配 对所有未处理的CrawlerData执行关键词匹配并更新related字段 POST /crawler-data-keyword-matcher/execute */
export async function executeKeywordMatching(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeKeywordMatchingParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data-keyword-matcher/execute", {
    method: "POST",
    params: {
      // batchSize has a default value: 100
      batchSize: "100",
      ...params,
    },
    ...(options || {}),
  });
}

/** 执行关键词匹配（指定数据源） 对指定数据源的未处理CrawlerData执行关键词匹配并更新related字段 POST /crawler-data-keyword-matcher/execute/${param0} */
export async function executeKeywordMatchingBySource(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.executeKeywordMatchingBySourceParams,
  options?: { [key: string]: any }
) {
  const { sourceName: param0, ...queryParams } = params;
  return request<Record<string, any>>(
    `/crawler-data-keyword-matcher/execute/${param0}`,
    {
      method: "POST",
      params: {
        // batchSize has a default value: 100
        batchSize: "100",
        ...queryParams,
      },
      ...(options || {}),
    }
  );
}

/** 健康检查 检查关键词匹配服务是否正常运行 GET /crawler-data-keyword-matcher/health */
export async function healthCheck1(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data-keyword-matcher/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取匹配统计信息 获取CrawlerData关键词匹配的统计信息 GET /crawler-data-keyword-matcher/statistics */
export async function getMatchingStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>(
    "/crawler-data-keyword-matcher/statistics",
    {
      method: "GET",
      ...(options || {}),
    }
  );
}

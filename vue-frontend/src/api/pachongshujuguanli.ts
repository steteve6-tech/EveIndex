// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 删除爬虫数据 删除指定的爬虫数据 DELETE /crawler-data/${param0} */
export async function deleteCrawlerData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteCrawlerDataParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/crawler-data/${param0}`, {
    method: "DELETE",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** 更新爬虫数据相关性 更新指定爬虫数据的相关性状态 PUT /crawler-data/${param0}/related */
export async function updateCrawlerData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateCrawlerDataParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/crawler-data/${param0}/related`, {
    method: "PUT",
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 更新爬虫数据风险等级 更新指定爬虫数据的风险等级状态 PUT /crawler-data/${param0}/risk-level */
export async function updateCrawlerDataRiskLevel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: { id: string; riskLevel: string },
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params;
  return request<Record<string, any>>(`/crawler-data/${param0}/risk-level`, {
    method: "PUT",
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}

/** 批量更新爬虫数据风险等级 批量更新指定爬虫数据的风险等级状态 PUT /crawler-data/batch-update-risk-level */
export async function batchUpdateCrawlerDataRiskLevel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: { ids: string[]; riskLevel: string },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/batch-update-risk-level", {
    method: "PUT",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 批量设置所有数据为中风险 将数据库中所有数据的风险等级设置为中风险 POST /crawler-data/set-all-medium-risk */
export async function setAllDataToMediumRisk(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/set-all-medium-risk", {
    method: "POST",
    ...(options || {}),
  });
}

/** 获取风险等级统计信息 获取爬虫数据的风险等级分布统计 GET /crawler-data/risk-level-statistics */
export async function getRiskLevelStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/risk-level-statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 自动处理相关状态 根据关键词自动设置数据的相关状态 POST /crawler-data/auto-process-related */
export async function autoProcessRelated(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/auto-process-related", {
    method: "POST",
    ...(options || {}),
  });
}

/** 根据数据源自动处理相关状态 根据关键词自动设置指定数据源的相关状态 POST /crawler-data/auto-process-related-by-source */
export async function autoProcessRelatedBySource(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.autoProcessRelatedBySourceParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/crawler-data/auto-process-related-by-source",
    {
      method: "POST",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 根据标题和内容自动更新国家字段 分析数据的标题和内容，自动识别并更新country字段 POST /crawler-data/auto-update-country */
export async function autoUpdateCountry(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/auto-update-country", {
    method: "POST",
    ...(options || {}),
  });
}

/** 批量将所有数据设置为未确定 将所有爬虫数据的相关性状态设置为未确定 POST /crawler-data/batch-set-undetermined */
export async function batchSetAllUndetermined(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/crawler-data/batch-set-undetermined", {
    method: "POST",
    ...(options || {}),
  });
}

/** 批量更新爬虫数据相关性 批量更新指定爬虫数据的相关性状态 PUT /crawler-data/batch-update-related */
export async function batchUpdateCrawlerDataRelated(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.batchUpdateCrawlerDataRelatedParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/batch-update-related", {
    method: "PUT",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 根据产品名称查询数据 根据产品名称查询爬虫数据 GET /crawler-data/by-product */
export async function findByProduct(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.findByProductParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/by-product", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 根据产品名称和关键词查询 根据产品名称和关键词进行复合查询 GET /crawler-data/by-product-and-keyword */
export async function findByProductAndKeyword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.findByProductAndKeywordParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/by-product-and-keyword", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 根据产品名称模糊查询 根据产品名称进行模糊查询 GET /crawler-data/by-product-contains */
export async function findByProductContaining(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.findByProductContainingParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/by-product-contains", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 根据数据源和产品名称查询 根据数据源和产品名称查询数据 GET /crawler-data/by-source-and-product */
export async function findBySourceNameAndProduct(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.findBySourceNameAndProductParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/by-source-and-product", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取国家分布统计 获取当前数据库中所有数据的国家分布情况 GET /crawler-data/country-distribution */
export async function getCountryDistribution(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/country-distribution", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取高风险数据国家分布统计 获取当前数据库中所有高风险数据的国家分布情况 GET /crawler-data/high-risk-country-distribution */
export async function getHighRiskCountryDistribution(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/high-risk-country-distribution", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取文件关键词 从CertNewsKeywords.txt文件获取关键词列表 GET /crawler-data/keywords/file */
export async function getFileKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/keywords/file", {
    method: "GET",
    ...(options || {}),
  });
}

/** 保存关键词到文件 将关键词列表保存到CertNewsKeywords.txt文件 POST /crawler-data/keywords/file */
export async function saveKeywordsToFile(
  body: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/keywords/file", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 迁移本地关键词到文件 将localStorage中的关键词迁移到CertNewsKeywords.txt文件 POST /crawler-data/keywords/migrate-from-local */
export async function migrateKeywordsFromLocalStorage(
  body: string[],
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/crawler-data/keywords/migrate-from-local",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      data: body,
      ...(options || {}),
    }
  );
}

/** 获取爬虫数据列表 支持关键词搜索、国家筛选、相关性筛选、日期范围筛选、分页和排序 GET /crawler-data/list */
export async function getCrawlerData(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCrawlerDataParams,
  options?: { [key: string]: any }
) {
  return request<API.CrawlerDataResponse>("/crawler-data/list", {
    method: "GET",
    params: {
      // size has a default value: 10
      size: "10",

      // sortBy has a default value: publishDate
      sortBy: "publishDate",
      // sortDirection has a default value: desc
      sortDirection: "desc",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取热门产品列表 获取按数据量排序的热门产品列表 GET /crawler-data/popular-products */
export async function getPopularProducts(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPopularProductsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/popular-products", {
    method: "GET",
    params: {
      // limit has a default value: 10
      limit: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取产品详细统计 获取指定产品的详细统计信息 GET /crawler-data/product-detail-statistics */
export async function getProductDetailStatistics(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getProductDetailStatisticsParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/crawler-data/product-detail-statistics",
    {
      method: "GET",
      params: {
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 统计各产品的数据量 统计各产品的数据量分布 GET /crawler-data/product-statistics */
export async function getProductStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/product-statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取相关状态统计 获取爬虫数据相关状态的统计信息 GET /crawler-data/related-statistics */
export async function getRelatedStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/related-statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 保存爬取结果 将爬虫爬取的结果保存到数据库中 POST /crawler-data/save-results */
export async function saveCrawlerResults(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/save-results", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 发送邮件 发送包含新闻内容的邮件 POST /crawler-data/send-email */
export async function sendEmail(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/send-email", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 测试数据源分布 查看数据库中各个数据源的数据分布情况 GET /crawler-data/source-distribution */
export async function getSourceDistribution(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/source-distribution", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取所有数据源名称 获取数据库中所有可用的数据源名称 GET /crawler-data/source-names */
export async function getAllSourceNames(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/source-names", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取统计数据 获取爬虫数据的统计信息 GET /crawler-data/statistics */
export async function getStatistics1(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 测试查询所有数据 测试当所有参数都为空时是否能正确返回所有数据 GET /crawler-data/test-all */
export async function testGetAllData(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/test-all", {
    method: "GET",
    ...(options || {}),
  });
}

/** 更新爬虫数据 更新爬虫数据的各个字段，包括风险等级 PUT /crawler-data/update */
export async function updateCrawlerData1(
  body: API.CrawlerData,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/update", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新今天的数据 手动触发更新今天的数据 POST /crawler-data/update-today-data */
export async function updateTodayData(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/update-today-data", {
    method: "POST",
    ...(options || {}),
  });
}

/** 完整更新爬虫数据 更新爬虫数据的各个字段，包括风险等级 PUT /crawler-data/update */
export async function updateCrawlerDataFull(
  body: API.CrawlerData,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/update", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

// ========== Dashboard专用统计接口 ==========

/** 获取Dashboard统计数据 获取各风险等级的数据统计，专门为Dashboard页面优化 GET /crawler-data/dashboard/statistics */
export async function getDashboardStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/dashboard/statistics", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取国家风险统计数据 获取各国风险等级分布统计，专门为Dashboard国家风险分析优化 GET /crawler-data/dashboard/country-risk-stats */
export async function getCountryRiskStatistics(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/crawler-data/dashboard/country-risk-stats", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取最新高风险数据 获取最新的高风险数据列表，专门为Dashboard最新风险信息优化 GET /crawler-data/dashboard/latest-high-risk */
export async function getLatestHighRiskData(
  params: { limit?: number },
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/crawler-data/dashboard/latest-high-risk", {
    method: "GET",
    params: {
      // limit has a default value: 3
      limit: "3",
      ...params,
    },
    ...(options || {}),
  });
}

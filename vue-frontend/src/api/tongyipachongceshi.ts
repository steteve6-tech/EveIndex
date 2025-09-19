// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 统一爬取所有爬虫 按照关键词文件统一爬取6个爬虫的数据 POST /unicrawler/crawl/all */
export async function crawlAllCrawlers(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.crawlAllCrawlersParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unicrawler/crawl/all", {
    method: "POST",
    params: {
      // maxRecordsPerCrawler has a default value: 50
      maxRecordsPerCrawler: "50",
      // batchSize has a default value: 10
      batchSize: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 按关键词爬取所有爬虫 使用指定关键词爬取所有6个爬虫的数据 POST /unicrawler/crawl/keyword */
export async function crawlByKeyword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.crawlByKeywordParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unicrawler/crawl/keyword", {
    method: "POST",
    params: {
      // maxRecordsPerCrawler has a default value: 20
      maxRecordsPerCrawler: "20",
      // batchSize has a default value: 10
      batchSize: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 健康检查 检查统一爬虫测试服务是否正常运行 GET /unicrawler/health */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/unicrawler/health", {
    method: "GET",
    ...(options || {}),
  });
}

/** 加载搜索关键词 从searchkeywords.txt文件加载搜索关键词 GET /unicrawler/keywords */
export async function loadSearchKeywords(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/unicrawler/keywords", {
    method: "GET",
    ...(options || {}),
  });
}

/** 获取统一爬虫状态 获取所有6个爬虫的状态信息 GET /unicrawler/status */
export async function getUniCrawlerStatus(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/unicrawler/status", {
    method: "GET",
    ...(options || {}),
  });
}

/** 测试爬虫可用性 测试各种爬虫的可用性 GET /unicrawler/test/availability */
export async function testCrawlerAvailability(options?: {
  [key: string]: any;
}) {
  return request<Record<string, any>>("/unicrawler/test/availability", {
    method: "GET",
    ...(options || {}),
  });
}

/** 执行完整爬虫测试 执行完整的爬虫测试流程 POST /unicrawler/test/full */
export async function runFullTest(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.runFullTestParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unicrawler/test/full", {
    method: "POST",
    params: {
      // totalCount has a default value: 50
      totalCount: "50",
      ...params,
    },
    ...(options || {}),
  });
}

/** 测试UL爬虫批量保存功能 测试UL爬虫的批量保存功能，每10条数据批量保存 POST /unicrawler/test/ul-batch-save */
export async function testUlCrawlerBatchSave(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testULCrawlerBatchSaveParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unicrawler/test/ul-batch-save", {
    method: "POST",
    params: {
      // totalCount has a default value: 30
      totalCount: "30",
      // batchSize has a default value: 10
      batchSize: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 从指定位置继续执行UL爬虫 从指定索引位置继续执行UL爬虫 POST /unicrawler/test/ul-continue-from-position */
export async function testUlCrawlerContinueFromPosition(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testULCrawlerContinueFromPositionParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>(
    "/unicrawler/test/ul-continue-from-position",
    {
      method: "POST",
      params: {
        // totalCount has a default value: 10
        totalCount: "10",
        ...params,
      },
      ...(options || {}),
    }
  );
}

/** 测试UL爬虫最新数据爬取 测试UL爬虫的最新数据爬取功能 POST /unicrawler/test/ul-latest */
export async function testUlCrawlerLatest(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testULCrawlerLatestParams,
  options?: { [key: string]: any }
) {
  return request<Record<string, any>>("/unicrawler/test/ul-latest", {
    method: "POST",
    params: {
      // totalCount has a default value: 10
      totalCount: "10",
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取UL爬虫可爬取的总数量 获取当前UL网站可爬取的数据总数量 GET /unicrawler/test/ul-total-count */
export async function getUlTotalCount(options?: { [key: string]: any }) {
  return request<Record<string, any>>("/unicrawler/test/ul-total-count", {
    method: "GET",
    ...(options || {}),
  });
}

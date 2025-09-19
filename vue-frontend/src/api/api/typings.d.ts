declare namespace API {
  type addKeyword1Params = {
    /** 关键词 */
    keyword: string;
    /** 描述 */
    description?: string;
  };

  type autoProcessRelatedBySourceParams = {
    /** 数据源名称 */
    sourceName: string;
  };

  type basicBTISearchParams = {
    /** 最大页数 */
    maxPages?: number;
  };

  type batchExecuteSgsCrawlerParams = {
    /** 关键词列表，用逗号分隔 */
    keywords?: string;
    /** 每个关键词的爬取数量 */
    countPerKeyword?: number;
  };

  type batchUpdateCrawlerDataRelatedParams = {
    /** 数据ID列表 */
    ids: string[];
    /** 相关性状态 */
    related: boolean;
  };

  type batchUpdateRiskLevelAndKeywordsParams = {
    /** 实体类型 */
    entityType: string;
  };

  type calculateStatsParams = {
    statDate: string;
  };

  type checkKeywordsParams = {
    /** 要检查的文本 */
    text: string;
  };

  type cleanupOldLogsParams = {
    daysToKeep?: number;
  };

  type CompetitorInfo = {
    id?: number;
    deviceName?: string;
    manufacturerBrand?: string;
    deviceCode?: string;
    usageScope?: string;
    deviceDescription?: string;
    dataSource?: string;
    certificationType?: string;
    status?: "ACTIVE" | "INACTIVE" | "EXPIRED";
    riskLevel?: "LOW" | "MEDIUM" | "HIGH";
    certificationDate?: string;
    expiryDate?: string;
    remarks?: string;
    createdAt?: string;
    updatedAt?: string;
  };

  type crawlAllCrawlersParams = {
    /** 每个爬虫的最大记录数 */
    maxRecordsPerCrawler?: number;
    /** 批处理大小 */
    batchSize?: number;
  };

  type crawlByKeywordParams = {
    /** 搜索关键词 */
    keyword: string;
    /** 每个爬虫的最大记录数 */
    maxRecordsPerCrawler?: number;
    /** 批处理大小 */
    batchSize?: number;
  };

  type CrawlerData = {
    /** 数据ID */
    id?: string;
    /** 数据源名称 */
    sourceName?: string;
    title?: string;
    url?: string;
    summary?: string;
    content?: string;
    country?: string;
    type?: string;
    /** 适用商品/产品 */
    product?: string;
    publishDate?: string;
    /** 发布时间列表 */
    releaseDate?: string[];
    /** 执行时间列表 */
    executionDate?: string[];
    crawlTime?: string;
    status?: "NEW" | "PROCESSING" | "PROCESSED" | "ERROR" | "DUPLICATE";
    isProcessed?: boolean;
    processedTime?: string;
    remarks?: string;
    /** 是否相关 */
    related?: boolean;
    /** 匹配的关键词 */
    matchedKeywords?: string;
    /** 风险等级 */
    riskLevel?: "HIGH" | "MEDIUM" | "LOW" | "NONE";
    /** 风险说明 */
    riskDescription?: string;
    createdAt?: string;
    updatedAt?: string;
    deleted?: number;
  };

  type CrawlerDataItem = {
    /** 数据ID */
    id?: string;
    /** 标题 */
    title?: string;
    /** 摘要 */
    summary?: string;
    /** 内容 */
    content?: string;
    /** URL */
    url?: string;
    /** 数据源名称 */
    sourceName?: string;
    /** 国家 */
    country?: string;
    /** 类型 */
    type?: string;
    /** 产品 */
    product?: string;
    /** 相关性 */
    related?: boolean;
    /** 状态 */
    status?: string;
    /** 备注 */
    remarks?: string;
    /** 爬取时间 */
    crawlTime?: string;
    /** 创建时间 */
    createdAt?: string;
    /** 更新时间 */
    updatedAt?: string;
  };

  type CrawlerDataQueryRequest = {
    /** 关键词搜索 */
    keyword?: string;
    /** 市场代码 */
    marketCode?: string;
    /** 国家/地区 */
    country?: string;
    /** 数据源名称 */
    sourceName?: string;
    /** 自定义关键词 */
    customKeywords?: string;
    /** 是否相关：true-相关，false-不相关，null-未确定 */
    related?: boolean;
    /** 开始日期 */
    startDate?: string;
    /** 结束日期 */
    endDate?: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type CrawlerDataResponse = {
    /** 是否成功 */
    success?: boolean;
    /** 响应消息 */
    message?: string;
    /** 响应时间戳 */
    timestamp?: string;
    data?: DataWrapper;
    /** 错误信息 */
    error?: string;
  };

  type CrawlerDataSearchResult = {
    /** 爬虫数据列表 */
    crawlerDataList?: CrawlerData[];
    /** 总数 */
    total?: number;
    /** 当前页 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 总页数 */
    totalPages?: number;
    /** 搜索关键词 */
    searchKeywords?: string[];
    /** 匹配的关键词统计 */
    matchedKeywords?: Record<string, any>;
    /** 市场代码 */
    marketCode?: string;
    /** 市场名称 */
    marketName?: string;
    /** 主管机构 */
    authority?: string;
    /** 高优先级监测项 */
    highPriorityItems?: string[];
  };

  type crawlRegistrationWithKeywordsParams = {
    /** 关键词列表(逗号分隔) */
    inputKeywords?: string;
    /** 最大记录数，0或-1表示爬取所有数据 */
    maxRecords?: number;
    /** 批次大小 */
    batchSize?: number;
    /** 开始日期(可选,格式:yyyy-MM-dd) */
    dateFrom?: string;
    /** 结束日期(可选,格式:yyyy-MM-dd) */
    dateTo?: string;
  };

  type DataWrapper = {
    /** 数据列表 */
    content?: CrawlerDataItem[];
    /** 总记录数 */
    totalElements?: number;
    /** 总页数 */
    totalPages?: number;
    /** 当前页码 */
    currentPage?: number;
    /** 每页大小 */
    pageSize?: number;
    /** 是否有下一页 */
    hasNext?: boolean;
    /** 是否有上一页 */
    hasPrevious?: boolean;
  };

  type deleteCompetitorInfoParams = {
    /** 竞品信息ID */
    id: number;
  };

  type deleteCrawlerDataParams = {
    /** 数据ID */
    id: string;
  };

  type deleteKeyword1Params = {
    /** 关键词 */
    keyword: string;
  };

  type deleteKeywordParams = {
    index: number;
  };

  type deleteNotificationParams = {
    id: number;
  };

  type deleteStandardParams = {
    /** 标准ID */
    id: number;
  };

  type diagnoseUrlParams = {
    /** 要诊断的URL */
    url: string;
  };

  type executeAllCrawlersParams = {
    /** 搜索关键词（可选） */
    keyword?: string;
    /** 每个爬虫爬取的数量 */
    countPerCrawler?: number;
  };

  type executeKeywordMatchingBySourceParams = {
    /** 数据源名称 */
    sourceName: string;
    /** 批处理大小 */
    batchSize?: number;
  };

  type executeKeywordMatchingParams = {
    /** 批处理大小 */
    batchSize?: number;
  };

  type executeSgsCrawlerParams = {
    /** 爬取数量 */
    count?: number;
  };

  type executeSgsCrawlerWithFiltersParams = {
    /** 搜索关键词 */
    keyword?: string;
    /** 爬取数量 */
    count?: number;
    /** 新闻类型值（可选） */
    newsType?: string;
    /** 日期范围值（可选） */
    dateRange?: string;
    /** 主题值列表（逗号分隔，可选） */
    topics?: string;
  };

  type executeSgsCrawlerWithKeywordParams = {
    /** 搜索关键词 */
    keyword: string;
    /** 爬取数量 */
    count?: number;
  };

  type executeSpecificCrawlerParams = {
    /** 爬虫名称（SGS或UL） */
    crawlerName: string;
    /** 搜索关键词（可选） */
    keyword?: string;
    /** 爬取数量 */
    count?: number;
  };

  type exportDataBySourceToExcelParams = {
    /** 数据源名称 */
    sourceName: string;
  };

  type exportDataByStatusToExcelParams = {
    /** 数据状态 */
    status: "NEW" | "PROCESSING" | "PROCESSED" | "ERROR" | "DUPLICATE";
  };

  type findByProductAndKeywordParams = {
    /** 产品名称 */
    product: string;
    /** 关键词 */
    keyword: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type findByProductContainingParams = {
    /** 产品名称关键词 */
    product: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type findByProductParams = {
    /** 产品名称 */
    product: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type findBySourceNameAndProductParams = {
    /** 数据源名称 */
    sourceName: string;
    /** 产品名称 */
    product: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type generateTrendChartParams = {
    /** 天数 */
    days?: number;
  };

  type getAllCountriesTrendParams = {
    days?: number;
  };

  type getCompetitorInfoDetailParams = {
    /** 竞品信息ID */
    id: number;
  };

  type getCompetitorListParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 搜索关键词 */
    keyword?: string;
    /** 状态筛选 */
    status?: string;
    /** 数据来源筛选 */
    dataSource?: string;
    /** 风险等级筛选 */
    riskLevel?: string;
  };

  type getCountryRiskChangeDataParams = {
    country: string;
    startDate: string;
    endDate: string;
  };

  type getCountryRiskLevelDistributionParams = {
    country: string;
    statDate: string;
  };

  type getCountryRiskStatsByDateParams = {
    /** 日期，格式：yyyy-MM-dd */
    date: string;
  };

  type getCountryRiskTrendSummaryParams = {
    country: string;
    days?: number;
  };

  type getCountryStatsByDateRangeParams = {
    startDate: string;
    endDate: string;
  };

  type getCountrySumByDateRangeParams = {
    startDate: string;
    endDate: string;
  };

  type getCountryTrendParams = {
    country: string;
    startDate: string;
    endDate: string;
  };

  type getCrawlerConfigParams = {
    /** 爬虫类型 */
    crawlerType: string;
  };

  type getCrawlerDataParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 关键词搜索（标题、内容、摘要） */
    keyword?: string;
    /** 国家筛选 */
    country?: string;
    /** 相关性筛选 */
    related?: boolean;
    /** 数据源筛选 */
    sourceName?: string;
    /** 类型筛选 */
    type?: string;
    /** 开始日期 (yyyy-MM-dd) */
    startDate?: string;
    /** 结束日期 (yyyy-MM-dd) */
    endDate?: string;
    /** 排序字段 */
    sortBy?: string;
    /** 排序方向 */
    sortDirection?: string;
    /** 风险等级筛选 */
    riskLevel?: string;
  };

  type getCustomsCasesParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 裁定结果 */
    rulingResult?: string;
    /** 违规类型 */
    violationType?: string;
  };

  type getDevice510KRecordsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 设备名称 */
    deviceName?: string;
    /** 申请人 */
    applicant?: string;
    /** 设备类别 */
    deviceClass?: string;
  };

  type getDeviceEventReportsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 品牌名称 */
    brandName?: string;
    /** 通用名称 */
    genericName?: string;
    /** 制造商名称 */
    manufacturerName?: string;
  };

  type getDeviceRecallRecordsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 产品描述 */
    productDescription?: string;
    /** 召回公司 */
    recallingFirm?: string;
    /** 设备名称 */
    deviceName?: string;
  };

  type getDeviceRegistrationRecordsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 制造商名称 */
    manufacturerName?: string;
  };

  type getErrorLogsParams = {
    limit?: number;
  };

  type getFilteredSmartChartDataParams = {
    countries?: string[];
    startDate: string;
    endDate: string;
  };

  type getGuidanceDocumentsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 标题 */
    title?: string;
    /** 话题 */
    topic?: string;
  };

  type getHighRiskDataByTypeParams = {
    /** 数据类型 */
    dataType: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 排序字段 */
    sortBy?: string;
    /** 排序方向 */
    sortDir?: string;
  };

  type getHighRiskDataDetailParams = {
    /** 数据类型 */
    dataType: string;
    /** 数据ID */
    id: number;
  };

  type getLatestStandardsByPublishedDateParams = {
    /** 限制数量 */
    limit?: number;
  };

  type getLogsParams = {
    level?: string;
    type?: string;
    source?: string;
    entityType?: string;
    status?: string;
    startTime?: string;
    endTime?: string;
    page?: number;
    limit?: number;
  };

  type getMultiCountryComparisonDataParams = {
    startDate: string;
    endDate: string;
  };

  type getNotificationsParams = {
    type?: string;
    isSent?: boolean;
    page?: number;
    limit?: number;
  };

  type getPopularProductsParams = {
    /** 限制数量 */
    limit?: number;
  };

  type getPredefinedCountryFilteredSmartChartDataParams = {
    startDate: string;
    endDate: string;
  };

  type getPredefinedCountryTrendDataParams = {
    displayCountryName: string;
    startDate: string;
    endDate: string;
  };

  type getProductDetailStatisticsParams = {
    /** 产品名称 */
    product: string;
  };

  type getRecentLogsParams = {
    limit?: number;
  };

  type getRecentlyUpdatedStandardsParams = {
    /** 限制数量 */
    limit?: number;
  };

  type getSmartChartDataParams = {
    startDate: string;
    endDate: string;
  };

  type getSmartCountryTrendDataParams = {
    country: string;
    startDate: string;
    endDate: string;
  };

  type getStandardByNumberParams = {
    /** 标准编号 */
    standardNumber: string;
  };

  type getStandardParams = {
    /** 标准ID */
    id: number;
  };

  type getStandardsByCountryParams = {
    /** 国家 */
    country: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    limit?: number;
  };

  type getStandardsManagementParams = {
    /** 关键词搜索 */
    keyword?: string;
    /** 风险等级 */
    risk?: string;
    /** 国家/地区 */
    country?: string;
    /** 标准状态 */
    status?: "DRAFT" | "ACTIVE" | "SUPERSEDED" | "WITHDRAWN" | "UNDER_REVISION";
    /** 是否监控 */
    isMonitored?: boolean;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type getStandardsParams = {
    /** 关键词搜索 */
    keyword?: string;
    /** 风险等级 */
    riskLevel?: string;
    /** 国家 */
    country?: string;
    /** 标准状态 */
    status?: string;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type getStatsByDateParams = {
    statDate: string;
  };

  type getTrendData1Params = {
    days?: number;
  };

  type getTrendDataParams = {
    /** 天数 */
    days?: number;
  };

  type getUpcomingStandardsByEffectiveDateParams = {
    /** 限制数量 */
    limit?: number;
  };

  type getUpcomingStandardsParams = {
    /** 天数 */
    days?: number;
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type PageableObject = {
    offset?: number;
    sort?: SortObject;
    pageSize?: number;
    pageNumber?: number;
    unpaged?: boolean;
    paged?: boolean;
  };

  type PageMapStringObject = {
    totalElements?: number;
    totalPages?: number;
    size?: number;
    content?: Record<string, any>[];
    number?: number;
    sort?: SortObject;
    first?: boolean;
    last?: boolean;
    numberOfElements?: number;
    pageable?: PageableObject;
    empty?: boolean;
  };

  type queryCrawlerDataByMarketParams = {
    /** 市场代码 */
    marketCode: string;
    request: CrawlerDataQueryRequest;
  };

  type queryCrawlerDataParams = {
    request: CrawlerDataQueryRequest;
  };

  type queryHighPriorityCrawlerDataParams = {
    request: CrawlerDataQueryRequest;
  };

  type runFullTestParams = {
    /** 要爬取的总数量 */
    totalCount?: number;
  };

  type searchBTIParams = {
    /** 发布国家 */
    refCountry?: string;
    /** BTI参考号 */
    reference?: string;
    /** 有效期开始日期(DD/MM/YYYY) */
    valStartDate?: string;
    /** 有效期开始日期结束(DD/MM/YYYY) */
    valStartDateTo?: string;
    /** 有效期结束日期(DD/MM/YYYY) */
    valEndDate?: string;
    /** 有效期结束日期结束(DD/MM/YYYY) */
    valEndDateTo?: string;
    /** 补充日期(DD/MM/YYYY) */
    supplDate?: string;
    /** 商品编码 */
    nomenc?: string;
    /** 商品编码结束 */
    nomencTo?: string;
    /** 关键词搜索 */
    keywordSearch?: string;
    /** 关键词匹配规则(OR/AND) */
    keywordMatchRule?: string;
    /** 排除关键词 */
    excludeKeyword?: string;
    /** 最大页数 */
    maxPages?: number;
  };

  type searchCustomsCaseParams = {
    /** HS编码 */
    hsCode?: string;
    /** 最大记录数 */
    maxRecords?: number;
    /** 批次大小 */
    batchSize?: number;
    /** 开始日期(MM/DD/YYYY) */
    startDate?: string;
  };

  type searchD510KParams = {
    /** 设备名称关键词 */
    deviceName?: string;
    /** 申请人名称 */
    applicantName?: string;
    /** 开始日期(MM/DD/YYYY) */
    dateFrom?: string;
    /** 结束日期(MM/DD/YYYY) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchDEventParams = {
    /** 品牌名称 */
    brandName?: string;
    /** 制造商名称 */
    manufacturer?: string;
    /** 型号 */
    modelNumber?: string;
    /** 开始日期(MM/DD/YYYY) */
    dateFrom?: string;
    /** 结束日期(MM/DD/YYYY) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchDeviceDataByKeywordsParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 实体类型列表，用逗号分隔 */
    entityTypes?: string;
    /** 风险等级过滤 */
    riskLevel?: string;
    /** 国家过滤 */
    country?: string;
  };

  type searchDRecallParams = {
    /** 产品名称 */
    productName?: string;
    /** 召回原因 */
    reasonForRecall?: string;
    /** 召回公司 */
    recallingFirm?: string;
    /** 开始日期(MM/DD/YYYY) */
    dateFrom?: string;
    /** 结束日期(MM/DD/YYYY) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchDRegistrationParams = {
    /** 设备名称（使用device_name搜索） */
    establishmentName?: string;
    /** 专有名称（使用proprietary_name搜索） */
    proprietaryName?: string;
    /** 制造商名称（使用manufacturer_name搜索） */
    ownerOperatorName?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchGuidanceParams = {
    /** 最大记录数 */
    maxRecords?: number;
  };

  type searchHighRiskDataParams = {
    /** 页码 */
    page?: number;
    /** 每页大小 */
    size?: number;
  };

  type searchRegistrationParams = {
    /** Trade name */
    tradeName?: string;
    /** 制造商名称 */
    manufacturerName?: string;
    /** 风险等级 */
    riskClass?: string;
    /** 最大记录数，-1表示爬取所有数据 */
    maxRecords?: number;
    /** 批次大小 */
    batchSize?: number;
    /** 开始日期(可选,格式:yyyy-MM-dd) */
    dateFrom?: string;
    /** 结束日期(可选,格式:yyyy-MM-dd) */
    dateTo?: string;
  };

  type searchUnicrawlParams = {
    /** 总爬取数量 */
    totalCount?: number;
    /** 开始日期(YYYY-MM-DD) */
    dateFrom?: string;
    /** 结束日期(YYYY-MM-DD) */
    dateTo?: string;
    /** 输入关键词列表 */
    inputKeywords?: string;
    /** 最大页数(0表示爬取所有) */
    maxPages?: number;
  };

  type searchUS510KParams = {
    /** 设备名称关键词 */
    deviceName?: string;
    /** 申请人名称 */
    applicantName?: string;
    /** trade_name关键词(使用openfda.device_name搜索) */
    tradeName?: string;
    /** 开始日期(YYYY-MM-DD) */
    dateFrom?: string;
    /** 结束日期(YYYY-MM-DD) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchUSEventParams = {
    /** 设备名称 */
    deviceName?: string;
    /** 制造商名称 */
    manufacturer?: string;
    /** 产品问题 */
    productProblem?: string;
    /** 开始日期(YYYY-MM-DD) */
    dateFrom?: string;
    /** 结束日期(YYYY-MM-DD) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type searchUSRecallParams = {
    /** 召回公司 */
    recallingFirm?: string;
    /** brand name */
    brandName?: string;
    /** 产品描述 */
    productDescription?: string;
    /** 开始日期(YYYY-MM-DD) */
    dateFrom?: string;
    /** 结束日期(YYYY-MM-DD) */
    dateTo?: string;
    /** 最大爬取页数，0表示爬取所有数据 */
    maxPages?: number;
    /** 输入关键词列表 */
    inputKeywords?: string;
  };

  type setDateDataSameAsPreviousDayParams = {
    targetDate: string;
  };

  type SortObject = {
    empty?: boolean;
    sorted?: boolean;
    unsorted?: boolean;
  };

  type Standard = {
    /** 标准ID */
    id?: number;
    /** 标准编号 */
    standardNumber?: string;
    /** 标准版本 */
    version?: string;
    /** 标准标题 */
    title?: string;
    /** 标准描述 */
    description?: string;
    /** 发布日期 */
    publishedDate?: string;
    /** 生效日期 */
    effectiveDate?: string;
    /** 下载链接 */
    downloadUrl?: string;
    /** 关键词 */
    keywords?: string;
    /** 风险等级 */
    riskLevel?: "LOW" | "MEDIUM" | "HIGH";
    /** 监管影响 */
    regulatoryImpact?: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    /** 标准状态 */
    standardStatus?:
      | "DRAFT"
      | "ACTIVE"
      | "SUPERSEDED"
      | "WITHDRAWN"
      | "UNDER_REVISION";
    /** 主要国家/地区 */
    country?: string;
    /** 适用国家列表（JSON格式） */
    countries?: string;
    /** 适用范围 */
    scope?: string;
    /** 产品类型 */
    productTypes?: string;
    /** 频率范围 */
    frequencyBands?: string;
    /** 功率限制 */
    powerLimits?: string;
    /** 测试方法 */
    testMethods?: string;
    /** 合规截止日期 */
    complianceDeadline?: string;
    /** 过渡期结束 */
    transitionEnd?: string;
    /** 风险评分 */
    riskScore?: number;
    /** 匹配的产品档案 */
    matchedProfiles?: string;
    /** 原始摘要 */
    rawExcerpt?: string;
    /** 是否监控 */
    isMonitored?: boolean;
    /** 创建时间 */
    createdAt?: string;
    /** 更新时间 */
    updatedAt?: string;
    /** 逻辑删除标记 */
    deleted?: number;
    countryList?: string[];
  };

  type StandardCreateRequest = {
    /** 标准编号 */
    standardNumber: string;
    /** 标准版本 */
    version?: string;
    /** 标准标题 */
    title: string;
    /** 标准描述 */
    description?: string;
    /** 发布日期 */
    publishedDate?: string;
    /** 生效日期 */
    effectiveDate?: string;
    /** 下载链接 */
    downloadUrl?: string;
    /** 关键词 */
    keywords?: string;
    /** 风险等级 */
    riskLevel?: "LOW" | "MEDIUM" | "HIGH";
    /** 监管影响 */
    regulatoryImpact?: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    /** 标准状态 */
    standardStatus?:
      | "DRAFT"
      | "ACTIVE"
      | "SUPERSEDED"
      | "WITHDRAWN"
      | "UNDER_REVISION";
    /** 主要国家/地区 */
    country?: string;
    /** 适用国家列表 */
    countries?: string[];
    /** 适用范围 */
    scope?: string;
    /** 产品类型 */
    productTypes?: string;
    /** 频率范围 */
    frequencyBands?: string;
    /** 功率限制 */
    powerLimits?: string;
    /** 测试方法 */
    testMethods?: string;
    /** 合规截止日期 */
    complianceDeadline?: string;
    /** 过渡期结束 */
    transitionEnd?: string;
    /** 风险评分 */
    riskScore?: number;
    /** 匹配的产品档案 */
    matchedProfiles?: string;
    /** 原始摘要 */
    rawExcerpt?: string;
    /** 是否监控 */
    isMonitored?: boolean;
  };

  type StandardSearchResult = {
    /** 标准列表 */
    standards?: Standard[];
    /** 总数 */
    total?: number;
    /** 当前页 */
    page?: number;
    /** 每页大小 */
    size?: number;
    /** 总页数 */
    totalPages?: number;
    /** 风险统计 */
    riskStats?: Record<string, any>[];
    /** 状态统计 */
    statusStats?: Record<string, any>[];
    /** 是否缓存 */
    cached?: boolean;
    /** 时间戳 */
    timestamp?: string;
  };

  type StandardUpdateRequest = {
    /** 标准编号 */
    standardNumber?: string;
    /** 标准版本 */
    version?: string;
    /** 标准标题 */
    title?: string;
    /** 标准描述 */
    description?: string;
    /** 发布日期 */
    publishedDate?: string;
    /** 生效日期 */
    effectiveDate?: string;
    /** 下载链接 */
    downloadUrl?: string;
    /** 关键词 */
    keywords?: string;
    /** 风险等级 */
    riskLevel?: "LOW" | "MEDIUM" | "HIGH";
    /** 监管影响 */
    regulatoryImpact?: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    /** 标准状态 */
    standardStatus?:
      | "DRAFT"
      | "ACTIVE"
      | "SUPERSEDED"
      | "WITHDRAWN"
      | "UNDER_REVISION";
    /** 主要国家/地区 */
    country?: string;
    /** 适用国家列表 */
    countries?: string[];
    /** 适用范围 */
    scope?: string;
    /** 产品类型 */
    productTypes?: string;
    /** 频率范围 */
    frequencyBands?: string;
    /** 功率限制 */
    powerLimits?: string;
    /** 测试方法 */
    testMethods?: string;
    /** 合规截止日期 */
    complianceDeadline?: string;
    /** 过渡期结束 */
    transitionEnd?: string;
    /** 风险评分 */
    riskScore?: number;
    /** 匹配的产品档案 */
    matchedProfiles?: string;
    /** 原始摘要 */
    rawExcerpt?: string;
    /** 是否监控 */
    isMonitored?: boolean;
  };

  type testULCrawlerBatchSaveParams = {
    /** 要爬取的总数量 */
    totalCount?: number;
    /** 每批保存数量 */
    batchSize?: number;
    /** 开始位置索引 */
    startIndex?: number;
  };

  type testULCrawlerContinueFromPositionParams = {
    /** 要爬取的总数量 */
    totalCount?: number;
    /** 开始位置索引 */
    startIndex?: number;
  };

  type testULCrawlerLatestParams = {
    /** 要爬取的总数量 */
    totalCount?: number;
  };

  type updateCompetitorInfoParams = {
    /** 竞品信息ID */
    id: number;
  };

  type updateCrawlerDataParams = {
    /** 数据ID */
    id: string;
    /** 相关性状态 */
    related: boolean;
  };

  type updateEntityRiskLevelAndKeywordsParams = {
    /** 实体类型 */
    entityType: string;
    /** 实体ID */
    id: number;
  };

  type updateKeyword1Params = {
    /** 关键词ID */
    id: number;
    /** 关键词 */
    keyword: string;
    /** 描述 */
    description?: string;
    /** 是否启用 */
    enabled?: boolean;
  };

  type updateKeywordParams = {
    index: number;
  };

  type updateMonitoringStatusParams = {
    /** 标准ID */
    id: number;
  };

  type updateRiskLevelParams = {
    /** 数据类型 */
    dataType: string;
    /** 数据ID */
    id: number;
  };

  type updateStandardParams = {
    /** 标准ID */
    id: number;
  };
}

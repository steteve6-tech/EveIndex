import request from '@/utils/request'

/**
 * 爬虫数据管理API
 */

// 获取爬虫数据列表（支持搜索、分页、排序）
export function getCrawlerData(params) {
  return request({
    url: '/api/crawler-data/list',
    method: 'get',
    params
  })
}

// 更新爬虫数据的相关性
export function updateCrawlerData(id, related) {
  return request({
    url: `/api/crawler-data/${id}/related`,
    method: 'put',
    params: { related }
  })
}

// 自动处理相关性
export function autoProcessRelated() {
  return request({
    url: '/api/crawler-data/auto-process-related',
    method: 'post'
  })
}

// 发送邮件
export function sendEmail(data) {
  return request({
    url: '/api/crawler-data/send-email',
    method: 'post',
    data
  })
}

// 删除爬虫数据
export function deleteCrawlerData(id) {
  return request({
    url: `/api/crawler-data/${id}`,
    method: 'delete'
  })
}

// 获取统计数据
export function getStatistics() {
  return request({
    url: '/api/crawler-data/statistics',
    method: 'get'
  })
}

// 根据产品名称查询数据
export function findByProduct(params) {
  return request({
    url: '/api/crawler-data/by-product',
    method: 'get',
    params
  })
}

// 根据产品名称模糊查询
export function findByProductContaining(params) {
  return request({
    url: '/api/crawler-data/by-product-contains',
    method: 'get',
    params
  })
}

// 根据数据源和产品名称查询
export function findBySourceNameAndProduct(params) {
  return request({
    url: '/api/crawler-data/by-source-and-product',
    method: 'get',
    params
  })
}

// 根据产品名称和关键词查询
export function findByProductAndKeyword(params) {
  return request({
    url: '/api/crawler-data/by-product-and-keyword',
    method: 'get',
    params
  })
}

// 统计各产品的数据量
export function getProductStatistics() {
  return request({
    url: '/api/crawler-data/product-statistics',
    method: 'get'
  })
}

// 获取热门产品列表
export function getPopularProducts(limit = 10) {
  return request({
    url: '/api/crawler-data/popular-products',
    method: 'get',
    params: { limit }
  })
}

// 获取指定产品的详细统计信息
export function getProductDetailStatistics(product) {
  return request({
    url: '/api/crawler-data/product-detail-statistics',
    method: 'get',
    params: { product }
  })
}

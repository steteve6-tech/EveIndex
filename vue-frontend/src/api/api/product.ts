import request from '@/request'

// 竞品信息相关接口

/**
 * 获取竞品信息列表
 */
export function getProductList(params?: {
  keyword?: string
  deviceClass?: string
  page?: number
  size?: number
}) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取竞品信息详情
 */
export function getProductById(id: number) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

/**
 * 创建竞品信息
 */
export function createProduct(data: any) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

/**
 * 更新竞品信息
 */
export function updateProduct(id: number, data: any) {
  return request({
    url: `/products/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除竞品信息
 */
export function deleteProduct(id: number) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}

/**
 * 从高风险数据生成竞品信息
 */
export function generateProductFromHighRiskData(data: {
  dataSource: string
  sourceDataId: number
  productName: string
  applicantName?: string
  brandName?: string
  deviceCode?: string
  deviceClass?: string
  deviceDescription?: string
}) {
  return request({
    url: '/products/generate',
    method: 'post',
    data
  })
}

/**
 * 批量从高风险数据生成竞品信息
 */
export function batchGenerateProducts(data: any[]) {
  return request({
    url: '/products/batch-generate',
    method: 'post',
    data
  })
}

/**
 * 获取竞品信息统计
 */
export function getProductStatistics() {
  return request({
    url: '/products/statistics',
    method: 'get'
  })
}

/**
 * 检查竞品信息是否存在
 */
export function checkProductExists(dataSource: string, sourceDataId: number) {
  return request({
    url: '/products/check-exists',
    method: 'get',
    params: {
      dataSource,
      sourceDataId
    }
  })
}

// 竞品信息数据类型定义
export interface Product {
  id?: number
  productName: string
  brand?: string
  model?: string
  productType?: string
  applicantName?: string
  brandName?: string
  deviceCode?: string
  dataSource?: string
  sourceDataId?: number
  deviceClass?: string
  deviceDescription?: string
  remarks?: string
  isActive?: number
  createTime?: string
  updateTime?: string
}

// 竞品信息列表响应类型
export interface ProductListResponse {
  success: boolean
  data: {
    list: Product[]
    total: number
    totalPages: number
    currentPage: number
    pageSize: number
  }
  message: string
}

// 竞品信息统计响应类型
export interface ProductStatisticsResponse {
  success: boolean
  data: {
    totalRecords: number
    activeCompetitors: number
    class1Count: number
    class2Count: number
    class3Count: number
    monthlyNew: number
    riskAlerts: number
  }
  message: string
}

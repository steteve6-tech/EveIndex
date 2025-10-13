import request from '../request'

/**
 * AI判断参数接口
 */
export interface AIJudgeParams {
  country?: string
  entityTypes?: string[]
  riskLevel?: string
  limit?: number
}

/**
 * 审核项接口
 */
export interface AuditItem {
  id: number
  entityType: string
  deviceName: string
  relatedToSkinDevice: boolean
  reason: string
  confidence: number
  blacklistKeywords: string[]
}

/**
 * AI判断结果接口
 */
export interface AIJudgeResult {
  success: boolean
  message: string
  data: {
    auditItems: AuditItem[]
    keptCount: number
    downgradedCount: number
    failedCount: number
    estimatedCost: string
  }
}

/**
 * 统计结果接口
 */
export interface AIJudgeStatistics {
  success: boolean
  data: {
    totalCount: number
    byEntityType: Record<string, number>
    byRiskLevel: Record<string, number>
  }
}

/**
 * 预览AI判断结果
 * @param params 判断参数
 */
export function previewAIJudge(params: AIJudgeParams) {
  return request.post<AIJudgeResult>('/device-data/ai-judge/preview', params)
}

/**
 * 执行AI判断操作
 * @param auditItems 审核项列表
 */
export function executeAIJudge(auditItems: AuditItem[]) {
  return request.post('/device-data/ai-judge/execute', { auditItems })
}

/**
 * 获取AI判断统计信息
 * @param params 查询参数
 */
export function getAIJudgeStatistics(params: AIJudgeParams) {
  return request.get<AIJudgeStatistics>('/device-data/ai-judge/statistics', { params })
}

/**
 * 预览AI判断（带黑名单检查）
 * @param params 判断参数
 */
export function previewAIJudgeWithBlacklist(params: any) {
  return request.post('/device-data/ai-judge/preview-with-blacklist', params)
}

/**
 * 执行AI判断（带黑名单更新）
 * @param params 执行参数
 */
export function executeAIJudgeWithBlacklist(params: any) {
  return request.post('/device-data/ai-judge/execute-with-blacklist', params)
}

/**
 * 获取黑名单关键词列表
 */
export function getBlacklistKeywords() {
  return request.get('/device-data/ai-judge/blacklist-keywords')
}


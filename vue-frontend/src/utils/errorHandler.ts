// 错误处理工具类
export class ErrorHandler {
  // 判断是否为关键错误（会阻止页面显示）
  static isCriticalError(error: any): boolean {
    // 网络连接错误
    if (error.code === 'ECONNABORTED' || error.code === 'NETWORK_ERROR') {
      return true
    }
    
    // 服务器错误
    if (error.response?.status >= 500) {
      return true
    }
    
    // 认证错误
    if (error.response?.status === 401 || error.response?.status === 403) {
      return true
    }
    
    return false
  }
  
  // 判断是否为可忽略的错误
  static isIgnorableError(error: any): boolean {
    // 404错误（某些API可能不存在）
    if (error.response?.status === 404) {
      return true
    }
    
    // 某些统计API失败不应该阻止页面显示
    const ignorableUrls = [
      '/risk-level-statistics',
      '/daily-country-risk-stats',
      '/country-risk-trends',
      '/country-risk-ranking'
    ]
    
    const url = error.config?.url || ''
    return ignorableUrls.some(ignorableUrl => url.includes(ignorableUrl))
  }
  
  // 获取用户友好的错误信息
  static getErrorMessage(error: any): string {
    if (this.isIgnorableError(error)) {
      return '' // 不显示可忽略的错误
    }
    
    if (error.response?.status === 404) {
      return '某些功能暂时不可用'
    }
    
    if (error.code === 'ECONNABORTED') {
      return '请求超时，请稍后重试'
    }
    
    if (error.response?.status >= 500) {
      return '服务器暂时不可用，请稍后重试'
    }
    
    return error.message || '请求失败'
  }
}

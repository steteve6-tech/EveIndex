// 性能优化工具类
export class PerformanceOptimizer {
  private static cache = new Map<string, { data: any; timestamp: number; ttl: number }>()
  private static readonly DEFAULT_TTL = 5 * 60 * 1000 // 5分钟缓存

  // 缓存数据
  static setCache(key: string, data: any, ttl: number = this.DEFAULT_TTL): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  // 获取缓存数据
  static getCache(key: string): any | null {
    const cached = this.cache.get(key)
    if (!cached) return null

    // 检查是否过期
    if (Date.now() - cached.timestamp > cached.ttl) {
      this.cache.delete(key)
      return null
    }

    return cached.data
  }

  // 清除缓存
  static clearCache(key?: string): void {
    if (key) {
      this.cache.delete(key)
    } else {
      this.cache.clear()
    }
  }

  // 防抖函数
  static debounce<T extends (...args: any[]) => any>(
    func: T,
    wait: number
  ): (...args: Parameters<T>) => void {
    let timeout: NodeJS.Timeout
    return (...args: Parameters<T>) => {
      clearTimeout(timeout)
      timeout = setTimeout(() => func.apply(this, args), wait)
    }
  }

  // 节流函数
  static throttle<T extends (...args: any[]) => any>(
    func: T,
    limit: number
  ): (...args: Parameters<T>) => void {
    let inThrottle: boolean
    return (...args: Parameters<T>) => {
      if (!inThrottle) {
        func.apply(this, args)
        inThrottle = true
        setTimeout(() => inThrottle = false, limit)
      }
    }
  }

  // 批量处理数据
  static batchProcess<T>(items: T[], batchSize: number, processor: (batch: T[]) => void): void {
    for (let i = 0; i < items.length; i += batchSize) {
      const batch = items.slice(i, i + batchSize)
      processor(batch)
    }
  }

  // 延迟执行
  static delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

import axios, { AxiosRequestConfig, AxiosResponse } from 'axios'

interface RetryConfig {
  retries: number
  retryDelay: number
  retryCondition?: (error: any) => boolean
}

const defaultRetryConfig: RetryConfig = {
  retries: 3,
  retryDelay: 1000,
  retryCondition: (error) => {
    // 只对网络错误和5xx错误重试
    return !error.response || error.response.status >= 500
  }
}

/**
 * 带重试机制的请求函数
 * @param config Axios请求配置
 * @param retryConfig 重试配置
 * @returns Promise<AxiosResponse>
 */
export const requestWithRetry = async (
  config: AxiosRequestConfig,
  retryConfig: RetryConfig = defaultRetryConfig
): Promise<AxiosResponse> => {
  let lastError: any
  
  for (let i = 0; i <= retryConfig.retries; i++) {
    try {
      const response = await axios(config)
      return response
    } catch (error) {
      lastError = error
      
      if (i === retryConfig.retries || !retryConfig.retryCondition!(error)) {
        throw error
      }
      
      // 指数退避延迟
      const delay = retryConfig.retryDelay * Math.pow(2, i)
      console.log(`请求失败，${delay}ms后重试 (${i + 1}/${retryConfig.retries})`)
      await new Promise(resolve => setTimeout(resolve, delay))
    }
  }
  
  throw lastError
}

/**
 * 为大数据量请求创建专门的重试配置
 */
export const dataRequestRetryConfig: RetryConfig = {
  retries: 2,
  retryDelay: 2000,
  retryCondition: (error) => {
    // 对超时和5xx错误重试
    return error.code === 'ECONNABORTED' || 
           !error.response || 
           error.response.status >= 500
  }
}

/**
 * 为快速请求创建的重试配置
 */
export const quickRequestRetryConfig: RetryConfig = {
  retries: 1,
  retryDelay: 500,
  retryCondition: (error) => {
    // 只对5xx错误重试
    return !error.response || error.response.status >= 500
  }
}

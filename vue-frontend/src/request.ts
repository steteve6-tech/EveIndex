import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const request = axios.create({
  baseURL: '', // 使用相对路径，让 Vite 代理处理
  timeout: 60000, // 增加到60秒，特别是AI处理接口需要更长时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等认证信息
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 添加调试信息
    console.log('📊 Axios响应拦截器:', {
      status: response.status,
      statusText: response.statusText,
      url: response.config.url,
      contentType: response.headers['content-type'],
      dataType: typeof response.data,
      dataLength: response.data ? response.data.length : 0
    })
    
    // 检查响应内容类型
    const contentType = response.headers['content-type'] || ''
    if (contentType.includes('text/html')) {
      console.error('❌ 收到HTML响应而不是JSON:', response.data)
      throw new Error('收到HTML响应而不是JSON数据，可能是API路径错误')
    }
    
    // 直接返回响应数据
    return response.data
  },
  (error) => {
    // 处理错误响应
    console.error('💥 Axios错误拦截器:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      contentType: error.response?.headers['content-type'],
      data: error.response?.data
    })
    
    let errorMessage = '请求失败'
    
    if (error.response) {
      const { status, data } = error.response
      const contentType = error.response.headers['content-type'] || ''
      
      // 如果收到HTML响应，说明API路径有问题
      if (contentType.includes('text/html')) {
        errorMessage = 'API路径错误，收到HTML响应而不是JSON数据'
      } else {
        switch (status) {
          case 400:
            errorMessage = data?.message || '请求参数错误'
            break
          case 401:
            errorMessage = '未授权，请重新登录'
            break
          case 403:
            errorMessage = '拒绝访问'
            break
          case 404:
            errorMessage = '请求的资源不存在'
            break
          case 500:
            errorMessage = '服务器内部错误'
            break
          default:
            errorMessage = data?.message || `请求失败 (${status})`
        }
      }
    } else if (error.request) {
      errorMessage = '网络连接失败，请检查网络设置'
    } else {
      errorMessage = error.message || '请求配置错误'
    }
    
    message.error(errorMessage)
    return Promise.reject(error)
  }
)

// 为AI处理接口创建专门的请求实例，设置更长的超时时间
const aiRequest = axios.create({
  baseURL: '', // 使用相对路径，让 Vite 代理处理
  timeout: 300000, // 5分钟超时，AI处理需要更长时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// AI请求的响应拦截器
aiRequest.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('💥 AI处理请求错误:', error)
    let errorMessage = 'AI处理失败'
    
    if (error.code === 'ECONNABORTED') {
      errorMessage = 'AI处理超时，请稍后重试'
    } else if (error.response) {
      errorMessage = error.response.data?.message || 'AI处理失败'
    }
    
    message.error(errorMessage)
    return Promise.reject(error)
  }
)

export default request
export { aiRequest }

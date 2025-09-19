import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || 'http://localhost:8080', // API的base_url
  timeout: 30000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    console.log('发送请求:', config.url, config.params || config.data)
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    const res = response.data
    
    console.log('收到响应:', response.config.url, res)
    
    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    if (response.status === 200) {
      return res
    } else {
      // 否则的话抛出错误
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 对响应错误做点什么
    console.error('响应错误:', error)
    
    let errorMessage = '网络错误'
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      switch (status) {
        case 400:
          errorMessage = data.message || '请求参数错误'
          break
        case 401:
          errorMessage = '未授权，请重新登录'
          break
        case 403:
          errorMessage = '拒绝访问'
          break
        case 404:
          errorMessage = '请求地址出错'
          break
        case 408:
          errorMessage = '请求超时'
          break
        case 500:
          errorMessage = data.message || '服务器内部错误'
          break
        case 501:
          errorMessage = '服务未实现'
          break
        case 502:
          errorMessage = '网关错误'
          break
        case 503:
          errorMessage = '服务不可用'
          break
        case 504:
          errorMessage = '网关超时'
          break
        case 505:
          errorMessage = 'HTTP版本不受支持'
          break
        default:
          errorMessage = data.message || `连接错误${status}`
      }
    } else if (error.request) {
      // 请求已经发出，但没有收到响应
      errorMessage = '网络连接失败，请检查网络'
    } else {
      // 其他错误
      errorMessage = error.message || '未知错误'
    }
    
    message.error(errorMessage)
    return Promise.reject(error)
  }
)

export default service

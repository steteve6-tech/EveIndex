<template>
  <div class="crawler-test">
    <h1>爬虫管理系统测试</h1>
    
    <div class="test-section">
      <h2>爬虫执行测试</h2>
      
      <a-row :gutter="[8, 8]">
        <a-col :span="6">
          <a-button type="primary" @click="testExecuteAllCrawlers" :loading="loading" block>
            执行所有爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testExecuteSgsCrawler" :loading="loading" block>
            执行SGS爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testExecuteULCrawler" :loading="loading" block>
            执行UL爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetCrawlerStates" :loading="loading" block>
            获取爬虫状态
          </a-button>
        </a-col>
      </a-row>

      <a-row :gutter="[8, 8]" style="margin-top: 8px">
        <a-col :span="6">
          <a-button type="primary" @click="testGetCrawlerStatistics" :loading="loading" block>
            获取统计信息
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetSystemHealth" :loading="loading" block>
            系统健康检查
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testResetSgsCrawler" :loading="loading" block>
            重置SGS爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testResetULCrawler" :loading="loading" block>
            重置UL爬虫
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 测试结果显示 -->
    <div class="result-section">
      <h3>测试结果:</h3>
      <pre>{{ testResult }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  executeAllCrawlers,
  executeSgsCrawler,
  executeULCrawler,
  getAllCrawlerStates,
  getCrawlerStatistics,
  getSystemHealth,
  resetCrawlerState
} from '@/api/crawlerManager'

const loading = ref(false)
const testResult = ref('')

// 执行所有爬虫
const testExecuteAllCrawlers = async () => {
  loading.value = true
  testResult.value = '正在执行所有爬虫...'
  
  try {
    console.log('测试执行所有爬虫...')
    const result = await executeAllCrawlers()
    console.log('执行所有爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('执行所有爬虫成功')
    } else {
      message.error('执行所有爬虫失败')
    }
  } catch (error: any) {
    console.error('执行所有爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('执行所有爬虫失败')
  } finally {
    loading.value = false
  }
}

// 执行SGS爬虫
const testExecuteSgsCrawler = async () => {
  loading.value = true
  testResult.value = '正在执行SGS爬虫...'
  
  try {
    console.log('测试执行SGS爬虫...')
    const result = await executeSgsCrawler()
    console.log('执行SGS爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('执行SGS爬虫成功')
    } else {
      message.error('执行SGS爬虫失败')
    }
  } catch (error: any) {
    console.error('执行SGS爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('执行SGS爬虫失败')
  } finally {
    loading.value = false
  }
}

// 执行UL爬虫
const testExecuteULCrawler = async () => {
  loading.value = true
  testResult.value = '正在执行UL爬虫...'
  
  try {
    console.log('测试执行UL爬虫...')
    const result = await executeULCrawler()
    console.log('执行UL爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('执行UL爬虫成功')
    } else {
      message.error('执行UL爬虫失败')
    }
  } catch (error: any) {
    console.error('执行UL爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('执行UL爬虫失败')
  } finally {
    loading.value = false
  }
}

// 获取爬虫状态
const testGetCrawlerStates = async () => {
  loading.value = true
  testResult.value = '正在获取爬虫状态...'
  
  try {
    console.log('测试获取爬虫状态...')
    const result = await getAllCrawlerStates()
    console.log('获取爬虫状态返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取爬虫状态成功')
    } else {
      message.error('获取爬虫状态失败')
    }
  } catch (error: any) {
    console.error('获取爬虫状态失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取爬虫状态失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const testGetCrawlerStatistics = async () => {
  loading.value = true
  testResult.value = '正在获取统计信息...'
  
  try {
    console.log('测试获取统计信息...')
    const result = await getCrawlerStatistics()
    console.log('获取统计信息返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取统计信息成功')
    } else {
      message.error('获取统计信息失败')
    }
  } catch (error: any) {
    console.error('获取统计信息失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取统计信息失败')
  } finally {
    loading.value = false
  }
}

// 系统健康检查
const testGetSystemHealth = async () => {
  loading.value = true
  testResult.value = '正在检查系统健康状态...'
  
  try {
    console.log('测试系统健康检查...')
    const result = await getSystemHealth()
    console.log('系统健康检查返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('系统健康检查成功')
    } else {
      message.error('系统健康检查失败')
    }
  } catch (error: any) {
    console.error('系统健康检查失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('系统健康检查失败')
  } finally {
    loading.value = false
  }
}

// 重置SGS爬虫
const testResetSgsCrawler = async () => {
  loading.value = true
  testResult.value = '正在重置SGS爬虫...'
  
  try {
    console.log('测试重置SGS爬虫...')
    const result = await resetCrawlerState('SGS')
    console.log('重置SGS爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('重置SGS爬虫成功')
    } else {
      message.error('重置SGS爬虫失败')
    }
  } catch (error: any) {
    console.error('重置SGS爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('重置SGS爬虫失败')
  } finally {
    loading.value = false
  }
}

// 重置UL爬虫
const testResetULCrawler = async () => {
  loading.value = true
  testResult.value = '正在重置UL爬虫...'
  
  try {
    console.log('测试重置UL爬虫...')
    const result = await resetCrawlerState('UL')
    console.log('重置UL爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('重置UL爬虫成功')
    } else {
      message.error('重置UL爬虫失败')
    }
  } catch (error: any) {
    console.error('重置UL爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('重置UL爬虫失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.crawler-test {
  padding: 24px;
}

.test-section {
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.test-section h2 {
  margin-bottom: 16px;
  color: #1890ff;
  font-size: 18px;
  font-weight: 600;
}

.result-section {
  margin-top: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
}

.result-section h3 {
  margin-bottom: 12px;
  color: #262626;
  font-size: 16px;
  font-weight: 600;
}

.result-section pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 500px;
  overflow-y: auto;
  background: #fff;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
}

.test-section .ant-btn {
  margin-bottom: 8px;
  font-weight: 500;
}

.test-section .ant-btn-primary {
  background: #1890ff;
  border-color: #1890ff;
}

.test-section .ant-btn-primary:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.test-section .ant-btn-default {
  background: #fff;
  border-color: #d9d9d9;
  color: #595959;
}

.test-section .ant-btn-default:hover {
  background: #f5f5f5;
  border-color: #40a9ff;
  color: #40a9ff;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .crawler-test {
    padding: 16px;
  }
  
  .test-section {
    padding: 16px;
  }
  
  .result-section pre {
    max-height: 300px;
    font-size: 11px;
  }
}
</style>

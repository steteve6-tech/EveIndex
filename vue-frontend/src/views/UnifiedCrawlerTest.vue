<template>
  <div class="unified-crawler-test">
    <h1>统一爬虫控制器测试</h1>
    
    <div class="test-section">
      <h2>统一爬虫执行测试</h2>
      
      <a-row :gutter="[8, 8]">
        <a-col :span="6">
          <a-button type="primary" @click="testExecuteUnifiedCrawl" :loading="loading" block>
            执行统一爬取
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testExecuteQuickTest" :loading="loading" block>
            快速测试
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetAvailableCrawlers" :loading="loading" block>
            获取可用爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetDefaultConfig" :loading="loading" block>
            获取默认配置
          </a-button>
        </a-col>
      </a-row>

      <a-row :gutter="[8, 8]" style="margin-top: 8px">
        <a-col :span="6">
          <a-button type="primary" @click="testGetQuickTestConfig" :loading="loading" block>
            获取快速测试配置
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testValidateKeywords" :loading="loading" block>
            验证关键词文件
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetSystemStatus" :loading="loading" block>
            获取系统状态
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="testGetExecutionHistory" :loading="loading" block>
            获取执行历史
          </a-button>
        </a-col>
      </a-row>
    </div>

    <div class="test-section">
      <h2>特定爬虫测试</h2>
      
      <a-row :gutter="[8, 8]">
        <a-col :span="6">
          <a-button type="default" @click="testTestCustomsCrawler" :loading="loading" block>
            测试海关爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testTestDevice510KCrawler" :loading="loading" block>
            测试510K爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testTestDeviceEventCrawler" :loading="loading" block>
            测试设备事件爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testTestDeviceRecallCrawler" :loading="loading" block>
            测试设备召回爬虫
          </a-button>
        </a-col>
      </a-row>

      <a-row :gutter="[8, 8]" style="margin-top: 8px">
        <a-col :span="6">
          <a-button type="default" @click="testTestDeviceRegistrationCrawler" :loading="loading" block>
            测试设备注册爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testTestGuidanceCrawler" :loading="loading" block>
            测试指导文档爬虫
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testClearExecutionHistory" :loading="loading" block>
            清理执行历史
          </a-button>
        </a-col>
        <a-col :span="6">
          <a-button type="default" @click="testCustomConfig" :loading="loading" block>
            自定义配置测试
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
  executeUnifiedCrawl,
  executeUnifiedCrawlWithConfig,
  executeQuickTest,
  testSpecificCrawler,
  getAvailableCrawlers,
  getDefaultConfig,
  getQuickTestConfig,
  validateKeywordsFile,
  getSystemStatus,
  getExecutionHistory,
  clearExecutionHistory,
  type UnifiedCrawlConfig
} from '@/api/unifiedCrawler'

const loading = ref(false)
const testResult = ref('')

// 执行统一爬取
const testExecuteUnifiedCrawl = async () => {
  loading.value = true
  testResult.value = '正在执行统一爬取...'
  
  try {
    console.log('测试执行统一爬取...')
    const result = await executeUnifiedCrawl()
    console.log('执行统一爬取返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('执行统一爬取成功')
    } else {
      message.error('执行统一爬取失败')
    }
  } catch (error: any) {
    console.error('执行统一爬取失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('执行统一爬取失败')
  } finally {
    loading.value = false
  }
}

// 执行快速测试
const testExecuteQuickTest = async () => {
  loading.value = true
  testResult.value = '正在执行快速测试...'
  
  try {
    console.log('测试执行快速测试...')
    const result = await executeQuickTest()
    console.log('执行快速测试返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('执行快速测试成功')
    } else {
      message.error('执行快速测试失败')
    }
  } catch (error: any) {
    console.error('执行快速测试失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('执行快速测试失败')
  } finally {
    loading.value = false
  }
}

// 获取可用爬虫
const testGetAvailableCrawlers = async () => {
  loading.value = true
  testResult.value = '正在获取可用爬虫...'
  
  try {
    console.log('测试获取可用爬虫...')
    const result = await getAvailableCrawlers()
    console.log('获取可用爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取可用爬虫成功')
    } else {
      message.error('获取可用爬虫失败')
    }
  } catch (error: any) {
    console.error('获取可用爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取可用爬虫失败')
  } finally {
    loading.value = false
  }
}

// 获取默认配置
const testGetDefaultConfig = async () => {
  loading.value = true
  testResult.value = '正在获取默认配置...'
  
  try {
    console.log('测试获取默认配置...')
    const result = await getDefaultConfig()
    console.log('获取默认配置返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取默认配置成功')
    } else {
      message.error('获取默认配置失败')
    }
  } catch (error: any) {
    console.error('获取默认配置失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取默认配置失败')
  } finally {
    loading.value = false
  }
}

// 获取快速测试配置
const testGetQuickTestConfig = async () => {
  loading.value = true
  testResult.value = '正在获取快速测试配置...'
  
  try {
    console.log('测试获取快速测试配置...')
    const result = await getQuickTestConfig()
    console.log('获取快速测试配置返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取快速测试配置成功')
    } else {
      message.error('获取快速测试配置失败')
    }
  } catch (error: any) {
    console.error('获取快速测试配置失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取快速测试配置失败')
  } finally {
    loading.value = false
  }
}

// 验证关键词文件
const testValidateKeywords = async () => {
  loading.value = true
  testResult.value = '正在验证关键词文件...'
  
  try {
    console.log('测试验证关键词文件...')
    const result = await validateKeywordsFile()
    console.log('验证关键词文件返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('验证关键词文件成功')
    } else {
      message.error('验证关键词文件失败')
    }
  } catch (error: any) {
    console.error('验证关键词文件失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('验证关键词文件失败')
  } finally {
    loading.value = false
  }
}

// 获取系统状态
const testGetSystemStatus = async () => {
  loading.value = true
  testResult.value = '正在获取系统状态...'
  
  try {
    console.log('测试获取系统状态...')
    const result = await getSystemStatus()
    console.log('获取系统状态返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取系统状态成功')
    } else {
      message.error('获取系统状态失败')
    }
  } catch (error: any) {
    console.error('获取系统状态失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取系统状态失败')
  } finally {
    loading.value = false
  }
}

// 获取执行历史
const testGetExecutionHistory = async () => {
  loading.value = true
  testResult.value = '正在获取执行历史...'
  
  try {
    console.log('测试获取执行历史...')
    const result = await getExecutionHistory()
    console.log('获取执行历史返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('获取执行历史成功')
    } else {
      message.error('获取执行历史失败')
    }
  } catch (error: any) {
    console.error('获取执行历史失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('获取执行历史失败')
  } finally {
    loading.value = false
  }
}

// 测试海关爬虫
const testTestCustomsCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试海关爬虫...'
  
  try {
    console.log('测试海关爬虫...')
    const result = await testSpecificCrawler('customs', 'Skin', 5)
    console.log('测试海关爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试海关爬虫成功')
    } else {
      message.error('测试海关爬虫失败')
    }
  } catch (error: any) {
    console.error('测试海关爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试海关爬虫失败')
  } finally {
    loading.value = false
  }
}

// 测试510K爬虫
const testTestDevice510KCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试510K爬虫...'
  
  try {
    console.log('测试510K爬虫...')
    const result = await testSpecificCrawler('device510k', 'Skin', 5)
    console.log('测试510K爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试510K爬虫成功')
    } else {
      message.error('测试510K爬虫失败')
    }
  } catch (error: any) {
    console.error('测试510K爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试510K爬虫失败')
  } finally {
    loading.value = false
  }
}

// 测试设备事件爬虫
const testTestDeviceEventCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试设备事件爬虫...'
  
  try {
    console.log('测试设备事件爬虫...')
    const result = await testSpecificCrawler('deviceEvent', 'Skin', 5)
    console.log('测试设备事件爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试设备事件爬虫成功')
    } else {
      message.error('测试设备事件爬虫失败')
    }
  } catch (error: any) {
    console.error('测试设备事件爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试设备事件爬虫失败')
  } finally {
    loading.value = false
  }
}

// 测试设备召回爬虫
const testTestDeviceRecallCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试设备召回爬虫...'
  
  try {
    console.log('测试设备召回爬虫...')
    const result = await testSpecificCrawler('deviceRecall', 'Skin', 5)
    console.log('测试设备召回爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试设备召回爬虫成功')
    } else {
      message.error('测试设备召回爬虫失败')
    }
  } catch (error: any) {
    console.error('测试设备召回爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试设备召回爬虫失败')
  } finally {
    loading.value = false
  }
}

// 测试设备注册爬虫
const testTestDeviceRegistrationCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试设备注册爬虫...'
  
  try {
    console.log('测试设备注册爬虫...')
    const result = await testSpecificCrawler('deviceRegistration', 'Skin', 5)
    console.log('测试设备注册爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试设备注册爬虫成功')
    } else {
      message.error('测试设备注册爬虫失败')
    }
  } catch (error: any) {
    console.error('测试设备注册爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试设备注册爬虫失败')
  } finally {
    loading.value = false
  }
}

// 测试指导文档爬虫
const testTestGuidanceCrawler = async () => {
  loading.value = true
  testResult.value = '正在测试指导文档爬虫...'
  
  try {
    console.log('测试指导文档爬虫...')
    const result = await testSpecificCrawler('guidance', 'Skin', 5)
    console.log('测试指导文档爬虫返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('测试指导文档爬虫成功')
    } else {
      message.error('测试指导文档爬虫失败')
    }
  } catch (error: any) {
    console.error('测试指导文档爬虫失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('测试指导文档爬虫失败')
  } finally {
    loading.value = false
  }
}

// 清理执行历史
const testClearExecutionHistory = async () => {
  loading.value = true
  testResult.value = '正在清理执行历史...'
  
  try {
    console.log('测试清理执行历史...')
    const result = await clearExecutionHistory()
    console.log('清理执行历史返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('清理执行历史成功')
    } else {
      message.error('清理执行历史失败')
    }
  } catch (error: any) {
    console.error('清理执行历史失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('清理执行历史失败')
  } finally {
    loading.value = false
  }
}

// 自定义配置测试
const testCustomConfig = async () => {
  loading.value = true
  testResult.value = '正在执行自定义配置测试...'
  
  try {
    console.log('测试自定义配置...')
    
    // 创建自定义配置
    const customConfig: UnifiedCrawlConfig = {
      batchSize: 5,
      timeoutMs: 10000,
      maxRetries: 2,
      parallelExecution: false,
      enabledCrawlers: ['customs', 'device510k'],
      keywords: ['Skin', 'Medical'],
      maxRecordsPerKeyword: 3
    }
    
    const result = await executeUnifiedCrawlWithConfig(customConfig)
    console.log('自定义配置测试返回:', result)
    
    testResult.value = JSON.stringify(result, null, 2)
    if (result.success) {
      message.success('自定义配置测试成功')
    } else {
      message.error('自定义配置测试失败')
    }
  } catch (error: any) {
    console.error('自定义配置测试失败:', error)
    testResult.value = `错误: ${error.message}\n\n详细信息: ${JSON.stringify(error, null, 2)}`
    message.error('自定义配置测试失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.unified-crawler-test {
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
  .unified-crawler-test {
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

<template>
  <div class="cert-news-ai-judge">
    <!-- 配置卡片 -->
    <a-card title="🤖 AI智能判断（认证新闻）" class="config-card">
      <template #extra>
        <a-space>
          <a-tag color="blue">智能提取认证关键词</a-tag>
          <a-tag color="green">自动写入remarks</a-tag>
        </a-space>
      </template>

      <a-form layout="vertical">
        <a-row :gutter="16">
          <!-- 筛选条件 -->
          <a-col :span="6">
            <a-form-item label="风险等级">
              <a-select v-model:value="config.riskLevel" placeholder="全部等级" allow-clear>
                <a-select-option value="">全部等级</a-select-option>
                <a-select-option value="HIGH">高风险</a-select-option>
                <a-select-option value="MEDIUM">中风险</a-select-option>
                <a-select-option value="LOW">低风险</a-select-option>
                <a-select-option value="UNDETERMINED">未确定</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="数据源">
              <a-select v-model:value="config.sourceName" placeholder="全部数据源" allow-clear>
                <a-select-option value="">全部数据源</a-select-option>
                <a-select-option value="SGS">SGS</a-select-option>
                <a-select-option value="UL Solutions">UL Solutions</a-select-option>
                <a-select-option value="北测">北测</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="判断方式">
              <a-radio-group v-model:value="config.judgeMode" button-style="solid">
                <a-radio-button value="limit">指定数量</a-radio-button>
                <a-radio-button value="async">异步全部</a-radio-button>
              </a-radio-group>
              <a-input-number
                v-if="config.judgeMode === 'limit'"
                v-model:value="config.limit"
                :min="1"
                :max="100"
                placeholder="判断数量(≤100)"
                style="width: 100%; margin-top: 8px"
              />
              <a-alert
                v-if="config.judgeMode === 'async'"
                message="异步模式将在后台处理所有数据，适合几千条数据"
                type="info"
                show-icon
                style="margin-top: 8px"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 功能说明 -->
        <a-form-item>
          <a-alert
            message="AI判断说明"
            description="AI将判断中风险新闻是否与无线电子设备认证标准相关（FCC、CE、SRRC、RED、RoHS等）。相关新闻会设为高风险并提取认证关键词写入matched_keywords字段，判断依据写入remarks字段。"
            type="info"
            show-icon
            closable
          />
          
          <!-- 数据量警告 -->
          <div v-if="config.limit > 20 || config.judgeMode === 'all'" style="margin-top: 8px">
            <a-alert
              message="⚠️ 数据量较大"
              :description="`将处理 ${config.judgeMode === 'all' ? '所有' : config.limit} 条数据，可能需要较长时间，请耐心等待。建议单次处理不超过20条数据。`"
              type="warning"
              show-icon
              closable
            />
          </div>
        </a-form-item>

        <!-- 进度条 -->
        <a-form-item v-if="judging || progress > 0">
          <a-progress 
            :percent="progress" 
            :status="progress === 100 ? 'success' : 'active'"
            :stroke-color="progress === 100 ? '#52c41a' : '#1890ff'"
          />
          <div style="text-align: center; margin-top: 8px; color: #666;">
            {{ progressText }}
          </div>
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item>
          <a-space>
            <a-button
              type="primary"
              size="large"
              :icon="h(RobotOutlined)"
              :loading="judging"
              @click="startJudge"
            >
              执行AI判断
            </a-button>
            <a-button @click="resetConfig">重置配置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 结果展示模态框 -->
    <a-modal
      v-model:open="showResultModal"
      title="AI判断执行结果"
      width="1000px"
      :footer="null"
      @cancel="showResultModal = false"
    >
      <div v-if="resultData" style="padding: 16px 0;">
        <!-- 统计信息 -->
        <a-row :gutter="12" style="margin-bottom: 20px;">
          <a-col :span="8">
            <div style="text-align: center; padding: 16px; background: #f0f9ff; border-radius: 8px; border: 1px solid #91d5ff;">
              <div style="font-size: 28px; font-weight: 600; color: #1890ff; margin-bottom: 4px;">{{ resultData.totalCount || 0 }}</div>
              <div style="font-size: 13px; color: #0050b3;">📊 总处理</div>
            </div>
          </a-col>
          <a-col :span="8">
            <div style="text-align: center; padding: 16px; background: #fff7e6; border-radius: 8px; border: 1px solid #ffd591;">
              <div style="font-size: 28px; font-weight: 600; color: #fa8c16; margin-bottom: 4px;">{{ resultData.aiKept || 0 }}</div>
              <div style="font-size: 13px; color: #d46b08;">🔥 相关新闻</div>
            </div>
          </a-col>
          <a-col :span="8">
            <div style="text-align: center; padding: 16px; background: #fff1f0; border-radius: 8px; border: 1px solid #ffccc7;">
              <div style="font-size: 28px; font-weight: 600; color: #f5222d; margin-bottom: 4px;">{{ resultData.aiDowngraded || 0 }}</div>
              <div style="font-size: 13px; color: #a8071a;">⬇️ 不相关</div>
            </div>
          </a-col>
        </a-row>

        <!-- 提取关键词信息 -->
        <a-alert
          v-if="resultData.extractedKeywordCount > 0"
          message="已提取认证关键词"
          :description="`从相关新闻中提取了 ${resultData.extractedKeywordCount} 个认证关键词，已写入matched_keywords字段和关键词文件`"
          type="success"
          show-icon
          style="margin-bottom: 16px;"
        />

        <!-- 标签页展示详细结果 -->
        <h4 style="margin: 20px 0 16px 0;">📋 详细处理结果 (共 {{ (resultData.auditItems || []).length }} 条)</h4>
        
        <a-tabs v-if="(resultData.auditItems || []).length > 0" type="card">
          <!-- 相关新闻标签页 -->
          <a-tab-pane 
            v-if="(resultData.auditItems || []).filter((item: any) => item.relatedToCertification).length > 0"
            key="kept"
            :tab="`🔥 相关新闻 (${(resultData.auditItems || []).filter((item: any) => item.relatedToCertification).length})`"
          >
            <div style="max-height: 500px; overflow-y: auto;">
              <table style="width: 100%; border-collapse: collapse; border: 1px solid #e8e8e8;">
                <thead style="position: sticky; top: 0; background: #fff7e6; z-index: 1;">
                  <tr>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 28%;">标题</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 8%;">国家</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 10%;">数据源</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 8%;">置信度</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 15%;">提取关键词</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px; width: 23%;">判断依据(remarks)</th>
                    <th style="padding: 10px; text-align: center; border-bottom: 1px solid #ffd591; font-size: 12px; width: 8%;">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in (resultData.auditItems || []).filter((item: any) => item.relatedToCertification)" 
                    :key="item.id || index"
                    :style="{ background: index % 2 === 0 ? '#fafafa' : 'white' }"
                  >
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.title || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag color="orange" style="font-size: 10px;">{{ item.country || '-' }}</a-tag>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.sourceName || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag v-if="item.confidence" color="orange" style="font-size: 10px; font-weight: 600;">
                        {{ Math.round(item.confidence * 100) }}%
                      </a-tag>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-space v-if="item.extractedKeywords && item.extractedKeywords.length > 0" wrap :size="2">
                        <a-tag v-for="(keyword, idx) in item.extractedKeywords" :key="idx" color="blue" style="font-size: 9px; margin: 0;">
                          {{ keyword }}
                        </a-tag>
                      </a-space>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.remark || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: center;">
                      <a-button type="link" size="small" @click="showDetail(item.id)">详情</a-button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </a-tab-pane>

          <!-- 不相关新闻标签页 -->
          <a-tab-pane 
            v-if="(resultData.auditItems || []).filter((item: any) => !item.relatedToCertification).length > 0"
            key="downgraded"
            :tab="`⬇️ 不相关新闻 (${(resultData.auditItems || []).filter((item: any) => !item.relatedToCertification).length})`"
          >
            <div style="max-height: 500px; overflow-y: auto;">
              <table style="width: 100%; border-collapse: collapse; border: 1px solid #e8e8e8;">
                <thead style="position: sticky; top: 0; background: #fff1f0; z-index: 1;">
                  <tr>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 33%;">标题</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 10%;">国家</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 12%;">数据源</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 8%;">置信度</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 29%;">判断依据(remarks)</th>
                    <th style="padding: 10px; text-align: center; border-bottom: 1px solid #ffccc7; font-size: 12px; width: 8%;">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in (resultData.auditItems || []).filter((item: any) => !item.relatedToCertification)" 
                    :key="item.id || index"
                    :style="{ background: index % 2 === 0 ? '#fafafa' : 'white' }"
                  >
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.title || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag color="red" style="font-size: 10px;">{{ item.country || '-' }}</a-tag>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.sourceName || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag v-if="item.confidence" color="red" style="font-size: 10px; font-weight: 600;">
                        {{ Math.round(item.confidence * 100) }}%
                      </a-tag>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.remark || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: center;">
                      <a-button type="link" size="small" @click="showDetail(item.id)">详情</a-button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </a-tab-pane>

          <!-- 新提取认证关键词标签页 -->
          <a-tab-pane 
            v-if="resultData.newExtractedKeywords && resultData.newExtractedKeywords.length > 0"
            key="keywords"
            :tab="`📝 提取的认证关键词 (${resultData.newExtractedKeywords.length})`"
          >
            <div style="padding: 20px; display: flex; flex-wrap: wrap; gap: 8px; background: #f0f9ff; border-radius: 8px;">
              <a-tag 
                v-for="(keyword, index) in resultData.newExtractedKeywords" 
                :key="index" 
                color="blue"
                closable
                @close="handleRemoveKeyword(keyword)"
                style="font-size: 12px; padding: 6px 12px; cursor: pointer;"
              >
                <span @dblclick="handleEditKeyword(keyword, index)">{{ keyword }}</span>
              </a-tag>
            </div>
            <div style="margin-top: 12px; padding: 12px; background: #e6f7ff; border-radius: 4px;">
              <p style="margin: 0; font-size: 12px; color: #0050b3;">
                💡 这些认证关键词已从相关新闻中提取，已写入数据的matched_keywords字段，并自动添加到关键词文件中
              </p>
              <p style="margin: 8px 0 0 0; font-size: 11px; color: #1890ff;">
                🔧 双击关键词可编辑，点击X可删除
              </p>
            </div>
          </a-tab-pane>
        </a-tabs>

        <a-empty v-else description="暂无处理结果" style="margin: 40px 0;" />
      </div>
    </a-modal>

    <!-- 数据详情模态框 -->
    <a-modal
      v-model:open="showDetailModal"
      title="新闻详情"
      width="800px"
      :footer="null"
      @cancel="showDetailModal = false"
    >
      <div v-if="detailData" style="padding: 16px 0;">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="ID">{{ detailData.id }}</a-descriptions-item>
          <a-descriptions-item label="国家">{{ detailData.country || '-' }}</a-descriptions-item>
          <a-descriptions-item label="数据源">{{ detailData.sourceName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="风险等级">
            <a-tag :color="getRiskLevelColor(detailData.riskLevel)">
              {{ getRiskLevelText(detailData.riskLevel) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="标题" :span="2">{{ detailData.title || '-' }}</a-descriptions-item>
          <a-descriptions-item label="发布日期" :span="2">{{ detailData.publishDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="链接" :span="2">
            <a :href="detailData.link" target="_blank" v-if="detailData.link">{{ detailData.link }}</a>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="匹配关键词" :span="2">{{ detailData.matchedKeywords || '-' }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ detailData.remarks || '-' }}</a-descriptions-item>
          <a-descriptions-item label="摘要" :span="2">{{ detailData.summary || '-' }}</a-descriptions-item>
          <a-descriptions-item label="内容" :span="2">
            <div style="max-height: 300px; overflow-y: auto; white-space: pre-wrap;">{{ detailData.content || '-' }}</div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
      <a-empty v-else description="暂无详情数据" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  RobotOutlined
} from '@ant-design/icons-vue'
import { aiRequest } from '@/request'
import request from '@/request'

// Emits
const emit = defineEmits<{
  (e: 'judgeCompleted'): void
}>()

// 响应式数据
const config = reactive({
  riskLevel: '',
  sourceName: '',
  judgeMode: 'limit' as 'limit' | 'async',
  limit: 10  // 默认限制为10条
})

// 异步任务相关
const asyncTask = reactive({
  taskId: '',
  polling: false,
  progress: 0,
  status: '',
  totalCount: 0,
  processedCount: 0,
  relatedCount: 0,
  unrelatedCount: 0,
  keywordCount: 0
})

const judging = ref(false)
const progress = ref(0)
const progressText = ref('')

// 结果模态框
const showResultModal = ref(false)
const resultData = ref<any>(null)

// 详情模态框
const showDetailModal = ref(false)
const detailData = ref<any>(null)

// 方法
const startJudge = async () => {
  // 异步模式
  if (config.judgeMode === 'async') {
    startAsyncJudge()
    return
  }
  
  // 同步模式
  judging.value = true
  progress.value = 0
  progressText.value = '正在初始化...'
  
  try {
    const params: any = {
      limit: Math.min(config.limit, 100)
    }
    
    // 只添加非空参数
    if (config.riskLevel) params.riskLevel = config.riskLevel
    if (config.sourceName) params.sourceName = config.sourceName

    console.log('🔍 开始执行认证新闻AI判断，参数:', params)
    
    progress.value = 10
    progressText.value = '正在发送请求...'

    const response = await aiRequest.post('/crawler-data/ai-judge/execute-direct', null, { params })

    progress.value = 90
    progressText.value = '正在处理结果...'

    // 响应拦截器已返回response.data，所以response就是后端返回的数据
    if (response && response.success) {
      progress.value = 100
      progressText.value = '处理完成！'
      
      showExecutionResult(response.data)
      message.success(response.message || 'AI判断执行成功！')
      
      emit('judgeCompleted')
      
      setTimeout(() => {
        progress.value = 0
        progressText.value = ''
      }, 1000)
    } else {
      message.error(response?.message || 'AI判断执行失败')
    }
  } catch (error: any) {
    console.error('AI判断执行失败:', error)
    message.error(error.response?.data?.message || 'AI判断执行失败，请检查后端服务')
    progress.value = 0
    progressText.value = ''
  } finally {
    judging.value = false
  }
}

// 启动异步判断任务
const startAsyncJudge = async () => {
  judging.value = true
  
  try {
    const params: any = {}
    if (config.riskLevel) params.riskLevel = config.riskLevel
    if (config.sourceName) params.sourceName = config.sourceName
    
    console.log('🔍 创建异步AI判断任务，参数:', params)
    
    const response = await request.post('/crawler-data/ai-judge/task/create', null, { params })
    
    if (response && response.success) {
      asyncTask.taskId = response.taskId
      asyncTask.status = 'RUNNING'
      asyncTask.polling = true
      
      message.success('任务已创建，正在后台处理...')
      
      // 开始轮询进度
      pollTaskProgress()
    } else {
      message.error('创建任务失败')
      judging.value = false
    }
  } catch (error: any) {
    console.error('创建异步任务失败:', error)
    message.error('创建任务失败: ' + (error.response?.data?.error || error.message))
    judging.value = false
  }
}

// 轮询任务进度
const pollTaskProgress = async () => {
  if (!asyncTask.polling) return
  
  try {
    const response = await request.get(`/crawler-data/ai-judge/task/${asyncTask.taskId}`)
    
    if (response && response.success) {
      const task = response.task
      asyncTask.status = task.status
      asyncTask.progress = task.progress || 0
      asyncTask.totalCount = task.totalCount || 0
      asyncTask.processedCount = task.processedCount || 0
      asyncTask.relatedCount = task.relatedCount || 0
      asyncTask.unrelatedCount = task.unrelatedCount || 0
      asyncTask.keywordCount = task.keywordCount || 0
      
      progress.value = asyncTask.progress
      progressText.value = `正在处理: ${asyncTask.processedCount}/${asyncTask.totalCount} (${asyncTask.progress}%)`
      
      if (task.status === 'COMPLETED') {
        // 任务完成
        asyncTask.polling = false
        judging.value = false
        message.success(`处理完成！相关${asyncTask.relatedCount}条，不相关${asyncTask.unrelatedCount}条，提取关键词${asyncTask.keywordCount}个`)
        emit('judgeCompleted')
        
        // 显示统计摘要
        showAsyncTaskSummary()
      } else if (task.status === 'FAILED') {
        // 任务失败
        asyncTask.polling = false
        judging.value = false
        message.error('任务执行失败: ' + (task.errorMessage || '未知错误'))
      } else if (task.status === 'CANCELLED') {
        // 任务取消
        asyncTask.polling = false
        judging.value = false
        message.warning('任务已取消')
      } else {
        // 继续轮询
        setTimeout(pollTaskProgress, 2000) // 每2秒查询一次
      }
    }
  } catch (error: any) {
    console.error('查询任务进度失败:', error)
    asyncTask.polling = false
    judging.value = false
    message.error('查询进度失败')
  }
}

// 取消异步任务
const cancelAsyncTask = async () => {
  try {
    const response = await request.post(`/crawler-data/ai-judge/task/${asyncTask.taskId}/cancel`)
    if (response && response.success) {
      asyncTask.polling = false
      judging.value = false
      message.success('任务已取消')
    }
  } catch (error: any) {
    message.error('取消任务失败')
  }
}

// 显示异步任务摘要
const showAsyncTaskSummary = () => {
  Modal.info({
    title: '异步AI判断完成',
    width: 600,
    content: h('div', { style: { padding: '16px 0' } }, [
      h('div', { style: { marginBottom: '12px', fontSize: '16px', fontWeight: 'bold' } }, '处理统计'),
      h('div', { style: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' } }, [
        h('div', { style: { padding: '12px', background: '#f0f9ff', borderRadius: '8px' } }, [
          h('div', { style: { fontSize: '12px', color: '#666' } }, '总处理数'),
          h('div', { style: { fontSize: '24px', fontWeight: 'bold', color: '#1890ff' } }, asyncTask.totalCount)
        ]),
        h('div', { style: { padding: '12px', background: '#fff7e6', borderRadius: '8px' } }, [
          h('div', { style: { fontSize: '12px', color: '#666' } }, '相关新闻'),
          h('div', { style: { fontSize: '24px', fontWeight: 'bold', color: '#fa8c16' } }, asyncTask.relatedCount)
        ]),
        h('div', { style: { padding: '12px', background: '#fff1f0', borderRadius: '8px' } }, [
          h('div', { style: { fontSize: '12px', color: '#666' } }, '不相关新闻'),
          h('div', { style: { fontSize: '24px', fontWeight: 'bold', color: '#cf1322' } }, asyncTask.unrelatedCount)
        ]),
        h('div', { style: { padding: '12px', background: '#f6ffed', borderRadius: '8px' } }, [
          h('div', { style: { fontSize: '12px', color: '#666' } }, '提取关键词'),
          h('div', { style: { fontSize: '24px', fontWeight: 'bold', color: '#52c41a' } }, asyncTask.keywordCount)
        ])
      ]),
      h('div', { style: { marginTop: '16px', padding: '12px', background: '#e6f7ff', borderRadius: '8px', border: '1px solid #91d5ff' } }, [
        h('div', { style: { fontSize: '14px', color: '#0050b3' } }, '✅ 所有数据已完成处理，请刷新页面查看最新数据')
      ])
    ]),
    okText: '知道了'
  })
}

// 显示执行结果
const showExecutionResult = (result: any) => {
  console.log('📊 显示执行结果，数据:', result)
  console.log('📊 auditItems:', result.auditItems)
  console.log('📊 auditItems长度:', (result.auditItems || []).length)
  
  resultData.value = result
  showResultModal.value = true
}

// 重置配置
const resetConfig = () => {
  config.riskLevel = ''
  config.sourceName = ''
  config.judgeMode = 'limit'
  config.limit = 10
  progress.value = 0
  progressText.value = ''
}

// 查看详情
const showDetail = async (id: string) => {
  try {
    console.log('🔍 查看新闻详情，ID:', id)
    const response = await request.get(`/crawler-data/${id}`)
    // 响应拦截器已返回response.data
    if (response && response.success) {
      detailData.value = response.data
      showDetailModal.value = true
    } else {
      message.error('获取新闻详情失败')
    }
  } catch (error: any) {
    console.error('获取新闻详情失败:', error)
    message.error('获取新闻详情失败，请稍后重试')
  }
}

// 获取风险等级颜色
const getRiskLevelColor = (riskLevel: string) => {
  const colors: Record<string, string> = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'blue',
    'UNDETERMINED': 'gray',
    'NONE': 'default'
  }
  return colors[riskLevel] || 'default'
}

// 获取风险等级文本
const getRiskLevelText = (riskLevel: string) => {
  const texts: Record<string, string> = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'UNDETERMINED': '未确定',
    'NONE': '无风险'
  }
  return texts[riskLevel] || riskLevel
}

// 删除关键词
const handleRemoveKeyword = (keyword: string) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除关键词"${keyword}"吗？删除后将从关键词文件中移除。`,
    onOk: async () => {
      try {
        // 从结果数据中移除
        if (resultData.value && resultData.value.newExtractedKeywords) {
          const index = resultData.value.newExtractedKeywords.indexOf(keyword)
          if (index > -1) {
            resultData.value.newExtractedKeywords.splice(index, 1)
            resultData.value.extractedKeywordCount = resultData.value.newExtractedKeywords.length
          }
        }
        
        // 调用后端API删除关键词
        const response = await request.delete(`/crawler-data/keywords/${encodeURIComponent(keyword)}`)
        // 响应拦截器已返回response.data
        if (response && response.success) {
          message.success('关键词已删除')
        } else {
          message.warning('关键词已从显示中移除，但从文件删除可能失败')
        }
      } catch (error: any) {
        console.error('删除关键词失败:', error)
        message.error('删除关键词失败')
      }
    }
  })
}

// 编辑关键词
const handleEditKeyword = (keyword: string, index: number) => {
  Modal.confirm({
    title: '编辑关键词',
    content: h('div', [
      h('p', '当前关键词：' + keyword),
      h('input', {
        id: 'edit-keyword-input',
        type: 'text',
        value: keyword,
        style: {
          width: '100%',
          padding: '8px',
          border: '1px solid #d9d9d9',
          borderRadius: '4px',
          marginTop: '8px'
        }
      })
    ]),
    onOk: async () => {
      const input = document.getElementById('edit-keyword-input') as HTMLInputElement
      const newKeyword = input?.value?.trim()
      
      if (!newKeyword) {
        message.error('关键词不能为空')
        return Promise.reject()
      }
      
      if (newKeyword === keyword) {
        return Promise.resolve()
      }
      
      try {
        // 更新显示中的关键词
        if (resultData.value && resultData.value.newExtractedKeywords) {
          resultData.value.newExtractedKeywords[index] = newKeyword
        }
        
        // 调用后端API更新关键词
        const response = await request.put('/crawler-data/keywords', {
          oldKeyword: keyword,
          newKeyword: newKeyword
        })
        
        // 响应拦截器已返回response.data
        if (response && response.success) {
          message.success('关键词已更新')
        } else {
          message.warning('关键词显示已更新，但文件更新可能失败')
        }
      } catch (error: any) {
        console.error('更新关键词失败:', error)
        message.error('更新关键词失败')
        return Promise.reject()
      }
    }
  })
}

// 暴露方法
defineExpose({
  startJudge
})
</script>

<style scoped>
.cert-news-ai-judge {
  margin-bottom: 24px;
}

.config-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.config-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.config-card :deep(.ant-card-head-title) {
  color: white;
  font-weight: 600;
}

.config-card :deep(.ant-card-extra) {
  color: white;
}
</style>

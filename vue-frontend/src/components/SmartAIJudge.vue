<template>
  <div class="smart-ai-judge">
    <!-- 配置卡片 -->
    <a-card title="🤖 智能AI判断（黑名单优先）" class="config-card">
      <template #extra>
        <a-space>
          <a-tag color="green">黑名单优先，节省成本</a-tag>
          <a-tag color="blue">{{ blacklistKeywords.length }} 个黑名单</a-tag>
        </a-space>
      </template>

      <a-form layout="vertical">
        <a-row :gutter="16">
          <!-- 筛选条件 -->
          <a-col :span="6">
            <a-form-item label="选择国家">
              <a-select v-model:value="config.country" placeholder="全部国家" allow-clear>
                <a-select-option value="">全部国家</a-select-option>
                <a-select-option value="US">美国</a-select-option>
                <a-select-option value="EU">欧盟</a-select-option>
                <a-select-option value="KR">韩国</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="风险等级">
              <a-select v-model:value="config.riskLevel" placeholder="全部等级" allow-clear>
                <a-select-option value="">全部等级</a-select-option>
                <a-select-option value="HIGH">高风险</a-select-option>
                <a-select-option value="MEDIUM">中风险</a-select-option>
                <a-select-option value="LOW">低风险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="数据类型">
              <a-select
                v-model:value="config.entityTypes"
                mode="multiple"
                placeholder="全部类型"
                :max-tag-count="2"
                allow-clear>
                <a-select-option value="Device510K">申请记录</a-select-option>
                <a-select-option value="DeviceRegistrationRecord">注册记录</a-select-option>
                <a-select-option value="DeviceRecallRecord">召回记录</a-select-option>
                <a-select-option value="DeviceEventReport">事件报告</a-select-option>
                <a-select-option value="GuidanceDocument">指导文档</a-select-option>
                <a-select-option value="CustomsCase">海关案例</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="判断方式">
              <a-radio-group v-model:value="config.judgeMode" button-style="solid">
                <a-radio-button value="limit">指定数量</a-radio-button>
                <a-radio-button value="all">全部数据</a-radio-button>
              </a-radio-group>
              <a-input-number
                v-if="config.judgeMode === 'limit'"
                v-model:value="config.limit"
                :min="1"
                :max="500"
                placeholder="判断数量"
                style="width: 100%; margin-top: 8px"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 黑名单关键词管理 -->
        <a-form-item label="黑名单关键词（匹配后直接设为低风险，跳过AI判断）">
          <div class="blacklist-keywords-container">
            <!-- 添加关键词输入框 -->
            <div class="add-keyword-section">
              <a-input
                v-if="showAddBlacklist"
                ref="blacklistInputRef"
                v-model:value="newBlacklist"
                size="small"
                style="width: 300px"
                @blur="addBlacklist"
                @keyup.enter="addBlacklist"
                placeholder="输入黑名单关键词"
              />
              <a-button v-else type="dashed" size="small" @click="showAddBlacklist = true">
                <PlusOutlined />
                添加黑名单关键词
              </a-button>
            </div>
            
            <!-- 黑名单关键词列表 -->
            <div v-if="blacklistKeywords.length > 0" class="keywords-list">
              <div 
                v-for="(keyword, index) in blacklistKeywords" 
                :key="index"
                class="keyword-item"
              >
                <span class="keyword-number">{{ index + 1 }}.</span>
                <span class="keyword-name">{{ keyword }}</span>
                <a-button 
                  type="text" 
                  danger 
                  size="small"
                  @click="removeBlacklist(keyword)"
                  class="delete-btn"
                >
                  <DeleteOutlined />
                </a-button>
              </div>
            </div>
            
            <!-- 空状态提示 -->
            <div v-else class="empty-state">
              <span class="empty-text">暂无黑名单关键词</span>
            </div>
          </div>
          <div style="margin-top: 8px">
            <a-alert
              message="黑名单说明"
              description="包含黑名单关键词的数据将直接标记为低风险，无需消耗AI调用。黑名单会自动学习（低风险数据的制造商自动加入）。"
              type="info"
              show-icon
              closable
            />
          </div>
          
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
            <a-button :icon="h(ReloadOutlined)" @click="loadBlacklist">
              刷新黑名单
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
      width="900px"
      :footer="null"
      @cancel="showResultModal = false"
    >
      <div v-if="resultData" style="padding: 16px 0;">
        <!-- 统计信息 -->
        <a-row :gutter="12" style="margin-bottom: 20px;">
          <a-col :span="6">
            <div style="text-align: center; padding: 16px; background: #f0f9ff; border-radius: 8px; border: 1px solid #91d5ff;">
              <div style="font-size: 28px; font-weight: 600; color: #1890ff; margin-bottom: 4px;">{{ resultData.totalCount || 0 }}</div>
              <div style="font-size: 13px; color: #0050b3;">📊 总处理</div>
            </div>
          </a-col>
          <a-col :span="6">
            <div style="text-align: center; padding: 16px; background: #f6ffed; border-radius: 8px; border: 1px solid #b7eb8f;">
              <div style="font-size: 28px; font-weight: 600; color: #52c41a; margin-bottom: 4px;">{{ resultData.blacklistFiltered || 0 }}</div>
              <div style="font-size: 13px; color: #389e0d;">🛡️ 黑名单</div>
            </div>
          </a-col>
          <a-col :span="6">
            <div style="text-align: center; padding: 16px; background: #fff7e6; border-radius: 8px; border: 1px solid #ffd591;">
              <div style="font-size: 28px; font-weight: 600; color: #fa8c16; margin-bottom: 4px;">{{ resultData.aiKept || 0 }}</div>
              <div style="font-size: 13px; color: #d46b08;">🔥 AI保留</div>
            </div>
          </a-col>
          <a-col :span="6">
            <div style="text-align: center; padding: 16px; background: #fff1f0; border-radius: 8px; border: 1px solid #ffccc7;">
              <div style="font-size: 28px; font-weight: 600; color: #f5222d; margin-bottom: 4px;">{{ resultData.aiDowngraded || 0 }}</div>
              <div style="font-size: 13px; color: #a8071a;">⬇️ AI降级</div>
            </div>
          </a-col>
        </a-row>

        <!-- 成本信息 -->
        <a-alert
          v-if="resultData.newBlacklistCount > 0"
          message="新增黑名单关键词"
          :description="`新增 ${resultData.newBlacklistCount} 个黑名单关键词，这些关键词将用于未来的判断，节省AI调用成本`"
          type="success"
          show-icon
          style="margin-bottom: 16px;"
        />

        <!-- 标签页展示详细结果 -->
        <h4 style="margin: 20px 0 16px 0;">📋 详细处理结果 (共 {{ (resultData.auditItems || []).length }} 条)</h4>
        
        <a-tabs v-if="(resultData.auditItems || []).length > 0" type="card">
          <!-- 黑名单过滤标签页 -->
          <a-tab-pane 
            v-if="(resultData.auditItems || []).filter(item => item.blacklistMatched).length > 0"
            key="blacklist"
            :tab="`🛡️ 黑名单过滤 (${(resultData.auditItems || []).filter(item => item.blacklistMatched).length})`"
          >
            <div style="max-height: 400px; overflow-y: auto;">
              <table style="width: 100%; border-collapse: collapse; border: 1px solid #e8e8e8;">
                <thead style="position: sticky; top: 0; background: #f6ffed; z-index: 1;">
                  <tr>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #b7eb8f; font-size: 12px;">数据类型</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #b7eb8f; font-size: 12px;">设备名称</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #b7eb8f; font-size: 12px;">制造商</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #b7eb8f; font-size: 12px;">匹配关键词</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #b7eb8f; font-size: 12px;">备注</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in (resultData.auditItems || []).filter(item => item.blacklistMatched)" 
                    :key="item.id || index"
                    :style="{ background: index % 2 === 0 ? '#fafafa' : 'white' }"
                  >
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag color="green" style="font-size: 10px;">{{ item.entityType || '-' }}</a-tag>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.deviceName || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.manufacturer || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag v-if="item.matchedBlacklistKeyword" color="red" style="font-size: 10px;">{{ item.matchedBlacklistKeyword }}</a-tag>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.remark || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </a-tab-pane>

          <!-- 高风险数据标签页 -->
          <a-tab-pane 
            v-if="(resultData.auditItems || []).filter(item => !item.blacklistMatched && item.relatedToSkinDevice).length > 0"
            key="kept"
            :tab="`🔥 高风险数据 (${(resultData.auditItems || []).filter(item => !item.blacklistMatched && item.relatedToSkinDevice).length})`"
          >
            <div style="max-height: 400px; overflow-y: auto;">
              <table style="width: 100%; border-collapse: collapse; border: 1px solid #e8e8e8;">
                <thead style="position: sticky; top: 0; background: #fff7e6; z-index: 1;">
                  <tr>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">数据类型</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">设备名称</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">制造商</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">置信度</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">AI判断原因</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffd591; font-size: 12px;">备注</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in (resultData.auditItems || []).filter(item => !item.blacklistMatched && item.relatedToSkinDevice)" 
                    :key="item.id || index"
                    :style="{ background: index % 2 === 0 ? '#fafafa' : 'white' }"
                  >
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag color="orange" style="font-size: 10px;">{{ item.entityType || '-' }}</a-tag>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.deviceName || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.manufacturer || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag v-if="item.confidence" color="orange" style="font-size: 10px; font-weight: 600;">
                        {{ Math.round(item.confidence * 100) }}%
                      </a-tag>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.reason || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.remark || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </a-tab-pane>

          <!-- 低风险数据标签页 -->
          <a-tab-pane 
            v-if="(resultData.auditItems || []).filter(item => !item.blacklistMatched && !item.relatedToSkinDevice).length > 0"
            key="downgraded"
            :tab="`⬇️ 低风险数据 (${(resultData.auditItems || []).filter(item => !item.blacklistMatched && !item.relatedToSkinDevice).length})`"
          >
            <div style="max-height: 400px; overflow-y: auto;">
              <table style="width: 100%; border-collapse: collapse; border: 1px solid #e8e8e8;">
                <thead style="position: sticky; top: 0; background: #fff1f0; z-index: 1;">
                  <tr>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">数据类型</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">设备名称</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">制造商</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">置信度</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">AI判断原因</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">建议黑名单</th>
                    <th style="padding: 10px; text-align: left; border-bottom: 1px solid #ffccc7; font-size: 12px;">备注</th>
                  </tr>
                </thead>
                <tbody>
                  <tr 
                    v-for="(item, index) in (resultData.auditItems || []).filter(item => !item.blacklistMatched && !item.relatedToSkinDevice)" 
                    :key="item.id || index"
                    :style="{ background: index % 2 === 0 ? '#fafafa' : 'white' }"
                  >
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag color="red" style="font-size: 10px;">{{ item.entityType || '-' }}</a-tag>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.deviceName || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">{{ item.manufacturer || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-tag v-if="item.confidence" color="red" style="font-size: 10px; font-weight: 600;">
                        {{ Math.round(item.confidence * 100) }}%
                      </a-tag>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.reason || '-' }}</td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                      <a-space v-if="item.suggestedBlacklist && item.suggestedBlacklist.length > 0" wrap :size="2">
                        <a-tag v-for="(keyword, idx) in item.suggestedBlacklist" :key="idx" color="red" style="font-size: 9px; margin: 0;">
                          {{ keyword }}
                        </a-tag>
                      </a-space>
                      <span v-else>-</span>
                    </td>
                    <td style="padding: 8px 10px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 11px;">{{ item.remark || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </a-tab-pane>

          <!-- 新增黑名单关键词标签页 -->
          <a-tab-pane 
            v-if="resultData.newBlacklistKeywords && resultData.newBlacklistKeywords.length > 0"
            key="keywords"
            :tab="`📝 新增黑名单 (${resultData.newBlacklistKeywords.length})`"
          >
            <div style="padding: 20px; display: flex; flex-wrap: wrap; gap: 8px; background: #f0f9ff; border-radius: 8px;">
              <a-tag 
                v-for="(keyword, index) in resultData.newBlacklistKeywords" 
                :key="index" 
                color="blue"
                style="font-size: 12px; padding: 6px 12px;"
              >
                {{ keyword }}
              </a-tag>
            </div>
          </a-tab-pane>
        </a-tabs>

        <a-empty v-else description="暂无处理结果" style="margin: 40px 0;" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  RobotOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import request, { aiRequest } from '@/request'

// Emits
const emit = defineEmits<{
  (e: 'judgeCompleted'): void
}>()

// 响应式数据
const config = reactive({
  country: '',
  riskLevel: '',
  entityTypes: [] as string[],
  judgeMode: 'limit' as 'limit' | 'all',
  limit: 10  // 默认限制为10条，避免一次性处理过多数据
})

const blacklistKeywords = ref<string[]>([])
const showAddBlacklist = ref(false)
const newBlacklist = ref('')
const blacklistInputRef = ref()

const judging = ref(false)
const progress = ref(0)
const progressText = ref('')
const processingData = ref<any[]>([])

// 结果模态框
const showResultModal = ref(false)
const resultData = ref<any>(null)

// 不再需要表格列定义和计算属性，因为移除了预览功能

// 方法
const startJudge = async () => {
  judging.value = true
  progress.value = 0
  progressText.value = '正在初始化...'
  processingData.value = []
  
  try {
    const params = {
      country: config.country || undefined,
      entityTypes: config.entityTypes.length > 0 ? config.entityTypes : undefined,
      riskLevel: config.riskLevel || undefined,
      limit: config.judgeMode === 'limit' ? Math.min(config.limit, 20) : 20, // 限制单次处理数量
      judgeAll: config.judgeMode === 'all'
    }

    console.log('🔍 开始直接执行AI判断，参数:', params)
    
    // 显示进度
    progress.value = 10
    progressText.value = '正在发送请求...'

    const response = await aiRequest.post('/device-data/ai-judge/execute-direct', params)

    progress.value = 90
    progressText.value = '正在处理结果...'

    // 响应拦截器已返回response.data，所以response就是后端返回的数据
    if (response && response.success) {
      progress.value = 100
      progressText.value = '处理完成！'
      
      // 显示处理结果
      showExecutionResult(response.data)
      message.success(response.message || 'AI判断执行成功！')
      
      // 触发父组件刷新数据
      emit('judgeCompleted')
      
      // 延迟重置进度
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
  config.country = ''
  config.riskLevel = ''
  config.entityTypes = []
  config.judgeMode = 'limit'
  config.limit = 10
  progress.value = 0
  progressText.value = ''
}

const loadBlacklist = async () => {
  try {
    const response = await request.get('/device-data/ai-judge/blacklist-keywords')
    // 响应拦截器已返回response.data
    if (response && response.success && response.data) {
      blacklistKeywords.value = response.data
    }
  } catch (error) {
    console.error('加载黑名单失败:', error)
  }
}

const addBlacklist = () => {
  if (newBlacklist.value.trim()) {
    if (!blacklistKeywords.value.includes(newBlacklist.value.trim())) {
      blacklistKeywords.value.push(newBlacklist.value.trim())
      // 立即保存到后端
      saveBlacklistToBackend()
    }
    newBlacklist.value = ''
  }
  showAddBlacklist.value = false
}

const removeBlacklist = (keyword: string) => {
  const index = blacklistKeywords.value.indexOf(keyword)
  if (index > -1) {
    blacklistKeywords.value.splice(index, 1)
    saveBlacklistToBackend()
  }
}

const saveBlacklistToBackend = async () => {
  try {
    // 这里可以调用后端API保存黑名单
    // 暂时只保存在内存中
  } catch (error) {
    console.error('保存黑名单失败:', error)
  }
}

// 组件挂载时加载黑名单
onMounted(() => {
  loadBlacklist()
})

// 暴露方法
defineExpose({
  startJudge,
  loadBlacklist
})
</script>

<style scoped>
.smart-ai-judge {
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

/* 黑名单关键词样式 */
.blacklist-keywords-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.add-keyword-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keywords-list {
  max-height: 300px;
  overflow-y: auto;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
}

.keyword-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.keyword-item:last-child {
  border-bottom: none;
}

.keyword-item:hover {
  background-color: #f5f5f5;
}

.keyword-number {
  color: #999;
  font-size: 12px;
  min-width: 25px;
  margin-right: 8px;
}

.keyword-name {
  flex: 1;
  color: #262626;
  font-weight: 500;
  word-break: break-all;
  margin-right: 8px;
}

.delete-btn {
  opacity: 0.6;
  transition: opacity 0.2s;
}

.delete-btn:hover {
  opacity: 1;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
  background-color: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
}

.empty-text {
  font-size: 14px;
}
</style>

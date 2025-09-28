<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>系统设置</h1>
    </div>

    <a-row :gutter="24">
      <!-- 通知设置 -->
      <a-col :span="12">
        <a-card title="通知设置" style="margin-bottom: 24px;">
          <a-form layout="vertical">
            <a-form-item label="Slack通知">
              <a-switch v-model:checked="settings.slack.enabled" />
            </a-form-item>
            <a-form-item label="Slack Webhook URL" v-if="settings.slack.enabled">
              <a-input v-model:value="settings.slack.webhookUrl" placeholder="https://hooks.slack.com/..." />
            </a-form-item>
            
            <a-divider />
            
            <a-form-item label="邮件通知">
              <a-switch v-model:checked="settings.email.enabled" />
            </a-form-item>
            <template v-if="settings.email.enabled">
              <a-form-item label="SMTP服务器">
                <a-input v-model:value="settings.email.smtpHost" placeholder="smtp.gmail.com" />
              </a-form-item>
              <a-form-item label="SMTP端口">
                <a-input-number v-model:value="settings.email.smtpPort" :min="1" :max="65535" />
              </a-form-item>
              <a-form-item label="用户名">
                <a-input v-model:value="settings.email.username" />
              </a-form-item>
              <a-form-item label="密码">
                <a-input-password v-model:value="settings.email.password" />
              </a-form-item>
              <a-form-item label="发件人">
                <a-input v-model:value="settings.email.from" />
              </a-form-item>
              <a-form-item label="收件人">
                <a-select
                  v-model:value="settings.email.to"
                  mode="tags"
                  placeholder="输入邮箱地址"
                />
              </a-form-item>
            </template>
          </a-form>
        </a-card>
      </a-col>

      <!-- 风险阈值设置 -->
      <a-col :span="12">
        <a-card title="风险阈值设置" style="margin-bottom: 24px;">
          <a-form layout="vertical">
            <a-form-item label="高风险阈值">
              <a-slider
                v-model:value="settings.thresholds.highRisk"
                :min="0"
                :max="1"
                :step="0.1"
                :marks="{ 0: '0', 0.5: '0.5', 1: '1' }"
              />
              <span>当前值: {{ settings.thresholds.highRisk }}</span>
            </a-form-item>
            <a-form-item label="中风险阈值">
              <a-slider
                v-model:value="settings.thresholds.mediumRisk"
                :min="0"
                :max="1"
                :step="0.1"
                :marks="{ 0: '0', 0.5: '0.5', 1: '1' }"
              />
              <span>当前值: {{ settings.thresholds.mediumRisk }}</span>
            </a-form-item>
          </a-form>
        </a-card>

        <!-- 数据更新设置 -->
        <a-card title="数据更新设置">
          <a-form layout="vertical">
            <a-form-item label="认证新闻数据更新频率">
              <a-select v-model:value="settings.certNewsUpdateFrequency">
                <a-select-option value="realtime">实时更新</a-select-option>
                <a-select-option value="5min">每5分钟</a-select-option>
                <a-select-option value="15min">每15分钟</a-select-option>
                <a-select-option value="30min">每30分钟</a-select-option>
                <a-select-option value="hourly">每小时</a-select-option>
                <a-select-option value="daily">每天</a-select-option>
              </a-select>
              <div class="frequency-description">
                <a-tag color="blue">当前: {{ getFrequencyDescription(settings.certNewsUpdateFrequency) }}</a-tag>
              </div>
            </a-form-item>
            
            <a-form-item label="医疗认证风险数据更新频率">
              <a-select v-model:value="settings.medicalRiskUpdateFrequency">
                <a-select-option value="realtime">实时更新</a-select-option>
                <a-select-option value="5min">每5分钟</a-select-option>
                <a-select-option value="15min">每15分钟</a-select-option>
                <a-select-option value="30min">每30分钟</a-select-option>
                <a-select-option value="hourly">每小时</a-select-option>
                <a-select-option value="daily">每天</a-select-option>
              </a-select>
              <div class="frequency-description">
                <a-tag color="green">当前: {{ getFrequencyDescription(settings.medicalRiskUpdateFrequency) }}</a-tag>
              </div>
            </a-form-item>
            
            <a-divider />
            
            <a-form-item label="通用数据更新频率">
              <a-select v-model:value="settings.updateFrequency">
                <a-select-option value="hourly">每小时</a-select-option>
                <a-select-option value="daily">每天</a-select-option>
                <a-select-option value="weekly">每周</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="缓存时间">
              <a-input-number
                v-model:value="settings.cacheTtl"
                :min="1"
                :max="24"
                addon-after="小时"
              />
            </a-form-item>
            
            <a-divider />
            
            <!-- 更新状态显示 -->
            <a-form-item label="当前更新状态">
              <a-space direction="vertical" style="width: 100%;">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-statistic 
                      title="认证新闻数据" 
                      :value="lastUpdateTime.certNews" 
                      :value-style="{ fontSize: '14px' }"
                    />
                  </a-col>
                  <a-col :span="12">
                    <a-statistic 
                      title="医疗风险数据" 
                      :value="lastUpdateTime.medicalRisk" 
                      :value-style="{ fontSize: '14px' }"
                    />
                  </a-col>
                </a-row>
                <a-space>
                  <a-button size="small" @click="triggerCertNewsUpdate" :loading="updating.certNews">
                    手动更新认证新闻
                  </a-button>
                  <a-button size="small" @click="triggerMedicalRiskUpdate" :loading="updating.medicalRisk">
                    手动更新风险数据
                  </a-button>
                </a-space>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <!-- 操作按钮 -->
    <div class="actions">
      <a-space>
        <a-button type="primary" @click="saveSettings">保存设置</a-button>
        <a-button @click="resetSettings">重置</a-button>
        <a-button @click="testNotification">测试通知</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

const settings = reactive({
  slack: {
    enabled: false,
    webhookUrl: ''
  },
  email: {
    enabled: false,
    smtpHost: '',
    smtpPort: 587,
    username: '',
    password: '',
    from: '',
    to: []
  },
  thresholds: {
    highRisk: 0.7,
    mediumRisk: 0.4
  },
  // 新增的更新频率设置
  certNewsUpdateFrequency: 'hourly',
  medicalRiskUpdateFrequency: '30min',
  updateFrequency: 'daily',
  cacheTtl: 6
})

// 更新状态相关数据
const lastUpdateTime = reactive({
  certNews: '从未更新',
  medicalRisk: '从未更新'
})

const updating = reactive({
  certNews: false,
  medicalRisk: false
})

// 获取频率描述
const getFrequencyDescription = (frequency: string) => {
  const descriptions: Record<string, string> = {
    'realtime': '实时更新',
    '5min': '每5分钟',
    '15min': '每15分钟',
    '30min': '每30分钟',
    'hourly': '每小时',
    'daily': '每天',
    'weekly': '每周'
  }
  return descriptions[frequency] || frequency
}

const saveSettings = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.success('设置保存成功')
  } catch (error) {
    message.error('设置保存失败')
  }
}

const resetSettings = () => {
  // 重置为默认值
  Object.assign(settings, {
    slack: { enabled: false, webhookUrl: '' },
    email: { enabled: false, smtpHost: '', smtpPort: 587, username: '', password: '', from: '', to: [] },
    thresholds: { highRisk: 0.7, mediumRisk: 0.4 },
    certNewsUpdateFrequency: 'hourly',
    medicalRiskUpdateFrequency: '30min',
    updateFrequency: 'daily',
    cacheTtl: 6
  })
  message.success('设置已重置')
}

const testNotification = async () => {
  try {
    message.loading('正在发送测试通知...', 0)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    message.destroy()
    message.success('测试通知发送成功')
  } catch (error) {
    message.destroy()
    message.error('测试通知发送失败')
  }
}

// 手动触发认证新闻数据更新
const triggerCertNewsUpdate = async () => {
  updating.certNews = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    lastUpdateTime.certNews = new Date().toLocaleString()
    message.success('认证新闻数据更新成功')
  } catch (error) {
    message.error('认证新闻数据更新失败')
  } finally {
    updating.certNews = false
  }
}

// 手动触发医疗风险数据更新
const triggerMedicalRiskUpdate = async () => {
  updating.medicalRisk = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    lastUpdateTime.medicalRisk = new Date().toLocaleString()
    message.success('医疗风险数据更新成功')
  } catch (error) {
    message.error('医疗风险数据更新失败')
  } finally {
    updating.medicalRisk = false
  }
}

// 组件挂载时获取当前更新状态
onMounted(() => {
  // 模拟获取当前更新状态
  lastUpdateTime.certNews = '2024-12-02 14:30:25'
  lastUpdateTime.medicalRisk = '2024-12-02 14:25:10'
})
</script>

<style scoped>
.settings-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.actions {
  margin-top: 24px;
  text-align: center;
}

.frequency-description {
  margin-top: 8px;
}

.frequency-description .ant-tag {
  margin: 0;
}

/* 更新状态区域样式 */
.ant-statistic-title {
  font-size: 12px !important;
  color: #666;
}

.ant-statistic-content {
  font-size: 14px !important;
  color: #1890ff;
}

/* 手动更新按钮样式 */
.ant-btn-sm {
  height: 28px;
  padding: 0 12px;
  font-size: 12px;
}
</style>

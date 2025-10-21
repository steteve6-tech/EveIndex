<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>系统设置</h1>
    </div>

    <a-row :gutter="24">
      <!-- 通知设置 -->
      <!-- <a-col :span="12">
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
      </a-col> -->

      <!-- 风险阈值设置 -->
      <!-- <a-col :span="12">
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
      </a-col> -->

      <!-- 定时爬取设置 -->
      <a-col :span="24">
        <a-card title="定时爬取设置">
          <a-tabs v-model:activeKey="activeTab" type="card">
            <!-- 系统配置 -->
            <a-tab-pane key="systemconfig" tab="系统配置">
              <div class="system-config-section">
                <a-row :gutter="16">
                  <a-col :span="12">
                    <a-card title="AI配置" size="small" style="margin-bottom: 16px;">
                      <a-form layout="vertical" size="small">
                        <a-form-item
                          v-for="config in aiConfigs"
                          :key="config.configKey"
                          :label="config.description"
                        >
                          <a-input
                            v-if="config.valueType === 'STRING'"
                            v-model:value="config.configValue"
                            :type="config.configKey.includes('key') ? 'password' : 'text'"
                            @blur="saveConfigValue(config)"
                          />
                          <a-input-number
                            v-else-if="config.valueType === 'INTEGER'"
                            v-model:value="config.configValue"
                            style="width: 100%"
                            @blur="saveConfigValue(config)"
                          />
                          <a-input-number
                            v-else-if="config.valueType === 'DOUBLE'"
                            v-model:value="config.configValue"
                            :step="0.1"
                            style="width: 100%"
                            @blur="saveConfigValue(config)"
                          />
                          <a-switch
                            v-else-if="config.valueType === 'BOOLEAN'"
                            v-model:checked="config.configValue"
                            @change="saveConfigValue(config)"
                          />
                        </a-form-item>
                      </a-form>
                    </a-card>
                  </a-col>

                  <a-col :span="12">
                    <a-card title="爬虫配置" size="small" style="margin-bottom: 16px;">
                      <a-form layout="vertical" size="small">
                        <a-form-item
                          v-for="config in crawlerConfigs"
                          :key="config.configKey"
                          :label="config.description"
                        >
                          <a-input-number
                            v-if="config.valueType === 'INTEGER'"
                            v-model:value="config.configValue"
                            style="width: 100%"
                            :min="0"
                            @blur="saveConfigValue(config)"
                          />
                          <a-input-number
                            v-else-if="config.valueType === 'DOUBLE'"
                            v-model:value="config.configValue"
                            :step="0.1"
                            style="width: 100%"
                            @blur="saveConfigValue(config)"
                          />
                          <a-switch
                            v-else-if="config.valueType === 'BOOLEAN'"
                            v-model:checked="config.configValue"
                            @change="saveConfigValue(config)"
                          />
                          <a-input
                            v-else
                            v-model:value="config.configValue"
                            @blur="saveConfigValue(config)"
                          />
                        </a-form-item>
                      </a-form>
                    </a-card>
                  </a-col>
                </a-row>

                <div class="config-actions">
                  <a-space>
                    <a-button type="primary" @click="batchSaveConfigs">
                      批量保存所有配置
                    </a-button>
                    <a-button @click="loadSystemConfigs">
                      重新加载配置
                    </a-button>
                    <a-button @click="initDefaults" danger>
                      恢复默认配置
                    </a-button>
                  </a-space>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- 操作按钮 -->
    <!-- <div class="actions">
      <a-space>
        <a-button type="primary" @click="saveSettings">保存设置</a-button>
        <a-button @click="resetSettings">重置</a-button>
        <a-button @click="testNotification">测试通知</a-button>
      </a-space>
    </div> -->
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  getAllConfigsGrouped,
  saveConfig,
  batchSaveConfigs as batchSaveConfigsAPI,
  initDefaultConfigs,
  type SystemConfig
} from '@/api/systemConfig'

// const settings = reactive({
//   slack: {
//     enabled: false,
//     webhookUrl: ''
//   },
//   email: {
//     enabled: false,
//     smtpHost: '',
//     smtpPort: 587,
//     username: '',
//     password: '',
//     from: '',
//     to: []
//   },
//   thresholds: {
//     highRisk: 0.7,
//     mediumRisk: 0.4
//   },
//   // 新增的更新频率设置
//   certNewsUpdateFrequency: 'hourly',
//   medicalRiskUpdateFrequency: '30min',
//   updateFrequency: 'daily',
//   cacheTtl: 6
// })

// 定时爬取相关数据
const activeTab = ref('systemconfig')

// 系统配置数据
const aiConfigs = ref<SystemConfig[]>([])
const crawlerConfigs = ref<SystemConfig[]>([])
const systemConfigLoading = ref(false)

// 获取频率描述

// const saveSettings = async () => {
//   try {
//     // 模拟API调用
//     await new Promise(resolve => setTimeout(resolve, 1000))
//     message.success('设置保存成功')
//   } catch (error) {
//     message.error('设置保存失败')
//   }
// }

// const resetSettings = () => {
//   // 重置为默认值
//   Object.assign(settings, {
//     slack: { enabled: false, webhookUrl: '' },
//     email: { enabled: false, smtpHost: '', smtpPort: 587, username: '', password: '', from: '', to: [] },
//     thresholds: { highRisk: 0.7, mediumRisk: 0.4 },
//     certNewsUpdateFrequency: 'hourly',
//     medicalRiskUpdateFrequency: '30min',
//     updateFrequency: 'daily',
//     cacheTtl: 6
//   })
//   message.success('设置已重置')
// }

// const testNotification = async () => {
//   try {
//     message.loading('正在发送测试通知...', 0)
//     // 模拟API调用
//     await new Promise(resolve => setTimeout(resolve, 2000))
//     message.destroy()
//     message.success('测试通知发送成功')
//   } catch (error) {
//     message.destroy()
//     message.error('测试通知发送失败')
//   }
// }


// 系统配置相关方法
const loadSystemConfigs = async () => {
  try {
    systemConfigLoading.value = true
    const grouped = await getAllConfigsGrouped()

    // 分离AI配置和爬虫配置
    aiConfigs.value = (grouped['AI_CONFIG'] || []).map(config => ({
      ...config,
      configValue: parseConfigValue(config)
    }))

    crawlerConfigs.value = (grouped['CRAWLER_CONFIG'] || []).map(config => ({
      ...config,
      configValue: parseConfigValue(config)
    }))

    message.success('系统配置加载成功')
  } catch (error: any) {
    message.error('加载系统配置失败: ' + error.message)
  } finally {
    systemConfigLoading.value = false
  }
}

const parseConfigValue = (config: SystemConfig) => {
  if (config.valueType === 'INTEGER') {
    return parseInt(config.configValue || '0')
  } else if (config.valueType === 'DOUBLE') {
    return parseFloat(config.configValue || '0')
  } else if (config.valueType === 'BOOLEAN') {
    return config.configValue === 'true'
  }
  return config.configValue
}

const saveConfigValue = async (config: SystemConfig) => {
  try {
    // 转换值为字符串
    const configToSave = {
      ...config,
      configValue: String(config.configValue)
    }

    await saveConfig(configToSave)
    message.success(`${config.description} 保存成功`)
  } catch (error: any) {
    message.error(`保存失败: ${error.message}`)
  }
}

const batchSaveConfigs = async () => {
  try {
    const allConfigs = [
      ...aiConfigs.value.map(c => ({ ...c, configValue: String(c.configValue) })),
      ...crawlerConfigs.value.map(c => ({ ...c, configValue: String(c.configValue) }))
    ]

    await batchSaveConfigsAPI(allConfigs)
    message.success('批量保存成功')
  } catch (error: any) {
    message.error(`批量保存失败: ${error.message}`)
  }
}

const initDefaults = async () => {
  try {
    await initDefaultConfigs()
    message.success('默认配置初始化成功')
    await loadSystemConfigs()
  } catch (error: any) {
    message.error(`初始化失败: ${error.message}`)
  }
}

// 组件挂载时加载系统配置
onMounted(async () => {
  await loadSystemConfigs()
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

/* 系统配置区域样式 */
.system-config-section {
  padding: 16px;
}

.config-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}

.system-config-section .ant-card {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.system-config-section .ant-card-head-title {
  font-weight: 600;
  color: #1890ff;
}

.system-config-section .ant-form-item-label > label {
  font-weight: 500;
  font-size: 13px;
}
</style>

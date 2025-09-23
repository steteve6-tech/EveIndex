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
            <a-form-item label="自动更新频率">
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
import { reactive } from 'vue'
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
  updateFrequency: 'daily',
  cacheTtl: 6
})

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
</style>

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
            <!-- CertNewsData模块设置 -->
            <a-tab-pane key="certnewsdata" tab="认证新闻数据">
              <div class="crawler-config-section">
                <h4>认证新闻模块爬虫配置</h4>
                <a-table 
                  :columns="certNewsDataColumns" 
                  :data-source="certNewsDataConfigs"
                  :pagination="false"
                  size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'enabled'">
                      <a-switch 
                        v-model:checked="record.enabled" 
                        @change="toggleCrawlerConfig(record)"
                        :loading="record.updating"
                      />
                    </template>
                    <template v-else-if="column.key === 'cronExpression'">
                      <a-space>
                        <a-select 
                          v-model:value="record.cronExpression"
                          @change="updateCrawlerConfig(record)"
                          style="width: 200px"
                          placeholder="选择执行时间"
                        >
                          <a-select-option value="0 0 1 * * ?">每天凌晨1点</a-select-option>
                          <a-select-option value="0 0 2 * * ?">每天凌晨2点</a-select-option>
                          <a-select-option value="0 30 2 * * ?">每天凌晨2点30分</a-select-option>
                          <a-select-option value="0 0 3 * * ?">每天凌晨3点</a-select-option>
                          <a-select-option value="0 30 3 * * ?">每天凌晨3点30分</a-select-option>
                          <a-select-option value="0 0 4 * * ?">每天凌晨4点</a-select-option>
                          <a-select-option value="0 30 4 * * ?">每天凌晨4点30分</a-select-option>
                          <a-select-option value="0 0 5 * * ?">每天凌晨5点</a-select-option>
                          <a-select-option value="0 30 5 * * ?">每天凌晨5点30分</a-select-option>
                          <a-select-option value="0 0 6 * * ?">每天凌晨6点</a-select-option>
                          <a-select-option value="0 30 6 * * ?">每天凌晨6点30分</a-select-option>
                          <a-select-option value="0 0 7 * * ?">每天凌晨7点</a-select-option>
                          <a-select-option value="0 30 7 * * ?">每天凌晨7点30分</a-select-option>
                          <a-select-option value="0 0 8 * * ?">每天凌晨8点</a-select-option>
                          <a-select-option value="0 30 8 * * ?">每天凌晨8点30分</a-select-option>
                          <a-select-option value="0 0 9 * * ?">每天凌晨9点</a-select-option>
                          <a-select-option value="0 30 9 * * ?">每天凌晨9点30分</a-select-option>
                          <a-select-option value="0 0 12 * * ?">每天中午12点</a-select-option>
                          <a-select-option value="0 0 18 * * ?">每天下午6点</a-select-option>
                          <a-select-option value="0 0 22 * * ?">每天晚上10点</a-select-option>
                          <a-select-option value="0 0 * * * ?">每小时整点</a-select-option>
                          <a-select-option value="0 30 * * * ?">每小时30分</a-select-option>
                          <a-select-option value="0 */30 * * * ?">每30分钟</a-select-option>
                          <a-select-option value="0 0 2 ? * MON">每周一凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * TUE">每周二凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * WED">每周三凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * THU">每周四凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * FRI">每周五凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * SAT">每周六凌晨2点</a-select-option>
                          <a-select-option value="0 0 2 ? * SUN">每周日凌晨2点</a-select-option>
              </a-select>
                        <a-button 
                          size="small" 
                          @click="showCustomCronModal(record)"
                          type="dashed"
                        >
                          自定义
                        </a-button>
                      </a-space>
                    </template>
                    <template v-else-if="column.key === 'lastExecutionStatus'">
                      <a-tag :color="getStatusColor(record.lastExecutionStatus)">
                        {{ getStatusText(record.lastExecutionStatus) }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'actions'">
                      <a-space>
                        <a-button 
                          size="small" 
                          type="primary" 
                          @click="triggerCrawler(record)"
                          :loading="record.triggering"
                        >
                          手动触发
                        </a-button>
                        <a-button 
                          size="small" 
                          @click="showCrawlerParamsModal(record)"
                        >
                          参数
                        </a-button>
<!--                        <a-button -->
<!--                          size="small" -->
<!--                          @click="viewCrawlerDetails(record)"-->
<!--                        >-->
<!--                          详情-->
<!--                        </a-button>-->
                      </a-space>
                    </template>
                  </template>
                </a-table>
              </div>
            </a-tab-pane>

            <!-- 设备数据模块设置 -->
            <a-tab-pane key="devicedata" tab="医疗认证数据">
              <div class="crawler-config-section">
                <h4>医疗认证模块爬虫配置</h4>
                <a-tabs v-model:activeKey="deviceDataActiveTab" size="small">
                  <!-- 美国设备数据 -->
                  <a-tab-pane key="us" tab="美国">
                    <a-table 
                      :columns="deviceDataColumns" 
                      :data-source="usDeviceDataConfigs"
                      :pagination="false"
                      size="small"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'enabled'">
                          <a-switch 
                            v-model:checked="record.enabled" 
                            @change="toggleCrawlerConfig(record)"
                            :loading="record.updating"
                          />
                        </template>
                        <template v-else-if="column.key === 'cronExpression'">
                          <a-space>
                            <a-select 
                              v-model:value="record.cronExpression"
                              @change="updateCrawlerConfig(record)"
                              style="width: 200px"
                              placeholder="选择执行时间"
                            >
                              <a-select-option value="0 0 1 * * ?">每天凌晨1点</a-select-option>
                              <a-select-option value="0 0 2 * * ?">每天凌晨2点</a-select-option>
                              <a-select-option value="0 30 2 * * ?">每天凌晨2点30分</a-select-option>
                              <a-select-option value="0 0 3 * * ?">每天凌晨3点</a-select-option>
                              <a-select-option value="0 30 3 * * ?">每天凌晨3点30分</a-select-option>
                              <a-select-option value="0 0 4 * * ?">每天凌晨4点</a-select-option>
                              <a-select-option value="0 30 4 * * ?">每天凌晨4点30分</a-select-option>
                              <a-select-option value="0 0 5 * * ?">每天凌晨5点</a-select-option>
                              <a-select-option value="0 30 5 * * ?">每天凌晨5点30分</a-select-option>
                              <a-select-option value="0 0 6 * * ?">每天凌晨6点</a-select-option>
                              <a-select-option value="0 30 6 * * ?">每天凌晨6点30分</a-select-option>
                              <a-select-option value="0 0 7 * * ?">每天凌晨7点</a-select-option>
                              <a-select-option value="0 30 7 * * ?">每天凌晨7点30分</a-select-option>
                              <a-select-option value="0 0 8 * * ?">每天凌晨8点</a-select-option>
                              <a-select-option value="0 30 8 * * ?">每天凌晨8点30分</a-select-option>
                              <a-select-option value="0 0 9 * * ?">每天凌晨9点</a-select-option>
                              <a-select-option value="0 30 9 * * ?">每天凌晨9点30分</a-select-option>
                              <a-select-option value="0 0 12 * * ?">每天中午12点</a-select-option>
                              <a-select-option value="0 0 18 * * ?">每天下午6点</a-select-option>
                              <a-select-option value="0 0 22 * * ?">每天晚上10点</a-select-option>
                              <a-select-option value="0 0 * * * ?">每小时整点</a-select-option>
                              <a-select-option value="0 30 * * * ?">每小时30分</a-select-option>
                              <a-select-option value="0 */30 * * * ?">每30分钟</a-select-option>
                              <a-select-option value="0 0 2 ? * MON">每周一凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * TUE">每周二凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * WED">每周三凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * THU">每周四凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * FRI">每周五凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * SAT">每周六凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * SUN">每周日凌晨2点</a-select-option>
              </a-select>
                            <a-button 
                              size="small" 
                              @click="showCustomCronModal(record)"
                              type="dashed"
                            >
                              自定义
                            </a-button>
                          </a-space>
                        </template>
                        <template v-else-if="column.key === 'lastExecutionStatus'">
                          <a-tag :color="getStatusColor(record.lastExecutionStatus)">
                            {{ getStatusText(record.lastExecutionStatus) }}
                          </a-tag>
                        </template>
                        <template v-else-if="column.key === 'actions'">
                          <a-space>
                            <a-button 
                              size="small" 
                              type="primary" 
                              @click="triggerDeviceCrawler(record)"
                              :loading="record.triggering"
                            >
                              手动触发
                            </a-button>
                            <a-button 
                              size="small" 
                              @click="showCrawlerParamsModal(record)"
                            >
                              参数
                            </a-button>
<!--                            <a-button -->
<!--                              size="small" -->
<!--                              @click="viewCrawlerDetails(record)"-->
<!--                            >-->
<!--                              详情-->
<!--                            </a-button>-->
                          </a-space>
                        </template>
                      </template>
                    </a-table>
                  </a-tab-pane>

                  <!-- 欧盟设备数据 -->
                  <a-tab-pane key="eu" tab="欧盟">
                    <a-table 
                      :columns="deviceDataColumns" 
                      :data-source="euDeviceDataConfigs"
                      :pagination="false"
                      size="small"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'enabled'">
                          <a-switch 
                            v-model:checked="record.enabled" 
                            @change="toggleCrawlerConfig(record)"
                            :loading="record.updating"
                          />
                        </template>
                        <template v-else-if="column.key === 'cronExpression'">
                          <a-space>
                            <a-select 
                              v-model:value="record.cronExpression"
                              @change="updateCrawlerConfig(record)"
                              style="width: 200px"
                              placeholder="选择执行时间"
                            >
                              <a-select-option value="0 0 1 * * ?">每天凌晨1点</a-select-option>
                              <a-select-option value="0 0 2 * * ?">每天凌晨2点</a-select-option>
                              <a-select-option value="0 30 2 * * ?">每天凌晨2点30分</a-select-option>
                              <a-select-option value="0 0 3 * * ?">每天凌晨3点</a-select-option>
                              <a-select-option value="0 30 3 * * ?">每天凌晨3点30分</a-select-option>
                              <a-select-option value="0 0 4 * * ?">每天凌晨4点</a-select-option>
                              <a-select-option value="0 30 4 * * ?">每天凌晨4点30分</a-select-option>
                              <a-select-option value="0 0 5 * * ?">每天凌晨5点</a-select-option>
                              <a-select-option value="0 30 5 * * ?">每天凌晨5点30分</a-select-option>
                              <a-select-option value="0 0 6 * * ?">每天凌晨6点</a-select-option>
                              <a-select-option value="0 30 6 * * ?">每天凌晨6点30分</a-select-option>
                              <a-select-option value="0 0 7 * * ?">每天凌晨7点</a-select-option>
                              <a-select-option value="0 30 7 * * ?">每天凌晨7点30分</a-select-option>
                              <a-select-option value="0 0 8 * * ?">每天凌晨8点</a-select-option>
                              <a-select-option value="0 30 8 * * ?">每天凌晨8点30分</a-select-option>
                              <a-select-option value="0 0 9 * * ?">每天凌晨9点</a-select-option>
                              <a-select-option value="0 30 9 * * ?">每天凌晨9点30分</a-select-option>
                              <a-select-option value="0 0 12 * * ?">每天中午12点</a-select-option>
                              <a-select-option value="0 0 18 * * ?">每天下午6点</a-select-option>
                              <a-select-option value="0 0 22 * * ?">每天晚上10点</a-select-option>
                              <a-select-option value="0 0 * * * ?">每小时整点</a-select-option>
                              <a-select-option value="0 30 * * * ?">每小时30分</a-select-option>
                              <a-select-option value="0 */30 * * * ?">每30分钟</a-select-option>
                              <a-select-option value="0 0 2 ? * MON">每周一凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * TUE">每周二凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * WED">每周三凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * THU">每周四凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * FRI">每周五凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * SAT">每周六凌晨2点</a-select-option>
                              <a-select-option value="0 0 2 ? * SUN">每周日凌晨2点</a-select-option>
              </a-select>
                            <a-button 
                              size="small" 
                              @click="showCustomCronModal(record)"
                              type="dashed"
                            >
                              自定义
                            </a-button>
                          </a-space>
                        </template>
                        <template v-else-if="column.key === 'lastExecutionStatus'">
                          <a-tag :color="getStatusColor(record.lastExecutionStatus)">
                            {{ getStatusText(record.lastExecutionStatus) }}
                          </a-tag>
                        </template>
                        <template v-else-if="column.key === 'actions'">
                          <a-space>
                            <a-button 
                              size="small" 
                              type="primary" 
                              @click="triggerDeviceCrawler(record)"
                              :loading="record.triggering"
                            >
                              手动触发
                            </a-button>
                            <a-button 
                              size="small" 
                              @click="showCrawlerParamsModal(record)"
                            >
                              参数
                            </a-button>
                            <a-button 
                              size="small" 
                              @click="viewCrawlerDetails(record)"
                            >
                              详情
                            </a-button>
                          </a-space>
                        </template>
                      </template>
                    </a-table>
                  </a-tab-pane>
                </a-tabs>
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

  <!-- 自定义Cron表达式模态框 -->
  <a-modal
    v-model:open="customCronModal.visible"
    title="自定义执行时间"
    @ok="saveCustomCron"
    @cancel="cancelCustomCron"
    width="600px"
  >
    <div class="custom-cron-modal">
      <a-form layout="vertical">
        <a-form-item label="爬虫名称">
          <a-input :value="customCronModal.record?.crawlerName || ''" disabled />
        </a-form-item>
        
        <a-form-item label="执行频率">
          <a-radio-group v-model:value="customCronModal.frequency" @change="onFrequencyChange">
            <a-radio value="daily">每天</a-radio>
            <a-radio value="weekly">每周</a-radio>
            <a-radio value="monthly">每月</a-radio>
            <a-radio value="hourly">每小时</a-radio>
            <a-radio value="custom">自定义</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 每天执行 -->
        <template v-if="customCronModal.frequency === 'daily'">
          <a-form-item label="执行时间">
            <a-time-picker
              v-model:value="customCronModal.time"
              format="HH:mm"
              style="width: 200px"
            />
          </a-form-item>
        </template>

        <!-- 每周执行 -->
        <template v-if="customCronModal.frequency === 'weekly'">
          <a-form-item label="星期">
            <a-select v-model:value="customCronModal.dayOfWeek" style="width: 200px">
              <a-select-option value="MON">星期一</a-select-option>
              <a-select-option value="TUE">星期二</a-select-option>
              <a-select-option value="WED">星期三</a-select-option>
              <a-select-option value="THU">星期四</a-select-option>
              <a-select-option value="FRI">星期五</a-select-option>
              <a-select-option value="SAT">星期六</a-select-option>
              <a-select-option value="SUN">星期日</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="执行时间">
            <a-time-picker
              v-model:value="customCronModal.time"
              format="HH:mm"
              style="width: 200px"
            />
          </a-form-item>
        </template>

        <!-- 每月执行 -->
        <template v-if="customCronModal.frequency === 'monthly'">
          <a-form-item label="日期">
            <a-input-number
              v-model:value="customCronModal.dayOfMonth"
              :min="1"
              :max="31"
              style="width: 200px"
            />
          </a-form-item>
          <a-form-item label="执行时间">
            <a-time-picker
              v-model:value="customCronModal.time"
              format="HH:mm"
              style="width: 200px"
            />
          </a-form-item>
        </template>

        <!-- 每小时执行 -->
        <template v-if="customCronModal.frequency === 'hourly'">
          <a-form-item label="分钟">
            <a-input-number
              v-model:value="customCronModal.minute"
              :min="0"
              :max="59"
              style="width: 200px"
            />
          </a-form-item>
        </template>

        <!-- 自定义Cron表达式 -->
        <template v-if="customCronModal.frequency === 'custom'">
          <a-form-item label="Cron表达式">
            <a-input
              v-model:value="customCronModal.customExpression"
              placeholder="例如: 0 0 2 * * ?"
              style="width: 100%"
            />
            <div class="cron-help">
              <a-typography-text type="secondary">
                格式: 秒 分 时 日 月 星期<br/>
                示例: 0 0 2 * * ? (每天凌晨2点)<br/>
                0 30 2 ? * MON (每周一凌晨2点30分)
              </a-typography-text>
  </div>
          </a-form-item>
        </template>

        <a-form-item label="预览">
          <a-alert
            :message="customCronModal.preview"
            type="info"
            show-icon
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>

  <!-- 爬取参数设置模态框 -->
  <a-modal
    v-model:open="crawlerParamsModal.visible"
    title="爬取参数设置"
    @ok="saveCrawlerParams"
    @cancel="cancelCrawlerParams"
    width="500px"
  >
    <div class="crawler-params-modal">
      <a-form layout="vertical">
        <a-form-item label="爬虫名称">
          <a-input :value="crawlerParamsModal.record?.crawlerName || ''" disabled />
        </a-form-item>
        
        <a-form-item label="批次大小">
          <a-input-number
            v-model:value="crawlerParamsModal.batchSize"
            :min="1"
            :max="1000"
            style="width: 100%"
            placeholder="每次爬取的数据条数"
          />
          <div class="param-help">
            建议值：10-100，数值过大会影响性能
          </div>
        </a-form-item>
        
        <a-form-item label="最大记录数">
          <a-input-number
            v-model:value="crawlerParamsModal.maxRecords"
            :min="1"
            :max="10000"
            style="width: 100%"
            placeholder="单次任务最大爬取记录数"
          />
          <div class="param-help">
            建议值：100-1000，数值过大会增加执行时间
          </div>
        </a-form-item>
        
        <a-form-item label="重试次数">
          <a-input-number
            v-model:value="crawlerParamsModal.retryCount"
            :min="0"
            :max="10"
            style="width: 100%"
            placeholder="失败时的重试次数"
          />
          <div class="param-help">
            建议值：3-5次，避免过度重试
          </div>
        </a-form-item>
        
        <a-form-item label="超时时间（秒）">
          <a-input-number
            v-model:value="crawlerParamsModal.timeout"
            :min="10"
            :max="300"
            style="width: 100%"
            placeholder="请求超时时间"
          />
          <div class="param-help">
            建议值：30-120秒，根据网络情况调整
          </div>
        </a-form-item>
        
        <a-form-item label="延迟时间（秒）">
          <a-input-number
            v-model:value="crawlerParamsModal.delay"
            :min="0"
            :max="60"
            style="width: 100%"
            placeholder="请求间隔时间"
          />
          <div class="param-help">
            建议值：1-5秒，避免对目标网站造成压力
          </div>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  getScheduledCrawlerConfigs,
  updateScheduledCrawlerConfig,
  toggleScheduledCrawlerConfig,
  triggerCertNewsDataCrawler,
  triggerDeviceDataCrawler
} from '@/api/scheduledCrawler'

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

// 更新状态相关数据
const lastUpdateTime = reactive({
  certNews: '从未更新',
  medicalRisk: '从未更新'
})


// 定时爬取相关数据
const activeTab = ref('certnewsdata')
const deviceDataActiveTab = ref('us')
const loading = ref(false)

// 自定义Cron表达式模态框
const customCronModal = reactive({
  visible: false,
  record: null as any,
  frequency: 'daily',
  time: null as any,
  dayOfWeek: 'MON',
  dayOfMonth: 1,
  minute: 0,
  customExpression: '',
  preview: ''
})

// 爬取参数设置模态框
const crawlerParamsModal = reactive({
  visible: false,
  record: null as any,
  batchSize: 50,
  maxRecords: 200,
  retryCount: 3,
  timeout: 30,
  delay: 1
})

// 爬虫配置数据
const allCrawlerConfigs = ref<any[]>([])

// 表格列定义
const certNewsDataColumns = [
  { title: '爬虫名称', dataIndex: 'crawlerName', key: 'crawlerName' },
  { title: '启用状态', dataIndex: 'enabled', key: 'enabled' },
  { title: '执行时间', dataIndex: 'cronExpression', key: 'cronExpression' },
  { title: '最后执行时间', dataIndex: 'lastExecutionTime', key: 'lastExecutionTime' },
  { title: '执行状态', dataIndex: 'lastExecutionStatus', key: 'lastExecutionStatus' },
  { title: '执行次数', dataIndex: 'executionCount', key: 'executionCount' },
  { title: '操作', key: 'actions' }
]

const deviceDataColumns = [
  { title: '爬虫名称', dataIndex: 'crawlerName', key: 'crawlerName' },
  { title: '模块名称', dataIndex: 'moduleName', key: 'moduleName' },
  { title: '启用状态', dataIndex: 'enabled', key: 'enabled' },
  { title: '执行时间', dataIndex: 'cronExpression', key: 'cronExpression' },
  { title: '最后执行时间', dataIndex: 'lastExecutionTime', key: 'lastExecutionTime' },
  { title: '执行状态', dataIndex: 'lastExecutionStatus', key: 'lastExecutionStatus' },
  { title: '执行次数', dataIndex: 'executionCount', key: 'executionCount' },
  { title: '操作', key: 'actions' }
]

// 计算属性
const certNewsDataConfigs = computed(() => {
  return allCrawlerConfigs.value.filter(config => config.moduleName === 'certnewsdata')
})

const usDeviceDataConfigs = computed(() => {
  return allCrawlerConfigs.value.filter(config => 
    config.moduleName !== 'certnewsdata' && config.countryCode === 'US'
  )
})

const euDeviceDataConfigs = computed(() => {
  return allCrawlerConfigs.value.filter(config => 
    config.moduleName !== 'certnewsdata' && config.countryCode === 'EU'
  )
})

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

// 手动触发认证新闻数据更新

// 手动触发医疗风险数据更新

// 定时爬取相关方法
const loadCrawlerConfigs = async () => {
  try {
    loading.value = true
    const response = await getScheduledCrawlerConfigs()
    if (response.success) {
      allCrawlerConfigs.value = response.data || []
    } else {
      message.error('获取爬虫配置失败: ' + response.error)
    }
  } catch (error: any) {
    message.error('获取爬虫配置失败: ' + error.message)
  } finally {
    loading.value = false
  }
}


const toggleCrawlerConfig = async (record: any) => {
  try {
    record.updating = true
    const response = await toggleScheduledCrawlerConfig(record.id, record.enabled)
    if (response.success) {
      message.success(record.enabled ? '爬虫已启用' : '爬虫已禁用')
    } else {
      message.error('操作失败: ' + response.error)
      record.enabled = !record.enabled // 回滚状态
    }
  } catch (error: any) {
    message.error('操作失败: ' + error.message)
    record.enabled = !record.enabled // 回滚状态
  } finally {
    record.updating = false
  }
}

const updateCrawlerConfig = async (record: any) => {
  try {
    const response = await updateScheduledCrawlerConfig(record)
    if (response.success) {
      message.success('配置更新成功')
    } else {
      message.error('配置更新失败: ' + response.error)
    }
  } catch (error: any) {
    message.error('配置更新失败: ' + error.message)
  }
}

const triggerCrawler = async (record: any) => {
  try {
    record.triggering = true
    const response = await triggerCertNewsDataCrawler(record.crawlerName)
    if (response.success) {
      message.success('爬虫触发成功: ' + response.message)
    } else {
      message.error('爬虫触发失败: ' + response.error)
    }
  } catch (error: any) {
    message.error('爬虫触发失败: ' + error.message)
  } finally {
    record.triggering = false
  }
}

const triggerDeviceCrawler = async (record: any) => {
  try {
    record.triggering = true
    const response = await triggerDeviceDataCrawler(
      record.crawlerName, 
      record.countryCode, 
      record.moduleName
    )
    if (response.success) {
      message.success('爬虫触发成功: ' + response.message)
    } else {
      message.error('爬虫触发失败: ' + response.error)
    }
  } catch (error: any) {
    message.error('爬虫触发失败: ' + error.message)
  } finally {
    record.triggering = false
  }
}

const viewCrawlerDetails = (record: any) => {
  // 显示爬虫详情模态框
  message.info('查看爬虫详情: ' + record.crawlerName)
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'SUCCESS': 'green',
    'FAILED': 'red',
    'RUNNING': 'blue',
    'PENDING': 'orange',
    'CANCELLED': 'gray'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'RUNNING': '运行中',
    'PENDING': '等待中',
    'CANCELLED': '已取消'
  }
  return textMap[status] || '未知'
}


// 自定义Cron表达式相关方法
const showCustomCronModal = (record: any) => {
  customCronModal.visible = true
  customCronModal.record = record
  customCronModal.frequency = 'daily'
  customCronModal.time = null
  customCronModal.dayOfWeek = 'MON'
  customCronModal.dayOfMonth = 1
  customCronModal.minute = 0
  customCronModal.customExpression = record.cronExpression || ''
  updateCronPreview()
}

const onFrequencyChange = () => {
  updateCronPreview()
}

const updateCronPreview = () => {
  let preview = ''
  
  switch (customCronModal.frequency) {
    case 'daily':
      if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
        preview = `每天 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} 执行`
      }
      break
      
    case 'weekly':
      if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
        const dayNames: Record<string, string> = {
          'MON': '星期一', 'TUE': '星期二', 'WED': '星期三', 'THU': '星期四',
          'FRI': '星期五', 'SAT': '星期六', 'SUN': '星期日'
        }
        preview = `每周${dayNames[customCronModal.dayOfWeek] || '未知'} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} 执行`
      }
      break
      
    case 'monthly':
      if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
        preview = `每月${customCronModal.dayOfMonth}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} 执行`
      }
      break
      
    case 'hourly':
      preview = `每小时${customCronModal.minute}分执行`
      break
      
    case 'custom':
      preview = `自定义表达式: ${customCronModal.customExpression}`
      break
  }
  
  customCronModal.preview = preview
}

const saveCustomCron = async () => {
  try {
    let cronExpression = ''
    
    switch (customCronModal.frequency) {
      case 'daily':
        if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
          cronExpression = `0 ${minute} ${hour} * * ?`
        }
        break
        
      case 'weekly':
        if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
          cronExpression = `0 ${minute} ${hour} ? * ${customCronModal.dayOfWeek}`
        }
        break
        
      case 'monthly':
        if (customCronModal.time) {
        const hour = customCronModal.time?.hour() || 0
        const minute = customCronModal.time?.minute() || 0
          cronExpression = `0 ${minute} ${hour} ${customCronModal.dayOfMonth} * ?`
        }
        break
        
      case 'hourly':
        cronExpression = `0 ${customCronModal.minute} * * * ?`
        break
        
      case 'custom':
        cronExpression = customCronModal.customExpression
        break
    }
    
    if (!cronExpression) {
      message.error('请设置执行时间')
      return
    }
    
    // 更新记录
    if (customCronModal.record) {
      customCronModal.record.cronExpression = cronExpression
    }
    
    // 保存到后端
    if (customCronModal.record) {
      await updateCrawlerConfig(customCronModal.record)
    }
    
    customCronModal.visible = false
    message.success('执行时间设置成功')
    
  } catch (error: any) {
    message.error('设置失败: ' + error.message)
  }
}

const cancelCustomCron = () => {
  customCronModal.visible = false
}

// 爬取参数设置相关方法
const showCrawlerParamsModal = (record: any) => {
  crawlerParamsModal.visible = true
  crawlerParamsModal.record = record
  
  // 解析现有参数
  try {
    if (record.crawlParams) {
      const params = JSON.parse(record.crawlParams)
      crawlerParamsModal.batchSize = params.batchSize || 50
      crawlerParamsModal.maxRecords = params.maxRecords || 200
      crawlerParamsModal.retryCount = params.retryCount || 3
      crawlerParamsModal.timeout = params.timeout || 30
      crawlerParamsModal.delay = params.delay || 1
    } else {
      // 设置默认值
      crawlerParamsModal.batchSize = 50
      crawlerParamsModal.maxRecords = 200
      crawlerParamsModal.retryCount = 3
      crawlerParamsModal.timeout = 30
      crawlerParamsModal.delay = 1
    }
  } catch (error) {
    console.error('解析爬取参数失败:', error)
    // 使用默认值
    crawlerParamsModal.batchSize = 50
    crawlerParamsModal.maxRecords = 200
    crawlerParamsModal.retryCount = 3
    crawlerParamsModal.timeout = 30
    crawlerParamsModal.delay = 1
  }
}

const saveCrawlerParams = async () => {
  try {
    // 构建参数对象
    const params = {
      batchSize: crawlerParamsModal.batchSize,
      maxRecords: crawlerParamsModal.maxRecords,
      retryCount: crawlerParamsModal.retryCount,
      timeout: crawlerParamsModal.timeout,
      delay: crawlerParamsModal.delay
    }
    
    // 更新记录
    if (crawlerParamsModal.record) {
      crawlerParamsModal.record.crawlParams = JSON.stringify(params)
      
      // 保存到后端
      await updateCrawlerConfig(crawlerParamsModal.record)
    }
    
    crawlerParamsModal.visible = false
    message.success('爬取参数设置成功')
    
  } catch (error: any) {
    message.error('设置失败: ' + error.message)
  }
}

const cancelCrawlerParams = () => {
  crawlerParamsModal.visible = false
}

// 组件挂载时获取当前更新状态
onMounted(async () => {
  // 模拟获取当前更新状态
  lastUpdateTime.certNews = '2024-12-02 14:30:25'
  lastUpdateTime.medicalRisk = '2024-12-02 14:25:10'
  
  // 加载定时爬取配置
  await loadCrawlerConfigs()
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

/* 定时爬取设置样式 */
.crawler-config-section {
  margin-bottom: 16px;
}

.crawler-config-section h4 {
  margin-bottom: 16px;
  color: #1890ff;
  font-weight: 600;
}

.crawler-statistics {
  padding: 16px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 0;
}

.stat-item:last-child {
  margin-bottom: 0;
}

/* 表格样式优化 */
.ant-table-small .ant-table-tbody > tr > td {
  padding: 8px;
}

/* 状态标签样式 */
.ant-tag {
  margin: 0;
}

/* 操作按钮样式 */
.ant-space {
  display: flex;
  align-items: center;
}

/* 自定义Cron表达式模态框样式 */
.custom-cron-modal {
  padding: 16px 0;
}

.cron-help {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
}

/* 时间选择器样式 */
.ant-time-picker {
  width: 100%;
}

/* 选择器组样式 */
.ant-select-option-group-label {
  font-weight: 600;
  color: #1890ff;
  background-color: #f0f8ff;
  padding: 8px 12px;
  margin: 0;
  border-bottom: 1px solid #e8e8e8;
}

.ant-select-option-group {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin-bottom: 8px;
}

.ant-select-option-group .ant-select-option {
  padding-left: 24px;
}

/* 预览区域样式 */
.ant-alert {
  margin-top: 8px;
}

/* 表单样式优化 */
.ant-form-item {
  margin-bottom: 16px;
}

.ant-form-item-label {
  font-weight: 500;
}

/* 单选按钮组样式 */
.ant-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.ant-radio-wrapper {
  margin-right: 0;
}

/* 爬取参数模态框样式 */
.crawler-params-modal {
  padding: 16px 0;
}

.param-help {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

/* 数字输入框样式 */
.ant-input-number {
  width: 100%;
}

/* 模态框标题样式 */
.ant-modal-title {
  font-weight: 600;
  color: #1890ff;
}

/* 表单验证样式 */
.ant-form-item-explain-error {
  font-size: 12px;
  margin-top: 4px;
}

/* 按钮组样式 */
.ant-space .ant-btn {
  margin-right: 8px;
}

.ant-space .ant-btn:last-child {
  margin-right: 0;
}
</style>

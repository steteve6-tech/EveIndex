<template>
  <div class="crawler-management">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
        <h1>🕷️ 美国爬虫管理系统</h1>
        <p>管理美国FDA相关数据爬虫，支持参数化测试和批量操作</p>
        </div>
        <div class="header-actions">
          <a-space>
          <a-button @click="testAllCrawlers" :loading="testAllLoading" type="primary" v-if="activeTab === 'crawlers'">
              <template #icon>
              <BugOutlined />
              </template>
            测试所有爬虫
            </a-button>
          <a-button @click="refreshAllStatus" :loading="refreshLoading" v-if="activeTab === 'crawlers'">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新状态
            </a-button>
          <a-button @click="refreshKeywords" :loading="keywordLoading" type="primary" v-if="activeTab === 'keywords'">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新关键词
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 标签页 -->
      <a-tabs v-model:activeKey="activeTab" class="main-tabs">
        <!-- 爬虫管理标签页 -->
        <a-tab-pane key="crawlers" tab="爬虫管理">
          <template #tab>
            <span>
              <BugOutlined />
              爬虫管理
            </span>
          </template>

    <!-- 美国爬虫管理 -->
    <div class="usa-crawler-section">
      <a-card :title="`🇺🇸 美国爬虫 (${usaCrawlers.length}个)`" :bordered="false" class="country-card">
        <template #extra>
                  <a-space>
            <a-tag color="blue">运行中: {{ usaRunningCount }}</a-tag>
            <a-tag color="green">可用: {{ usaAvailableCount }}</a-tag>
            <a-tag color="red">停止: {{ usaStoppedCount }}</a-tag>
                  </a-space>
                </template>

            <!-- 美国爬虫列表 -->
        <div class="crawler-list">
          <div 
            v-for="crawler in usaCrawlers" 
            :key="crawler.key"
            class="crawler-list-item"
            :class="{ 
              'running': crawler.status === 'running', 
              'selected': selectedCrawlers.includes(crawler.key),
              'testing': crawler.testing,
              'expanded': expandedCrawlers.includes(crawler.key)
            }"
          >
            <!-- 列表项头部 -->
            <div class="crawler-list-header">
              <div class="crawler-icon">
                <div class="icon-wrapper" :class="crawler.key">
                  <BugOutlined />
                </div>
              </div>
              <div class="crawler-info">
                <div class="crawler-name-section">
                <h3 class="crawler-name">{{ crawler.displayName }}</h3>
                <div class="crawler-meta">
                  <a-tag :color="getStatusColor(crawler.status)" class="status-tag">
                    <template #icon>
                      <div class="status-dot" :class="crawler.status"></div>
                    </template>
                    {{ getStatusText(crawler.status) }}
                  </a-tag>
                  <span class="entity-tag">{{ crawler.entity }}</span>
                </div>
              </div>
                <div class="crawler-description">
                  <p>{{ crawler.description }}</p>
                </div>
              </div>
              <div class="crawler-actions">
              <div class="crawler-checkbox">
                <a-checkbox 
                  :checked="selectedCrawlers.includes(crawler.key)"
                  @change="(e: any) => handleCrawlerSelect(crawler.key, e.target.checked)"
                />
              </div>
                <div class="test-actions">
                <a-button
                  type="primary"
                  size="small"
                    @click="showTestInterface(crawler)"
                  :loading="crawler.testing"
                  :disabled="crawler.testing"
                >
                  <template #icon>
                    <BugOutlined />
                  </template>
                    测试
                </a-button>
                </div>
              </div>
            </div>


            <!-- 加载遮罩 -->
            <div v-if="crawler.testing" class="loading-overlay">
              <a-spin size="large" />
              <span class="loading-text">测试中...</span>
            </div>
          </div>
        </div>

        <!-- 批量操作 -->
        <div class="batch-actions" v-if="selectedCrawlers.length > 0">
          <a-alert 
            :message="`已选择 ${selectedCrawlers.length} 个爬虫`"
            type="info"
            show-icon
            style="margin-bottom: 16px"
          />
          <a-space>
            <a-button @click="batchQuickTest" :loading="batchTestLoading">
              <template #icon>
                <ThunderboltOutlined />
              </template>
              批量快速测试
            </a-button>
            <a-button @click="clearSelection">
              清空选择
            </a-button>
          </a-space>
        </div>
    </a-card>
    </div>

    <!-- Knif4j风格测试界面 -->
    <a-modal
      v-model:open="testInterfaceVisible"
      :title="`${selectedCrawler?.displayName || ''} - API测试`"
      width="1200px"
      :footer="null"
      class="knif4j-modal"
    >
      <div v-if="selectedCrawler" class="knif4j-interface">
        <!-- 接口信息 -->
        <div class="api-info-section">
          <a-descriptions :column="2" bordered size="small">
            <a-descriptions-item label="接口名称">
              {{ selectedCrawler.displayName }}
            </a-descriptions-item>
            <a-descriptions-item label="请求方式">
              <a-tag color="blue">POST</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="接口地址">
              <code>{{ selectedCrawler.testEndpoint }}</code>
            </a-descriptions-item>
            <a-descriptions-item label="Content-Type">
              <a-tag color="green">application/json</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- 参数输入区域 -->
        <div class="params-section">
          <h4>请求参数</h4>
          <div class="params-form">
            <!-- D_510K 参数 -->
            <template v-if="selectedCrawler.key === 'd510k'">
              <!-- 关键词来源选择 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词来源">
                    <a-radio-group v-model:value="testParams.keywordSource" @change="(e: any) => console.log('D_510K关键词来源变化:', e.target.value)">
                      <a-radio value="manual">手动输入关键词</a-radio>
                      <a-radio value="list">使用关键词列表</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 手动输入关键词 -->
              <a-row v-if="testParams.keywordSource === 'manual'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="手动输入关键词">
                    <a-textarea
                      v-model:value="testParams.inputKeywords"
                      placeholder="请输入关键词，每行一个，如：&#10;Pacemaker&#10;Medtronic&#10;Cardiac"
                      :rows="3"
                      allow-clear
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 使用关键词列表 -->
              <a-row v-if="testParams.keywordSource === 'list'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词列表">
                    <div style="padding: 12px; background: #f5f5f5; border-radius: 6px;">
                      <div style="margin-bottom: 8px;">
                        <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                        <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading" style="margin-left: 8px;">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                      </div>
                      <div style="max-height: 120px; overflow-y: auto;">
                        <a-tag v-for="option in keywordOptions.slice(0, 10)" :key="option.value" style="margin: 2px;">
                          {{ option.value }}
                        </a-tag>
                        <a-tag v-if="keywordOptions.length > 10" color="default">
                          ... 还有 {{ keywordOptions.length - 10 }} 个关键词
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="设备名称">
                    <a-input
                      v-model:value="testParams.deviceName"
                      placeholder="如：Pacemaker"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="申请人名称">
                    <a-input
                      v-model:value="testParams.applicantName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="决策日期开始">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="决策日期结束">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="最大页数">
                    <a-input-number
                      v-model:value="testParams.maxPages"
                      :min="1"
                      :max="50"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- D_event 参数 -->
            <template v-else-if="selectedCrawler.key === 'devent'">
              <!-- 关键词来源选择 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词来源">
                    <a-radio-group v-model:value="testParams.keywordSource" @change="(e: any) => console.log('D_event关键词来源变化:', e.target.value)">
                      <a-radio value="manual">手动输入关键词</a-radio>
                      <a-radio value="list">使用关键词列表</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 手动输入关键词 -->
              <a-row v-if="testParams.keywordSource === 'manual'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="手动输入关键词">
                    <a-textarea
                      v-model:value="testParams.inputKeywords"
                      placeholder="请输入关键词，每行一个，如：&#10;Medtronic&#10;Pacemaker&#10;Cardiac"
                      :rows="3"
                      allow-clear
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 使用关键词列表 -->
              <a-row v-if="testParams.keywordSource === 'list'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词列表">
                    <div style="padding: 12px; background: #f5f5f5; border-radius: 6px;">
                      <div style="margin-bottom: 8px;">
                        <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                        <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading" style="margin-left: 8px;">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                      </div>
                      <div style="max-height: 120px; overflow-y: auto;">
                        <a-tag v-for="option in keywordOptions.slice(0, 10)" :key="option.value" style="margin: 2px;">
                          {{ option.value }}
                        </a-tag>
                        <a-tag v-if="keywordOptions.length > 10" color="default">
                          ... 还有 {{ keywordOptions.length - 10 }} 个关键词
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="品牌名称">
                    <a-input
                      v-model:value="testParams.brandName"
                      placeholder="如：Medtronic"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="制造商">
                    <a-input
                      v-model:value="testParams.manufacturer"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="型号">
                    <a-input
                      v-model:value="testParams.modelNumber"
                      placeholder="如：Model 123"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="报告接收日期开始">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="报告接收日期结束">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="最大页数">
                    <a-input-number
                      v-model:value="testParams.maxPages"
                      :min="1"
                      :max="50"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- D_recall 参数 -->
            <template v-else-if="selectedCrawler.key === 'drecall'">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="产品名称">
                    <a-input
                      v-model:value="testParams.productName"
                      placeholder="如：Pacemaker"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="召回原因">
                    <a-input
                      v-model:value="testParams.reasonForRecall"
                      placeholder="如：Software Defect"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="召回公司">
                    <a-input
                      v-model:value="testParams.recallingFirm"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="召回日期开始">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="召回日期结束">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="最大页数">
                    <a-input-number
                      v-model:value="testParams.maxPages"
                      :min="1"
                      :max="50"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              
              <!-- 关键词来源选择 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词来源">
                    <a-radio-group v-model:value="testParams.keywordSource" @change="(e: any) => console.log('D_recall关键词来源变化:', e.target.value)">
                      <a-radio value="manual">手动输入关键词</a-radio>
                      <a-radio value="list">使用关键词列表</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>
              
              <!-- 手动输入关键词 -->
              <a-row v-if="testParams.keywordSource === 'manual'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="输入关键词">
                    <a-textarea
                      v-model:value="testParams.inputKeywords"
                      placeholder="请输入关键词，每行一个，如：&#10;Pacemaker&#10;Defibrillator&#10;Stent"
                      :rows="4"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              
              <!-- 使用关键词列表 -->
              <a-row v-if="testParams.keywordSource === 'list'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词列表">
                    <div style="padding: 12px; background: #f5f5f5; border-radius: 6px;">
                      <div style="margin-bottom: 8px;">
                        <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                        <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading" style="margin-left: 8px;">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                      </div>
                      <div style="max-height: 120px; overflow-y: auto;">
                        <a-tag v-for="option in keywordOptions.slice(0, 10)" :key="option.value" style="margin: 2px;">
                          {{ option.value }}
                        </a-tag>
                        <a-tag v-if="keywordOptions.length > 10" color="default">
                          ... 还有 {{ keywordOptions.length - 10 }} 个关键词
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
              
            </template>

            <!-- D_registration 参数 -->
            <template v-else-if="selectedCrawler.key === 'dregistration'">
              <!-- 关键词来源选择 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词来源">
                    <a-radio-group v-model:value="testParams.keywordSource" @change="(e: any) => console.log('D_registration关键词来源变化:', e.target.value)">
                      <a-radio value="manual">手动输入关键词</a-radio>
                      <a-radio value="list">使用关键词列表</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 手动输入关键词 -->
              <a-row v-if="testParams.keywordSource === 'manual'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="手动输入关键词">
                    <a-textarea
                      v-model:value="testParams.inputKeywords"
                      placeholder="请输入关键词，每行一个，如：&#10;Medtronic&#10;Pacemaker&#10;Cardiac"
                      :rows="3"
                      allow-clear
                    />
                  </a-form-item>
                </a-col>
              </a-row>

              <!-- 使用关键词列表 -->
              <a-row v-if="testParams.keywordSource === 'list'" :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词列表">
                    <div style="padding: 12px; background: #f5f5f5; border-radius: 6px;">
                      <div style="margin-bottom: 8px;">
                        <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                        <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading" style="margin-left: 8px;">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                      </div>
                      <div style="max-height: 120px; overflow-y: auto;">
                        <a-tag v-for="option in keywordOptions.slice(0, 10)" :key="option.value" style="margin: 2px;">
                          {{ option.value }}
                        </a-tag>
                        <a-tag v-if="keywordOptions.length > 10" color="default">
                          ... 还有 {{ keywordOptions.length - 10 }} 个关键词
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="机构/贸易名称">
                    <a-input
                      v-model:value="testParams.establishmentName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="专有名称">
                    <a-input
                      v-model:value="testParams.proprietaryName"
                      placeholder="如：Pacemaker"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="所有者/经营者名称">
                    <a-input
                      v-model:value="testParams.ownerOperatorName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="最大页数">
                    <a-input-number
                      v-model:value="testParams.maxPages"
                      :min="1"
                      :max="50"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- unicrawl 参数 -->
            <template v-else-if="selectedCrawler.key === 'unicrawl'">
              <a-row :gutter="16">
                <a-col :span="6">
                  <a-form-item label="总爬取数量">
                    <a-input-number
                      v-model:value="testParams.totalCount"
                      :min="0"
                      :max="1000"
                      placeholder="0表示使用默认值"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="开始日期">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      placeholder="YYYY-MM-DD"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="结束日期">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      placeholder="YYYY-MM-DD"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="最大页数">
                    <a-input-number
                      v-model:value="testParams.maxPages"
                      :min="0"
                      :max="100"
                      placeholder="0表示爬取所有"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词来源">
                    <a-radio-group v-model:value="testParams.keywordSource">
                      <a-radio value="manual">手动输入关键词</a-radio>
                      <a-radio value="list">使用关键词列表</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16" v-if="testParams.keywordSource === 'manual'">
                <a-col :span="24">
                  <a-form-item label="输入关键词">
                    <a-textarea
                      v-model:value="testParams.inputKeywords"
                      placeholder="输入关键词，每行一个，留空则使用文件关键词"
                      :rows="3"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16" v-if="testParams.keywordSource === 'list'">
                <a-col :span="24">
                  <a-form-item label="关键词列表">
                    <div style="padding: 12px; background: #f5f5f5; border-radius: 6px;">
                      <div style="margin-bottom: 8px;">
                        <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                        <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading" style="margin-left: 8px;">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                      </div>
                      <div style="max-height: 120px; overflow-y: auto;">
                        <a-tag v-for="option in keywordOptions.slice(0, 10)" :key="option.value" style="margin: 2px;">
                          {{ option.value }}
                        </a-tag>
                        <a-tag v-if="keywordOptions.length > 10" color="default">
                          ... 还有 {{ keywordOptions.length - 10 }} 个关键词
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- CustomsCaseCrawler 参数 -->
            <template v-else-if="selectedCrawler.key === 'customs-case'">
              <a-row :gutter="16">
                <a-col :span="6">
                  <a-form-item label="HS编码">
                    <a-input
                      v-model:value="testParams.hsCode"
                      placeholder="如：9018"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="最大记录数">
                    <a-input-number
                      v-model:value="testParams.maxRecords"
                      :min="1"
                      :max="1000"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="批次大小">
                    <a-input-number
                      v-model:value="testParams.batchSize"
                      :min="1"
                      :max="100"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="6">
                  <a-form-item label="开始日期">
                    <a-date-picker
                      v-model:value="testParams.startDate"
                      format="MM/DD/YYYY"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- GuidanceCrawler 参数 -->
            <template v-else-if="selectedCrawler.key === 'guidance'">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="最大记录数">
                    <a-input-number
                      v-model:value="testParams.maxRecords"
                      :min="1"
                      :max="1000"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>
          </div>
        </div>

        <!-- 请求体预览 -->
        <div class="request-preview-section">
          <h4>请求体预览</h4>
          <div class="json-preview">
            <pre><code>{{ JSON.stringify(getRequestPayload(), null, 2) }}</code></pre>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <a-space>
            <a-button @click="resetTestParams">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置参数
            </a-button>
            <a-button @click="testInterfaceVisible = false">
              取消
            </a-button>
            <a-button 
              type="primary" 
              @click="executeTest"
              :loading="testExecuting"
            >
              <template #icon>
                <BugOutlined />
              </template>
              发送请求
            </a-button>
          </a-space>
        </div>

        <!-- 响应结果 -->
        <div v-if="testResult" class="response-section">
          <h4>响应结果</h4>
          <div class="response-info">
            <a-descriptions :column="3" bordered size="small">
              <a-descriptions-item label="状态码">
                <a-tag :color="testResult.success ? 'green' : 'red'">
                  {{ testResult.success ? '200' : '500' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="响应时间">
                {{ testResult.responseTime }}ms
              </a-descriptions-item>
              <a-descriptions-item label="数据大小">
                {{ testResult.dataSize }}B
              </a-descriptions-item>
            </a-descriptions>
          </div>
          <div class="response-content">
            <a-tabs v-model:activeKey="responseTabActive">
              <a-tab-pane key="formatted" tab="格式化">
                <div class="json-response">
                  <pre><code>{{ JSON.stringify(testResult.data, null, 2) }}</code></pre>
                </div>
              </a-tab-pane>
              <a-tab-pane key="raw" tab="原始数据">
                <div class="raw-response">
                  <pre>{{ testResult.rawResponse }}</pre>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 参数测试模态框 -->
    <a-modal
      v-model:open="testModalVisible"
      :title="`${selectedCrawler?.displayName || ''} - 参数化测试`"
      width="800px"
      :footer="null"
    >
      <div v-if="selectedCrawler" class="test-modal-content">
            <a-form
          :model="testParams"
              :label-col="{ span: 6 }"
              :wrapper-col="{ span: 18 }"
          class="test-form"
            >
          <!-- D_510K 参数 -->
          <template v-if="selectedCrawler.key === 'd510k'">
            <a-form-item label="设备名称">
              <a-input
                v-model:value="testParams.deviceName"
                placeholder="请输入设备名称，如：Pacemaker"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="申请人名称">
              <a-input
                v-model:value="testParams.applicantName"
                placeholder="请输入申请人名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="决策日期开始">
              <a-date-picker
                v-model:value="testParams.dateFrom"
                format="YYYY-MM-DD"
                placeholder="选择决策日期开始"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="决策日期结束">
              <a-date-picker
                v-model:value="testParams.dateTo"
                format="YYYY-MM-DD"
                placeholder="选择决策日期结束"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="最大页数">
              <a-input-number
                v-model:value="testParams.maxPages"
                :min="1"
                :max="50"
                placeholder="最大爬取页数"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- D_event 参数 -->
          <template v-else-if="selectedCrawler.key === 'devent'">
            <a-form-item label="品牌名称">
              <a-input
                v-model:value="testParams.brandName"
                placeholder="请输入品牌名称，如：Medtronic"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="制造商">
              <a-input
                v-model:value="testParams.manufacturer"
                placeholder="请输入制造商名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="型号">
              <a-input
                v-model:value="testParams.modelNumber"
                placeholder="请输入型号，如：Model 123"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="报告接收日期开始">
              <a-date-picker
                v-model:value="testParams.dateFrom"
                format="YYYY-MM-DD"
                placeholder="选择报告接收日期开始"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="报告接收日期结束">
              <a-date-picker
                v-model:value="testParams.dateTo"
                format="YYYY-MM-DD"
                placeholder="选择报告接收日期结束"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="最大页数">
              <a-input-number
                v-model:value="testParams.maxPages"
                :min="1"
                :max="50"
                placeholder="最大爬取页数"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- D_recall 参数 -->
          <template v-else-if="selectedCrawler.key === 'drecall'">
            <a-form-item label="产品名称">
              <a-input
                v-model:value="testParams.productName"
                placeholder="请输入产品名称，如：Pacemaker"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="召回原因">
              <a-input
                v-model:value="testParams.reasonForRecall"
                placeholder="请输入召回原因，如：Software Defect"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="召回公司">
              <a-input
                v-model:value="testParams.recallingFirm"
                placeholder="请输入召回公司名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="召回日期开始">
              <a-date-picker
                v-model:value="testParams.dateFrom"
                format="YYYY-MM-DD"
                placeholder="选择召回日期开始"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="召回日期结束">
              <a-date-picker
                v-model:value="testParams.dateTo"
                format="YYYY-MM-DD"
                placeholder="选择召回日期结束"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="最大页数">
              <a-input-number
                v-model:value="testParams.maxPages"
                :min="1"
                :max="50"
                placeholder="最大爬取页数"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- D_registration 参数 -->
          <template v-else-if="selectedCrawler.key === 'dregistration'">
            <a-form-item label="机构/贸易名称">
              <a-input
                v-model:value="testParams.establishmentName"
                placeholder="请输入机构或贸易名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="专有名称">
              <a-input
                v-model:value="testParams.proprietaryName"
                placeholder="请输入专有名称，如：Pacemaker"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="所有者/经营者名称">
              <a-input
                v-model:value="testParams.ownerOperatorName"
                placeholder="请输入所有者或经营者名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="最大页数">
              <a-input-number
                v-model:value="testParams.maxPages"
                :min="1"
                :max="50"
                placeholder="最大爬取页数"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- unicrawl 参数 -->
          <template v-else-if="selectedCrawler.key === 'unicrawl'">
            <a-form-item label="总爬取数量">
              <a-input-number
                v-model:value="testParams.totalCount"
                :min="0"
                :max="1000"
                placeholder="0表示使用默认值"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="开始日期">
              <a-date-picker
                v-model:value="testParams.dateFrom"
                placeholder="YYYY-MM-DD"
                style="width: 100%"
                format="YYYY-MM-DD"
              />
            </a-form-item>
            <a-form-item label="结束日期">
              <a-date-picker
                v-model:value="testParams.dateTo"
                placeholder="YYYY-MM-DD"
                style="width: 100%"
                format="YYYY-MM-DD"
              />
            </a-form-item>
            <a-form-item label="最大页数">
              <a-input-number
                v-model:value="testParams.maxPages"
                :min="0"
                :max="100"
                placeholder="0表示爬取所有"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="输入关键词">
              <a-textarea
                v-model:value="testParams.inputKeywords"
                placeholder="输入关键词，每行一个，留空则使用文件关键词"
                :rows="3"
                style="width: 100%"
              />
            </a-form-item>
                    </template>

          <!-- CustomsCaseCrawler 参数 -->
          <template v-else-if="selectedCrawler.key === 'customs-case'">
            <a-form-item label="HS编码">
              <a-input
                v-model:value="testParams.hsCode"
                placeholder="请输入HS编码，如：9018"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="最大记录数">
              <a-input-number
                v-model:value="testParams.maxRecords"
                :min="1"
                :max="1000"
                placeholder="最大爬取记录数"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="批次大小">
              <a-input-number
                v-model:value="testParams.batchSize"
                :min="1"
                :max="100"
                placeholder="批量保存大小"
                style="width: 100%"
              />
            </a-form-item>
            <a-form-item label="开始日期">
              <a-date-picker
                v-model:value="testParams.startDate"
                format="MM/DD/YYYY"
                placeholder="选择开始日期"
                style="width: 100%"
              />
            </a-form-item>
                    </template>

          <!-- GuidanceCrawler 参数 -->
          <template v-else-if="selectedCrawler.key === 'guidance'">
            <a-form-item label="最大记录数">
              <a-input-number
                v-model:value="testParams.maxRecords"
                :min="1"
                :max="1000"
                placeholder="最大爬取记录数"
                style="width: 100%"
              />
            </a-form-item>
                    </template>
        </a-form>

        <div class="test-actions">
                <a-space>
            <a-button @click="resetTestParams">
              重置参数
          </a-button>
            <a-button @click="testModalVisible = false">
              取消
          </a-button>
                  <a-button 
                    type="primary" 
              @click="executeParameterizedTest"
              :loading="testExecuting"
                  >
                    <template #icon>
                <BugOutlined />
                    </template>
              执行测试
                  </a-button>
                </a-space>
              </div>
              </div>
    </a-modal>

    <!-- 测试结果模态框 -->
    <a-modal
      v-model:open="testResultModalVisible"
      title="爬虫测试结果"
      width="1000px"
      :footer="null"
    >
      <div v-if="currentTestResult" class="test-result-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="爬虫名称">
            {{ currentTestResult.crawlerName }}
          </a-descriptions-item>
          <a-descriptions-item label="测试状态">
            <a-tag :color="currentTestResult.success ? 'green' : 'red'">
              {{ currentTestResult.success ? '成功' : '失败' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="保存记录数">
            {{ currentTestResult.totalSaved || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="跳过记录数">
            {{ currentTestResult.totalSkipped || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="总页数">
            {{ currentTestResult.totalPages || 0 }}
          </a-descriptions-item>
          <a-descriptions-item label="测试时间">
            {{ currentTestResult.testTime }}
          </a-descriptions-item>
          <a-descriptions-item label="消息" :span="2">
            {{ currentTestResult.message }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 详细结果 -->
        <div v-if="currentTestResult.details" class="test-details" style="margin-top: 16px;">
          <h4>详细结果</h4>
          <pre class="details-content">{{ JSON.stringify(currentTestResult.details, null, 2) }}</pre>
              </div>
        </div>
    </a-modal>

    <!-- 爬虫详情模态框 -->
    <a-modal
      v-model:open="crawlerDetailModalVisible"
      :title="`${selectedCrawler?.displayName || ''} - 爬虫详情`"
      width="800px"
      :footer="null"
    >
      <div v-if="selectedCrawler" class="crawler-detail-content">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="显示名称">
            {{ selectedCrawler.displayName }}
          </a-descriptions-item>
          <a-descriptions-item label="类名">
            {{ selectedCrawler.className }}
          </a-descriptions-item>
          <a-descriptions-item label="实体">
            {{ selectedCrawler.entity }}
          </a-descriptions-item>
          <a-descriptions-item label="描述">
            {{ selectedCrawler.description }}
          </a-descriptions-item>
          <a-descriptions-item label="测试端点">
            {{ selectedCrawler.testEndpoint }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedCrawler.status)">
              {{ getStatusText(selectedCrawler.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="最后测试">
            {{ selectedCrawler.lastTest || '未测试' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 添加关键词模态框 -->
    <a-modal
      v-model:open="addKeywordModalVisible"
      title="添加关键词"
      @ok="handleAddKeyword"
      :confirm-loading="addKeywordLoading"
    >
      <a-form :model="newKeyword" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="关键词" required>
          <a-input
            v-model:value="newKeyword.keyword"
            placeholder="请输入关键词"
            @press-enter="handleAddKeyword"
            ref="keywordInput"
          />
        </a-form-item>
        <a-form-item label="预览">
          <div class="keyword-preview">
            <a-tag color="blue">{{ newKeyword.keyword || '请输入关键词' }}</a-tag>
            <span v-if="newKeyword.keyword" class="length-info">
              ({{ newKeyword.keyword.length }} 字符)
            </span>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑关键词模态框 -->
    <a-modal
      v-model:open="editKeywordModalVisible"
      title="编辑关键词"
      @ok="handleEditKeyword"
      :confirm-loading="editKeywordLoading"
    >
      <a-form :model="editingKeyword" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="索引">
          <a-input :value="editingKeyword.index + 1" disabled />
        </a-form-item>
        <a-form-item label="关键词" required>
          <a-input
            v-model:value="editingKeyword.keyword"
            placeholder="请输入关键词"
            @press-enter="handleEditKeyword"
            ref="editKeywordInput"
          />
        </a-form-item>
        <a-form-item label="预览">
          <div class="keyword-preview">
            <a-tag color="green">{{ editingKeyword.keyword || '请输入关键词' }}</a-tag>
            <span v-if="editingKeyword.keyword" class="length-info">
              ({{ editingKeyword.keyword.length }} 字符)
            </span>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量编辑关键词模态框 -->
    <a-modal
      v-model:open="batchKeywordModalVisible"
      title="批量编辑关键词"
      width="800px"
      @ok="handleBatchKeywordUpdate"
      :confirm-loading="batchKeywordLoading"
    >
      <div class="batch-edit-content">
        <a-alert
          message="批量编辑说明"
          description="每行一个关键词，空行将被忽略，重复的关键词将被去重。"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />
        
        <a-textarea
          v-model:value="batchKeywordsText"
          placeholder="请输入关键词，每行一个"
          :rows="15"
          style="font-family: monospace;"
        />
        
        <div class="batch-stats" style="margin-top: 16px;">
          <a-space>
            <span>总行数: {{ batchKeywordsText.split('\n').length }}</span>
            <span>有效关键词: {{ getValidBatchKeywords().length }}</span>
            <span>重复关键词: {{ getDuplicateBatchKeywords().length }}</span>
          </a-space>
        </div>
      </div>
    </a-modal>

        </a-tab-pane>

        <!-- 关键词管理标签页 -->
        <a-tab-pane key="keywords" tab="关键词管理">
          <template #tab>
            <span>
              <SearchOutlined />
              关键词管理
            </span>
          </template>

          <!-- 关键词管理内容 -->
          <div class="keyword-management-section">
            <!-- 统计信息 -->
            <div class="stats-section">
              <a-row :gutter="16">
                <a-col :span="6">
                  <a-card>
                    <a-statistic
                      title="总关键词数"
                      :value="keywords.length"
                      :value-style="{ color: '#1890ff' }"
                    >
                      <template #prefix>
                        <FileTextOutlined />
                      </template>
                    </a-statistic>
                  </a-card>
                </a-col>
                <a-col :span="6">
                  <a-card>
                    <a-statistic
                      title="平均长度"
                      :value="averageKeywordLength"
                      :precision="1"
                      :value-style="{ color: '#52c41a' }"
                    >
                      <template #prefix>
                        <BarChartOutlined />
                      </template>
                    </a-statistic>
                  </a-card>
                </a-col>
                <a-col :span="6">
                  <a-card>
                    <a-statistic
                      title="最长关键词"
                      :value="maxKeywordLength"
                      :value-style="{ color: '#fa8c16' }"
                    >
                      <template #prefix>
                        <LineChartOutlined />
                      </template>
                    </a-statistic>
                  </a-card>
                </a-col>
                <a-col :span="6">
                  <a-card>
                    <a-statistic
                      title="最短关键词"
                      :value="minKeywordLength"
                      :value-style="{ color: '#eb2f96' }"
                    >
                      <template #prefix>
                        <MinusOutlined />
                      </template>
                    </a-statistic>
                  </a-card>
                </a-col>
              </a-row>
            </div>

            <!-- 关键词列表 -->
            <div class="keywords-section">
              <a-card title="关键词列表" :bordered="false">
                <template #extra>
                  <a-space>
                    <a-input-search
                      v-model:value="keywordSearchText"
                      placeholder="搜索关键词"
                      style="width: 200px"
                      @search="handleKeywordSearch"
                      allow-clear
                    />
                    <a-button @click="showAddKeywordModal" type="primary">
                      <template #icon>
                        <PlusOutlined />
                      </template>
                      添加关键词
                    </a-button>
                    <a-button @click="showBatchKeywordModal" :disabled="keywords.length === 0">
                      <template #icon>
                        <EditOutlined />
                      </template>
                      批量编辑
                    </a-button>
                    <a-button @click="clearAllKeywords" :disabled="keywords.length === 0" danger>
                      <template #icon>
                        <DeleteOutlined />
                      </template>
                      清空全部
                    </a-button>
                  </a-space>
                </template>

                <div v-if="keywordLoading" class="loading-container">
                  <a-spin size="large" />
                  <p>加载中...</p>
                </div>

                <div v-else-if="filteredKeywords.length === 0" class="empty-container">
                  <a-empty description="暂无关键词数据">
                    <a-button type="primary" @click="showAddKeywordModal">
                      添加第一个关键词
                    </a-button>
                  </a-empty>
                </div>

                <div v-else class="keywords-list">
                  <div 
                    v-for="(keyword, index) in filteredKeywords" 
                    :key="index"
                    class="keyword-item"
                  >
                    <div class="keyword-content">
                      <div class="keyword-index">{{ getOriginalKeywordIndex(index) + 1 }}</div>
                      <div class="keyword-text" :title="keyword">{{ keyword }}</div>
                      <div class="keyword-stats">
                        <a-tag size="small" color="blue">{{ keyword.length }} 字符</a-tag>
                      </div>
                    </div>
                    <div class="keyword-actions">
                      <a-space>
                        <a-button 
                          size="small" 
                          @click="editKeyword(getOriginalKeywordIndex(index), keyword)"
                          type="primary"
                          ghost
                        >
                          <template #icon>
                            <EditOutlined />
                          </template>
                          编辑
                        </a-button>
                        <a-button 
                          size="small" 
                          @click="deleteKeyword(getOriginalKeywordIndex(index), keyword)"
                          danger
                          ghost
                        >
                          <template #icon>
                            <DeleteOutlined />
                          </template>
                          删除
                        </a-button>
                      </a-space>
                    </div>
                  </div>
                </div>
              </a-card>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  BugOutlined,
  ReloadOutlined,
  EyeOutlined,
  ThunderboltOutlined,
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FileTextOutlined,
  BarChartOutlined,
  LineChartOutlined,
  MinusOutlined
} from '@ant-design/icons-vue';
import {
  getAllKeywords,
  addKeyword,
  updateKeyword,
  deleteKeyword as deleteKeywordApi,
  batchUpdateKeywords,
  clearAllKeywords as clearAllKeywordsApi
} from '@/api/keywordManagement';

// 响应式数据
const testAllLoading = ref(false);
const refreshLoading = ref(false);
const batchTestLoading = ref(false);
const testExecuting = ref(false);
const selectedCrawlers = ref<string[]>([]);
const expandedCrawlers = ref<string[]>([]);
const testModalVisible = ref(false);
const testInterfaceVisible = ref(false);
const testResultModalVisible = ref(false);
const crawlerDetailModalVisible = ref(false);
const currentTestResult = ref<any>(null);
const selectedCrawler = ref<any>(null);
const testResult = ref<any>(null);
const responseTabActive = ref('formatted');

// 标签页管理
const activeTab = ref('crawlers');

// 关键词管理相关
const keywordLoading = ref(false);
const addKeywordLoading = ref(false);
const editKeywordLoading = ref(false);
const batchKeywordLoading = ref(false);
const keywords = ref<string[]>([]);
const keywordSearchText = ref('');
const addKeywordModalVisible = ref(false);
const editKeywordModalVisible = ref(false);
const batchKeywordModalVisible = ref(false);
const newKeyword = ref({ keyword: '' });
const editingKeyword = ref({ index: -1, keyword: '' });
const batchKeywordsText = ref('');

// 关键词选项（用于下拉选择）
const keywordOptions = computed(() => {
  return keywords.value
    .filter(keyword => keyword && keyword.trim().length > 0) // 过滤空字符串和空白字符
    .map(keyword => ({
      label: keyword,
      value: keyword
    }));
});

// 测试参数
const testParams = ref({
  // 通用参数
  maxRecords: 10,
  batchSize: 10,
  dateFrom: null,
  dateTo: null,
  totalCount: 50,
  hsCode: '9018',
  startDate: null,
  maxPages: 5,
  inputKeywords: '',
  keywordSource: 'manual', // 关键词来源：manual, list
  
  // D_510K 专用参数
  deviceName: '',
  applicantName: '',
  
  // D_event 专用参数
  brandName: '',
  manufacturer: '',
  modelNumber: '',
  
  // D_recall 专用参数
  productName: '',
  reasonForRecall: '',
  recallingFirm: '',
  
  // D_registration 专用参数
  establishmentName: '',
  proprietaryName: '',
  ownerOperatorName: ''
});


// 美国爬虫配置
const usaCrawlers = ref([
  {
    key: 'd510k',
    displayName: 'D_510K - FDA 510K设备',
    className: 'com.certification.crawler.countrydata.us.D_510K',
    entity: 'Device510K',
    description: 'FDA 510K设备审批数据爬虫，用于获取FDA 510K设备审批信息',
    testEndpoint: '/api/api/us-crawler/search/d510k',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      deviceName: '',
      applicantName: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 5
    }
  },
  {
    key: 'devent',
    displayName: 'D_event - FDA事件报告',
    className: 'com.certification.crawler.countrydata.us.D_event',
    entity: 'DeviceEventReport',
    description: 'FDA设备不良事件数据爬虫，用于获取FDA设备不良事件信息',
    testEndpoint: '/api/api/us-crawler/search/devent',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      brandName: '',
      manufacturer: '',
      modelNumber: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 5
    }
  },
  {
    key: 'drecall',
    displayName: 'D_recall - FDA召回数据',
    className: 'com.certification.crawler.countrydata.us.D_recall',
    entity: 'DeviceRecallRecord',
    description: 'FDA设备召回数据爬虫，用于获取FDA设备召回信息',
    testEndpoint: '/api/api/us-crawler/search/drecall',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      productName: '',
      reasonForRecall: '',
      recallingFirm: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 5,
      useKeywordList: false,
      inputKeywords: '',
      selectedKeywords: []
    }
  },
  {
    key: 'dregistration',
    displayName: 'D_registration - FDA注册数据',
    className: 'com.certification.crawler.countrydata.us.D_registration',
    entity: 'DeviceRegistrationRecord',
    description: 'FDA设备注册信息爬虫，用于获取FDA设备注册信息',
    testEndpoint: '/api/api/us-crawler/search/dregistration',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      establishmentName: '',
      proprietaryName: '',
      ownerOperatorName: '',
      maxPages: 5
    }
  },
  {
    key: 'unicrawl',
    displayName: 'unicrawl - 统一爬虫',
    className: 'com.certification.crawler.countrydata.us.unicrawl',
    entity: 'UnifiedCrawler',
    description: '统一爬虫，支持多种数据源的统一爬取',
    testEndpoint: '/api/api/us-crawler/test/unicrawl',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      totalCount: 50,
      dateFrom: null,
      dateTo: null,
      inputKeywords: '',
      maxPages: 0
    }
  },
  {
    key: 'customs-case',
    displayName: 'CustomsCaseCrawler - 海关案例',
    className: 'com.certification.crawler.generalArchitecture.us.CustomsCaseCrawler',
    entity: 'CustomsCase',
    description: 'CBP海关裁定数据爬虫，用于获取美国海关与边境保护局裁定信息',
    testEndpoint: '/api/api/us-crawler/search/customs-case',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      hsCode: '9018',
      maxRecords: 10,
      batchSize: 10,
      startDate: null
    }
  },
  {
    key: 'guidance',
    displayName: 'GuidanceCrawler - 指导文档',
    className: 'com.certification.crawler.generalArchitecture.us.GuidanceCrawler',
    entity: 'GuidanceDocument',
    description: 'FDA指导文档爬虫，用于获取FDA医疗设备指导文档',
    testEndpoint: '/api/api/us-crawler/search/guidance',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      maxRecords: 10
    }
  }
]);

// 计算属性
const usaRunningCount = computed(() => 
  usaCrawlers.value.filter(c => c.status === 'running').length
);

const usaStoppedCount = computed(() => 
  usaCrawlers.value.filter(c => c.status === 'stopped').length
);

const usaAvailableCount = computed(() => 
  usaCrawlers.value.filter(c => c.status === 'available').length
);

// 关键词管理计算属性
const filteredKeywords = computed(() => {
  if (!keywordSearchText.value) {
    return keywords.value;
  }
  return keywords.value.filter(keyword => 
    keyword.toLowerCase().includes(keywordSearchText.value.toLowerCase())
  );
});

const averageKeywordLength = computed(() => {
  if (keywords.value.length === 0) return 0;
  const totalLength = keywords.value.reduce((sum, keyword) => sum + keyword.length, 0);
  return totalLength / keywords.value.length;
});

const maxKeywordLength = computed(() => {
  if (keywords.value.length === 0) return 0;
  return Math.max(...keywords.value.map(keyword => keyword.length));
});

const minKeywordLength = computed(() => {
  if (keywords.value.length === 0) return 0;
  return Math.min(...keywords.value.map(keyword => keyword.length));
});

// 方法
const showTestInterface = (crawler: any) => {
  selectedCrawler.value = crawler;
  resetTestParams();
  testResult.value = null;
  testInterfaceVisible.value = true;
};

const getRequestPayload = () => {
  if (!selectedCrawler.value) return {};
  
  const params: any = {};
  
  // 首先处理关键词相关参数
  console.log('关键词来源:', testParams.value.keywordSource);
  console.log('手动输入关键词:', testParams.value.inputKeywords);
  console.log('关键词选项数量:', keywordOptions.value.length);
  console.log('关键词选项:', keywordOptions.value);
  
  if (testParams.value.keywordSource === 'list') {
    // 使用所有可用的关键词列表，转换为空格分隔的字符串
    const allKeywords = keywordOptions.value.map(option => option.value);
    params['inputKeywords'] = allKeywords.join(' ');
    console.log('使用所有关键词列表，数量:', allKeywords.length);
    console.log('转换后:', params['inputKeywords']);
  } else if (testParams.value.keywordSource === 'manual' && testParams.value.inputKeywords) {
    // 手动输入的关键词，将字符串按行分割成数组
    const keywords = testParams.value.inputKeywords.split('\n')
      .map((k: string) => k.trim())
      .filter((k: string) => k.length > 0);
    if (keywords.length > 0) {
      params['inputKeywords'] = keywords;
      console.log('使用手动输入关键词，转换后:', params['inputKeywords']);
    }
  } else {
    console.log('没有设置inputKeywords，keywordSource:', testParams.value.keywordSource);
  }

  // 根据爬虫类型构建其他参数
  Object.keys(testParams.value).forEach(key => {
    const value = (testParams.value as any)[key];
    if (value !== null && value !== undefined && value !== '' && key !== 'inputKeywords') {
      if (key.includes('Date') && value) {
        // 处理日期格式
        if (key === 'startDate') {
          params[key] = dayjs(value).format('MM/DD/YYYY');
        } else {
          params[key] = dayjs(value).format('YYYY-MM-DD');
        }
      } else {
        params[key] = value;
      }
    }
  });
  
  return params;
};

const executeTest = async () => {
  if (!selectedCrawler.value) return;
  
  testExecuting.value = true;
  const startTime = Date.now();
  
  try {
    const payload = getRequestPayload();
    console.log(`开始执行测试: ${selectedCrawler.value.displayName}`, payload);
    console.log('payload.inputKeywords:', payload.inputKeywords);
    
    // 判断是否使用GET请求（参数化搜索接口）
    const isSearchEndpoint = selectedCrawler.value.testEndpoint.includes('/search/');
    
    let response;
    if (isSearchEndpoint) {
      // 构建查询参数
      const queryParams = new URLSearchParams();
      Object.keys(payload).forEach(key => {
        if (payload[key] !== null && payload[key] !== undefined && payload[key] !== '') {
          // 特殊处理inputKeywords参数
          if (key === 'inputKeywords' && Array.isArray(payload[key])) {
            // 如果是数组，转换为空格分隔的字符串
            queryParams.append(key, payload[key].join(' '));
          } else {
            queryParams.append(key, payload[key]);
          }
        }
      });
      
      const url = `http://localhost:8080${selectedCrawler.value.testEndpoint}?${queryParams.toString()}`;
      console.log('GET请求URL:', url);
      console.log('queryParams.toString():', queryParams.toString());
      
      response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });
    } else {
      // 使用POST请求发送JSON数据
      response = await fetch(`http://localhost:8080${selectedCrawler.value.testEndpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
      });
    }
    
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    const responseText = await response.text();
    const dataSize = new Blob([responseText]).size;
    
    let result;
    try {
      result = JSON.parse(responseText);
    } catch (e) {
      result = { message: responseText };
    }
    
    testResult.value = {
      success: response.ok,
      status: response.status,
      responseTime,
      dataSize,
      data: result,
      rawResponse: responseText,
      timestamp: new Date().toLocaleString()
    };
    
    // 更新爬虫状态
    selectedCrawler.value.lastTest = testResult.value.timestamp;
    selectedCrawler.value.status = result.success ? 'available' : 'stopped';
    
    if (result.success) {
      message.success(`${selectedCrawler.value.displayName} 测试成功`);
    } else {
      message.error(`${selectedCrawler.value.displayName} 测试失败: ${result.message}`);
    }
    
  } catch (error: any) {
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    console.error(`测试失败: ${selectedCrawler.value.displayName}`, error);
    
    testResult.value = {
      success: false,
      status: 0,
      responseTime,
      dataSize: 0,
      data: { error: error.toString() },
      rawResponse: error.toString(),
      timestamp: new Date().toLocaleString()
    };
    
    message.error(`${selectedCrawler.value.displayName} 测试失败`);
  } finally {
    testExecuting.value = false;
  }
};

const resetCrawlerParams = (crawler: any) => {
  switch (crawler.key) {
    case 'd510k':
      crawler.testParams = {
        deviceName: '',
        applicantName: '',
        dateFrom: null,
        dateTo: null,
        maxPages: 5
      };
      break;
    case 'devent':
      crawler.testParams = {
        brandName: '',
        manufacturer: '',
        modelNumber: '',
        dateFrom: null,
        dateTo: null,
        maxPages: 5
      };
      break;
    case 'drecall':
      crawler.testParams = {
        productName: '',
        reasonForRecall: '',
        recallingFirm: '',
        dateFrom: null,
        dateTo: null,
        maxPages: 5,
        useKeywordList: false,
        inputKeywords: '',
        selectedKeywords: []
      };
      break;
    case 'dregistration':
      crawler.testParams = {
        establishmentName: '',
        proprietaryName: '',
        ownerOperatorName: '',
        maxPages: 5
      };
      break;
    case 'unicrawl':
      crawler.testParams = {
        totalCount: 50,
        dateFrom: null,
        dateTo: null,
        inputKeywords: '',
        maxPages: 0
      };
      break;
    case 'customs-case':
      crawler.testParams = {
        hsCode: '9018',
        maxRecords: 10,
        batchSize: 10,
        startDate: null
      };
      break;
    case 'guidance':
      crawler.testParams = {
        maxRecords: 10
      };
      break;
  }
  message.success('参数已重置');
};


const resetTestParams = () => {
  testParams.value = {
    // 通用参数
    maxRecords: 10,
    batchSize: 10,
    dateFrom: null,
    dateTo: null,
    totalCount: 50,
    hsCode: '9018',
    startDate: null,
    maxPages: 5,
    inputKeywords: '',
    keywordSource: 'manual',
    
    // D_510K 专用参数
    deviceName: '',
    applicantName: '',
    
    // D_event 专用参数
    brandName: '',
    manufacturer: '',
    modelNumber: '',
    
    // D_recall 专用参数
    productName: '',
    reasonForRecall: '',
    recallingFirm: '',
    
    // D_registration 专用参数
    establishmentName: '',
    proprietaryName: '',
    ownerOperatorName: ''
  };
};

const executeParameterizedTest = async (crawler: any) => {
  if (!crawler) return;
  
  crawler.testing = true;
  
  try {
    // 构建请求参数
    const params: any = {};
    
    // 根据爬虫类型构建参数
    Object.keys(crawler.testParams).forEach(key => {
      const value = crawler.testParams[key];
      if (value !== null && value !== undefined && value !== '') {
        if (key.includes('Date') && value) {
          // 处理日期格式
          if (key === 'startDate') {
            params[key] = dayjs(value).format('MM/DD/YYYY');
          } else {
            params[key] = dayjs(value).format('YYYY-MM-DD');
          }
        } else {
          params[key] = value;
        }
      }
    });
    
    console.log(`开始执行参数化测试: ${crawler.displayName}`, params);
    
    const response = await fetch(`http://localhost:8080${crawler.testEndpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(params)
    });
    
    const result = await response.json();
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: result.success,
      message: result.message || '测试完成',
      totalSaved: result.totalSaved || 0,
      totalSkipped: result.totalSkipped || 0,
      totalPages: result.totalPages || 0,
      testTime: new Date().toLocaleString(),
      details: result
    };
    
    // 更新爬虫状态
    crawler.lastTest = testResult.testTime;
    crawler.status = result.success ? 'available' : 'stopped';
    
    // 显示测试结果
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    if (result.success) {
      message.success(`${crawler.displayName} 参数化测试成功`);
    } else {
      message.error(`${crawler.displayName} 参数化测试失败: ${result.message}`);
    }
    
  } catch (error: any) {
    console.error(`参数化测试失败: ${crawler.displayName}`, error);
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: false,
      message: `参数化测试失败: ${error}`,
      totalSaved: 0,
      totalSkipped: 0,
      totalPages: 0,
      testTime: new Date().toLocaleString(),
      details: { error: error.toString() }
    };
    
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    message.error(`${crawler.displayName} 参数化测试失败`);
  } finally {
    crawler.testing = false;
  }
};

const quickTest = async (crawler: any) => {
  crawler.testing = true;
  
  try {
    console.log(`开始快速测试: ${crawler.displayName}`);
    
    // 使用默认参数进行快速测试
    const defaultParams = getDefaultParams(crawler.key);
    
    const response = await fetch(`http://localhost:8080${crawler.testEndpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(defaultParams)
    });
    
    const result = await response.json();
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: result.success,
      message: result.message || '快速测试完成',
      totalSaved: result.totalSaved || 0,
      totalSkipped: result.totalSkipped || 0,
      totalPages: result.totalPages || 0,
      testTime: new Date().toLocaleString(),
      details: result
    };
    
    // 更新爬虫状态
    crawler.lastTest = testResult.testTime;
    crawler.status = result.success ? 'available' : 'stopped';
    
    // 显示测试结果
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;

    if (result.success) {
      message.success(`${crawler.displayName} 快速测试成功`);
      } else {
      message.error(`${crawler.displayName} 快速测试失败: ${result.message}`);
    }
    
  } catch (error: any) {
    console.error(`快速测试失败: ${crawler.displayName}`, error);
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: false,
      message: `快速测试失败: ${error}`,
      totalSaved: 0,
      totalSkipped: 0,
      totalPages: 0,
      testTime: new Date().toLocaleString(),
      details: { error: error.toString() }
    };
    
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    message.error(`${crawler.displayName} 快速测试失败`);
  } finally {
    crawler.testing = false;
  }
};

const getDefaultParams = (crawlerKey: string) => {
  switch (crawlerKey) {
    case 'd510k':
      return { maxPages: 3, inputKeywords: '', keywordSource: 'manual' };
    case 'devent':
      return { maxPages: 3, inputKeywords: '', keywordSource: 'manual' };
    case 'drecall':
      return { maxPages: 3, inputKeywords: '', keywordSource: 'manual' };
    case 'dregistration':
      return { maxPages: 3, inputKeywords: '', keywordSource: 'manual' };
    case 'unicrawl':
      return { totalCount: 10, dateFrom: null, dateTo: null, inputKeywords: '', maxPages: 0 };
    case 'customs-case':
      return { hsCode: '9018', maxRecords: 5, batchSize: 5, inputKeywords: '' };
    case 'guidance':
      return { maxRecords: 5, inputKeywords: '' };
    default:
      return { inputKeywords: '' };
  }
};

const testAllCrawlers = async () => {
  testAllLoading.value = true;
  
  try {
    const promises = usaCrawlers.value.map(crawler => quickTest(crawler));
    await Promise.all(promises);
    
    const successCount = usaCrawlers.value.filter(c => c.status === 'available').length;
    message.success(`所有爬虫快速测试完成！成功: ${successCount}/${usaCrawlers.value.length}`);
    
  } catch (error) {
    console.error('批量测试失败:', error);
    message.error('批量测试失败');
  } finally {
    testAllLoading.value = false;
  }
};

const batchQuickTest = async () => {
  if (selectedCrawlers.value.length === 0) {
    message.warning('请选择要测试的爬虫');
    return;
  }
  
  batchTestLoading.value = true;
  
  try {
    const selectedCrawlerObjects = usaCrawlers.value.filter(c => 
      selectedCrawlers.value.includes(c.key)
    );
    
    const promises = selectedCrawlerObjects.map(crawler => quickTest(crawler));
    await Promise.all(promises);
    
    message.success(`批量快速测试完成！共测试 ${selectedCrawlers.value.length} 个爬虫`);
    clearSelection();
    
  } catch (error) {
    console.error('批量测试失败:', error);
    message.error('批量测试失败');
  } finally {
    batchTestLoading.value = false;
  }
};

const refreshAllStatus = async () => {
  refreshLoading.value = true;
  
  try {
    // 这里可以调用状态检查API
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    message.success('状态刷新完成');
  } catch (error) {
    console.error('刷新状态失败:', error);
    message.error('刷新状态失败');
  } finally {
    refreshLoading.value = false;
  }
};

const viewCrawlerDetails = (crawler: any) => {
  selectedCrawler.value = crawler;
  crawlerDetailModalVisible.value = true;
};

const handleCrawlerSelect = (crawlerKey: string, checked: boolean) => {
  if (checked) {
    if (!selectedCrawlers.value.includes(crawlerKey)) {
      selectedCrawlers.value.push(crawlerKey);
    }
  } else {
    const index = selectedCrawlers.value.indexOf(crawlerKey);
    if (index > -1) {
      selectedCrawlers.value.splice(index, 1);
    }
  }
};

const clearSelection = () => {
  selectedCrawlers.value = [];
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'running':
      return 'green';
    case 'stopped':
      return 'red';
    case 'available':
      return 'blue';
    default:
      return 'default';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'running':
      return '运行中';
    case 'stopped':
      return '已停止';
    case 'available':
      return '可用';
    default:
      return '未知';
  }
};

// 关键词管理方法
const refreshKeywords = async () => {
  keywordLoading.value = true;
  try {
    const response = await getAllKeywords();
    if (response.success) {
      keywords.value = response.keywords || [];
      message.success(`成功加载 ${keywords.value.length} 个关键词`);
    } else {
      message.error(response.message || '加载关键词失败');
    }
  } catch (error: any) {
    console.error('加载关键词失败:', error);
    message.error('加载关键词失败: ' + error.message);
  } finally {
    keywordLoading.value = false;
  }
};

const showAddKeywordModal = () => {
  newKeyword.value.keyword = '';
  addKeywordModalVisible.value = true;
};

const handleAddKeyword = async () => {
  if (!newKeyword.value.keyword.trim()) {
    message.warning('请输入关键词');
    return;
  }
  
  addKeywordLoading.value = true;
  try {
    const response = await addKeyword(newKeyword.value.keyword.trim());
    if (response.success) {
      message.success('关键词添加成功');
      addKeywordModalVisible.value = false;
      await refreshKeywords();
    } else {
      message.error(response.message || '添加关键词失败');
    }
  } catch (error: any) {
    console.error('添加关键词失败:', error);
    message.error('添加关键词失败: ' + error.message);
  } finally {
    addKeywordLoading.value = false;
  }
};

const editKeyword = (index: number, keyword: string) => {
  editingKeyword.value = { index, keyword };
  editKeywordModalVisible.value = true;
};

const handleEditKeyword = async () => {
  if (!editingKeyword.value.keyword.trim()) {
    message.warning('请输入关键词');
    return;
  }
  
  editKeywordLoading.value = true;
  try {
    const response = await updateKeyword(editingKeyword.value.index, editingKeyword.value.keyword.trim());
    if (response.success) {
      message.success('关键词更新成功');
      editKeywordModalVisible.value = false;
      await refreshKeywords();
    } else {
      message.error(response.message || '更新关键词失败');
    }
  } catch (error: any) {
    console.error('更新关键词失败:', error);
    message.error('更新关键词失败: ' + error.message);
  } finally {
    editKeywordLoading.value = false;
  }
};

const deleteKeyword = (index: number, keyword: string) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除关键词 "${keyword}" 吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await deleteKeywordApi(index);
        if (response.success) {
          message.success('关键词删除成功');
          await refreshKeywords();
        } else {
          message.error(response.message || '删除关键词失败');
        }
      } catch (error: any) {
        console.error('删除关键词失败:', error);
        message.error('删除关键词失败: ' + error.message);
      }
    }
  });
};

const showBatchKeywordModal = () => {
  batchKeywordsText.value = keywords.value.join('\n');
  batchKeywordModalVisible.value = true;
};

const getValidBatchKeywords = () => {
  return batchKeywordsText.value
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0);
};

const getDuplicateBatchKeywords = () => {
  const validKeywords = getValidBatchKeywords();
  const seen = new Set();
  return validKeywords.filter(keyword => {
    if (seen.has(keyword)) {
      return true;
    }
    seen.add(keyword);
    return false;
  });
};

const handleBatchKeywordUpdate = async () => {
  const validKeywords = getValidBatchKeywords();
  if (validKeywords.length === 0) {
    message.warning('请输入至少一个关键词');
    return;
  }
  
  batchKeywordLoading.value = true;
  try {
    const response = await batchUpdateKeywords(validKeywords);
    if (response.success) {
      message.success(`批量更新成功，共 ${validKeywords.length} 个关键词`);
      batchKeywordModalVisible.value = false;
      await refreshKeywords();
    } else {
      message.error(response.message || '批量更新失败');
    }
  } catch (error: any) {
    console.error('批量更新失败:', error);
    message.error('批量更新失败: ' + error.message);
  } finally {
    batchKeywordLoading.value = false;
  }
};

const clearAllKeywords = () => {
  Modal.confirm({
    title: '确认清空',
    content: `确定要清空所有 ${keywords.value.length} 个关键词吗？此操作不可恢复！`,
    okText: '清空',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await clearAllKeywordsApi();
        if (response.success) {
          message.success('所有关键词已清空');
          await refreshKeywords();
        } else {
          message.error(response.message || '清空关键词失败');
        }
      } catch (error: any) {
        console.error('清空关键词失败:', error);
        message.error('清空关键词失败: ' + error.message);
      }
    }
  });
};

const handleKeywordSearch = () => {
  // 搜索逻辑已在计算属性中处理
};

const getOriginalKeywordIndex = (filteredIndex: number) => {
  const filteredKeyword = filteredKeywords.value[filteredIndex];
  return keywords.value.indexOf(filteredKeyword);
};



// 生命周期
onMounted(() => {
  console.log('美国爬虫管理系统初始化完成');
  // 初始化时加载关键词
  refreshKeywords();
});
</script>

<style scoped>
.crawler-management {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.header-content p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.usa-crawler-section {
  width: 100%;
}

.country-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.crawler-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.crawler-list-item {
  position: relative;
  background: #ffffff;
  border: 1px solid #e8eaed;
  border-radius: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.crawler-list-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #d1d9e0;
}

.crawler-list-item.running {
  border-color: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.15), 0 4px 16px rgba(82, 196, 26, 0.1);
}

.crawler-list-item.selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.15), 0 4px 16px rgba(24, 144, 255, 0.1);
}

.crawler-list-item.testing {
  pointer-events: none;
  opacity: 0.8;
}

.crawler-list-item.expanded {
  border-color: #1890ff;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.1);
}

/* 列表头部 */
.crawler-list-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.crawler-list-header:hover {
  background: #f0f2f5;
}

.crawler-icon {
  margin-right: 16px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.icon-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.9;
}

.icon-wrapper > * {
  position: relative;
  z-index: 1;
}

.icon-wrapper.d510k::before {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-wrapper.devent::before {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.icon-wrapper.drecall::before {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.icon-wrapper.dregistration::before {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.icon-wrapper.unicrawl::before {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.icon-wrapper.customs-case::before {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.icon-wrapper.guidance::before {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.crawler-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.crawler-name-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.crawler-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.3;
}

.crawler-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.crawler-description {
  margin: 0;
}

.crawler-description p {
  margin: 0;
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.crawler-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.test-actions {
  display: flex;
  align-items: center;
}

.status-tag {
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-dot.running {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.entity-tag {
  background: #f0f2f5;
  color: #666;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

.crawler-checkbox {
  margin-left: 12px;
}

/* 展开内容区域 */
.crawler-expanded-content {
  padding: 20px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
}

.crawler-description {
  margin-bottom: 20px;
}

.crawler-description p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.parameter-test-section {
  background: #fafbfc;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.parameter-test-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.test-params {
  margin-bottom: 16px;
}

.test-params .ant-form-item {
  margin-bottom: 12px;
}

.test-params .ant-form-item-label {
  padding-bottom: 4px;
}

.test-params .ant-form-item-label > label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.test-actions {
  display: flex;
  justify-content: flex-start;
  padding-top: 12px;
  border-top: 1px solid #e8eaed;
}

/* Knif4j风格样式 */
.knif4j-modal .ant-modal-body {
  padding: 0;
}

.knif4j-interface {
  max-height: 80vh;
  overflow-y: auto;
}

.api-info-section {
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8eaed;
}

.params-section {
  padding: 16px;
  border-bottom: 1px solid #e8eaed;
}

.params-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.params-form {
  background: #fff;
}

.request-preview-section {
  padding: 16px;
  border-bottom: 1px solid #e8eaed;
}

.request-preview-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.json-preview {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.json-preview pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #333;
}

.action-buttons {
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8eaed;
  display: flex;
  justify-content: flex-end;
}

.response-section {
  padding: 16px;
}

.response-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.response-info {
  margin-bottom: 16px;
}

.response-content {
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.json-response,
.raw-response {
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
  background: #f5f5f5;
}

.json-response pre,
.raw-response pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 卡片内容 */
.crawler-card-content {
  padding: 16px 20px;
}

.crawler-description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.crawler-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #e9ecef;
}

.detail-label {
  font-size: 12px;
  color: #8c8c8c;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}

/* 卡片底部 */
.crawler-card-footer {
  padding: 16px 20px 20px 20px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.primary-btn {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: white;
}

.secondary-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.info-btn {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  color: white;
}

/* 加载遮罩 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  z-index: 10;
}

.loading-text {
  margin-top: 12px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.batch-actions {
  margin-top: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.test-modal-content {
  padding: 16px 0;
}

.test-form {
  margin-bottom: 24px;
}

.test-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.test-result-content {
  padding: 16px 0;
}

.test-details {
  margin-top: 16px;
}

.details-content {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.crawler-detail-content {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .crawler-list-header {
    padding: 12px 16px;
  }
  
  .crawler-expanded-content {
    padding: 16px;
  }
  
  .crawler-info {
    gap: 6px;
  }
  
  .crawler-name {
    font-size: 15px;
  }
  
  .crawler-details {
    flex-direction: column;
    gap: 4px;
  }
  
  .crawler-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .test-actions {
    width: 100%;
  }
  
  .test-actions .ant-btn {
    width: 100%;
  }
  
  .icon-wrapper {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .parameter-test-section {
    padding: 12px;
  }
  
  .test-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .test-actions .ant-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .crawler-management {
    padding: 16px;
  }
  
  .crawler-list-header {
    padding: 10px 12px;
  }
  
  .crawler-expanded-content {
    padding: 12px;
  }
  
  .crawler-name {
    font-size: 14px;
  }
  
  .crawler-description p {
    font-size: 13px;
  }
  
  .crawler-details .detail-item {
    font-size: 11px;
  }
  
  .parameter-test-section {
    padding: 10px;
  }
  
  .parameter-test-section h4 {
    font-size: 13px;
  }
  
  .test-params .ant-form-item-label > label {
    font-size: 11px;
  }
}

/* 关键词管理样式 */
.main-tabs {
  margin-top: 16px;
}

.keyword-management-section {
  padding: 16px 0;
}

.stats-section {
  margin-bottom: 24px;
}

.keywords-section {
  width: 100%;
}

.loading-container {
  text-align: center;
  padding: 40px 0;
}

.empty-container {
  text-align: center;
  padding: 40px 0;
}

.keywords-list {
  max-height: 600px;
  overflow-y: auto;
}

.keyword-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #fff;
  transition: all 0.3s ease;
}

.keyword-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.keyword-content {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.keyword-index {
  width: 40px;
  height: 40px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #666;
  margin-right: 12px;
  flex-shrink: 0;
}

.keyword-text {
  flex: 1;
  font-size: 14px;
  color: #262626;
  margin-right: 12px;
  word-break: break-all;
  min-width: 0;
}

.keyword-stats {
  flex-shrink: 0;
}

.keyword-actions {
  flex-shrink: 0;
}

.keyword-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.length-info {
  color: #8c8c8c;
  font-size: 12px;
}

.batch-edit-content {
  padding: 16px 0;
}

.batch-stats {
  color: #8c8c8c;
  font-size: 12px;
}

/* 响应式设计 - 关键词管理 */
@media (max-width: 768px) {
  .keyword-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .keyword-content {
    width: 100%;
  }
  
  .keyword-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
<template>
  <div class="crawler-management">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
        <h1>🕷️ 爬虫管理系统</h1>
        <p>管理美国FDA和欧盟相关数据爬虫，支持参数化爬取和批量操作</p>
        </div>
        <div class="header-actions">
          <a-space>
          <a-button @click="testAllCrawlers" :loading="testAllLoading" type="primary" v-if="activeTab === 'crawlers'">
              <template #icon>
              <BugOutlined />
              </template>
            爬取所有爬虫
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
                    爬取
                </a-button>
                </div>
              </div>
            </div>


            <!-- 加载遮罩 -->
            <div v-if="crawler.testing" class="loading-overlay">
              <a-spin size="large" />
              <span class="loading-text">爬取中...</span>
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
              批量快速爬取
            </a-button>
            <a-button @click="clearSelection">
              清空选择
            </a-button>
          </a-space>
        </div>
    </a-card>
    </div>

    <!-- Knif4j风格爬取界面 -->
    <a-modal
      v-model:open="testInterfaceVisible"
      :title="`${selectedCrawler?.displayName || ''} - API爬取`"
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
            <!-- US_510K 参数 -->
            <template v-if="selectedCrawler.key === 'us510k'">
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="设备名称">
                    <a-input
                      v-model:value="testParams.deviceName"
                      placeholder="如：Pacemaker"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="申请人名称">
                    <a-input
                      v-model:value="testParams.applicantName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="贸易名称">
                    <a-input
                      v-model:value="testParams.tradeName"
                      placeholder="如：Trade Name"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- US_event 参数 -->
            <template v-else-if="selectedCrawler.key === 'usevent'">
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="设备名称">
                    <a-input
                      v-model:value="testParams.deviceName"
                      placeholder="如：Pacemaker"
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
                  <a-form-item label="产品问题">
                    <a-input
                      v-model:value="testParams.productProblem"
                      placeholder="如：Product Problem"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- US_recall 参数 -->
            <template v-else-if="selectedCrawler.key === 'usrecall'">
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                        <template #icon>
                          <ReloadOutlined />
                        </template>
                        刷新关键词列表
                      </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>

              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="召回公司">
                    <a-input
                      v-model:value="testParams.recallingFirm"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="品牌名称">
                    <a-input
                      v-model:value="testParams.brandName"
                      placeholder="如：Medtronic"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="产品描述">
                    <a-input
                      v-model:value="testParams.productDescription"
                      placeholder="如：Product Description"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- US_registration 参数 -->
            <template v-else-if="selectedCrawler.key === 'usregistration'">
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="16" style="width: 100%;">
                <a-col :span="8" :xs="24" :sm="12" :md="8">
                  <a-form-item label="机构/贸易名称" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
                    <a-input
                      v-model:value="testParams.establishmentName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" :xs="24" :sm="12" :md="8">
                  <a-form-item label="专有名称" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
                    <a-input
                      v-model:value="testParams.proprietaryName"
                      placeholder="如：Pacemaker"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8" :xs="24" :sm="12" :md="8">
                  <a-form-item label="所有者/经营者名称" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
                    <a-input
                      v-model:value="testParams.ownerOperatorName"
                      placeholder="如：Medtronic Inc"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
<!--              <a-row :gutter="24">-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="机构/贸易名称">-->
<!--                    <a-input-->
<!--                      v-model:value="testParams.establishmentName"-->
<!--                      placeholder="如：Medtronic Inc"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="专有名称">-->
<!--                    <a-input-->
<!--                      v-model:value="testParams.proprietaryName"-->
<!--                      placeholder="如：Pacemaker"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="所有者/经营者名称">-->
<!--                    <a-input-->
<!--                      v-model:value="testParams.ownerOperatorName"-->
<!--                      placeholder="如：Medtronic Inc"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--              </a-row>-->
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
                  <!-- <a-form-item label="开始日期">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      placeholder="YYYY-MM-DD"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item> -->
                </a-col>
                <a-col :span="6">
                  <!-- <a-form-item label="结束日期">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      placeholder="YYYY-MM-DD"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item> -->
                </a-col>
              </a-row>
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
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
                  <!-- <a-form-item label="开始日期">
                    <a-date-picker
                      v-model:value="testParams.startDate"
                      format="MM/DD/YYYY"
                      style="width: 100%"
                    />
                  </a-form-item> -->
                </a-col>
              </a-row>
            </template>

            <!-- GuidanceCrawler 参数 -->
            <template v-else-if="selectedCrawler.key === 'guidance'">
              <!-- Guidance爬虫不需要额外参数，默认爬取所有数据 -->
            </template>

            <!-- EU_CustomCase 参数 -->
            <template v-else-if="selectedCrawler.key === 'eu-custom-case'">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="TARIC编码">
                    <a-input
                      v-model:value="testParams.taricCode"
                      placeholder="如：9018"
                    />
                  </a-form-item>
                </a-col>
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="最大记录数">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.maxRecords"-->
<!--                      :min="-1"-->
<!--                      placeholder="-1表示爬取所有数据"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="批次大小">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.batchSize"-->
<!--                      :min="1"-->
<!--                      placeholder="如：100"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
              </a-row>
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
<!--                <a-col :span="24">-->
<!--                  <a-form-item label="TARIC编码设置">-->
<!--                    <a-space>-->
<!--                      <a-button -->
<!--                        :type="testParams.useKeywords ? 'primary' : 'default'"-->
<!--                        @click="testParams.useKeywords = !testParams.useKeywords"-->
<!--                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"-->
<!--                      >-->
<!--                        {{ testParams.useKeywords ? '已启用TARIC编码列表' : '使用TARIC编码列表' }}-->
<!--                      </a-button>-->
<!--                      <a-button type="link" size="small" @click="refreshTaricCodes" :loading="keywordLoading">-->
<!--                          <template #icon>-->
<!--                            <ReloadOutlined />-->
<!--                          </template>-->
<!--                          刷新TARIC编码列表-->
<!--                        </a-button>-->
<!--                    </a-space>-->
<!--                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">-->
<!--                      <a-tag color="blue">将使用所有 {{ taricCodeOptions.length }} 个TARIC编码</a-tag>-->
<!--                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">-->
<!--                        <a-tag v-for="code in taricCodeOptions" :key="code.value" style="margin: 2px;">-->
<!--                          {{ code.label }}-->
<!--                        </a-tag>-->
<!--                      </div>-->
<!--                    </div>-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
              </a-row>
            </template>

            <!-- EU_Guidance 参数 -->
            <template v-else-if="selectedCrawler.key === 'eu-guidance'">
              <a-row :gutter="16">
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="最大页数">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.maxPages"-->
<!--                      :min="0"-->
<!--                      placeholder="0表示爬取所有页"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="最大记录数">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.maxRecords"-->
<!--                      :min="-1"-->
<!--                      placeholder="-1表示爬取所有数据"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="批次大小">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.batchSize"-->
<!--                      :min="1"-->
<!--                      placeholder="如：100"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
              </a-row>
            </template>

            <!-- EU_Recall 参数 -->
            <template v-else-if="selectedCrawler.key === 'eu-recall'">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="搜索关键词">
                    <a-input
                      v-model:value="testParams.searchKeyword"
                      placeholder="如：medical device"
                    />
                  </a-form-item>
                </a-col>
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="最大记录数">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.maxRecords"-->
<!--                      :min="-1"-->
<!--                      placeholder="-1表示爬取所有数据"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="8">-->
<!--                  <a-form-item label="批次大小">-->
<!--                    <a-input-number-->
<!--                      v-model:value="testParams.batchSize"-->
<!--                      :min="1"-->
<!--                      placeholder="如：50"-->
<!--                      style="width: 100%"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--              </a-row>-->
<!--              <a-row :gutter="16">-->
<!--                <a-col :span="12">-->
<!--                  <a-form-item label="开始日期">-->
<!--                    <a-date-picker-->
<!--                      v-model:value="testParams.dateFrom"-->
<!--                      placeholder="yyyy-MM-dd"-->
<!--                      style="width: 100%"-->
<!--                      format="YYYY-MM-DD"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
<!--                <a-col :span="12">-->
<!--                  <a-form-item label="结束日期">-->
<!--                    <a-date-picker-->
<!--                      v-model:value="testParams.dateTo"-->
<!--                      placeholder="yyyy-MM-dd"-->
<!--                      style="width: 100%"-->
<!--                      format="YYYY-MM-DD"-->
<!--                    />-->
<!--                  </a-form-item>-->
<!--                </a-col>-->
              </a-row>
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
                  </a-form-item>
                </a-col>
              </a-row>
            </template>

            <!-- EU_Registration 参数 -->
            <template v-else-if="selectedCrawler.key === 'eu-registration'">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="关键词">
                    <a-input
                      v-model:value="testParams.inputKeyword"
                      placeholder="如：medical device"
                    />
                  </a-form-item>
                </a-col>
                <!-- <a-col :span="8">
                  <a-form-item label="最大记录数">
                    <a-input-number
                      v-model:value="testParams.maxRecords"
                      :min="-1"
                      placeholder="0或-1表示爬取所有数据"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col> -->
                <!-- <a-col :span="8">
                  <a-form-item label="批次大小">
                    <a-input-number
                      v-model:value="testParams.batchSize"
                      :min="1"
                      placeholder="如：50"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col> -->
              </a-row>
              <!-- <a-row :gutter="16">
                <a-col :span="12">
                  <a-form-item label="开始日期">
                    <a-date-picker
                      v-model:value="testParams.dateFrom"
                      placeholder="yyyy-MM-dd"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="结束日期">
                    <a-date-picker
                      v-model:value="testParams.dateTo"
                      placeholder="yyyy-MM-dd"
                      style="width: 100%"
                      format="YYYY-MM-DD"
                    />
                  </a-form-item>
                </a-col>
              </a-row> -->
              <!-- 关键词列表按钮 -->
              <a-row :gutter="16">
                <a-col :span="24">
                  <a-form-item label="关键词设置">
                    <a-space>
                      <a-button 
                        :type="testParams.useKeywords ? 'primary' : 'default'"
                        @click="testParams.useKeywords = !testParams.useKeywords"
                        :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                      >
                        {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                      </a-button>
                      <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                          <template #icon>
                            <ReloadOutlined />
                          </template>
                          刷新关键词列表
                        </a-button>
                    </a-space>
                    <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                      <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                      <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                        <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                          {{ keyword.label }}
                        </a-tag>
                      </div>
                    </div>
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
            <pre><code>{{ JSON.stringify(requestPayload, null, 2) }}</code></pre>
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
              <a-tab-pane key="summary" tab="爬取结果">
                <div class="crawl-summary">
                  <a-row :gutter="16">
                    <a-col :span="8">
                      <a-statistic
                        title="爬取状态"
                        :value="testResult.data?.success ? '成功' : '失败'"
                        :value-style="{ color: testResult.data?.success ? '#3f8600' : '#cf1322' }"
                      />
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="新增数据"
                        :value="testResult.data?.savedCount || 0"
                        :value-style="{ color: '#1890ff' }"
                        suffix="条"
                      />
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="重复数据"
                        :value="testResult.data?.skippedCount || 0"
                        :value-style="{ color: '#faad14' }"
                        suffix="条"
                      />
                    </a-col>
                  </a-row>
                  
                  <a-divider />
                  
                  <div class="result-message">
                    <a-alert
                      :type="testResult.data?.success ? 'success' : 'error'"
                      :message="testResult.data?.message || '无消息'"
                      :description="getResultDescription()"
                      show-icon
                    />
                  </div>
                  
                  <!-- 错误详情显示 -->
                  <div v-if="!testResult.data?.success && (testResult.data?.error || testResult.data?.errorDetails)" class="error-details">
                    <h5>错误详情：</h5>
                    <a-alert
                      type="error"
                      :message="testResult.data?.errorDetails || testResult.data?.error"
                      show-icon
                    />
                  </div>
                  
                  <div v-if="testResult.data?.databaseResult" class="database-result">
                    <h5>数据库保存详情：</h5>
                    <a-typography-text code>{{ testResult.data.databaseResult }}</a-typography-text>
                  </div>
                </div>
              </a-tab-pane>
              <a-tab-pane key="formatted" tab="完整响应">
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

    <!-- 参数爬取模态框 -->
    <a-modal
      v-model:open="testModalVisible"
      :title="`${selectedCrawler?.displayName || ''} - 参数化爬取`"
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
          <!-- US_510K 参数 -->
          <template v-if="selectedCrawler.key === 'us510k'">
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
            <a-form-item label="贸易名称">
              <a-input
                v-model:value="testParams.tradeName"
                placeholder="请输入贸易名称，如：Trade Name"
                allow-clear
              />
            </a-form-item>
          </template>

          <!-- US_event 参数 -->
          <template v-else-if="selectedCrawler.key === 'usevent'">
            <a-form-item label="设备名称">
              <a-input
                v-model:value="testParams.deviceName"
                placeholder="请输入设备名称，如：Pacemaker"
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
            <a-form-item label="产品问题">
              <a-input
                v-model:value="testParams.productProblem"
                placeholder="请输入产品问题，如：Product Problem"
                allow-clear
              />
            </a-form-item>
          </template>

          <!-- US_recall 参数 -->
          <template v-else-if="selectedCrawler.key === 'usrecall'">
            <a-form-item label="召回公司">
              <a-input
                v-model:value="testParams.recallingFirm"
                placeholder="请输入召回公司名称，如：Medtronic Inc"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="品牌名称">
              <a-input
                v-model:value="testParams.brandName"
                placeholder="请输入品牌名称，如：Medtronic"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="产品描述">
              <a-input
                v-model:value="testParams.productDescription"
                placeholder="请输入产品描述，如：Product Description"
                allow-clear
              />
            </a-form-item>
          </template>

          <!-- US_registration 参数 -->
          <template v-else-if="selectedCrawler.key === 'usregistration'">
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
            <!-- <a-form-item label="开始日期">
              <a-date-picker
                v-model:value="testParams.dateFrom"
                placeholder="YYYY-MM-DD"
                style="width: 100%"
                format="YYYY-MM-DD"
              />
            </a-form-item>
            <!-- <a-form-item label="结束日期">
              <a-date-picker
                v-model:value="testParams.dateTo"
                placeholder="YYYY-MM-DD"
                style="width: 100%"
                format="YYYY-MM-DD"
              />
            </a-form-item> -->
            <a-form-item label="关键词设置">
              <a-space>
                <a-button 
                  :type="testParams.useKeywords ? 'primary' : 'default'"
                  @click="testParams.useKeywords = !testParams.useKeywords"
                  :icon="testParams.useKeywords ? h(CheckOutlined) : h(PlusOutlined)"
                >
                  {{ testParams.useKeywords ? '已启用关键词列表' : '使用关键词列表' }}
                </a-button>
                <a-button type="link" size="small" @click="refreshKeywords" :loading="keywordLoading">
                  <template #icon>
                    <ReloadOutlined />
                  </template>
                  刷新关键词列表
                </a-button>
              </a-space>
              <div v-if="testParams.useKeywords" style="margin-top: 8px;">
                <a-tag color="blue">将使用所有 {{ keywordOptions.length }} 个关键词</a-tag>
                <div style="max-height: 150px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px;">
                  <a-tag v-for="keyword in keywordOptions" :key="keyword.value" style="margin: 2px;">
                    {{ keyword.label }}
                  </a-tag>
                </div>
              </div>
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
                    </template>

          <!-- GuidanceCrawler 参数 -->
          <template v-else-if="selectedCrawler.key === 'guidance'">
            <!-- Guidance爬虫不需要额外参数，默认爬取所有数据 -->
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
              执行爬取
                  </a-button>
                </a-space>
              </div>
              </div>
    </a-modal>

    <!-- 爬取结果模态框 -->
    <a-modal
      v-model:open="testResultModalVisible"
      title="爬虫爬取结果"
      width="1000px"
      :footer="null"
    >
      <div v-if="currentTestResult" class="test-result-content">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="爬虫名称">
            {{ currentTestResult.crawlerName }}
          </a-descriptions-item>
          <a-descriptions-item label="爬取状态">
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
          <a-descriptions-item label="爬取时间">
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
          <a-descriptions-item label="爬取端点">
            {{ selectedCrawler.testEndpoint }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(selectedCrawler.status)">
              {{ getStatusText(selectedCrawler.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="最后爬取">
            {{ selectedCrawler.lastTest || '未爬取' }}
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

        <!-- 欧盟爬虫管理标签页 -->
        <a-tab-pane key="eu-crawlers" tab="欧盟爬虫">
          <template #tab>
            <span>
              🇪🇺 欧盟爬虫
            </span>
          </template>

          <!-- 欧盟爬虫管理 -->
          <div class="eu-crawler-section">
            <a-card :title="`🇪🇺 欧盟爬虫 (${euCrawlers.length}个)`" :bordered="false" class="country-card">
              <template #extra>
                <a-space>
                  <a-tag color="blue">运行中: {{ euRunningCount }}</a-tag>
                  <a-tag color="green">可用: {{ euAvailableCount }}</a-tag>
                  <a-tag color="red">停止: {{ euStoppedCount }}</a-tag>
                </a-space>
              </template>

              <!-- 欧盟爬虫列表 -->
              <div class="crawler-list">
                <div 
                  v-for="crawler in euCrawlers" 
                  :key="crawler.key"
                  class="crawler-list-item"
                  :class="{ 
                    'running': crawler.status === 'running', 
                    'selected': selectedEUCrawlers.includes(crawler.key),
                    'testing': crawler.testing,
                    'expanded': expandedEUCrawlers.includes(crawler.key)
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
                          :checked="selectedEUCrawlers.includes(crawler.key)"
                          @change="(e: any) => handleEUCrawlerSelect(crawler.key, e.target.checked)"
                        />
                      </div>
                      <div class="test-actions">
                        <a-button
                          type="primary"
                          size="small"
                          @click="showEUTestInterface(crawler)"
                          :loading="crawler.testing"
                          :disabled="crawler.testing"
                        >
                          <template #icon>
                            <BugOutlined />
                          </template>
                          爬取
                        </a-button>
                      </div>
                    </div>
                  </div>

                  <!-- 加载遮罩 -->
                  <div v-if="crawler.testing" class="loading-overlay">
                    <a-spin size="large" />
                    <span class="loading-text">爬取中...</span>
                  </div>
                </div>
              </div>

              <!-- 批量操作 -->
              <div class="batch-actions" v-if="selectedEUCrawlers.length > 0">
                <a-alert 
                  :message="`已选择 ${selectedEUCrawlers.length} 个欧盟爬虫`"
                  type="info"
                  show-icon
                  style="margin-bottom: 16px"
                />
                <a-space>
                  <a-button @click="batchEUQuickTest" :loading="batchEUTestLoading">
                    <template #icon>
                      <ThunderboltOutlined />
                    </template>
                    批量快速爬取
                  </a-button>
                  <a-button @click="clearEUSelection">
                    <template #icon>
                      <ClearOutlined />
                    </template>
                    清除选择
                  </a-button>
                </a-space>
              </div>
            </a-card>
          </div>
        </a-tab-pane>

        <!-- 关键词管理标签页 -->
        <a-tab-pane key="keywords" tab="搜索关键词列表">
          <template #tab>
            <span>
              <SearchOutlined />
              关键词管理
            </span>
          </template>

          <!-- 关键词管理内容 -->
          <div class="keyword-management-section">
            <!-- 简化的统计信息 -->
            <div class="stats-section">
              <div class="simple-stats">
                <span class="stat-item">
                  <FileTextOutlined />
                  共 {{ keywords.length }} 个关键词
                </span>
                <span class="stat-item">
                  <BarChartOutlined />
                  平均长度 {{ averageKeywordLength.toFixed(1) }} 字符
                </span>
              </div>
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
                      <span class="keyword-text">{{ keyword }}</span>
                      <span class="keyword-length">{{ keyword.length }}字</span>
                    </div>
                    <div class="keyword-actions">
                      <a-button 
                        size="small" 
                        @click="editKeyword(getOriginalKeywordIndex(index), keyword)"
                        type="text"
                        class="action-btn"
                      >
                        <EditOutlined />
                      </a-button>
                      <a-button 
                        size="small" 
                        @click="deleteKeyword(getOriginalKeywordIndex(index), keyword)"
                        type="text"
                        danger
                        class="action-btn"
                      >
                        <DeleteOutlined />
                      </a-button>
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
import { ref, onMounted, computed, h } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  BugOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FileTextOutlined,
  BarChartOutlined,
  CheckOutlined
} from '@ant-design/icons-vue';
import { PerformanceOptimizer } from '@/utils/performanceOptimizer';

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

// 关键词管理相关 - 改为前端本地管理
const keywordLoading = ref(false);
const addKeywordLoading = ref(false);
const editKeywordLoading = ref(false);
const batchKeywordLoading = ref(false);
// 初始化关键词列表
const keywords = ref<string[]>([
  'Skin', 'Analyzer', '3D', 'AI', 'Facial', 'Detector', 'Scanner', 'Spectra', 
  'Skin Analysis', 'Skin Scanner', 'skin imaging', 'Facial Imaging', 'pigmentation', 
  'skin elasticity', 'visia', 'PSI', 'PIE', 'ISEMECO', 'OBSERV', 'AURA', 'canfield'
]);
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

// TARIC编码选项（用于EU_CustomCase）
const taricCodeOptions = computed(() => {
  const taricCodes = ['9018','8543','9031.49','9031.49','525'];
  return taricCodes.map(code => ({
    label: code,
    value: code
  }));
});

// 爬取参数
const testParams = ref({
  // 通用参数 - 默认爬取所有数据
  maxRecords: -1,  // -1表示爬取所有记录
  batchSize: 100,  // 使用较大的批次大小提高效率
  dateFrom: null,
  dateTo: null,
  totalCount: 50,
  hsCode: '',
  startDate: null,
  maxPages: 0,     // 0表示爬取所有页
  useKeywords: false, // 是否使用关键词列表
  
  // US_510K 专用参数
  deviceName: '',
  applicantName: '',
  tradeName: '',
  
  // US_event 专用参数
  manufacturer: '',
  productProblem: '',
  
  // US_recall 专用参数
  recallingFirm: '',
  brandName: '',
  productDescription: '',
  
  // US_registration 专用参数
  establishmentName: '',
  proprietaryName: '',
  ownerOperatorName: ''
});


// 美国爬虫配置
const usaCrawlers = ref([
  {
    key: 'us510k',
    displayName: 'US_510K - FDA 510K设备',
    className: 'com.certification.crawler.countrydata.us.US_510K',
    entity: 'Device510K',
    description: 'FDA 510K设备审批数据爬虫，用于获取FDA 510K设备审批信息',
    testEndpoint: '/api/us-crawler/test/us510k',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      deviceName: '',
      applicantName: '',
      tradeName: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 0,  // 0表示爬取所有页
      maxRecords: -1,  // -1表示爬取所有记录
      batchSize: 100  // 使用较大的批次大小
    }
  },
  {
    key: 'usevent',
    displayName: 'US_event - FDA事件报告',
    className: 'com.certification.crawler.countrydata.us.US_event_api',
    entity: 'DeviceEventReport',
    description: 'FDA设备不良事件数据爬虫，用于获取FDA设备不良事件信息',
    testEndpoint: '/api/us-crawler/execute/usevent',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      deviceName: '',
      manufacturer: '',
      productProblem: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 0,  // 0表示爬取所有页
      maxRecords: -1,  // -1表示爬取所有记录
      batchSize: 100  // 使用较大的批次大小
    }
  },
  {
    key: 'usrecall',
    displayName: 'US_recall - FDA召回数据',
    className: 'com.certification.crawler.countrydata.us.US_recall_api',
    entity: 'DeviceRecallRecord',
    description: 'FDA设备召回数据爬虫，用于获取FDA设备召回信息',
    testEndpoint: '/api/us-crawler/test/usrecall',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      recallingFirm: '',
      brandName: '',
      productDescription: '',
      dateFrom: null,
      dateTo: null,
      maxPages: 0,  // 0表示爬取所有页
      maxRecords: -1,  // -1表示爬取所有记录
      batchSize: 100  // 使用较大的批次大小
    }
  },
  {
    key: 'usregistration',
    displayName: 'US_registration - FDA注册数据',
    className: 'com.certification.crawler.countrydata.us.US_registration',
    entity: 'DeviceRegistrationRecord',
    description: 'FDA设备注册信息爬虫，用于获取FDA设备注册信息',
    testEndpoint: '/api/us-crawler/test/usregistration',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      establishmentName: '',
      proprietaryName: '',
      ownerOperatorName: '',
      maxPages: 0,  // 0表示爬取所有页
      maxRecords: -1,  // -1表示爬取所有记录
      batchSize: 100  // 使用较大的批次大小
    }
  },
  {
    key: 'customs-case',
    displayName: 'CustomsCaseCrawler - 海关案例',
    className: 'com.certification.crawler.countrydata.us.CustomsCaseCrawler',
    entity: 'CustomsCase',
    description: 'CBP海关裁定数据爬虫，用于获取美国海关与边境保护局裁定信息',
    testEndpoint: '/api/us-crawler/test/customs-case',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      hsCode: '9018',
      maxRecords: -1,  // -1表示爬取所有记录
      batchSize: 100,  // 使用较大的批次大小
      startDate: null,
      maxPages: 0  // 0表示爬取所有页
    }
  },
  {
    key: 'guidance',
    displayName: 'GuidanceCrawler - 指导文档',
    className: 'com.certification.crawler.countrydata.us.GuidanceCrawler',
    entity: 'GuidanceDocument',
    description: 'FDA指导文档爬虫，用于获取FDA医疗设备指导文档',
    testEndpoint: '/api/us-crawler/test/guidance',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      maxRecords: -1,  // -1表示爬取所有记录
      maxPages: 0,     // 0表示爬取所有页
      batchSize: 100   // 使用较大的批次大小
    }
  }
]);

// 欧盟爬虫配置
const euCrawlers = ref([
  {
    key: 'eu-custom-case',
    displayName: 'EU_CustomCase - 欧盟海关案例',
    className: 'com.certification.crawler.countrydata.eu.Eu_customcase',
    entity: 'CustomsCase',
    description: '欧盟TARIC编码关税措施数据爬虫，用于获取欧盟海关与边境保护局裁定信息',
    testEndpoint: '/api/eu-crawler/test/eu-custom-case',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      taricCode: '9018',              // TARIC编码，默认9018
      maxRecords: -1,                 // -1表示爬取所有记录
      batchSize: 100,                 // 批次大小
      useKeywords: false              // 是否使用关键词列表
    }
  },
  {
    key: 'eu-guidance',
    displayName: 'EU_Guidance - 欧盟指导文档',
    className: 'com.certification.crawler.countrydata.eu.Eu_guidance',
    entity: 'GuidanceDocument',
    description: '欧盟医疗设备最新更新新闻爬虫，用于获取欧盟医疗设备指导文档',
    testEndpoint: '/api/eu-crawler/test/eu-guidance',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      maxRecords: -1,  // -1表示爬取所有记录
      maxPages: 0,     // 0表示爬取所有页
      batchSize: 100   // 使用较大的批次大小
    }
  },
  {
    key: 'eu-recall',
    displayName: 'EU_Recall - 欧盟召回数据',
    className: 'com.certification.crawler.countrydata.eu.Eu_recall',
    entity: 'DeviceRecallRecord',
    description: '欧盟设备召回数据爬虫，用于获取欧盟设备召回信息',
    testEndpoint: '/api/eu-crawler/test/eu-recall',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      searchKeyword: 'medical device',                           // 搜索关键词
      maxRecords: -1,                                            // -1表示爬取所有记录
      batchSize: 50,                                             // 批次大小
      dateFrom: '',                                              // 开始日期
      dateTo: '',                                                // 结束日期
      useKeywords: false                                         // 是否使用关键词列表
    }
  },
  {
    key: 'eu-registration',
    displayName: 'EU_Registration - 欧盟注册数据',
    className: 'com.certification.crawler.countrydata.eu.Eu_registration',
    entity: 'DeviceRegistrationRecord',
    description: '欧盟设备注册信息爬虫，用于获取欧盟设备注册信息',
    testEndpoint: '/api/eu-crawler/test/eu-registration',
    status: 'available',
    lastTest: null,
    testing: false,
    testParams: {
      inputKeyword: 'medical device',                            // 关键词
      maxRecords: 100,                                          // 最大记录数，默认100
      batchSize: 50,                                            // 批次大小，默认50
      dateFrom: '',                                             // 开始日期
      dateTo: '',                                               // 结束日期
      useKeywords: false                                        // 是否使用关键词列表
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

// 欧盟爬虫计算属性
const euRunningCount = computed(() => 
  euCrawlers.value.filter(c => c.status === 'running').length
);

const euStoppedCount = computed(() => 
  euCrawlers.value.filter(c => c.status === 'stopped').length
);

const euAvailableCount = computed(() => 
  euCrawlers.value.filter(c => c.status === 'available').length
);

// 欧盟爬虫状态管理
const selectedEUCrawlers = ref<string[]>([]);
const expandedEUCrawlers = ref<string[]>([]);
const batchEUTestLoading = ref(false);

// 欧盟爬虫选择处理
const handleEUCrawlerSelect = (crawlerKey: string, checked: boolean) => {
  if (checked) {
    selectedEUCrawlers.value.push(crawlerKey);
  } else {
    const index = selectedEUCrawlers.value.indexOf(crawlerKey);
    if (index > -1) {
      selectedEUCrawlers.value.splice(index, 1);
    }
  }
};

// 清除欧盟爬虫选择
const clearEUSelection = () => {
  selectedEUCrawlers.value = [];
};

// 显示欧盟爬虫测试界面
const showEUTestInterface = (crawler: any) => {
  selectedCrawler.value = crawler;
  testInterfaceVisible.value = true;
};

// 批量欧盟爬虫快速测试
const batchEUQuickTest = async () => {
  if (selectedEUCrawlers.value.length === 0) {
    message.warning('请先选择要测试的欧盟爬虫');
    return;
  }

  batchEUTestLoading.value = true;
  
  try {
    const promises = selectedEUCrawlers.value.map(crawlerKey => {
      const crawler = euCrawlers.value.find(c => c.key === crawlerKey);
      if (crawler) {
        return testEUCrawler(crawler);
      }
      return Promise.resolve();
    });

    await Promise.all(promises);
    message.success(`批量测试完成，共测试 ${selectedEUCrawlers.value.length} 个欧盟爬虫`);
  } catch (error: any) {
    message.error(`批量测试失败: ${error.message}`);
  } finally {
    batchEUTestLoading.value = false;
  }
};

// 刷新TARIC编码列表
const refreshTaricCodes = async () => {
  keywordLoading.value = true;
  try {
    // 模拟刷新TARIC编码列表
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success('TARIC编码列表已刷新');
  } catch (error) {
    message.error('刷新TARIC编码列表失败');
  } finally {
    keywordLoading.value = false;
  }
};

// 欧盟爬虫测试方法
const testEUCrawler = async (crawler: any) => {
  crawler.testing = true;
  crawler.status = 'running';
  
  try {
    // 根据useKeywords标志选择API端点
    let endpoint = crawler.testEndpoint;
    let params = { ...crawler.testParams };
    
    if (crawler.testParams.useKeywords) {
      // 使用关键词列表模式
      switch (crawler.key) {
        case 'eu-custom-case':
          endpoint = '/api/eu-crawler/test/eu-custom-case/batch';
          params = {
            taricCodes: taricCodeOptions.value.map(code => code.value).join(','), // 使用TARIC编码选项
            maxRecords: crawler.testParams.maxRecords,
            batchSize: crawler.testParams.batchSize
          };
          break;
        case 'eu-recall':
          endpoint = '/api/eu-crawler/test/eu-recall/batch';
          params = {
            searchKeywords: keywordOptions.value.map(keyword => keyword.value).join(','), // 使用关键词选项
            maxRecords: crawler.testParams.maxRecords,
            batchSize: crawler.testParams.batchSize,
            dateFrom: crawler.testParams.dateFrom,
            dateTo: crawler.testParams.dateTo
          };
          break;
        case 'eu-registration':
          endpoint = '/api/eu-crawler/test/eu-registration/batch';
          params = {
            inputKeywords: keywordOptions.value.map(keyword => keyword.value).join(','), // 使用关键词选项
            maxRecords: crawler.testParams.maxRecords,
            batchSize: crawler.testParams.batchSize,
            dateFrom: crawler.testParams.dateFrom,
            dateTo: crawler.testParams.dateTo
          };
          break;
      }
    } else {
      // 单个爬取模式，使用原始参数
      switch (crawler.key) {
        case 'eu-custom-case':
          params = {
            taricCode: crawler.testParams.taricCode,
            maxRecords: crawler.testParams.maxRecords,
            batchSize: crawler.testParams.batchSize
          };
          break;
        case 'eu-recall':
          params = {
            maxPages: 5,
            searchKeyword: crawler.testParams.searchKeyword,
            sortField: '',
            sortDirection: '',
            language: '',
            productCategories: ''
          };
          break;
        case 'eu-registration':
          params = {
            inputKeywords: crawler.testParams.inputKeyword,
            maxRecords: crawler.testParams.maxRecords,
            batchSize: crawler.testParams.batchSize,
            dateFrom: crawler.testParams.dateFrom,
            dateTo: crawler.testParams.dateTo
          };
          break;
      }
    }
    
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams(params)
    });
    
    const result = await response.json();
    
    crawler.lastTest = {
      success: result.success,
      message: result.message,
      savedCount: result.savedCount || 0,
      skippedCount: result.skippedCount || 0,
      totalProcessed: result.totalProcessed || 0,
      timestamp: new Date(),
      isAllDuplicate: result.isAllDuplicate || false,
      error: result.error || null,
      errorDetails: result.errorDetails || null
    };
    
    crawler.status = result.success ? 'available' : 'stopped';
    
    if (result.success) {
      if (result.isAllDuplicate) {
        message.warning(`${crawler.displayName} 爬取完成，但没有数据更新。`);
      } else {
        message.success(`${crawler.displayName} 测试成功！`);
      }
    } else {
      const errorMsg = result.errorDetails || result.error || result.message;
      message.error(`${crawler.displayName} 测试失败: ${errorMsg}`);
    }
    
  } catch (error: any) {
    console.error('测试欧盟爬虫失败:', error);
    crawler.lastTest = {
      success: false,
      message: `网络错误: ${error.message}`,
      savedCount: 0,
      skippedCount: 0,
      totalProcessed: 0,
      timestamp: new Date()
    };
    crawler.status = 'stopped';
    message.error(`${crawler.displayName} 测试失败: ${error.message}`);
  } finally {
    crawler.testing = false;
  }
};


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

// 移除了未使用的计算属性

// 方法
const showTestInterface = (crawler: any) => {
  selectedCrawler.value = crawler;
  resetTestParams();
  testResult.value = null;
  testInterfaceVisible.value = true;
};

// 将getRequestPayload改为计算属性，避免重复执行
const requestPayload = computed(() => {
  if (!selectedCrawler.value) return {};
  
  const params: any = {};
  
  // 处理关键词参数
  if (testParams.value.useKeywords) {
    // 使用所有可用的关键词列表，转换为逗号分隔的字符串（后端期望格式）
    const allKeywords = keywordOptions.value.map(option => option.value);
    params['inputKeywords'] = allKeywords.join(', ');
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
});

// 保留原函数用于执行爬取时调用
const getRequestPayload = () => {
  console.log('是否使用关键词:', testParams.value.useKeywords);
  console.log('关键词选项数量:', keywordOptions.value.length);
  console.log('关键词选项:', keywordOptions.value);
  
  const payload = requestPayload.value;
  
  if (testParams.value.useKeywords) {
    console.log('使用关键词列表，数量:', keywordOptions.value.length);
    console.log('转换后:', payload.inputKeywords);
  } else {
    console.log('不使用关键词列表');
  }
  
  return payload;
};

const executeTest = async () => {
  if (!selectedCrawler.value) return;
  
  testExecuting.value = true;
  const startTime = Date.now();
  
  try {
    const payload = getRequestPayload();
    console.log(`开始执行爬取: ${selectedCrawler.value.displayName}`, payload);
    console.log('payload.inputKeywords:', payload.inputKeywords);
    
    // 所有接口都使用POST请求发送JSON数据
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 300000); // 5分钟超时
    
    const response = await fetch(`http://localhost:8080${selectedCrawler.value.testEndpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      body: JSON.stringify(payload),
      signal: controller.signal
      });
    
    clearTimeout(timeoutId);
    
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    console.log('响应状态:', response.status);
    console.log('响应头:', Object.fromEntries(response.headers.entries()));
    
    let responseText;
    let result;
    
    try {
      // 尝试获取响应文本
      responseText = await response.text();
      console.log('原始响应文本:', responseText);
      
      // 尝试解析JSON
      result = JSON.parse(responseText);
      console.log('解析后的结果:', result);
    } catch (parseError: any) {
      console.error('JSON解析失败:', parseError);
      console.error('响应文本:', responseText);
      result = { 
        success: false, 
        message: '响应解析失败', 
        rawResponse: responseText,
        parseError: parseError.toString()
      };
    }
    
    const dataSize = new Blob([responseText || '']).size;
    
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
      const savedCount = result.savedCount || 0;
      const skippedCount = result.skippedCount || 0;
      
      if (savedCount === 0 && skippedCount > 0) {
        message.warning(`${selectedCrawler.value.displayName} 爬取完成 - 没有新数据，所有 ${skippedCount} 条都是重复数据`);
      } else if (savedCount > 0) {
        message.success(`${selectedCrawler.value.displayName} 爬取成功 - 新增 ${savedCount} 条数据${skippedCount > 0 ? `，跳过 ${skippedCount} 条重复数据` : ''}`);
    } else {
        message.info(`${selectedCrawler.value.displayName} 爬取完成 - 没有数据更新`);
      }
    } else {
      message.error(`${selectedCrawler.value.displayName} 爬取失败: ${result.message || result.error || '未知错误'}`);
    }
    
  } catch (error: any) {
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    console.error(`爬取失败: ${selectedCrawler.value.displayName}`, error);
    console.error('错误类型:', error.name);
    console.error('错误消息:', error.message);
    console.error('错误堆栈:', error.stack);
    
    let errorMessage = '未知错误';
    if (error.name === 'AbortError') {
      errorMessage = '请求超时（5分钟）';
    } else if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
      errorMessage = '网络连接失败，请检查后端服务是否运行';
    } else {
      errorMessage = error.message || error.toString();
    }
    
    testResult.value = {
      success: false,
      status: 0,
      responseTime,
      dataSize: 0,
      data: { 
        error: errorMessage,
        errorType: error.name,
        errorDetails: error.toString()
      },
      rawResponse: error.toString(),
      timestamp: new Date().toLocaleString()
    };
    
    message.error(`${selectedCrawler.value.displayName} 爬取失败: ${errorMessage}`);
  } finally {
    testExecuting.value = false;
  }
};

// 移除了未使用的resetCrawlerParams函数


const resetTestParams = () => {
  testParams.value = {
    // 通用参数 - 默认爬取所有数据
    maxRecords: -1,  // -1表示爬取所有记录
    batchSize: 100,  // 使用较大的批次大小提高效率
    dateFrom: null,
    dateTo: null,
    totalCount: 50,
    hsCode: '9018',
    startDate: null,
    maxPages: 0,     // 0表示爬取所有页
    useKeywords: false,
    
    // US_510K 专用参数
    deviceName: '',
    applicantName: '',
    tradeName: '',
    
    // US_event 专用参数
    manufacturer: '',
    productProblem: '',
    
    // US_recall 专用参数
    recallingFirm: '',
    brandName: '',
    productDescription: '',
    
    // US_registration 专用参数
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
    
    console.log(`开始执行参数化爬取: ${crawler.displayName}`, params);
    
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
      message: result.message || '爬取完成',
      totalSaved: result.totalSaved || 0,
      totalSkipped: result.totalSkipped || 0,
      totalPages: result.totalPages || 0,
      testTime: new Date().toLocaleString(),
      details: result
    };
    
    // 更新爬虫状态
    crawler.lastTest = testResult.testTime;
    crawler.status = result.success ? 'available' : 'stopped';
    
    // 显示爬取结果
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    if (result.success) {
      message.success(`${crawler.displayName} 参数化爬取成功`);
    } else {
      message.error(`${crawler.displayName} 参数化爬取失败: ${result.message}`);
    }
    
  } catch (error: any) {
    console.error(`参数化爬取失败: ${crawler.displayName}`, error);
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: false,
      message: `参数化爬取失败: ${error}`,
      totalSaved: 0,
      totalSkipped: 0,
      totalPages: 0,
      testTime: new Date().toLocaleString(),
      details: { error: error.toString() }
    };
    
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    message.error(`${crawler.displayName} 参数化爬取失败`);
  } finally {
    crawler.testing = false;
  }
};

const quickTest = async (crawler: any) => {
  crawler.testing = true;
  
  try {
    console.log(`开始快速爬取: ${crawler.displayName}`);
    
    // 使用默认参数进行快速爬取
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
      message: result.message || '快速爬取完成',
      totalSaved: result.totalSaved || 0,
      totalSkipped: result.totalSkipped || 0,
      totalPages: result.totalPages || 0,
      testTime: new Date().toLocaleString(),
      details: result
    };
    
    // 更新爬虫状态
    crawler.lastTest = testResult.testTime;
    crawler.status = result.success ? 'available' : 'stopped';
    
    // 显示爬取结果
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;

    if (result.success) {
      message.success(`${crawler.displayName} 快速爬取成功`);
      } else {
      message.error(`${crawler.displayName} 快速爬取失败: ${result.message}`);
    }
    
  } catch (error: any) {
    console.error(`快速爬取失败: ${crawler.displayName}`, error);
    
    const testResult = {
      crawlerName: crawler.displayName,
      success: false,
      message: `快速爬取失败: ${error}`,
      totalSaved: 0,
      totalSkipped: 0,
      totalPages: 0,
      testTime: new Date().toLocaleString(),
      details: { error: error.toString() }
    };
    
    currentTestResult.value = testResult;
    testResultModalVisible.value = true;
    
    message.error(`${crawler.displayName} 快速爬取失败`);
  } finally {
    crawler.testing = false;
  }
};

const getDefaultParams = (crawlerKey: string) => {
  // 使用默认的全量爬取参数
  const defaultMaxPages = 0;  // 0表示爬取所有页
  const defaultBatchSize = 100;  // 使用较大的批次大小
  const defaultMaxRecords = -1;  // -1表示爬取所有记录
  
  switch (crawlerKey) {
    case 'us510k':
      return { maxPages: defaultMaxPages, useKeywords: false };
    case 'usevent':
      return { maxPages: defaultMaxPages, useKeywords: false };
    case 'usrecall':
      return { maxPages: defaultMaxPages, useKeywords: false };
    case 'usregistration':
      return { maxPages: defaultMaxPages, useKeywords: false };
    case 'customs-case':
      return { 
        hsCode: '9018', 
        maxRecords: defaultMaxRecords, // 使用全量爬取
        batchSize: defaultBatchSize, 
        useKeywords: false 
      };
    case 'guidance':
      return { maxRecords: defaultMaxRecords, useKeywords: false }; // 使用全量爬取
    default:
      return { useKeywords: false };
  }
};

const testAllCrawlers = async () => {
  testAllLoading.value = true;
  
  try {
    console.log('🚀 开始批量爬取所有爬虫...')
    
    // 使用Promise.allSettled来避免单个失败影响整体
    const promises = usaCrawlers.value.map(crawler => quickTest(crawler));
    const results = await Promise.allSettled(promises);
    
    // 统计结果
    const successCount = results.filter(result => result.status === 'fulfilled').length
    const failedCount = results.filter(result => result.status === 'rejected').length
    
    console.log(`✅ 批量爬取完成: 成功 ${successCount}, 失败 ${failedCount}`)
    
    if (failedCount === 0) {
      message.success(`所有爬虫快速爬取完成！成功: ${successCount}/${usaCrawlers.value.length}`);
    } else {
      message.warning(`批量爬取完成！成功: ${successCount}, 失败: ${failedCount}`);
    }
    
  } catch (error) {
    console.error('批量爬取失败:', error);
    message.error('批量爬取失败');
  } finally {
    testAllLoading.value = false;
  }
};

const batchQuickTest = async () => {
  if (selectedCrawlers.value.length === 0) {
    message.warning('请选择要爬取的爬虫');
    return;
  }
  
  batchTestLoading.value = true;
  
  try {
    console.log(`🚀 开始批量爬取选中的 ${selectedCrawlers.value.length} 个爬虫...`)
    
    const selectedCrawlerObjects = usaCrawlers.value.filter(c => 
      selectedCrawlers.value.includes(c.key)
    );
    
    // 使用Promise.allSettled来避免单个失败影响整体
    const promises = selectedCrawlerObjects.map(crawler => quickTest(crawler));
    const results = await Promise.allSettled(promises);
    
    // 统计结果
    const successCount = results.filter(result => result.status === 'fulfilled').length
    const failedCount = results.filter(result => result.status === 'rejected').length
    
    console.log(`✅ 批量爬取完成: 成功 ${successCount}, 失败 ${failedCount}`)
    
    if (failedCount === 0) {
      message.success(`批量快速爬取完成！共爬取 ${selectedCrawlers.value.length} 个爬虫，全部成功`);
    } else {
      message.warning(`批量快速爬取完成！成功: ${successCount}, 失败: ${failedCount}`);
    }
    
    clearSelection();
    
  } catch (error) {
    console.error('批量爬取失败:', error);
    message.error('批量爬取失败');
  } finally {
    batchTestLoading.value = false;
  }
};

// 节流刷新状态函数
const throttledRefreshStatus = PerformanceOptimizer.throttle(async () => {
  console.log('🔄 执行状态刷新...')
  // 这里可以调用状态检查API
  await new Promise(resolve => setTimeout(resolve, 500)); // 减少延迟
  message.success('状态刷新完成');
}, 2000) // 2秒内只能执行一次

const refreshAllStatus = async () => {
  refreshLoading.value = true;
  
  try {
    await throttledRefreshStatus();
  } catch (error) {
    console.error('刷新状态失败:', error);
    message.error('刷新状态失败');
  } finally {
    refreshLoading.value = false;
  }
};

// 移除了未使用的viewCrawlerDetails函数

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

// 关键词管理方法 - 优化版本
const refreshKeywords = async () => {
  // 检查缓存
  const cacheKey = 'crawler-keywords'
  const cachedData = PerformanceOptimizer.getCache(cacheKey)
  if (cachedData) {
    console.log('📊 使用缓存的关键词数据')
    keywords.value = cachedData
    message.success(`成功加载 ${keywords.value.length} 个关键词（来自缓存）`)
    return
  }

  keywordLoading.value = true;
  try {
    console.log('🔄 开始刷新关键词数据...')
    // 模拟加载延迟，但减少时间
    await new Promise(resolve => setTimeout(resolve, 200));
    
    // 缓存关键词数据
    PerformanceOptimizer.setCache(cacheKey, keywords.value, 10 * 60 * 1000) // 10分钟缓存
    
    message.success(`成功加载 ${keywords.value.length} 个关键词`)
    console.log('✅ 关键词数据刷新完成')
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
    // 检查是否已存在
    if (keywords.value.includes(newKeyword.value.keyword.trim())) {
      message.warning('关键词已存在');
      return;
    }
    
    // 添加到本地列表
    keywords.value.push(newKeyword.value.keyword.trim());
    message.success('关键词添加成功');
    addKeywordModalVisible.value = false;
    newKeyword.value.keyword = '';
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
    // 检查是否已存在（排除当前编辑的关键词）
    const trimmedKeyword = editingKeyword.value.keyword.trim();
    const existingIndex = keywords.value.findIndex((k, i) => k === trimmedKeyword && i !== editingKeyword.value.index);
    if (existingIndex !== -1) {
      message.warning('关键词已存在');
      return;
    }
    
    // 更新本地列表
    keywords.value[editingKeyword.value.index] = trimmedKeyword;
    message.success('关键词更新成功');
    editKeywordModalVisible.value = false;
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
        // 从本地列表删除
        keywords.value.splice(index, 1);
        message.success('关键词删除成功');
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
    console.log('🔄 开始批量更新关键词...')
    
    // 去重处理
    const uniqueKeywords = [...new Set(validKeywords)];
    
    // 批量处理关键词
    PerformanceOptimizer.batchProcess(uniqueKeywords, 50, (batch) => {
      console.log(`处理关键词批次: ${batch.length} 个`)
    });
    
    // 更新本地列表
    keywords.value = uniqueKeywords;
    
    // 清除缓存，因为数据已更新
    PerformanceOptimizer.clearCache('crawler-keywords')
    
    message.success(`批量更新成功，共 ${uniqueKeywords.length} 个关键词`);
    batchKeywordModalVisible.value = false;
    
    console.log('✅ 批量关键词更新完成')
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
        console.log('🗑️ 开始清空所有关键词...')
        
        // 清空本地列表
        keywords.value = [];
        
        // 清除相关缓存
        PerformanceOptimizer.clearCache('crawler-keywords')
        
        message.success('所有关键词已清空');
        console.log('✅ 关键词清空完成')
      } catch (error: any) {
        console.error('清空关键词失败:', error);
        message.error('清空关键词失败: ' + error.message);
      }
    }
  });
};

// 防抖搜索函数
const debouncedKeywordSearch = PerformanceOptimizer.debounce(() => {
  console.log('🔍 执行关键词搜索:', keywordSearchText.value)
}, 300)

const handleKeywordSearch = () => {
  // 使用防抖搜索
  debouncedKeywordSearch()
};

const getOriginalKeywordIndex = (filteredIndex: number) => {
  const filteredKeyword = filteredKeywords.value[filteredIndex];
  return keywords.value.indexOf(filteredKeyword);
};

// 获取爬取结果描述
const getResultDescription = () => {
  if (!testResult.value?.data) return '';
  
  const data = testResult.value.data;
  const savedCount = data.savedCount || 0;
  const skippedCount = data.skippedCount || 0;
  const totalProcessed = data.totalProcessed || (savedCount + skippedCount);
  
  if (data.success) {
    if (savedCount === 0 && skippedCount > 0) {
      return `没有发现新数据，所有 ${skippedCount} 条数据都是重复的。`;
    } else if (savedCount > 0 && skippedCount > 0) {
      return `成功处理 ${totalProcessed} 条数据，其中新增 ${savedCount} 条，跳过重复 ${skippedCount} 条。`;
    } else if (savedCount > 0 && skippedCount === 0) {
      return `成功新增 ${savedCount} 条数据，没有重复数据。`;
    } else {
      return '爬取完成，但没有数据更新。';
    }
  } else {
    return data.error || '爬取过程中发生未知错误。';
  }
};



// 生命周期
onMounted(async () => {
  console.log('🚀 美国爬虫管理系统初始化完成');
  
  // 所有爬虫默认爬取所有数据
  console.log('✅ 已设置默认全量爬取参数:', {
    maxPages: testParams.value.maxPages,
    batchSize: testParams.value.batchSize,
    maxRecords: testParams.value.maxRecords
  });
  
  // 初始化关键词缓存
  const cacheKey = 'crawler-keywords'
  const cachedKeywords = PerformanceOptimizer.getCache(cacheKey)
  if (cachedKeywords) {
    console.log('📊 从缓存加载关键词数据')
    keywords.value = cachedKeywords
  } else {
    // 缓存初始关键词数据
    PerformanceOptimizer.setCache(cacheKey, keywords.value, 10 * 60 * 1000)
    console.log(`📝 初始化关键词列表，共 ${keywords.value.length} 个关键词`)
  }
  
  console.log('✅ 爬虫管理系统初始化完成')
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

.icon-wrapper.us510k::before {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-wrapper.usevent::before {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.icon-wrapper.usrecall::before {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.icon-wrapper.usregistration::before {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.icon-wrapper.customs-case::before {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.icon-wrapper.guidance::before {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

/* 欧盟爬虫图标样式 */
.icon-wrapper.eu-custom-case::before {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-wrapper.eu-guidance::before {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.icon-wrapper.eu-recall::before {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.icon-wrapper.eu-registration::before {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
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

/* 简化的关键词管理样式 */
.main-tabs {
  margin-top: 16px;
}

.keyword-management-section {
  padding: 16px 0;
}

.stats-section {
  margin-bottom: 20px;
}

.simple-stats {
  display: flex;
  gap: 24px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.stat-item .anticon {
  color: #1890ff;
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
  max-height: 500px;
  overflow-y: auto;
}

.keyword-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.keyword-item:hover {
  background-color: #f8f9fa;
}

.keyword-item:last-child {
  border-bottom: none;
}

.keyword-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.keyword-text {
  font-size: 14px;
  color: #262626;
  word-break: break-all;
  min-width: 0;
}

.keyword-length {
  font-size: 12px;
  color: #8c8c8c;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}

.keyword-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  padding: 4px 8px !important;
  height: auto !important;
  min-width: auto !important;
}

.keyword-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 爬取结果展示样式 */
.crawl-summary {
  padding: 16px;
}

.result-message {
  margin: 16px 0;
}

.database-result {
  margin-top: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.database-result h5 {
  margin: 0 0 8px 0;
  color: #262626;
  font-size: 14px;
  font-weight: 600;
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
  .simple-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .keyword-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 12px;
  }
  
  .keyword-content {
    width: 100%;
    justify-content: space-between;
  }
  
  .keyword-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
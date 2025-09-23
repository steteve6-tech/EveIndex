<template>
  <div class="device-data">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>设备数据管理</h1>
      <p>按国家管理和展示医疗器械相关数据</p>
    </div>

    <!-- 国家标签页 -->
    <a-tabs v-model:activeKey="activeCountry" class="country-tabs" @change="handleCountryChange">
<!--      标签页名称设置-->
      <a-tab-pane key="usa" tab="">
        <div class="country-device-content">
<!--          &lt;!&ndash; 各国设备数据概览 &ndash;&gt;-->
<!--          <div class="country-info">-->
<!--            <a-alert-->
<!--                message="各国设备数据概览"-->
<!--                description="展示各个国家具有哪些数据以及数据条数，基于jd_country字段进行统计"-->
<!--                type="info"-->
<!--                show-icon-->
<!--                style="margin-bottom: 16px"-->
<!--            />-->
<!--          </div>-->

          <!-- 各国数据统计 -->
          <div class="country-stats-section" v-if="countryDataStats && Object.keys(countryDataStats).length > 0">
            <a-card class="country-stats-card">



              <!-- 简洁统计概览 -->
              <div class="simplified-stats-section">
                <a-card title="数据统计概览">
                  <a-row :gutter="16">
                    <!-- 美国数据统计 -->
                    <a-col :span="4">
                      <a-statistic
                          title="召回记录"
                          :value="stats.recallCount"
                          :value-style="{ color: '#ff4d4f' }"
                      >
                        <template #prefix>
                          <WarningOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="4">
                      <a-statistic
                          title="申请记录"
                          :value="stats.device510KCount"
                          :value-style="{ color: '#1890ff' }"
                      >
                        <template #prefix>
                          <ExperimentOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="4">
                      <a-statistic
                          title="事件报告"
                          :value="stats.eventReportCount"
                          :value-style="{ color: '#faad14' }"
                      >
                        <template #prefix>
                          <AlertOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="4">
                      <a-statistic
                          title="注册记录"
                          :value="stats.registrationCount"
                          :value-style="{ color: '#52c41a' }"
                      >
                        <template #prefix>
                          <FileTextOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="4">
                      <a-statistic
                          title="指导文档"
                          :value="stats.guidanceCount"
                          :value-style="{ color: '#722ed1' }"
                      >
                        <template #prefix>
                          <BookOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                    <a-col :span="4">
                      <a-statistic
                          title="海关案例"
                          :value="stats.customsCount"
                          :value-style="{ color: '#13c2c2' }"
                      >
                        <template #prefix>
                          <GlobalOutlined/>
                        </template>
                      </a-statistic>
                    </a-col>
                  </a-row>
                </a-card>
              </div>



              <a-row :gutter="16">
                <a-col :span="12" v-for="(countryData, countryCode) in countryDataStats" :key="countryCode">
                  <a-card class="country-stat-card" size="small" :title="getCountryDisplayName(countryCode)">
                    <div class="country-data-overview">
                      <a-row :gutter="8">
                        <a-col :span="8" v-for="(count, dataType) in countryData" :key="dataType">
                          <div class="data-type-item">
                            <div class="data-type-label">{{ dataType }}</div>
                            <div class="data-type-count" :style="{ color: getDataTypeColorByChineseName(dataType) }">
                              {{ count }}
                            </div>
                          </div>
                        </a-col>
                      </a-row>
                      <div class="country-total">
                        <span class="total-label">总计：</span>
                        <span class="total-count">{{ getCountryTotal(countryData) }}</span>
                      </div>
                    </div>
                  </a-card>
                </a-col>
              </a-row>
            </a-card>
          </div>



          <!-- 美国主要内容区域 -->
          <div class="main-content">
            <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
              <!-- 统一关键词搜索 -->
              <a-tab-pane key="analysis" tab="数据分析">
                <div class="tab-content">
                  <div class="analysis-section">
                    <!-- 统一关键词搜索管理 -->
                    <a-card title="数据分析" style="margin-bottom: 16px;">
                      <a-form layout="vertical">
                        <a-row :gutter="16">
                          <a-col :span="6">
                            <a-form-item label="选择国家">
                              <a-select v-model:value="unifiedConfig.country" placeholder="选择国家"
                                        style="width: 100%">
                                <a-select-option value="">全部国家</a-select-option>
                                <a-select-option value="US">美国</a-select-option>
                                <a-select-option value="CN">中国</a-select-option>
                                <a-select-option value="EU">欧盟</a-select-option>
                              </a-select>
                            </a-form-item>
                          </a-col>
                          <a-col :span="6">
                            <a-form-item label="实体类型">
                              <a-select
                                  v-model:value="unifiedConfig.entityTypes"
                                  mode="multiple"
                                  placeholder="选择数据类型"
                                  style="width: 100%"
                              >
                                <a-select-option value="Device510K">申请记录</a-select-option>
                                <a-select-option value="DeviceEventReport">事件报告</a-select-option>
                                <a-select-option value="DeviceRecallRecord">召回记录</a-select-option>
                                <a-select-option value="DeviceRegistrationRecord">注册记录</a-select-option>
                                <a-select-option value="GuidanceDocument">指导文档</a-select-option>
                                <a-select-option value="CustomsCase">海关案例</a-select-option>
                              </a-select>
                            </a-form-item>
                          </a-col>
                          <a-col :span="6">
                            <a-form-item label="分析模式">
                              <a-select v-model:value="unifiedConfig.analysisMode" placeholder="选择搜索模式"
                                        style="width: 100%">
                                <a-select-option value="search">统一关键词搜索</a-select-option>
                              </a-select>
                            </a-form-item>
                          </a-col>
                          <a-col :span="6">
                            <a-form-item label="搜索模式">
                              <a-select v-model:value="unifiedConfig.searchMode" placeholder="选择搜索模式"
                                        style="width: 100%">
                                <a-select-option value="fuzzy">模糊搜索</a-select-option>
                                <a-select-option value="exact">精确搜索</a-select-option>
                              </a-select>
                            </a-form-item>
                          </a-col>
                        </a-row>
                        <a-row :gutter="16">
                          <a-col :span="6">
                            <a-form-item label="设置风险等级">
                              <a-select v-model:value="unifiedConfig.saveRiskLevel" placeholder="保存时设置的风险等级"
                                        style="width: 100%">
                                <a-select-option value="HIGH">高风险</a-select-option>
                                <a-select-option value="MEDIUM">中风险</a-select-option>
                                <a-select-option value="LOW">低风险</a-select-option>
                                <a-select-option value="AUTO">自动判断</a-select-option>
                              </a-select>
                            </a-form-item>
                          </a-col>
                        </a-row>

                        <a-form-item label="关键词列表">
                          <div class="unified-keywords-container">
                            <a-tag
                                v-for="(keyword, index) in unifiedConfig.keywords"
                                :key="index"
                                closable
                                @close="removeUnifiedKeyword(index)"
                                class="keyword-tag"
                                :color="getKeywordColor(keyword)"
                            >
                              <div class="keyword-content">
                                <span class="keyword-text">{{ keyword }}</span>
                                <a-badge
                                    :count="getKeywordDataCount(keyword)"
                                    class="keyword-badge"
                                    :show-zero="false"
                                />
                              </div>
                            </a-tag>
                            <a-input
                                v-if="showUnifiedKeywordInput"
                                ref="unifiedKeywordInputRef"
                                v-model:value="newUnifiedKeyword"
                                size="small"
                                style="width: 200px;"
                                @blur="addUnifiedKeyword"
                                @keyup.enter="addUnifiedKeyword"
                                placeholder="输入关键词后按回车"
                            />
                            <a-button v-else type="dashed" size="small" @click="showUnifiedKeywordInput = true">
                              <PlusOutlined/>
                              添加关键词
                            </a-button>
                          </div>
                        </a-form-item>
                        
                        <a-form-item label="黑名单关键词">
                          <div class="blacklist-keywords-container">
                            <a-tag
                                v-for="(keyword, index) in unifiedConfig.blacklistKeywords"
                                :key="index"
                                closable
                                @close="removeBlacklistKeyword(index)"
                                class="blacklist-keyword-tag"
                                color="red"
                            >
                              <div class="keyword-content">
                                <span class="keyword-text">{{ keyword }}</span>
                              </div>
                            </a-tag>
                            <a-input
                                v-if="showBlacklistKeywordInput"
                                ref="blacklistKeywordInputRef"
                                v-model:value="newBlacklistKeyword"
                                size="small"
                                style="width: 200px;"
                                @blur="addBlacklistKeyword"
                                @keyup.enter="addBlacklistKeyword"
                                placeholder="输入黑名单关键词后按回车"
                            />
                            <a-button v-else type="dashed" size="small" @click="showBlacklistKeywordInput = true">
                              <PlusOutlined/>
                              添加黑名单关键词
                            </a-button>
                          </div>
<!--                          <div class="blacklist-description">-->
<!--                            <a-alert-->
<!--                                message="黑名单关键词说明"-->
<!--                                description="包含黑名单关键词的数据将被过滤掉，不会出现在搜索结果中。数据必须同时满足：1. 包含关键词列表中的关键词 2. 不包含黑名单关键词"-->
<!--                                type="info"-->
<!--                                show-icon-->
<!--                                style="margin-top: 8px;"-->
<!--                            />-->
<!--                          </div>-->
                          <!-- 关键词统计信息 -->
                          <div v-if="unifiedResults.length > 0" class="keyword-statistics">
                            <div class="statistics-title">关键词匹配统计：</div>
                            <a-row :gutter="16">
                              <a-col :span="8" v-for="keyword in unifiedConfig.keywords" :key="keyword">
                                <div class="statistics-item">
                                  <span class="keyword-label">{{ keyword }}:</span>
                                  <a-tag :color="getKeywordColor(keyword)" class="count-tag">
                                    {{ getKeywordDataCount(keyword) }} 条数据
                                  </a-tag>
                                </div>
                              </a-col>
                            </a-row>
                          </div>
                        </a-form-item>
                        <a-form-item>
                          <a-space>
                            <a-button type="primary" @click="startUnifiedSearch" :loading="unifiedLoading">
                              <SearchOutlined/>
                              开始数据分析
                            </a-button>
                            <a-button type="default" @click="saveUnifiedKeywords">
                              <SaveOutlined/>
                              保存关键词
                            </a-button>
                            <a-button @click="resetUnifiedConfig">
                              重置配置
                            </a-button>
        <a-button type="warning" @click="resetAllDataToMediumRisk" :loading="resettingRisk">
          <ReloadOutlined/>
          重置
        </a-button>
                            <a-button
                                type="default"
                                @click="exportUnifiedResults"
                                :disabled="unifiedResults.length === 0"
                            >
                              <DownloadOutlined/>
                              导出结果
                            </a-button>
                            <a-button
                                type="primary"
                                @click="saveUnifiedResultsToDatabase"
                                :loading="savingToDatabase"
                                :disabled="unifiedResults.length === 0"
                            >
                              <SaveOutlined/>
                              保存到数据库
                            </a-button>
                          </a-space>
                        </a-form-item>
                      </a-form>
                    </a-card>

                    <!-- 搜索结果 -->
                    <div v-if="keywordSearchResults && Object.keys(keywordSearchResults).length > 0">
                      <a-card title="关键词搜索结果" style="margin-bottom: 16px;">
                        <a-row :gutter="16">
                          <a-col :span="6" v-for="(result, entityType) in keywordSearchResults" :key="entityType">
                            <a-statistic
                                :title="getEntityTypeDisplayName(entityType)"
                                :value="result.length"
                                :value-style="{ color: getEntityTypeColor(entityType) }"
                            >
                              <template #prefix>
                                <component :is="getEntityTypeIcon(entityType)"/>
                              </template>
                            </a-statistic>
                          </a-col>
                        </a-row>
                      </a-card>

                      <!-- 详细搜索结果 -->
                      <a-card title="详细搜索结果">
                        <a-tabs v-model:activeKey="searchResultActiveTab">
                          <a-tab-pane key="Device510K" tab="申请记录" v-if="keywordSearchResults.Device510K">
                            <a-table
                                :columns="device510KColumns"
                                :data-source="keywordSearchResults.Device510K"
                                :loading="searchLoading"
                                row-key="id"
                                size="small"
                            >
                              <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                  <a-button type="link" size="small" @click="viewDevice510KDetail(record)">查看详情
                                  </a-button>
                                </template>
                              </template>
                            </a-table>
                          </a-tab-pane>
                          <a-tab-pane key="DeviceEventReport" tab="事件报告"
                                      v-if="keywordSearchResults.DeviceEventReport">
                            <a-table
                                :columns="eventColumns"
                                :data-source="keywordSearchResults.DeviceEventReport"
                                :loading="searchLoading"
                                row-key="id"
                                size="small"
                            >
                              <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                  <a-button type="link" size="small" @click="viewEventDetail(record)">查看详情
                                  </a-button>
                                </template>
                              </template>
                            </a-table>
                          </a-tab-pane>
                          <a-tab-pane key="DeviceRecallRecord" tab="召回记录"
                                      v-if="keywordSearchResults.DeviceRecallRecord">
                            <a-table
                                :columns="recallColumns"
                                :data-source="keywordSearchResults.DeviceRecallRecord"
                                :loading="searchLoading"
                                row-key="id"
                                size="small"
                            >
                              <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                  <a-button type="link" size="small" @click="viewRecallDetail(record)">查看详情
                                  </a-button>
                                </template>
                              </template>
                            </a-table>
                          </a-tab-pane>
                          <a-tab-pane key="DeviceRegistrationRecord" tab="注册记录"
                                      v-if="keywordSearchResults.DeviceRegistrationRecord">
                            <a-table
                                :columns="registrationColumns"
                                :data-source="keywordSearchResults.DeviceRegistrationRecord"
                                :loading="searchLoading"
                                row-key="id"
                                size="small"
                            >
                              <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                  <a-button type="link" size="small" @click="viewRegistrationDetail(record)">查看详情
                                  </a-button>
                                </template>
                              </template>
                            </a-table>
                          </a-tab-pane>
                        </a-tabs>
                      </a-card>
                    </div>

                    <!-- 统一关键词搜索结果 -->
                    <div v-if="unifiedResults.length > 0">
                      <a-card title="统一关键词搜索结果" style="margin-bottom: 16px;">
                        <a-row :gutter="16">
                          <a-col :span="6" v-for="(result, index) in unifiedResults" :key="index">
                            <a-statistic
                                :title="getEntityTypeDisplayName(result?.entityType || 'Unknown')"
                                :value="result?.totalCount || 0"
                                :value-style="{ color: getEntityTypeColor(result?.entityType || 'Unknown') }"
                            >
                              <template #prefix>
                                <component :is="getEntityTypeIcon(result?.entityType || 'Unknown')"/>
                              </template>
                            </a-statistic>
                          </a-col>
                        </a-row>
                      </a-card>

                      <!-- 详细统一搜索结果 -->
                      <a-card title="详细统一搜索结果">
                        <!-- 检查是否有可显示的数据 -->
                        <div v-if="unifiedResults.length === 0" style="text-align: center; padding: 40px; color: #999;">
                          <p>没有找到可显示的数据</p>
                          <p>可能的原因：</p>
                          <ul style="text-align: left; display: inline-block;">
                            <li>搜索结果为空</li>
                            <li>数据结构不正确</li>
                            <li>数据过滤条件过于严格</li>
                          </ul>
                        </div>

                        <a-tabs v-model:activeKey="unifiedResultActiveTab" v-if="unifiedResults.length > 0">
                          <a-tab-pane
                              v-for="(result, index) in unifiedResults"
                              :key="result?.entityType || index"
                              :tab="getEntityTypeDisplayName(result?.entityType || 'Unknown')"
                          >
                            <a-table
                                :columns="getColumnsByEntityType(result?.entityType || 'Unknown')"
                                :data-source="result?.data || []"
                                :loading="unifiedLoading"
                                row-key="id"
                                size="small"
                            >
                              <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                  <a-button type="link" size="small"
                                            @click="viewDetailByEntityType(record, result?.entityType || 'Unknown')">
                                    查看详情
                                  </a-button>
                                </template>
                                <template v-else-if="column.key === 'matchedKeywords'">
                                  <div v-if="record.matchedKeywords && record.matchedKeywords.length > 0">
                                    <a-tag
                                        v-for="keyword in record.matchedKeywords"
                                        :key="keyword"
                                        :color="getKeywordColor(keyword)"
                                        size="small"
                                        style="margin: 2px;"
                                    >
                                      {{ keyword }}
                                    </a-tag>
                                  </div>
                                  <span v-else style="color: #999;">-</span>
                                </template>
                              </template>
                            </a-table>
                          </a-tab-pane>
                        </a-tabs>
                      </a-card>
                    </div>
                  </div>
                </div>
              </a-tab-pane>


              <!-- 召回记录 -->
              <a-tab-pane key="recall" tab="召回记录">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="recallSearchForm">
                      <a-form-item label="产品代码">
                        <a-input v-model:value="recallSearchForm.productCode" placeholder="输入产品代码"/>
                      </a-form-item>
                      <a-form-item label="召回状态">
                        <a-select v-model:value="recallSearchForm.recallStatus" placeholder="选择状态"
                                  style="width: 120px">
                          <a-select-option value="">全部</a-select-option>
                          <a-select-option value="Ongoing">进行中</a-select-option>
                          <a-select-option value="Completed">已完成</a-select-option>
                          <a-select-option value="Terminated">已终止</a-select-option>
                        </a-select>
                      </a-form-item>
                      <a-form-item label="国家">
                        <a-select v-model:value="recallSearchForm.countryCode" placeholder="选择国家"
                                  style="width: 120px">
                          <a-select-option value="">全部</a-select-option>
                          <a-select-option value="US">美国</a-select-option>
                          <a-select-option value="CN">中国</a-select-option>
                          <a-select-option value="EU">欧盟</a-select-option>
                        </a-select>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchRecallRecords" :loading="recallLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetRecallSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="recallColumns"
                      :data-source="recallData"
                      :loading="recallLoading"
                      :pagination="recallPagination"
                      @change="handleRecallTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewRecallDetail(record)">查看详情</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>


              <!-- 申请记录 -->
              <a-tab-pane key="510k" tab="申请记录">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="device510KSearchForm">
                      <a-form-item label="设备名称">
                        <a-input v-model:value="device510KSearchForm.deviceName" placeholder="输入设备名称"/>
                      </a-form-item>
                      <a-form-item label="设备类别">
                        <a-select v-model:value="device510KSearchForm.deviceClass" placeholder="选择类别"
                                  style="width: 120px">
                          <a-select-option value="">全部</a-select-option>
                          <a-select-option value="I">I类</a-select-option>
                          <a-select-option value="II">II类</a-select-option>
                          <a-select-option value="III">III类</a-select-option>
                        </a-select>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchDevice510KRecords" :loading="device510KLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetDevice510KSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="device510KColumns"
                      :data-source="device510KData"
                      :loading="device510KLoading"
                      :pagination="device510KPagination"
                      @change="handleDevice510KTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewDevice510KDetail(record)">查看详情</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <!-- 事件报告 -->
              <a-tab-pane key="event" tab="事件报告">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="eventSearchForm">
                      <a-form-item label="事件类型">
                        <a-input v-model:value="eventSearchForm.eventType" placeholder="输入事件类型"/>
                      </a-form-item>
                      <a-form-item label="制造商">
                        <a-input v-model:value="eventSearchForm.manufacturerName" placeholder="输入制造商名称"/>
                      </a-form-item>
                      <a-form-item label="设备类别">
                        <a-select v-model:value="eventSearchForm.deviceClass" placeholder="选择类别"
                                  style="width: 120px">
                          <a-select-option value="">全部</a-select-option>
                          <a-select-option value="I">I类</a-select-option>
                          <a-select-option value="II">II类</a-select-option>
                          <a-select-option value="III">III类</a-select-option>
                        </a-select>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchEventReports" :loading="eventLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetEventSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="eventColumns"
                      :data-source="eventData"
                      :loading="eventLoading"
                      :pagination="eventPagination"
                      @change="handleEventTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewEventDetail(record)">查看详情</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <!-- 注册记录 -->
              <a-tab-pane key="registration" tab="注册记录">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="registrationSearchForm">
                      <a-form-item label="设备名称">
                        <a-input v-model:value="registrationSearchForm.deviceName" placeholder="输入设备名称"/>
                      </a-form-item>
                      <a-form-item label="制造商">
                        <a-input v-model:value="registrationSearchForm.manufacturerName" placeholder="输入制造商名称"/>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchRegistrationRecords" :loading="registrationLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetRegistrationSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="registrationColumns"
                      :data-source="registrationData"
                      :loading="registrationLoading"
                      :pagination="registrationPagination"
                      @change="handleRegistrationTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewRegistrationDetail(record)">查看详情</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <!-- 指导文档 -->
              <a-tab-pane key="guidance" tab="指导文档">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="guidanceSearchForm">
                      <a-form-item label="文档标题">
                        <a-input v-model:value="guidanceSearchForm.title" placeholder="输入文档标题"/>
                      </a-form-item>
                      <a-form-item label="文档类型">
                        <a-input v-model:value="guidanceSearchForm.documentType" placeholder="输入文档类型"/>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchGuidanceDocuments" :loading="guidanceLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetGuidanceSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="guidanceColumns"
                      :data-source="guidanceData"
                      :loading="guidanceLoading"
                      :pagination="guidancePagination"
                      @change="handleGuidanceTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewGuidanceDetail(record)">查看详情</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <!-- 海关案例 -->
              <a-tab-pane key="customs" tab="海关案例">
                <div class="tab-content">
                  <div class="search-section">
                    <a-form layout="inline" :model="customsSearchForm">
                      <a-form-item label="案例标题">
                        <a-input v-model:value="customsSearchForm.title" placeholder="输入案例标题"/>
                      </a-form-item>
                      <a-form-item label="案例类型">
                        <a-input v-model:value="customsSearchForm.caseType" placeholder="输入案例类型"/>
                      </a-form-item>
                      <a-form-item>
                        <a-button type="primary" @click="searchCustomsCases" :loading="customsLoading">
                          搜索
                        </a-button>
                        <a-button style="margin-left: 8px" @click="resetCustomsSearch">
                          重置
                        </a-button>
                      </a-form-item>
                    </a-form>
                  </div>

                  <a-table
                      :columns="customsColumns"
                      :data-source="customsData"
                      :loading="customsLoading"
                      :pagination="customsPagination"
                      @change="handleCustomsTableChange"
                      row-key="id"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'action'">
                        <a-button type="link" @click="viewCustomsDetail(record)">查看详情</a-button>
                      </template>
                      <template v-else-if="column.key === 'hsCodeUsed'">
                        <div v-if="record.hsCodeUsed">
                          <div v-for="(item, index) in record.hsCodeUsed.split(',').map(s => s.trim())" :key="index">
                            {{ item }}
                          </div>
                        </div>
                        <span v-else>-</span>
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </div>


        <!-- 搜索结果 -->
        <div v-if="keywordSearchResults && Object.keys(keywordSearchResults).length > 0">
          <a-card title="关键词搜索结果" style="margin-bottom: 16px;">
            <a-row :gutter="16">
              <a-col :span="6" v-for="(result, entityType) in keywordSearchResults" :key="entityType">
                <a-statistic
                    :title="getEntityTypeDisplayName(entityType)"
                    :value="result.length"
                    :value-style="{ color: getEntityTypeColor(entityType) }"
                >
                  <template #prefix>
                    <component :is="getEntityTypeIcon(entityType)"/>
                  </template>
                </a-statistic>
              </a-col>
            </a-row>
          </a-card>

          <!-- 详细搜索结果 -->
          <a-card title="详细搜索结果">
            <a-tabs v-model:activeKey="searchResultActiveTab">
              <a-tab-pane key="Device510K" tab="申请记录" v-if="keywordSearchResults.Device510K">
                <a-table
                    :columns="device510KColumns"
                    :data-source="keywordSearchResults.Device510K"
                    :loading="searchLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewDevice510KDetail(record)">查看详情</a-button>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="DeviceEventReport" tab="事件报告" v-if="keywordSearchResults.DeviceEventReport">
                <a-table
                    :columns="eventColumns"
                    :data-source="keywordSearchResults.DeviceEventReport"
                    :loading="searchLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewEventDetail(record)">查看详情</a-button>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="DeviceRecallRecord" tab="召回记录" v-if="keywordSearchResults.DeviceRecallRecord">
                <a-table
                    :columns="recallColumns"
                    :data-source="keywordSearchResults.DeviceRecallRecord"
                    :loading="searchLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewRecallDetail(record)">查看详情</a-button>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="DeviceRegistrationRecord" tab="注册记录"
                          v-if="keywordSearchResults.DeviceRegistrationRecord">
                <a-table
                    :columns="registrationColumns"
                    :data-source="keywordSearchResults.DeviceRegistrationRecord"
                    :loading="searchLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewRegistrationDetail(record)">查看详情</a-button>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </div>

<!--        &lt;!&ndash; 统一关键词搜索结果 &ndash;&gt;-->
<!--        <div v-if="unifiedResults.length > 0">-->
<!--          &lt;!&ndash;                &lt;!&ndash; 调试信息 &ndash;&gt;&ndash;&gt;-->
<!--          &lt;!&ndash;                <div style="background: #f0f0f0; padding: 10px; margin-bottom: 10px; border-radius: 4px;">&ndash;&gt;-->
<!--          &lt;!&ndash;                  <p><strong>调试信息:</strong></p>&ndash;&gt;-->
<!--          &lt;!&ndash;                  <p>unifiedResults 长度: {{ unifiedResults.length }}</p>&ndash;&gt;-->
<!--          &lt;!&ndash;                  <p>unifiedResults 内容: {{ JSON.stringify(unifiedResults, null, 2) }}</p>&ndash;&gt;-->
<!--          &lt;!&ndash;                </div>&ndash;&gt;-->

<!--          <a-card title="统一关键词搜索结果" style="margin-bottom: 16px;">-->
<!--            <a-row :gutter="16">-->
<!--              <a-col :span="6" v-for="(result, index) in unifiedResults" :key="index">-->
<!--                <a-statistic-->
<!--                    :title="getEntityTypeDisplayName(result?.entityType || 'Unknown')"-->
<!--                    :value="result?.totalCount || 0"-->
<!--                    :value-style="{ color: getEntityTypeColor(result?.entityType || 'Unknown') }"-->
<!--                >-->
<!--                  <template #prefix>-->
<!--                    <component :is="getEntityTypeIcon(result?.entityType || 'Unknown')"/>-->
<!--                  </template>-->
<!--                </a-statistic>-->
<!--              </a-col>-->
<!--            </a-row>-->
<!--          </a-card>-->

<!--          &lt;!&ndash; 详细统一搜索结果 &ndash;&gt;-->
<!--          <a-card title="详细统一搜索结果">-->
<!--            &lt;!&ndash; 检查是否有可显示的数据 &ndash;&gt;-->
<!--            <div v-if="unifiedResults.length === 0" style="text-align: center; padding: 40px; color: #999;">-->
<!--              <p>没有找到可显示的数据</p>-->
<!--              <p>可能的原因：</p>-->
<!--              <ul style="text-align: left; display: inline-block;">-->
<!--                <li>搜索结果为空</li>-->
<!--                <li>数据结构不正确</li>-->
<!--                <li>数据过滤条件过于严格</li>-->
<!--              </ul>-->
<!--            </div>-->

<!--            <a-tabs v-model:activeKey="unifiedResultActiveTab" v-if="unifiedResults.length > 0">-->
<!--              <a-tab-pane-->
<!--                  v-for="(result, index) in unifiedResults"-->
<!--                  :key="result?.entityType || index"-->
<!--                  :tab="getEntityTypeDisplayName(result?.entityType || 'Unknown')"-->
<!--              >-->
<!--                <a-table-->
<!--                    :columns="getColumnsByEntityType(result?.entityType || 'Unknown')"-->
<!--                    :data-source="result?.data || []"-->
<!--                    :loading="unifiedLoading"-->
<!--                    row-key="id"-->
<!--                    size="small"-->
<!--                >-->
<!--                  <template #bodyCell="{ column, record }">-->
<!--                    <template v-if="column.key === 'action'">-->
<!--                      <a-button type="link" size="small"-->
<!--                                @click="viewDetailByEntityType(record, result?.entityType || 'Unknown')">-->
<!--                        查看详情-->
<!--                      </a-button>-->
<!--                    </template>-->
<!--                    <template v-else-if="column.key === 'matchedKeywords'">-->
<!--                      <a-tag v-for="keyword in record.matchedKeywords" :key="keyword" color="blue">-->
<!--                        {{ keyword }}-->
<!--                      </a-tag>-->
<!--                    </template>-->
<!--                    <template v-else-if="column.key === 'keywords'">-->
<!--                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">-->
<!--                        {{ keyword }}-->
<!--                      </a-tag>-->
<!--                    </template>-->
<!--                    <template v-else-if="column.key === 'riskLevel'">-->
<!--                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">-->
<!--                        {{ record.riskLevel || 'NONE' }}-->
<!--                      </a-tag>-->
<!--                    </template>-->
<!--                    <template v-else>-->
<!--                      {{ record[column.dataIndex] || '-' }}-->
<!--                    </template>-->
<!--                  </template>-->
<!--                </a-table>-->
<!--              </a-tab-pane>-->
<!--            </a-tabs>-->
<!--          </a-card>-->
<!--        </div>-->

        <!-- 分析结果 -->
        <div v-if="analysisResults.length > 0">
          <a-card title="分析结果" style="margin-bottom: 16px;">
            <a-row :gutter="16">
              <a-col :span="6" v-for="(result, index) in analysisResults" :key="index">
                <a-statistic
                    :title="result.moduleName"
                    :value="result.totalCount"
                    :value-style="{ color: result.color }"
                >
                  <template #prefix>
                    <component :is="result.icon"/>
                  </template>
                </a-statistic>
              </a-col>
            </a-row>
          </a-card>

          <!-- 详细结果 -->
          <a-card title="详细搜索结果">
            <a-tabs v-model:activeKey="resultActiveTab">
              <a-tab-pane key="recall" tab="召回记录">
                <a-table
                    :columns="analysisRecallColumns"
                    :data-source="analysisResults[0]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewRecallDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="510k" tab="申请记录">
                <a-table
                    :columns="analysisDevice510KColumns"
                    :data-source="analysisResults[1]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewDevice510KDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="event" tab="事件报告">
                <a-table
                    :columns="analysisEventColumns"
                    :data-source="analysisResults[2]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewEventDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="registration" tab="注册记录">
                <a-table
                    :columns="analysisRegistrationColumns"
                    :data-source="analysisResults[3]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewRegistrationDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="guidance" tab="指导文档">
                <a-table
                    :columns="analysisGuidanceColumns"
                    :data-source="analysisResults[4]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewGuidanceDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
              <a-tab-pane key="customs" tab="海关案例">
                <a-table
                    :columns="analysisCustomsColumns"
                    :data-source="analysisResults[5]?.data || []"
                    :loading="analysisLoading"
                    row-key="id"
                    size="small"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'action'">
                      <a-button type="link" size="small" @click="viewCustomsDetail(record)">查看详情</a-button>
                    </template>
                    <template v-else-if="column.key === 'matchedFields'">
                      <a-tag v-for="field in record.matchedFields" :key="field" color="blue">
                        {{ field }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'keywords'">
                      <a-tag v-for="keyword in record.keywords" :key="keyword" color="green">
                        {{ keyword }}
                      </a-tag>
                    </template>
                    <template v-else-if="column.key === 'riskLevel'">
                      <a-tag :color="record.riskLevel === 'HIGH' ? 'red' : 'default'">
                        {{ record.riskLevel || 'NONE' }}
                      </a-tag>
                    </template>
                  </template>
                </a-table>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </div>
      </a-tab-pane>

<!--      <a-tab-pane key="eu" tab="🇪🇺 欧盟">-->
<!--        <div class="country-device-content">-->
<!--          &lt;!&ndash; 各国设备数据概览 &ndash;&gt;-->
<!--          <div class="country-info">-->
<!--            <a-alert-->
<!--                message="各国设备数据概览"-->
<!--                description="展示各个国家具有哪些数据以及数据条数，基于jd_country字段进行统计"-->
<!--                type="warning"-->
<!--                show-icon-->
<!--                style="margin-bottom: 16px"-->
<!--            />-->
<!--          </div>-->

<!--          &lt;!&ndash; 各国数据统计 &ndash;&gt;-->
<!--          <div class="country-stats-section" v-if="countryDataStats && Object.keys(countryDataStats).length > 0">-->
<!--            <a-card title="各国数据统计" class="country-stats-card">-->
<!--              <a-row :gutter="16">-->
<!--                <a-col :span="12" v-for="(countryData, countryCode) in countryDataStats" :key="countryCode">-->
<!--                  <a-card class="country-stat-card" size="small" :title="getCountryDisplayName(countryCode)">-->
<!--                    <div class="country-data-overview">-->
<!--                      <a-row :gutter="8">-->
<!--                        <a-col :span="8" v-for="(count, dataType) in countryData" :key="dataType">-->
<!--                          <div class="data-type-item">-->
<!--                            <div class="data-type-label">{{ dataType }}</div>-->
<!--                            <div class="data-type-count" :style="{ color: getDataTypeColorByChineseName(dataType) }">-->
<!--                              {{ count }}-->
<!--                            </div>-->
<!--                          </div>-->
<!--                        </a-col>-->
<!--                      </a-row>-->
<!--                      <div class="country-total">-->
<!--                        <span class="total-label">总计：</span>-->
<!--                        <span class="total-count">{{ getCountryTotal(countryData) }}</span>-->
<!--                      </div>-->
<!--                    </div>-->
<!--                  </a-card>-->
<!--                </a-col>-->
<!--              </a-row>-->
<!--            </a-card>-->
<!--          </div>-->

<!--          &lt;!&ndash; 欧盟统计概览 &ndash;&gt;-->
<!--          <div class="stats-section">-->
<!--            <a-row :gutter="16">-->
<!--              <a-col :span="6">-->
<!--                <a-card>-->
<!--                  <a-statistic-->
<!--                      title="CE认证设备"-->
<!--                      :value="0"-->
<!--                      :value-style="{ color: '#1890ff' }"-->
<!--                  >-->
<!--                    <template #prefix>-->
<!--                      <ExperimentOutlined/>-->
<!--                    </template>-->
<!--                  </a-statistic>-->
<!--                </a-card>-->
<!--              </a-col>-->
<!--              <a-col :span="6">-->
<!--                <a-card>-->
<!--                  <a-statistic-->
<!--                      title="MDR数据"-->
<!--                      :value="0"-->
<!--                      :value-style="{ color: '#52c41a' }"-->
<!--                  >-->
<!--                    <template #prefix>-->
<!--                      <FileTextOutlined/>-->
<!--                    </template>-->
<!--                  </a-statistic>-->
<!--                </a-card>-->
<!--              </a-col>-->
<!--              <a-col :span="6">-->
<!--                <a-card>-->
<!--                  <a-statistic-->
<!--                      title="IVDR数据"-->
<!--                      :value="0"-->
<!--                      :value-style="{ color: '#faad14' }"-->
<!--                  >-->
<!--                    <template #prefix>-->
<!--                      <AlertOutlined/>-->
<!--                    </template>-->
<!--                  </a-statistic>-->
<!--                </a-card>-->
<!--              </a-col>-->
<!--              <a-col :span="6">-->
<!--                <a-card>-->
<!--                  <a-statistic-->
<!--                      title="指导文档"-->
<!--                      :value="0"-->
<!--                      :value-style="{ color: '#722ed1' }"-->
<!--                  >-->
<!--                    <template #prefix>-->
<!--                      <BookOutlined/>-->
<!--                    </template>-->
<!--                  </a-statistic>-->
<!--                </a-card>-->
<!--              </a-col>-->
<!--            </a-row>-->
<!--          </div>-->

<!--          &lt;!&ndash; 欧盟设备数据列表 &ndash;&gt;-->
<!--          <div class="main-content">-->
<!--            <a-empty-->
<!--                description="欧盟设备数据模块开发中"-->
<!--                image="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg"-->
<!--            >-->
<!--              <template #image>-->
<!--                <div style="font-size: 48px; color: #faad14;">🚧</div>-->
<!--              </template>-->
<!--              <a-button type="primary" @click="handleAddEuDeviceData">-->
<!--                添加欧盟设备数据-->
<!--              </a-button>-->
<!--            </a-empty>-->
<!--          </div>-->
<!--        </div>-->
<!--      </a-tab-pane>-->

    </a-tabs>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailVisible"
      title="详细信息"
      :width="900"
      :footer="null"
      @cancel="detailVisible = false"
    >
      <div v-if="selectedRecord" class="detail-content">
        <a-descriptions :column="2" bordered size="small">
          <template v-for="(value, key) in selectedRecord" :key="key">
            <a-descriptions-item 
              :label="formatFieldLabel(key)" 
              :span="isLongField(key) ? 2 : 1"
            >
              <div v-if="isLongField(key)" class="long-field-content">
                {{ formatFieldValue(key, value) }}
              </div>
              <div v-else>
                {{ formatFieldValue(key, value) }}
              </div>
            </a-descriptions-item>
          </template>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import {ref, reactive, onMounted, h} from 'vue'
import {message, Modal} from 'ant-design-vue'
import {
  WarningOutlined,
  ExperimentOutlined,
  AlertOutlined,
  FileTextOutlined,
  BookOutlined,
  GlobalOutlined,
  PlusOutlined,
  DownloadOutlined,
  SearchOutlined,
  SaveOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  searchDeviceDataByKeywords,
  getDeviceDataOverview,
  getDeviceDataByCountry,
  getDevice510KRecords,
  getDeviceRecallRecords,
  getDeviceEventReports,
  getDeviceRegistrationRecords,
  getGuidanceDocuments,
  getCustomsCases
} from '@/api/deviceData'
import {
  saveUnifiedKeywordConfig,
  getUnifiedKeywordConfig
} from '@/api/keywordguanli'

// 为了兼容性，创建别名函数
const getRecallRecords = getDeviceRecallRecords
const getEventReports = getDeviceEventReports
const getRegistrationRecords = getDeviceRegistrationRecords

// 响应式数据
const activeTab = ref('analysis')
const activeCountry = ref('usa')
const detailVisible = ref(false)
const selectedRecord = ref<any>(null)

// 各国数据统计
const countryDataStats = ref<Record<string, Record<string, number>>>({})
const loadingCountryStats = ref(false)

// 关键词搜索相关
const searchKeywords = ref<string[]>([])
const showSearchKeywordInput = ref(false)
const newSearchKeyword = ref('')
const searchKeywordInputRef = ref()
const selectedEntityTypes = ref<string[]>(['Device510K', 'DeviceEventReport', 'DeviceRecallRecord', 'DeviceRegistrationRecord'])
const searchLoading = ref(false)
const keywordSearchResults = ref<any>({})
const searchResultActiveTab = ref('Device510K')

// 统计数据
const stats = reactive({
  recallCount: 0,
  device510KCount: 0,
  eventReportCount: 0,
  registrationCount: 0,
  guidanceCount: 0,
  customsCount: 0
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    console.log('🔄 开始加载统计数据...')
    const overviewResponse = await getDeviceDataOverview()
    console.log('📊 统计数据响应:', overviewResponse)
    
    if (overviewResponse && overviewResponse.success && overviewResponse.data) {
      const data = overviewResponse.data
      stats.recallCount = data.deviceRecallRecordCount || 0
      stats.device510KCount = data.device510KCount || 0
      stats.eventReportCount = data.deviceEventReportCount || 0
      stats.registrationCount = data.deviceRegistrationRecordCount || 0
      stats.guidanceCount = data.guidanceDocumentCount || 0
      stats.customsCount = data.customsCaseCount || 0
      console.log('✅ 统计数据加载成功:', stats)
    } else {
      console.warn('⚠️ 统计数据响应格式不正确:', overviewResponse)
    }
  } catch (error) {
    console.error('❌ 加载统计数据失败:', error)
  }
}

// 召回记录相关
const recallLoading = ref(false)
const recallData = ref<any[]>([])
const recallPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const recallSearchForm = reactive({
  productCode: '',
  recallStatus: '',
  countryCode: ''
})

// 510K设备相关
const device510KLoading = ref(false)
const device510KData = ref<any[]>([])
const device510KPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const device510KSearchForm = reactive({
  deviceName: '',
  deviceClass: ''
})

// 事件报告相关
const eventLoading = ref(false)
const eventData = ref<any[]>([])
const eventPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const eventSearchForm = reactive({
  eventType: '',
  manufacturerName: '',
  deviceClass: ''
})

// 注册记录相关
const registrationLoading = ref(false)
const registrationData = ref<any[]>([])
const registrationPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const registrationSearchForm = reactive({
  deviceName: '',
  manufacturerName: '',
  createdDate: ''
})

// 指导文档相关
const guidanceLoading = ref(false)
const guidanceData = ref<any[]>([])
const guidancePagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const guidanceSearchForm = reactive({
  title: '',
  documentType: ''
})

// 海关案例相关
const customsLoading = ref(false)
const customsData = ref<any[]>([])
const customsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
})
const customsSearchForm = reactive({
  title: '',
  caseType: ''
})

// 统一关键词搜索相关
const analysisLoading = ref(false)
const analysisResults = ref<any[]>([])
const resultActiveTab = ref('recall')
const showKeywordInput = ref(false)
const newKeyword = ref('')
const keywordInputRef = ref()

const analysisConfig = reactive({
  country: '',
  keywords: [
    "Skin Analysis", "Skin Scanner", "3D skin imaging system", "Facial Imaging",
    "Skin pigmentation analysis system", "3D skin imaging system", "skin elasticity analysis"
  ]
})

// 统一关键词搜索配置
const unifiedConfig = reactive({
  country: '',
  entityTypes: ['Device510K', 'DeviceEventReport', 'DeviceRecallRecord', 'DeviceRegistrationRecord', 'GuidanceDocument', 'CustomsCase'],
  analysisMode: 'search', // 默认选择统一关键词搜索模式
  searchMode: 'fuzzy', // 新增：搜索模式，'fuzzy'为模糊搜索，'exact'为精确搜索
  searchRiskLevel: 'MEDIUM', // 搜索时使用的风险等级（固定为中等风险）
  saveRiskLevel: 'HIGH', // 保存时设置的风险等级（默认高风险，用户可调整）
  keywords: [
    "Skin Analysis", "Skin Scanner", "3D skin imaging system", "Facial Imaging",
    "Skin pigmentation analysis system", "3D skin imaging system", "skin elasticity analysis"
  ] as string[],
  blacklistKeywords: [] as string[] // 黑名单关键词，用于过滤数据
})

// 统一关键词搜索相关状态
const showUnifiedKeywordInput = ref(false)
const newUnifiedKeyword = ref('')
const unifiedKeywordInputRef = ref()
const unifiedLoading = ref(false)
const savingToDatabase = ref(false)
const resettingRisk = ref(false)
const unifiedResults = ref<any[]>([])
const unifiedResultActiveTab = ref('Device510K')

// 黑名单关键词管理状态
const showBlacklistKeywordInput = ref(false)
const newBlacklistKeyword = ref('')
const blacklistKeywordInputRef = ref()

// 表格列定义
const recallColumns = [
  {title: '产品描述', dataIndex: 'productDescription', key: 'productDescription'},
  {title: '召回公司', dataIndex: 'recallingFirm', key: 'recallingFirm'},
  {title: '事件日期', dataIndex: 'eventDatePosted', key: 'eventDatePosted'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '操作', key: 'action', width: 100}
]

const device510KColumns = [
  {title: '设备名称', dataIndex: 'deviceName', key: 'deviceName'},
  {title: '申请人', dataIndex: 'applicant', key: 'applicant'},
  {title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '操作', key: 'action', width: 100}
]

const eventColumns = [
  {title: '品牌名称', dataIndex: 'brandName', key: 'brandName'},
  {title: '制造商', dataIndex: 'manufacturerName', key: 'manufacturerName'},
  {title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '操作', key: 'action', width: 100}
]

const registrationColumns = [
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '商品名称',
    dataIndex: 'proprietaryName',
    key: 'proprietaryName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '制造商',
    dataIndex: 'manufacturerName',
    key: 'manufacturerName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '创建日期',
    dataIndex: 'createdDate',
    key: 'createdDate',
    width: 120,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '匹配字段', key: 'matchedFields', width: 150},
  {title: '操作', key: 'action', width: 100}
]

const guidanceColumns = [
  {title: '文档标题', dataIndex: 'title', key: 'title'},
  {title: '文档类型', dataIndex: 'topic', key: 'topic'},
  {title: '发布日期', dataIndex: 'publicationDate', key: 'publicationDate'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '操作', key: 'action', width: 100}
]

const customsColumns = [
  {title: '案例编号', dataIndex: 'caseNumber', key: 'caseNumber'},
  {title: '案例标题', dataIndex: 'rulingResult', key: 'rulingResult'},
  {title: 'HS编码', dataIndex: 'hsCodeUsed', key: 'hsCodeUsed'},
  {title: '处理日期', dataIndex: 'caseDate', key: 'caseDate'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200},
  {title: '匹配字段', key: 'matchedFields', width: 150},
  {title: '操作', key: 'action', width: 100}
]

// 分析结果专用列定义
const analysisRecallColumns = [
  {title: '产品描述', dataIndex: 'productDescription', key: 'productDescription'},
  {title: '召回公司', dataIndex: 'recallingFirm', key: 'recallingFirm'},
  {title: '事件日期', dataIndex: 'eventDatePosted', key: 'eventDatePosted'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

const analysisDevice510KColumns = [
  {title: '设备名称', dataIndex: 'deviceName', key: 'deviceName'},
  {title: '申请人', dataIndex: 'applicant', key: 'applicant'},
  {title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

const analysisEventColumns = [
  {title: '品牌名称', dataIndex: 'brandName', key: 'brandName'},
  {title: '制造商', dataIndex: 'manufacturerName', key: 'manufacturerName'},
  {title: '接收日期', dataIndex: 'dateReceived', key: 'dateReceived'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

const analysisRegistrationColumns = [
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '商品名称',
    dataIndex: 'proprietaryName',
    key: 'proprietaryName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '制造商',
    dataIndex: 'manufacturerName',
    key: 'manufacturerName',
    ellipsis: true,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {
    title: '创建日期',
    dataIndex: 'createdDate',
    key: 'createdDate',
    width: 120,
    customRender: ({text}: { text: any }) => {
      return text || '-'
    }
  },
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

const analysisGuidanceColumns = [
  {title: '文档标题', dataIndex: 'title', key: 'title'},
  {title: '文档类型', dataIndex: 'topic', key: 'topic'},
  {title: '发布日期', dataIndex: 'publicationDate', key: 'publicationDate'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

const analysisCustomsColumns = [
  {title: '案例编号', dataIndex: 'caseNumber', key: 'caseNumber'},
  {title: '案例标题', dataIndex: 'rulingResult', key: 'rulingResult'},
  {title: 'HS编码', dataIndex: 'hsCodeUsed', key: 'hsCodeUsed'},
  {title: '处理日期', dataIndex: 'caseDate', key: 'caseDate'},
  {title: '匹配关键词', key: 'matchedKeywords', width: 200, customRender: ({ record }: any) => {
    if (record.matchedKeywords && Array.isArray(record.matchedKeywords)) {
      return record.matchedKeywords.map((keyword: string) => 
        h('a-tag', { color: 'blue', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '匹配字段', key: 'matchedFields', width: 150, customRender: ({ record }: any) => {
    if (record.matchedFields && Array.isArray(record.matchedFields)) {
      return record.matchedFields.map((field: string) => 
        h('a-tag', { color: 'green', style: 'margin: 2px;' }, field)
      )
    }
    return '-'
  }},
  {title: '风险等级', key: 'riskLevel', width: 100, customRender: ({ record }: any) => {
    const riskLevel = record.riskLevel
    let color = 'default'
    if (riskLevel === 'HIGH') color = 'red'
    else if (riskLevel === 'MEDIUM') color = 'orange'
    else if (riskLevel === 'LOW') color = 'green'
    return h('a-tag', { color }, riskLevel || 'NONE')
  }},
  {title: '关键词列表', key: 'keywords', width: 200, customRender: ({ record }: any) => {
    if (record.keywords && Array.isArray(record.keywords)) {
      return record.keywords.map((keyword: string) => 
        h('a-tag', { color: 'orange', style: 'margin: 2px;' }, keyword)
      )
    }
    return '-'
  }},
  {title: '操作', key: 'action', width: 100}
]

// 方法
const loadOverviewStatistics = async () => {
  try {
    console.log('开始加载设备数据统计...')
    const result = await getDeviceDataOverview()
    console.log('设备数据统计API返回:', result)
    if (result && result.success && result.data) {
      Object.assign(stats, result.data)
      console.log('统计数据更新成功:', stats)
    } else {
      console.warn('API返回数据格式不正确:', result)
      message.warning('统计数据格式不正确')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    message.error('加载统计数据失败: ' + (error.message || '未知错误'))
  }
}

const searchRecallRecords = async () => {
  recallLoading.value = true
  try {
    console.log('开始搜索召回记录...')
    const params = {
      page: recallPagination.current - 1,
      size: recallPagination.pageSize,
      ...recallSearchForm
    }
    console.log('搜索参数:', params)
    const result = await getDeviceRecallRecords(params)
    console.log('召回记录API返回:', result)
    if (result && result.success && result.data) {
      recallData.value = result.data
      recallPagination.total = result.totalElements || 0
      console.log('召回记录数据更新成功，共', result.data.length, '条')
    } else {
      console.warn('API返回数据格式不正确:', result)
      message.warning('召回记录数据格式不正确')
    }
  } catch (error) {
    console.error('搜索召回记录失败:', error)
    message.error('搜索召回记录失败: ' + (error.message || '未知错误'))
  } finally {
    recallLoading.value = false
  }
}

const searchDevice510KRecords = async () => {
  device510KLoading.value = true
  try {
    const params = {
      page: device510KPagination.current - 1,
      size: device510KPagination.pageSize,
      ...device510KSearchForm
    }
    const result = await getDevice510KRecords(params)
    if (result && result.success && result.data) {
      device510KData.value = result.data
      device510KPagination.total = result.totalElements || 0
    }
  } catch (error) {
    console.error('搜索510K设备记录失败:', error)
    message.error('搜索510K设备记录失败')
  } finally {
    device510KLoading.value = false
  }
}

const searchEventReports = async () => {
  eventLoading.value = true
  try {
    const params = {
      page: eventPagination.current - 1,
      size: eventPagination.pageSize,
      ...eventSearchForm
    }
    const result = await getDeviceEventReports(params)
    if (result && result.success && result.data) {
      eventData.value = result.data
      eventPagination.total = result.totalElements || 0
    }
  } catch (error) {
    console.error('搜索事件报告失败:', error)
    message.error('搜索事件报告失败')
  } finally {
    eventLoading.value = false
  }
}

const searchRegistrationRecords = async () => {
  registrationLoading.value = true
  try {
    const params = {
      page: registrationPagination.current - 1,
      size: registrationPagination.pageSize,
      ...registrationSearchForm
    }
    const result = await getDeviceRegistrationRecords(params)
    if (result && result.success && result.data) {
      registrationData.value = result.data
      registrationPagination.total = result.totalElements || 0
    }
  } catch (error) {
    console.error('搜索注册记录失败:', error)
    message.error('搜索注册记录失败')
  } finally {
    registrationLoading.value = false
  }
}

const searchGuidanceDocuments = async () => {
  guidanceLoading.value = true
  try {
    const params = {
      page: guidancePagination.current - 1,
      size: guidancePagination.pageSize,
      ...guidanceSearchForm
    }
    const result = await getGuidanceDocuments(params)
    if (result && result.success && result.data) {
      guidanceData.value = result.data
      guidancePagination.total = result.totalElements || 0
    }
  } catch (error) {
    console.error('搜索指导文档失败:', error)
    message.error('搜索指导文档失败')
  } finally {
    guidanceLoading.value = false
  }
}

// 关键词搜索相关方法
const addSearchKeyword = () => {
  if (newSearchKeyword.value.trim()) {
    searchKeywords.value.push(newSearchKeyword.value.trim())
    newSearchKeyword.value = ''
    showSearchKeywordInput.value = false
  }
}

const removeSearchKeyword = (index: number) => {
  searchKeywords.value.splice(index, 1)
}

const resetKeywordSearch = () => {
  searchKeywords.value = []
  keywordSearchResults.value = {}
  searchResultActiveTab.value = 'Device510K'
}

const performKeywordSearch = async () => {
  if (searchKeywords.value.length === 0) {
    message.warning('请至少添加一个搜索关键词')
    return
  }

  if (selectedEntityTypes.value.length === 0) {
    message.warning('请至少选择一个实体类型')
    return
  }

  searchLoading.value = true
  try {
    console.log('开始关键词搜索...', {
      keywords: searchKeywords.value,
      entityTypes: selectedEntityTypes.value
    })

    const result = await searchDeviceDataByKeywords(
        searchKeywords.value,
        0,
        100, // 获取更多数据
        selectedEntityTypes.value,
        undefined, // 不限制风险等级，搜索所有数据
        unifiedConfig.country // 传递国家过滤参数
    )

    if (result && result.success && result.data) {
      keywordSearchResults.value = result.data
      console.log('关键词搜索结果:', result.data)
      message.success(`搜索完成，共找到 ${result.totalResults} 条记录`)
    } else {
      console.warn('关键词搜索API返回数据格式不正确:', result)
      message.warning('搜索结果格式不正确')
    }
  } catch (error) {
    console.error('关键词搜索失败:', error)
    message.error('关键词搜索失败: ' + (error.message || '未知错误'))
  } finally {
    searchLoading.value = false
  }
}

// 这些函数已在后面重新定义，删除重复声明

const searchCustomsCases = async () => {
  customsLoading.value = true
  try {
    const params = {
      page: customsPagination.current - 1,
      size: customsPagination.pageSize,
      ...customsSearchForm
    }
    const result = await getCustomsCases(params)
    if (result && result.success && result.data) {
      customsData.value = result.data
      customsPagination.total = result.totalElements || 0
    }
  } catch (error) {
    console.error('搜索海关案例失败:', error)
    message.error('搜索海关案例失败')
  } finally {
    customsLoading.value = false
  }
}

// 重置搜索
const resetRecallSearch = () => {
  Object.assign(recallSearchForm, {productCode: '', recallStatus: '', countryCode: ''})
  searchRecallRecords()
}

const resetDevice510KSearch = () => {
  Object.assign(device510KSearchForm, {deviceName: '', deviceClass: ''})
  searchDevice510KRecords()
}

const resetEventSearch = () => {
  Object.assign(eventSearchForm, {eventType: '', manufacturerName: '', deviceClass: ''})
  searchEventReports()
}

const resetRegistrationSearch = () => {
  Object.assign(registrationSearchForm, {
    deviceName: '',
    manufacturerName: '',
    createdDate: ''
  })
  searchRegistrationRecords()
}

const resetGuidanceSearch = () => {
  Object.assign(guidanceSearchForm, {title: '', documentType: ''})
  searchGuidanceDocuments()
}

const resetCustomsSearch = () => {
  Object.assign(customsSearchForm, {title: '', caseType: ''})
  searchCustomsCases()
}

// 分析模块方法
const addKeyword = () => {
  if (newKeyword.value.trim()) {
    analysisConfig.keywords.push(newKeyword.value.trim())
    newKeyword.value = ''
  }
  showKeywordInput.value = false
}

const removeKeyword = (index: number) => {
  analysisConfig.keywords.splice(index, 1)
}

// 统一关键词管理方法
const addUnifiedKeyword = () => {
  if (newUnifiedKeyword.value.trim()) {
    unifiedConfig.keywords.push(newUnifiedKeyword.value.trim())
    newUnifiedKeyword.value = ''
  }
  showUnifiedKeywordInput.value = false
}

const removeUnifiedKeyword = (index: number) => {
  unifiedConfig.keywords.splice(index, 1)
}

// 黑名单关键词管理方法
const addBlacklistKeyword = () => {
  if (newBlacklistKeyword.value.trim()) {
    // 检查是否已存在
    if (!unifiedConfig.blacklistKeywords.includes(newBlacklistKeyword.value.trim())) {
      unifiedConfig.blacklistKeywords.push(newBlacklistKeyword.value.trim())
      newBlacklistKeyword.value = ''
    } else {
      message.warning('该黑名单关键词已存在')
    }
  }
  showBlacklistKeywordInput.value = false
}

const removeBlacklistKeyword = (index: number) => {
  unifiedConfig.blacklistKeywords.splice(index, 1)
}

const saveUnifiedKeywords = async () => {
  try {
    console.log('🔄 开始保存关键词配置...', {
      normalKeywords: unifiedConfig.keywords,
      blacklistKeywords: unifiedConfig.blacklistKeywords,
      searchMode: unifiedConfig.searchMode
    })
    
    // 保存到localStorage
    const keywordsData = {
      keywords: unifiedConfig.keywords,
      blacklistKeywords: unifiedConfig.blacklistKeywords,
      country: unifiedConfig.country,
      entityTypes: unifiedConfig.entityTypes,
      analysisMode: unifiedConfig.analysisMode,
      searchMode: unifiedConfig.searchMode, // 保存搜索模式
      searchRiskLevel: unifiedConfig.searchRiskLevel,
      saveRiskLevel: unifiedConfig.saveRiskLevel,
      timestamp: new Date().toISOString()
    }
    localStorage.setItem('unifiedKeywordsConfig', JSON.stringify(keywordsData))
    console.log('✅ 已保存到localStorage')
    
    // 同时保存到后端
    try {
      const response = await saveUnifiedKeywordConfig({
        normalKeywords: unifiedConfig.keywords,
        blacklistKeywords: unifiedConfig.blacklistKeywords,
        searchMode: unifiedConfig.searchMode
      })
      
      console.log('📡 后端响应:', response)
      
      // if (response && (response as any).success) {
      message.success('关键词配置已保存到本地和后端')
      console.log('✅ 后端保存成功')
      // } else {
      //   message.warning('关键词配置已保存到本地，但后端保存失败')
      //   console.warn('⚠️ 后端保存失败，响应:', response)
      // }
    } catch (backendError) {
      console.error('❌ 保存到后端失败:', backendError)
      message.warning('关键词配置已保存到本地，但后端保存失败')
    }
    
  } catch (error) {
    console.error('❌ 保存关键词配置失败:', error)
    message.error('保存失败: ' + (error.message || '未知错误'))
  }
}

const loadSavedUnifiedConfig = async () => {
  try {
    console.log('🔄 开始加载关键词配置...')
    
    // 首先尝试从后端加载配置
    try {
      console.log('📡 尝试从后端加载配置...')
      const response = await getUnifiedKeywordConfig()
      console.log('📡 后端响应:', response)
      
      if (response && (response as any).normalKeywords && (response as any).blacklistKeywords) {
        // 后端直接返回UnifiedKeywordConfig对象
        const config = response as any
        if (config.normalKeywords && Array.isArray(config.normalKeywords)) {
          unifiedConfig.keywords = config.normalKeywords
        }
        if (config.blacklistKeywords && Array.isArray(config.blacklistKeywords)) {
          unifiedConfig.blacklistKeywords = config.blacklistKeywords
        }
        if (config.searchMode) {
          unifiedConfig.searchMode = config.searchMode
        }
        console.log('✅ 已从后端加载关键词配置:', config)
        return // 成功从后端加载，直接返回
      } else if (response && (response as any).success && (response as any).data) {
        // 兼容标准响应格式
        const config = (response as any).data
        if (config.normalKeywords && Array.isArray(config.normalKeywords)) {
          unifiedConfig.keywords = config.normalKeywords
        }
        if (config.blacklistKeywords && Array.isArray(config.blacklistKeywords)) {
          unifiedConfig.blacklistKeywords = config.blacklistKeywords
        }
        if (config.searchMode) {
          unifiedConfig.searchMode = config.searchMode
        }
        console.log('✅ 已从后端加载关键词配置（标准格式）:', config)
        return // 成功从后端加载，直接返回
      } else {
        console.warn('⚠️ 后端响应格式不正确，尝试从本地加载')
      }
    } catch (backendError) {
      console.warn('⚠️ 从后端加载配置失败，尝试从本地加载:', backendError)
    }
    
    // 如果后端加载失败，则从localStorage加载
    const savedConfig = localStorage.getItem('unifiedKeywordsConfig')
    if (savedConfig) {
      const config = JSON.parse(savedConfig)
      if (config.keywords && Array.isArray(config.keywords)) {
        unifiedConfig.keywords = config.keywords
      }
      if (config.blacklistKeywords && Array.isArray(config.blacklistKeywords)) {
        unifiedConfig.blacklistKeywords = config.blacklistKeywords
      }
      if (config.country) {
        unifiedConfig.country = config.country
      }
      if (config.entityTypes && Array.isArray(config.entityTypes)) {
        unifiedConfig.entityTypes = config.entityTypes
      }
      if (config.analysisMode) {
        unifiedConfig.analysisMode = config.analysisMode
      }
      if (config.searchMode) {
        unifiedConfig.searchMode = config.searchMode
      }
      if (config.searchRiskLevel) {
        unifiedConfig.searchRiskLevel = config.searchRiskLevel
      }
      if (config.saveRiskLevel) {
        unifiedConfig.saveRiskLevel = config.saveRiskLevel
      }
      // 兼容旧版本配置
      if (config.riskLevel && !config.searchRiskLevel && !config.saveRiskLevel) {
        unifiedConfig.searchRiskLevel = 'MEDIUM' // 搜索风险等级固定为中等风险
        unifiedConfig.saveRiskLevel = config.riskLevel // 保存风险等级使用旧配置
      }
      console.log('✅ 已从本地加载保存的关键词配置:', config)
    }
  } catch (error) {
    console.error('加载保存的关键词配置失败:', error)
  }
}

// 根据实体类型获取匹配字段
const getMatchFieldsByEntityType = (entityType: string): string[] => {
  const matchFieldsMap: { [key: string]: string[] } = {
    'Device510K': ['deviceName', 'applicant', 'openfda'],
    'DeviceEventReport': ['brandName', 'manufacturerName', 'mdrTextDescription'],
    'DeviceRecallRecord': ['recallingFirm', 'productDescription'],
    'DeviceRegistrationRecord': ['deviceName', 'manufacturerName', 'createdDate'],
    'GuidanceDocument': ['title', 'topic'],
    'CustomsCase': ['title', 'caseType']
  }
  return matchFieldsMap[entityType] || []
}

// 根据实体类型获取列定义
const getColumnsByEntityType = (entityType: string) => {
  const columnsMap: { [key: string]: any[] } = {
    'Device510K': device510KColumns,
    'DeviceEventReport': eventColumns,
    'DeviceRecallRecord': recallColumns,
    'DeviceRegistrationRecord': registrationColumns,
    'GuidanceDocument': guidanceColumns,
    'CustomsCase': customsColumns
  }
  return columnsMap[entityType] || []
}

// 根据实体类型获取显示名称
const getEntityTypeDisplayName = (entityType: string): string => {
  const displayNames: { [key: string]: string } = {
    'Device510K': '申请记录',
    'DeviceEventReport': '事件报告',
    'DeviceRecallRecord': '召回记录',
    'DeviceRegistrationRecord': '注册记录',
    'GuidanceDocument': '指导文档',
    'CustomsCase': '海关案例'
  }
  return displayNames[entityType] || entityType
}

// 根据实体类型获取图标
const getEntityTypeIcon = (entityType: string) => {
  const iconMap: { [key: string]: any } = {
    'Device510K': ExperimentOutlined,
    'DeviceEventReport': AlertOutlined,
    'DeviceRecallRecord': WarningOutlined,
    'DeviceRegistrationRecord': FileTextOutlined,
    'GuidanceDocument': BookOutlined,
    'CustomsCase': GlobalOutlined
  }
  return iconMap[entityType] || ExperimentOutlined
}

// 根据实体类型获取颜色
const getEntityTypeColor = (entityType: string): string => {
  const colorMap: { [key: string]: string } = {
    'Device510K': '#1890ff',
    'DeviceEventReport': '#faad14',
    'DeviceRecallRecord': '#ff4d4f',
    'DeviceRegistrationRecord': '#52c41a',
    'GuidanceDocument': '#722ed1',
    'CustomsCase': '#13c2c2'
  }
  return colorMap[entityType] || '#1890ff'
}

// 根据实体类型查看详情
const viewDetailByEntityType = (record: any, entityType: string) => {
  switch (entityType) {
    case 'Device510K':
      viewDevice510KDetail(record)
      break
    case 'DeviceEventReport':
      viewEventDetail(record)
      break
    case 'DeviceRecallRecord':
      viewRecallDetail(record)
      break
    case 'DeviceRegistrationRecord':
      viewRegistrationDetail(record)
      break
    case 'GuidanceDocument':
      viewGuidanceDetail(record)
      break
    case 'CustomsCase':
      viewCustomsDetail(record)
      break
    default:
      console.log('未知的实体类型:', entityType)
  }
}

const getKeywordColor = (keyword: string) => {
  // 根据关键词类型返回不同颜色
  const colors = ['blue', 'green', 'orange', 'purple', 'cyan', 'magenta']
  const index = keyword.length % colors.length
  return colors[index]
}

// 检查记录匹配了哪些关键词
const checkMatchedKeywords = (record: any, keywords: string[], entityType: string): string[] => {
  const matchedKeywords: string[] = []

  // 根据实体类型定义要检查的字段
  const searchFields = getSearchFieldsByEntityType(entityType)

  for (const keyword of keywords) {
    let isMatched = false

    // 检查每个搜索字段
    for (const field of searchFields) {
      const fieldValue = record[field]
      if (fieldValue && typeof fieldValue === 'string') {
        if (fieldValue.toLowerCase().includes(keyword.toLowerCase())) {
          isMatched = true
          break
        }
      }
    }

    if (isMatched) {
      matchedKeywords.push(keyword)
    }
  }

  return matchedKeywords
}

// 根据搜索模式进行关键词匹配
const matchKeywordsByMode = (text: string, keywords: string[], searchMode: string): string[] => {
  if (!text || !keywords || keywords.length === 0) {
    return []
  }

  const matchedKeywords: string[] = []
  const searchText = text.toLowerCase()

  for (const keyword of keywords) {
    const keywordLower = keyword.toLowerCase()
    let isMatched = false

    if (searchMode === 'exact') {
      // 精确搜索：完全匹配
      isMatched = searchText === keywordLower
    } else {
      // 模糊搜索：包含匹配
      isMatched = searchText.includes(keywordLower)
    }

    if (isMatched) {
      matchedKeywords.push(keyword)
    }
  }

  return matchedKeywords
}

// 修改现有的关键词匹配函数，支持搜索模式
const matchKeywordsInRecord = (record: any, keywords: string[], searchFields: string[], searchMode: string): string[] => {
  const matchedKeywords: string[] = []

  for (const keyword of keywords) {
    let isMatched = false

    for (const field of searchFields) {
      const fieldValue = record[field]
      if (fieldValue && typeof fieldValue === 'string') {
        if (searchMode === 'exact') {
          // 精确搜索：完全匹配
          isMatched = fieldValue.toLowerCase() === keyword.toLowerCase()
        } else {
          // 模糊搜索：包含匹配
          isMatched = fieldValue.toLowerCase().includes(keyword.toLowerCase())
        }

        if (isMatched) {
          break
        }
      }
    }

    if (isMatched) {
      matchedKeywords.push(keyword)
    }
  }

  return matchedKeywords
}

// 应用黑名单过滤
const applyBlacklistFilter = (data: any[], blacklistKeywords: string[]): any[] => {
  if (!blacklistKeywords || blacklistKeywords.length === 0) {
    return data
  }

  return data.filter(item => {
    const searchFields = getSearchFieldsByEntityType(item.entityType || '')
    const searchText = searchFields
      .map(field => item[field] || '')
      .join(' ')
      .toLowerCase()

    return !blacklistKeywords.some(blacklistKeyword => 
      searchText.includes(blacklistKeyword.toLowerCase())
    )
  })
}

// 自动判断风险等级
const determineRiskLevel = (item: any, matchedKeywords: string[]): string => {
  // 根据匹配的关键词数量和内容判断风险等级
  const highRiskKeywords = ['recall', 'safety', 'adverse', 'death', 'injury']
  const mediumRiskKeywords = ['warning', 'caution', 'risk', 'harm']
  
  const hasHighRiskKeyword = matchedKeywords.some(keyword => 
    highRiskKeywords.some(riskKeyword => 
      keyword.toLowerCase().includes(riskKeyword)
    )
  )
  
  const hasMediumRiskKeyword = matchedKeywords.some(keyword => 
    mediumRiskKeywords.some(riskKeyword => 
      keyword.toLowerCase().includes(riskKeyword)
    )
  )

  if (hasHighRiskKeyword) return 'HIGH'
  if (hasMediumRiskKeyword) return 'MEDIUM'
  return 'LOW'
}

// 根据实体类型获取搜索字段
const getSearchFieldsByEntityType = (entityType: string): string[] => {
  const fieldMap: { [key: string]: string[] } = {
    'Device510K': ['deviceName', 'applicant', 'openfda'],
    'DeviceEventReport': ['brandName', 'manufacturerName', 'mdrTextDescription'],
    'DeviceRecallRecord': ['productDescription', 'recallingFirm'],
    'DeviceRegistrationRecord': ['deviceName', 'manufacturerName', 'createdDate'],
    'GuidanceDocument': ['title', 'topic'],
    'CustomsCase': ['rulingResult', 'hsCodeUsed', 'violationType']
  }
  return fieldMap[entityType] || []
}

// 获取指定关键词匹配的数据量
const getKeywordDataCount = (keyword: string): number => {
  if (!unifiedResults.value || unifiedResults.value.length === 0) {
    return 0
  }

  let count = 0
  for (const result of unifiedResults.value) {
    if (result.data && Array.isArray(result.data)) {
      for (const item of result.data) {
        if (item.matchedKeywords && Array.isArray(item.matchedKeywords)) {
          if (item.matchedKeywords.includes(keyword)) {
            count++
          }
        }
      }
    }
  }
  return count
}

const resetUnifiedConfig = () => {
  unifiedConfig.country = ''
  unifiedConfig.entityTypes = ['Device510K', 'DeviceEventReport', 'DeviceRecallRecord', 'DeviceRegistrationRecord', 'GuidanceDocument', 'CustomsCase']
  unifiedConfig.analysisMode = 'search' // 默认选择统一关键词搜索模式
  unifiedConfig.searchMode = 'fuzzy' // 默认模糊搜索
  unifiedConfig.searchRiskLevel = 'MEDIUM' // 搜索时固定使用中等风险
  unifiedConfig.saveRiskLevel = 'HIGH' // 保存时默认设置为高风险
  unifiedConfig.keywords = [
    "Skin Analysis", "Skin Scanner", "3D skin imaging system", "Facial Imaging",
    "Skin pigmentation analysis system", "3D skin imaging system", "skin elasticity analysis"
  ]
  unifiedConfig.blacklistKeywords = [] // 重置黑名单关键词
  unifiedResults.value = []
}

const resetAnalysis = () => {
  analysisConfig.country = ''
  analysisConfig.keywords = [
    "Skin Analysis", "Skin Scanner", "3D skin imaging system", "Facial Imaging",
    "Skin pigmentation analysis system", "3D skin imaging system", "skin elasticity analysis"
  ]
  analysisResults.value = []
  resultActiveTab.value = 'recall'
}

const startAnalysis = async () => {
  if (analysisConfig.keywords.length === 0) {
    message.warning('请至少添加一个关键词')
    return
  }

  analysisLoading.value = true
  try {
    const results = []
    const moduleConfigs = [
      {
        name: '召回记录',
        api: getRecallRecords,
        icon: WarningOutlined,
        color: '#ff4d4f',
        matchFields: ['recallingFirm', 'productDescription'],
        entityType: 'DeviceRecallRecord'
      },
      {
        name: '申请记录',
        api: getDevice510KRecords,
        icon: ExperimentOutlined,
        color: '#1890ff',
        matchFields: ['deviceName', 'applicant', 'openfda'],
        entityType: 'Device510K'
      },
      {
        name: '事件报告',
        api: getEventReports,
        icon: AlertOutlined,
        color: '#faad14',
        matchFields: ['brandName', 'manufacturerName', 'mdrTextDescription'],
        entityType: 'DeviceEventReport'
      },
      {
        name: '注册记录',
        api: getRegistrationRecords,
        icon: FileTextOutlined,
        color: '#52c41a',
        matchFields: ['deviceNames', 'manufacturerName'],
        entityType: 'DeviceRegistrationRecord'
      },
      {
        name: '指导文档',
        api: getGuidanceDocuments,
        icon: BookOutlined,
        color: '#722ed1',
        matchFields: ['title', 'topic'],
        entityType: 'GuidanceDocument'
      },
      {
        name: '海关案例',
        api: getCustomsCases,
        icon: GlobalOutlined,
        color: '#13c2c2',
        matchFields: ['title', 'caseType'],
        entityType: 'CustomsCase'
      }
    ]

    for (const config of moduleConfigs) {
      const moduleResults = []

      for (const keyword of analysisConfig.keywords) {
        try {
          // 调用API获取数据
          const result = await config.api(0, 100)
          // 处理Axios响应对象
          const responseData = result?.data || result
          if (responseData && responseData.success && responseData.data) {
            // 使用智能匹配策略进行关键词匹配
            const matchedData = responseData.data.filter((item: any) => {
              return config.matchFields.some(field => {
                const fieldValue = item[field]
                if (!fieldValue) return false

                // 处理数组和JSON字符串
                let searchText = ''
                if (Array.isArray(fieldValue)) {
                  searchText = fieldValue.join(' ').toLowerCase()
                } else if (typeof fieldValue === 'string') {
                  // 尝试解析JSON字符串
                  try {
                    const parsed = JSON.parse(fieldValue)
                    if (Array.isArray(parsed)) {
                      searchText = parsed.join(' ').toLowerCase()
                    } else {
                      searchText = fieldValue.toLowerCase()
                    }
                  } catch {
                    searchText = fieldValue.toLowerCase()
                  }
                } else {
                  searchText = String(fieldValue).toLowerCase()
                }

                return searchText.includes(keyword.toLowerCase())
              })
            })

            if (matchedData.length > 0) {
              // 为匹配的数据设置风险等级和关键词
              const processedData = matchedData.map((item: any) => {
                const processedItem = {...item}

                // 设置风险等级为高
                if (processedItem.riskLevel !== undefined) {
                  processedItem.riskLevel = 'HIGH'
                }

                // 添加或更新关键词字段
                if (processedItem.keywords) {
                  // 如果已有关键词，添加新的关键词
                  const existingKeywords = Array.isArray(processedItem.keywords)
                      ? processedItem.keywords
                      : [processedItem.keywords]
                  if (!existingKeywords.includes(keyword)) {
                    existingKeywords.push(keyword)
                  }
                  processedItem.keywords = existingKeywords
                } else {
                  // 如果没有关键词字段，创建新的
                  processedItem.keywords = [keyword]
                }

                // 添加匹配信息
                processedItem.matchedKeyword = keyword
                processedItem.matchedFields = config.matchFields.filter(field => {
                  const fieldValue = item[field]
                  if (!fieldValue) return false

                  let searchText = ''
                  if (Array.isArray(fieldValue)) {
                    searchText = fieldValue.join(' ').toLowerCase()
                  } else if (typeof fieldValue === 'string') {
                    try {
                      const parsed = JSON.parse(fieldValue)
                      if (Array.isArray(parsed)) {
                        searchText = parsed.join(' ').toLowerCase()
                      } else {
                        searchText = fieldValue.toLowerCase()
                      }
                    } catch {
                      searchText = fieldValue.toLowerCase()
                    }
                  } else {
                    searchText = String(fieldValue).toLowerCase()
                  }

                  return searchText.includes(keyword.toLowerCase())
                })

                return processedItem
              })

              moduleResults.push(...processedData)
            }
          }
        } catch (error) {
          console.error(`搜索关键词 "${keyword}" 在 ${config.name} 中失败:`, error)
          // 添加更详细的错误信息
          if (error.response) {
            console.error('错误响应:', error.response.data)
            console.error('状态码:', error.response.status)
          } else if (error.request) {
            console.error('请求错误:', error.request)
          } else {
            console.error('错误信息:', error.message)
          }
        }
      }

      // 去重并统计
      const uniqueResults = moduleResults.filter((item: any, index: number, self: any[]) =>
          index === self.findIndex((t: any) => t.id === item.id)
      )

      results.push({
        moduleName: config.name,
        totalCount: uniqueResults.length,
        data: uniqueResults,
        icon: config.icon,
        color: config.color,
        entityType: config.entityType
      })
    }

    analysisResults.value = results
    message.success(`分析完成，共找到 ${results.reduce((sum, r) => sum + r.totalCount, 0)} 条匹配记录`)

    // 显示分析摘要
    const summary = results.map(r => `${r.moduleName}: ${r.totalCount}条`).join(', ')
    message.info(`分析摘要: ${summary}`)

  } catch (error) {
    console.error('分析失败:', error)
    message.error('分析失败: ' + (error.message || '未知错误'))
  } finally {
    analysisLoading.value = false
  }
}

// 统一关键词搜索方法
const startUnifiedAnalysis = async () => {
  if (unifiedConfig.keywords.length === 0) {
    message.warning('请至少添加一个关键词')
    return
  }

  unifiedLoading.value = true
  try {
    await startUnifiedSearch()
    message.success('统一关键词搜索完成')
  } catch (error) {
    console.error('统一关键词搜索失败:', error)
    message.error('统一关键词搜索失败: ' + (error.message || '未知错误'))
  } finally {
    unifiedLoading.value = false
  }
}

const resetAllDataToMediumRisk = async () => {
  try {
    resettingRisk.value = true
    
    // 确认对话框
    const confirmed = await new Promise((resolve) => {
      Modal.confirm({
        title: '确认重置',
        content: '确定要将所有数据设置为中等风险吗？此操作不可撤销。',
        okText: '确定',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      })
    })
    
    if (!confirmed) {
      return
    }
    
    // 调用后端API重置所有数据为中等风险
    const result = await fetch('/api/device-data/reset-all-to-medium-risk', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({})
    }).then(response => response.json())
    
    if (result.success) {
      const totalUpdated = result.totalUpdated || 0
      message.success(`所有数据已重置为中等风险，共更新 ${totalUpdated} 条记录`)
      
      // 刷新统计数据
      await loadStatistics()
      
      // 如果当前有搜索结果，重新执行搜索以显示更新后的数据
      if (unifiedResults.value.length > 0) {
        await startUnifiedSearch()
      }
    } else {
      message.error(result.message || '重置失败')
    }
    
  } catch (error) {
    console.error('重置数据失败:', error)
    message.error('重置失败: ' + (error.message || '未知错误'))
  } finally {
    resettingRisk.value = false
  }
}

const startUnifiedSearch = async () => {
  if (unifiedConfig.keywords.length === 0) {
    message.warning('请至少添加一个关键词')
    return
  }

  if (unifiedConfig.entityTypes.length === 0) {
    message.warning('请至少选择一个实体类型')
    return
  }

  unifiedLoading.value = true
  try {


    const results = []

    // 根据选择的实体类型执行搜索
    for (const entityType of unifiedConfig.entityTypes) {
      try {
        const result = await searchDeviceDataByKeywords(
            unifiedConfig.keywords,
            0,
            100,
            [entityType],
            unifiedConfig.searchRiskLevel,
            unifiedConfig.country,
            unifiedConfig.blacklistKeywords,
            unifiedConfig.searchMode
        )

        // 检查API返回结果是否有效
        if (!result) {
          continue
        }

        // 处理API响应数据
        const responseData = result?.data || result

        // 从响应数据中提取对应实体类型的数据
        let entityData = null
        if (responseData && responseData[entityType]) {
          entityData = responseData[entityType]
        } else if (responseData && responseData.data && responseData.data[entityType]) {
          entityData = responseData.data[entityType]
        } else if (responseData && responseData.success && responseData.data) {
          entityData = responseData.data
        }

        // 检查提取的数据是否有效
        if (!entityData) {
          continue
        }

        if (entityData && Array.isArray(entityData) && entityData.length > 0) {
          // 根据搜索模式进行关键词匹配
          const searchFields = getSearchFieldsByEntityType(entityType)
          const processedData = entityData.map((item: any) => {
            // 后端返回的数据结构：{ entity: {...}, matchedKeywords: [...], matchedFields: [...], matchDetails: {...} }
            const entity = item.entity || item
            let matchedKeywords = item.matchedKeywords || []
            let matchedFields = item.matchedFields || []
            const matchDetails = item.matchDetails || {}

            // 如果后端没有返回匹配信息，使用前端匹配逻辑
            if (matchedKeywords.length === 0) {
              matchedKeywords = matchKeywordsInRecord(
                entity, 
                unifiedConfig.keywords, 
                searchFields, 
                unifiedConfig.searchMode
              )
              
              matchedFields = searchFields.filter(field => {
                const fieldValue = entity[field]
                if (!fieldValue) return false

                if (unifiedConfig.searchMode === 'exact') {
                  return unifiedConfig.keywords.some(keyword => 
                    fieldValue.toLowerCase() === keyword.toLowerCase()
                  )
                } else {
                  return unifiedConfig.keywords.some(keyword => 
                    fieldValue.toLowerCase().includes(keyword.toLowerCase())
                  )
                }
              })
            }

            // 设置风险等级
            if (unifiedConfig.saveRiskLevel === 'AUTO') {
              // 自动判断风险等级
              entity.riskLevel = determineRiskLevel(entity, matchedKeywords)
            } else {
              entity.riskLevel = unifiedConfig.saveRiskLevel
            }

            // 设置匹配的关键词
            entity.matchedKeywords = matchedKeywords
            entity.matchedFields = matchedFields
            entity.matchDetails = matchDetails

            // 设置关键词列表
            entity.keywords = [...new Set([...(entity.keywords || []), ...matchedKeywords])]

            return entity
          }).filter((item: any) => item.matchedKeywords && item.matchedKeywords.length > 0)

          // 应用黑名单过滤
          const filteredData = applyBlacklistFilter(processedData, unifiedConfig.blacklistKeywords)

          if (filteredData.length > 0) {
            results.push({
              entityType,
              data: filteredData,
              totalCount: filteredData.length
            })
          }
        }
      } catch (error) {
        // 搜索失败，继续下一个实体类型
      }
    }

    unifiedResults.value = results
    const totalCount = results.reduce((sum, r) => sum + r.totalCount, 0)
    
    message.success(`搜索完成！共找到 ${totalCount} 条匹配记录（${unifiedConfig.searchMode === 'exact' ? '精确' : '模糊'}搜索）`)

  } catch (error) {
    console.error('统一搜索失败:', error)
    message.error('统一搜索失败: ' + (error.message || '未知错误'))
  } finally {
    unifiedLoading.value = false
  }
}

const exportUnifiedResults = () => {
  if (unifiedResults.value.length === 0) {
    message.warning('没有可导出的统一搜索结果')
    return
  }

  try {
    // 创建CSV内容
    let csvContent = '实体类型,记录数量,关键词,匹配字段,风险等级\n'

    unifiedResults.value.forEach((result) => {
      if (result.data && result.data.length > 0) {
        result.data.forEach((item: any) => {
          const keywords = Array.isArray(item.keywords) ? item.keywords.join(';') : (item.keywords || '')
          const matchedFields = Array.isArray(item.matchedFields) ? item.matchedFields.join(';') : (item.matchedFields || '')

          csvContent += `"${getEntityTypeDisplayName(result.entityType)}","${item.matchedKeyword || ''}","${matchedFields}","${item.riskLevel || 'NONE'}","${keywords}"\n`
        })
      }
    })

    // 创建下载链接
    const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'})
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `统一关键词搜索结果_${new Date().toISOString().slice(0, 10)}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    message.success('统一搜索结果导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败: ' + (error.message || '未知错误'))
  }
}

const saveUnifiedResultsToDatabase = async () => {
  if (unifiedResults.value.length === 0) {
    message.warning('没有可保存的统一搜索结果')
    return
  }

  try {
    savingToDatabase.value = true
    message.info('开始保存统一搜索结果到数据库...')

    let totalSaved = 0
    let totalErrors = 0
    const errorDetails: string[] = []

    // 遍历所有实体类型的结果
    for (const result of unifiedResults.value) {
      if (result.data && result.data.length > 0) {
        console.log(`开始保存实体类型 ${result.entityType} 的数据...`)

        for (const item of result.data) {
          try {
            // 根据实体类型调用不同的更新API
            const success = await updateEntityRiskLevelAndKeywords(result.entityType, item)
            if (success) {
              totalSaved++
              console.log(`成功保存数据项 ${item.id}`)
            } else {
              totalErrors++
              errorDetails.push(`${result.entityType} ID:${item.id}`)
            }
          } catch (error) {
            console.error(`保存数据项 ${item.id} 失败:`, error)
            totalErrors++
            errorDetails.push(`${result.entityType} ID:${item.id} - ${error.message || '未知错误'}`)
          }
        }
      }
    }

    if (totalErrors === 0) {
      message.success(`成功保存 ${totalSaved} 条记录到数据库`)
    } else {
      const errorMessage = `保存完成：成功 ${totalSaved} 条，失败 ${totalErrors} 条`
      message.warning(errorMessage)
      
      // 显示详细错误信息
      // if (errorDetails.length > 0) {
      //   console.error('保存失败的详细信息:', errorDetails)
      //   // 可以选择是否显示详细错误信息给用户
      //   if (errorDetails.length <= 10) {
      //     message.error(`失败详情: ${errorDetails.join(', ')}`)
      //   } else {
      //     message.error(`失败详情: ${errorDetails.slice(0, 10).join(', ')}...等${errorDetails.length}项`)
      //   }
      // }
    }

    // 刷新数据以显示最新的风险等级和关键词
    if (totalSaved > 0) {
      message.info('正在刷新数据...')
      await startUnifiedSearch() // 重新执行搜索以获取最新数据
    }

  } catch (error) {
    console.error('保存到数据库失败:', error)
    // message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    savingToDatabase.value = false
  }
}

import {resetAllDataToMediumRisk as resetAllDataAPI} from '@/api/deviceDataUpdate'
import request from '@/request'

// 根据实体类型调用对应的更新API
const updateEntityByType = async (entityType: string, id: string, riskLevel: string, keywords: string) => {
  try {
    const response = await request(`/device-data/update/${entityType}/${id}`, {
      method: 'PUT',
      data: {
        riskLevel: riskLevel,
        keywords: keywords
      }
    })
    return response
  } catch (error) {
    console.error(`更新 ${entityType} 失败:`, error)
    return { success: false, message: `更新失败: ${error}` }
  }
}

const updateEntityRiskLevelAndKeywords = async (entityType: string, item: any) => {
  try {
    // 准备更新的数据
    let keywords = ''
    
    // 处理关键词格式
    if (item.matchedKeywords && Array.isArray(item.matchedKeywords) && item.matchedKeywords.length > 0) {
      // 使用匹配到的关键词
      keywords = JSON.stringify(item.matchedKeywords)
    } else if (item.keywords && Array.isArray(item.keywords) && item.keywords.length > 0) {
      // 使用原始关键词
      keywords = JSON.stringify(item.keywords)
    } else if (item.keywords && typeof item.keywords === 'string') {
      // 如果是字符串，尝试解析或直接使用
      try {
        JSON.parse(item.keywords)
        keywords = item.keywords
      } catch (e) {
        // 如果不是JSON，转换为数组格式
        keywords = JSON.stringify([item.keywords])
      }
    } else {
      // 默认空数组
      keywords = JSON.stringify([])
    }
    
    const updateData = {
      riskLevel: item.riskLevel || unifiedConfig.saveRiskLevel, // 使用保存专用的风险等级
      keywords: keywords
    }

    console.log(`更新 ${entityType} 数据:`, updateData)

    // 后端期望的是字符串格式的关键词，保持JSON字符串格式
    const keywordsString = keywords

    // 调用后端API更新数据 - 需要根据entityType调用不同的API
    const result = await updateEntityByType(entityType, item.id, updateData.riskLevel, keywordsString)

    // 处理响应结果
    const responseData = (result as any).data || result
    if (responseData && responseData.success) {
      console.log(`成功更新 ${entityType}，ID: ${item.id}`, responseData)
      return true
    } else {
      // console.error(`更新 ${entityType} 失败，ID: ${item.id}:`, responseData?.message || '未知错误')
      return false
    }

  } catch (error) {
    console.error(`调用更新API失败，实体类型: ${entityType}, ID: ${item.id}`, error)
    return false
  }
}

// 表格分页处理
const handleRecallTableChange = (pagination: any) => {
  recallPagination.current = pagination.current
  recallPagination.pageSize = pagination.pageSize
  searchRecallRecords()
}

const handleDevice510KTableChange = (pagination: any) => {
  device510KPagination.current = pagination.current
  device510KPagination.pageSize = pagination.pageSize
  searchDevice510KRecords()
}

const handleEventTableChange = (pagination: any) => {
  eventPagination.current = pagination.current
  eventPagination.pageSize = pagination.pageSize
  searchEventReports()
}

const handleRegistrationTableChange = (pagination: any) => {
  registrationPagination.current = pagination.current
  registrationPagination.pageSize = pagination.pageSize
  searchRegistrationRecords()
}

const handleGuidanceTableChange = (pagination: any) => {
  guidancePagination.current = pagination.current
  guidancePagination.pageSize = pagination.pageSize
  searchGuidanceDocuments()
}

const handleCustomsTableChange = (pagination: any) => {
  customsPagination.current = pagination.current
  customsPagination.pageSize = pagination.pageSize
  searchCustomsCases()
}

// 标签页切换
const handleTabChange = (key: string) => {
  activeTab.value = key
  // 根据标签页加载对应数据
  switch (key) {
    case 'recall':
      searchRecallRecords()
      break
    case '510k':
      searchDevice510KRecords()
      break
    case 'event':
      searchEventReports()
      break
    case 'registration':
      searchRegistrationRecords()
      break
    case 'guidance':
      searchGuidanceDocuments()
      break
    case 'customs':
      searchCustomsCases()
      break
    case 'analysis':
      // 分析模块不需要自动加载数据
      break
  }
}

// 查看详情
const viewRecallDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const viewDevice510KDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const viewEventDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const viewRegistrationDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const viewGuidanceDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

const viewCustomsDetail = (record: any) => {
  selectedRecord.value = record
  detailVisible.value = true
}

// 处理国家切换
const handleCountryChange = (country: string) => {
  console.log('切换到国家:', country)
  activeCountry.value = country

  if (country === 'eu') {
    // 如果切换到欧盟，可以在这里加载欧盟数据
    console.log('欧盟设备数据模块待开发')
  } else if (country === 'usa') {
    // 切换到美国，重新加载美国数据
    loadOverviewStatistics()
  }
}

// 处理添加欧盟设备数据 - 暂时未使用
// const handleAddEuDeviceData = () => {
//   message.info('欧盟设备数据添加功能开发中，敬请期待！')
//   // 这里可以打开添加设备数据的模态框或跳转到添加页面
// }

// 导出分析结果 - 暂时未使用
/*
const exportAnalysisResults = () => {
  if (analysisResults.value.length === 0) {
    message.warning('没有可导出的分析结果')
    return
  }

  try {
    // 创建CSV内容
    let csvContent = '数据模块,记录数量,匹配关键词,风险等级,关键词列表\n'

    analysisResults.value.forEach((result) => {
      if (result.data && result.data.length > 0) {
        result.data.forEach((item: any) => {
          const keywords = Array.isArray(item.keywords) ? item.keywords.join(';') : (item.keywords || '')
          const matchedFields = Array.isArray(item.matchedFields) ? item.matchedFields.join(';') : (item.matchedFields || '')

          csvContent += `"${result.moduleName}","${item.matchedKeyword || ''}","${matchedFields}","${item.riskLevel || 'NONE'}","${keywords}"\n`
        })
      }
    })

    // 创建下载链接
    const blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'})
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `统一关键词搜索结果_${new Date().toISOString().slice(0, 10)}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    message.success('分析结果导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败: ' + ((error as any).message || '未知错误'))
  }
}
*/

// 格式化字段标签
const formatFieldLabel = (key: string): string => {
  const labelMap: Record<string, string> = {
    // 通用字段
    'id': 'ID',
    'riskLevel': '风险等级',
    'keywords': '关键词',
    'dataSource': '数据来源',
    'dataStatus': '数据状态',
    'crawlTime': '爬取时间',
    'jdCountry': '国家',
    'createTime': '创建时间',
    'updateTime': '更新时间',
    'createdTime': '创建时间',
    'updatedTime': '更新时间',
    
    // DeviceRecallRecord 召回记录字段
    'cfresId': '召回事件ID',
    'productDescription': '产品描述',
    'recallingFirm': '召回公司',
    'recallStatus': '召回等级',
    'eventDatePosted': '召回发布日期',
    'deviceName': '设备名称',
    'productCode': '产品代码',
    'recallCountryCode': '国家代码',
    
    // Device510K 申请记录字段
    'deviceClass': '设备类别',
    'tradeName': '品牌名称',
    'applicant': '申请人',
    'device510KCountryCode': '国家代码',
    'dateReceived': '接收日期',
    'kNumber': 'K号',
    'deviceUrl': '设备URL',
    'decisionDate': '决定日期',
    'regulationNumber': '法规编号',
    'openfda': 'OpenFDA数据',
    
    // DeviceEventReport 事件报告字段
    'reportNumber': '报告编号',
    'eventType': '事件类型',
    'typeOfReport': '报告类型',
    'dateOfEvent': '事件日期',
    'dateReport': '报告日期',
    'sourceType': '来源类型',
    'reportSourceCode': '报告来源代码',
    'brandName': '品牌名称',
    'modelNumber': '型号',
    'genericName': '通用名称',
    'manufacturerName': '制造商名称',
    'manufacturerCity': '制造商城市',
    'manufacturerState': '制造商州/省',
    'manufacturerCountry': '制造商国家',
    'medicalSpecialty': '医学专业',
    'deviceEvaluatedByManufacturer': '制造商评估设备',
    'mdrTextDescription': 'MDR文本描述',
    'mdrTextAction': 'MDR文本行动',
    'contactPerson': '联系人',
    'contactPhone': '联系电话',
    'dateAdded': '添加日期',
    'patientCount': '患者数量',
    
    // EU Safety Gate 特有字段
    'productNameSpecific': '产品具体名称',
    'euProductDescription': '产品描述',
    'riskType': '风险类型',
    'riskDescription': '风险描述',
    'notifyingCountry': '通知国家',
    'productCategory': '产品类别',
    'productSubcategory': '产品子类别',
    'measuresDescription': '措施描述',
    'detailUrl': '详情URL',
    'imageUrl': '图片URL',
    'brandsList': '品牌列表',
    'risksList': '风险列表',
    
    // FDA 特有字段
    'adverseEventFlag': '不良事件标志',
    'dateReportToFda': '向FDA报告日期',
    'reportToFda': '向FDA报告',
    'reportToManufacturer': '向制造商报告',
    'mdrReportKey': 'MDR报告键',
    'eventLocation': '事件位置',
    'eventKey': '事件键',
    'numberDevicesInEvent': '事件中设备数量',
    'productProblemFlag': '产品问题标志',
    'productProblemsList': '产品问题列表',
    'remedialActionList': '补救措施列表',
    
    // DeviceRegistrationRecord 注册记录字段
    'registrationNumber': '注册编号',
    'feiNumber': 'FEI编号',
    'proprietaryName': '专有名称',
    'riskClass': '风险等级',
    'statusCode': '状态码',
    'createdDate': '创建日期',
    
    // GuidanceDocument 指导文档字段
    'documentType': '文档类型',
    'title': '标题',
    'publicationDate': '发布日期',
    'topic': '话题',
    'guidanceStatus': '指导状态',
    'documentUrl': '文档URL',
    'sourceUrl': '来源URL',
    
    // EU 新闻特有字段
    'newsType': '新闻类型',
    'summary': '摘要',
    'content': '内容',
    'author': '作者',
    'tags': '标签',
    'category': '类别',
    'subcategory': '子类别',
    'language': '语言',
    'readCount': '阅读次数',
    'shareCount': '分享次数',
    'commentCount': '评论次数',
    'isFeatured': '是否精选',
    'isPublished': '是否发布',
    'publishDate': '发布日期',
    'expireDate': '过期日期',
    'relatedDocuments': '相关文档',
    'attachments': '附件',
    'metadata': '元数据',
    
    // CustomsCase 海关案例字段
    'caseNumber': '案例编号',
    'caseDate': '案例日期',
    'hsCodeUsed': 'HS编码',
    'rulingResult': '裁定结果',
    'violationType': '违规类型',
    'penaltyAmount': '处罚金额'
  }
  return labelMap[key] || key
}

// 判断是否为长字段
const isLongField = (key: string): boolean => {
  const longFields = [
    // 通用长字段
    'description', 'content', 'summary', 'keywords', 'title', 'metadata',
    
    // DeviceRecallRecord 召回记录长字段
    'productDescription',
    
    // Device510K 申请记录长字段
    'openfda',
    
    // DeviceEventReport 事件报告长字段
    'mdrTextDescription', 'mdrTextAction', 'eventProductDescription', 'riskDescription', 
    'measuresDescription', 'productProblemsList', 'remedialActionList',
    
    // DeviceRegistrationRecord 注册记录长字段
    'proprietaryName',
    
    // GuidanceDocument 指导文档长字段
    'title', 'summary', 'content', 'relatedDocuments', 'attachments',
    
    // CustomsCase 海关案例长字段
    'rulingResult', 'hsCodeUsed'
  ]
  return longFields.includes(key)
}

// 格式化字段值
const formatFieldValue = (key: string, value: any): string => {
  if (value === null || value === undefined) {
    return '-'
  }
  
  if (typeof value === 'boolean') {
    return value ? '是' : '否'
  }
  
  if (value instanceof Date) {
    return value.toLocaleDateString('zh-CN')
  }
  
  if (typeof value === 'string' && value.trim() === '') {
    return '-'
  }
  
  // 处理数组类型（如逗号分隔的列表）
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  
  // 处理对象类型（如JSON数据）
  if (typeof value === 'object' && value !== null) {
    try {
      return JSON.stringify(value, null, 2)
    } catch (e) {
      return String(value)
    }
  }
  
  // 处理数字类型
  if (typeof value === 'number') {
    // 处理金额字段
    if (key === 'penaltyAmount') {
      return new Intl.NumberFormat('zh-CN', {
        style: 'currency',
        currency: 'USD'
      }).format(value)
    }
    // 处理其他数字
    return value.toLocaleString('zh-CN')
  }
  
  // 处理枚举类型（风险等级等）
  if (key === 'riskLevel') {
    const riskLevelMap: Record<string, string> = {
      'HIGH': '高风险',
      'MEDIUM': '中风险',
      'LOW': '低风险',
      'NONE': '无风险'
    }
    return riskLevelMap[value] || value
  }
  
  // 处理特殊字段的格式化
  if (key === 'keywords') {
    try {
      // 尝试解析JSON格式的关键词
      const keywords = JSON.parse(value)
      if (Array.isArray(keywords)) {
        return keywords.join(', ')
      }
    } catch (e) {
      // 如果不是JSON，直接返回
    }
  }
  
  // 处理URL字段
  if (key.includes('url') || key.includes('Url')) {
    if (typeof value === 'string' && value.startsWith('http')) {
      return value // 保持URL原样，前端可以处理链接
    }
  }
  
  // 处理日期字符串
  if (key.includes('date') || key.includes('Date') || key.includes('time') || key.includes('Time')) {
    if (typeof value === 'string') {
      try {
        const date = new Date(value)
        if (!isNaN(date.getTime())) {
          return date.toLocaleDateString('zh-CN')
        }
      } catch (e) {
        // 日期解析失败，返回原值
      }
    }
  }
  
  return String(value)
}

// 获取各国数据统计
const loadCountryDataStats = async () => {
  try {
    loadingCountryStats.value = true
    const response = await getDeviceDataByCountry()
    if (response && (response as any).data) {
      countryDataStats.value = (response as any).data
    }
  } catch (error) {
    console.error('获取各国数据统计失败:', error)
    message.error('获取各国数据统计失败')
  } finally {
    loadingCountryStats.value = false
  }
}

// 获取国家显示名称
const getCountryDisplayName = (countryCode: string): string => {
  const countryNames: Record<string, string> = {
    'US': '美国',
    'CN': '中国',
    'EU': '欧盟',
    'JP': '日本',
    'KR': '韩国',
    'CA': '加拿大',
    'AU': '澳大利亚',
    'GB': '英国',
    'DE': '德国',
    'FR': '法国',
    'Unknown': '未知'
  }
  return countryNames[countryCode] || countryCode
}

// 获取数据类型显示名称 - 暂时未使用
/*
const getDataTypeDisplayName = (dataType: string): string => {
  const typeNames: Record<string, string> = {
    'Device510K': '申请记录',
    'DeviceEventReport': '事件报告',
    'DeviceRecallRecord': '召回记录',
    'DeviceRegistrationRecord': '注册记录',
    'GuidanceDocument': '指导文档',
    'CustomsCase': '海关案例'
  }
  return typeNames[dataType] || dataType
}
*/


// 获取数据类型颜色 - 暂时未使用
/*
const getDataTypeColor = (dataType: string): string => {
  const colors: Record<string, string> = {
    'Device510K': '#1890ff',
    'DeviceEventReport': '#faad14',
    'DeviceRecallRecord': '#ff4d4f',
    'DeviceRegistrationRecord': '#52c41a',
    'GuidanceDocument': '#722ed1',
    'CustomsCase': '#13c2c2'
  }
  return colors[dataType] || '#666'
}
*/

// 根据中文名称获取数据类型颜色
const getDataTypeColorByChineseName = (chineseName: string): string => {
  const colors: Record<string, string> = {
    '申请记录': '#1890ff',
    '事件报告': '#faad14',
    '召回记录': '#ff4d4f',
    '注册记录': '#52c41a',
    '指导文档': '#722ed1',
    '海关案例': '#13c2c2'
  }
  return colors[chineseName] || '#666'
}

// 计算国家总数据量
const getCountryTotal = (countryData: Record<string, number>): number => {
  return Object.values(countryData).reduce((sum, count) => sum + count, 0)
}

// 组件挂载时初始化
onMounted(async () => {
  await loadOverviewStatistics()
  await searchRecallRecords()
  await loadSavedUnifiedConfig() // 异步加载关键词配置
  loadStatistics()
  await loadCountryDataStats()
})
</script>
<style scoped>
.device-data {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

/* 国家标签页样式 */
.country-tabs {
  margin-bottom: 20px;
}

.country-tabs :deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 500;
}

.country-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff;
  font-weight: 600;
}

.country-device-content {
  padding-top: 16px;
}

.country-info {
  margin-bottom: 20px;
}

.country-info :deep(.ant-alert-message) {
  font-weight: 600;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: rgba(0, 0, 0, 0.65);
}

.stats-section {
  margin-bottom: 24px;
}

.main-content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tab-content {
  min-height: 400px;
}

.search-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 600;
  background: #fafafa;
}

/* 分析模块样式 */
.analysis-section {
  min-height: 400px;
}

.keywords-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
}

.keywords-container:hover {
  border-color: #40a9ff;
}

.keywords-container .ant-tag {
  margin: 0;
  cursor: pointer;
}

.keywords-container .ant-tag:hover {
  background: #f0f0f0;
}

.analysis-results {
  margin-top: 16px;
}

.result-statistics {
  margin-bottom: 16px;
}

.result-tables {
  background: #fafafa;
  padding: 16px;
  border-radius: 6px;
}

/* 关键词搜索模块样式 */
.search-keywords-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
}

.search-keywords-container:hover {
  border-color: #52c41a;
}

.search-keywords-container .ant-tag {
  margin: 0;
  cursor: pointer;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.search-keywords-container .ant-tag:hover {
  background: #d9f7be;
  border-color: #95de64;
}

/* 统一关键词管理样式 */
.unified-keywords-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  padding: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.unified-keywords-container:hover {
  border-color: #1890ff;
  background: #fff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.unified-keywords-container .ant-tag {
  margin: 0;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.unified-keywords-container .ant-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.unified-keywords-container .ant-input {
  border: 1px dashed #1890ff;
  border-radius: 4px;
}

.unified-keywords-container .ant-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* 搜索结果统计卡片样式 */
.search-results-stats {
  margin-bottom: 16px;
}

.search-results-stats .ant-statistic {
  text-align: center;
}

.search-results-stats .ant-statistic-title {
  font-size: 14px;
  color: #666;
}

.search-results-stats .ant-statistic-content {
  font-size: 24px;
  font-weight: bold;
}

/* 关键词标签样式 */
.keyword-tag {
  margin-bottom: 8px !important;
  margin-right: 8px !important;
  padding: 8px 12px !important;
  font-size: 14px !important;
  border-radius: 6px !important;
  transition: all 0.3s ease !important;
}

.keyword-tag:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

.keyword-content {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
}

.keyword-text {
  font-weight: 500 !important;
}

.keyword-badge {
  background-color: #52c41a !important;
  color: white !important;
  border-radius: 10px !important;
  font-size: 12px !important;
  min-width: 20px !important;
  height: 20px !important;
  line-height: 20px !important;
}

/* 关键词统计信息样式 */
.keyword-statistics {
  margin-top: 12px !important;
  padding: 16px !important;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%) !important;
  border-radius: 8px !important;
  border: 1px solid #e8e8e8 !important;
}

.statistics-title {
  font-weight: 600 !important;
  margin-bottom: 12px !important;
  color: #333 !important;
  font-size: 14px !important;
}

.statistics-item {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  padding: 6px 0 !important;
  border-bottom: 1px solid #f0f0f0 !important;
}

.statistics-item:last-child {
  border-bottom: none !important;
}

.keyword-label {
  color: #666 !important;
  font-size: 13px !important;
  font-weight: 500 !important;
}

.count-tag {
  margin: 0 !important;
  font-weight: 500 !important;
  border-radius: 4px !important;
}

/* 简洁统计概览样式 */
.simplified-stats-section {
  margin-bottom: 24px;
}

.simplified-stats-section .ant-card {
  text-align: center;
}

.simplified-stats-section .ant-statistic-title {
  font-size: 14px;
  color: #666;
}

.simplified-stats-section .ant-statistic-content {
  font-size: 24px;
  font-weight: bold;
}

/* 各国数据统计简洁显示样式 */
.country-stats-summary {
  margin-top: 16px;
}

.country-summary-item {
  text-align: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.country-summary-item:hover {
  background: #f0f0f0;
  border-color: #d9d9d9;
}

.country-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.country-total-simple {
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

/* 各国数据统计样式 */
.country-stats-section {
  margin-bottom: 24px;
}

.country-stats-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.country-stat-card {
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.country-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.country-data-overview {
  padding: 8px 0;
}

.data-type-item {
  text-align: center;
  padding: 8px 4px;
  border-radius: 4px;
  background: #f8f9fa;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.data-type-item:hover {
  background: #e9ecef;
  transform: scale(1.02);
}

.data-type-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  font-weight: 500;
}

.data-type-count {
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
}

.country-total {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}

.total-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.total-count {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  margin-left: 8px;
}

/* 详情模态框样式 */
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.long-field-content {
  word-break: break-word;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
}

.detail-content :deep(.ant-descriptions-item-label) {
  font-weight: 600;
  color: #333;
  background-color: #fafafa;
}

.detail-content :deep(.ant-descriptions-item-content) {
  color: #666;
}

/* 黑名单关键词样式 */
.blacklist-keywords-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  min-height: 32px;
  padding: 8px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background-color: #fafafa;
}

.blacklist-keyword-tag {
  background-color: #fff2f0 !important;
  border-color: #ffccc7 !important;
  color: #cf1322 !important;
}

.blacklist-keyword-tag .keyword-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.blacklist-description {
  margin-top: 8px;
}
</style>



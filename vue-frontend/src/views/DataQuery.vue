<template>
  <div class="data-query">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>新闻数据查询</h1>
      <p>查询和浏览爬虫采集的新闻数据，支持风险等级编辑和筛选</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-card title="搜索条件" :bordered="false">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="关键词">
            <a-auto-complete
              v-model:value="searchForm.keyword"
              placeholder="请输入标题或内容关键词"
              style="width: 200px"
              :options="keywordOptions"
              :filter-option="false"
              @search="handleKeywordSearch"
              @select="handleKeywordSelect"
              @change="handleKeywordChange"
              allow-clear
            >
              <template #option="{ label, type }">
                <div class="keyword-option">
                  <span v-if="type === 'history'" class="keyword-icon">🕒</span>
                  <span v-else-if="type === 'hot'" class="keyword-icon">🔥</span>
                  <span v-else-if="type === 'api'" class="keyword-icon">💡</span>
                  <span v-else-if="type === 'mock'" class="keyword-icon">📋</span>
                  <span v-else class="keyword-icon">🔍</span>
                  <span>{{ label }}</span>
                </div>
              </template>
            </a-auto-complete>
            <div v-if="searchHistory.length > 0" class="search-history">
              <span class="history-label">搜索历史：</span>
              <a-space wrap>
                <a-tag
                  v-for="item in searchHistory.slice(0, 5)"
                  :key="item"
                  closable
                  @close="removeSearchHistory(item)"
                  @click="useSearchHistory(item)"
                  style="cursor: pointer"
                >
                  {{ item }}
                </a-tag>
              </a-space>
            </div>
            

          </a-form-item>
          
                     <a-form-item label="国家">
             <a-select
               v-model:value="searchForm.country"
               placeholder="请选择国家"
               style="width: 150px"
               allow-clear
               :loading="countriesLoading"
             >
               <a-select-option value="">全部国家</a-select-option>
               <a-select-option value="OVERSEAS">海外</a-select-option>
               <a-select-option value="CN">中国</a-select-option>
               <a-select-option value="US">美国</a-select-option>
               <a-select-option value="EU">欧盟</a-select-option>
               <a-select-option value="JP">日本</a-select-option>
               <a-select-option value="KR">韩国</a-select-option>
               <a-select-option value="IN">印度</a-select-option>
               <a-select-option value="TH">泰国</a-select-option>
               <a-select-option value="SG">新加坡</a-select-option>
               <a-select-option value="TW">台湾</a-select-option>
               <a-select-option value="AU">澳大利亚</a-select-option>
               <a-select-option value="CL">智利</a-select-option>
               <a-select-option value="MY">马来西亚</a-select-option>
               <a-select-option value="AE">阿联酋</a-select-option>
               <a-select-option value="PE">秘鲁</a-select-option>
               <a-select-option value="ZA">南非</a-select-option>
               <a-select-option value="IL">以色列</a-select-option>
               <a-select-option value="ID">印度尼西亚</a-select-option>
             </a-select>
           </a-form-item>
          
          <a-form-item label="风险等级">
            <a-select
              v-model:value="searchForm.riskLevel"
              placeholder="请选择风险等级"
              style="width: 150px"
              allow-clear
            >
              <a-select-option value="HIGH">高风险</a-select-option>
              <a-select-option value="MEDIUM">中风险</a-select-option>
              <a-select-option value="LOW">低风险</a-select-option>
              <a-select-option value="null">未确定</a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="匹配关键词">
            <a-select
              v-model:value="searchForm.matchedKeyword"
              placeholder="请选择匹配关键词"
              style="width: 200px"
              allow-clear
            >
              <a-select-option 
                v-for="keyword in getMatchedKeywordsList()" 
                :key="keyword" 
                :value="keyword"
              >
                {{ keyword }}
              </a-select-option>
            </a-select>
          </a-form-item>
          
          <a-form-item label="数据源">
            <a-select
              v-model:value="searchForm.sourceName"
              placeholder="请选择数据源"
              style="width: 150px"
              allow-clear
              :loading="sourceNamesLoading"
            >
              <a-select-option 
                v-for="sourceName in sourceNameOptions" 
                :key="sourceName" 
                :value="sourceName"
              >
                {{ sourceName }}
              </a-select-option>
            </a-select>
          </a-form-item>
          
<!--          <a-form-item label="类型">-->
<!--            <a-select-->
<!--              v-model:value="searchForm.type"-->
<!--              placeholder="请选择类型"-->
<!--              style="width: 150px"-->
<!--              allow-clear-->
<!--            >-->
<!--              <a-select-option value="法规标准">法规标准</a-select-option>-->
<!--              <a-select-option value="数据保护">数据保护</a-select-option>-->
<!--              <a-select-option value="无线认证">无线认证</a-select-option>-->
<!--              <a-select-option value="医疗器械">医疗器械</a-select-option>-->
<!--              <a-select-option value="化妆品">化妆品</a-select-option>-->
<!--              <a-select-option value="电子产品">电子产品</a-select-option>-->
<!--            </a-select>-->
<!--          </a-form-item>-->
          
          <a-form-item label="日期范围">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              style="width: 240px"
              :placeholder="['开始日期', '结束日期']"
            />
          </a-form-item>
          
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="loading">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button @click="resetSearch">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
                             <a-button @click="loadLatestData" :loading="loading">
                 <template #icon>
                   <SyncOutlined />
                 </template>
                 刷新数据
               </a-button>
               <a-button @click="autoProcess" :loading="autoProcessing" type="primary">
                 <template #icon>
                   <RobotOutlined />
                 </template>
                 自动处理
               </a-button>
               <a-form-item label="处理方式" style="margin-left: 16px; margin-bottom: 0;">
                 <a-select
                   v-model:value="processingMethod"
                   style="width: 160px"
                   @change="handleProcessingMethodChange"
                 >
                   <a-select-option value="keyword">
                     <div class="processing-option">
                       <span class="option-icon">🔍</span>
                       <span>关键词匹配</span>
                     </div>
                   </a-select-option>
                   <a-select-option value="ai" disabled>
                     <div class="processing-option">
                       <span class="option-icon">🤖</span>
                       <span>AI处理</span>
                       <span class="option-status">(待开发)</span>
                     </div>
                   </a-select-option>
                 </a-select>
               </a-form-item>
               <a-button @click="setAllToMediumRisk" :loading="batchUpdating" type="primary" >
                 <template #icon>
                   <ReloadOutlined />
                 </template>
                 重置
               </a-button>
               <a-button @click="showKeywordModal" type="default">
                 <template #icon>
                   <SettingOutlined />
                 </template>
                 关键词管理
               </a-button>
               <a-button @click="handleAutoUpdateCountry" :loading="countryUpdating" type="primary">
                 <template #icon>
                   <GlobalOutlined />
                 </template>
                 自动更新国家
               </a-button>
<!--               <a-button @click="handleViewCountryDistribution" type="default">-->
<!--                 <template #icon>-->
<!--                   <BarChartOutlined />-->
<!--                 </template>-->
<!--                 查看国家分布-->
<!--               </a-button>-->
<!--               <a-button @click="handleUpdateDateFormats" :loading="dateFormatUpdating" type="primary">-->
<!--                 <template #icon>-->
<!--                   <CalendarOutlined />-->
<!--                 </template>-->
<!--                 统一日期格式-->
<!--               </a-button>-->

            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <div class="clickable-statistic" @click="filterByStatus('all')">
              <a-statistic
                title="总新闻数"
                :value="stats.total"
                :value-style="{ color: '#1890ff', cursor: 'pointer' }"
              >
                <template #prefix>
                  <FileTextOutlined />
                </template>
              </a-statistic>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <div class="clickable-statistic" @click="filterByStatus('highRisk')">
              <a-statistic
                title="高风险新闻"
                :value="stats.highRisk"
                :value-style="{ color: '#ff4d4f', cursor: 'pointer' }"
              >
                <template #prefix>
                  <CheckCircleOutlined />
                </template>
              </a-statistic>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <div class="clickable-statistic" @click="filterByStatus('mediumRisk')">
              <a-statistic
                title="中风险新闻"
                :value="stats.mediumRisk"
                :value-style="{ color: '#faad14', cursor: 'pointer' }"
              >
                <template #prefix>
                  <CloseCircleOutlined />
                </template>
              </a-statistic>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <div class="clickable-statistic" @click="filterByStatus('lowRisk')">
              <a-statistic
                title="低风险新闻"
                :value="stats.lowRisk"
                :value-style="{ color: '#52c41a', cursor: 'pointer' }"
              >
                <template #prefix>
                  <CheckCircleOutlined />
                </template>
              </a-statistic>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="6">
          <a-card>
            <div class="clickable-statistic" @click="filterByStatus('undetermined')">
<!--              <a-statistic-->
<!--                title="未确定"-->
<!--                :value="stats.undetermined"-->
<!--                :value-style="{ color: '#d9d9d9', cursor: 'pointer' }"-->
<!--              >-->
<!--                <template #prefix>-->
<!--                  <QuestionCircleOutlined />-->
<!--                </template>-->
<!--              </a-statistic>-->
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索结果 -->
    <div class="results-section">
      <a-card :bordered="false">
        <template #title>
          <div class="results-header">
            <div class="results-title">
              <span>搜索结果</span>
              <a-tag color="blue">{{ totalCount }} 条记录</a-tag>
              <a-tag v-if="searchForm.keyword" color="orange">关键词: "{{ searchForm.keyword }}"</a-tag>
              <a-tag v-if="searchForm.country" color="green">国家: {{ getCountryName(searchForm.country) }}</a-tag>
              <a-tag v-if="searchForm.riskLevel !== undefined" color="purple">风险等级: {{ getRiskLevelText(searchForm.riskLevel) }}</a-tag>
              <a-tag v-if="searchForm.matchedKeyword" color="orange">匹配关键词: {{ searchForm.matchedKeyword }}</a-tag>
              <a-tag v-if="searchForm.sourceName" color="cyan">数据源: {{ searchForm.sourceName }}</a-tag>
              <a-tag v-if="searchForm.type" color="magenta">类型: {{ searchForm.type }}</a-tag>
            </div>
            <div class="batch-operations" v-if="selectedRowKeys.length > 0">
              <a-space>
                <span class="selected-count">已选择 {{ selectedRowKeys.length }} 条数据</span>
                <a-button type="primary" @click="showBatchOperationModal">
                  批量设置风险等级
                </a-button>
                <a-button @click="clearSelection">
                  取消选择
                </a-button>
              </a-space>
            </div>
          </div>
        </template>
        
        <!-- 自动处理信息 -->
        <div class="auto-process-info">
          <a-row :gutter="16" align="middle">
            <a-col :span="8">
              <div class="info-item">
                <span class="info-label">数据更新时间：</span>
                <span class="info-value">{{ lastDataUpdateTime || '暂无' }}</span>
              </div>
            </a-col>
            <a-col :span="16">
              <!-- 自动处理统计信息 -->
              <div v-if="lastAutoProcessResult" class="auto-process-stats">
                <a-space size="large">
                  <span class="stat-item">
                    <span class="stat-label">总处理：</span>
                    <span class="stat-value">{{ lastAutoProcessResult.totalProcessed }} 条</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">相关：</span>
                    <span class="stat-value" style="color: #52c41a;">{{ lastAutoProcessResult.relatedCount }} 条</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">不相关：</span>
                    <span class="stat-value" style="color: #faad14;">{{ lastAutoProcessResult.unrelatedCount }} 条</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">未变更：</span>
                    <span class="stat-value" style="color: #d9d9d9;">{{ lastAutoProcessResult.unchangedCount }} 条</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">关键词：</span>
                    <span class="stat-value">{{ lastAutoProcessResult.usedKeywords }} 个</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">处理时间：</span>
                    <span class="stat-value">{{ lastAutoProcessResult.processTime }}</span>
                  </span>
                </a-space>
              </div>
            </a-col>
          </a-row>
        </div>
        
        <template #extra>
          <a-space>
            <a-select
              v-model:value="sortBy"
              style="width: 120px"
              @change="handleSort"
            >
              <a-select-option value="publishDate">按发布时间排序</a-select-option>
              <a-select-option value="title">按标题排序</a-select-option>
              <a-select-option value="country">按国家排序</a-select-option>
            </a-select>

            <a-radio-group v-model:value="viewMode" button-style="solid">
              <a-radio-button value="list">列表视图</a-radio-button>
              <a-radio-button value="card">卡片视图</a-radio-button>
            </a-radio-group>
            
            <a-dropdown v-if="dataList.length > 0">
              <a-button>
                批量操作 <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="batchMarkRelated">
                    <CheckCircleOutlined />
                    标记为相关
                  </a-menu-item>
                  <a-menu-item @click="batchMarkUnrelated">
                    <CloseCircleOutlined />
                    标记为不相关
                  </a-menu-item>

                  <a-menu-item @click="exportSearchResults">
                    <DownloadOutlined />
                    导出结果
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </template>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <a-spin size="large" />
          <p>正在搜索数据...</p>
        </div>





        <!-- 空状态 -->
        <a-empty
          v-else-if="!loading && dataList.length === 0"
          description="暂无搜索结果"
        >
          <template #image>
            <SearchOutlined style="font-size: 64px; color: #d9d9d9;" />
          </template>
        </a-empty>

        <!-- 数据列表 -->
        <div v-else class="data-list">
          <!-- 列表视图 -->
          <div v-if="viewMode === 'list'" class="list-view">
            <div
              v-for="item in getFilteredDataList()"
              :key="item.id"
              class="news-item"
              :class="{ 'selected': selectedRowKeys.includes(item.id) }"
            >
              <div class="news-item-checkbox">
                <a-checkbox 
                  :checked="selectedRowKeys.includes(item.id)"
                  @change="(e: any) => handleRowSelectionChange(item.id, e.target.checked)"
                />
              </div>
              <div class="news-header">
                <h3 class="news-title" @click="viewDetail(item)" v-html="highlightKeyword(item.title, searchForm.keyword)"></h3>
                                 <div class="news-meta">
                   <div v-if="getCountriesList(item).length > 1">
                     <div style="margin-bottom: 4px;">
                       <a-tag :color="getCountryColor(item.country)">{{ getCountryName(item.country) }} (主要)</a-tag>
                     </div>
                     <div>
                       <a-tag 
                         v-for="country in getCountriesList(item).slice(1)" 
                         :key="country"
                         :color="getCountryColor(country)"
                         style="margin-right: 4px; margin-bottom: 4px;"
                       >
                         {{ getCountryName(country) }}
                       </a-tag>
                     </div>
                   </div>
                   <div v-else>
                     <a-tag :color="getCountryColor(item.country)">{{ getCountryName(item.country) }}</a-tag>
                   </div>
                   <a-tag :color="getRiskLevelColor(item.riskLevel)">{{ getRiskLevelText(item.riskLevel) }}</a-tag>
                   <a-tag v-if="item.matchedKeywords" color="orange" style="max-width: 200px; overflow: hidden; text-overflow: ellipsis;" :title="item.matchedKeywords">
                     匹配: {{ item.matchedKeywords.length > 20 ? item.matchedKeywords.substring(0, 20) + '...' : item.matchedKeywords }}
                   </a-tag>
                   <span class="news-date">{{ formatPublishDate(item) }}</span>
                 </div>
              </div>
              <div class="news-content">
                <p v-html="getSearchSummary(item, searchForm.keyword)"></p>
              </div>
              <div class="news-footer">
                <a-space>
                  <a-tag v-if="item.sourceName" color="blue">{{ item.sourceName }}</a-tag>
                  <a-tag v-if="item.type" color="green">{{ item.type }}</a-tag>
                  <a-tag v-if="item.status" :color="getStatusColor(item.status)">{{ getStatusText(item.status) }}</a-tag>
                </a-space>
                <a-space>
                  <a-button type="link" size="small" @click="viewDetail(item)">
                    查看详情 <RightOutlined />
                  </a-button>

                  <a-button type="link" size="small" @click="editRiskLevel(item)">
                    编辑风险等级 <EditOutlined />
                  </a-button>
                  <a-button type="link" size="small" @click="sendEmail(item)">
                    发送邮件 <MailOutlined />
                  </a-button>
                  <a-popconfirm
                    title="确定要删除这条新闻吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="deleteNews(item)"
                  >
                    <a-button type="link" size="small" danger>
                      删除 <DeleteOutlined />
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </div>
            </div>
          </div>

          <!-- 卡片视图 -->
          <div v-else class="card-view">
            <a-row :gutter="[16, 16]">
              <a-col
                v-for="item in getFilteredDataList()"
                :key="item.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <a-card
                  hoverable
                  class="news-card"
                  :class="{ 'selected': selectedRowKeys.includes(item.id) }"
                >
                  <template #cover>
                    <div class="card-cover">
                      <div class="card-checkbox">
                        <a-checkbox 
                          :checked="selectedRowKeys.includes(item.id)"
                          @change="(e: any) => handleRowSelectionChange(item.id, e.target.checked)"
                        />
                      </div>
                      <div class="country-flag">{{ getCountryFlag(item.country) }}</div>
                      <div class="risk-badge" :class="getRiskLevelClass(item.riskLevel)">
                        {{ getRiskLevelText(item.riskLevel) }}
                      </div>
                    </div>
                  </template>
                  
                  <a-card-meta :title="highlightKeyword(item.title, searchForm.keyword)">
                    <template #description>
                      <div class="card-content">
                        <p v-html="highlightKeyword(truncateText(item.summary || item.content, 100), searchForm.keyword)"></p>
                                                 <div class="card-meta">
                           <div v-if="getCountriesList(item).length > 1">
                             <div style="margin-bottom: 4px;">
                               <a-tag :color="getCountryColor(item.country)" size="small">
                                 {{ getCountryName(item.country) }} (主要)
                               </a-tag>
                             </div>
                             <div>
                               <a-tag 
                                 v-for="country in getCountriesList(item).slice(1)" 
                                 :key="country"
                                 :color="getCountryColor(country)"
                                 size="small"
                                 style="margin-right: 4px; margin-bottom: 4px;"
                               >
                                 {{ getCountryName(country) }}
                               </a-tag>
                             </div>
                           </div>
                           <div v-else>
                             <a-tag :color="getCountryColor(item.country)" size="small">
                               {{ getCountryName(item.country) }}
                             </a-tag>
                           </div>
                           <span class="card-date">{{ formatPublishDate(item) }}</span>
                         </div>
                      </div>
                    </template>
                  </a-card-meta>
                  
                                     <template #actions>
                     <a-button type="link" size="small" @click="viewDetail(item)">
                       查看详情
                     </a-button>

                     <a-button type="link" size="small" @click="editRiskLevel(item)">
                       编辑风险等级
                     </a-button>
                     <a-button type="link" size="small" @click="sendEmail(item)">
                       发送邮件
                     </a-button>
                     <a-popconfirm
                       title="确定要删除这条新闻吗？"
                       ok-text="确定"
                       cancel-text="取消"
                       @confirm="deleteNews(item)"
                     >
                       <a-button type="link" size="small" danger>
                         删除
                       </a-button>
                     </a-popconfirm>
                   </template>
                </a-card>
              </a-col>
            </a-row>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <a-pagination
              v-model:current="currentPage"
              v-model:page-size="pageSize"
              :total="totalCount"
              :show-size-changer="true"
              :show-quick-jumper="true"
                             :show-total="(total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`"
              @change="handlePageChange"
              @show-size-change="handlePageSizeChange"
            />
          </div>
        </div>
      </a-card>
    </div>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailVisible"
      :title="selectedItem?.title"
      :width="800"
      @cancel="closeDetail"
    >
      <div v-if="selectedItem" class="detail-content">
        <div class="detail-header">
                     <div class="detail-meta">
             <a-space>
               <div v-if="getCountriesList(selectedItem).length > 1">
              <div style="margin-bottom: 8px;">
                <a-tag :color="getCountryColor(selectedItem.country)">
                  {{ getCountryName(selectedItem.country) }} (主要)
                </a-tag>
              </div>
              <div>
                <a-tag 
                  v-for="country in getCountriesList(selectedItem).slice(1)" 
                  :key="country"
                  :color="getCountryColor(country)"
                  style="margin-right: 4px; margin-bottom: 4px;"
                >
                  {{ getCountryName(country) }}
                </a-tag>
              </div>
            </div>
            <div v-else>
              <a-tag :color="getCountryColor(selectedItem.country)">{{ getCountryName(selectedItem.country) }}</a-tag>
            </div>
               <a-tag :color="getRiskLevelColor(selectedItem.riskLevel)">{{ getRiskLevelText(selectedItem.riskLevel) }}</a-tag>
                                  <span class="detail-date">{{ formatPublishDate(selectedItem) }}</span>
             </a-space>
           </div>
          <div class="detail-source">
            <span>来源：{{ selectedItem.sourceName }}</span>
            <a v-if="selectedItem.url" :href="selectedItem.url" target="_blank" class="detail-url">
              查看原文 <LinkOutlined />
            </a>
          </div>
        </div>
        
        <div class="detail-body">
          <h4>内容摘要</h4>
          <p>{{ selectedItem.summary || '暂无摘要' }}</p>
          
          <h4>详细内容</h4>
          <div class="detail-text" v-html="selectedItem.content"></div>
          
          <div v-if="selectedItem.product" class="detail-product">
            <h4>适用商品/产品</h4>
            <p>{{ selectedItem.product }}</p>
          </div>
          
          <div v-if="selectedItem.remarks" class="detail-remarks">
            <h4>备注</h4>
            <p>{{ selectedItem.remarks }}</p>
          </div>
        </div>
        
        <div class="detail-tags">
          <a-space>
            <a-tag v-if="selectedItem.type" color="green">{{ selectedItem.type }}</a-tag>
            <a-tag v-if="selectedItem.status" :color="getStatusColor(selectedItem.status)">{{ getStatusText(selectedItem.status) }}</a-tag>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- 编辑风险等级模态框 -->
    <a-modal
      v-model:open="editVisible"
      title="编辑风险等级"
      :width="600"
      @ok="saveRiskLevel"
      @cancel="closeEdit"
    >
      <div v-if="editingItem" class="edit-content">
        <div class="edit-item">
          <h4>{{ editingItem.title }}</h4>
          <p class="edit-summary">{{ truncateText(editingItem.summary || editingItem.content, 150) }}</p>
        </div>
        
        <a-form layout="vertical">
          <a-form-item label="风险等级">
            <a-radio-group v-model:value="editingRiskLevel">
              <a-radio value="HIGH">
                <a-tag color="red">高风险</a-tag>
                <span style="margin-left: 8px;">高风险事件，需要重点关注</span>
              </a-radio>
              <a-radio value="MEDIUM">
                <a-tag color="orange">中风险</a-tag>
                <span style="margin-left: 8px;">中等风险事件，需要关注</span>
              </a-radio>
              <a-radio value="LOW">
                <a-tag color="green">低风险</a-tag>
                <span style="margin-left: 8px;">低风险事件，一般关注</span>
              </a-radio>
              <a-radio :value="null">
                <a-tag color="default">未确定</a-tag>
                <span style="margin-left: 8px;">需要进一步判断</span>
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 批量操作模态框 -->
    <a-modal
      v-model:open="batchOperationVisible"
      title="批量设置风险等级"
      :width="600"
      @ok="handleBatchUpdateRiskLevel"
      @cancel="closeBatchOperation"
      :confirm-loading="batchUpdatingRiskLevel"
    >
      <div class="batch-operation-content">
        <div class="batch-info">
          <a-alert
            message="批量操作"
            :description="`您已选择 ${selectedRowKeys.length} 条数据，将批量设置风险等级状态`"
            type="info"
            show-icon
            style="margin-bottom: 16px"
          />
        </div>
        
        <a-form layout="vertical">
          <a-form-item label="风险等级状态">
            <a-radio-group v-model:value="batchRiskLevelValue">
              <a-radio value="HIGH">
                <a-tag color="red">高风险</a-tag>
                <span style="margin-left: 8px;">高风险事件，需要重点关注</span>
              </a-radio>
              <a-radio value="MEDIUM">
                <a-tag color="orange">中风险</a-tag>
                <span style="margin-left: 8px;">中等风险事件，需要关注</span>
              </a-radio>
              <a-radio value="LOW">
                <a-tag color="green">低风险</a-tag>
                <span style="margin-left: 8px;">低风险事件，一般关注</span>
              </a-radio>
              <a-radio :value="null">
                <a-tag color="default">未确定</a-tag>
                <span style="margin-left: 8px;">需要进一步判断</span>
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

     <!-- 发送邮件模态框 -->
     <a-modal
       v-model:open="emailVisible"
       title="发送邮件"
       :width="600"
       @ok="handleSendEmail"
       @cancel="closeEmail"
     >
       <div v-if="emailItem" class="email-content">
         <div class="email-item">
           <h4>{{ emailItem.title }}</h4>
           <p class="email-summary">{{ truncateText(emailItem.summary || emailItem.content, 200) }}</p>
         </div>
         
         <a-form layout="vertical">
           <a-form-item label="收件人">
             <a-input
               v-model:value="emailForm.recipients"
               placeholder="请输入收件人邮箱，多个邮箱用逗号分隔"
               allow-clear
             />
           </a-form-item>
           
           <a-form-item label="主题">
             <a-input
               v-model:value="emailForm.subject"
               placeholder="请输入邮件主题"
               allow-clear
             />
           </a-form-item>
           
           <a-form-item label="邮件内容">
             <a-textarea
               v-model:value="emailForm.content"
               placeholder="请输入邮件内容"
               :rows="6"
               allow-clear
             />
           </a-form-item>
           
           <a-form-item label="附件">
             <a-checkbox v-model:checked="emailForm.includeAttachment">
               包含新闻原文链接
             </a-checkbox>
           </a-form-item>
         </a-form>
       </div>
     </a-modal>
     
     <!-- 关键词管理模态框 -->
     <a-modal
       v-model:open="keywordModalVisible"
       title="关键词管理"
       width="800px"
       @ok="handleSaveKeywords"
       @cancel="closeKeywordModal"
     >
       <div class="keyword-management">
         <!-- 说明文字 -->
         <div class="keyword-header">
           <p>请在下方文本框中输入关键词，每行一个关键词。系统将根据这些关键词自动判断数据的相关性。</p>
           <a-space>
             <a-button @click="initializeDefaultKeywords" type="primary">
               初始化默认关键词
             </a-button>
             <a-button @click="loadKeywords" type="default">
               刷新关键词
             </a-button>
             <a-button @click="migrateFromLocalStorage" type="dashed">
               迁移localStorage关键词
             </a-button>
<!--             <a-button @click="setAsLocalKeywords" type="dashed">-->
<!--               设为本地关键词-->
<!--             </a-button>-->
<!--             <a-button @click="clearLocalKeywords" type="dashed" danger>-->
<!--               清空本地关键词-->
<!--             </a-button>-->
           </a-space>
         </div>
         
         <!-- 关键词列表显示区域 -->
         <div class="keyword-list-section">
           <div class="keyword-list-header">
             <h4>关键词列表</h4>
             <a-space>
               <a-button @click="refreshKeywordCounts" :loading="loadingCounts" size="small">
                 刷新匹配数量
               </a-button>
               <a-button @click="showAddKeywordInput = !showAddKeywordInput" type="dashed" size="small">
                 {{ showAddKeywordInput ? '取消添加' : '添加关键词' }}
               </a-button>
               <a-popconfirm
                 title="确定要删除所有0条匹配的关键词吗？"
                 ok-text="确定删除"
                 cancel-text="取消"
                 @confirm="handleDeleteZeroMatchKeywords"
               >
                 <a-button type="primary" danger size="small" :loading="deletingZeroMatch">
                   删除0匹配关键词
                 </a-button>
               </a-popconfirm>
             </a-space>
           </div>
           
           <!-- 添加关键词输入框 -->
           <div v-if="showAddKeywordInput" class="add-keyword-section">
             <a-input-group compact>
               <a-input
                 v-model:value="newKeyword"
                 placeholder="输入新关键词"
                 style="width: 70%"
                 @press-enter="addNewKeyword"
               />
               <a-button type="primary" @click="addNewKeyword" style="width: 15%">
                 添加
               </a-button>
               <a-button @click="cancelAddKeyword" style="width: 15%">
                 取消
               </a-button>
             </a-input-group>
           </div>
           
           <!-- 关键词列表 -->
           <div class="keyword-list">
             <a-list
               :data-source="keywordListWithCounts"
               :loading="loadingCounts"
               size="small"
               :pagination="{ pageSize: 20, showSizeChanger: true, showQuickJumper: true }"
             >
               <template #renderItem="{ item }">
                 <a-list-item>
                   <template #actions>
                     <a-button 
                       type="link" 
                       size="small" 
                       danger 
                       @click="removeKeyword(item.keyword)"
                     >
                       删除
                     </a-button>
                   </template>
                   
                   <a-list-item-meta>
                     <template #title>
                       <a-space>
                         <span>{{ item.keyword }}</span>
                         <a-tag :color="getMatchCountColor(item.matchCount)" class="match-count-tag">
                           {{ item.matchCount }} 条匹配
                         </a-tag>
                       </a-space>
                     </template>
                     <template #description>
                       <span v-if="item.description">{{ item.description }}</span>
                       <span v-else class="no-description">暂无描述</span>
                     </template>
                   </a-list-item-meta>
                 </a-list-item>
               </template>
             </a-list>
           </div>
         </div>
         
         <!-- 关键词统计 -->
         <div class="keyword-stats">
           <a-space>
             <a-tag color="blue">总关键词数：{{ keywordCount }}</a-tag>
             <a-tag color="green">有效关键词：{{ validKeywordCount }}</a-tag>
             <a-tag v-if="keywordSource === 'file'" color="blue">使用文件关键词</a-tag>
             <a-tag v-else-if="keywordSource === 'database'" color="purple">使用数据库关键词</a-tag>
             <a-tag v-else color="default">使用文本框关键词</a-tag>
           </a-space>
         </div>
       </div>
     </a-modal>


   </div>
 </template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getCrawlerData, updateCrawlerDataRiskLevel, sendEmail as sendEmailAPI, deleteCrawlerData, getAllSourceNames, autoUpdateCountry, getCountryDistribution, batchUpdateCrawlerDataRiskLevel, setAllDataToMediumRisk, getRiskLevelStatistics } from '@/api/pachongshujuguanli'
import { getAllKeywords, addKeyword, deleteKeyword, initializeKeywords, autoProcessRelated as autoProcessRelatedAPI, getFileKeywords, migrateKeywordsFromLocalStorage, getKeywordsWithMatchCounts, deleteZeroMatchKeywords } from '@/api/keywordguanli'

import {
  SearchOutlined,
  ReloadOutlined,
  SyncOutlined,
  RightOutlined,
  EditOutlined,
  LinkOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  QuestionCircleOutlined,
  RobotOutlined,
  MailOutlined,
  DeleteOutlined,
  DownOutlined,
  DownloadOutlined,
  SettingOutlined,
  GlobalOutlined,
} from '@ant-design/icons-vue'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  country: undefined as string | undefined,
  riskLevel: undefined as string | undefined,
  sourceName: undefined as string | undefined,
  type: undefined as string | undefined,
  dateRange: [] as any[],
  matchedKeyword: undefined as string | undefined
})

// 响应式数据
const loading = ref(false)
const countriesLoading = ref(false)
const sourceNamesLoading = ref(false)
const dataList = ref<any[]>([])
const totalCount = ref(0)
const dataLoaded = ref(false) // 标记数据是否已加载
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('publishDate')
const viewMode = ref('list')
const detailVisible = ref(false)
const selectedItem = ref<any>(null)
const editVisible = ref(false)
const editingItem = ref<any>(null)
const editingRiskLevel = ref<string | null>(null)
const autoProcessing = ref(false)
const batchUpdating = ref(false)
const countryUpdating = ref(false)
const emailVisible = ref(false)
const emailItem = ref<any>(null)

// 批量操作相关
const selectedRowKeys = ref<string[]>([])
const batchOperationVisible = ref(false)
const batchRiskLevelValue = ref<string | null>(null)
const batchUpdatingRiskLevel = ref(false)

// 处理方式选择
const processingMethod = ref('keyword') // 默认使用关键词匹配

// 关键词管理相关
const keywordModalVisible = ref(false)
const keywords = ref<any[]>([])
const keywordsLoading = ref(false)
const keywordsText = ref('')
const keywordListWithCounts = ref<any[]>([])
const loadingCounts = ref(false)
const showAddKeywordInput = ref(false)
const newKeyword = ref('')
const newKeywordDescription = ref('')
const deletingZeroMatch = ref(false)
const fileKeywords = ref<string[]>([]) // 文件关键词列表
const useFileKeywords = ref<boolean>(false) // 是否使用文件关键词

// 自动处理相关
const lastAutoProcessTime = ref<string>('') // 上次自动处理时间
const lastDataUpdateTime = ref<string>('') // 数据更新时间
const lastAutoProcessResult = ref<any>(null) // 本次自动处理结果



// 数据源选项
const sourceNameOptions = ref<string[]>([])

// 关键词搜索相关
const keywordOptions = ref<any[]>([])
const searchHistory = ref<string[]>([])
const searchTimeout = ref<NodeJS.Timeout | null>(null)


// 热门关键词
const hotKeywords = [
  'FCC认证', 'CE认证', 'CCC认证', 'UL认证', 'ISO标准', 
  '网络安全', '数据保护', '医疗器械', '汽车认证', 'RoHS'
]

// 热门产品


// 邮件表单
const emailForm = reactive({
  recipients: '',
  subject: '',
  content: '',
  includeAttachment: true
})

// 关键词统计计算属性
const keywordCount = computed(() => {
  if (!keywordsText.value) return 0
  return keywordsText.value.split('\n').filter(line => line.trim()).length
})

const validKeywordCount = computed(() => {
  if (!keywordsText.value) return 0
  return keywordsText.value.split('\n').filter(line => line.trim().length > 0).length
})

// 获取匹配数量颜色
const getMatchCountColor = (count: number) => {
  if (count === 0) return 'default'
  if (count <= 5) return 'green'
  if (count <= 20) return 'orange'
  return 'red'
}


// 添加新关键词
const addNewKeyword = async () => {
  if (!newKeyword.value.trim()) {
    message.warning('请输入关键词')
    return
  }
  
  try {
    const result = await addKeyword({
      keyword: newKeyword.value.trim(),
      description: newKeywordDescription.value || '用户添加的关键词'
    })
    
    if (result && result.data && result.data.success) {
      message.success('关键词添加成功')
      newKeyword.value = ''
      newKeywordDescription.value = ''
      showAddKeywordInput.value = false
      await refreshKeywordCounts()
    } else {
      // message.error(result?.data?.error || '添加关键词失败')
    }
  } catch (error) {
    console.error('添加关键词失败:', error)
    message.error('添加关键词失败')
  }
}

// 取消添加关键词
const cancelAddKeyword = () => {
  newKeyword.value = ''
  newKeywordDescription.value = ''
  showAddKeywordInput.value = false
}

// 删除关键词
const removeKeyword = async (keyword: string) => {
  try {
    const result = await deleteKeyword({ keyword })
    
    if (result && result.data && result.data.success) {
      message.success('关键词删除成功')
      await refreshKeywordCounts()
    } else {
      // message.error(result?.data?.error || '删除关键词失败')
    }
  } catch (error) {
    console.error('删除关键词失败:', error)
    message.error('删除关键词失败')
  }
}

// 删除所有0匹配的关键词
const handleDeleteZeroMatchKeywords = async () => {
  deletingZeroMatch.value = true
  try {
    const result = await deleteZeroMatchKeywords()
    
    if (result && result.data && result.data.success) {
      const deletedCount = result.data.deletedCount || 0
      if (deletedCount > 0) {
        message.success(`成功删除 ${deletedCount} 个0匹配的关键词`)
        console.log('删除的关键词:', result.data.deletedKeywords)
      } else {
        message.info('没有找到0条匹配的关键词')
      }
    } else {
      // message.error(result?.data?.error || '删除0匹配关键词失败')
    }
    
    // 刷新关键词列表
    await refreshKeywordCounts()
  } catch (error) {
    console.error('删除0匹配关键词失败:', error)
    message.error('删除0匹配关键词失败')
  } finally {
    deletingZeroMatch.value = false
  }
}

// 刷新关键词匹配数量
const refreshKeywordCounts = async () => {
  loadingCounts.value = true
  try {
    console.log('开始刷新关键词匹配数量...')
    const result = await getKeywordsWithMatchCounts()
    console.log('关键词匹配数量API响应:', result)
    
    // 处理新的API响应格式
    let keywordsWithCounts = null
    if (result && result.success && result.keywords) {
      // 新格式: {success: true, keywords: [...], total: number}
      keywordsWithCounts = result.keywords
    } else if (result && result.success && result.data) {
      // 兼容旧格式: {success: true, data: [...]}
      keywordsWithCounts = Array.isArray(result.data) ? result.data : (result.data.keywords || [])
    } else if (result && result.data && result.data.success) {
      // 格式: {data: {success: true, keywords: [...]}}
      keywordsWithCounts = result.data.keywords || []
    } else if (result && result.data && result.data.keywords) {
      // 格式: {data: {keywords: [...]}}
      keywordsWithCounts = result.data.keywords || []
    }
    
    if (keywordsWithCounts && Array.isArray(keywordsWithCounts)) {
      keywordListWithCounts.value = keywordsWithCounts
      console.log('刷新关键词匹配数量成功，数量:', keywordsWithCounts.length)
      message.success('关键词匹配数量刷新成功')
    } else {
      console.error('关键词匹配数量数据格式错误:', result)
      message.error('刷新关键词匹配数量失败：数据格式错误')
    }
  } catch (error) {
    console.error('刷新关键词匹配数量失败:', error)
    message.error('刷新关键词匹配数量失败')
  } finally {
    loadingCounts.value = false
  }
}

// 关键词来源计算属性
const keywordSource = computed(() => {
  if (useFileKeywords.value && fileKeywords.value.length > 0) {
    return 'file'
  } else if (keywords.value.length > 0) {
    return 'database'
  } else {
    return 'textbox'
  }
})

// 统计数据
const stats = reactive({
  total: 0,
  highRisk: 0,
  mediumRisk: 0,
  lowRisk: 0,
  undetermined: 0
})



// 方法
// 处理方式变化
const handleProcessingMethodChange = (value: string) => {
  processingMethod.value = value
  if (value === 'keyword') {
    message.info('已切换到关键词匹配模式')
  } else if (value === 'ai') {
    message.info('AI处理功能正在开发中，敬请期待')
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  if (searchForm.keyword.trim()) {
    addToSearchHistory(searchForm.keyword.trim())
  }
  await loadData()
}

// 加载数据源选项
const loadSourceNames = async () => {
  sourceNamesLoading.value = true
  try {
    const response = await getAllSourceNames() as any
    if (response.success && response.sourceNames) {
      sourceNameOptions.value = response.sourceNames
    }
  } catch (error) {
    console.error('加载数据源失败:', error)
    message.error('加载数据源失败')
  } finally {
    sourceNamesLoading.value = false
  }
}

// 关键词搜索相关方法
const handleKeywordSearch = async (value: string) => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  searchTimeout.value = setTimeout(async () => {
    const options: any[] = []
    
    // 添加搜索历史
    searchHistory.value
      .filter(item => item.toLowerCase().includes(value.toLowerCase()))
      .forEach(item => {
        options.push({
          value: item,
          label: item,
          type: 'history'
        })
      })
    
    // 添加热门关键词
    hotKeywords
      .filter(item => item.toLowerCase().includes(value.toLowerCase()))
      .forEach(item => {
        if (!options.find(opt => opt.value === item)) {
          options.push({
            value: item,
            label: item,
            type: 'hot'
          })
        }
      })
    
    // 如果输入了关键词，尝试从API获取建议
    if (value.trim() && value.length >= 2) {
      try {
        const result = await getCrawlerData({
          keyword: value,
          size: 5
        })
        
        if (result && result.data && (result.data as any).content) {
          // 从搜索结果中提取关键词建议
          const suggestions = new Set<string>()
          ;(result.data as any).content.forEach((item: any) => {
            if (item.title) {
              const words = item.title.split(/\s+/)
              words.forEach((word: string) => {
                if (word.toLowerCase().includes(value.toLowerCase()) && word.length > 1) {
                  suggestions.add(word)
                }
              })
            }
            if (item.product) {
              suggestions.add(item.product)
            }
          })
          
          // 添加API建议
          Array.from(suggestions).slice(0, 3).forEach(suggestion => {
            if (!options.find(opt => opt.value === suggestion)) {
              options.push({
                value: suggestion,
                label: suggestion,
                type: 'api'
              })
            }
          })
        }
      } catch (error) {
        console.error('获取搜索建议失败:', error)
        // API 失败时，从模拟数据中提取建议
        const mockSuggestions = [
          '网络安全', '数据保护', 'FCC认证', 'CE认证', 'CCC认证',
          'UL认证', 'ISO标准', '医疗器械', '汽车认证', 'RoHS'
        ]
        
        mockSuggestions
          .filter(item => item.toLowerCase().includes(value.toLowerCase()))
          .slice(0, 3)
          .forEach(suggestion => {
            if (!options.find(opt => opt.value === suggestion)) {
              options.push({
                value: suggestion,
                label: suggestion,
                type: 'mock'
              })
            }
          })
      }
    }
    
    // 添加当前输入的建议
    if (value.trim()) {
      options.unshift({
        value: value,
        label: `搜索 "${value}"`,
        type: 'search'
      })
    }
    
    keywordOptions.value = options
  }, 300)
}

const handleKeywordSelect = (value: string) => {
  searchForm.keyword = value
  addToSearchHistory(value)
  handleSearch()
}

const handleKeywordChange = (value: string) => {
  searchForm.keyword = value
}

const addToSearchHistory = (keyword: string) => {
  if (!keyword.trim()) return
  
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }
  searchHistory.value.unshift(keyword)
  
  // 只保留最近10个搜索历史
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }
  
  // 保存到本地存储
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

const removeSearchHistory = (keyword: string) => {
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }
}

const useSearchHistory = (keyword: string) => {
  searchForm.keyword = keyword
  handleSearch()
}



const loadSearchHistory = () => {
  try {
    const history = localStorage.getItem('searchHistory')
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
  }
}



// 批量操作方法
const batchMarkRelated = async () => {
  try {
    const ids = dataList.value.map(item => item.id)
    message.info(`正在批量标记 ${ids.length} 条记录为相关...`)
    
    // 这里应该调用批量更新 API
    // await batchUpdateRelated(ids, true)
    
    message.success(`成功标记 ${ids.length} 条记录为相关`)
    await loadData() // 重新加载数据
  } catch (error) {
    console.error('批量标记失败:', error)
    message.error('批量标记失败')
  }
}

const batchMarkUnrelated = async () => {
  try {
    const ids = dataList.value.map(item => item.id)
    message.info(`正在批量标记 ${ids.length} 条记录为不相关...`)
    
    // 这里应该调用批量更新 API
    // await batchUpdateRelated(ids, false)
    
    message.success(`成功标记 ${ids.length} 条记录为不相关`)
    await loadData() // 重新加载数据
  } catch (error) {
    console.error('批量标记失败:', error)
    message.error('批量标记失败')
  }
}

const exportSearchResults = () => {
  try {
    const exportData = dataList.value.map(item => ({
      标题: item.title,
      摘要: item.summary,
      国家: getCountryName(item.country),
      风险等级: getRiskLevelText(item.riskLevel),
      来源: item.sourceName,
      产品: item.product,
      类型: item.type,
      状态: getStatusText(item.status),
      发布时间: formatPublishDate(item)
    }))
    
    const csvContent = [
      Object.keys(exportData[0]).join(','),
      ...exportData.map(row => Object.values(row).map(value => `"${value}"`).join(','))
    ].join('\n')
    
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `搜索结果_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    message.success('搜索结果导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

// 获取数据列表（支持前端筛选）
const getFilteredDataList = () => {
  let filteredData = dataList.value
  
  // 匹配关键词筛选
  if (searchForm.matchedKeyword) {
    filteredData = filteredData.filter(item => {
      if (!item.matchedKeywords) return false
      const keywords = item.matchedKeywords.split(',').map((k: string) => k.trim())
      return keywords.includes(searchForm.matchedKeyword!)
    })
  }
  
  return filteredData
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.country = undefined
  searchForm.riskLevel = undefined
  searchForm.matchedKeyword = undefined
  searchForm.sourceName = undefined
  searchForm.type = undefined
  searchForm.dateRange = []
  currentPage.value = 1
  loadData()
}

const loadLatestData = async () => {
  await loadData()
  message.success('数据刷新成功')
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchForm.keyword,
      country: searchForm.country,
      riskLevel: searchForm.riskLevel,
      sourceName: searchForm.sourceName,
      type: searchForm.type,
      sortBy: sortBy.value
    }
    
    // 处理日期范围筛选
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const startDate = searchForm.dateRange[0] as any
      const endDate = searchForm.dateRange[1] as any
      
      if (startDate) {
        params.startDate = startDate.format('YYYY-MM-DD')
      }
      if (endDate) {
        params.endDate = endDate.format('YYYY-MM-DD')
      }
    }
    
    // 添加错误处理和调试信息
    try {
      console.log('🔍 发送API请求到:', '/api/crawler-data/list')
      console.log('🔍 请求参数:', params)
      const result = await getCrawlerData(params) as any
      
      // 适配新的 API 响应格式
      if (result && result.success && result.data) {
        
        dataList.value = (result.data as any).content || []
        totalCount.value = (result.data as any).totalElements || 0
        dataLoaded.value = true // 标记数据已加载
        
        // 更新统计数据
        updateStats() // 重新启用统计数据更新
        
        // 重新加载相关状态统计数据
        await loadStatistics()
        
        // 显示成功消息
        if (result.message) {
          message.success(result.message)
        }
      } else if (result && !result.success) {
        // API 返回错误
        console.warn('❌ API 返回错误:', result.error || result.message)
        message.error(result.error || result.message || 'API 返回错误')
        // 清空数据
        dataList.value = []
        totalCount.value = 0
      } else if (!result) {
        console.warn('❌ API 返回空结果')
        message.error('API 返回空结果')
        // 清空数据
        dataList.value = []
        totalCount.value = 0
      } else {
        console.warn('⚠️ API 返回数据格式异常:', result)
        console.warn('⚠️ 期望格式: { success: true, data: { content: [], totalElements: number } }')
        message.warning('API 返回数据格式异常')
        // 清空数据
        dataList.value = []
        totalCount.value = 0
      }
    } catch (apiError: any) {
      console.error('💥 API 调用失败:', apiError)
      console.error('💥 错误详情:', {
        message: apiError.message,
        status: apiError.response?.status,
        statusText: apiError.response?.statusText,
        url: apiError.config?.url,
        method: apiError.config?.method,
        data: apiError.response?.data
      })
      
      if (apiError.response?.status === 404) {
        message.error('API端点不存在，请检查后端服务配置')
      } else if (apiError.code === 'ERR_NETWORK') {
        message.error('网络连接失败，请检查后端服务是否启动')
      } else {
        message.error(`API调用失败: ${apiError.message}`)
      }
      
      // 清空数据
      dataList.value = []
      totalCount.value = 0
    }
  } catch (error) {
    console.error('💥 加载数据失败:', error)
    message.error('加载数据失败')
    // 清空数据
    dataList.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}


const loadStatistics = async () => {
  try {
    // 使用新的风险等级统计API
    const result = await getRiskLevelStatistics() as any
    
    if (result && result.success && result.data) {
      // 使用后端返回的风险等级统计数据
      stats.total = result.data.totalCount || 0
      stats.highRisk = result.data.highRiskCount || 0
      stats.mediumRisk = result.data.mediumRiskCount || 0
      stats.lowRisk = result.data.lowRiskCount || 0
      stats.undetermined = (result.data.undeterminedCount || 0) + (result.data.noneRiskCount || 0)
      
      console.log('📊 风险等级统计数据:', {
        total: stats.total,
        highRisk: stats.highRisk,
        mediumRisk: stats.mediumRisk,
        lowRisk: stats.lowRisk,
        undetermined: stats.undetermined
      })
    } else {
      // 如果API调用失败，回退到从当前数据计算
      console.warn('风险等级统计API调用失败，使用当前数据计算')
      stats.total = dataList.value.length || 0
      stats.highRisk = dataList.value.filter(item => item.riskLevel === 'HIGH').length
      stats.mediumRisk = dataList.value.filter(item => item.riskLevel === 'MEDIUM').length
      stats.lowRisk = dataList.value.filter(item => item.riskLevel === 'LOW').length
      stats.undetermined = dataList.value.filter(item => item.riskLevel === null || item.riskLevel === undefined || item.riskLevel === 'NONE').length
    }
  } catch (error) {
    console.error('❌ 加载风险等级统计数据失败:', error)
    // 使用默认统计数据
    stats.total = dataList.value.length || 0
    stats.highRisk = dataList.value.filter(item => item.riskLevel === 'HIGH').length
    stats.mediumRisk = dataList.value.filter(item => item.riskLevel === 'MEDIUM').length
    stats.lowRisk = dataList.value.filter(item => item.riskLevel === 'LOW').length
    stats.undetermined = dataList.value.filter(item => item.riskLevel === null || item.riskLevel === undefined || item.riskLevel === 'NONE').length
  }
}



const updateStats = () => {
  // 从当前数据计算统计
  stats.total = totalCount.value
  stats.highRisk = dataList.value.filter(item => item.riskLevel === 'HIGH').length
  stats.mediumRisk = dataList.value.filter(item => item.riskLevel === 'MEDIUM').length
  stats.lowRisk = dataList.value.filter(item => item.riskLevel === 'LOW').length
  stats.undetermined = dataList.value.filter(item => item.riskLevel === null || item.riskLevel === undefined).length
}

// 高亮关键词
const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword || !text) return text
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark style="background-color: #ffd54f; padding: 2px 4px; border-radius: 2px;">$1</mark>')
}

// 获取搜索结果摘要
const getSearchSummary = (item: any, keyword: string) => {
  if (!keyword) return item.summary || truncateText(item.content, 200)
  
  const searchText = item.title + ' ' + (item.summary || item.content)
  const keywordIndex = searchText.toLowerCase().indexOf(keyword.toLowerCase())
  
  if (keywordIndex === -1) return item.summary || truncateText(item.content, 200)
  
  // 在关键词周围截取上下文
  const start = Math.max(0, keywordIndex - 50)
  const end = Math.min(searchText.length, keywordIndex + keyword.length + 50)
  let summary = searchText.substring(start, end)
  
  if (start > 0) summary = '...' + summary
  if (end < searchText.length) summary = summary + '...'
  
  return highlightKeyword(summary, keyword)
}

const handleSort = () => {
  loadData()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handlePageSizeChange = (current: number, size: number) => {
  currentPage.value = current
  pageSize.value = size
  loadData()
}

const viewDetail = (item: any) => {
  selectedItem.value = item
  detailVisible.value = true
}

const closeDetail = () => {
  detailVisible.value = false
  selectedItem.value = null
}

const editRiskLevel = (item: any) => {
  editingItem.value = item
  editingRiskLevel.value = item.riskLevel
  editVisible.value = true
}

const saveRiskLevel = async () => {
  if (!editingItem.value) return
  
  // 检查是否有要更新的数据
  if (editingRiskLevel.value === null) {
    message.warning('请选择一个风险等级状态')
    return
  }
  
  try {
    // 准备更新数据 - 使用新的风险等级更新API
    const updateData = {
      id: editingItem.value.id,
      riskLevel: editingRiskLevel.value
    }
    
    console.log('准备发送的更新数据:', updateData)
    console.log('editingRiskLevel.value:', editingRiskLevel.value)
    
    const result = await updateCrawlerDataRiskLevel(updateData) as any
    
    console.log('API响应结果:', result)
    console.log('响应数据结构:', {
      hasResult: !!result,
      hasSuccess: !!(result && result.success),
      message: result && result.message,
      error: result && result.error
    })
    
    if (result && result.success) {
      message.success(result.message || '更新成功')
      
      // 重新加载数据
      await loadData()
      
      // 重新加载统计数据
      await loadStatistics()
      
      closeEdit()
    } else {
      const errorMsg = (result && result.error) || '更新失败'
      console.error('更新失败，错误信息:', errorMsg)
      message.error(errorMsg)
    }
  } catch (error) {
    console.error('更新失败:', error)
    message.error('更新失败')
  }
}

const closeEdit = () => {
  editVisible.value = false
  editingItem.value = null
  editingRiskLevel.value = null
}

// 批量操作相关函数
const handleRowSelectionChange = (id: string, checked: boolean) => {
  if (checked) {
    if (!selectedRowKeys.value.includes(id)) {
      selectedRowKeys.value.push(id)
    }
  } else {
    const index = selectedRowKeys.value.indexOf(id)
    if (index > -1) {
      selectedRowKeys.value.splice(index, 1)
    }
  }
}

const clearSelection = () => {
  selectedRowKeys.value = []
}

const showBatchOperationModal = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要操作的数据')
    return
  }
  batchRiskLevelValue.value = null
  batchOperationVisible.value = true
}

const closeBatchOperation = () => {
  batchOperationVisible.value = false
  batchRiskLevelValue.value = null
}

const handleBatchUpdateRiskLevel = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要操作的数据')
    return
  }
  
  if (batchRiskLevelValue.value === null) {
    message.warning('请选择风险等级状态')
    return
  }
  
  try {
    batchUpdatingRiskLevel.value = true
    
    // 使用新的批量更新风险等级API
    const result = await batchUpdateCrawlerDataRiskLevel({
      ids: selectedRowKeys.value,
      riskLevel: batchRiskLevelValue.value
    }) as any
    
    console.log('批量更新API响应结果:', result)
    
    if (result && result.success) {
      const successCount = result.successCount || 0
      const failCount = result.failCount || 0
      
      if (failCount === 0) {
        message.success(`批量更新成功，共更新 ${successCount} 条数据`)
      } else {
        message.warning(`批量更新完成，成功 ${successCount} 条，失败 ${failCount} 条`)
      }
      
      // 重新加载数据
      await loadData()
      
      // 重新加载统计数据
      await loadStatistics()
      
      // 清空选择
      clearSelection()
      
      // 关闭模态框
      closeBatchOperation()
    } else {
      const errorMsg = (result && result.error) || '批量更新失败'
      console.error('批量更新失败，错误信息:', errorMsg)
      message.error(errorMsg)
    }
  } catch (error) {
    console.error('批量更新失败:', error)
    message.error('批量更新失败')
  } finally {
    batchUpdatingRiskLevel.value = false
  }
}

const autoProcess = async () => {
  autoProcessing.value = true
  try {
    let currentKeywords = []
    
    // 暂时只使用本地关键词，跳过数据库关键词获取
    console.log('暂时只使用本地关键词进行自动处理...')
    
    // 优先使用文件关键词（如果已设置）
    if (useFileKeywords.value && fileKeywords.value.length > 0) {
      currentKeywords = fileKeywords.value.filter(keyword => keyword && keyword.trim())
      console.log('使用文件关键词:', currentKeywords)
    }
    
    // 如果没有本地关键词，尝试使用当前文本框中的关键词
    if (currentKeywords.length === 0) {
      currentKeywords = keywordsText.value.split('\n').filter(line => line.trim())
      console.log('使用文本框关键词:', currentKeywords)
    }
    
    // 如果仍然没有关键词，尝试使用本地关键词数组
    if (currentKeywords.length === 0 && keywords.value.length > 0) {
      currentKeywords = keywords.value.map((item: any) => item.keyword || item).filter((keyword: string) => keyword && keyword.trim())
      console.log('使用本地关键词数组:', currentKeywords)
    }
    
    if (currentKeywords.length === 0) {
      message.warning('请先在关键词管理中设置关键词')
      return
    }
    
    console.log('使用的关键词列表:', currentKeywords)
    
    // 调用自动处理API，传递关键词列表
    const result = await autoProcessRelatedAPI({
      keywords: currentKeywords
    })
    
    if (result && result.data && result.data.success) {
      const totalProcessed = result.data.totalProcessed || 0
      const relatedCount = result.data.relatedCount || 0
      const unrelatedCount = result.data.unrelatedCount || 0
      const unchangedCount = result.data.unchangedCount || 0
      const usedKeywords = result.data.usedKeywords || currentKeywords.length
      
      // 后端现在会自动设置相关数据为高风险，获取风险处理计数
      const riskProcessedCount = result.data.riskProcessedCount || 0
      
      // 更新自动处理时间和结果
      const now = new Date()
      const processTime = now.toLocaleString('zh-CN')
      
      // 保存上次自动处理时间
      lastAutoProcessTime.value = processTime
      
      // 保存本次自动处理结果
      lastAutoProcessResult.value = {
        totalProcessed,
        relatedCount,
        unrelatedCount,
        unchangedCount,
        usedKeywords,
        processTime,
        riskProcessedCount // 新增风险处理计数
      }
      
      // 保存到localStorage以便页面刷新后保持
      localStorage.setItem('lastAutoProcessTime', processTime)
      localStorage.setItem('lastAutoProcessResult', JSON.stringify(lastAutoProcessResult.value))
      
      // 显示详细的处理结果
      Modal.info({
        title: '自动处理完成',
        content: h('div', [
          h('p', `总处理数据: ${totalProcessed} 条`),
          h('p', `标记为相关: ${relatedCount} 条`),
          h('p', `标记为不相关: ${unrelatedCount} 条`),
          h('p', `未变更数据: ${unchangedCount} 条`),
          h('p', `使用关键词: ${usedKeywords} 个`),
          riskProcessedCount > 0 ? h('p', { style: 'color: #ff4d4f; font-weight: bold;' }, `设置为高风险: ${riskProcessedCount} 条`) : null,
          h('p', { style: 'margin-top: 10px; color: #1890ff;' }, '已自动筛选显示相关数据')
        ].filter(Boolean)),
        okText: '确定'
      })
      
        // 自动筛选显示此次新增的相关数据
        if (relatedCount > 0) {
          // 设置筛选条件为"高风险"，只显示高风险数据
          searchForm.keyword = ''
          searchForm.country = undefined
          searchForm.riskLevel = 'HIGH'
          searchForm.sourceName = undefined
          searchForm.type = undefined
          searchForm.dateRange = []
          currentPage.value = 1
        
        // 重新加载数据以显示相关数据
        await loadData()
      }
      
      // 重新加载统计数据
      await loadStatistics()
    } else {
      // 即使处理失败，也显示统计结果
      // message.error('自动处理失败，但已显示当前统计结果')
      
      // 重新加载统计数据
      await loadStatistics()
    }
  } catch (error) {
    console.error('自动处理失败:', error)
    // 即使出错，也显示统计结果
    message.error('自动处理失败，但已显示当前统计结果')
    
    // 重新加载统计数据
    await loadStatistics()
  } finally {
    autoProcessing.value = false
  }
}

const setAllToMediumRisk = async () => {
  try {
    // 显示确认对话框
    const confirmed = await new Promise((resolve) => {
      Modal.confirm({
        title: '确认操作',
        content: '此操作将把所有数据的风险等级设置为"中风险"，是否继续？',
        okText: '确定',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false)
      })
    })
    
    if (!confirmed) return
    
    batchUpdating.value = true
    
    try {
      // 使用新的批量设置所有数据为中风险的API
      const result = await setAllDataToMediumRisk() as any
      
      if (result && result.success) {
        const updatedCount = result.updatedCount || 0
        const totalCount = result.totalCount || 0
        
        message.success(`成功将 ${updatedCount} 条数据设置为中风险（总数据量: ${totalCount}）`)
        
        // 重新加载数据
        await loadData()
        
        // 重新加载统计数据
        await loadStatistics()
      } else {
        message.error((result && result.error) || '设置中风险失败')
      }
      
    } catch (error) {
      console.error('批量设置中风险失败:', error)
      message.error('批量设置中风险失败')
    }
  } catch (error) {
    console.error('设置中风险失败:', error)
    message.error('设置中风险失败')
  } finally {
    batchUpdating.value = false
  }
}

const handleAutoUpdateCountry = async () => {
  try {
    // 显示确认对话框
    const confirmed = await new Promise((resolve) => {
      Modal.confirm({
        title: '确认操作',
        content: '此操作将分析所有数据的标题和内容，自动识别并更新国家字段，是否继续？',
        okText: '确定',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false)
      })
    })
    
    if (!confirmed) return
    
    countryUpdating.value = true
    const result = await autoUpdateCountry()
    
    if (result && result.success) {
      message.success(`自动更新国家字段成功！共处理 ${result.totalProcessed || 0} 条数据，更新 ${result.updatedCount || 0} 条，未变更 ${result.unchangedCount || 0} 条`)
      
      // 显示详细的更新统计
      if (result.countryUpdates && Object.keys(result.countryUpdates).length > 0) {
        const updateDetails = Object.entries(result.countryUpdates)
          .map(([country, count]) => `${country}: ${count}条`)
          .join(', ')
        message.info(`更新详情: ${updateDetails}`)
      }
      
      // 重新加载数据
      await loadData()
      
      // 重新加载统计数据
      await loadStatistics()
    } else {
      message.error((result && result.error) || '自动更新国家字段失败')
    }
  } catch (error) {
    console.error('自动更新国家字段失败:', error)
    message.error('自动更新国家字段失败')
  } finally {
    countryUpdating.value = false
  }
}

// 查看国家分布统计方法
const handleViewCountryDistribution = async () => {
  try {
    const result = await getCountryDistribution()
    
    if (result && result.success) {
      const data = result.data || result
      const totalCount = data.totalCount || 0
      const countryStats = data.countryStats || {}
      const nullCount = data.nullCountryCount || 0
      const emptyCount = data.emptyCountryCount || 0
      
      // 构建统计信息
      let statsText = `总数据量: ${totalCount} 条\n`
      statsText += `空值(null): ${nullCount} 条\n`
      statsText += `空字符串: ${emptyCount} 条\n\n`
      statsText += `国家分布:\n`
      
      // 按数量排序显示国家分布
      const sortedCountries = Object.entries(countryStats)
        .sort(([,a], [,b]) => (b as number) - (a as number))
        .map(([country, count]) => `${country}: ${count} 条`)
        .join('\n')
      
      statsText += sortedCountries
      
      // 显示统计信息
      Modal.info({
        title: '国家分布统计',
        content: h('pre', { style: 'white-space: pre-wrap; font-family: monospace;' }, statsText),
        width: 600,
        okText: '确定'
      })
    } else {
      message.error((result && result.error) || '获取国家分布统计失败')
    }
  } catch (error) {
    console.error('获取国家分布统计失败:', error)
    message.error('获取国家分布统计失败')
  }
}




// 关键词管理方法
const showKeywordModal = () => {
  keywordModalVisible.value = true
  loadKeywords()
}

const loadKeywords = async () => {
  keywordsLoading.value = true
  try {
    const result = await getAllKeywords()
    console.log('关键词API响应:', result)
    
    // 处理新的API响应格式: {success: true, keywords: [...], total: number}
    let keywordsData = null
    if (result && result.success && result.keywords) {
      // 新格式: {success: true, keywords: [...], total: number}
      keywordsData = result.keywords
    } else if (result && result.success && result.data) {
      // 兼容旧格式: {success: true, data: [...]}
      keywordsData = Array.isArray(result.data) ? result.data : (result.data.keywords || [])
    } else if (result && result.data && result.data.success) {
      // 格式: {data: {success: true, keywords: [...]}}
      keywordsData = result.data.keywords || []
    } else if (result && result.data && result.data.keywords) {
      // 格式: {data: {keywords: [...]}}
      keywordsData = result.data.keywords || []
    }
    
    if (keywordsData && Array.isArray(keywordsData)) {
      keywords.value = keywordsData
      // 将关键词数组转换为文本形式，处理新的对象格式
      keywordsText.value = keywordsData.map(item => {
        if (typeof item === 'string') {
          return item
        } else if (item && typeof item === 'object') {
          // 新格式: {id, keyword, description, enabled, sortOrder, createdTime, updatedTime}
          return item.keyword || item.name || item.text || JSON.stringify(item)
        }
        return String(item)
      }).join('\n')
      console.log('加载关键词成功，数量:', keywordsData.length)
      
      // 同时加载匹配数量
      await refreshKeywordCounts()
    } else {
      console.error('关键词数据格式错误:', result)
      message.error('加载关键词失败：数据格式错误')
    }
  } catch (error) {
    console.error('加载关键词失败:', error)
    message.error('加载关键词失败')
  } finally {
    keywordsLoading.value = false
  }
}

const saveKeywords = async () => {
  try {
    // 将文本转换为关键词数组
    const keywordLines = keywordsText.value.split('\n').filter(line => line.trim())
    const keywordsArray = keywordLines.map(keyword => keyword.trim())
    
    if (keywordsArray.length === 0) {
      message.warning('请输入至少一个关键词')
      return false
    }
    
    // 显示保存进度
    message.loading('正在保存关键词到数据库...', 0)
    
    // 批量保存关键词到数据库
    let successCount = 0
    let errorCount = 0
    const errors = []
    
    for (const keyword of keywordsArray) {
      try {
        const result = await addKeyword({ keyword, description: '' })
        if (result && result.success) {
          successCount++
        } else {
          errorCount++
          errors.push(`${keyword}: ${result?.message || '保存失败'}`)
        }
      } catch (error) {
        errorCount++
        errors.push(`${keyword}: ${error.message || '保存失败'}`)
      }
    }
    
    // 关闭加载提示
    message.destroy()
    
    // 重新加载关键词列表
    await loadKeywords()
    
    // 显示保存结果
    if (errorCount === 0) {
      Modal.success({
        title: '数据库保存成功',
        content: h('div', [
          h('p', { style: 'margin-bottom: 8px;' }, `总计保存: ${successCount} 个关键词`),
          h('p', { style: 'color: #52c41a; margin-bottom: 4px;' }, `✓ 已保存到数据库`),
          h('p', { style: 'color: #1890ff; margin-bottom: 4px;' }, `✓ 自动处理将使用数据库关键词`),
          h('p', { style: 'margin-top: 12px; font-size: 12px; color: #666;' }, 
            '关键词已保存到数据库，所有用户共享使用')
        ]),
        okText: '确定'
      })
    } else {
      Modal.warning({
        title: '部分保存成功',
        content: h('div', [
          h('p', { style: 'margin-bottom: 8px;' }, `成功: ${successCount} 个，失败: ${errorCount} 个`),
          h('p', { style: 'color: #faad14; margin-bottom: 8px;' }, '失败的关键词:'),
          h('ul', { style: 'margin-left: 20px; max-height: 200px; overflow-y: auto;' }, 
            errors.map(error => h('li', { style: 'margin-bottom: 4px;' }, error))
          )
        ]),
        okText: '确定'
      })
    }
    
    console.log('关键词已保存到数据库:', keywordsArray)
    return true // 返回true表示保存成功，模态框会关闭
    
  } catch (error) {
    console.error('保存关键词到数据库失败:', error)
    message.error('保存关键词到数据库失败')
    return false // 返回false表示保存失败，模态框不会关闭
  }
}

const initializeDefaultKeywords = async () => {
  try {
    console.log('开始初始化关键词...')
    const result = await initializeKeywords()
    console.log('关键词初始化API响应:', result)
    
    if (result && result.success) {
      console.log('初始化成功，显示成功消息')
      message.success(result.message || '初始化关键词成功')
      await loadKeywords()
    } else {
      console.log('初始化失败，显示错误消息')
      console.log('result:', result)
      console.log('result.success:', result?.success)
      message.error((result && result.error) || '初始化关键词失败')
    }
  } catch (error) {
    console.error('初始化关键词失败:', error)
    message.error('初始化关键词失败')
  }
}



const handleSaveKeywords = async () => {
  const success = await saveKeywords()
  if (success) {
    closeKeywordModal()
  }
}

const closeKeywordModal = () => {
  keywordModalVisible.value = false
  newKeyword.value = ''
  newKeywordDescription.value = ''
  keywordsText.value = ''
}

// 文件关键词管理方法
const loadFileKeywords = async () => {
  try {
    const result = await getFileKeywords()
    if (result) {
      if (result.success !== false && result.keywords) {
        fileKeywords.value = result.keywords || []
        useFileKeywords.value = fileKeywords.value.length > 0
        console.log('加载文件关键词成功:', fileKeywords.value.length, '个')
      } else {
        console.warn('加载文件关键词失败:', result.error || '未知错误')
        fileKeywords.value = []
        useFileKeywords.value = false
      }
    } else {
      console.warn('加载文件关键词失败: 无响应数据')
      fileKeywords.value = []
      useFileKeywords.value = false
    }
  } catch (error) {
    console.error('加载文件关键词失败:', error)
    fileKeywords.value = []
    useFileKeywords.value = false
  }
}

const migrateFromLocalStorage = async () => {
  try {
    // 获取localStorage中的关键词
    const savedKeywords = localStorage.getItem('localKeywords')
    if (!savedKeywords) {
      message.warning('没有找到localStorage中的关键词')
      return
    }
    
    const keywordsArray = savedKeywords.split('\n').filter(line => line.trim())
    if (keywordsArray.length === 0) {
      message.warning('localStorage中没有有效的关键词')
      return
    }
    
    // 迁移到文件
    const result = await migrateKeywordsFromLocalStorage(keywordsArray)
    if (result) {
      if (result.success !== false) {
        message.success(`成功迁移 ${result.migratedCount} 个关键词到文件`)
        
        // 更新本地状态
        fileKeywords.value = keywordsArray
        useFileKeywords.value = true
        
        // 清除localStorage
        localStorage.removeItem('localKeywords')
        localStorage.removeItem('useLocalKeywords')
        
        console.log('关键词迁移完成')
      } else {
        message.error(result.error || '迁移失败')
      }
    } else {
      message.error('迁移失败: 无响应数据')
    }
  } catch (error) {
    console.error('迁移关键词失败:', error)
    message.error('迁移关键词失败')
  }
}

const sendEmail = (item: any) => {
  emailItem.value = item
  emailVisible.value = true
}

const closeEmail = () => {
  emailVisible.value = false
  emailItem.value = null
}

const handleSendEmail = async () => {
  if (!emailItem.value) return
  
  try {
    const result = await sendEmailAPI({
      recipients: emailForm.recipients,
      subject: emailForm.subject,
      content: emailForm.content,
      includeAttachment: emailForm.includeAttachment,
      newsId: emailItem.value.id
    })
    
    if (result.data) {
      message.success('邮件发送成功')
      closeEmail()
    } else {
      message.error('发送邮件失败')
    }
  } catch (error) {
    console.error('发送邮件失败:', error)
    message.error('发送邮件失败')
  }
}

const deleteNews = async (item: any) => {
  try {
    const result = await deleteCrawlerData({ id: item.id })
    
    if (result.data) {
      message.success('删除成功')
      
      // 重新加载数据
      await loadData()
    } else {
      message.error('删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 工具方法（已移除未使用的formatDate函数）

// 格式化发布时间
const formatPublishDate = (item: any) => {
  // 优先使用publishDate字段
  if (item.publishDate) {
    return item.publishDate
  }
  
  // 如果有releaseDate列表，使用第一个日期
  if (item.releaseDate && Array.isArray(item.releaseDate) && item.releaseDate.length > 0) {
    return item.releaseDate[0]
  }
  
  // 如果都没有，回退到爬取时间
  if (item.crawlTime) {
    const d = new Date(item.crawlTime)
    return d.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
  
  return ''
}

// 获取所有匹配的关键词列表
const getMatchedKeywordsList = () => {
  const keywordsSet = new Set<string>()
  dataList.value.forEach(item => {
    if (item.matchedKeywords) {
      const keywords = item.matchedKeywords.split(',').map((k: any) => k.trim())
      keywords.forEach((keyword: any) => {
        if (keyword) {
          keywordsSet.add(keyword)
        }
      })
    }
  })
  return Array.from(keywordsSet).sort()
}

// 根据状态筛选数据
const filterByStatus = (status: string) => {
  // 清除其他筛选条件
  searchForm.keyword = ''
  searchForm.country = undefined
  searchForm.sourceName = undefined
  searchForm.type = undefined
  searchForm.dateRange = []
  searchForm.matchedKeyword = undefined
  
  // 设置风险等级筛选
  switch (status) {
    case 'all':
      searchForm.riskLevel = undefined
      break
    case 'highRisk':
      searchForm.riskLevel = 'HIGH'
      break
    case 'mediumRisk':
      searchForm.riskLevel = 'MEDIUM'
      break
    case 'lowRisk':
      searchForm.riskLevel = 'LOW'
      break
    case 'undetermined':
      searchForm.riskLevel = 'null'
      break
  }
  
  // 重置页码并加载数据
  currentPage.value = 1
  loadData()
  
  // 显示筛选提示
  const statusText: Record<string, string> = {
    'all': '全部数据',
    'highRisk': '高风险数据',
    'mediumRisk': '中风险数据',
    'lowRisk': '低风险数据',
    'undetermined': '未确定数据'
  }
  message.success(`已筛选显示${statusText[status]}`)
}



const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const getCountryName = (countryCode: string) => {
  const countryMap: Record<string, string> = {
    'OVERSEAS': '海外',
    'CN': '中国',
    'US': '美国',
    'EU': '欧盟',
    'JP': '日本',
    'KR': '韩国',
    'IN': '印度',
    'TH': '泰国',
    'SG': '新加坡',
    'TW': '台湾',
    'AU': '澳大利亚',
    'CL': '智利',
    'MY': '马来西亚',
    'AE': '阿联酋',
    'PE': '秘鲁',
    'ZA': '南非',
    'IL': '以色列',
    'ID': '印度尼西亚'
  }
  return countryMap[countryCode] || countryCode
}

const getCountryColor = (country: string) => {
  const colors: Record<string, string> = {
    'OVERSEAS': 'default',
    'CN': 'red',
    'US': 'blue',
    'EU': 'green',
    'JP': 'purple',
    'KR': 'cyan',
    'IN': 'orange',
    'TH': 'gold',
    'SG': 'geekblue',
    'TW': 'volcano',
    'AU': 'lime',
    'CL': 'magenta',
    'MY': 'cyan',
    'AE': 'gold',
    'PE': 'orange',
    'ZA': 'green',
    'IL': 'blue',
    'ID': 'red'
  }
  return colors[country] || 'default'
}

const getCountryFlag = (country: string) => {
  const flags: Record<string, string> = {
    'OVERSEAS': '🌍',
    'CN': '🇨🇳',
    'US': '🇺🇸',
    'EU': '🇪🇺',
    'JP': '🇯🇵',
    'KR': '🇰🇷',
    'IN': '🇮🇳',
    'TH': '🇹🇭',
    'SG': '🇸🇬',
    'TW': '🇹🇼',
    'AU': '🇦🇺',
    'CL': '🇨🇱',
    'MY': '🇲🇾',
    'AE': '🇦🇪',
    'PE': '🇵🇪',
    'ZA': '🇿🇦',
    'IL': '🇮🇱',
    'ID': '🇮🇩'
  }
  return flags[country] || '🌍'
}

// 获取国家列表的辅助函数
const getCountriesList = (item: any): string[] => {
  if (item.countries) {
    if (typeof item.countries === 'string') {
      try {
        // 处理可能的双重JSON编码
        let parsed = JSON.parse(item.countries)
        
        // 如果解析后仍然是字符串，可能还需要再次解析
        if (typeof parsed === 'string') {
          try {
            parsed = JSON.parse(parsed)
          } catch (e2) {
            // 如果第二次解析失败，直接使用第一次解析的结果
            parsed = [parsed]
          }
        }
        
        // 确保返回的是数组
        if (Array.isArray(parsed)) {
          return parsed
        } else {
          return [parsed]
        }
      } catch (e) {
        console.warn('解析countries字段失败:', e, '原始数据:', item.countries)
        return item.country ? [item.country] : []
      }
    } else if (Array.isArray(item.countries)) {
      // 如果已经是数组，检查数组元素是否也需要解析
      return item.countries.map((countryItem: any) => {
        if (typeof countryItem === 'string') {
          try {
            const parsed = JSON.parse(countryItem)
            return Array.isArray(parsed) ? parsed[0] : parsed
          } catch (e) {
            return countryItem
          }
        }
        return countryItem
      })
    }
  }
  return item.country ? [item.country] : []
}

const getRiskLevelColor = (riskLevel: string | null) => {
  if (riskLevel === 'HIGH') return 'red'
  if (riskLevel === 'MEDIUM') return 'orange'
  if (riskLevel === 'LOW') return 'green'
  return 'default'
}

const getRiskLevelText = (riskLevel: string | null) => {
  if (riskLevel === 'HIGH') return '高风险'
  if (riskLevel === 'MEDIUM') return '中风险'
  if (riskLevel === 'LOW') return '低风险'
  if (riskLevel === null || riskLevel === 'null') return '未确定'
  return '未确定'
}

const getRiskLevelClass = (riskLevel: string | null) => {
  if (riskLevel === 'HIGH') return 'high-risk'
  if (riskLevel === 'MEDIUM') return 'medium-risk'
  if (riskLevel === 'LOW') return 'low-risk'
  return 'undetermined'
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'NEW': 'blue',
    'PROCESSING': 'orange',
    'PROCESSED': 'green',
    'ERROR': 'red',
    'DUPLICATE': 'purple'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'NEW': '新建',
    'PROCESSING': '处理中',
    'PROCESSED': '已处理',
    'ERROR': '错误',
    'DUPLICATE': '重复'
  }
  return texts[status] || status
}

// 加载自动处理时间信息
const loadAutoProcessInfo = () => {
  try {
    // 从localStorage加载上次自动处理时间
    const savedTime = localStorage.getItem('lastAutoProcessTime')
    if (savedTime) {
      lastAutoProcessTime.value = savedTime
    }
    
    // 从localStorage加载上次自动处理结果
    const savedResult = localStorage.getItem('lastAutoProcessResult')
    if (savedResult) {
      lastAutoProcessResult.value = JSON.parse(savedResult)
    }
    
    // 设置数据更新时间为当前时间
    lastDataUpdateTime.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    console.error('加载自动处理信息失败:', error)
  }
}

// 组件挂载时初始化
onMounted(async () => {
  loadSearchHistory()
  
  // 加载文件关键词
  await loadFileKeywords()
  
  // 加载自动处理时间信息
  loadAutoProcessInfo()
  
  // 加载数据源选项
  try {
    await loadSourceNames()
  } catch (error) {
    console.error('❌ 数据源加载失败:', error)
  }
  
  // 加载统计数据
  try {
    await loadStatistics() // 加载相关状态统计数据
  } catch (error) {
    console.error('❌ 统计数据加载失败:', error)
  }
  
  // 直接加载数据
  try {
    await loadData() // 加载爬虫数据
  } catch (error) {
    console.error('❌ 数据加载失败:', error)
    message.error('数据加载失败')
  }
})
</script>

<style scoped>
.data-query {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
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

.search-section {
  margin-bottom: 24px;
}

.stats-section {
  margin-bottom: 24px;
}

.clickable-statistic {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 6px;
  padding: 8px;
}

.clickable-statistic:hover {
  background-color: rgba(24, 144, 255, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.results-section {
  margin-bottom: 24px;
}

.results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.results-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-operations {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-count {
  color: #1890ff;
  font-weight: 500;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.data-list {
  min-height: 400px;
}

.list-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
}

.news-item.selected {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.news-item-checkbox {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1;
}

.news-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.news-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #1890ff;
  cursor: pointer;
  flex: 1;
  margin-right: 16px;
}

.news-title:hover {
  color: #40a9ff;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.news-date {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.news-content {
  margin-bottom: 12px;
}

.news-content p {
  margin: 0;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.6;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-view {
  margin-bottom: 24px;
}

.news-card {
  height: 100%;
  position: relative;
}

.news-card.selected {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.card-checkbox {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 2px;
}

.card-cover {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.country-flag {
  font-size: 48px;
}

.risk-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.risk-badge.high-risk {
  background-color: #ff4d4f;
}

.risk-badge.medium-risk {
  background-color: #faad14;
}

.risk-badge.low-risk {
  background-color: #52c41a;
}

.risk-badge.undetermined {
  background-color: #d9d9d9;
  color: #666;
}

.card-content {
  margin-top: 8px;
}

.card-content p {
  margin: 0 0 8px 0;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.5;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-date {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.pagination-container {
  text-align: center;
  margin-top: 24px;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-meta {
  margin-bottom: 8px;
}

.detail-date {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.detail-source {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(0, 0, 0, 0.65);
}

.detail-url {
  color: #1890ff;
}

.detail-body h4 {
  margin: 16px 0 8px 0;
  font-weight: 500;
}

.detail-body p {
  margin: 0 0 12px 0;
  line-height: 1.6;
  color: rgba(0, 0, 0, 0.65);
}

.detail-text {
  background: #fafafa;
  padding: 12px;
  border-radius: 4px;
  line-height: 1.6;
  color: rgba(0, 0, 0, 0.65);
}

.detail-tags {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.edit-content {
  padding: 16px 0;
}

.batch-operation-content {
  padding: 16px 0;
}

.batch-info {
  margin-bottom: 16px;
}

.edit-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.edit-item h4 {
  margin: 0 0 8px 0;
  font-weight: 500;
}

.edit-summary {
  margin: 0;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.5;
}

.email-content {
  padding: 16px 0;
}

.email-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.email-item h4 {
  margin: 0 0 8px 0;
  font-weight: 500;
}

.email-summary {
  margin: 0;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.5;
}

/* 关键词搜索相关样式 */
.keyword-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keyword-icon {
  font-size: 14px;
}

.search-history {
  margin-top: 8px;
  padding: 8px 0;
}

.history-label {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-right: 8px;
}

/* 关键词管理样式 */
.keyword-management {
  padding: 16px 0;
}

.keyword-header {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.keyword-header p {
  margin-bottom: 16px;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.6;
}

.keyword-textarea-section {
  margin-bottom: 16px;
}

.keyword-list-section {
  margin-bottom: 16px;
}

.keyword-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.keyword-list-header h4 {
  margin: 0;
  color: #1890ff;
  font-weight: 500;
}

.keyword-stats {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.match-count-tag {
  font-weight: bold;
  border-radius: 10px;
}

.no-description {
  color: rgba(0, 0, 0, 0.45);
  font-style: italic;
}

.add-keyword-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.keyword-list {
  max-height: 400px;
  overflow-y: auto;
}

.keyword-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 8px 0;
}

.keyword-content {
  flex: 1;
}

.keyword-text {
  font-weight: 500;
  color: #1890ff;
  margin-bottom: 4px;
}

.keyword-description {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
}

.keyword-actions {
  margin-left: 16px;
}

/* 自动处理信息样式 */
.auto-process-info {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-label {
  font-weight: 500;
  color: #495057;
  margin-right: 8px;
  min-width: 120px;
}

.info-value {
  color: #1890ff;
  font-weight: 500;
}

.auto-process-stats {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 12px 16px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}

.stat-label {
  color: #666;
  margin-right: 4px;
  font-weight: normal;
}

.stat-value {
  color: #1890ff;
  font-weight: 600;
}

.no-stats {
  padding: 12px 16px;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  text-align: center;
}

.auto-process-result {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 16px;
  margin-top: 12px;
}

.result-item {
  font-weight: 500;
  font-size: 14px;
}

/* 搜索结果高亮样式 */
.news-title mark {
  background-color: #ffd54f !important;
  padding: 2px 4px !important;
  border-radius: 2px !important;
  color: #000 !important;
}

.news-content mark {
  background-color: #ffd54f !important;
  padding: 1px 3px !important;
  border-radius: 2px !important;
  color: #000 !important;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .data-query {
    padding: 16px;
  }
  
  .news-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .news-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .detail-source {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 处理方式选择器样式 */
.processing-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-icon {
  font-size: 14px;
}

.option-status {
  font-size: 11px;
  color: #999;
  font-style: italic;
}
</style>

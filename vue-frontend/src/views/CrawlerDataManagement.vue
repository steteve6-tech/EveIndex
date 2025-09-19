<template>
    <div class="crawler-data-management-page">
    <div class="page-header">
      <h1>{{ pageTitle }}</h1>
    </div>

    <!-- 高风险数据管理内容 -->
    <div class="high-risk-data-content">
        <div class="tab-content">
          <div class="tab-header">
            <a-space>
              <a-button @click="refreshData" :loading="loading">
                <template #icon>
                  <ReloadOutlined />
                </template>
                刷新
              </a-button>
              <a-button type="primary" @click="showAddModal = true">
                <template #icon>
                  <PlusOutlined />
                </template>
                添加数据
              </a-button>
            </a-space>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-section">
            <a-row :gutter="16">
              <a-col :span="8">
                <a-card>
                  <Statistic
                    title="高风险数据总数"
                    :value="statistics.total"
                    :value-style="{ color: '#cf1322' }"
                    :loading="loading"
                  />
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card>
                  <Statistic
                    title="今日新增高风险"
                    :value="statistics.todayCount"
                    :value-style="{ color: '#fa8c16' }"
                    :loading="loading"
                  />
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card>
                  <Statistic
                    title="涉及国家数量"
                    :value="statistics.countryCount"
                    :value-style="{ color: '#52c41a' }"
                    :loading="loading"
                  />
                </a-card>
              </a-col>
            </a-row>
          </div>

          <!-- 搜索和筛选 -->
          <a-card style="margin-bottom: 16px;">
            <a-row :gutter="16">
              <a-col :span="5">
                <a-input
                  v-model:value="searchForm.keyword"
                  placeholder="搜索标题或内容"
                  allow-clear
                  @change="handleSearch"
                >
                  <template #prefix>
                    <SearchOutlined />
                  </template>
                </a-input>
              </a-col>
              <a-col :span="4">
                <a-select
                  v-model:value="searchForm.matchedKeyword"
                  placeholder="匹配关键词"
                  allow-clear
                  show-search
                  :filter-option="filterOption"
                  @change="handleSearch"
                  style="width: 150px;"
                >
                  <a-select-option 
                    v-for="keyword in keywordOptions" 
                    :key="keyword.value" 
                    :value="keyword.value"
                  >
                    {{ keyword.label }} ({{ keyword.count }})
                  </a-select-option>
                </a-select>
              </a-col>
<!--              <a-col :span="4">-->
<!--                <a-tag color="red" style="height: 32px; line-height: 30px; padding: 0 12px;">-->
<!--                  仅显示高风险数据-->
<!--                </a-tag>-->
<!--              </a-col>-->
<!--              <a-col :span="2">-->
<!--                <a-button type="link" @click="showAllRiskLevels = !showAllRiskLevels">-->
<!--                  {{ showAllRiskLevels ? '隐藏其他' : '显示全部' }}-->
<!--                </a-button>-->
<!--              </a-col>-->
              <a-col :span="3">
                <a-select
                  v-model:value="searchForm.country"
                  placeholder="国家/地区"
                  allow-clear
                  @change="handleSearch"
                >
                  <a-select-option 
                    v-for="option in countryOptions" 
                    :key="option.value" 
                    :value="option.value"
                  >
                    {{ option.label }}
                  </a-select-option>
                </a-select>
              </a-col>
              <a-col :span="3">
                <a-select
                  v-model:value="searchForm.sortBy"
                  placeholder="排序方式"
                  @change="handleSortChange"
                >
                  <a-select-option value="publishTime">发布时间</a-select-option>
                  <a-select-option value="title">标题</a-select-option>
                  <a-select-option value="riskLevel">风险等级</a-select-option>
                  <a-select-option value="country">国家</a-select-option>
                  <a-select-option value="matchedKeywords">匹配关键词</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="2">
                <a-select
                  v-model:value="searchForm.sortOrder"
                  @change="handleSortChange"
                >
                  <a-select-option value="desc">降序</a-select-option>
                  <a-select-option value="asc">升序</a-select-option>
                </a-select>
              </a-col>
              <a-col :span="6">
                <a-space>
                  <a-button type="primary" @click="handleSearch" :loading="loading">
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
                </a-space>
              </a-col>
            </a-row>
          </a-card>

<!--          &lt;!&ndash; 关键词统计列表 &ndash;&gt;-->
<!--          <a-card style="margin-bottom: 16px;" v-if="keywordStats.length > 0">-->
<!--            <template #title>-->
<!--              <span>匹配关键词统计</span>-->
<!--              <a-button -->
<!--                type="link" -->
<!--                size="small" -->
<!--                @click="refreshKeywordStats"-->
<!--                :loading="keywordStatsLoading"-->
<!--                style="margin-left: 8px;"-->
<!--              >-->
<!--                <template #icon>-->
<!--                  <ReloadOutlined />-->
<!--                </template>-->
<!--                刷新-->
<!--              </a-button>-->
<!--            </template>-->
<!--            <a-table-->
<!--              :columns="keywordColumns"-->
<!--              :data-source="keywordStats"-->
<!--              :loading="keywordStatsLoading"-->
<!--              :pagination="keywordPagination"-->
<!--              @change="handleKeywordTableChange"-->
<!--              row-key="keyword"-->
<!--              size="small"-->
<!--              class="keyword-table"-->
<!--            >-->
<!--              <template #bodyCell="{ column, record }">-->
<!--                <template v-if="column.key === 'keyword'">-->
<!--                  <a-button -->
<!--                    type="link" -->
<!--                    @click="filterByKeyword(record.keyword)"-->
<!--                    :class="{ 'keyword-selected': searchForm.matchedKeyword === record.keyword }"-->
<!--                  >-->
<!--                    {{ record.keyword }}-->
<!--                  </a-button>-->
<!--                </template>-->
<!--                <template v-else-if="column.key === 'count'">-->
<!--                  <a-tag color="blue">{{ record.count }} 条</a-tag>-->
<!--                </template>-->
<!--                <template v-else-if="column.key === 'highRisk'">-->
<!--                  <a-tag v-if="record.highRisk > 0" color="red">{{ record.highRisk }}</a-tag>-->
<!--                  <span v-else style="color: #999;">0</span>-->
<!--                </template>-->
<!--                <template v-else-if="column.key === 'mediumRisk'">-->
<!--                  <a-tag v-if="record.mediumRisk > 0" color="orange">{{ record.mediumRisk }}</a-tag>-->
<!--                  <span v-else style="color: #999;">0</span>-->
<!--                </template>-->
<!--                <template v-else-if="column.key === 'lowRisk'">-->
<!--                  <a-tag v-if="record.lowRisk > 0" color="green">{{ record.lowRisk }}</a-tag>-->
<!--                  <span v-else style="color: #999;">0</span>-->
<!--                </template>-->
<!--                <template v-else-if="column.key === 'action'">-->
<!--                  <a-space>-->
<!--                    <a @click="filterByKeyword(record.keyword)">筛选</a>-->
<!--                    <a-divider type="vertical" />-->
<!--                    <a @click="viewKeywordDetail(record)">详情</a>-->
<!--                  </a-space>-->
<!--                </template>-->
<!--              </template>-->
<!--            </a-table>-->
<!--          </a-card>-->

          <!-- 数据列表 -->
          <a-card>
            <template #extra>
              <a-space>
                <a-tag color="red">仅显示高风险数据</a-tag>
                <a-button @click="exportData" :loading="exporting">
                  <template #icon>
                    <DownloadOutlined />
                  </template>
                  导出高风险数据
                </a-button>
              </a-space>
            </template>

            <a-table
              :columns="columns"
              :data-source="crawlerDataList"
              :loading="loading"
              :pagination="pagination"
              @change="handleTableChange"
              row-key="id"
              :scroll="{ x: 1400 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'riskLevel'">
                  <a-tag :color="getRiskColor(record.riskLevel)">
                    {{ getRiskText(record.riskLevel) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'matchedKeywords'">
                  <a-tag v-if="record.matchedKeywords" color="orange">
                    {{ record.matchedKeywords }}
                  </a-tag>
                  <span v-else style="color: #999;">无匹配关键词</span>
                </template>
                <template v-else-if="column.key === 'country'">
                  <a-tag :color="getCountryColor(record.country)">
                    {{ getCountryName(record.country) }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'publishDate'">
                  {{ formatDate(record.publishDate) }}
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space>
                    <a @click="viewDetail(record)">查看</a>
                    <a-divider type="vertical" />
                    <a @click="editData(record)">编辑</a>
                    <a-divider type="vertical" />
                    <a @click="setRiskLevel(record)">风险等级</a>
<!--                    <a-divider type="vertical" />-->
<!--                    <a @click="generateStandardData(record)">生成标准</a>-->
<!--                    <a-divider type="vertical" />-->
                    <a-popconfirm
                      title="确定要删除这条数据吗？"
                      @confirm="deleteData(record)"
                    >
                      <a style="color: #ff4d4f">删除</a>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </div>
    </div>

    <!-- 添加/编辑数据模态框 -->
    <a-modal
      v-model:open="showAddModal"
      :title="editingData ? '编辑数据' : '添加数据'"
      width="800px"
      @ok="handleSave"
      @cancel="handleCancel"
      :confirm-loading="saving"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标题" name="title">
              <a-input
                v-model:value="formData.title"
                placeholder="请输入标题"
                :maxlength="200"
                show-count
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="国家/地区" name="country">
              <a-select
                v-model:value="formData.country"
                placeholder="请选择国家/地区"
                allow-clear
              >
                <a-select-option 
                  v-for="option in countryOptions" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据源" name="sourceName">
              <a-input
                v-model:value="formData.sourceName"
                placeholder="请输入数据源名称"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="类型" name="type">
              <a-input
                v-model:value="formData.type"
                placeholder="请输入数据类型"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="摘要" name="summary">
          <a-textarea
            v-model:value="formData.summary"
            placeholder="请输入摘要"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="内容" name="content">
          <a-textarea
            v-model:value="formData.content"
            placeholder="请输入内容"
            :rows="6"
            :maxlength="2000"
            show-count
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="URL" name="url">
              <a-input
                v-model:value="formData.url"
                placeholder="请输入URL"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="发布时间" name="publishDate">
              <a-date-picker
                v-model:value="formData.publishDate"
                style="width: 100%"
                placeholder="请选择发布时间"
                show-time
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="匹配关键词" name="related">
              <a-select
                v-model:value="formData.related"
                placeholder="请选择是否匹配关键词"
              >
                <a-select-option :value="true">匹配</a-select-option>
                <a-select-option :value="false">不匹配</a-select-option>
                <a-select-option :value="null">未确定</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="风险等级" name="riskLevel">
              <a-select
                v-model:value="formData.riskLevel"
                placeholder="请选择风险等级"
              >
                <a-select-option value="HIGH">高风险</a-select-option>
                <a-select-option value="MEDIUM">中风险</a-select-option>
                <a-select-option value="LOW">低风险</a-select-option>
                <a-select-option value="NONE">无风险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注" name="remarks">
          <a-textarea
            v-model:value="formData.remarks"
            placeholder="请输入备注"
            :rows="2"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 风险等级设置模态框 -->
    <a-modal
      v-model:open="showRiskModal"
      title="设置风险等级"
      width="500px"
      @ok="handleRiskSave"
      @cancel="handleRiskCancel"
      :confirm-loading="saving"
    >
      <a-form layout="vertical">
        <a-form-item label="数据标题">
          <a-input
            :value="selectedData?.title"
            readonly
            style="background-color: #f5f5f5"
          />
        </a-form-item>
        
        <a-form-item label="风险等级" name="riskLevel">
          <a-radio-group v-model:value="riskForm.riskLevel">
            <a-radio value="HIGH">
              <a-tag color="red">高风险</a-tag>
            </a-radio>
            <a-radio value="MEDIUM">
              <a-tag color="orange">中风险</a-tag>
            </a-radio>
            <a-radio value="LOW">
              <a-tag color="green">低风险</a-tag>
            </a-radio>
            <a-radio value="NONE">
              <a-tag color="default">无风险</a-tag>
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="风险说明" name="riskDescription">
          <a-textarea
            v-model:value="riskForm.riskDescription"
            placeholder="请输入风险说明"
            :rows="3"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 生成标准数据模态框 -->
    <a-modal
      v-model:open="showGenerateStandardModal"
      title="生成标准数据"
      width="700px"
      @ok="handleGenerateStandard"
      @cancel="handleGenerateStandardCancel"
      :confirm-loading="generatingStandard"
    >
      <a-form layout="vertical">
        <a-form-item label="标准名称" required>
          <a-input
            v-model:value="standardForm.title"
            placeholder="请输入标准名称"
            :maxlength="200"
            show-count
          />
        </a-form-item>
        
        <a-form-item label="标准描述">
          <a-textarea
            v-model:value="standardForm.description"
            placeholder="请输入标准描述"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="关键词">
              <a-input
                v-model:value="standardForm.keywords"
                placeholder="用逗号分隔多个关键词"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="国家/地区" required>
              <a-select
                v-model:value="standardForm.country"
                placeholder="请选择国家/地区"
              >
                <a-select-option 
                  v-for="option in countryOptions" 
                  :key="option.value" 
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="风险等级">
              <a-select v-model:value="standardForm.riskLevel">
                <a-select-option value="LOW">低风险</a-select-option>
                <a-select-option value="MEDIUM">中风险</a-select-option>
                <a-select-option value="HIGH">高风险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="标准状态">
              <a-select v-model:value="standardForm.standardStatus">
                <a-select-option value="DRAFT">草案</a-select-option>
                <a-select-option value="ACTIVE">生效</a-select-option>
                <a-select-option value="SUPERSEDED">已替代</a-select-option>
                <a-select-option value="WITHDRAWN">已撤销</a-select-option>
                <a-select-option value="UNDER_REVISION">修订中</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-form-item label="数据来源URL">
          <a-input
            v-model:value="standardForm.sourceUrl"
            placeholder="数据来源URL（自动填充）"
            readonly
            style="background-color: #f5f5f5"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情查看模态框 -->
    <a-modal
      v-model:open="showDetailModal"
      title="数据详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="标题" :span="2">
          {{ selectedData?.title }}
        </a-descriptions-item>
        <a-descriptions-item label="国家/地区">
          <a-tag :color="getCountryColor(selectedData?.country)">
            {{ getCountryName(selectedData?.country) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="数据源">
          {{ selectedData?.sourceName }}
        </a-descriptions-item>
        <a-descriptions-item label="类型">
          {{ selectedData?.type }}
        </a-descriptions-item>
        <a-descriptions-item label="匹配关键词">
          <a-tag :color="getRelatedColor(selectedData?.related)">
            {{ getRelatedText(selectedData?.related) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="风险等级">
          <a-tag :color="getRiskColor(selectedData?.riskLevel)">
            {{ getRiskText(selectedData?.riskLevel) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="发布时间">
          {{ formatDate(selectedData?.publishDate) }}
        </a-descriptions-item>
        <a-descriptions-item label="爬取时间">
          {{ formatDate(selectedData?.crawlTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="URL" :span="2">
          <a :href="selectedData?.url" target="_blank">{{ selectedData?.url }}</a>
        </a-descriptions-item>
        <a-descriptions-item label="摘要" :span="2">
          {{ selectedData?.summary }}
        </a-descriptions-item>
        <a-descriptions-item label="内容" :span="2">
          <div style="max-height: 200px; overflow-y: auto;">
            {{ selectedData?.content }}
          </div>
        </a-descriptions-item>
        <a-descriptions-item label="匹配关键词" :span="2">
          <a-tag v-if="selectedData?.matchedKeywords" color="orange">
            {{ selectedData.matchedKeywords }}
          </a-tag>
          <span v-else style="color: #999;">无匹配关键词</span>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ selectedData?.remarks || '无备注' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { Statistic } from 'ant-design-vue'
import {
  ReloadOutlined,
  PlusOutlined,
  SearchOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { getCrawlerData, updateCrawlerDataRiskLevel, updateCrawlerDataFull, deleteCrawlerData, getRiskLevelStatistics } from '@/api/pachongshujuguanli'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'

// 路由实例
const route = useRoute()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const showAddModal = ref(false)
const showRiskModal = ref(false)
const showDetailModal = ref(false)
const showGenerateStandardModal = ref(false)
const editingData = ref<any>(null)
const selectedData = ref<any>(null)
const generatingStandard = ref(false)
const formRef = ref()
const crawlerDataList = ref<any[]>([])
const showAllRiskLevels = ref(false) // 是否显示所有风险等级

// 国家选项列表（按用户指定顺序排列）
const countryList = ref<Array<{code: string, name: string}>>([
  { code: 'US', name: '美国' },
  { code: 'EU', name: '欧盟' },
  { code: 'CN', name: '中国' },
  { code: 'KR', name: '韩国' },
  { code: 'JP', name: '日本' },
  { code: 'AE', name: '阿联酋' },
  { code: 'IN', name: '印度' },
  { code: 'TH', name: '泰国' },
  { code: 'SG', name: '新加坡' },
  { code: 'TW', name: '台湾' },
  { code: 'AU', name: '澳大利亚' },
  { code: 'CL', name: '智利' },
  { code: 'MY', name: '马来西亚' },
  { code: 'PE', name: '秘鲁' },
  { code: 'ZA', name: '南非' },
  { code: 'IL', name: '以色列' },
  { code: 'ID', name: '印尼' },
  { code: 'OTHER', name: '其他国家' },
  { code: 'UNKNOWN', name: '未确定' }
])

// 表单数据
const formData = reactive({
  id: '',
  title: '',
  country: undefined as string | undefined,
  sourceName: '',
  type: '',
  summary: '',
  content: '',
  url: '',
  publishDate: null as Dayjs | null,
  related: null as boolean | null,
  riskLevel: 'NONE' as string,
  remarks: ''
})

// 风险等级表单
const riskForm = reactive({
  riskLevel: 'NONE' as string,
  riskDescription: ''
})

// 生成标准数据表单
const standardForm = reactive({
  title: '',
  description: '',
  keywords: '',
  country: '',
  riskLevel: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
  standardStatus: 'ACTIVE' as 'DRAFT' | 'ACTIVE' | 'SUPERSEDED' | 'WITHDRAWN' | 'UNDER_REVISION',
  sourceUrl: ''
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  country: [{ required: true, message: '请选择国家/地区', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  matchedKeyword: undefined as string | undefined,
  country: undefined as string | undefined,
  sortBy: 'publishTime' as string,
  sortOrder: 'desc' as string
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) => 
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 统计数据
const statistics = reactive({
  total: 0,        // 高风险数据总数
  todayCount: 0,   // 今日新增高风险数据
  countryCount: 0  // 涉及的国家数量
})

// 关键词相关数据
const keywordStats = ref<any[]>([])
const keywordOptions = ref<Array<{value: string, label: string, count: number}>>([])
const keywordStatsLoading = ref(false)


// 页面标题更新
const pageTitle = ref('高风险数据管理')

// 国家选项 - 从国家列表生成
const countryOptions = ref<Array<{value: string, label: string}>>([])

// 表格列配置
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 300,
    ellipsis: true
  },
  {
    title: '国家/地区',
    dataIndex: 'country',
    key: 'country',
    width: 100
  },
  {
    title: '匹配关键词',
    dataIndex: 'matchedKeywords',
    key: 'matchedKeywords',
    width: 200
  },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    key: 'riskLevel',
    width: 100
  },
  {
    title: '数据源',
    dataIndex: 'sourceName',
    key: 'sourceName',
    width: 120
  },
  {
    title: '发布时间',
    dataIndex: 'publishDate',
    key: 'publishDate',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 初始化国家选项
const initCountryOptions = () => {
  countryOptions.value = countryList.value.map(country => ({
    value: country.name, // 使用中文名称作为value，与实际数据中的country字段匹配
    label: country.name
  }))
}

// 从高风险数据中提取国家信息用于筛选 - 暂时注释掉动态显示功能
/*
const updateCountryOptionsFromData = () => {
  const countrySet = new Set<string>()
  crawlerDataList.value.forEach(item => {
    if (item.country && item.riskLevel === 'HIGH') {
      countrySet.add(item.country.trim()) // 添加trim处理
    }
  })
  
  // 更新国家选项，只包含有高风险数据的国家
  const existingCountries = countryList.value.filter(country => 
    countrySet.has(country.name) // 使用name而不是code进行匹配
  )
  
  // 添加新发现的国家（不在预定义列表中的）
  countrySet.forEach(countryName => {
    if (!existingCountries.find(c => c.name === countryName)) {
      existingCountries.push({
        code: 'UNKNOWN', // 未知国家使用UNKNOWN代码
        name: countryName
      })
    }
  })
  
  countryOptions.value = existingCountries.map(country => ({
    value: country.name, // 使用中文名称作为value
    label: country.name
  }))
}
*/


// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      matchedKeyword: searchForm.matchedKeyword,
      country: searchForm.country,
      riskLevel: showAllRiskLevels.value ? undefined : 'HIGH', // 默认只显示高风险数据
      sortBy: searchForm.sortBy,
      sortOrder: searchForm.sortOrder
    }

    const result = await getCrawlerData(params as any)
    
    if (result && result.data) {
      crawlerDataList.value = (result.data as any).content || []
      const totalFromBackend = ((result.data as any).total ?? (result.data as any).totalElements) || 0
      pagination.total = totalFromBackend
      
      // 更新国家筛选选项（基于高风险数据）- 暂时注释掉动态显示功能
      // updateCountryOptionsFromData()
      
      // 更新统计数据
      await updateStatistics(totalFromBackend)
      
      console.log('📋 高风险数据加载完成:', {
        count: crawlerDataList.value.length,
        total: totalFromBackend,
        countries: countryOptions.value.length
      })
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateStatistics = async (totalFromBackend?: number) => {
  try {
    // 获取高风险数据统计
    const result = await getRiskLevelStatistics() as any
    
    if (result && result.success && result.data) {
      // 高风险数据总数
      statistics.total = result.data.highRiskCount || 0
      
      // 直接从高风险数据计算国家数量（更加精确和高效）
      const uniqueCountries = new Set<string>()
      
      // 从当前加载的高风险数据中统计国家
      const highRiskData = crawlerDataList.value.filter(item => item.riskLevel === 'HIGH')
      highRiskData.forEach(item => {
        if (item.country && item.country.trim()) {
          uniqueCountries.add(item.country.trim())
        }
      })
      
      // 如果当前数据不够，尝试从所有高风险数据中计算
      if (uniqueCountries.size === 0 || highRiskData.length < 50) {
        try {
          console.log('🔍 当前数据不够，查询所有高风险数据的国家分布...')
          const allHighRiskResult = await getCrawlerData({
            page: 0,
            size: 1000, // 获取更多数据用于统计
            riskLevel: 'HIGH'
          }) as any
          
          if (allHighRiskResult && allHighRiskResult.data && allHighRiskResult.data.content) {
            const allHighRiskData = allHighRiskResult.data.content
            allHighRiskData.forEach((item: any) => {
              if (item.country && item.country.trim()) {
                uniqueCountries.add(item.country.trim())
              }
            })
            console.log('🌍 从所有高风险数据中统计到 {} 个国家', uniqueCountries.size)
          }
        } catch (error) {
          console.warn('⚠️ 查询所有高风险数据失败，使用当前数据:', error)
        }
      }
      
      statistics.countryCount = uniqueCountries.size
      console.log('🌍 高风险数据涉及国家数量:', statistics.countryCount, '国家列表:', Array.from(uniqueCountries))
      
      // TODO: 实现今日新增高风险数据统计
      statistics.todayCount = 0 // 暂时设为0，需要后端支持按日期筛选的高风险数据统计
      
      console.log('📊 高风险数据统计:', {
        total: statistics.total,
        todayCount: statistics.todayCount,
        countryCount: statistics.countryCount
      })
    } else {
      // 如果API调用失败，从当前数据计算
      const highRiskData = crawlerDataList.value.filter(item => item.riskLevel === 'HIGH')
      statistics.total = totalFromBackend ?? highRiskData.length
      
      // 直接从高风险数据计算国家数量（更加精确和高效）
      const uniqueCountries = new Set<string>()
      
      // 从当前加载的高风险数据中统计国家
      highRiskData.forEach(item => {
        if (item.country && item.country.trim()) {
          uniqueCountries.add(item.country.trim())
        }
      })
      
      // 如果当前数据不够，尝试从所有高风险数据中计算
      if (uniqueCountries.size === 0 || crawlerDataList.value.length < 50) {
        try {
          console.log('🔍 当前数据不够，查询所有高风险数据的国家分布...')
          const allHighRiskResult = await getCrawlerData({
            page: 0,
            size: 1000, // 获取更多数据用于统计
            riskLevel: 'HIGH'
          }) as any
          
          if (allHighRiskResult && allHighRiskResult.data && allHighRiskResult.data.content) {
            const allHighRiskData = allHighRiskResult.data.content
            allHighRiskData.forEach((item: any) => {
              if (item.country && item.country.trim()) {
                uniqueCountries.add(item.country.trim())
              }
            })
            console.log('🌍 从所有高风险数据中统计到 {} 个国家', uniqueCountries.size)
          }
        } catch (error) {
          console.warn('⚠️ 查询所有高风险数据失败，使用当前数据:', error)
        }
      }
      
      statistics.countryCount = uniqueCountries.size
      console.log('🌍 高风险数据涉及国家数量:', statistics.countryCount, '国家列表:', Array.from(uniqueCountries))
      statistics.todayCount = 0
    }
  } catch (error) {
    console.error('更新统计数据失败:', error)
    // 如果统计更新失败，使用基本统计
    statistics.total = totalFromBackend ?? 0
    statistics.todayCount = 0
    statistics.countryCount = 0
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleSortChange = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    matchedKeyword: undefined,
    country: undefined,
    sortBy: 'publishTime',
    sortOrder: 'desc'
  })
  showAllRiskLevels.value = false // 重置为只显示高风险
  pagination.current = 1
  loadData()
}


const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const refreshData = () => {
  loadData()
}

const exportData = async () => {
  exporting.value = true
  try {
    // 实现导出功能
    message.success('导出功能开发中...')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const viewDetail = (record: any) => {
  selectedData.value = record
  showDetailModal.value = true
}

const editData = (record: any) => {
  editingData.value = record
  Object.assign(formData, {
    id: record.id,
    title: record.title,
    country: record.country,
    sourceName: record.sourceName || '',
    type: record.type || '',
    summary: record.summary || '',
    content: record.content || '',
    url: record.url || '',
    publishDate: record.publishDate ? dayjs(record.publishDate) : null,
    related: record.related,
    riskLevel: record.riskLevel || 'NONE',
    remarks: record.remarks || ''
  })
  showAddModal.value = true
}

const setRiskLevel = (record: any) => {
  selectedData.value = record
  Object.assign(riskForm, {
    riskLevel: record.riskLevel || 'NONE',
    riskDescription: record.riskDescription || ''
  })
  showRiskModal.value = true
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    const data = {
      ...formData,
      publishDate: formData.publishDate ? formData.publishDate.format('YYYY-MM-DD HH:mm:ss') : undefined,
      related: formData.related === null ? undefined : formData.related,
      riskLevel: formData.riskLevel as 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH'
    }
    
    const result = await updateCrawlerDataFull(data)
    
    if (result && result.data) {
      message.success(editingData.value ? '更新成功' : '添加成功')
      showAddModal.value = false
      loadData()
      handleCancel()
    } else {
      message.error(editingData.value ? '更新失败' : '添加失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleRiskSave = async () => {
  try {
    saving.value = true
    
    // 使用新的风险等级更新API
    const result = await updateCrawlerDataRiskLevel({
      id: selectedData.value.id,
      riskLevel: riskForm.riskLevel
    })
    
    if (result && result.data) {
      message.success('风险等级设置成功')
      showRiskModal.value = false
      loadData()
      // 刷新关键词统计
      await loadKeywordStats()
      handleRiskCancel()
    } else {
      message.error('风险等级设置失败')
    }
  } catch (error) {
    console.error('设置风险等级失败:', error)
    message.error('设置风险等级失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  editingData.value = null
  Object.assign(formData, {
    id: '',
    title: '',
    country: undefined,
    sourceName: '',
    type: '',
    summary: '',
    content: '',
    url: '',
    publishDate: null,
    related: null,
    riskLevel: 'NONE',
    remarks: ''
  })
  formRef.value?.resetFields()
}

const handleRiskCancel = () => {
  selectedData.value = null
  Object.assign(riskForm, {
    riskLevel: 'NONE',
    riskDescription: ''
  })
}

const generateStandardData = (record: any) => {
  selectedData.value = record
  
  // 预填充标准数据表单
  Object.assign(standardForm, {
    title: record.title || '',
    description: record.summary || record.content || '',
    keywords: record.matchedKeywords || '',
    country: record.country || '',
    riskLevel: record.riskLevel === 'HIGH' ? 'HIGH' : 'MEDIUM',
    standardStatus: 'ACTIVE',
    sourceUrl: record.url || ''
  })
  
  showGenerateStandardModal.value = true
}

const handleGenerateStandard = async () => {
  if (!standardForm.title.trim()) {
    message.error('请输入标准名称')
    return
  }
  
  if (!standardForm.country) {
    message.error('请选择国家/地区')
    return
  }
  
  try {
    generatingStandard.value = true
    
    // 准备标准数据
    const standardData = {
      standardNumber: `STD-${Date.now()}`, // 生成唯一编号
      title: standardForm.title,
      description: standardForm.description,
      keywords: standardForm.keywords,
      country: standardForm.country,
      countries: [standardForm.country],
      riskLevel: standardForm.riskLevel as 'LOW' | 'MEDIUM' | 'HIGH',
      standardStatus: standardForm.standardStatus as 'DRAFT' | 'ACTIVE' | 'SUPERSEDED' | 'WITHDRAWN' | 'UNDER_REVISION',
      downloadUrl: standardForm.sourceUrl,
      publishedDate: new Date().toISOString().split('T')[0],
      isMonitored: true,
      // 添加数据来源信息
      rawExcerpt: selectedData.value?.content || '',
      scope: `基于爬虫数据生成 - 来源: ${selectedData.value?.sourceName || '未知'}`,
      sourceUrl: standardForm.sourceUrl
    }
    
    // 调用创建标准API
    const { createStandard } = await import('@/api/biaozhunguanli')
    const result = await createStandard(standardData)
    
    if (result) {
      message.success('标准数据生成成功！')
      showGenerateStandardModal.value = false
      handleGenerateStandardCancel()
    } else {
      message.error('生成标准数据失败')
    }
  } catch (error: any) {
    console.error('生成标准数据失败:', error)
    message.error('生成标准数据失败: ' + (error.message || '未知错误'))
  } finally {
    generatingStandard.value = false
  }
}

const handleGenerateStandardCancel = () => {
  selectedData.value = null
  Object.assign(standardForm, {
    title: '',
    description: '',
    keywords: '',
    country: '',
    riskLevel: 'MEDIUM',
    standardStatus: 'ACTIVE',
    sourceUrl: ''
  })
}

const deleteData = async (record: any) => {
  try {
    const result = await deleteCrawlerData({ id: record.id })
    if (result && result.data) {
      message.success('删除成功')
      loadData()
    } else {
      message.error('删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 工具方法
const getRiskColor = (riskLevel: string) => {
  const colors: Record<string, string> = {
    'HIGH': 'red',
    'MEDIUM': 'orange',
    'LOW': 'green',
    'NONE': 'default'
  }
  return colors[riskLevel] || 'default'
}

const getRiskText = (riskLevel: string) => {
  const texts: Record<string, string> = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险',
    'NONE': '无风险'
  }
  return texts[riskLevel] || '无风险'
}

const getRelatedColor = (related: boolean | null) => {
  if (related === true) return 'green'
  if (related === false) return 'red'
  return 'default'
}

const getRelatedText = (related: boolean | null) => {
  if (related === true) return '匹配'
  if (related === false) return '不匹配'
  return '未确定'
}

const getCountryColor = (country: string) => {
  if (!country || country === '未确定' || country === 'UNKNOWN') return 'default'
  
  // 预设颜色映射（按用户指定的顺序和优先级）
  const colors: Record<string, string> = {
    '美国': 'blue',
    '欧盟': 'green', 
    '中国': 'red',
    '韩国': 'cyan',
    '日本': 'purple',
    '阿联酋': 'orange',
    '印度': 'volcano',
    '泰国': 'gold',
    '新加坡': 'lime',
    '台湾': 'geekblue',
    '澳大利亚': 'magenta',
    '智利': 'pink',
    '马来西亚': 'processing',
    '秘鲁': 'warning',
    '南非': 'success',
    '以色列': 'error',
    '印尼': 'default',
    '其他国家': 'default',
    '未确定': 'default'
  }
  
  // 如果有预设颜色就使用，否则使用默认颜色
  return colors[country] || 'default'
}

const getCountryName = (country: string) => {
  if (!country || country === '未确定') return '未确定'
  // 数据中的国家字段就是中文名称，直接返回
  return country
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 关键词相关方法
const loadKeywordStats = async () => {
  keywordStatsLoading.value = true
  try {
    // 获取高风险数据来统计关键词
    const allDataParams = {
      page: 0,
      size: 10000, // 获取大量数据进行统计
      riskLevel: 'HIGH', // 只统计高风险数据
      keyword: searchForm.keyword,
      country: searchForm.country
    }
    
    const allDataResponse = await getCrawlerData(allDataParams)
    const allData = (allDataResponse.data as any)?.content || []
    
    // 统计高风险数据中的关键词
    const keywordMap = new Map<string, number>()
    
    allData.forEach((item: any) => {
      if (item.matchedKeywords && item.riskLevel === 'HIGH') {
        const keywords = item.matchedKeywords.split(',').map((k: string) => k.trim()).filter((k: string) => k)
        keywords.forEach((keyword: string) => {
          keywordMap.set(keyword, (keywordMap.get(keyword) || 0) + 1)
        })
      }
    })
    
    // 转换为数组并排序（按出现次数降序）
    keywordStats.value = Array.from(keywordMap.entries())
      .map(([keyword, count]) => ({
        keyword,
        count,
        highRisk: count // 都是高风险数据
      }))
      .sort((a, b) => b.count - a.count)
    
    // 更新关键词选项
    keywordOptions.value = keywordStats.value.map(item => ({
      value: item.keyword,
      label: item.keyword,
      count: item.count
    }))
    
    console.log('📊 高风险关键词统计加载完成:', keywordStats.value.length, '个关键词')
  } catch (error) {
    console.error('加载关键词统计失败:', error)
    message.error('加载关键词统计失败')
  } finally {
    keywordStatsLoading.value = false
  }
}

const filterOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 组件挂载时初始化
onMounted(async () => {
  // 初始化国家选项
  initCountryOptions()
  
  // 处理URL参数
  const urlCountry = route.query.country as string
  
  if (urlCountry) {
    // 设置国家筛选
    searchForm.country = urlCountry
    console.log('从URL设置国家筛选:', urlCountry)
  }
  
  // 加载关键词统计（用于关键词筛选）
  await loadKeywordStats()
  
  // 加载高风险数据
  console.log('🔍 开始加载高风险数据...')
  loadData()
})
</script>

<style scoped>
.crawler-data-management-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.tab-content {
  padding: 16px 0;
}

.tab-header {
  margin-bottom: 16px;
}

.stats-section {
  margin-bottom: 24px;
}

.ant-table-wrapper {
  margin-top: 16px;
}

/* 国家管理表格样式 */
.country-table .ant-table-tbody > tr > td {
  vertical-align: middle;
}

.country-table .ant-tag {
  margin: 0;
}

/* 国家卡片样式 */
.country-cards {
  margin-bottom: 24px;
}

.country-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.country-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.country-card-header {
  margin-bottom: 12px;
  text-align: center;
}

.country-card-body {
  text-align: center;
}

.risk-stats {
  margin-top: 12px;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 6px;
}

.risk-stats .ant-tag {
  margin: 0;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 关键词表格样式 */
.keyword-selected {
  color: #1890ff !important;
  font-weight: 600;
}

.keyword-table .ant-table-tbody > tr > td {
  vertical-align: middle;
}

.keyword-table .ant-tag {
  margin: 0;
}
</style>

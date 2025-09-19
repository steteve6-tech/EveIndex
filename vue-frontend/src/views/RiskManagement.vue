<template>
  <div class="risk-management-page">
    <div class="page-header">
      <h1>风险管理</h1>
      <a-space>
        <a-button @click="loadKeywords" :loading="loading">
          刷新匹配数量
        </a-button>
        <a-button type="primary" @click="showAddKeywordModal = true">
          添加风险关键字
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="24">
      <!-- 风险统计 -->
      <a-col :span="8">
        <a-card title="风险统计">
          <div class="risk-stats">
            <div class="stat-item">
              <div class="stat-number high">{{ stats.high }}</div>
              <div class="stat-label">高风险</div>
            </div>
            <div class="stat-item">
              <div class="stat-number medium">{{ stats.medium }}</div>
              <div class="stat-label">中风险</div>
            </div>
            <div class="stat-item">
              <div class="stat-number low">{{ stats.low }}</div>
              <div class="stat-label">低风险</div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 风险关键字管理 -->
      <a-col :span="16">
        <a-card title="风险关键字管理">
          <a-table
            :columns="keywordColumns"
            :data-source="keywords"
            :loading="loading"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'matchCount'">
                <a-tag 
                  :color="getMatchCountColor(record.matchCount)"
                  :class="getMatchCountClass(record.matchCount)"
                >
                  {{ record.matchCount || 0 }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'severity'">
                <a-tag :color="getSeverityColor(record.severity)">
                  {{ record.severity }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a @click="editKeyword(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a-popconfirm
                    title="确定要删除这个关键字吗？"
                    @confirm="deleteKeyword(record.id)"
                  >
                    <a class="text-danger">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 添加/编辑关键字模态框 -->
    <a-modal
      v-model:open="showAddKeywordModal"
      :title="editingKeyword ? '编辑风险关键字' : '添加风险关键字'"
      @ok="handleSaveKeyword"
      @cancel="handleCancelKeyword"
    >
      <a-form
        ref="keywordFormRef"
        :model="keywordForm"
        :rules="keywordRules"
        layout="vertical"
      >
        <a-form-item label="关键字" name="keyword">
          <a-input v-model:value="keywordForm.keyword" />
        </a-form-item>
        <a-form-item label="分类" name="category">
          <a-select v-model:value="keywordForm.category">
            <a-select-option value="core_tech">核心技术</a-select-option>
            <a-select-option value="product_terms">产品关键词</a-select-option>
            <a-select-option value="environmental">环境法规</a-select-option>
            <a-select-option value="regional">区域法规</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="严重程度" name="severity">
          <a-select v-model:value="keywordForm.severity">
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="keywordForm.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'

const loading = ref(false)
const showAddKeywordModal = ref(false)
const editingKeyword = ref<any>(null)

const stats = reactive({
  high: 5,
  medium: 12,
  low: 8
})

const keywords = ref([
  {
    id: 1,
    keyword: 'EN 18031',
    category: 'core_tech',
    severity: 'HIGH',
    description: '网络安全要求标准',
    matchCount: 15
  },
  {
    id: 2,
    keyword: 'FCC Part 15',
    category: 'core_tech',
    severity: 'MEDIUM',
    description: '美国FCC无线设备认证',
    matchCount: 8
  },
  {
    id: 3,
    keyword: 'REACH',
    category: 'environmental',
    severity: 'MEDIUM',
    description: '欧盟化学品法规',
    matchCount: 3
  }
])

const keywordColumns = [
  {
    title: '关键字',
    dataIndex: 'keyword',
    key: 'keyword'
  },
  {
    title: '匹配数量',
    dataIndex: 'matchCount',
    key: 'matchCount',
    width: 100,
    sorter: (a, b) => a.matchCount - b.matchCount
  },
  {
    title: '分类',
    dataIndex: 'category',
    key: 'category',
    width: 120
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    key: 'severity',
    width: 100
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description'
  },
  {
    title: '操作',
    key: 'action',
    width: 120
  }
]

const keywordForm = reactive({
  keyword: '',
  category: 'core_tech',
  severity: 'MEDIUM',
  description: ''
})

const keywordRules = {
  keyword: [{ required: true, message: '请输入关键字' }],
  category: [{ required: true, message: '请选择分类' }],
  severity: [{ required: true, message: '请选择严重程度' }]
}

const keywordFormRef = ref<FormInstance>()

const getSeverityColor = (severity: string) => {
  switch (severity) {
    case 'HIGH': return 'red'
    case 'MEDIUM': return 'orange'
    case 'LOW': return 'green'
    default: return 'default'
  }
}

const getMatchCountColor = (count: number) => {
  if (count === 0) return 'default'
  if (count <= 5) return 'green'
  if (count <= 20) return 'orange'
  return 'red'
}

const getMatchCountClass = (count: number) => {
  if (count === 0) return 'match-count-zero'
  if (count <= 5) return 'match-count-low'
  if (count <= 20) return 'match-count-medium'
  return 'match-count-high'
}

const editKeyword = (record: any) => {
  editingKeyword.value = record
  Object.assign(keywordForm, record)
  showAddKeywordModal.value = true
}

const deleteKeyword = async (id: number) => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    message.success('删除成功')
    loadKeywords()
  } catch (error) {
    message.error('删除失败')
  }
}

const handleSaveKeyword = async () => {
  try {
    await keywordFormRef.value?.validate()
    
    if (editingKeyword.value) {
      message.success('更新成功')
    } else {
      message.success('添加成功')
    }
    
    showAddKeywordModal.value = false
    loadKeywords()
  } catch (error) {
    message.error('保存失败')
  }
}

const handleCancelKeyword = () => {
  showAddKeywordModal.value = false
  editingKeyword.value = null
  keywordFormRef.value?.resetFields()
}

const loadKeywords = async () => {
  loading.value = true
  try {
    // 调用后端API获取带匹配数量的关键词列表
    const response = await fetch('/api/keywords/with-match-counts')
    const result = await response.json()
    
    if (result.success) {
      keywords.value = result.keywords || []
      message.success(`加载了 ${result.totalCount} 个关键词`)
    } else {
      message.error(result.error || '加载数据失败')
    }
  } catch (error) {
    console.error('加载关键词失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadKeywords()
})
</script>

<style scoped>
.risk-management-page {
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

.risk-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-number.high {
  color: #ff4d4f;
}

.stat-number.medium {
  color: #faad14;
}

.stat-number.low {
  color: #52c41a;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.text-danger {
  color: #ff4d4f;
}

.match-count-tag {
  font-weight: bold;
  min-width: 40px;
  text-align: center;
}

.match-count-high {
  background-color: #ff4d4f;
  color: white;
}

.match-count-medium {
  background-color: #faad14;
  color: white;
}

.match-count-low {
  background-color: #52c41a;
  color: white;
}

.match-count-zero {
  background-color: #d9d9d9;
  color: #666;
}
</style>

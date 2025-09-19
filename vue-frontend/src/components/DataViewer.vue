<template>
  <div class="crawler-data-viewer">
    <!-- 标题 -->
    <div class="header">
      <h1>爬虫数据查看器</h1>
      <div class="stats">
        <span>总数据: {{ totalCount }}</span>
        <span>今日新增: {{ todayCount }}</span>
      </div>
    </div>

    <!-- 过滤器 -->
    <div class="filters">
      <div class="filter-group">
        <label>数据源:</label>
        <select v-model="filters.sourceName" @change="loadData">
          <option value="">全部</option>
          <option value="UL Solutions">UL Solutions</option>
          <option value="SGS">SGS</option>
        </select>
      </div>

      <div class="filter-group">
        <label>状态:</label>
        <select v-model="filters.status" @change="loadData">
          <option value="">全部</option>
          <option value="NEW">新建</option>
          <option value="PROCESSED">已处理</option>
          <option value="ERROR">错误</option>
          <option value="DUPLICATE">重复</option>
        </select>
      </div>

      <div class="filter-group">
        <label>关键词:</label>
        <input
            v-model="filters.keyword"
            placeholder="搜索标题、内容..."
            @input="debounceSearch"
        />
      </div>

      <div class="filter-group">
        <button @click="refreshData" :disabled="loading">
          {{ loading ? '加载中...' : '刷新' }}
        </button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="data-container">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>正在加载数据...</p>
      </div>

      <div v-else-if="data.length === 0" class="empty">
        <p>暂无数据</p>
      </div>

      <div v-else class="data-list">
        <div
            v-for="item in data"
            :key="item.id"
            class="data-item"
            :class="getStatusClass(item.status)"
        >
          <div class="item-header">
            <h3 class="title">{{ item.title }}</h3>
            <div class="meta">
              <span class="source">{{ item.sourceName }}</span>
              <span class="status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
              <span class="time">{{ formatDate(item.crawlTime) }}</span>
            </div>
          </div>

          <div class="item-content">
            <div class="info-row">
              <span class="label">国家:</span>
              <span class="value">{{ item.country || '未知' }}</span>
            </div>
            <div class="info-row">
              <span class="label">类型:</span>
              <span class="value">{{ item.type || '未知' }}</span>
            </div>
            <div class="info-row">
              <span class="label">发布时间:</span>
              <span class="value">{{ item.publishDate || '未知' }}</span>
            </div>
            <div class="info-row" v-if="item.summary">
              <span class="label">摘要:</span>
              <span class="value summary">{{ item.summary }}</span>
            </div>
          </div>

          <div class="item-actions">
            <button
                @click="markAsProcessed(item.id)"
                :disabled="item.status === 'PROCESSED'"
                class="btn-process"
            >
              标记已处理
            </button>
            <a
                :href="item.url"
                target="_blank"
                rel="noopener noreferrer"
                class="btn-view"
            >
              查看原文
            </a>
            <button
                @click="toggleDetail(item.id)"
                class="btn-detail"
            >
              {{ expandedItems.includes(item.id) ? '收起详情' : '查看详情' }}
            </button>
          </div>

          <!-- 详细信息 -->
          <div v-if="expandedItems.includes(item.id)" class="item-detail">
            <div class="detail-content">
              <h4>详细内容:</h4>
              <div class="content-text" v-html="formatContent(item.content)"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
          @click="changePage(page - 1)"
          :disabled="page === 0"
          class="btn-page"
      >
        上一页
      </button>

      <span class="page-info">
        第 {{ page + 1 }} 页，共 {{ totalPages }} 页
      </span>

      <button
          @click="changePage(page + 1)"
          :disabled="page >= totalPages - 1"
          class="btn-page"
      >
        下一页
      </button>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'CrawlerDataViewer',
  data() {
    return {
      data: [],
      loading: false,
      page: 0,
      size: 10,
      totalPages: 0,
      totalCount: 0,
      todayCount: 0,
      filters: {
        sourceName: '',
        status: '',
        keyword: ''
      },
      expandedItems: [],
      message: '',
      messageType: 'info',
      searchTimeout: null
    };
  },

  methods: {
    // 加载数据
    async loadData() {
      this.loading = true;
      try {
        let url = `/api/crawler-data?page=${this.page}&size=${this.size}`;

        // 根据过滤器构建URL
        if (this.filters.sourceName) {
          url = `/api/crawler-data/source/${encodeURIComponent(this.filters.sourceName)}/page?page=${this.page}&size=${this.size}`;
        } else if (this.filters.status) {
          url = `/api/crawler-data/status/${this.filters.status}?page=${this.page}&size=${this.size}`;
        } else if (this.filters.keyword) {
          url = `/api/crawler-data/search?keyword=${encodeURIComponent(this.filters.keyword)}&page=${this.page}&size=${this.size}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        this.data = result.content || result;
        this.totalPages = result.totalPages || 0;
        this.totalCount = result.totalElements || result.length;

      } catch (error) {
        console.error('加载数据失败:', error);
        this.showMessage('加载数据失败: ' + error.message, 'error');
      } finally {
        this.loading = false;
      }
    },

    // 加载统计信息
    async loadStatistics() {
      try {
        const response = await fetch('/api/crawler-data/statistics');
        if (response.ok) {
          const stats = await response.json();
          this.totalCount = stats.totalCount || 0;
          this.todayCount = stats.todayCount || 0;
        }
      } catch (error) {
        console.error('加载统计信息失败:', error);
      }
    },

    // 标记为已处理
    async markAsProcessed(id) {
      try {
        const response = await fetch(`/api/crawler-data/${id}/mark-processed`, {
          method: 'PUT'
        });

        if (response.ok) {
          this.showMessage('标记成功', 'success');
          this.loadData(); // 重新加载数据
        } else {
          throw new Error('标记失败');
        }
      } catch (error) {
        console.error('标记失败:', error);
        this.showMessage('标记失败: ' + error.message, 'error');
      }
    },

    // 切换详情显示
    toggleDetail(id) {
      const index = this.expandedItems.indexOf(id);
      if (index > -1) {
        this.expandedItems.splice(index, 1);
      } else {
        this.expandedItems.push(id);
      }
    },

    // 切换页面
    changePage(newPage) {
      this.page = newPage;
      this.loadData();
    },

    // 刷新数据
    refreshData() {
      this.loadData();
      this.loadStatistics();
    },

    // 防抖搜索
    debounceSearch() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }
      this.searchTimeout = setTimeout(() => {
        this.page = 0;
        this.loadData();
      }, 500);
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },

    // 格式化内容
    formatContent(content) {
      if (!content) return '暂无详细内容';
      return content.replace(/\n/g, '<br>');
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'NEW': '新建',
        'PROCESSING': '处理中',
        'PROCESSED': '已处理',
        'ERROR': '错误',
        'DUPLICATE': '重复'
      };
      return statusMap[status] || status;
    },

    // 获取状态样式类
    getStatusClass(status) {
      return `status-${status.toLowerCase()}`;
    },

    // 显示消息
    showMessage(text, type = 'info') {
      this.message = text;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
      }, 3000);
    }
  },

  mounted() {
    this.loadData();
    this.loadStatistics();
  }
};
</script>

<style scoped>
.crawler-data-viewer {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.stats {
  display: flex;
  gap: 20px;
}

.stats span {
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
}

.filters {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #555;
  min-width: 60px;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.filter-group button {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.filter-group button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.data-container {
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty {
  text-align: center;
  padding: 40px;
  color: #666;
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.data-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  background: white;
  transition: all 0.2s ease;
}

.data-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.item-header {
  margin-bottom: 10px;
}

.title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  line-height: 1.4;
}

.meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #666;
}

.source {
  font-weight: 500;
  color: #007bff;
}

.status {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
}

.status-new { background: #e3f2fd; color: #1976d2; }
.status-processing { background: #fff3e0; color: #f57c00; }
.status-processed { background: #e8f5e8; color: #388e3c; }
.status-error { background: #ffebee; color: #d32f2f; }
.status-duplicate { background: #f3e5f5; color: #7b1fa2; }

.item-content {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  margin-bottom: 5px;
  font-size: 14px;
}

.label {
  font-weight: 500;
  color: #555;
  min-width: 80px;
}

.value {
  color: #333;
}

.summary {
  font-style: italic;
  color: #666;
}

.item-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.item-actions button,
.item-actions a {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  text-decoration: none;
  display: inline-block;
}

.btn-process {
  background: #28a745;
  color: white;
}

.btn-process:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-view {
  background: #17a2b8;
  color: white;
}

.btn-detail {
  background: #6c757d;
  color: white;
}

.item-detail {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.detail-content h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
}

.content-text {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.5;
  color: #555;
  max-height: 200px;
  overflow-y: auto;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.btn-page {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-page:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.message.info { background: #17a2b8; }
.message.success { background: #28a745; }
.message.error { background: #dc3545; }
.message.warning { background: #ffc107; color: #333; }

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .crawler-data-viewer {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .filters {
    flex-direction: column;
    gap: 10px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .meta {
    flex-direction: column;
    gap: 5px;
  }

  .item-actions {
    flex-direction: column;
  }

  .pagination {
    flex-direction: column;
    gap: 10px;
  }
}
</style>

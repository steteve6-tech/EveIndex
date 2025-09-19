<template>
  <div class="dashboard">
    <!-- 头部统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statistics" :key="stat.title">
        <div class="stat-icon" :class="stat.iconClass">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-content">
          <h3 class="stat-title">{{ stat.title }}</h3>
          <p class="stat-value">{{ stat.value }}</p>
          <p class="stat-change" :class="stat.changeClass">
            {{ stat.change }}
          </p>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>数据源分布</h3>
          <button @click="generateSourcePieChart" class="btn-refresh">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
        <div class="chart-content">
          <img v-if="sourcePieChart" :src="sourcePieChart" alt="数据源分布" class="chart-image" />
          <div v-else class="chart-placeholder">
            <i class="fas fa-chart-pie"></i>
            <p>点击刷新生成图表</p>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3>数据状态分布</h3>
          <button @click="generateStatusBarChart" class="btn-refresh">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
        <div class="chart-content">
          <img v-if="statusBarChart" :src="statusBarChart" alt="数据状态分布" class="chart-image" />
          <div v-else class="chart-placeholder">
            <i class="fas fa-chart-bar"></i>
            <p>点击刷新生成图表</p>
          </div>
        </div>
      </div>

      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>数据趋势 (30天)</h3>
          <button @click="generateTrendChart" class="btn-refresh">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
        <div class="chart-content">
          <img v-if="trendChart" :src="trendChart" alt="数据趋势" class="chart-image" />
          <div v-else class="chart-placeholder">
            <i class="fas fa-chart-line"></i>
            <p>点击刷新生成图表</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新数据表格 -->
    <div class="data-section">
      <div class="section-header">
        <h3>最新数据</h3>
        <div class="section-actions">
          <button @click="exportAllData" class="btn-export">
            <i class="fas fa-download"></i> 导出所有数据
          </button>
          <button @click="refreshData" class="btn-refresh">
            <i class="fas fa-sync-alt"></i> 刷新
          </button>
        </div>
      </div>
      
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>数据源</th>
              <th>标题</th>
              <th>国家</th>
              <th>类型</th>
              <th>状态</th>
              <th>爬取时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in latestData" :key="item.id">
              <td>
                <span class="source-badge" :class="getSourceClass(item.sourceName)">
                  {{ item.sourceName }}
                </span>
              </td>
              <td class="title-cell">
                <a :href="item.url" target="_blank" class="title-link">
                  {{ item.title }}
                </a>
              </td>
              <td>{{ item.country || '-' }}</td>
              <td>{{ item.type || '-' }}</td>
              <td>
                <span class="status-badge" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
              </td>
              <td>{{ formatDate(item.crawlTime) }}</td>
              <td>
                <div class="action-buttons">
                  <button @click="viewDetails(item)" class="btn-action" title="查看详情">
                    <i class="fas fa-eye"></i>
                  </button>
                  <button @click="markAsProcessed(item.id)" class="btn-action" title="标记已处理">
                    <i class="fas fa-check"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>加载中...</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'Dashboard',
  data() {
    return {
      loading: false,
      statistics: [
        { title: '总数据量', value: 0, change: '+0', changeClass: 'positive', icon: 'fas fa-database', iconClass: 'blue' },
        { title: '今日新增', value: 0, change: '+0', changeClass: 'positive', icon: 'fas fa-plus-circle', iconClass: 'green' },
        { title: '本周新增', value: 0, change: '+0', changeClass: 'positive', icon: 'fas fa-calendar-week', iconClass: 'orange' },
        { title: '本月新增', value: 0, change: '+0', changeClass: 'positive', icon: 'fas fa-calendar-alt', iconClass: 'purple' }
      ],
      sourcePieChart: null,
      statusBarChart: null,
      trendChart: null,
      latestData: []
    };
  },
  mounted() {
    this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        const response = await axios.get('/api/visualization/dashboard');
        const data = response.data;
        
        // 更新统计信息
        if (data.statistics) {
          this.statistics[0].value = data.statistics.totalCount || 0;
          this.statistics[1].value = data.statistics.todayCount || 0;
          this.statistics[2].value = data.statistics.weekCount || 0;
          this.statistics[3].value = data.statistics.monthCount || 0;
        }
        
        // 更新图表
        this.sourcePieChart = data.sourcePieChart;
        this.statusBarChart = data.statusBarChart;
        this.trendChart = data.trendChart;
        
        // 更新最新数据
        this.latestData = data.latestData || [];
        
      } catch (error) {
        console.error('加载仪表板数据失败:', error);
        this.$message.error('加载数据失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    
    async generateSourcePieChart() {
      try {
        const response = await axios.post('/api/visualization/charts/source-pie');
        this.sourcePieChart = response.data.chartPath;
        this.$message.success('数据源分布图生成成功');
      } catch (error) {
        console.error('生成数据源分布图失败:', error);
        this.$message.error('生成图表失败');
      }
    },
    
    async generateStatusBarChart() {
      try {
        const response = await axios.post('/api/visualization/charts/status-bar');
        this.statusBarChart = response.data.chartPath;
        this.$message.success('数据状态分布图生成成功');
      } catch (error) {
        console.error('生成数据状态分布图失败:', error);
        this.$message.error('生成图表失败');
      }
    },
    
    async generateTrendChart() {
      try {
        const response = await axios.post('/api/visualization/charts/trend?days=30');
        this.trendChart = response.data.chartPath;
        this.$message.success('数据趋势图生成成功');
      } catch (error) {
        console.error('生成数据趋势图失败:', error);
        this.$message.error('生成图表失败');
      }
    },
    
    async exportAllData() {
      try {
        const response = await axios.post('/api/visualization/export/all');
        this.$message.success('数据导出成功');
        // 这里可以添加下载文件的逻辑
      } catch (error) {
        console.error('导出数据失败:', error);
        this.$message.error('导出失败');
      }
    },
    
    async refreshData() {
      await this.loadDashboardData();
      this.$message.success('数据刷新成功');
    },
    
    async markAsProcessed(id) {
      try {
        await axios.put(`/api/crawler-data/${id}/mark-processed`);
        this.$message.success('标记成功');
        this.refreshData();
      } catch (error) {
        console.error('标记失败:', error);
        this.$message.error('标记失败');
      }
    },
    
    viewDetails(item) {
      // 这里可以打开详情模态框或跳转到详情页面
      console.log('查看详情:', item);
    },
    
    getSourceClass(sourceName) {
      const classes = {
        'SGS': 'sgs',
        'UL Solutions': 'ul',
        'TÜV Rheinland': 'tuv',
        'China Customs': 'china-customs',
        'US CBP': 'us-cbp',
        'US FDA': 'us-deviceRecallCrawler'
      };
      return classes[sourceName] || 'default';
    },
    
    getStatusClass(status) {
      const classes = {
        'NEW': 'new',
        'PROCESSING': 'processing',
        'PROCESSED': 'processed',
        'ERROR': 'error',
        'DUPLICATE': 'duplicate'
      };
      return classes[status] || 'default';
    },
    
    getStatusText(status) {
      const texts = {
        'NEW': '新建',
        'PROCESSING': '处理中',
        'PROCESSED': '已处理',
        'ERROR': '错误',
        'DUPLICATE': '重复'
      };
      return texts[status] || status;
    },
    
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    }
  }
};
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.green { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.orange { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-icon.purple { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.stat-content {
  flex: 1;
}

.stat-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  margin: 0 0 4px 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.stat-change {
  margin: 0;
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive { color: #10b981; }
.stat-change.negative { color: #ef4444; }

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.btn-refresh {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.btn-refresh:hover {
  background: #f3f4f6;
  color: #374151;
}

.chart-content {
  padding: 24px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.chart-placeholder {
  text-align: center;
  color: #9ca3af;
}

.chart-placeholder i {
  font-size: 48px;
  margin-bottom: 16px;
}

.chart-placeholder p {
  margin: 0;
  font-size: 14px;
}

/* 数据表格 */
.data-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.btn-export, .btn-refresh {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-export {
  background: #3b82f6;
  color: white;
}

.btn-export:hover {
  background: #2563eb;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.data-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.data-table td {
  font-size: 14px;
  color: #1f2937;
}

.title-cell {
  max-width: 300px;
}

.title-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
}

.title-link:hover {
  text-decoration: underline;
}

.source-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.source-badge.sgs { background: #10b981; }
.source-badge.ul { background: #3b82f6; }
.source-badge.tuv { background: #f59e0b; }
.source-badge.china-customs { background: #ef4444; }
.source-badge.us-cbp { background: #8b5cf6; }
.source-badge.us-deviceRecallCrawler { background: #06b6d4; }
.source-badge.default { background: #6b7280; }

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.status-badge.new { background: #3b82f6; }
.status-badge.processing { background: #f59e0b; }
.status-badge.processed { background: #10b981; }
.status-badge.error { background: #ef4444; }
.status-badge.duplicate { background: #6b7280; }
.status-badge.default { background: #9ca3af; }

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-action {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 6px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.btn-action:hover {
  background: #f3f4f6;
  color: #374151;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  text-align: center;
  color: #6b7280;
}

.loading-spinner i {
  font-size: 32px;
  margin-bottom: 16px;
}

.loading-spinner p {
  margin: 0;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>

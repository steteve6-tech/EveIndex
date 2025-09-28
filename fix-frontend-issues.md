# 前端页面加载问题修复指南

## 🚨 问题诊断

### 1. JavaScript警告
```
[Violation] Added non-passive event listener to a scroll-blocking 'mousewheel' event
[Violation] Added non-passive event listener to a scroll-blocking 'wheel' event
```

### 2. 页面加载问题
- Dashboard.vue 和 CrawlerDataManagement.vue 页面内容不显示
- 可能是API调用失败或数据处理超时

## 🔧 修复步骤

### 步骤1：检查浏览器控制台
1. 打开浏览器开发者工具 (F12)
2. 查看 Console 标签页
3. 查看 Network 标签页
4. 记录具体的错误信息

### 步骤2：检查API调用
在浏览器控制台中检查以下API调用是否成功：

#### Dashboard页面相关API：
```javascript
// 检查这些API调用
GET /api/crawler-data/list?size=10000&sortBy=publishDate&sortDirection=desc&page=0&related=true
GET /api/api/daily-country-risk-stats/all-countries-trend?days=7
```

#### CrawlerDataManagement页面相关API：
```javascript
// 检查这些API调用
GET /api/crawler-data/list?page=0&size=10&riskLevel=HIGH
GET /api/risk-level-statistics
```

### 步骤3：临时修复方案

#### 3.1 减少数据量
在Dashboard.vue中，临时减少数据请求量：

```javascript
// 将 size: 10000 改为 size: 100
const allDataResult = await getCrawlerData({ 
  page: 0, 
  size: 100,  // 从10000改为100
  related: true 
})
```

#### 3.2 添加错误处理
在API调用中添加更好的错误处理：

```javascript
try {
  const result = await getCrawlerData(params)
  if (result && result.data) {
    // 处理数据
  } else {
    console.error('API返回数据格式错误:', result)
    message.error('数据加载失败，请稍后重试')
  }
} catch (error) {
  console.error('API调用失败:', error)
  message.error('网络错误，请检查网络连接')
}
```

#### 3.3 优化ECharts配置
在Dashboard.vue中优化ECharts配置：

```javascript
// 添加被动事件监听器配置
echarts.init(chartDom, null, {
  renderer: 'canvas',
  useDirtyRect: false,
  useCoarsePointer: false,
  pointerSize: 0,
  ssr: false,
  width: null,
  height: null
})
```

### 步骤4：检查后端服务状态

#### 4.1 检查后端健康状态
```bash
curl https://your-backend-domain.zeabur.app/api/health
```

#### 4.2 检查具体API端点
```bash
# 测试数据列表API
curl "https://your-backend-domain.zeabur.app/api/crawler-data/list?size=10&page=0"

# 测试统计API
curl "https://your-backend-domain.zeabur.app/api/risk-level-statistics"
```

### 步骤5：前端调试

#### 5.1 添加调试日志
在Vue组件中添加更多调试信息：

```javascript
onMounted(async () => {
  console.log('🚀 Dashboard组件开始加载...')
  try {
    await refresh()
    console.log('✅ Dashboard组件加载完成')
  } catch (error) {
    console.error('❌ Dashboard组件加载失败:', error)
  }
})
```

#### 5.2 检查数据格式
确保API返回的数据格式正确：

```javascript
const result = await getCrawlerData(params)
console.log('API返回结果:', result)
console.log('数据类型:', typeof result)
console.log('数据内容:', result?.data)
```

## 🎯 快速修复建议

### 1. 立即检查
1. 打开浏览器开发者工具
2. 查看Console中的错误信息
3. 查看Network中的API请求状态

### 2. 临时解决方案
如果API调用失败，可以：
1. 减少数据请求量
2. 添加加载状态显示
3. 显示友好的错误信息

### 3. 长期解决方案
1. 优化后端API性能
2. 实现数据分页加载
3. 添加数据缓存机制
4. 优化前端渲染性能

## 📞 需要更多信息

请提供以下信息以便进一步诊断：

1. **浏览器控制台错误信息**
2. **Network标签页中的API请求状态**
3. **具体的页面加载行为描述**
4. **后端服务日志（如果可访问）**

这样我可以提供更精确的解决方案。

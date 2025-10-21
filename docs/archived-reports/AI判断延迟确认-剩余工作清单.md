# AI判断延迟确认系统 - 剩余工作清单

## ✅ 已完成部分 (当前进度: 约40%)

### 后端核心
1. ✅ 数据库迁移脚本 `database/migration_ai_judgment_and_keyword_stats.sql`
2. ✅ 实体类 `AIPendingJudgment.java`
3. ✅ Repository `AIPendingJudgmentRepository.java`
4. ✅ Service `PendingAIJudgmentService.java`
5. ✅ 修改 `AutoAIJudgeService.java` - 改为保存到待审核表
6. ✅ Controller `PendingAIJudgmentController.java` - 完整API接口

### API接口列表
```
GET  /api/ai-judgment/pending?moduleType=DEVICE_DATA                 - 获取待审核列表
GET  /api/ai-judgment/pending/count?moduleType=DEVICE_DATA           - 获取待审核数量
GET  /api/ai-judgment/pending/statistics/device-data                 - 获取设备数据统计
GET  /api/ai-judgment/pending/{id}                                   - 获取判断详情
POST /api/ai-judgment/confirm/{id}                                   - 确认判断
POST /api/ai-judgment/batch-confirm                                  - 批量确认
POST /api/ai-judgment/reject/{id}                                    - 拒绝判断
POST /api/ai-judgment/cleanup-expired                                - 清理过期记录
```

---

## 📋 剩余工作

### Phase 1: 后端补充 (约1-2小时)

#### 1.1 创建NewDataStatisticsService
文件: `spring-boot-backend/src/main/java/com/certification/service/NewDataStatisticsService.java`

```java
@Service
public class NewDataStatisticsService {
    // 获取各实体类型的新增数据数量
    public Map<String, Long> getNewDataCount(String moduleType);

    // 获取新增数据列表
    public List<?> getNewDataList(String entityType, Pageable pageable);

    // 标记为已查看
    public void markDataAsViewed(String entityType, List<Long> ids);
}
```

#### 1.2 创建NewDataController
文件: `spring-boot-backend/src/main/java/com/certification/controller/NewDataController.java`

```java
GET  /api/new-data/count?moduleType=DEVICE_DATA
GET  /api/new-data/list?entityType=Recall&page=0&size=20
POST /api/new-data/mark-viewed
```

#### 1.3 创建定时清理任务
文件: `spring-boot-backend/src/main/java/com/certification/scheduled/AIJudgmentCleanupJob.java`

```java
@Component
public class AIJudgmentCleanupJob {
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredJudgments();

    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void cleanupViewedNewData();
}
```

---

### Phase 2: 前端实现 (约2-3小时)

#### 2.1 创建AI判断预览弹窗
文件: `vue-frontend/src/components/AIJudgmentReviewPopup.vue`

**功能需求:**
- Props: `moduleType` (DEVICE_DATA/CERT_NEWS)
- 显示待审核列表（分页）
- 设备数据模块显示：
  - 被黑名单过滤的数据列表
  - 将被设置为高风险的数据列表
  - 新增黑名单关键词列表
- 医疗认证模块显示：
  - AI判断结果列表（无黑名单信息）
- 操作按钮：
  - 批量确认
  - 批量拒绝
  - 单个确认/拒绝

**技术要点:**
```vue
<template>
  <el-dialog title="AI判断审核" v-model="visible" width="80%">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-statistic title="待审核" :value="pendingCount" />
      </el-col>
      <el-col :span="8" v-if="moduleType === 'DEVICE_DATA'">
        <el-statistic title="黑名单过滤" :value="filteredCount" />
      </el-col>
      <el-col :span="8" v-if="moduleType === 'DEVICE_DATA'">
        <el-statistic title="高风险" :value="highRiskCount" />
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table :data="judgmentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="entityType" label="类型" />
      <el-table-column prop="suggestedRiskLevel" label="风险等级">
        <template #default="scope">
          <el-tag :type="getRiskLevelType(scope.row.suggestedRiskLevel)">
            {{ scope.row.suggestedRiskLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="confirmSingle(scope.row.id)">确认</el-button>
          <el-button size="small" type="danger" @click="rejectSingle(scope.row.id)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 底部操作 -->
    <template #footer>
      <el-button @click="batchConfirm" :disabled="selectedIds.length === 0">
        批量确认 ({{ selectedIds.length }})
      </el-button>
      <el-button type="danger" @click="batchReject" :disabled="selectedIds.length === 0">
        批量拒绝
      </el-button>
    </template>
  </el-dialog>
</template>
```

#### 2.2 修改DeviceData.vue
位置: `vue-frontend/src/views/DeviceData.vue`

**需要添加的内容:**
1. 顶部通知栏
```vue
<el-alert v-if="pendingCount > 0" type="warning" :closable="false">
  <template #title>
    有 {{ pendingCount }} 条AI判断待审核
    <el-button type="text" @click="openReviewPopup">立即审核</el-button>
  </template>
</el-alert>

<el-alert v-if="newDataCount > 0" type="info" :closable="false">
  <template #title>
    有 {{ newDataCount }} 条新增数据
    <el-button type="text" @click="filterNewData">查看</el-button>
  </template>
</el-alert>
```

2. "待审核AI判断"按钮（带徽章）
```vue
<el-button @click="openReviewPopup">
  待审核AI判断
  <el-badge :value="pendingCount" v-if="pendingCount > 0" />
</el-button>
```

3. "新增数据"筛选器
```vue
<el-select v-model="dataFilter">
  <el-option label="全部数据" value="all" />
  <el-option label="新增数据" value="new" />
</el-select>
```

4. 集成弹窗组件
```vue
<AIJudgmentReviewPopup
  v-model="reviewPopupVisible"
  module-type="DEVICE_DATA"
  @confirmed="refreshData"
/>
```

#### 2.3 修改CertNewsTaskManagement.vue
位置: `vue-frontend/src/views/CertNewsTaskManagement.vue`

**类似DeviceData.vue的修改，但:**
- 无黑名单相关显示
- moduleType="CERT_NEWS"

#### 2.4 修改Layout.vue（全局通知）
位置: `vue-frontend/src/components/Layout.vue`

**添加登录后检查:**
```js
async checkPendingData() {
  // 检查设备数据模块待审核
  const devicePending = await api.getPendingCount('DEVICE_DATA');
  // 检查医疗认证模块待审核
  const certPending = await api.getPendingCount('CERT_NEWS');

  // 检查新增数据
  const newData = await api.getNewDataCount('DEVICE_DATA');

  if (devicePending.total > 0 || certPending.total > 0 || newData.total > 0) {
    this.$notify({
      title: '提醒',
      message: `您有 ${devicePending.total + certPending.total} 条AI判断待审核，${newData.total} 条新增数据`,
      type: 'warning',
      duration: 0 // 不自动关闭
    });
  }
}
```

---

### Phase 3: API调用封装 (约30分钟)

#### 3.1 创建API文件
文件: `vue-frontend/src/api/aiJudgment.ts`

```typescript
import request from '@/utils/request'

// 获取待审核列表
export function getPendingJudgments(moduleType: string, status?: string) {
  return request({
    url: '/ai-judgment/pending',
    method: 'get',
    params: { moduleType, status }
  })
}

// 获取待审核数量
export function getPendingCount(moduleType: string) {
  return request({
    url: '/ai-judgment/pending/count',
    method: 'get',
    params: { moduleType }
  })
}

// 获取设备数据统计
export function getDeviceDataStatistics() {
  return request({
    url: '/ai-judgment/pending/statistics/device-data',
    method: 'get'
  })
}

// 确认判断
export function confirmJudgment(id: number, confirmedBy?: string) {
  return request({
    url: `/ai-judgment/confirm/${id}`,
    method: 'post',
    params: { confirmedBy }
  })
}

// 批量确认
export function batchConfirm(ids: number[], confirmedBy?: string) {
  return request({
    url: '/ai-judgment/batch-confirm',
    method: 'post',
    data: { ids, confirmedBy }
  })
}

// 拒绝判断
export function rejectJudgment(id: number, rejectedBy?: string) {
  return request({
    url: `/ai-judgment/reject/${id}`,
    method: 'post',
    params: { rejectedBy }
  })
}
```

---

## 🔧 下一步操作（按优先级）

### 立即执行：
1. **运行数据库迁移** ⚠️ 最重要
   ```bash
   mysql -u root -p common_db < database/migration_ai_judgment_and_keyword_stats.sql
   ```

2. **编译测试后端**
   ```bash
   cd spring-boot-backend
   mvn clean compile
   ```

3. **启动后端测试API**
   ```bash
   mvn spring-boot:run
   ```
   访问: http://localhost:8080/api/doc.html 查看Swagger文档

### 继续开发：
4. 创建 `NewDataStatisticsService` 和 `NewDataController`
5. 创建定时清理任务
6. 创建前端 `AIJudgmentReviewPopup.vue` 组件
7. 修改 `DeviceData.vue` 和 `CertNewsTaskManagement.vue`
8. 测试完整流程

---

## 📊 当前进度

| 模块 | 完成度 |
|------|--------|
| 数据库设计 | 100% |
| 后端实体和Repository | 100% |
| 后端Service | 100% |
| 后端Controller | 100% |
| 定时任务 | 0% |
| 前端组件 | 0% |
| 前端页面集成 | 0% |
| **总体进度** | **约40%** |

---

## ⏱️ 预计剩余时间
- 后端补充（NewDataService + 定时任务）：1-2小时
- 前端实现（组件 + 页面集成）：2-3小时
- 测试和调试：1-2小时
- **总计：4-7小时**

---

## 💡 技术要点提醒

### 前端调用示例
```js
// 在 DeviceData.vue 的 mounted 钩子中
async loadPendingCount() {
  try {
    const res = await getPendingCount('DEVICE_DATA');
    this.pendingCount = res.data.total;
  } catch (error) {
    console.error('获取待审核数量失败', error);
  }
}

// 打开审核弹窗
async openReviewPopup() {
  this.reviewPopupVisible = true;
}

// 确认后刷新数据
async refreshData() {
  await this.loadPendingCount();
  await this.loadTableData();
  this.$message.success('操作成功');
}
```

### 定时任务配置
```yaml
# application.yml
spring:
  task:
    scheduling:
      enabled: true
```

---

如需继续实施，请告知下一步需要完成哪个部分！

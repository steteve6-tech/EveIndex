# HighRiskDataTable.vue 恢复清单

## 需要还原的修改

### 1. 移除操作按钮中的生成竞品信息按钮

**位置**：第138-145行
**还原为**：
```vue
<template v-else-if="column.key === 'action'">
  <a-space>
    <a-button type="link" size="small" @click="handleViewDetail(record)">
      查看详情
    </a-button>
  </a-space>
</template>
```

### 2. 移除生成竞品信息模态框

**位置**：第152-208行
**操作**：完全删除整个模态框代码块

### 3. 移除详情弹窗中的jdCountry特殊显示

**位置**：第218-233行
**还原为**：
```vue
<a-descriptions :column="2" bordered>
  <a-descriptions-item 
    v-for="(value, key) in currentRecord" 
    :key="key" 
    :label="getColumnLabel(key)"
    :span="isWideField(key) ? 2 : 1"
  >
```

### 4. 移除import语句

**位置**：第267行
**移除**：
```javascript
import { generateProductFromHighRiskData, checkProductExists } from '@/api/api/product'
```

### 5. 移除事件定义

**位置**：第282行
**还原为**：
```javascript
const emit = defineEmits<{
  'data-loaded': [dataType: string, data: any[], total: number]
  'keyword-click': [record: any, keyword: string]
}>()
```

### 6. 移除响应式数据

**位置**：第295-312行
**移除**：
- `generateModalVisible`
- `generateLoading` 
- `generateForm`

### 7. 移除所有生成竞品信息相关的方法

**移除以下方法**：
- `shouldShowGenerateButton()` (第615行)
- `handleGenerateCompetitorInfo()` (第619-647行)
- `handleConfirmGenerate()` (第649-675行)
- `getProductName()` (第677-693行)
- `getApplicantName()` (第695-711行)
- `getBrandName()` (第713-729行)
- `getDeviceCode()` (第731-747行)
- `getDeviceClass()` (第749-757行)
- `getDeviceDescription()` (第759-775行)

### 8. 移除jdCountry相关的方法

**移除以下方法**：
- `getCountryDisplayName()` (第962-988行)
- `getCountryColor()` (第990-1008行)
- `getJdCountryValue()` (第1010-1032行)
- `getJdCountryFieldName()` (第1034-1052行)

### 9. 恢复filteredRecord计算属性

**位置**：第467-485行
**还原为**：
```javascript
// 移除filteredRecord，直接使用currentRecord
```

### 10. 恢复handleViewDetail方法

**位置**：第610-644行
**还原为**：
```javascript
const handleViewDetail = (record: any) => {
  currentRecord.value = record
  detailModalVisible.value = true
}
```

## 操作建议

由于修改较多，建议：
1. 备份当前的HighRiskDataTable.vue文件
2. 从git历史中恢复原始版本
3. 或者手动按照上述清单逐项还原

这样可以确保组件恢复到原始的简洁状态，只保留基本的查看详情功能。

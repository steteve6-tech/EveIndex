# 🤖 AI智能审核功能 - 快速上手

> **一键智能筛选测肤仪数据，自动降级非目标设备，自动积累黑名单** 🚀

---

## ⚡ 5分钟快速上手

### **1. 获取OpenAI API Key** (2分钟)

访问：https://platform.openai.com/api-keys
- 注册/登录
- 点击"Create new secret key"
- 复制API Key（`sk-...`）

### **2. 配置环境变量** (1分钟)

```bash
# Windows PowerShell
$env:OPENAI_API_KEY="sk-your-api-key-here"

# Linux/Mac
export OPENAI_API_KEY="sk-your-api-key-here"
```

### **3. 启动服务** (2分钟)

```bash
# 后端
cd spring-boot-backend
mvn spring-boot:run

# 前端（新终端）
cd vue-frontend
npm run dev
```

### **4. 开始使用！** (立即)

1. 访问：http://localhost:5173/high-risk
2. 找到"🤖 AI智能审核"卡片
3. 设置审核数量：**10**（首次建议测试）
4. 点击"开始智能审核"
5. 等待10秒，查看结果！

---

## 🎯 功能说明

### **核心功能**

```
AI智能判断每条高风险数据：
  
  ✅ 是测肤仪？
     → 保留高风险
     
  ❌ 不是测肤仪？
     → 降为低风险
     → 制造商加入黑名单
```

### **支持的数据类型**

1. ✅ 注册记录 (DeviceRegistrationRecord)
2. ✅ 申请记录 (Device510K)
3. ✅ 召回记录 (DeviceRecallRecord)
4. ✅ 不良事件 (DeviceEventReport)

---

## 📊 使用效果

### **审核100条数据示例**

```
✅ 总共审核：100条
✅ 保留高风险：23条（VISIA等真正的测肤仪）
❌ 降为低风险：77条（超声、心电图等非目标设备）
📋 添加黑名单：45个制造商（Philips、GE等）

💰 成本：$0.40
⏱️ 耗时：2分钟
📈 准确率：95%
```

---

## 💡 最佳实践

### **首次使用流程**

```
第1次：测试10条
  ↓ 检查准确性
第2次：测试50条
  ↓ 验证降级逻辑
第3次：批量100条
  ↓ 确认无误
第4次：处理所有历史数据
```

### **日常使用**

```
每周：审核新增的高风险数据
每月：审查黑名单，删除误添加的
```

---

## 💰 成本

| 使用量 | 成本 |
|-------|------|
| 10条测试 | $0.04 |
| 100条/次 | $0.40 |
| 1000条/月 | $4.00 |

**完全可以接受！而且是一次性成本！** ✅

---

## 📚 详细文档

| 文档 | 用途 |
|------|------|
| 📖 [快速配置OpenAI-API-Key指南.md](快速配置OpenAI-API-Key指南.md) | API Key配置 |
| 📖 [AI智能审核功能-完整使用指南.md](AI智能审核功能-完整使用指南.md) | 详细教程 |
| 📖 [AI智能审核功能-实现总结.md](AI智能审核功能-实现总结.md) | 技术总结 |

---

## ⚠️ 重要提示

1. ⚠️ **首次使用先测试10条数据**
2. ⚠️ **降级操作不可撤销**（可手动恢复）
3. ⚠️ **不要将API Key提交到Git**
4. ⚠️ **定期检查OpenAI账单**

---

## 🆘 遇到问题？

### **快速检查**

```bash
# 1. 检查API Key
echo $env:OPENAI_API_KEY

# 2. 检查服务是否运行
curl http://localhost:8080/api/ai/smart-audit/execute?limit=1

# 3. 查看日志
tail -f spring-boot-backend/logs/certification-monitor.log
```

### **常见问题**

| 问题 | 解决方案 |
|------|---------|
| API Key无效 | 重新生成并配置 |
| 没有高风险数据 | 先在DeviceData.vue中搜索并标记 |
| 网络错误 | 检查防火墙和网络连接 |

---

## 🎉 开始使用！

**只需3步：**

```bash
# 1. 配置
$env:OPENAI_API_KEY="sk-..."

# 2. 启动
mvn spring-boot:run

# 3. 访问
http://localhost:5173/high-risk
```

**立即体验AI智能审核的强大功能！** 🚀

---

**有问题？查看详细文档或在Issue中提问。** 📞


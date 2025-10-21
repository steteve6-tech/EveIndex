# 🔒 GitHub 上传前安全检查清单

## ⚠️ 已修复的安全问题

### 1. ✅ 移除硬编码的 API 密钥

**问题**: `application.yml` 中硬编码了以下敏感信息：
- ❌ 火山引擎翻译 Access Key: `AKLT***********` (已撤销)
- ❌ 火山引擎翻译 Secret Key: `Tm1G***********` (已撤销)
- ❌ OpenAI API Key: `sk-***********` (已撤销)
- ❌ 火山引擎 AI API Key: `a3e5***********` (已撤销)
- ❌ MySQL 密码: `****` (已修改)
- ❌ Druid 密码: `****` (已修改)

**已修复**: 所有敏感信息已改为从环境变量读取

### 2. ✅ 停止跟踪敏感文件

- `.env.prod` 已从 Git 仓库中移除（但保留在本地）
- `.env.dev` 不会被上传
- 更新了 `.gitignore` 确保所有环境配置文件被忽略

---

## 📋 上传前检查清单

### ✅ 可以安全上传的文件

```bash
# 配置文件模板（不含真实密钥）
.env.prod.example          # ✅ 仅包含示例配置
.env.prod.minimal          # ✅ 仅包含占位符密码

# 文档文件
README.md                  # ✅ 项目说明
DEPLOYMENT_README.md       # ✅ 部署指南
DEPLOYMENT_CHECKLIST.md    # ✅ 部署检查清单
PODMAN_DEPLOYMENT_GUIDE.md # ✅ Podman 部署指南
SYSTEM_MAINTENANCE_GUIDE.md # ✅ 系统维护指南
SECURITY_CHECKLIST.md      # ✅ 安全检查清单（本文件）

# 部署脚本
deploy-quick-start.sh      # ✅ Linux/Mac 部署脚本
deploy-quick-start.bat     # ✅ Windows 部署脚本
docker-compose.prod.yml    # ✅ Docker Compose 配置

# 数据库
database/init_database_full.sql  # ✅ 数据库初始化脚本
database/README.md         # ✅ 数据库文档

# 源代码
spring-boot-backend/**/*.java   # ✅ 后端源码
vue-frontend/src/**/*      # ✅ 前端源码
```

### ❌ 绝对不能上传的文件

```bash
# 环境配置文件（包含真实密钥）
.env.prod                  # ❌ 包含真实生产环境密钥
.env.dev                   # ❌ 包含开发环境配置
.env                       # ❌ 本地环境配置

# 临时文件
null                       # ❌ 临时文件/错误产物
*.log                      # ❌ 日志文件
```

---

## 🔍 检查方法

### 方法1: 使用 Git 检查即将提交的文件

```bash
# 查看即将提交的文件
git status

# 查看具体修改内容（检查是否有密钥）
git diff

# 搜索可能的 API 密钥
git grep -i "api.key\|secret\|password" -- ':!*.md' ':!.gitignore'
```

### 方法2: 搜索敏感关键词

```bash
# Windows (PowerShell)
findstr /S /I "sk-.*API.*KEY.*secret.*password" *.yml *.properties *.java

# Linux/Mac
grep -r -i -E "(sk-[a-zA-Z0-9]{20,}|api.key|secret.key|password)" --include="*.yml" --include="*.properties" --include="*.java" --exclude-dir=node_modules
```

### 方法3: 使用工具自动检测

```bash
# 安装 git-secrets（推荐）
git secrets --scan

# 或使用 truffleHog
trufflehog git file://. --since-commit HEAD
```

---

## 🛡️ 已实施的安全措施

### 1. 环境变量配置

所有敏感配置已改为从环境变量读取：

**application.yml** (已修复):
```yaml
# ✅ 安全：从环境变量读取
openai:
  api:
    key: ${OPENAI_API_KEY:}  # 无默认值，必须配置

volcengine:
  translate:
    access-key: ${VOLCENGINE_ACCESS_KEY:}
    secret-key: ${VOLCENGINE_SECRET_KEY:}

spring:
  datasource:
    username: ${DATABASE_USERNAME:root}
    password: ${DATABASE_PASSWORD:dev123}  # 默认值仅用于开发
```

### 2. .gitignore 配置

已更新 `.gitignore` 确保敏感文件不被上传：

```gitignore
# 环境变量文件
.env
.env.*
!.env.prod.example      # 例外：允许示例文件
!.env.prod.minimal      # 例外：允许最小配置模板
.env.prod               # 明确忽略生产配置
.env.dev                # 明确忽略开发配置

# API密钥文件
**/secret.properties
**/secrets.yml
**/*secret*
**/*key*
!**/DeviceMatchKeywords.java  # 例外：业务关键词类
```

### 3. 双重验证

配置文件提供了两种验证方式：
- ✅ `.env.prod.example` - 完整配置示例（含所有选项）
- ✅ `.env.prod.minimal` - 最小化配置（仅必需项）

---

## 🚨 如果密钥已泄露怎么办？

### 立即行动步骤：

1. **撤销已泄露的密钥**
   ```bash
   # OpenAI
   # 访问 https://platform.openai.com/api-keys
   # 立即删除泄露的 API Key

   # 火山引擎
   # 访问火山引擎控制台
   # 删除并重新生成 Access Key
   ```

2. **从 Git 历史中移除敏感信息**
   ```bash
   # 使用 BFG Repo-Cleaner（推荐）
   bfg --replace-text passwords.txt

   # 或使用 git filter-branch（手动）
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch .env.prod" \
     --prune-empty --tag-name-filter cat -- --all

   # 强制推送（危险操作！）
   git push origin --force --all
   ```

3. **生成新密钥**
   - OpenAI: 生成新的 API Key
   - 火山引擎: 生成新的 Access Key 和 Secret Key
   - MySQL: 修改数据库密码

4. **更新本地配置**
   ```bash
   # 编辑 .env.prod
   vim .env.prod

   # 填入新密钥
   OPENAI_API_KEY=sk-new-key-here
   VOLCENGINE_ACCESS_KEY=new-access-key
   VOLCENGINE_SECRET_KEY=new-secret-key
   ```

---

## ✅ 最终上传前检查

在推送到 GitHub 之前，执行以下命令确认：

```bash
# 1. 确认 .env.prod 不在暂存区
git status | grep ".env.prod"
# 应该输出：nothing 或为空

# 2. 确认没有硬编码的密钥
git grep -i "sk-" -- '*.yml' '*.properties' '*.java'
# 应该无结果或只在注释中

# 3. 确认 .gitignore 生效
git check-ignore -v .env.prod
# 应该输出：.gitignore:xx:.env.prod

# 4. 查看即将推送的内容
git diff main..HEAD

# 5. 最后确认
echo "✅ 安全检查通过，可以推送"
```

---

## 📞 额外建议

### 1. 使用环境变量管理工具
- **开发环境**: 使用 `direnv` 或 `.env` 文件
- **生产环境**: 使用 Docker secrets、Kubernetes ConfigMap/Secrets

### 2. 配置 GitHub 安全扫描
在 GitHub 仓库设置中启用：
- Secret scanning（密钥扫描）
- Dependabot alerts（依赖漏洞告警）

### 3. 定期审计
```bash
# 每月检查一次 Git 历史
git log --all --full-history --source -- '**/*.env*'

# 检查是否有敏感文件被意外提交
git log --all --full-history --source -- '**/application*.yml' | grep -i "password\|key"
```

---

## 📊 安全状态

| 检查项 | 状态 | 备注 |
|--------|------|------|
| API 密钥使用环境变量 | ✅ 已修复 | application.yml 已更新 |
| .env.prod 被 .gitignore | ✅ 已配置 | 已从 Git 移除 |
| .env.dev 被 .gitignore | ✅ 已配置 | 已从 Git 移除 |
| 提供配置模板 | ✅ 已完成 | .env.prod.example, .env.prod.minimal |
| 文档说明安全配置 | ✅ 已完成 | 本文档 |
| 密码使用强密码 | ⚠️ 待确认 | 请在 .env.prod 中使用强密码 |

---

**检查清单版本**: v1.0.0
**最后更新**: 2025-01-20
**检查人员**: _______________
**检查日期**: _______________
**确认状态**: ☐ 通过  ☐ 需修改

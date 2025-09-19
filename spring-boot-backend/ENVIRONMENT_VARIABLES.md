# 环境变量配置说明

本项目需要配置以下环境变量才能正常运行：

## 必需的环境变量

### 火山引擎翻译服务
```bash
# Linux/Mac
export VOLCENGINE_ACCESS_KEY="你的火山引擎访问密钥ID"
export VOLCENGINE_SECRET_KEY="你的火山引擎访问密钥"

# Windows PowerShell
$env:VOLCENGINE_ACCESS_KEY="你的火山引擎访问密钥ID"
$env:VOLCENGINE_SECRET_KEY="你的火山引擎访问密钥"

# Windows CMD
set VOLCENGINE_ACCESS_KEY=你的火山引擎访问密钥ID
set VOLCENGINE_SECRET_KEY=你的火山引擎访问密钥
```

### 其他服务（可选）
```bash
# ARK API密钥（如果使用相关功能）
export ARK_API_KEY="你的ARK_API密钥"
```

## 配置方式

### 1. 系统环境变量
在系统中设置环境变量，重启应用即可生效。

### 2. IDE环境变量
在IDE（如IntelliJ IDEA）的运行配置中添加环境变量。

### 3. Docker环境变量
在docker-compose.yml中配置：
```yaml
services:
  app:
    environment:
      - VOLCENGINE_ACCESS_KEY=your_key_here
      - VOLCENGINE_SECRET_KEY=your_secret_here
```

### 4. 生产环境
在生产服务器上设置环境变量，确保应用启动前已加载。

## 安全注意事项

⚠️ **重要提醒**：
- 绝不要将真实的API密钥提交到代码仓库中
- 使用环境变量或安全的密钥管理服务
- 定期轮换API密钥
- 限制API密钥的访问权限

# Tesseract OCR Training Data

## 📥 下载训练数据

请下载以下训练数据文件并放置到此目录：

### 英文训练数据（必需）
下载 `eng.traineddata` 文件：

**方式1: 直接下载（推荐）**
```
https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
```

**方式2: 使用命令下载**
```bash
# Windows (PowerShell)
Invoke-WebRequest -Uri "https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata" -OutFile "eng.traineddata"

# Linux/Mac
wget https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
# 或
curl -L -o eng.traineddata https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
```

### 其他可选训练数据

**繁体中文（台湾）**
```
https://github.com/tesseract-ocr/tessdata/raw/main/chi_tra.traineddata
```

**简体中文**
```
https://github.com/tesseract-ocr/tessdata/raw/main/chi_sim.traineddata
```

## 📂 文件结构

下载完成后，此目录应包含：
```
tessdata/
├── README.md           # 本说明文件
├── eng.traineddata     # 英文训练数据（必需）
├── chi_tra.traineddata # 繁体中文（可选）
└── chi_sim.traineddata # 简体中文（可选）
```

## ✅ 验证安装

下载完成后，重启应用并访问测试接口：
```bash
curl http://localhost:8080/api/tw/captcha/test
```

如果看到 "OCR识别成功" 日志，说明配置成功！

## 🔍 故障排查

### 问题1: 找不到 tessdata

**错误信息**：
```
未找到tessdata训练数据，OCR识别可能失败
```

**解决方法**：
1. 确认 `eng.traineddata` 文件已下载到此目录
2. 检查文件权限
3. 重启应用

### 问题2: OCR识别准确率低

**解决方法**：
1. 查看 `logs/captcha/` 中的验证码图片
2. 如果验证码复杂，考虑使用第三方识别服务
3. 调整图像预处理参数（在 `TwCaptchaService.java` 中）

## 📚 更多信息

- Tesseract OCR 官网: https://github.com/tesseract-ocr/tesseract
- Tess4J (Java包装): https://github.com/nguyenq/tess4j
- 训练数据下载: https://github.com/tesseract-ocr/tessdata

---

**最后更新**: 2025-01-20

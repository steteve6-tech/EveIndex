// Zeabur 构建脚本
// 这个脚本用于在Zeabur环境中构建前端应用

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('🚀 开始Zeabur构建流程...');

// 设置环境变量
process.env.NODE_ENV = 'production';
process.env.VITE_API_BASE_URL = process.env.VITE_API_BASE_URL || 'https://your-backend-domain.zeabur.app/api';

console.log('📝 环境变量配置:');
console.log(`  NODE_ENV: ${process.env.NODE_ENV}`);
console.log(`  VITE_API_BASE_URL: ${process.env.VITE_API_BASE_URL}`);

try {
  // 安装依赖
  console.log('📦 安装依赖...');
  execSync('npm ci --only=production --silent', { stdio: 'inherit' });

  // 构建应用
  console.log('🔨 构建应用...');
  execSync('npm run build', { stdio: 'inherit' });

  // 检查构建结果
  const distPath = path.join(__dirname, 'dist');
  if (fs.existsSync(distPath)) {
    console.log('✅ 构建成功！');
    console.log(`📁 构建文件位于: ${distPath}`);
  } else {
    throw new Error('构建失败：dist目录不存在');
  }

} catch (error) {
  console.error('❌ 构建失败:', error.message);
  process.exit(1);
}

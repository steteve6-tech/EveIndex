import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3100,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '/api'), // 保持路径不变
      },
      '/country-risk-statistics': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/country-risk-statistics/, '/api/country-risk-statistics'),
      },
      '/crawler-data': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/crawler-data/, '/api/crawler-data'),
      },
      '/competitor-info': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/competitor-info/, '/api/competitor-info'),
      },
      '/device-data/': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/device-data/, '/api/device-data'),
      },
      '/api/device-data': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api\/device-data/, '/api/device-data'),
      },
      '/crawler': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/crawler/, '/api/crawler'),
      },
      '/unified-crawler': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/unified-crawler/, '/api/unified-crawler'),
      },
      '/cert-keywords': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/cert-keywords/, '/api/cert-keywords'),
      },
      '/keywords': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/keywords/, '/api/keywords'),
      },
      '/high-risk-data': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/high-risk-data/, '/api/high-risk-data'),
      },
      '/products': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/products/, '/api/products'),
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
})

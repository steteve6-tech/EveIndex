import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
// import Standards from '@/views/Standards.vue' // 文件不存在
import Settings from '@/views/Settings.vue'
// import RiskManagement from '@/views/RiskManagement.vue' // 文件不存在
// import RiskMonitor from '@/views/RiskMonitor.vue' // 文件不存在
import DataQuery from '@/views/DataQuery.vue'
import CrawlerManagement from '@/views/CrawlerManagement.vue'
import CrawlerDataManagement from '@/views/CrawlerDataManagement.vue'
import DeviceData from '@/views/DeviceData.vue'
import UnifiedCrawlerManagement from '@/views/UnifiedCrawlerManagement.vue'
import UnifiedCrawlerManagementNew from '@/views/UnifiedCrawlerManagementNew.vue'
// import CrawlerTest from '@/views/CrawlerTest.vue' // 文件不存在
// import UnifiedCrawlerTest from '@/views/UnifiedCrawlerTest.vue' // 文件不存在
import HighRiskDataManagement from '@/views/HighRiskDataManagement.vue'
import CertNewsTaskManagement from '@/views/CertNewsTaskManagement.vue'
// import KeywordManagement from '@/views/KeywordManagement.vue' // 文件不存在
// import CompetitorInfo from '@/views/CompetitorInfo.vue' // 文件不存在

const routes = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '监控总览' }
      },
      // {
      //   path: '/standards',
      //   name: 'Standards',
      //   component: Standards,
      //   meta: { title: '标准管理' }
      // }, // 文件不存在
      {
        path: '/settings',
        name: 'Settings',
        component: Settings,
        meta: { title: '系统设置' }
      },
      // {
      //   path: '/risk',
      //   name: 'RiskManagement',
      //   component: RiskManagement,
      //   meta: { title: '风险管理' }
      // }, // 文件不存在
      // {
      //   path: '/risk-monitor',
      //   name: 'RiskMonitor',
      //   component: RiskMonitor,
      //   meta: { title: '风险监控' }
      // }, // 文件不存在
      {
        path: '/query',
        name: 'DataQuery',
        component: DataQuery,
        meta: { title: '数据查询' }
      },
      {
        path: '/crawler-data-management',
        name: 'CrawlerDataManagement',
        component: CrawlerDataManagement,
        meta: { title: '相关数据管理' }
      },
      {
        path: '/crawler-management',
        name: 'CrawlerManagement',
        component: CrawlerManagement,
        meta: { title: '爬虫管理' }
      },
      {
        path: '/device-data',
        name: 'DeviceData',
        component: DeviceData,
        meta: { title: '设备数据管理' }
      },
      // {
      //   path: '/crawler-test',
      //   name: 'CrawlerTest',
      //   component: CrawlerTest,
      //   meta: { title: '爬虫管理测试' }
      // }, // 文件不存在
      // {
      //   path: '/unified-crawler-test',
      //   name: 'UnifiedCrawlerTest',
      //   component: UnifiedCrawlerTest,
      //   meta: { title: '统一爬虫测试' }
      // }, // 文件不存在
      {
        path: '/high-risk-data-management',
        name: 'HighRiskDataManagement',
        component: HighRiskDataManagement,
        meta: { title: '高风险数据管理' }
      },
      {
        path: '/unified-crawler-management',
        name: 'UnifiedCrawlerManagement',
        component: UnifiedCrawlerManagement,
        meta: { title: '爬虫调度管理平台' }
      },
      {
        path: '/unified-crawler-new',
        name: 'UnifiedCrawlerManagementNew',
        component: UnifiedCrawlerManagementNew,
        meta: { title: '统一爬虫管理平台（新版）' }
      },
      {
        path: '/cert-news-task',
        name: 'CertNewsTaskManagement',
        component: CertNewsTaskManagement,
        meta: { title: '认证新闻定时任务管理' }
      },
      // {
      //   path: '/keyword-management',
      //   name: 'KeywordManagement',
      //   component: KeywordManagement,
      //   meta: { title: '关键词管理' }
      // }, // 文件不存在
      // {
      //   path: '/competitor-info',
      //   name: 'CompetitorInfo',
      //   component: CompetitorInfo,
      //   meta: { title: '竞品信息' }
      // } // 文件不存在
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router

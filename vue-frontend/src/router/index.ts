import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import Standards from '@/views/Standards.vue'
import Settings from '@/views/Settings.vue'
import RiskManagement from '@/views/RiskManagement.vue'
import RiskMonitor from '@/views/RiskMonitor.vue'
import DataQuery from '@/views/DataQuery.vue'
import CrawlerManagement from '@/views/CrawlerManagement.vue'
import CrawlerDataManagement from '@/views/CrawlerDataManagement.vue'
import DeviceData from '@/views/DeviceData.vue'
import CrawlerTest from '@/views/CrawlerTest.vue'
import UnifiedCrawlerTest from '@/views/UnifiedCrawlerTest.vue'
import HighRiskDataManagement from '@/views/HighRiskDataManagement.vue'
import KeywordManagement from '@/views/KeywordManagement.vue'
import CompetitorInfo from '@/views/CompetitorInfo.vue'

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
      {
        path: '/standards',
        name: 'Standards',
        component: Standards,
        meta: { title: '标准管理' }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: Settings,
        meta: { title: '系统设置' }
      },
      {
        path: '/risk',
        name: 'RiskManagement',
        component: RiskManagement,
        meta: { title: '风险管理' }
      },
      {
        path: '/risk-monitor',
        name: 'RiskMonitor',
        component: RiskMonitor,
        meta: { title: '风险监控' }
      },
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
      {
        path: '/crawler-test',
        name: 'CrawlerTest',
        component: CrawlerTest,
        meta: { title: '爬虫管理测试' }
      },
      {
        path: '/unified-crawler-test',
        name: 'UnifiedCrawlerTest',
        component: UnifiedCrawlerTest,
        meta: { title: '统一爬虫测试' }
      },
      {
        path: '/high-risk-data-management',
        name: 'HighRiskDataManagement',
        component: HighRiskDataManagement,
        meta: { title: '高风险数据管理' }
      },
      {
        path: '/keyword-management',
        name: 'KeywordManagement',
        component: KeywordManagement,
        meta: { title: '关键词管理' }
      },
      {
        path: '/competitor-info',
        name: 'CompetitorInfo',
        component: CompetitorInfo,
        meta: { title: '竞品信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router

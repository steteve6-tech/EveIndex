<template>
  <a-layout class="layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      class="sidebar"
    >
      <div class="logo">
        <h2 v-if="!collapsed">认证监控</h2>
        <h2 v-else>CM</h2>
      </div>
      
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        @click="handleMenuClick"
      >



        <!-- 认证管理模块 -->
        <a-sub-menu key="certification-management">
          <template #icon>
            <SafetyOutlined />
          </template>
          <template #title>认证风险管理</template>

          <a-menu-item key="/">
            <template #icon>
              <DashboardOutlined />
            </template>
            <span>地区风险监控</span>
          </a-menu-item>

          

          <a-menu-item key="/crawler-data-management">
            <template #icon>
              <DatabaseOutlined />
            </template>
            <span>相关数据管理</span>
          </a-menu-item>

          <a-menu-item key="/query">
            <template #icon>
              <SearchOutlined />
            </template>
            <span>数据处理</span>
          </a-menu-item>

<!--          <a-menu-item key="/standards">-->
<!--            <template #icon>-->
<!--              <FileTextOutlined />-->
<!--            </template>-->
<!--            <span>标准管理</span>-->
<!--          </a-menu-item>-->



<!--          <a-menu-item key="/upcoming">-->
<!--            <template #icon>-->
<!--              <CalendarOutlined />-->
<!--            </template>-->
<!--            <span>即将生效</span>-->
<!--          </a-menu-item>-->

<!--          <a-menu-item key="/tasks">-->
<!--            <template #icon>-->
<!--              <CheckSquareOutlined />-->
<!--            </template>-->
<!--            <span>任务跟踪</span>-->
<!--          </a-menu-item>-->

<!--          <a-menu-item key="/alerts">-->
<!--            <template #icon>-->
<!--              <AlertOutlined />-->
<!--            </template>-->
<!--            <span>风险警报</span>-->
<!--          </a-menu-item>-->

        </a-sub-menu>
        
        <!-- 医疗认证风险管理模块 -->
        <a-sub-menu key="medical-risk-management">
          <template #icon>
            <ExperimentOutlined />
          </template>
          <template #title>医疗认证风险</template>
<!--          <a-menu-item key="/risk-monitor">-->
<!--            <template #icon>-->
<!--              <MonitorOutlined />-->
<!--            </template>-->
<!--            <span>竞品数据风险监控</span>-->
<!--          </a-menu-item>-->

          <a-menu-item key="/high-risk-data-management">
            <template #icon>
              <WarningOutlined />
            </template>
            <span>高风险数据</span>
          </a-menu-item>

          <a-menu-item key="/device-data">
            <template #icon>
              <ExperimentOutlined />
            </template>
            <span>数据分析</span>
          </a-menu-item>

<!--          <a-menu-item key="/competitor-info">-->
<!--            <template #icon>-->
<!--              <TrophyOutlined />-->
<!--            </template>-->
<!--            <span>竞品信息</span>-->
<!--          </a-menu-item>-->

          <a-menu-item key="/crawler-management">
            <template #icon>
              <BugOutlined />
            </template>
            <span>爬虫管理</span>
          </a-menu-item>

<!--          <a-menu-item key="/keyword-management">-->
<!--            <template #icon>-->
<!--              <SearchOutlined />-->
<!--            </template>-->
<!--            <span>关键词管理</span>-->
<!--          </a-menu-item>-->

          
<!--          <a-menu-item key="/api-test">-->
<!--            <template #icon>-->
<!--              <BugOutlined />-->
<!--            </template>-->
<!--            <span>API测试</span>-->
<!--          </a-menu-item>-->
        </a-sub-menu>
        

        
        <a-menu-item key="/settings">
          <template #icon>
            <SettingOutlined />
          </template>
          <span>系统设置</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <!-- 主内容区 -->
    <a-layout>
      <!-- 顶部导航 -->
      <a-layout-header :class="['header', { collapsed: collapsed }]">
        <div class="header-left">
          <a-button
            type="text"
            @click="toggleCollapsed"
            class="trigger"
          >
            <MenuUnfoldOutlined v-if="collapsed" />
            <MenuFoldOutlined v-else />
          </a-button>
          
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>认证监控系统</a-breadcrumb-item>
            <a-breadcrumb-item v-if="isCertificationManagementPage">认证管理</a-breadcrumb-item>
            <a-breadcrumb-item v-if="isMedicalRiskManagementPage">竞品医疗认证风险管理</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div class="header-right">
          <a-space>
<!--            <a-button-->
<!--              type="primary"-->
<!--              @click="refreshData"-->
<!--              :loading="refreshing"-->
<!--            >-->
<!--              <template #icon>-->
<!--                <ReloadOutlined />-->
<!--              </template>-->
<!--              刷新数据-->
<!--            </a-button>-->
            
            <a-dropdown>
              <a-button type="text">
                <template #icon>
                  <UserOutlined />
                </template>
                管理员
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile">
                    <UserOutlined />
                    个人资料
                  </a-menu-item>
                  <a-menu-item key="logout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </a-layout-header>
      
      <!-- 内容区域 -->
      <a-layout-content :class="['content', { collapsed: collapsed }]">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined,
  FileTextOutlined,
  CalendarOutlined,
  DatabaseOutlined,
  AlertOutlined,
  SafetyOutlined,
  MonitorOutlined,
  CheckSquareOutlined,
  SearchOutlined,
  SettingOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  ReloadOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined,
  BugOutlined,
  ExperimentOutlined,
  TrophyOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const collapsed = ref(false)
const selectedKeys = ref<string[]>(['/'])
const openKeys = ref<string[]>(['certification-management'])
const refreshing = ref(false)

// 计算属性
const currentPageTitle = computed(() => {
  return route.meta?.title || '监控总览'
})

const isCertificationManagementPage = computed(() => {
  return ['/standards', '/upcoming', '/tasks', '/alerts', '/query', '/crawler-data-management'].includes(route.path)
})

const isMedicalRiskManagementPage = computed(() => {
  return ['/crawler-management', '/device-data', '/competitor-info', '/high-risk-data-management', '/api-test'].includes(route.path)
})

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
    
    // 如果当前路径是认证管理下的子页面，确保父菜单展开
    if (['/standards', '/upcoming', '/tasks', '/alerts', '/query', '/crawler-data-management'].includes(newPath)) {
      if (!openKeys.value.includes('certification-management')) {
        openKeys.value.push('certification-management')
      }
    }
    
    // 如果当前路径是医疗认证风险管理下的子页面，确保父菜单展开
    if (['/crawler-management', '/device-data', '/competitor-info', '/high-risk-data-management', '/api-test'].includes(newPath)) {
      if (!openKeys.value.includes('medical-risk-management')) {
        openKeys.value.push('medical-risk-management')
      }
    }
  },
  { immediate: true }
)

// 方法
const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}

const handleMenuClick = ({ key }: { key: string }) => {
  // 如果是子菜单的key，则进行路由跳转
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const refreshData = async () => {
  refreshing.value = true
  try {
    // 这里可以调用全局的数据刷新方法
    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据刷新失败')
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.sidebar {
  background: #001529;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  overflow-y: auto;
  overflow-x: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  border-bottom: 1px solid #303030;
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
}

.header {
  background: white;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: fixed;
  top: 0;
  right: 0;
  left: 256px; /* 侧边栏展开时的宽度 */
  z-index: 999;
  transition: left 0.2s;
  height: 64px; /* 明确设置高度 */
}

.header.collapsed {
  left: 80px; /* 折叠时的侧边栏宽度 */
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger {
  font-size: 18px;
  margin-right: 16px;
  padding: 0 12px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.content {
  margin: 24px;
  background: transparent;
  margin-left: 280px; /* 侧边栏宽度 + 间距 */
  margin-top: 88px; /* 顶部导航栏高度 + 间距，避免被遮挡 */
  transition: margin-left 0.2s;
}

.content.collapsed {
  margin-left: 104px; /* 折叠时的侧边栏宽度 + 间距 */
  margin-top: 88px; /* 保持顶部边距 */
}

.content-wrapper {
  background: white;
  padding: 24px;
  border-radius: 6px;
  min-height: calc(100vh - 112px);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
  }
  
  .header {
    left: 0;
    padding: 0 16px;
  }
  
  .content {
    margin: 16px;
    margin-left: 16px;
    margin-top: 80px; /* 移动端顶部边距 */
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .breadcrumb {
    display: none;
  }
}

/* 确保侧边栏内容可以滚动 */
.sidebar :deep(.ant-layout-sider-children) {
  overflow-y: auto;
  overflow-x: hidden;
}

/* 自定义滚动条样式 */
.sidebar :deep(.ant-layout-sider-children)::-webkit-scrollbar {
  width: 6px;
}

.sidebar :deep(.ant-layout-sider-children)::-webkit-scrollbar-track {
  background: #001529;
}

.sidebar :deep(.ant-layout-sider-children)::-webkit-scrollbar-thumb {
  background: #434343;
  border-radius: 3px;
}

.sidebar :deep(.ant-layout-sider-children)::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>

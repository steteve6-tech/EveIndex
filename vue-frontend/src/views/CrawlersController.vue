<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>各国认证数据爬虫管理系统</title>
  <!-- 引入外部资源 -->
  <script src="https://cdn.tailwindcss.com"></script>
  <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.8/dist/chart.umd.min.js"></script>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

  <!-- Tailwind 配置 -->
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: {
            primary: '#2563EB', // 主色调：蓝色，代表专业和信任
            secondary: '#10B981', // 辅助色：绿色，代表成功和进度
            danger: '#EF4444', // 危险色：红色
            warning: '#F59E0B', // 警告色：橙色
            info: '#3B82F6', // 信息色：亮蓝色
            dark: '#1E293B',
            light: '#F8FAFC',
          },
          fontFamily: {
            inter: ['Inter', 'sans-serif'],
          },
        },
      }
    }
  </script>

  <!-- 自定义工具类 -->
  <style type="text/tailwindcss">
    @layer utilities {
      .content-auto {
        content-visibility: auto;
      }
      .transition-height {
        transition: height 0.3s ease;
      }
      .scrollbar-thin {
        scrollbar-width: thin;
      }
      .card-hover {
        @apply hover:shadow-md transition-shadow duration-200 hover:-translate-y-1 transition-transform duration-200;
      }
      .flag-icon {
        @apply w-6 h-4 bg-cover bg-center rounded-sm inline-block mr-2;
      }
    }
  </style>
</head>

<body class="font-inter bg-gray-50 text-dark min-h-screen flex flex-col">
<!-- 顶部导航栏 -->
<header class="bg-white shadow-sm z-30 sticky top-0">
  <div class="container mx-auto px-4 py-3 flex items-center justify-between">
    <div class="flex items-center space-x-3">
      <i class="fa fa-globe text-primary text-2xl"></i>
      <h1 class="text-xl font-bold">认证数据爬虫管理系统</h1>
    </div>

    <div class="hidden md:flex items-center space-x-6">
      <a href="#" class="text-gray-600 hover:text-primary transition-colors duration-200 flex items-center">
        <i class="fa fa-tachometer mr-2"></i> 仪表盘
      </a>
      <a href="#" class="text-primary font-medium flex items-center border-b-2 border-primary pb-1">
        <i class="fa fa-cogs mr-2"></i> 爬虫管理
      </a>
      <a href="#" class="text-gray-600 hover:text-primary transition-colors duration-200 flex items-center">
        <i class="fa fa-database mr-2"></i> 数据管理
      </a>
      <a href="#" class="text-gray-600 hover:text-primary transition-colors duration-200 flex items-center">
        <i class="fa fa-line-chart mr-2"></i> 统计分析
      </a>
    </div>

    <div class="flex items-center space-x-4">
      <button class="relative text-gray-600 hover:text-primary transition-colors">
        <i class="fa fa-bell text-xl"></i>
        <span class="absolute -top-1 -right-1 w-4 h-4 bg-danger rounded-full text-white text-xs flex items-center justify-center">3</span>
      </button>
      <div class="relative">
        <img src="https://picsum.photos/id/1005/200" alt="用户头像" class="h-9 w-9 rounded-full object-cover border-2 border-white shadow-sm">
      </div>
      <button class="md:hidden text-gray-600">
        <i class="fa fa-bars text-xl"></i>
      </button>
    </div>
  </div>
</header>

<!-- 主要内容区域 -->
<main class="flex-1 flex flex-col md:flex-row overflow-hidden">
  <!-- 左侧国家导航 -->
  <aside class="w-full md:w-64 bg-white shadow-sm border-r border-gray-200 flex-shrink-0 overflow-hidden flex flex-col">
    <div class="p-3 border-b border-gray-200">
      <div class="relative">
        <input type="text" placeholder="搜索国家..." class="w-full pl-9 pr-3 py-2 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all text-sm">
        <i class="fa fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
      </div>
    </div>

    <div class="overflow-y-auto flex-1 scrollbar-thin p-2">
      <div class="mb-6">
        <h3 class="text-xs font-semibold text-gray-500 uppercase tracking-wider px-3 mb-2">地区分组</h3>
        <ul class="space-y-1">
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md bg-primary/10 text-primary text-sm font-medium flex items-center justify-between">
              全部国家
              <span class="bg-primary/20 text-primary text-xs px-2 py-0.5 rounded-full">24</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              亚洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">8</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              欧洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">6</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              北美洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">3</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              南美洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">2</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              非洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">3</span>
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center justify-between transition-colors">
              大洋洲
              <span class="bg-gray-200 text-gray-700 text-xs px-2 py-0.5 rounded-full">2</span>
            </button>
          </li>
        </ul>
      </div>

      <div>
        <h3 class="text-xs font-semibold text-gray-500 uppercase tracking-wider px-3 mb-2">国家列表</h3>
        <ul class="space-y-1">
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/237/30/20')]"></span>
              中国
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md bg-primary/10 text-primary text-sm font-medium flex items-center">
              <span class="flag-icon bg-[url('https://picsum.photos/id/238/30/20')]"></span>
              美国
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/239/30/20')]"></span>
              欧盟
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/240/30/20')]"></span>
              日本
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/241/30/20')]"></span>
              韩国
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/242/30/20')]"></span>
              英国
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/243/30/20')]"></span>
              澳大利亚
            </button>
          </li>
          <li>
            <button class="w-full text-left px-3 py-2 rounded-md hover:bg-gray-100 text-gray-700 text-sm font-medium flex items-center transition-colors">
              <span class="flag-icon bg-[url('https://picsum.photos/id/244/30/20')]"></span>
              加拿大
            </button>
          </li>
        </ul>

        <button class="w-full text-left px-3 py-2 text-primary text-sm font-medium flex items-center mt-2 hover:bg-primary/5 rounded-md transition-colors">
          <i class="fa fa-plus-circle mr-2"></i> 添加国家
        </button>
      </div>
    </div>
  </aside>

  <!-- 右侧主内容 -->
  <div class="flex-1 flex flex-col overflow-hidden">
    <!-- 国家信息和操作栏 -->
    <div class="bg-white border-b border-gray-200 p-4 flex flex-col md:flex-row md:items-center justify-between">
      <div>
        <div class="flex items-center">
          <span class="flag-icon bg-[url('https://picsum.photos/id/238/30/20')] w-8 h-5"></span>
          <h2 class="text-xl font-semibold">美国 - FDA认证数据爬虫</h2>
        </div>
        <p class="text-sm text-gray-500 mt-0.5">管理美国FDA相关的各类认证数据爬虫配置和执行</p>
      </div>

      <div class="flex items-center space-x-3 mt-3 md:mt-0">
        <div class="relative">
          <select class="pl-3 pr-8 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all text-sm appearance-none bg-white">
            <option>全部爬虫状态</option>
            <option>运行中</option>
            <option>已停止</option>
            <option>异常</option>
          </select>
          <i class="fa fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none text-xs"></i>
        </div>

        <button class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
          <i class="fa fa-refresh mr-2"></i> 批量操作
        </button>

        <button class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/90 transition-colors flex items-center shadow-sm">
          <i class="fa fa-plus-circle mr-2"></i> 新增爬虫
        </button>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="bg-white border-b border-gray-200 p-4">
      <h3 class="text-sm font-semibold text-gray-700 mb-3">数据概览</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-blue-50 p-3 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-1">总爬虫数量</div>
          <div class="text-2xl font-bold text-gray-800">6</div>
          <div class="text-xs text-green-600 flex items-center mt-1">
            <i class="fa fa-check-circle mr-1"></i> 全部配置完成
          </div>
        </div>

        <div class="bg-green-50 p-3 rounded-lg border border-green-100">
          <div class="text-xs text-green-600 font-medium mb-1">运行中</div>
          <div class="text-2xl font-bold text-gray-800">4</div>
          <div class="text-xs text-green-600 flex items-center mt-1">
            <i class="fa fa-arrow-up mr-1"></i> 较昨日 +1
          </div>
        </div>

        <div class="bg-yellow-50 p-3 rounded-lg border border-yellow-100">
          <div class="text-xs text-yellow-600 font-medium mb-1">已停止</div>
          <div class="text-2xl font-bold text-gray-800">2</div>
          <div class="text-xs text-yellow-600 flex items-center mt-1">
            <i class="fa fa-pause-circle mr-1"></i> 手动暂停
          </div>
        </div>

        <div class="bg-gray-50 p-3 rounded-lg border border-gray-100">
          <div class="text-xs text-gray-600 font-medium mb-1">今日数据量</div>
          <div class="text-2xl font-bold text-gray-800">547,890</div>
          <div class="text-xs text-green-600 flex items-center mt-1">
            <i class="fa fa-arrow-up mr-1"></i> 较昨日 +12.5%
          </div>
        </div>
      </div>
    </div>

    <!-- 爬虫类型标签页 -->
    <div class="bg-white border-b border-gray-200 flex overflow-x-auto scrollbar-hide">
      <button class="px-4 py-3 text-sm font-medium text-primary border-b-2 border-primary whitespace-nowrap">
        全部类型
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        510K设备
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        设备注册
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        不良事件
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        召回记录
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        指导文档
      </button>
      <button class="px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 whitespace-nowrap transition-colors">
        海关案例
      </button>
    </div>

    <!-- 爬虫列表内容区域 -->
    <div class="flex-1 overflow-y-auto p-6 bg-gray-50">
      <!-- 爬虫列表 -->
      <div class="space-y-4">
        <!-- 510K设备爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-primary mr-3">
                <i class="fa fa-medkit"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-510K设备爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: 510K设备数据 | 更新频率: 每日一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <span class="w-2 h-2 rounded-full bg-green-500 mr-1.5"></span>运行中
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>

                <!-- 下拉菜单 -->
                <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10 hidden group-hover:block border border-gray-200">
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">编辑配置</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">查看日志</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">爬取历史</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">测试运行</a>
                  <div class="border-t border-gray-100 my-1"></div>
                  <a href="#" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">停止爬虫</a>
                </div>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">FDA OpenFDA API</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-15 08:30:45</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">REST API + 关键词搜索</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 89,245 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 3.8 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-primary text-white rounded-md hover:bg-primary/90 transition-colors flex items-center" onclick="runCrawler('us510k')">
                  <i class="fa fa-play mr-1.5"></i> 立即运行
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 设备注册爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center text-purple-600 mr-3">
                <i class="fa fa-registered"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-设备注册爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: 设备注册数据 | 更新频率: 每12小时一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <span class="w-2 h-2 rounded-full bg-green-500 mr-1.5"></span>运行中
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>

                <!-- 下拉菜单 -->
                <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10 hidden group-hover:block border border-gray-200">
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">编辑配置</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">查看日志</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">爬取历史</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">测试运行</a>
                  <div class="border-t border-gray-100 my-1"></div>
                  <a href="#" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">停止爬虫</a>
                </div>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">FDA设备注册数据库</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-15 00:15:32</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">API接口 + 关键词搜索</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 156,789 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 5.2 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-primary text-white rounded-md hover:bg-primary/90 transition-colors flex items-center" onclick="runCrawler('usregistration')">
                  <i class="fa fa-play mr-1.5"></i> 立即运行
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 不良事件爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center text-red-600 mr-3">
                <i class="fa fa-exclamation-triangle"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-不良事件爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: 设备不良事件 | 更新频率: 每6小时一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                  <span class="w-2 h-2 rounded-full bg-yellow-500 mr-1.5"></span>已停止
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>

                <!-- 下拉菜单 -->
                <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-10 hidden group-hover:block border border-gray-200">
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">编辑配置</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">查看日志</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">爬取历史</a>
                  <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">测试运行</a>
                  <div class="border-t border-gray-100 my-1"></div>
                  <a href="#" class="block px-4 py-2 text-sm text-green-600 hover:bg-gray-100">启动爬虫</a>
                </div>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">FDA MAUDE数据库</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-14 18:45:12</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">OpenFDA API + 关键词搜索</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 234,567 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 4.1 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors flex items-center" onclick="runCrawler('usevent')">
                  <i class="fa fa-play mr-1.5"></i> 启动爬虫
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 召回记录爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 mr-3">
                <i class="fa fa-truck"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-召回记录爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: 设备召回记录 | 更新频率: 每日一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <span class="w-2 h-2 rounded-full bg-green-500 mr-1.5"></span>运行中
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">FDA召回数据库</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-15 09:20:18</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">OpenFDA API + 关键词搜索</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 45,892 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 3.2 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-primary text-white rounded-md hover:bg-primary/90 transition-colors flex items-center" onclick="runCrawler('usrecall')">
                  <i class="fa fa-play mr-1.5"></i> 立即运行
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 指导文档爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-teal-100 flex items-center justify-center text-teal-600 mr-3">
                <i class="fa fa-book"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-指导文档爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: FDA指导文档 | 更新频率: 每周一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <span class="w-2 h-2 rounded-full bg-green-500 mr-1.5"></span>运行中
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">FDA指导文档中心</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-12 10:15:36</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">网页爬取 + PDF下载</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 12,456 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 8.7 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-primary text-white rounded-md hover:bg-primary/90 transition-colors flex items-center" onclick="runCrawler('guidance')">
                  <i class="fa fa-play mr-1.5"></i> 立即运行
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 海关案例爬虫 -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden card-hover">
          <div class="p-4 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mr-3">
                <i class="fa fa-balance-scale"></i>
              </div>
              <div>
                <h3 class="font-medium text-gray-800">美国-海关案例爬虫</h3>
                <p class="text-xs text-gray-500">数据类型: 海关案例数据 | 更新频率: 每周一次</p>
              </div>
            </div>

            <div class="flex items-center space-x-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                  <span class="w-2 h-2 rounded-full bg-yellow-500 mr-1.5"></span>已停止
                </span>

              <div class="relative group">
                <button class="p-1.5 text-gray-500 hover:text-gray-700 rounded-full hover:bg-gray-100 transition-colors">
                  <i class="fa fa-ellipsis-v"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="p-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取源</div>
                <div class="text-sm text-gray-800">美国海关和边境保护局</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">最后运行时间</div>
                <div class="text-sm text-gray-800">2023-06-10 14:30:22</div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">爬取方法</div>
                <div class="text-sm text-gray-800">API接口 + 关键词搜索</div>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="flex items-center">
                  <i class="fa fa-database text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">累计数据: 8,945 条</span>
                </div>
                <div class="flex items-center">
                  <i class="fa fa-clock-o text-gray-400 mr-1.5"></i>
                  <span class="text-sm text-gray-600">平均耗时: 6.3 分钟</span>
                </div>
              </div>

              <div class="flex items-center space-x-2">
                <button class="px-3 py-1.5 text-xs border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors flex items-center">
                  <i class="fa fa-history mr-1.5"></i> 历史
                </button>
                <button class="px-3 py-1.5 text-xs bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors flex items-center" onclick="runCrawler('customs-case')">
                  <i class="fa fa-play mr-1.5"></i> 启动爬虫
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</main>

<!-- 页脚 -->
<footer class="bg-white border-t border-gray-200 py-3">
  <div class="container mx-auto px-4 flex flex-col md:flex-row items-center justify-between text-sm text-gray-500">
    <div class="mb-2 md:mb-0">© 2023 认证数据爬虫管理系统 | 版本 v2.1.0</div>
    <div class="flex items-center space-x-4">
      <a href="#" class="hover:text-primary transition-colors">系统文档</a>
      <a href="#" class="hover:text-primary transition-colors">爬虫指南</a>
      <a href="#" class="hover:text-primary transition-colors">技术支持</a>
    </div>
  </div>
</footer>

<!-- 运行爬虫的加载动画模态框 -->
<div id="loadingModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center hidden">
  <div class="bg-white rounded-lg p-6 max-w-md w-full shadow-xl transform transition-all">
    <div class="text-center">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-primary border-t-transparent mb-4"></div>
      <h3 class="text-lg font-semibold text-gray-800 mb-2">正在执行爬虫</h3>
      <p class="text-gray-600 text-sm mb-4">正在从目标网站获取数据，请稍候...</p>
      <div class="w-full bg-gray-200 rounded-full h-1.5 mb-2">
        <div id="progressBar" class="bg-primary h-1.5 rounded-full" style="width: 0%"></div>
      </div>
      <p id="progressText" class="text-xs text-gray-500">准备中...</p>
    </div>
  </div>
</div>

<script>
  // 美国爬虫API交互
  document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('loadingModal');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');

    // 爬虫API配置
    const API_BASE_URL = 'http://localhost:8080/api/us-crawler';
    
    // 爬虫类型映射
    const crawlerEndpoints = {
      'us510k': '/execute/us510k',
      'usregistration': '/execute/usregistration', 
      'usevent': '/execute/usevent',
      'usrecall': '/execute/usrecall',
      'guidance': '/execute/guidance',
      'customs-case': '/execute/customs-case'
    };

    // 运行爬虫函数
    window.runCrawler = async function(crawlerType) {
      const endpoint = crawlerEndpoints[crawlerType];
      if (!endpoint) {
        alert('未知的爬虫类型: ' + crawlerType);
        return;
      }

      // 显示加载模态框
      modal.classList.remove('hidden');
      progressBar.style.width = '0%';
      progressText.textContent = '准备中...';

      try {
        // 构建请求参数
        const params = buildCrawlerParams(crawlerType);
        
        // 发送请求
        progressText.textContent = '正在连接API...';
        progressBar.style.width = '20%';
        
        const response = await fetch(API_BASE_URL + endpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(params)
        });

        progressText.textContent = '正在处理数据...';
        progressBar.style.width = '60%';

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        
        progressText.textContent = '处理完成...';
        progressBar.style.width = '90%';

        // 显示结果
        setTimeout(() => {
          progressBar.style.width = '100%';
          progressText.textContent = '爬取完成！';
          
          // 显示结果消息
          showResultMessage(result, crawlerType);
          
          // 关闭模态框
          setTimeout(() => {
            modal.classList.add('hidden');
          }, 1000);
        }, 500);

      } catch (error) {
        console.error('爬虫执行失败:', error);
        progressText.textContent = '执行失败: ' + error.message;
        progressBar.style.width = '100%';
        progressBar.style.backgroundColor = '#EF4444';
        
        setTimeout(() => {
          modal.classList.add('hidden');
          progressBar.style.backgroundColor = '#2563EB'; // 重置颜色
        }, 2000);
      }
    };

    // 构建爬虫参数
    function buildCrawlerParams(crawlerType) {
      const baseParams = {
        maxPages: 5,
        inputKeywords: "medical device,医疗器械"
      };

      switch (crawlerType) {
        case 'us510k':
          return {
            ...baseParams,
            deviceName: "medical device",
            maxPages: 3
          };
        case 'usregistration':
          return {
            ...baseParams,
            establishmentName: "medical",
            maxPages: 3
          };
        case 'usevent':
          return {
            ...baseParams,
            deviceName: "medical device",
            maxPages: 3
          };
        case 'usrecall':
          return {
            ...baseParams,
            recallingFirm: "medical",
            maxPages: 3
          };
        case 'guidance':
          return {
            maxRecords: 10
          };
        case 'customs-case':
          return {
            hsCode: "9018",
            maxRecords: 10,
            batchSize: 10,
            inputKeywords: "medical device"
          };
        default:
          return baseParams;
      }
    }

    // 显示结果消息
    function showResultMessage(result, crawlerType) {
      const message = result.success ? 
        `✅ ${crawlerType} 爬虫执行成功！\n${result.message}` : 
        `❌ ${crawlerType} 爬虫执行失败！\n${result.message}`;
      
      // 创建通知元素
      const notification = document.createElement('div');
      notification.className = `fixed top-4 right-4 p-4 rounded-lg shadow-lg z-50 max-w-md ${
        result.success ? 'bg-green-100 text-green-800 border border-green-200' : 
        'bg-red-100 text-red-800 border border-red-200'
      }`;
      
      notification.innerHTML = `
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <i class="fa ${result.success ? 'fa-check-circle' : 'fa-exclamation-circle'} text-lg"></i>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium">${result.success ? '执行成功' : '执行失败'}</h3>
            <div class="mt-1 text-sm">
              <p>${result.message}</p>
              ${result.totalSaved ? `<p>保存记录数: ${result.totalSaved}</p>` : ''}
              ${result.databaseResult ? `<p>数据库结果: ${result.databaseResult}</p>` : ''}
            </div>
          </div>
          <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-gray-400 hover:text-gray-600">
            <i class="fa fa-times"></i>
          </button>
        </div>
      `;
      
      document.body.appendChild(notification);
      
      // 5秒后自动移除
      setTimeout(() => {
        if (notification.parentElement) {
          notification.remove();
        }
      }, 5000);
    }

    // 为所有运行按钮添加事件监听器（兼容性处理）
    const runButtons = document.querySelectorAll('button[onclick*="runCrawler"]');
    runButtons.forEach(button => {
      // 移除原有的onclick属性，使用addEventListener
      const onclickAttr = button.getAttribute('onclick');
      if (onclickAttr) {
        button.removeAttribute('onclick');
        button.addEventListener('click', function() {
          const match = onclickAttr.match(/runCrawler\('([^']+)'\)/);
          if (match) {
            runCrawler(match[1]);
          }
        });
      }
    });
  });
</script>
</body>
</html>
    
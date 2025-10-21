@echo off
REM ================================================================================
REM 医疗器械认证监控系统 - Windows 快速部署脚本
REM ================================================================================
REM 功能: 一键部署完整系统（包含 MySQL 数据库）
REM 使用: deploy-quick-start.bat
REM ================================================================================

setlocal enabledelayedexpansion

echo ========================================
echo 医疗器械认证监控系统 - 快速部署
echo ========================================
echo.

REM 步骤1: 检查依赖
echo [1/7] 检查系统依赖...
where podman-compose >nul 2>&1
if %errorlevel% equ 0 (
    set COMPOSE_CMD=podman-compose
    echo √ 使用 Podman Compose
    goto :check_done
)

where docker-compose >nul 2>&1
if %errorlevel% equ 0 (
    set COMPOSE_CMD=docker-compose
    echo √ 使用 Docker Compose
    goto :check_done
)

echo × 错误: 未找到 podman-compose 或 docker-compose
echo 请先安装 Podman Desktop 或 Docker Desktop
pause
exit /b 1

:check_done

REM 步骤2: 创建环境配置文件
echo [2/7] 配置环境变量...
if not exist .env.prod (
    if exist .env.prod.minimal (
        copy .env.prod.minimal .env.prod
        echo √ 已创建 .env.prod（使用最小化配置）
        echo.
        echo 重要提醒:
        echo 1. 请编辑 .env.prod 文件
        echo 2. 修改所有密码（MYSQL_ROOT_PASSWORD, MYSQL_PASSWORD 等）
        echo 3. 修改 CORS_ALLOWED_ORIGINS 为实际服务器IP或域名
        echo.
        set /p EDIT_CONFIG="是否现在编辑配置文件? (y/n): "
        if /i "!EDIT_CONFIG!"=="y" (
            notepad .env.prod
        ) else (
            echo 请稍后手动编辑 .env.prod 文件并重新部署
        )
    ) else (
        echo × 错误: 未找到 .env.prod.minimal 模板文件
        pause
        exit /b 1
    )
) else (
    echo √ .env.prod 文件已存在
)

REM 步骤3: 检查数据库初始化脚本
echo [3/7] 检查数据库初始化脚本...
if not exist database\init_database_full.sql (
    echo × 错误: 未找到 database\init_database_full.sql
    echo 请确保数据库初始化脚本存在
    pause
    exit /b 1
)
echo √ 数据库初始化脚本就绪

REM 步骤4: 停止旧容器（如果存在）
echo [4/7] 清理旧容器...
%COMPOSE_CMD% -f docker-compose.prod.yml ps 2>nul | findstr "Up" >nul
if %errorlevel% equ 0 (
    echo 发现运行中的容器，正在停止...
    %COMPOSE_CMD% -f docker-compose.prod.yml down
    echo √ 旧容器已停止
) else (
    echo √ 无需清理
)

REM 步骤5: 构建镜像
echo [5/7] 构建 Docker 镜像...
echo 这可能需要几分钟时间，请耐心等待...
%COMPOSE_CMD% -f docker-compose.prod.yml build --no-cache
if %errorlevel% neq 0 (
    echo × 镜像构建失败
    pause
    exit /b 1
)
echo √ 镜像构建完成

REM 步骤6: 启动所有服务
echo [6/7] 启动所有服务...
%COMPOSE_CMD% -f docker-compose.prod.yml up -d
if %errorlevel% neq 0 (
    echo × 服务启动失败
    pause
    exit /b 1
)

REM 等待服务启动
echo 等待服务启动...
timeout /t 10 /nobreak >nul

REM 步骤7: 验证部署
echo [7/7] 验证部署状态...
%COMPOSE_CMD% -f docker-compose.prod.yml ps

echo.
echo ========================================
echo 部署完成！
echo ========================================
echo.
echo 📋 服务访问地址:
echo   前端应用:        http://localhost
echo   后端API:         http://localhost:8080/api
echo   API文档:         http://localhost:8080/api/doc.html
echo   数据库管理:      http://localhost:8081
echo   Druid监控:       http://localhost:8080/druid
echo.
echo 🔑 默认登录信息:
echo   数据库管理 (phpMyAdmin):
echo     用户名: root
echo     密码: 查看 .env.prod 中的 MYSQL_ROOT_PASSWORD
echo.
echo   Druid监控:
echo     用户名: admin
echo     密码: 查看 .env.prod 中的 DRUID_PASSWORD
echo.
echo 📊 查看日志:
echo   所有服务:  %COMPOSE_CMD% -f docker-compose.prod.yml logs -f
echo   后端:      %COMPOSE_CMD% -f docker-compose.prod.yml logs -f backend
echo   前端:      %COMPOSE_CMD% -f docker-compose.prod.yml logs -f frontend
echo   数据库:    %COMPOSE_CMD% -f docker-compose.prod.yml logs -f mysql
echo.
echo 🛑 停止服务:
echo   %COMPOSE_CMD% -f docker-compose.prod.yml stop
echo.
echo 🔄 重启服务:
echo   %COMPOSE_CMD% -f docker-compose.prod.yml restart
echo.
echo ❌ 完全清理:
echo   %COMPOSE_CMD% -f docker-compose.prod.yml down -v
echo.
echo 注意: 如果服务无法访问，请检查防火墙设置
echo.
pause

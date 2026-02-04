@echo off
chcp 65001 >nul
echo ========================================
echo  🍜 FoodDecisionAssistant 快速构建脚本
echo ========================================
echo.

:: 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Java，请先安装 JDK 17+
    pause
    exit /b 1
)
echo ✅ Java 已安装

:: 检查 Android SDK
if "%ANDROID_HOME%"=="" (
    echo ⚠️  警告: ANDROID_HOME 未设置
    echo    如果 Android Studio 能正常使用，可以忽略
) else (
    echo ✅ ANDROID_HOME: %ANDROID_HOME%
)

echo.
echo 选择操作:
echo   [1] 构建 Debug APK (推荐测试)
echo   [2] 构建 Release APK
echo   [3] 清理并重新构建
echo   [4] 安装到连接的设备
echo   [5] 退出
echo.

set /p choice="请输入选项 [1-5]: "

if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_release
if "%choice%"=="3" goto clean_build
if "%choice%"=="4" goto install_apk
if "%choice%"=="5" goto end

echo ❌ 无效选项
pause
exit /b 1

:build_debug
echo.
echo 🔨 正在构建 Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)
echo.
echo ✅ Debug APK 构建成功!
echo 📁 位置: app\build\outputs\apk\debug\app-debug.apk
echo.
choice /c YN /m "是否安装到设备"
if errorlevel 2 goto end
if errorlevel 1 goto install_apk
goto end

:build_release
echo.
echo 🔨 正在构建 Release APK...
echo ⚠️  注意: Release 版本需要签名密钥
echo.
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)
echo.
echo ✅ Release APK 构建成功!
echo 📁 位置: app\build\outputs\apk\release\app-release-unsigned.apk
echo ⚠️  注意: 此 APK 未签名，无法直接安装
goto end

:clean_build
echo.
echo 🧹 正在清理...
call gradlew.bat clean
echo.
echo 🔨 正在重新构建 Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ❌ 构建失败
    pause
    exit /b 1
)
echo.
echo ✅ 重新构建成功!
goto end

:install_apk
echo.
echo 📱 正在安装到设备...
adb devices >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 ADB，请确保 Android SDK 已安装
    pause
    exit /b 1
)

adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo ❌ 安装失败，请检查:
    echo    - 设备是否连接
    echo    - USB 调试是否开启
    echo    - 是否已安装同名应用
    pause
    exit /b 1
)
echo.
echo ✅ 安装成功! 请在手机上查看
goto end

:end
echo.
echo 按任意键退出...
pause >nul

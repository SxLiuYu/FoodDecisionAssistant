#!/bin/bash

# 🍜 FoodDecisionAssistant 快速构建脚本

set -e

echo "========================================"
echo " 🍜 FoodDecisionAssistant 快速构建脚本"
echo "========================================"
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到 Java，请先安装 JDK 17+"
    exit 1
fi
echo "✅ Java 已安装"

# 检查 Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  警告: ANDROID_HOME 未设置"
fi

echo ""
echo "选择操作:"
echo "  [1] 构建 Debug APK (推荐测试)"
echo "  [2] 构建 Release APK"
echo "  [3] 清理并重新构建"
echo "  [4] 安装到连接的设备"
echo "  [5] 退出"
echo ""

read -p "请输入选项 [1-5]: " choice

case $choice in
    1)
        echo ""
        echo "🔨 正在构建 Debug APK..."
        ./gradlew assembleDebug
        echo ""
        echo "✅ Debug APK 构建成功!"
        echo "📁 位置: app/build/outputs/apk/debug/app-debug.apk"
        echo ""
        read -p "是否安装到设备? [y/N]: " install
        if [[ $install =~ ^[Yy]$ ]]; then
            ./gradlew installDebug
            echo "✅ 安装成功!"
        fi
        ;;
        
    2)
        echo ""
        echo "🔨 正在构建 Release APK..."
        echo "⚠️  注意: Release 版本需要签名密钥"
        echo ""
        ./gradlew assembleRelease
        echo ""
        echo "✅ Release APK 构建成功!"
        echo "📁 位置: app/build/outputs/apk/release/app-release-unsigned.apk"
        echo "⚠️  注意: 此 APK 未签名，无法直接安装"
        ;;
        
    3)
        echo ""
        echo "🧹 正在清理..."
        ./gradlew clean
        echo ""
        echo "🔨 正在重新构建 Debug APK..."
        ./gradlew assembleDebug
        echo ""
        echo "✅ 重新构建成功!"
        ;;
        
    4)
        echo ""
        echo "📱 正在安装到设备..."
        if ! command -v adb &> /dev/null; then
            echo "❌ 错误: 未找到 ADB"
            exit 1
        fi
        ./gradlew installDebug
        echo "✅ 安装成功!"
        ;;
        
    5)
        exit 0
        ;;
        
    *)
        echo "❌ 无效选项"
        exit 1
        ;;
esac

echo ""
read -p "按回车键退出..."

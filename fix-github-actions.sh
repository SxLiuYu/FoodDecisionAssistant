#!/bin/bash
# 修复 GitHub Actions 构建脚本
# 在本地运行此脚本后推送

echo "🔧 修复 GitHub Actions 配置..."

# 创建完整的 GitHub Actions 配置
mkdir -p .github/workflows

cat > .github/workflows/android-build.yml << 'EOF'
name: Android CI - Build APK

on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3
      with:
        gradle-version: '8.2'
        
    - name: Build with Gradle
      run: gradle assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: FoodAssistant-Debug-APK
        path: app/build/outputs/apk/debug/*.apk
        if-no-files-found: warn
EOF

echo "✅ 配置已更新"
echo ""
echo "📤 提交更改:"
git add .github/workflows/android-build.yml
git commit -m "Fix: Use system Gradle instead of wrapper"
git push origin main

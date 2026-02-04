# 🔧 快速修复 GitHub Actions 构建

由于网络问题无法自动推送，请按以下步骤手动修复：

---

## 方案 1：直接在 GitHub 上修改（推荐，2分钟）

1. 访问 https://github.com/SxLiuYu/FoodDecisionAssistant/edit/main/.github/workflows/android-build.yml

2. **删除原文件内容**，替换为以下代码：

```yaml
name: Android CI - Build APK

on:
  push:
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
        
    - name: Build Debug APK
      run: gradle assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: FoodAssistant-Debug-APK
        path: app/build/outputs/apk/debug/*.apk
        if-no-files-found: warn
```

3. 点击 **Commit changes...**
4. 等待自动构建（2-3分钟）

---

## 方案 2：使用 GitHub CLI

在本地运行：
```bash
gh auth login
gh workflow run android-build.yml --repo SxLiuYu/FoodDecisionAssistant
```

---

## 方案 3：重新上传完整项目

下载修复版项目：[FoodDecisionAssistant-Fixed.zip](sandbox:/mnt/user-data/repos/default/FoodDecisionAssistant-Fixed.zip)

1. 删除当前仓库：https://github.com/SxLiuYu/FoodDecisionAssistant/settings → Delete repository
2. 重新创建同名仓库
3. 上传修复版项目

---

## ✅ 修复说明

**原问题：** 缺少 `gradle-wrapper.jar` 文件

**修复方法：** 改用系统 Gradle（`gradle/actions/setup-gradle@v3`）代替 wrapper

---

## 📱 修复后获取 APK

1. 提交修复后访问：https://github.com/SxLiuYu/FoodDecisionAssistant/actions
2. 等待构建完成（绿色 ✓）
3. 点击最新构建 → 下载 **FoodAssistant-Debug-APK**

**建议直接使用方法 1，最快！**

# 🚀 APK 构建指南

## 📋 前置要求

### 必需软件
1. **Android Studio** (推荐 Hedgehog 2023.1.1 或更新版本)
2. **JDK 17** (Android Studio 自带或单独安装)
3. **Git** (可选，用于版本控制)

### 系统要求
- Windows 10/11、macOS 或 Linux
- 至少 8GB RAM (推荐 16GB)
- 10GB 可用磁盘空间

---

## 🛠️ 步骤一：安装 Android Studio

### Windows
1. 访问 https://developer.android.com/studio
2. 下载并安装 Android Studio
3. 启动后选择 "Standard" 安装

### 首次启动配置
1. 打开 Android Studio
2. 选择 "More Actions" → "SDK Manager"
3. 安装以下组件：
   - ✅ Android SDK Platform 34
   - ✅ Android SDK Build-Tools 34
   - ✅ Android Emulator
   - ✅ Android SDK Platform-Tools
   - ✅ CMake 3.22.1
   - ✅ NDK (Side by side) 25.2.x

---

## 📂 步骤二：打开项目

### 方式 1：直接打开
1. 打开 Android Studio
2. 选择 "Open" → 选择 `FoodDecisionAssistant` 文件夹
3. 等待 Gradle 同步完成（可能需要 5-10 分钟，首次）

### 方式 2：导入
1. File → New → Import Project
2. 选择 `FoodDecisionAssistant` 文件夹
3. 选择 "Import project from external model" → Gradle

---

## ⚙️ 步骤三：项目配置

### 检查 SDK 路径
1. File → Settings (或 Android Studio → Preferences on Mac)
2. 搜索 "SDK" → Android SDK
3. 确认 SDK 路径正确

### 同步项目
1. 点击工具栏的 "Sync Project with Gradle Files" 按钮（大象图标）
2. 或 File → Sync Project with Gradle Files
3. 等待同步完成（底部状态栏显示 "Sync finished"）

---

## 🔨 步骤四：构建 APK

### 方式 1：通过 Android Studio（推荐）

#### 构建 Debug APK
1. 点击菜单栏 **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. 等待构建完成
3. 点击右下角弹出的 "locate" 查看 APK 位置
   - 路径: `app/build/outputs/apk/debug/app-debug.apk`

#### 构建 Release APK
1. 先生成签名密钥：
   ```bash
   # 在 Terminal 中运行（或命令提示符）
   keytool -genkey -v -keystore foodassistant.keystore -alias foodassistant -keyalg RSA -keysize 2048 -validity 10000
   ```
   - 记住你设置的密码
   - 记住密钥别名（默认 foodassistant）

2. 将密钥文件放入项目根目录

3. 在 `app/build.gradle.kts` 中添加签名配置：
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("../foodassistant.keystore")
               storePassword = "你的密码"
               keyAlias = "foodassistant"
               keyPassword = "你的密码"
           }
       }
       
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               // ... 其他配置
           }
       }
   }
   ```

4. Build → Generate Signed Bundle / APK
5. 选择 APK → Next
6. 选择或创建密钥库 → Next
7. 选择 release 版本 → Finish

### 方式 2：通过命令行

#### Windows
```cmd
# 进入项目目录
cd FoodDecisionAssistant

# 构建 Debug APK
gradlew.bat assembleDebug

# 构建 Release APK
gradlew.bat assembleRelease
```

#### Mac/Linux
```bash
# 进入项目目录
cd FoodDecisionAssistant

# 给 gradlew 添加执行权限
chmod +x gradlew

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### APK 输出位置
```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk          # 约 15-20MB (包含模拟推理)
└── release/
    └── app-release.apk        # 约 10-15MB
```

---

## 📱 步骤五：安装到手机

### 方式 1：通过 ADB
```bash
# 连接手机，开启 USB 调试
adb devices

# 安装 APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 如果已安装，强制重新安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 方式 2：直接传输
1. 将 APK 复制到手机
2. 在手机上点击安装
3. 允许 "安装未知来源应用"

### 方式 3：通过 Android Studio
1. 连接手机（开启 USB 调试）
2. 点击工具栏的运行按钮（绿色三角形）
3. 选择你的设备

---

## 🔧 常见问题

### 问题 1: Gradle 同步失败
**症状**: 同步时报错或卡住

**解决**:
```bash
# 清理 Gradle 缓存
./gradlew clean

# 或者删除 .gradle 文件夹
rm -rf ~/.gradle/caches/

# 重新同步
./gradlew build --refresh-dependencies
```

### 问题 2: 编译错误 "Cannot find androidx..."
**解决**:
1. File → Invalidate Caches / Restart
2. 选择 "Invalidate and Restart"
3. 重启后重新同步

### 问题 3: NDK 未找到
**解决**:
1. Tools → SDK Manager → SDK Tools
2. 勾选 "NDK (Side by side)" → Apply
3. 等待安装完成后重新同步

### 问题 4: 内存不足
**症状**: 构建时报错 OutOfMemoryError

**解决**:
编辑 `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx6g -Dfile.encoding=UTF-8
```

### 问题 5: 应用安装后闪退
**检查**:
1. 查看 logcat 日志: `adb logcat | grep AndroidRuntime`
2. 常见原因：
   - 权限未声明 → 检查 AndroidManifest.xml
   - Room 数据库迁移问题 → 清除应用数据或卸载重装

---

## 📦 发布准备

### 减小 APK 大小
1. 启用代码压缩：
   ```kotlin
   buildTypes {
       release {
           isMinifyEnabled = true
           isShrinkResources = true
           proguardFiles(...)
       }
   }
   ```

2. 移除未使用的资源

### 应用签名（必需）
发布到应用商店前必须使用正式签名。

---

## ✅ 快速检查清单

构建 APK 前确认：
- [ ] Android Studio 版本 >= 2023.1.1
- [ ] SDK 34 已安装
- [ ] 项目成功同步（无红色错误）
- [ ] 测试设备已连接（如需要）

---

## 🎉 构建成功

构建完成后，你会看到：
```
BUILD SUCCESSFUL in 2m 30s
50 actionable tasks: 50 executed
```

APK 文件位于：`app/build/outputs/apk/debug/app-debug.apk`

现在可以安装到手机使用了！

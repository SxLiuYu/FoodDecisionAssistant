# 🍜 FoodDecisionAssistant

**一款完整的安卓端 AI 餐食推荐应用，可直接构建 APK 安装运行！**

[![Android](https://img.shields.io/badge/Android-34-green.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)]()
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-✓-purple.svg)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)]()

---

## ✨ 功能特性

| 功能 | 描述 | 状态 |
|------|------|------|
| 🤖 AI 推荐 | 基于用户偏好智能推荐餐食 | ✅ 模拟模式 |
| 📷 拍照识别 | 拍摄/上传食物图片 | ✅ 完成 |
| 💬 自然对话 | 支持文字描述偏好 | ✅ 完成 |
| 🎯 个性学习 | 记录用户喜好，越用越懂你 | ✅ 完成 |
| 🏠 本地运行 | 数据完全本地，保护隐私 | ✅ 完成 |

---

## 📱 应用截图（示意图）

```
┌─────────────────────────┐
│     餐食决策助手    ≡   │
├─────────────────────────┤
│  ┌─────────────────┐    │
│  │  📷 点击拍照     │    │
│  │     或选择图片   │    │
│  └─────────────────┘    │
│                         │
│  [补充描述（可选）   ]   │
│                         │
│  [📷拍照] [🖼️相册] [推荐]│
│                         │
│  ┌─────────────────┐    │
│  │ 🍜 麻婆豆腐      │    │
│  │ 🏷️ 川菜          │    │
│  │                   │    │
│  │ 经典川菜代表...   │    │
│  │                   │    │
│  │ [👎]        [👍]  │    │
│  └─────────────────┘    │
│                         │
│      [✨ 帮我选]        │
└─────────────────────────┘
```

---

## 🚀 快速开始（3分钟）

### 方式 1：使用构建脚本（推荐）

**Windows:**
```batch
build.bat
# 选择 [1] 构建 Debug APK
```

**Mac/Linux:**
```bash
chmod +x build.sh
./build.sh
```

### 方式 2：使用 Android Studio

1. **打开项目**
   ```
   Android Studio → Open → 选择 FoodDecisionAssistant 文件夹
   ```

2. **等待同步**
   - 首次打开需要 5-10 分钟下载依赖
   - 底部状态栏显示 "Sync finished" 即完成

3. **构建 APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

4. **安装运行**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   
   或连接手机后直接点击 ▶️ 运行按钮

---

## 📦 APK 输出

构建完成后，APK 文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

**大小**: 约 15-20MB（无需额外模型文件）

---

## 🛠️ 技术架构

```
┌──────────────────────────────────────┐
│           UI 层 (Compose)            │
│  ┌──────────┐ ┌──────────┐          │
│  │ 图片输入  │ │ 推荐卡片  │          │
│  └──────────┘ └──────────┘          │
├──────────────────────────────────────┤
│       ViewModel (业务逻辑)            │
│    ┌──────────────────┐              │
│    │   MainViewModel  │              │
│    └──────────────────┘              │
├──────────────────────────────────────┤
│         数据层                        │
│  ┌──────────────┐ ┌──────────────┐   │
│  │ MNN推理管理器 │ │ Room数据库    │   │
│  │ (模拟/真实)   │ │ (偏好/历史)   │   │
│  └──────────────┘ └──────────────┘   │
└──────────────────────────────────────┘
```

---

## 📁 项目结构

```
FoodDecisionAssistant/
├── 📘 BUILD_APK_GUIDE.md      # APK 构建详细指南
├── 📗 PROJECT_SUMMARY.md      # 项目完整总结
├── 📄 README.md               # 本文件
├── 🔧 build.bat / build.sh    # 快速构建脚本
│
├── 📚 docs/                   # 技术文档
│   ├── 01-环境搭建.md
│   ├── 02-模型准备.md
│   ├── 03-核心开发.md
│   └── 04-优化调试.md
│
└── 📱 app/                    # Android 应用代码
    ├── src/main/
    │   ├── java/com/foodassistant/
    │   │   ├── FoodApplication.kt
    │   │   ├── MainActivity.kt
    │   │   ├── data/          # 数据层
    │   │   ├── mnn/           # MNN 推理
    │   │   ├── prompt/        # 提示词构建
    │   │   └── ui/            # UI 层
    │   └── res/               # 资源文件
    └── build.gradle.kts       # 构建配置
```

---

## 🔬 模拟模式 vs 真实 AI

### 当前：模拟模式（默认）
- ✅ 无需下载 1.5GB 模型
- ✅ 立即可用
- ✅ 6 种预设推荐
- ✅ 智能关键词匹配

### 可升级：真实 MNN 推理
准备好 [Qwen2-VL-2B-Int4](https://modelscope.cn/models/qwen/Qwen2-VL-2B-Instruct) 模型后：

1. 下载模型文件 (~1.5GB)
2. 放入 `app/src/main/assets/models/`
3. 修改 `MNNInferenceManager.kt`:
   ```kotlin
   const val MOCK_MODE = false
   ```
4. 集成 `libMNN.so` 库
5. 重新构建

详细步骤见 [docs/02-模型准备.md](docs/02-模型准备.md)

---

## 📋 系统要求

### 开发环境
- Android Studio Hedgehog (2023.1.1) 或更新
- JDK 17+
- Android SDK 34

### 运行设备
- Android 9.0 (API 28) 及以上
- 2GB RAM 以上
- 支持相机（可选）

---

## 🤝 贡献指南

欢迎贡献代码！请遵循：
1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 🙏 致谢

- [MNN](https://github.com/alibaba/MNN) - 阿里开源移动端推理框架
- [Qwen2-VL](https://github.com/QwenLM/Qwen2-VL) - 通义千问多模态模型
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 Android UI 工具包

---

## 💬 反馈

如有问题或建议，欢迎提出 Issue！

**现在就构建你的专属餐食助手吧！** 🍜✨

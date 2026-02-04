# 🌐 GitHub Actions 云端构建指南

使用 GitHub Actions 自动构建 APK，无需本地安装 Android Studio！

---

## 📋 前置条件

1. **GitHub 账号**（免费注册：https://github.com/signup）
2. **项目代码**（已准备好）

---

## 🚀 使用步骤

### 第 1 步：创建 GitHub 仓库

1. 登录 https://github.com
2. 点击右上角 **+** → **New repository**
3. 填写信息：
   - Repository name: `FoodDecisionAssistant`
   - Description: `安卓端 AI 餐食推荐助手`
   - 选择 **Public**（或 Private）
   - 勾选 **Add a README file**
4. 点击 **Create repository**

### 第 2 步：上传项目代码

**方式 A：通过网页上传（最简单）**

1. 进入你的新仓库
2. 点击 **Add file** → **Upload files**
3. 将 `FoodDecisionAssistant.zip` 解压
4. 拖拽所有文件到上传区域
5. 填写提交信息：`Initial commit`
6. 点击 **Commit changes**

**方式 B：使用 Git 命令**

```bash
# 克隆仓库
git clone https://github.com/你的用户名/FoodDecisionAssistant.git
cd FoodDecisionAssistant

# 复制项目文件
# 将 FoodDecisionAssistant 项目文件复制到此目录

# 提交并推送
git add .
git commit -m "Initial commit"
git push origin main
```

### 第 3 步：触发自动构建

上传代码后，GitHub Actions 会自动开始构建：

1. 进入仓库页面
2. 点击上方的 **Actions** 标签
3. 查看构建进度（约 3-5 分钟）

### 第 4 步：下载 APK

构建完成后：

1. 在 **Actions** 页面点击最新的工作流运行
2. 滚动到底部的 **Artifacts** 区域
3. 点击 **FoodAssistant-Debug-APK** 下载
4. 解压下载的文件即可获得 `app-debug.apk`

---

## 🔄 手动触发构建

如果你想重新构建或构建 Release 版本：

1. 进入仓库的 **Actions** 页面
2. 点击左侧的 **Android CI - Build APK**
3. 点击右侧的 **Run workflow**
4. 选择分支（main）
5. 选择构建类型（debug 或 release）
6. 点击 **Run workflow**

---

## 📁 构建产物

| 构建类型 | 文件名 | 大小 | 说明 |
|---------|--------|------|------|
| Debug | `app-debug.apk` | ~15-20MB | 可直接安装测试 |
| Release | `app-release-unsigned.apk` | ~10-15MB | 需签名后才能安装 |

---

## ⏱️ 构建时间

- **首次构建**：约 5-8 分钟（下载依赖）
- **后续构建**：约 2-3 分钟（使用缓存）

---

## 🚨 常见问题

### Q: Actions 没有自动运行？
A: 检查 `.github/workflows/android-build.yml` 文件是否在仓库中

### Q: 构建失败？
A: 
1. 点击失败的构建
2. 查看日志定位错误
3. 常见问题：
   - Gradle 权限问题 → 已配置 `chmod +x gradlew`
   - 依赖下载失败 → 重试即可

### Q: 下载的 APK 无法安装？
A: 
- Debug APK：需要在手机设置中允许"安装未知来源应用"
- Release APK：需要先签名，参考 BUILD_APK_GUIDE.md

---

## 🎉 完成！

现在每次你推送代码更新到 GitHub，都会自动构建新的 APK！

### 工作流程
```
推送代码 → GitHub Actions 自动构建 → 下载 APK → 安装测试
```

---

## 📚 相关文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [BUILD_APK_GUIDE.md](./BUILD_APK_GUIDE.md) - 本地构建指南
- [PROJECT_SUMMARY.md](./PROJECT_SUMMARY.md) - 项目总结

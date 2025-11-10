# DiTing
DiTing: 24/7 noise-filtering audio recorder for Android, built with Kotlin.（全天候智能录音助手）

> 基于 [Fossify Voice Recorder](https://github.com/FossifyOrg/Voice-Recorder) 深度定制，专为 **24 小时环境音采集 + 噪声过滤** 场景优化的开源录音应用。

![License](https://img.shields.io/github/license/JiashuaiXu/DiTing?color=blue)
![Platform](https://img.shields.io/badge/platform-Android-green)

## 🌐 项目关系

本项目基于以下仓库构建：

| 仓库 | 用途 | 地址 |
|------|------|------|
| **上游 (Upstream)** | Fossify 官方源码（只读） | https://github.com/FossifyOrg/Voice-Recorder |
| **开发 Fork** | 用于同步上游 + 自定义开发 | https://github.com/JiashuaiXu/Voice-Recorder |
| **本仓库 (DiTing)** | 最终产品发布与文档 | https://github.com/JiashuaiXu/DiTing |

> ✅ **开发与编译均在 `JiashuaiXu/Voice-Recorder` 中进行**，稳定后同步至此。

## 🚀 快速开始

### 克隆开发仓库（推荐）

```bash
# 克隆你的 fork（开发主战场）
git clone git@github.com:JiashuaiXu/Voice-Recorder.git
cd Voice-Recorder

# 添加上游源（仅需一次）
git remote add upstream https://github.com/FossifyOrg/Voice-Recorder.git

# 创建功能分支（示例：24 小时降噪）
git checkout -b feature/24h-noise-filter

# 编译 Debug 版本
./gradlew assembleDebug

# 安装到设备（可选）
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 同步上游更新

```bash
git fetch upstream
git checkout main
git rebase upstream/main
git push origin main
```

## 🛠️ 定制说明

- **包名 (applicationId)**：已修改为 `com.jiashuaixu.diting`
- **应用名称**：`DiTing`
- **核心增强功能**：
  - 24 小时后台录音（需 Android 权限适配）
  - 基于音量/频谱的噪声过滤模块
  - 自动归档录音文件（按日期/场景分类）
  - 隐私优先：完全离线，无需网络权限

## 📜 开源协议

本项目继承自 [Fossify Voice Recorder](https://github.com/FossifyOrg/Voice-Recorder)，遵循 **GNU General Public License v3.0 (GPLv3)**。

- 源码必须开源
- 分发时需包含 LICENSE 和版权声明
- 修改版本也需以 GPLv3 发布

详见 [LICENSE](./LICENSE)

## 🔗 相关链接

- [Fossify 官网](https://www.fossify.org)
- [开发仓库（Voice-Recorder）](https://github.com/JiashuaiXu/Voice-Recorder)
- [上游仓库（FossifyOrg）](https://github.com/FossifyOrg/Voice-Recorder)
- [本项目 Issues](https://github.com/JiashuaiXu/DiTing/issues)

---

Made with ❤️ by [Jesse](https://github.com/JiashuaiXu)  
For qzqh & future ventures 🌱



### ✅ 使用说明

1. 将上述内容保存为 `DiTing/README.md`
2. 确保 `DiTing` 仓库根目录包含 `LICENSE` 文件（建议直接从 Fossify 仓库复制）
3. 后续可将稳定版本的 APK 或发布说明放在 `Releases` 页面

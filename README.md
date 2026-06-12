# WatchVideo — 免费看视频

免费、无广告的 Android 视频聚合播放器。聚合多个影视站点资源，一个 App 搜全网，直接播放 HLS 串流，无需跳转第三方。

> **仅供学习交流使用，请勿用于商业用途。**

## 下载

前往 [Releases](../../releases/latest) 下载最新 APK，直接安装即可（需允许安装未知来源应用）。

## 功能

- **全网搜索**：一键搜索多个影视源，结果合并展示
- **搜索历史**：自动记录最近 20 条，点输入框即可快速复用
- **多线路 / 选集**：详情页支持多条播放线路切换，集数高亮当前集
- **内嵌播放器**：无广告，无跳转，直接播放
- **手势操作**：
  - 单击：显示 / 隐藏控制栏
  - 水平滑动：快进快退（实时预览秒数，松手跳转）
  - 长按 2 秒：2× 倍速播放，松手恢复
- **全屏**：一键横屏，隐藏状态栏和导航栏，切换不中断播放

## 截图

*（施工中）*

## 技术栈

| 层 | 技术 |
|---|---|
| 跨平台框架 | Kotlin Multiplatform + Compose Multiplatform |
| 网络 | Ktor Client（OkHttp 引擎）+ gzip |
| 播放器 | ExoPlayer (media3) HLS |
| 图片 | Coil 3 |

## 构建

需要 Java 17：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./gradlew :composeApp:assembleDebug
```

## 添加数据源

在 `commonMain/data/parsers/` 新建 `XxxParser.kt`，实现 `SiteParser` 接口，然后在 `ParserRegistry.kt` 注册即可。

## About

- **版本**：开发版（持续迭代）
- **平台**：Android（iOS 占位，待实现）
- **作者**：[@danweiyuancircle](https://github.com/danweiyuancircle)
- **License**：MIT

# WatchVideo

Android 视频聚合播放器（Kotlin Multiplatform）。聚合多个影视站点 JSON API，支持搜索、选集、HLS 串流播放。

## 功能

- 关键词搜索，支持多数据源并发查询
- 搜索历史（最近 20 条，可删除）
- 详情页多线路 / 选集切换
- 内嵌 ExoPlayer HLS 播放器
- 手势控制：
  - 单击：显示 / 隐藏控制栏
  - 水平滑动：快进快退（实时预览秒数，松手生效）
  - 长按 2 秒：2× 倍速，松手恢复
- 全屏：横屏 + 隐藏状态栏 / 导航栏，退出后自动恢复

## 下载

前往 [Releases](../../releases) 下载最新 APK。

## 技术栈

| 层 | 技术 |
|---|---|
| 跨平台框架 | Kotlin Multiplatform + Compose Multiplatform |
| 网络 | Ktor Client（OkHttp 引擎）+ ContentEncoding gzip |
| 播放器 | ExoPlayer (media3) HLS |
| 图片 | Coil 3 |
| 序列化 | kotlinx.serialization |
| 导航 | Jetpack Navigation Compose |

## 构建

需要 Java 17（Temurin 推荐）：

```bash
# Debug APK
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./gradlew :composeApp:assembleDebug

# 安装到已连接设备
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## 添加数据源

1. 在 `composeApp/src/commonMain/kotlin/com/watchvideo/data/parsers/` 新建 `XxxParser.kt`，实现 `SiteParser` 接口
2. 在 `ParserRegistry.kt` 的 `parsers` 列表中注册

```kotlin
interface SiteParser {
    val siteKey: String
    val siteName: String
    val baseUrl: String
    suspend fun search(keyword: String): List<SearchResult>
    suspend fun detail(id: String): List<Route>
    suspend fun playInfo(playPageUrl: String): PlayInfo
}
```

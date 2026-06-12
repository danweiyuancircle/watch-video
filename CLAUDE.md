# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# 编译 Android debug APK（必须用 Java 17）
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./gradlew :composeApp:assembleDebug

# 仅编译检查（不打包，速度更快）
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./gradlew :composeApp:compileDebugKotlinAndroid

# 安装到已连接的 Android 设备
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# 启动 App
adb shell am start -n com.watchvideo/.MainActivity

# 截图调试
adb exec-out screencap -p > /tmp/screenshot.png
```

**注意**：系统默认 Java 可能是 Java 8，会导致构建失败。所有 Gradle 命令必须加 `JAVA_HOME=.../temurin-17.jdk/Contents/Home` 前缀。

## Architecture

KMP（Kotlin Multiplatform）项目，Android + iOS 共享业务逻辑，UI 用 Compose Multiplatform。

### 分层结构

```
commonMain（共享）
  data/
    SiteParser.kt          # 站点解析接口（所有站点必须实现）
    ParserRegistry.kt      # 已注册站点列表（目前仅 ModuParser）
    parsers/
      ModuParser.kt        # 当前主要数据源，调用 JSON API
      XiaobaoParser.kt     # 备用，HTML 正则解析（未注册）
    model/Models.kt        # SearchResult / Episode / Route / PlayInfo
  ui/
    search/                # 搜索页（SearchScreen + SearchViewModel）
    detail/                # 详情页（DetailScreen + DetailViewModel）
    detail/DetailScreen.kt # 含 expect fun VideoPlayerArea()

androidMain（Android 特定）
  MainActivity.kt
  data/HttpClientFactory.android.kt  # OkHttp + CookieJar + ContentEncoding
  ui/detail/VideoPlayer.kt           # ExoPlayer 播放器组件（核心）
  ui/detail/VideoPlayerArea.android.kt  # actual 实现
```

### 数据流

```
搜索：SearchViewModel.search()
  → 并发调用所有 ParserRegistry.all() 中的 parser.search()
  → 结果合并展示

播放：DetailViewModel.selectEpisode()
  → parser.detail(id) → List<Route>（每条线路含集数列表）
  → parser.playInfo(playUrl) → PlayInfo.m3u8Url
  → VideoPlayer 用 ExoPlayer 播放 m3u8
```

### 添加新站点

1. 在 `commonMain/data/parsers/` 新建 `XxxParser.kt`，实现 `SiteParser` 接口
2. 在 `ParserRegistry.kt` 的 `parsers` 列表中注册

`SiteParser` 接口三个方法：
- `search(keyword)` → `List<SearchResult>`
- `detail(id)` → `List<Route>`（每条线路含 m3u8 直链或播放页 URL）
- `playInfo(playPageUrl)` → `PlayInfo`（m3u8 直链）

ModuParser 走 JSON API，detail() 直接返回 m3u8，playInfo() 透传即可。XiaobaoParser 走 HTML，detail() 返回播放页 URL，playInfo() 需再请求并解析 `var player_aaaa` JS 变量。

### expect/actual 模式

跨平台差异点：

| expect（commonMain） | androidMain | iosMain |
|---|---|---|
| `fun logD(tag, msg)` | `Log.e(tag, msg)` | `println()` |
| `fun createHttpClient()` | OkHttp + Cookie + Gzip | Darwin |
| `fun openPlayer(url, title)` | 已废弃（播放器嵌入详情页） | stub |
| `fun VideoPlayerArea(...)` | ExoPlayer 完整播放器 | 黑色占位 |

### 播放器（VideoPlayer.kt）

详情页顶部固定嵌入，布局：16:9 播放区域（全屏时 fillMaxSize）+ 下方可滚动的线路/集数选择。

手势：
- 单击：显示/隐藏控制栏（3秒自动隐藏）
- 横向滑动：快进快退（1px ≈ 300ms）
- 长按：临时 2× 速（5秒后自动恢复）

全屏：通过 `activity.requestedOrientation` + `WindowInsetsController` 控制，MainActivity 配置了 `configChanges` 防止 Activity 重建。

### 全面屏 / 沉浸式处理

**Android**

全屏时需同时做两件事（缺一不可）：
1. 切换横屏：`activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`
2. 隐藏系统栏（状态栏 + 导航栏）：
   ```kotlin
   val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
   insetsController.hide(WindowInsetsCompat.Type.systemBars())
   insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
   ```
3. 退出全屏时恢复：`SCREEN_ORIENTATION_UNSPECIFIED` + `insetsController.show(systemBars())`

`AndroidManifest.xml` 的 MainActivity 必须包含 `configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize"`，否则方向变化会重建 Activity 导致播放中断。

**iOS**

iOS 目前是占位实现（`VideoPlayerArea.ios.kt`）。实现时需要：

1. **隐藏状态栏**：`Info.plist` 设 `UIViewControllerBasedStatusBarAppearance = NO` + `UIStatusBarHidden = YES`，或通过 UIViewController 的 `prefersStatusBarHidden` 动态控制。
2. **横屏全屏**：调用 `UIDevice.current.setValue(UIInterfaceOrientation.landscapeRight.rawValue, forKey: "orientation")` 触发旋转（`Info.plist` 须允许 Landscape 方向）。
3. **隐藏 Home Indicator**：UIViewController `prefersHomeIndicatorAutoHidden = true`。
4. **Safe Area 适配**：全屏视频用 `edgesIgnoringSafeArea(.all)` 铺满，非全屏时遵从 safeArea 避免刘海遮挡。

iOS 全屏需通过 expect/actual 暴露 `fun enterFullscreen()` / `fun exitFullscreen()`，在 iosMain 里调用 UIKit API。

### 已知注意事项

- MIUI/HyperOS 过滤 `Log.d`，因此 Logger 使用 `Log.e`（Error 级别）
- 小宝影院（xiaobaotv.com）有 Cloudflare 防护，手机 IP 会被拦截，HTML 解析方案对该站不可用
- Ktor 请求需设置 `ContentEncoding { gzip(); deflate() }` 否则拿到的是压缩字节流
- 站点 JSON API 格式：多线路用 `$$$` 分隔，集数用 `#` 分隔，集数名和 URL 用 `$` 分隔

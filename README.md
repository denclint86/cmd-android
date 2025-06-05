# cmd-android：Android Shell 命令执行框架

本项目是基于 Kotlin 协程的 Android Shell 命令执行框架，支持 **User**、**Shizuku** 和 **Root** 三种权限模式，提供简洁、统一的 API，极大简化命令执行流程。无论是普通用户权限还是复杂的 Shizuku 回调处理，都能几行代码搞定！

```kotlin
val shell: Shell = ShizukuShell(context)
if (shell.isAvailable()) {
    val result = shell.exec("echo test", timeout = 20_000L)
    println("标准输出: ${result.stdout}")
    println("错误输出: ${result.stderr}")
    println("退出码: ${result.exitCode}")
}
```

## 核心亮点

- **多模式支持**：无缝支持 **User、Shizuku 和 Root** 三种权限模式，满足不同场景需求。
- **Shizuku API 封装**：本框架在内部处理了所有有关**授权、AIDL 实现、服务绑定**等的操作，只需实例化、执行命令即可
- **Kotlin 协程集成**：统一而简单的挂起式 API，告别繁琐。
- **统一接口**：`Shell` 接口抽象不同模式，提供一致、简洁的调用体验。 

![demo](https://github.com/niki914/cmd-android/blob/master/demo.gif)

[下载测载 demo](https://github.com/niki914/cmd-android/releases/tag/demo)

## 快速开始

### 1. 添加 maven

在 settings.gradle.kts 中添加 jitpack 官方 Maven

```kotlin
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
    	...
	    maven { url = uri("https://jitpack.io") }
	}
}
```

### 2. 添加依赖

在模块的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation("com.github.niki914:cmd-android:${latest_version}") // 查看本项目最新 release
}
```

> **提示**：想深入了解 Shizuku？查看 [Shizuku 官方文档](https://shizuku.rikka.app/) 或 [示例 Demo](https://github.com/RikkaApps/Shizuku-API/tree/master/demo)。

## 使用示例

### 执行简单命令

```kotlin
val shell: Shell = UserShell() // 或 ShizukuShell(context), RootShell()
if (shell.isAvailable()) {
    val result = shell.exec("pm list packages", timeout = 10_000L)
    if (result.isSuccess) {
        println("已安装应用: ${result.stdout}")
    } else {
        println("错误: ${result.stderr}, 退出码: ${result.exitCode}")
    }
}
```

### 处理复杂场景

```kotlin
val shell: Shell = ShizukuShell(context)
launch(Dispatchers.IO) {
    val result = shell.exec("dumpsys activity", timeout = 30_000L)
    withContext(Dispatchers.Main) {
    	if (result.isSuccess) {
        	toast("命令执行成功: ${result.stdout.take(100)}...")
        } else {
        	toast("命令失败: ${result.stderr}")
        }
    } 
}
```

## 已知问题

- **Root 模式**：依赖 `su` 命令，需确保设备已 Root 并可访问 `su`。
- **R8 混淆**：启用精简和混淆后，Shizuku 模块概率出现未知问题，表现为可以授权但命令不能被执行，需要在 `proguard-rules.pro` 中添加规则过滤:
- **不支持交互式命令调用**: 不支持运行如`su`等命令后进入的新的交互式命令状态的情况

```text
-keep class dev.rikka.shizuku.** { *; }

-keep class com.niki.** { *; }

-dontwarn dev.rikka.shizuku.**
-dontwarn com.niki.**
```

> 遇到问题？提交 [Issue](https://github.com/niki914/cmd-android/issues)。

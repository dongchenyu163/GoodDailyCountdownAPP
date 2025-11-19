# `moko-resources` 接入计划：实现 KMP 多语言支持

本文档旨在详细阐述如何将 `moko-resources` 集成到我们的 Kotlin Multiplatform 项目中，以实现高效、统一的多语言国际化（i18n）支持。

## 1. 添加 `moko-resources` Gradle 插件

首先，我们需要在 Gradle 构建配置中添加 `moko-resources` 插件。

- **`gradle/libs.versions.toml`**: 在版本目录中添加插件和库的版本。

  ```toml
  [versions]
  moko-resources = "0.24.0" # 建议使用最新稳定版

  [libraries]
  moko-resources = { module = "dev.icerock.moko:resources", version.ref = "moko-resources" }
  moko-resources-compose = { module = "dev.icerock.moko:resources-compose", version.ref = "moko-resources" }

  [plugins]
  moko-resources = { id = "dev.icerock.mobile.multiplatform-resources", version.ref = "moko-resources" }
  ```

- **根目录 `build.gradle.kts`**: 声明插件。

  ```kotlin
  plugins {
      // ... other plugins
      alias(libs.plugins.moko.resources) apply false
  }
  ```

- **`composeApp/build.gradle.kts`**: 应用插件。

  ```kotlin
  plugins {
      // ... other plugins
      alias(libs.plugins.moko.resources)
  }
  ```

## 2. 在 `sourceSets` 中添加依赖

在 `composeApp/build.gradle.kts` 文件中，为 `commonMain` 源集添加 `moko-resources` 的依赖项。

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // ...其他依赖
                api(libs.moko.resources)
                api(libs.moko.resources.compose) // 如果使用 Jetpack/JetBrains Compose
            }
        }
        // ...
    }
}
```

## 3. 创建字符串资源文件

`moko-resources` 遵循标准的 Android 资源目录结构。

1.  在 `composeApp/src/commonMain/` 目录下创建 `resources` 文件夹。
2.  在 `resources` 文件夹内，创建 `MR` 目录 (`composeApp/src/commonMain/resources/MR/`)。
3.  **默认语言（例如，英文）**:
    -   创建目录 `MR/strings/`。
    -   在该目录中创建 `strings.xml` 文件：
        ```xml
        <?xml version="1.0" encoding="UTF-8" ?>
        <resources>
            <string name="app_name">Countdown App</string>
            <string name="add_new_countdown">Add New Countdown</string>
        </resources>
        ```

4.  **其他语言（例如，中文）**:
    -   创建目录 `MR/values-zh/`。
    -   在该目录中创建 `strings.xml` 文件：
        ```xml
        <?xml version="1.0" encoding="UTF-8" ?>
        <resources>
            <string name="app_name">倒计时应用</string>
            <string name="add_new_countdown">添加新的倒计时</string>
        </resources>
        ```

## 4. 在共享代码中使用字符串

1.  **同步 Gradle**: 点击 "Sync" 按钮或通过命令行执行 Gradle 同步。这将触发 `moko-resources` 生成 `MR` 类，它包含了对所有资源的引用。

2.  **在 Composable 中使用**: 在 `commonMain` 的 Composable 函数中，通过 `stringResource` 函数和生成的 `MR` 对象来访问字符串。

    ```kotlin
    import dev.icerock.moko.resources.compose.stringResource
    import org.example.project.MR // 确保导入正确的 MR 类

    @Composable
    fun App() {
        MaterialTheme {
            // 示例：在 Text 组件中使用本地化字符串
            Text(text = stringResource(MR.strings.add_new_countdown))
        }
    }
    ```

## 5. 特定平台的配置

对于大多数基于 Compose 的项目，`moko-resources` 的配置是高度自动化的。

-   **Android**: 通常无需额外配置。插件会自动将 `MR` 资源链接到 Android 的资源系统。
-   **Desktop (JVM)**: 无需额外配置。
-   **iOS**: 对于纯 Compose UI，通常也无需手动初始化。如果遇到问题（例如，在混合 UIKit/SwiftUI 的场景中），请查阅 `moko-resources` 的官方文档以获取关于 `StringDesc.localeType` 或其他初始化步骤的最新指南。
-   **Web (Wasm/JS)**: 无需额外配置。

## 6. 构建与验证

1.  完成上述步骤后，执行 Gradle 同步。
2.  为目标平台（如 Desktop）构建项目，确保没有编译错误。
    ```shell
    ./gradlew :composeApp:jvmJar
    ```
3.  在不同语言环境的设备或模拟器上运行应用，验证 UI 是否正确显示了对应语言的字符串。

## 7. 待国际化（i18n）的文本资源

下表统计了需要在应用中进行多语言支持的文本。

| 位置 (File:Line)                   | 资源名称 (Resource Name)             | 中文文本 (Chinese)     | 英文文本 (English) |
|:---------------------------------|:---------------------------------|:-------------------| :--- |
| `App.kt:283`                     | `ok`                             | 知道了                | OK |
| `App.kt:284`                     | `reminder`                       | 提醒                 | Reminder |
| `App.kt:285`                     | `countdown_due_message`          | 《%s》的倒计时已经到期啦！     | The countdown for '%s' is over! |
| `App.kt:377`                     | `app_name`                       | 倒计时                | Countdown |
| `App.kt:388`                     | `search_countdown_placeholder`   | 搜索倒计时...           | Search countdowns... |
| `App.kt:417`                     | `no_results`                     | 无结果                | No Results |
| `App.kt:504`                     | `remaining_days`                 | 剩余 %d 天            | %d days remaining |
| `App.kt:598`                     | `delete`                         | 删除                 | Delete |
| `App.kt:680`                     | `ends_on_date`                   | 结束于 %d/%d/%d       | ends on %d/%d/%d |
| `App.kt:684`                     | `remaining_days_short`           | %dd                | %dd |
| `App.kt:752`                     | `edit`                           | 编辑                 | Edit |
| `CardDialogs.kt:65`              | `error_picking_image`            | 选择图片时发生错误          | Error picking image |
| `CardDialogs.kt:70`              | `no_image_picked_or_unsupported` | 未选择图片或当前平台暂不支持文件选择 | No image picked or file selection not supported |
| `CardDialogs.kt:76`              | `cannot_read_image`              | 无法读取所选图片，请尝试其他文件   | Cannot read the selected image, please try another file |
| `CardDialogs.kt:134`             | `edit_card`                      | 编辑卡片               | Edit Card |
| `CardDialogs.kt:134`             | `add_new_card`                   | 添加新卡片              | Add New Card |
| `CardDialogs.kt:141`             | `title`                          | 标题                 | Title |
| `CardDialogs.kt:148`             | `description`                    | 描述                 | Description |
| `CardDialogs.kt:155`             | `target_date`                    | 目标日期               | Target Date |
| `CardDialogs.kt:177`             | `remaining_days_label`           | 剩余天数               | Remaining Days |
| `CardDialogs.kt:160`             | `select_date`                    | 选择日期               | Select Date |
| `CardDialogs.kt:182`             | `select_icon`                    | 选择图标               | Select Icon |
| `CardDialogs.kt:202`             | `title_image`                    | 标题图片               | Title Image |
| `CardDialogs.kt:212`             | `picking_in_progress`            | 选择中...             | Picking... |
| `CardDialogs.kt:212`             | `select_file`                    | 选择文件               | Select File |
| `CardDialogs.kt:218`             | `edit_image_size`                | 编辑图片大小             | Edit Image Size |
| `CardDialogs.kt:224`             | `clear_image`                    | 清除图片               | Clear Image |
| `CardDialogs.kt:236`             | `current_image_id`               | 当前图片ID: %s         | Current image ID: %s |
| `CardDialogs.kt:245`             | `cancel`                         | 取消                 | Cancel |
| `CardDialogs.kt:276`             | `confirm`                        | 确认                 | Confirm |
| `ImageOffsetEditorDialog.kt:84`  | `drag_image_to_adjust`           | 拖拽图片以调整显示效果        | Drag image to adjust the display effect |
| `ImageOffsetEditorDialog.kt:89`  | `adjust_view_selection`          | 调整视图选择             | Adjust view selection |
| `TitleImageModels.kt:4`          | `view_card`                      | 卡片                 | Card |
| `TitleImageModels.kt:3`          | `view_list`                      | 列表                 | List |
| `TitleImageModels.kt:2`          | `view_grid`                      | 网格                 | Grid |
| `ImageOffsetEditorDialog.kt:164` | `reset`                          | 重置                 | Reset |
| `ImageOffsetEditorDialog.kt:167` | `apply`                          | 应用                 | Apply |
| `Onboarding.kt:21`               | `welcome_to_app`                 | 欢迎使用倒计时应用          | Welcome to Countdown App |
| `Onboarding.kt:23`               | `app_tagline`                    | 管理你的重要日程，专注每一天。    | Manage your important dates and focus on every day. |
| `Onboarding.kt:25`               | `next_step`                      | 下一步                | Next |
| `Onboarding.kt:41`               | `permission_request_message`     | 需要通知权限以按时提醒。       | Notification permission is required for timely reminders. |
| `Onboarding.kt:43`               | `grant_permission`               | 允许                 | Allow |
| `SettingsScreen.kt:28`           | `settings`                       | 设置                 | Settings |
| `SettingsScreen.kt:52`           | `countdown_display_style`        | 倒计时显示样式            | Countdown Display Style |
| `SettingsScreen.kt:55`           | `display_style_grid`             | 网格视图               | Grid View |
| `SettingsScreen.kt:56`           | `display_style_grid_description` | 一目了然地查看更多倒计时。      | See more countdowns at a glance. |
| `SettingsScreen.kt:61`           | `display_style_list`             | 列表视图               | List View |
| `SettingsScreen.kt:62`           | `display_style_list_description` | 适用于许多项目的紧凑视图。      | A compact view for many items. |
| `SettingsScreen.kt:67`           | `display_style_card`             | 卡片视图               | Card View |
| `SettingsScreen.kt:68`           | `display_style_card_description` | 为每个项目提供详细的全宽卡片。    | A detailed, full-width card for each item. |

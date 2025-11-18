# 项目概览 (Project Overview)

这是一个使用 Kotlin Multiplatform 和 Compose Multiplatform 构建的跨平台倒计时应用。该项目旨在为用户提供一个简单易用的工具来创建和管理各种事件的倒计时，例如生日、节日、项目截止日期等。

主要技术栈：
- **Kotlin Multiplatform**: 用于共享跨平台业务逻辑。
- **Compose Multiplatform**: 用于构建 Android, iOS, Web, 和 Desktop 的用户界面。
- **Kotlinx Serialization**: 用于数据的序列化和反序列化。
- **Kotlinx Datetime**: 用于处理日期和时间。

项目结构：
- `composeApp`: 包含所有 Compose Multiplatform 共享代码和各平台特定代码。
  - `commonMain`: 所有平台共享的代码，如 UI 组件、数据模型和业务逻辑。
  - `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain`: 各平台特定的代码。
- `iosApp`: iOS 应用的入口点和 SwiftUI 代码。

## 构建和运行 (Building and Running)

### Android 应用
- 构建:
```shell
# macOS/Linux
./gradlew :composeApp:assembleDebug
# Windows
.\gradlew.bat :composeApp:assembleDebug
```
- 运行:
```shell
# macOS/Linux
./gradlew :composeApp:installDebug
# Windows
.\gradlew.bat :composeApp:installDebug
```

### Desktop (JVM) 应用
- 构建:
```shell
# macOS/Linux
./gradlew :composeApp:jvmJar
# Windows
.\gradlew.bat :composeApp:jvmJar
```
- 运行:
```shell
# macOS/Linux
./gradlew :composeApp:run
# Windows
.\gradlew.bat :composeApp:run
```

### Web 应用
- 构建:
```shell
# Wasm target (更快，适用于现代浏览器)
# macOS/Linux
./gradlew :composeApp:wasmJsBrowserDistribution
# Windows
.\gradlew.bat :composeApp:wasmJsBrowserDistribution

# JS target (较慢，支持旧版浏览器)
# macOS/Linux
./gradlew :composeApp:jsBrowserDistribution
# Windows
.\gradlew.bat :composeApp:jsBrowserDistribution
```
- 运行:
```shell
# Wasm target (更快，适用于现代浏览器)
# macOS/Linux
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
# Windows
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun

# JS target (较慢，支持旧版浏览器)
# macOS/Linux
./gradlew :composeApp:jsBrowserDevelopmentRun
# Windows
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

### iOS 应用
- 构建和运行: 不在dongchenyu165的机器上开发，忽略。

## 开发约定 (Development Conventions)

- **代码风格**: 遵循 Kotlin 官方编码规范。修改的时候确保没有额外的空白行或多余的缩进，因为我发现你的修改的每一行之后都有空白行，这很麻烦，请删除。
- **修改、功能编译的验证**: **每个**新功能或修复（即：你每次修改前做的TODO List的每一项）都需要进行编译，确保没有编译错误（无需执行APP）。然后在进行Git提交。
- **架构**: 采用简单的状态驱动架构，使用 `@Composable` 函数和 `remember` 状态管理。
- **数据存储**: 使用 `kotlinx-serialization` 将数据序列化为 JSON 并保存到平台特定的文件系统中。
- **UI 组件**: 使用 Material Design 3 (Material 3) 组件构建 UI。
- **平台特定代码**: 使用 `expect`/`actual` 机制处理平台特定的功能，如文件读写和提醒通知。
- **Git提交**: **每个**独立的功能或修复（即：你每次修改前做的TODO List的每一项）完成后，如果没有编译错误的话，就着手Git提交，
- **提交信息格式**: 提交信息的格式使用Conventional格式，需要写明相关模块即：变更种类(相关模块): 提交信息。

- **开发、修复的流程**: 
  1. **制定计划**：创建TODO List，明确需要完成的任务项
  2. **选择任务**：按优先级或依赖关系，按顺序挑选一个任务开始执行
  3. **开发/修复/修改**：实现被选中的任务（注意确保没有额外的空白行或多余的缩进）
  4. **编译验证**：这个时候的验证只需要编译Desktop平台的，其他平台的编译不需要验证。
     - 如果存在编译错误，则读取错误信息，返回第3步继续修改
     - 如果没有编译错误，则进入下一步
  5. **进行Git提交**：使用Conventional格式提交信息
  6. **循环或结束初步修改**：
     - 如果还有任务未执行，则返回第2步继续
     - 如果所有任务完成，则进行步骤7
  7. **相关平台编译验证**：
     - 首先确定上述任务的编辑受影响的平台。仅针对需修改的平台进行编译错误的验证
     - 如果某平台存在编译错误，则仅修改对应平台的代码，直到编译成功。
     - 编译成功后，进行Git提交
     - 如果所有平台编译成功，则结束整个流程
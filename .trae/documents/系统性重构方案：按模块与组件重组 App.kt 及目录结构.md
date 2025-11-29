## 目标与原则

* 按模块与组件重组源码，严格遵循单一职责与高内聚低耦合。

* 采用小粒度、可追踪的原子提交；每一步完成后立即进行 JVM 编译验证与代码审查。

* 保持 UI 文本从 `MR.strings`、图片/动画从 `MR.images`/`Res.files` 读取。

## 执行分阶段计划（每一步均可独立提交）

### 阶段 A：目录与包结构初始化（不移动文件，仅创建包占位）

* 在 `com.dlx.smartalarm.demo` 下新增：`core/`、`components/`、`features/` 三级包。

* 子包：

  * `components/appbar`, `components/card`, `components/menu`, `components/scroll`, `components/image`, `components/favorite`

  * `features/app/navigation`, `features/main/components`, `features/main/logic`, `features/settings/logic`, `features/onboarding`, `features/cards/logic`, `features/cards/dialogs`

* 不改动现有类；仅建立空占位文件（如 `package-info.kt`），确保编译通过。

### 阶段 B：从 App.kt 进行首次瘦身（极小改动）

1. 提取 `enum class Screen` 到 `features/app/navigation/Screen.kt`。
2. 提取 `FavoriteButton(...)` 到 `components/favorite/FavoriteButton.kt`。
3. 更新 `App.kt` 引用与导入，保持行为一致。
4. 运行 `./gradlew :composeApp:jvmJar`；审查后提交。

### 阶段 C：拆分 MainScreen 入口与通用逻辑（仍保持在同一包内）

1. 在 `features/main/logic/SearchHighlight.kt` 中提取 `highlight(...)`。
2. 在 `features/main/logic/FilterMenuState.kt` 中安置 `FilterMenuState`/`applyFilterSelection`（从 `FilterMenuLogic.kt` 平移）。
3. `App.kt`/`MainScreen` 侧仅通过导入使用。
4. 编译验证并提交。

### 阶段 D：拆分 MainScreen 的内容区域（控制文件行数）

1. 将 `LazyVerticalGrid` 相关 UI 提取为 `features/main/components/MainGridContent.kt`（参数为已过滤列表及回调）。
2. 将 `LazyColumn` 相关 UI 提取为 `features/main/components/MainListContent.kt`。
3. `MainScreen` 成为容器，仅负责：顶部栏、FAB、根据 `DisplayStyle` 切换 `Grid` 或 `List`。
4. 编译验证并提交。

### 阶段 E：业务逻辑下沉（数据一致性）

1. 将 `validateAndFixCardData(...)` 移动到 `features/cards/logic/CardIntegrity.kt`。
2. 保持 `App.kt` 中的加载流程，改为调用新位置的函数。
3. 编译验证并提交。

### 阶段 F：对话框与菜单归类

1. 迁移 `CardDialogs.kt` 至 `features/cards/dialogs/`；保持类名与对外 API 不变。
2. 迁移 `ContextMenu.kt` 至 `components/menu/`。
3. 编译验证并提交。

### 阶段 G：通用组件归位

1. 迁移 `RoundCornerBox.kt`（`CountdownCard`/`AnimatedCountdownCard`）至 `components/card/`。
2. 迁移 `ScrollbarComponent.kt` 至 `components/scroll/`。
3. 迁移 `TitleImageRenderer.kt` 至 `components/image/`；与 `TitleImageModels.kt`、`TitleImageCache.kt` 保持同包或拆分为 `image/logic` 与 `image/model`（按编译依赖调整）。
4. 编译验证并提交。

### 阶段 H：设置与引导模块归位

1. 迁移 `SettingsScreen.kt` 至 `features/settings/`；`AppSettingsManager.kt`/`AppSettings.kt` 至 `features/settings/logic/`。
2. 迁移 `Onboarding.kt` 至 `features/onboarding/`。
3. 迁移 `LocaleManager.kt` 至 `features/settings/logic/`（或 `core/utils`，若被多处复用）。
4. 编译验证并提交。

### 阶段 I：标签与收藏相关归位

1. 迁移 `TagModels.kt` 至 `core/model/`。
2. 迁移 `TagRepository.kt` 至 `features/cards/logic/`（或 `core/repository`，若跨模块广泛使用）。
3. 迁移 `TagMultiselect.kt` 至 `components/menu/` 或 `components/tag/`（按复用频率）。
4. 编译验证并提交。

### 阶段 J：Platform 与 DesignSystem 收尾

1. 迁移 `Platform.kt` 至 `core/platform/`。
2. 迁移 `SimpleTheme.kt` 至 `core/designsystem/`；保留 `AppTheme` 入口。
3. 编译验证并提交。

### 阶段 K：最终清理

* 检查所有 `package` 声明与导入是否正确；删除空包占位。

* 运行 `./gradlew :composeApp:assembleDebug` 做 Android 校验；记录耗时对比。

* 提交最终变更。

## 文件迁移映射（现状 → 新位置）

* `App.kt` → `features/app/App.kt`（仅入口与导航）

* `App.kt::Screen` → `features/app/navigation/Screen.kt`

* `App.kt::FavoriteButton` → `components/favorite/FavoriteButton.kt`

* `App.kt::MainScreen` → `features/main/MainScreen.kt`

* `App.kt::Grid/List 内容` → `features/main/components/MainGridContent.kt` / `MainListContent.kt`

* `CardDialogs.kt` → `features/cards/dialogs/`

* `FilterMenuLogic.kt` → `features/main/logic/FilterMenuState.kt`

* `ContextMenu.kt` → `components/menu/AppContextMenu.kt`

* `ScrollbarComponent.kt` → `components/scroll/VerticalScrollbar.kt`

* `RoundCornerBox.kt` → `components/card/CountdownCard.kt`

* `TitleImageModels/Renderer/Cache/Processor/Storage` → `components/image/`（视 UI/数据边界可拆分子包）

* `AppSettings/AppSettingsManager/LocaleManager` → `features/settings/`（`logic/` 子包）

* `TagModels/TagRepository` → `core/model/` & `features/cards/logic/`

* `CountdownReminderObserver/ReminderHandler` → `features/cards/logic/`

## 规则校验（符合项目 rules）

* 所有字符串通过 `MR.strings`；不引入新的硬编码文本。

* 图片与动画资源位于 `MR.images`/`Res.files`；仅通过资源 API 访问。

* 每一步均执行 JVM 编译验证，审查通过后再进行 Git 提交；最终进行 Android 构建验证与性能记录。

## 验证清单（每次提交后的检查）

* `./gradlew :composeApp:jvmJar` 成功；代码审查无明显缺陷。

* 手动 UI 冒烟：主页列表/网格、搜索、筛选、收藏、上下文菜单、左滑删除、弹窗/提醒、设置、引导。

* 资源访问正常：`MR.strings` 文本加载正确；图片/动画正常渲染。

## 提交规范

* 使用 `feature/refactor-modularization` 开发分支。

* 每阶段结束后：添加简短提交信息，附带改动范围说明（例如：`refactor(main): extract grid/list content`）。

* 所有提交在审查通过后由版本管理代理进行 `git add && git commit`；在最终平台验证通过后询问是否 `git push`。


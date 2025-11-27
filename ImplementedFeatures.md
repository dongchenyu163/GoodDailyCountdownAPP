# 核心功能模块

## 1. 数据模型 (CardData)
- **CardData**: 主要数据类，包含倒计时卡片的所有信息：
    - `id`: 卡片唯一标识符
    - `title`: 倒计时标题
    - `date`: 目标日期（字符串格式）
    - `remainingDays`: 剩余天数（长期保留，动态计算）
    - `reminderSent`: 提醒发送状态
    - `description`: 倒计时描述（可选）
    - `icon`: 倒计时图标（emoji 或符号）
    - `titleImage`: 标题图片信息（可选，包含显示参数）
    - `isFavorite`: 收藏状态（支持按收藏筛选）

## 2. 数据持久化 (CardDataStorage)
- 使用 `kotlinx.serialization` 将 `CardData` 列表序列化为 JSON
- 跨平台文件操作，将数据保存到各平台特定位置：
    - Android: 应用内部存储
    - JVM: 用户主目录下的 `.smartalarm` 目录
    - iOS: 应用文档目录（待完善）
- 自动保存机制，监听数据变化并自动写入文件

## 3. UI 组件系统
- **倒计时卡片展示**: 支持三种显示样式：
    - 列表视图 (List View): 紧凑行样式，水平渐变背景
    - 网格视图 (Grid View): 2列网格，卡片风格，垂直渐变背景
    - 卡片视图 (Card View): 大卡片样式，垂直渐变背景
- **标题图片渲染**: 支持自定义图片背景，具有缩放、旋转、偏移等参数控制
- **滚动条组件**: 为长列表提供美观的滚动指示器
- **收藏功能**: 使用 Lottie 动画的收藏按钮，支持按收藏状态筛选

## 4. 交互功能
- **添加/编辑卡片**: 通过对话框添加新倒计时，支持标题、描述、日期、图标、自定义图片等
- **删除卡片**: 左滑删除操作，带视觉反馈动画
- **搜索功能**: 支持按标题和描述搜索，逗号分词实现多关键词"或"搜索
- **筛选功能**: 按收藏状态筛选倒计时卡片
- **上下文菜单**: 长按或点击菜单按钮显示编辑/删除选项
- **日期选择器**: 内置日期选择对话框，支持手动输入剩余天数

## 5. 提醒系统 (CountdownReminderObserver)
- **实时监控**: 监控倒计时状态变化，自动计算剩余天数
- **到期提醒**: 当倒计时到期时，自动触发提醒对话框和系统通知
- **状态管理**: 管理提醒发送状态，避免重复提醒
- **日期更新**: 支持动态更新剩余天数，确保数据准确

## 6. 设置与国际化
- **显示样式**: 支持切换列表、网格、卡片三种显示模式
- **多语言**: 支持中文、英文、日文三种语言
- **主题适配**: 使用 Material Design 3 组件，自动适配系统主题

## 7. 图片管理
- **自定义图片**: 支持为倒计时卡片设置自定义图片背景
- **图片编辑器**: 内置图片偏移、缩放、旋转编辑器
- **缓存管理**: 图片缓存系统，优化加载性能
- **存储管理**: 图片文件按 UUID 存储，自动清理未使用图片

## 8. 跨平台兼容性
- **平台抽象**: 使用 `expect`/`actual` 机制处理平台特定功能
- **文件操作**: 统一的跨平台文件读写接口
- **UI适配**: 根据平台特性适配字体和交互方式
- **通知系统**: 平台特定的通知实现（通过 `ReminderHandler` 抽象）

# 源代码文件对应关系

## 主要功能组件
- **App.kt**: 应用主入口，包含整体UI框架、导航、主屏幕和状态管理
- **CardDataManager.kt**: 数据模型定义(CardData类)、数据存储逻辑(CardDataStorage)
- **RoundCornerBox.kt**: 倒计时卡片UI组件实现(CountdownCard、AnimatedCountdownCard)
- **CardDialogs.kt**: 卡片对话框相关组件(AddCardDialog, EditCardDialog, DatePickerDialog)
- **SettingsScreen.kt**: 设置界面实现，包含显示样式和语言设置
- **CountdownReminderObserver.kt**: 倒计时提醒系统，监控倒计时状态和触发提醒
- **ReminderHandler.kt**: 提醒处理接口，跨平台通知实现抽象

## 图片与媒体处理
- **TitleImageModels.kt**: 图片相关数据模型定义(TitleImageInfo, TitleImageDisplayParameters等)
- **TitleImageRenderer.kt**: 图片渲染逻辑，实现图片的缩放、旋转、偏移等功能
- **TitleImageStorage.kt**: 图片存储管理，处理图片文件的保存和删除
- **TitleImageCache.kt**: 图片缓存系统，优化图片加载性能
- **ImageOffsetEditorDialog.kt**: 图片编辑对话框，提供图片参数调整界面

## UI组件与工具
- **SimpleTheme.kt**: 应用主题定义
- **ScrollbarComponent.kt**: 自定义滚动条组件
- **ContextMenu.kt**: 上下文菜单组件
- **Platform.kt**: 跨平台功能抽象接口
- **DisplayStyle.kt**: 显示样式枚举定义
- **LocaleManager.kt**: 多语言本地化管理

## 辅助功能
- **AppSettings.kt**: 应用设置数据模型
- **AppSettingsManager.kt**: 应用设置管理，包括持久化
- **Onboarding.kt**: 新用户引导界面(欢迎页和权限请求)
- **TitleImageProcessor.kt**: 图片处理工具函数

# 资源文件说明

## 图形资源 (drawable)
- **FilterIcon.svg**: 筛选功能的SVG图标，用于主界面筛选菜单

## 动画资源 (files)
- **FavIcon.lottie**: Lottie动画文件，用于收藏按钮的动画效果

## 字体资源 (font)
- **NotoColorEmoji-Regular.ttf**: 彩色表情符号字体，用于显示emoji图标
- **NotoEmoji-VariableFont_wght.ttf**: 可变权重的emoji字体
- **NotoSansSC.ttf**: 思源黑体简体中文版，用于中文文本显示

## 多语言资源 (moko-resources)
- **base/strings.xml**: 默认语言字符串资源
- **zh/strings.xml**: 中文字符串资源
- **ja/strings.xml**: 日文字符串资源

## 跨平台资源 (MR.strings)
- 通过Moko-resources库自动生成的类型安全资源访问器，用于访问字符串资源

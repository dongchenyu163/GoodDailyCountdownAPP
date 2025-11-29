## 功能目标与范围
- 为每张倒计时卡片添加可多选的 Tags（一个卡片可有多个 Tag）。
- 在卡片编辑弹窗中实现类似 Notion 的 Multiselect 编辑体验（含搜索、创建、新建预览、胶囊显示、删除 X、右侧更多弹窗编辑名称/颜色/删除）。
- 所有已存在的 Tag 在顶栏筛选菜单中列出，可按 Tag 过滤；保留并兼容现有“已收藏”筛选。
- 在不破坏现有功能的前提下，将“收藏”状态与一个保留的特殊 Tag（如 Favorite）打通，按你的要求保持原有收藏按钮的编辑方式不变。

## 数据模型
- 更新 `CardDataManager.kt`（CardData 定义）
  - 在 `CardData` 中新增字段：`tags: List<String> = emptyList()`（存储 Tag 的 id 列表），保留现有 `isFavorite: Boolean` 以兼容。
- 新增 `Tag` 模型（commonMain）：
  - `data class Tag(val id: String, val name: String, val color: TagColor)`。
  - `enum class TagColor { Default, Gray, Brown, Orange, Yellow, Green, Blue, Purple, Pink, Red }`。
- 约束：Tag 以 `id` 引用，重命名只改 `name`；删除 Tag 时需要清理所有卡片中的引用。

## 存储与共享
- 新增 `TagRepository`（commonMain）：
  - `loadTags()/saveTags()`：使用与 `CardDataStorage` 相同的 JSON 持久化机制（文件名如 `tags.json`），平台写读沿用当前架构。
  - 提供 `getAll()/add(name,color)/update(id,name,color)/delete(id)` 操作；统一去重与大小写规范。
  - 在 App 启动时加载并持有到状态（例如在 `App.kt` 顶层通过 `remember { TagRepository.loadTags() }`）。
- 特殊 Tag：`Favorite`（保留 id，例如 `__favorite__`），首次运行若不存在则自动创建；供收藏映射使用。

## 兼容与迁移策略
- 加载卡片时的轻量迁移：
  - 若 `card.isFavorite == true` 且 `Favorite` Tag 不在 `card.tags` 中，则追加 `__favorite__`。
- 收藏按钮行为（`App.kt:863–896` 附近 `FavoriteButton`）：
  - 原按钮继续存在；切换收藏时同时：
    - 更新 `card.isFavorite` 标志位（保持兼容）。
    - 在 `card.tags` 中添加/移除 `__favorite__`。
- 保存逻辑不变（沿用现有 `CardDataStorage`）。

## 编辑 UI（CardDialogs.kt 集成）
- 插入部位：`CardDialog(...)` 中，和 `description`/`icon` 同级的输入区块下方。
- 组件：`TagMultiselect(
    allTags: List<Tag>,
    selected: List<String>,
    onChange: (List<String>) -> Unit,
    onRequestEdit(tag: Tag) -> Unit,
    onCreate(name: String) -> Unit
)`
- 交互与视觉（参考 Notion）：
  1) 初始态：左侧标签“Tags”与图标；右侧显示灰色“Empty”。
  2) 编辑态：点击激活，顶部输入框显示占位“Search for an option...”；下方列出现有标签选项；每项左侧胶囊，右侧“更多”图标。
  3) 已选显示：已选标签以胶囊 pill 形式在输入框内展示，右侧小“X”用于移除；输入框保持可输入以继续添加。
  4) 创建新标签：输入框无匹配时，下拉底部显示“Create [输入文本]”预览（按当前选中的色盘颜色即时预览）。
  5) 更多弹窗：靠近标签选项弹出悬浮设置框，包含重命名、删除、颜色选择区（列出 10 色，当前色显示对勾）。
- 颜色：基于 `TagColor` 映射到 `MaterialTheme.colorScheme` 的具体色值；胶囊背景支持半透明/实体两种（遵循当前主题暗色/亮色）。
- 文案与 i18n：全部文案通过 `moko-resources`（新增 `tags_label`, `empty`, `search_placeholder`, `create_prefix`, `delete`, `colors` 等）。

## 筛选菜单（App.kt 顶栏）
- 在现有筛选 `DropdownMenu` 中，保持“已收藏”和“全部”项，并追加所有 `Tag` 列表项：
  - 点击 Tag 项后过滤逻辑仅显示包含该 Tag 的卡片；菜单点击不自动关闭，支持连续切换（维持当前改动策略）。
  - 当前保留“单选 Tag”过滤（一次选择一个 Tag）；后续如需“多选 Tag 过滤”可以 OR/AND 策略扩展。
- 视觉指示：在选中的项右侧显示“✓”；对 Tag 胶囊可复用 `TagColor` 样式，提升识别度。

## 过滤逻辑
- 在 `MainScreen` 的 `filtered` 计算中加入 Tag 过滤：
  - `filterFavorites == true` -> 保持现有逻辑（或映射为 `__favorite__` Tag 命中）。
  - `activeTagId != null` -> `card.tags.contains(activeTagId)`。
- 顺序：维持原排序规则（收藏优先、剩余天数升序），只改变筛选集合。

## 边界处理
- 删除 Tag：
  - 通过弹窗 `Delete` 删除时，同时清理所有 `CardData.tags` 中该 id；给出轻量提示。
- 重命名 Tag：
  - 只影响 `Tag.name`；因为卡片引用的是 id，不需要批量更新卡片。
- 创建重名：
  - 默认不允许同名（忽略大小写）；若需要允许，用 name+color 组合键去重（本期设为不允许）。

## 测试计划（commonTest）
- `TagRepositoryTest`：加载/新增/重命名/删除的持久化正确性。
- `CardFavoriteTagMappingTest`：收藏切换时 `isFavorite` 与 `__favorite__` Tag 映射一致。
- `TagFilteringTest`：为卡片添加多个 Tag 后，选择某 Tag 能正确过滤集合；与搜索 tokens 联合场景。
- `TagCreateAndEditUITest`（纯逻辑/状态单元）：输入匹配、创建预览、胶囊添加/移除、更多弹窗的状态流；Compose UI 仓内暂以状态建模单测为主。

## 性能与 UX
- 大量 Tag 时列表虚拟化：Compose `LazyColumn` 渲染下拉列表。
- 输入框去抖：300ms 去抖过滤，以减少重组频率。
- 色值缓存：`remember` 缓存 `TagColor -> Color` 映射。

## 交付与版本控制
- 分支：`feature/tags-multiselect`（从当前 `feature/persistent-filter-menu` 派生）。
- 提交拆分：
  1) `feat(tags-model):` 数据模型与仓库 + 迁移逻辑。
  2) `feat(tag-multiselect-ui):` 编辑控件 + i18n。
  3) `feat(filter-menu-tags):` 顶栏筛选集成与逻辑。
  4) `test(tags):` 单元测试覆盖。
- 不提交构建产物；保持 `moko-resources` 结构一致；通过 JDK 17 构建验证前再推送。

## 受影响文件（参考定位）
- `composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/CardDataManager.kt`（CardData 与存储）
- `composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/CardDialogs.kt`（插入 TagMultiselect）
- `composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/App.kt:421–452`（筛选菜单与状态）
- `composeApp/src/commonMain/moko-resources/{base,zh,ja}/strings.xml`（新增文案）
- 新增：`TagRepository.kt`、`TagModels.kt`、`TagMultiselect.kt`、`tests/...`
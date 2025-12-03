# 合并冲突修复计划：解决新旧结构混合问题

## 问题背景

在 commit `a8efaca3` 中，将 master 分支（从 `5cedec19` 到 `808bd6b7` 的开发内容）合并到 `ai_dev_card-dialogs` 分支时，产生了结构性冲突：

1. **时间线问题**：`5cedec19` 是在模块化重构之前的提交
2. **结构冲突**：朋友开发的功能使用了旧的文件结构，而我们已经完成了模块化重构
3. **导入混乱**：新模块化结构与旧结构混合，导致导入路径不一致

## 当前状态分析

### 1. 文件结构混乱
- **新结构**：`components/`, `core/`, `features/` 目录已建立
- **旧文件残留**：根目录下仍有大量未移动的文件：
  - `AppSettings.kt`, `AppSettingsManager.kt`
  - `TagModels.kt`, `TagMultiselect.kt`, `TagRepository.kt`
  - `TitleImageCache.kt`, `TitleImageModels.kt`, `TitleImageProcessor.kt`, `TitleImageRenderer.kt`
  - `SettingsScreen.kt`, `Onboarding.kt`, `ContextMenu.kt`, `SimpleTheme.kt`
  - `LocaleManager.kt`, `Platform.kt`
  - `ReminderHandler.kt`, `CountdownReminderObserver.kt`
  - `DisplayStyle.kt`, `ImageOffsetEditorDialog.kt`

### 2. 导入路径问题
发现以下导入问题：
- `MainGridContent.kt`: `import com.dlx.smartalarm.demo.TagRepository`
- `CardDialogs.kt`: 
  - `import com.dlx.smartalarm.demo.TagRepository`
  - `import com.dlx.smartalarm.demo.TagMultiselect`
  - `import com.dlx.smartalarm.demo.ImageOffsetEditorDialog`

### 3. 新功能模块
朋友开发的 reminder 功能：
- `reminder/CardNotificationScheduler.kt`
- `reminder/ReminderTimeCalculator.kt`
- 相关代码已集成到 `App.kt` 中

## 修复计划

### 阶段 1：清理和移动剩余文件

#### 1.1 移动核心文件到正确位置
```
AppSettings.kt          → features/settings/logic/
AppSettingsManager.kt   → features/settings/logic/
LocaleManager.kt        → features/settings/logic/
Platform.kt             → core/platform/
```

#### 1.2 移动组件文件到正确位置
```
TagModels.kt            → core/model/
TagMultiselect.kt       → components/menu/
TagRepository.kt        → features/cards/logic/
TitleImageCache.kt      → components/image/
TitleImageModels.kt     → components/image/
TitleImageRenderer.kt   → components/image/
ImageOffsetEditorDialog.kt → components/image/
```

#### 1.3 移动UI组件到正确位置
```
SettingsScreen.kt       → features/settings/
Onboarding.kt           → features/onboarding/
ContextMenu.kt          → components/menu/
SimpleTheme.kt          → core/designsystem/
```

#### 1.4 移动逻辑文件到正确位置
```
ReminderHandler.kt      → core/reminder/
CountdownReminderObserver.kt → core/reminder/
DisplayStyle.kt         → core/model/
```

### 阶段 2：修复导入路径

#### 2.1 更新所有导入语句
- 将 `import com.dlx.smartalarm.demo.TagRepository` 改为 `import com.dlx.smartalarm.demo.features.cards.logic.TagRepository`
- 将 `import com.dlx.smartalarm.demo.TagMultiselect` 改为 `import com.dlx.smartalarm.demo.components.menu.TagMultiselect`
- 将 `import com.dlx.smartalarm.demo.ImageOffsetEditorDialog` 改为 `import com.dlx.smartalarm.demo.components.image.ImageOffsetEditorDialog`
- 其他类似导入路径的修复

#### 2.2 检查并修复 App.kt 中的导入
- 更新所有移动文件的导入路径
- 确保新集成功能（reminder）的导入正确

### 阶段 3：验证和测试

#### 3.1 编译验证
- 逐个平台编译验证（jvm, android）
- 确保没有导入错误和编译错误

#### 3.2 功能验证
- 验证朋友开发的 reminder 功能正常工作
- 验证所有现有功能未受影响
- 验证 UI 组件正常显示

#### 3.3 结构验证
- 确认根目录下不再有应该移动的文件
- 确认所有文件都在正确的模块位置
- 确认 package-info.kt 文件正确引用

### 阶段 4：清理和优化

#### 4.1 删除重复文件
- 确认没有重复的文件存在
- 清理可能的空目录

#### 4.2 更新 package-info.kt
- 确保所有模块的 package-info.kt 文件正确
- 确保内部对象正确导出

#### 4.3 文档更新
- 更新相关文档以反映新的文件结构
- 确保开发约定文档与实际结构一致

## 执行顺序

1. **创建备份分支**：`git checkout -b fix/merge-conflicts`
2. **按阶段执行**：严格按照上述 4 个阶段执行
3. **每个阶段提交**：每个阶段完成后进行 git 提交，确保可回滚
4. **最终验证**：完成所有阶段后进行全面测试

## 风险评估

### 高风险
- 导入路径修复可能遗漏某些文件
- 移动文件可能影响依赖关系

### 中风险
- 新功能（reminder）可能在结构变更后出现问题
- 某些平台特定代码可能需要额外调整

### 缓解措施
- 每个阶段完成后立即编译验证
- 保留详细的修改记录
- 如遇问题可回滚到上一个稳定提交

## 预期结果

完成后将实现：
1. **统一的文件结构**：所有文件按照模块化架构正确放置
2. **清晰的导入路径**：所有导入语句指向正确的模块位置
3. **功能完整性**：朋友开发的 reminder 功能正常工作
4. **代码一致性**：符合项目的模块化开发约定

## 时间预估

- 阶段 1：30-45 分钟（移动文件）
- 阶段 2：45-60 分钟（修复导入）
- 阶段 3：30-45 分钟（验证测试）
- 阶段 4：15-30 分钟（清理优化）

**总计：约 2-3 小时**
# 合并冲突修复计划（修正版）：删除旧结构文件并修复导入

## 问题背景

在 commit `a8efaca3` 中，将 master 分支（从 `5cedec19` 到 `808bd6b7` 的开发内容）合并到 `ai_dev_card-dialogs` 分支时：

1. **重构已完成**：在 `5f1a078` 时我们已经完成了模块化重构
2. **朋友开发基于旧结构**：朋友的开发是基于旧的根目录文件结构
3. **合并带回旧文件**：合并时将朋友修改的旧结构文件重新带回了根目录

## 核心问题

朋友开发时操作的是**旧结构的文件**，而这些文件在我们重构时已经移动到了新的模块位置。合并后：

- **新模块文件**：已存在于 `components/`, `core/`, `features/` 目录
- **旧结构文件**：被合并重新带回根目录（朋友修改过的版本）
- **导入冲突**：朋友代码中的导入指向根目录，但实际应指向新模块

## 正确的修复策略

### 原则
1. **删除旧结构文件** - 朋友修改的文件应该与我们的新模块文件合并，而不是保留
2. **保留新模块文件** - 我们重构后的模块结构是正确的
3. **修复导入路径** - 让朋友的代码使用新的模块路径

### 需要处理的文件

#### 1. 朋友修改过的根目录文件（需要删除，内容已合并到新模块）
```
根目录文件 → 新模块位置（已存在）
CardDialogs.kt → features/cards/dialogs/CardDialogs.kt
```

#### 2. 朋友新增的 reminder 功能文件（需要保留，但修复导入）
```
reminder/ 目录（新功能，保留但需修复导入）
├── CardNotificationScheduler.kt
├── ReminderTimeCalculator.kt
└── 各平台实现文件
```

#### 3. 朋友修改的 App.kt（需要修复导入路径）
- 修复其中对根目录文件的导入，改为指向新模块

## 修复计划

### 阶段 1：分析和备份
1. 对比根目录文件与新模块文件的差异
2. 确保朋友的功能性修改不会丢失
3. 创建备份分支

### 阶段 2：安全移动重复文件到暂存区
移动以下根目录文件到 `pending_delete` 文件夹（安全暂存，确认无问题后再删除）：
- 如果 `CardDialogs.kt` 在根目录存在且与 `features/cards/dialogs/CardDialogs.kt` 重复

创建 `pending_delete` 文件夹用于暂存：
```bash
mkdir -p pending_delete/composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/
```

### 阶段 3：修复导入路径
#### 3.1 修复 App.kt 中的导入
- 确保所有导入指向新模块位置
- 特别注意 reminder 功能相关的导入

#### 3.2 修复 reminder 模块的导入
- `reminder/CardNotificationScheduler.kt` 中的导入
- `reminder/ReminderTimeCalculator.kt` 中的导入
- 各平台实现文件中的导入

#### 3.3 检查其他可能的导入问题
- 搜索所有指向根目录的导入
- 更新为指向新模块

### 阶段 4：合并功能性修改
1. 对比根目录 `CardDialogs.kt` 与新模块版本的差异
2. 将朋友的功能性修改合并到新模块版本中
3. 确保不丢失任何功能

### 阶段 5：验证和测试
1. 编译验证所有平台
2. 测试 reminder 功能正常工作
3. 测试所有现有功能未受影响

## 具体执行步骤

### 1. 创建备份分支
```bash
git checkout -b fix/merge-conflicts-corrected
```

### 2. 对比文件差异
```bash
# 对比 CardDialogs.kt 的差异
git diff HEAD:composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/CardDialogs.kt composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/features/cards/dialogs/CardDialogs.kt
```

### 3. 移动重复文件到暂存区
```bash
# 创建暂存目录
mkdir -p pending_delete/composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/

# 移动重复文件到暂存区（而不是直接删除）
git mv composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/CardDialogs.kt pending_delete/composeApp/src/commonMain/kotlin/com/dlx/smartalarm/demo/

# 提交移动操作
git commit -m "chore: 移动重复的旧结构文件到 pending_delete 暂存区"
```

### 4. 修复导入路径
- 逐个修复 reminder 文件中的导入
- 修复 App.kt 中的导入
- 搜索并修复其他导入问题

### 5. 合并功能差异
- 手动合并朋友的功能性修改到新模块文件中

## 风险评估

### 高风险
- 朋友的功能性修改可能在移动文件时丢失
- reminder 功能的依赖关系可能复杂
- 暂存文件可能占用额外空间

### 缓解措施
- 仔细对比文件差异，确保不丢失功能
- 分步骤执行，每步验证
- 保留详细记录，便于回滚
- 使用 `pending_delete` 文件夹安全暂存，确认无问题后再彻底删除
- 暂存文件保留一段时间，确保所有功能正常后再清理

## 预期结果

完成后将实现：
1. **干净的文件结构**：根目录不再有重复文件
2. **完整的功能**：朋友的 reminder 功能正常工作
3. **正确的导入**：所有导入指向新模块位置
4. **一致的架构**：符合模块化重构后的结构

## 关键注意点

1. **安全移动**：将重复文件移动到 `pending_delete` 暂存区，而不是直接删除
2. **保留功能**：确保我的以及朋友的所有功能性修改都被保留：我的是【Tag、Favourite、筛选】和【代码重构】；朋友的是【reminder】功能。
3. **修复导入**：让所有代码使用新的模块路径
4. **逐步验证**：每个步骤都要验证编译和功能
5. **暂存管理**：在确认所有功能正常后，再彻底删除 `pending_delete` 中的文件

## 暂存文件清理

在完成所有修复并验证功能正常后，可以清理暂存文件，清理之前询问用户确认：

```bash
# 确认所有功能正常后，删除暂存文件夹
rm -rf pending_delete/
git add pending_delete/
git commit -m "chore: 清理 pending_delete 暂存文件夹"
```
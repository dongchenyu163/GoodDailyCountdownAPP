本文档定义了在本项目中进行软件开发时，各 Agent 的协作流程。所有 Agent 必须严格遵守此规则。

## 环境变量
- `windows`首先使用`./env_export.ps1`设置环境变量。之后才能正常运行编译项目。

## 1. 协作工作流程 (Development Workflow)

当用户提交一个新的功能或修复请求时，Agents 应按以下流程协作：
### 第一阶段：初始化与规划
1.  **[kmp-build-developer]**: 分析用户需求，创建或更新TODO list。
    *   将大任务拆解为原子性的子任务。
2.  **[git-version-manager]**: 检查当前 Git 状态，确保在正确的分支。
    1. 保证开发分支是最新的，与远端主分支保持同步，并优先和[kmp-build-developer]解决与主分支可能的冲突。
    2. 扫描已有分支，检查是否有已存在的功能分支：
       1. 如果有已存在的功能分支，[git-version-manager]切换到该开发分支。
       2. 如果没有对应的功能分支，[git-version-manager]允许创建新的分支来完成开发任务。命名方式：`feature/[功能描述]`

### 第二阶段：迭代开发循环 (针对 TODO 中的每一项)
1.  **[kmp-build-developer]**: 
    *   领取一个待办事项。
    *   编写/修改代码。
    *   执行 **JVM 编译验证** (`./gradlew :composeApp:jvmJar`)。
    *   *循环*: 失败 -> 修复 -> 重试。
    *   成功 -> 呼叫 **[kmp-code-reviewer]**。
2.  **[kmp-code-reviewer]**: 
    *   审查代码变更。
    *   *分支*: 
        *   有明显缺陷 -> 驳回 -> **[kmp-build-developer]** 修改。
        *   合格 -> 批准 -> 呼叫 **[git-version-manager]**。
3.  **[git-version-manager]**: 
    *   执行 `git add` 和 `git commit`。
    *   更新 TODO list 状态为已完成。

### 第三阶段：最终验证与交付
1.  **[kmp-build-developer]**: 
    *   检查是否所有 TODO 已完成。
    *   执行 **Android 平台编译验证** (`./gradlew :composeApp:assembleDebug`)。
    *   如果 Android 编译失败 -> 修复 -> (进入小循环: 编译JVM -> 审阅 -> 提交)。
2.  **[git-version-manager]**:
    *   在所有平台编译验证通过后。
    *   询问用户是否推送到远程仓库。
    *   执行 `git push`。

---

## 3. 关键约束 (Critical Constraints)

1.  **验证优先**: 永远不要提交无法编译的代码。JVM 编译是最低门槛。
2.  **禁止臆测**: [kmp-build-developer] 必须先读文件再写代码。
3.  **资源管理**: UI 文本必须抽取到 `MR.strings`，图片放到 `MR.images`。

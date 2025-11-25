# 收藏功能实现计划

## 1. 功能概述
为倒计时卡片添加收藏功能，包括：
- 在卡片数据模型中添加收藏状态字段
- 实现收藏按钮和动画效果
- 实现收藏状态优先的排序逻辑
- 添加过滤器功能，支持按收藏状态过滤卡片

## 2. 详细实现步骤

### 2.1 数据模型修改
- 在 `CardData` 数据类中添加 `isFavorite: Boolean` 字段，默认值为 `false`
- 确保向后兼容性，为旧数据提供默认值

### 2.2 UI组件修改
- 在卡片组件右下角添加收藏切换按钮
- 使用Compottie库播放收藏动画
- 实现动画正向/反向播放逻辑

### 2.3 动画实现
- 使用 `FavIcon.lottie` 文件作为动画资源
- 需要使用【LottieCompositionSpec.DotLottie】来使用`.lottie`文件
- 实现收藏状态切换时的动画播放逻辑
- 未收藏→收藏：正向播放
- 收藏→未收藏：反向播放

### 2.4 排序逻辑
- 修改卡片排序逻辑，优先按收藏状态排序
- 次级排序参考剩余天数

### 2.5 过滤器功能
- 在主页面右上角添加过滤器按钮
- 使用 `FilterIcon.svg` 文件作为图标
- 实现过滤器菜单，包含"已收藏"和"清除过滤器"选项
- 实现"已收藏"过滤器功能

## 3. 技术要点

### 3.1 Lottie动画集成
```kotlin
// 使用Compottie库实现动画
val composition by rememberLottieComposition {
    LottieCompositionSpec.JsonString(
        Res.readBytes("files/FavIcon.lottie").decodeToString()
    )
}

val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = Compottie.IterateOnce,
    // 根据收藏状态设置播放方向
)
```

### 3.2 排序算法
```kotlin
// 优先按收藏状态排序，然后按剩余天数排序
val sortedCards = cardList.sortedWith { card1, card2 ->
    when {
        card1.isFavorite && !card2.isFavorite -> -1
        !card1.isFavorite && card2.isFavorite -> 1
        else -> card1.remainingDays.compareTo(card2.remainingDays)
    }
}
```

### 3.3 过滤器实现
- 使用状态管理过滤器选项
- 实现过滤器逻辑，仅显示符合条件的卡片

## 4. 实现顺序
1. 修改数据模型
2. 实现动画效果
3. 添加UI组件
4. 实现排序逻辑
5. 添加过滤器功能
6. 测试和优化
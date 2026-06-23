# 智慧出行打车系统 — Smart Travel App

## 项目概述

**浙江大学 · 课程设计项目**

《智慧出行打车系统》是一款基于 Android 平台的网约车打车演示应用，模拟了从叫车到行程完成的完整流程。本应用仅用于大学课程设计演示，**不接入真实网约车平台**，所有数据均为本地模拟数据。

## 技术栈

| 项目 | 内容 |
|------|------|
| 开发语言 | Java |
| 布局方式 | XML |
| 设计风格 | Material Design 3（蓝白主题） |
| 最低SDK | Android 8.0 (API 26) |
| 目标SDK | Android 15 (API 35) |
| 数据库 | SQLite（本地历史订单存储） |
| 导航方式 | Navigation Component + BottomNavigationView |
| 地图方案 | 矢量图占位模拟（无需配置高德SDK） |

## 项目结构

```
SmartTravelApp/
├── build.gradle.kts                       # 项目级构建配置
├── settings.gradle.kts                    # 项目设置
├── gradle.properties                      # Gradle属性
└── app/
    ├── build.gradle.kts                   # 模块级构建配置（含所有依赖）
    ├── proguard-rules.pro                 # ProGuard混淆规则
    └── src/main/
        ├── AndroidManifest.xml            # 清单文件（权限+Activity注册）
        ├── java/com/example/smarttravel/
        │   ├── MainActivity.java          # 主Activity（底部导航）
        │   ├── HomeFragment.java          # 首页碎片（搜索+叫车）
        │   ├── OrderFragment.java         # 订单碎片（进行中订单）
        │   ├── MessageFragment.java       # 消息碎片（系统通知）
        │   ├── ProfileFragment.java       # 我的碎片（个人中心入口）
        │   ├── CarSelectActivity.java     # 车型选择页
        │   ├── OrderConfirmActivity.java  # 订单确认页（匹配司机）
        │   ├── OrderDetailActivity.java   # 订单详情页（司机+Timeline）
        │   ├── HistoryOrderActivity.java  # 历史订单页（SQLite CRUD）
        │   ├── AboutActivity.java         # 关于系统页
        │   ├── model/
        │   │   ├── CarType.java           # 车型实体
        │   │   ├── OrderInfo.java         # 订单实体
        │   │   └── DriverInfo.java        # 司机信息实体
        │   ├── adapter/
        │   │   ├── CarTypeAdapter.java    # 车型列表适配器
        │   │   └── HistoryOrderAdapter.java # 历史订单适配器
        │   └── database/
        │       └── OrderDatabaseHelper.java # SQLite数据库帮助类
        └── res/
            ├── drawable/                  # 矢量图标+背景+形状资源（27个文件）
            ├── layout/                    # 布局文件（12个）
            ├── menu/                      # 底部导航菜单
            ├── navigation/                # 导航图
            ├── values/                    # 颜色/字符串/尺寸/主题
            ├── values-night/              # 夜间模式主题
            └── mipmap/                    # 启动图标（需自行生成）
```

## 功能流程

```
首页输入目的地 → 选择车型 → 确认订单
    ↓                                      ↓
   匹配司机（2秒模拟） ←—— 匹配动画遮罩层
    ↓
订单详情页（司机信息 + 行程进度TimeLine）
    ↓
历史订单可查看（SQLite持久化 + 长按删除）
```

## 运行时截图目录

| 页面 | 说明 |
|------|------|
| 首页 | 顶部搜索+地图占位+底部叫车面板 |
| 车型选择 | RecyclerView展示四种车型 |
| 订单确认 | 起终点/车型/费用+确认按钮 |
| 匹配动画 | 遮罩层+ProgressBar+"正在匹配司机" |
| 订单详情 | 司机卡片+五步TimeLine进度 |
| 我的 | 用户信息+四个功能入口 |
| 历史订单 | SQLite查询+列表展示+长按删除 |

## 运行说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17+
- Gradle 8.7+

### 运行步骤

1. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择 `SmartTravelApp` 目录

2. **等待 Gradle 同步**
   - 首次打开会自动下载依赖
   - 同步完成后点击 "Run" 按钮

3. **选择运行设备**
   - 连接真机（推荐）或创建模拟器（API 26+）
   - 建议使用 1080×1920 分辨率以获得最佳显示效果

4. **运行应用**
   - 点击运行按钮（绿色三角形）
   - 等待构建完成

5. **使用流程**
   - 首页输入目的地（如"西湖文化广场"）
   - 点击"立即叫车"
   - 选择一种车型
   - 点击"确认呼叫"（等待2秒匹配动画）
   - 查看订单详情和行程进度
   - 底部导航"我的"→"历史订单"查看已保存的订单

### 常见问题

**Q: 编译报错 `Unresolved reference: kotlinx`？**
A: 项目使用 Kotlin 仅作为 AndroidX core-ktx 依赖。如果 Gradle 同步失败，检查网络连接或使用国内镜像。

**Q: 地图区域显示空白？**
A: 地图区域使用矢量图占位（`@drawable/map_placeholder`），不需要配置高德SDK密钥。如果仍然空白，确认 `map_placeholder.xml` 文件存在且编译正常。

**Q: 历史订单数据在哪里？**
A: 使用 SQLite 本地数据库存储，路径为 `/data/data/com.example.smarttravel/databases/smart_travel.db`。可通过 Android Studio 的 Device File Explorer 查看。

## 课程设计要点

本项目的课程设计创新点：
1. **模拟网约车全流程**：从叫车→匹配→行程→完成的全链路仿真
2. **Material Design 3**：采用最新的 Material 3 设计规范和蓝白色主题
3. **本地数据持久化**：使用 SQLite 实现历史订单的增删查
4. **无服务器架构**：所有数据均为本地模拟，无需后端部署
5. **地图降级方案**：以矢量图模拟地图区域，避免高德SDK配置复杂性

## 许可证

仅供浙江大学课程设计教学使用。

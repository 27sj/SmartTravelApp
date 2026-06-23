# ============================================================
# ProGuard 混淆规则 — 智慧出行打车系统
# 纯本地演示项目，无需混淆，保持默认
# ============================================================

# 保留所有实体类（避免Gson/序列化时字段丢失）
-keep class com.example.smarttravel.model.** { *; }

# 保留数据库帮助类
-keep class com.example.smarttravel.database.** { *; }

# 保留所有Activity和Fragment
-keep class com.example.smarttravel.** { *; }

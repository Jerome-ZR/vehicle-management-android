# 车辆管理工作台 (Vehicle Manager)

一款功能完善的安卓车辆管理应用，以工作台形式呈现。

## 功能特性

- 🚗 **车辆档案管理** — 车牌号、编号、使用人、保养信息、年审日期
- 🔧 **维修保养记录** — 记录维保项目、地点、价格，自动更新车辆保养信息
- ⚡ **公里数录入** — 每月公里数录入，接近保养里程自动提醒
- 📋 **待办模块** — 自动生成年审/保养/维修待办，到期提醒
- 📊 **Excel 导入导出** — 清晰易读的多Sheet Excel表格
- 💾 **备份还原** — JSON格式数据备份与还原
- 🔔 **智能提醒** — 保养到期、年审到期自动提醒

## 技术栈

- Kotlin + Jetpack Compose (Material 3)
- Room Database
- Navigation Compose
- Apache POI (Excel)
- Gson (备份)
- GitHub Actions (云端编译)

## 构建

```bash
./gradlew assembleRelease
```

## GitHub Actions

Push到main分支自动编译APK并发布Release。

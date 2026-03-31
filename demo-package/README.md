# 演示资料包说明

这个目录用于统一存放“系统演示”所需的账号说明、清理脚本、演示素材和说明文档。

## 目录结构

- `accounts/`
  - 演示账号清单
- `files/`
  - 资质材料、服务过程凭证、售后凭证、服务项目 SVG 素材
- `scripts/`
  - 启动完整演示模式的脚本
- `sql/`
  - 清理非核心演示数据的 SQL 脚本

## 默认核心数据

系统默认启动时只保留最小可演示核心数据：

- 3 个核心演示账号
  - 1 个普通用户
  - 1 个服务人员
  - 1 个管理员
- 5 个服务项目分类
- 1 个与真实账号绑定的默认服务人员档案
- 基础角色、权限、用户资料
- 轻量演示业务数据
  - 收藏
  - 默认地址
  - 多笔不同状态订单
  - 支付记录
  - 服务过程记录
  - 一条售后与通知消息

默认核心账号如下：

- `demo_user / 13800000011 / 123456`
- `demo_worker / 13800000022 / 123456`
- `demo_admin / 13800000033 / 123456`

## 完整演示模式

如果要展示完整的订单、售后、资质审核、消息通知等功能，建议启动 `demo` profile：

```powershell
cd F:\Desktop\demo\project\housekeeping\backend\housekeeping-server
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

对应配置文件：

- [application-demo.yml](F:\Desktop\demo\project\housekeeping\backend\housekeeping-server\src\main\resources\application-demo.yml)

完整演示模式会额外补充：

- 有历史订单和售后的普通用户
- 待审核服务人员
- 已驳回服务人员
- 演示订单、支付、服务记录、售后、通知消息

## 清理旧假数据

如果数据库里还残留旧的演示数据或旧版默认账号，可以先执行：

```sql
SOURCE F:/Desktop/demo/project/housekeeping/demo-package/sql/01-clean-noncore-demo-data.sql;
```

这个脚本会清理：

- 旧版多余服务人员账号 `13800000023`、`13800000024`
- 完整演示模式下额外生成的用户、服务人员和业务数据
- 演示订单、支付、服务记录、评价、售后、通知与沟通记录

## 服务项目 SVG 素材

本次已经重新生成一套 SVG 服务项目展示图，目录如下：

- [services](F:\Desktop\demo\project\housekeeping\demo-package\files\services)

文件包括：

- `daily-cleaning.svg`
- `deep-cleaning.svg`
- `maternal-care.svg`
- `elderly-care.svg`
- `appliance-cleaning.svg`
- `service-demo-data.md`

这些文件同时已经同步到系统公开目录：

- [uploads/demo/services](F:\Desktop\demo\project\housekeeping\backend\housekeeping-server\uploads\demo\services)

## 其他演示附件

`files/` 目录下还保留了常用演示附件，可直接用于：

- 服务人员资质材料上传
- 服务过程打卡与完工凭证
- 售后凭证上传

账号清单见：

- [demo-accounts.md](F:\Desktop\demo\project\housekeeping\demo-package\accounts\demo-accounts.md)

渐进式演示说明见：

- [demo-walkthrough.md](F:\Desktop\demo\project\housekeeping\demo-package\demo-walkthrough.md)

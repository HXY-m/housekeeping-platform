# 家政服务预约平台渐进式演示说明

## 1. 演示目标

本说明用于按“由浅入深、逐步展开”的方式演示系统全部核心功能。

推荐按本文顺序展示，这样能自然覆盖：

- 登录与注册
- 服务浏览
- 地图选址与地址簿
- 收藏
- 预约下单与支付
- 用户订单管理
- 服务人员资质申请
- 服务人员工作台
- 过程记录上传
- 消息通知与订单沟通
- 评价与售后
- 管理后台治理
- 数据中心、报表与权限配置

## 2. 演示前准备

### 2.1 推荐启动方式

为了覆盖完整功能，建议启动完整演示模式：

```powershell
cd F:\Desktop\demo\project\housekeeping\backend\housekeeping-server
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

前端启动：

```powershell
cd F:\Desktop\demo\project\housekeeping\frontend\web
npm run dev
```

### 2.2 如需清理旧数据

先执行：

- [01-clean-noncore-demo-data.sql](F:\Desktop\demo\project\housekeeping\demo-package\sql\01-clean-noncore-demo-data.sql)

### 2.3 推荐打开地址

- 前端：[http://localhost:5173](http://localhost:5173)
- Knife4j：[http://localhost:8080/doc.html](http://localhost:8080/doc.html)
- Swagger UI：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 3. 演示账号

### 3.1 默认核心账号

| 角色 | 用户名 | 手机号 | 密码 | 用途 |
| --- | --- | --- | --- | --- |
| 普通用户 | `demo_user` | `13800000011` | `123456` | 前台浏览、收藏、预约、支付、地址簿 |
| 服务人员 | `demo_worker` | `13800000022` | `123456` | 已认证服务人员，演示工作台和履约 |
| 管理员 | `demo_admin` | `13800000033` | `123456` | 后台所有功能 |

### 3.2 完整演示模式附加账号

| 角色 | 用户名 | 手机号 | 密码 | 场景 |
| --- | --- | --- | --- | --- |
| 普通用户 | `user_0044` | `13800000044` | `123456` | 有评价、售后、历史订单 |
| 普通用户 | `user_0045` | `13800000045` | `123456` | 有待确认订单 |
| 服务人员 | `worker_0055` | `13800000055` | `123456` | 资质待审核 |
| 服务人员 | `worker_0066` | `13800000066` | `123456` | 资质已驳回 |

## 4. 演示素材文件

### 4.1 服务项目 SVG 素材

- [daily-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\daily-cleaning.svg)
- [deep-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\deep-cleaning.svg)
- [maternal-care.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\maternal-care.svg)
- [elderly-care.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\elderly-care.svg)
- [appliance-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\appliance-cleaning.svg)

### 4.2 资质与售后附件

- [qualification-id.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-id.svg)
- [qualification-health.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-health.svg)
- [qualification-training.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-training.svg)
- [after-sale-proof.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\after-sale-proof.svg)

### 4.3 服务过程附件

- [service-check-in.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-check-in.svg)
- [service-progress.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-progress.svg)
- [service-finish.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-finish.svg)

## 5. 推荐演示顺序

### 第 1 步：首页与服务浏览

页面：

- `/`
- `/workers`

演示：

1. 打开首页，展示服务项目卡片。
2. 展示默认只保留 1 个与真实账号绑定的推荐服务人员。
3. 进入服务人员列表，查看筛选和卡片信息。

所需数据与文件：

- 默认核心数据
- 服务项目 SVG 素材

### 第 2 步：登录与注册

页面：

- `/login`
- `/register`

演示：

1. 展示手机号登录和用户名登录。
2. 用 `demo_user` 演示手机号登录。
3. 用 `demo_admin` 演示用户名登录。
4. 展示普通用户和服务人员注册入口。

所需数据与文件：

- 默认核心账号

### 第 3 步：个人资料、地址簿与地图选址

账号：

- `demo_user / 123456`

页面：

- `/user/profile`

演示：

1. 进入个人资料页。
2. 新增地址。
3. 打开地图选址，搜索并选择地址。
4. 保存后查看城市、详细地址和定位坐标回填。

所需数据与文件：

- 默认核心数据

### 第 4 步：收藏与预约

账号：

- `demo_user / 123456`

页面：

- `/workers`
- `/booking/:workerId`
- `/user/favorites`

演示：

1. 收藏当前服务人员。
2. 进入预约页。
3. 选择地址、日期和具体时间段。
4. 提交预约。

所需数据与文件：

- 默认核心数据

### 第 5 步：支付与订单详情

账号：

- `demo_user / 123456`

页面：

- `/user/orders`

演示：

1. 查看订单列表摘要。
2. 点击“查看详情”，在当前页抽屉展开完整信息。
3. 完成支付。

所需数据与文件：

- 默认核心数据

### 第 6 步：服务人员资质申请

账号：

- 新注册服务人员
- 或 `worker_0055 / 123456`

页面：

- `/worker/qualification`

演示：

1. 展示注册后服务信息不在注册页填写。
2. 进入资质页填写服务信息与服务类型。
3. 上传资质文件。
4. 展示未审核前无法接单。

所需数据与文件：

- [qualification-id.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-id.svg)
- [qualification-health.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-health.svg)
- [qualification-training.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\qualification-training.svg)

### 第 7 步：管理员审核资质

账号：

- `demo_admin / 123456`

页面：

- `/admin/applications`

演示：

1. 查看待审核和已驳回申请。
2. 查看资质附件。
3. 审核通过或驳回。
4. 展示审核结果通知。

所需数据与文件：

- 完整演示模式附加账号
- 资质 SVG 附件

### 第 8 步：服务人员工作台与订单履约

账号：

- `demo_worker / 123456`

页面：

- `/worker/dashboard`
- `/worker/orders`

演示：

1. 查看工作台统计卡片与营业额流水。
2. 进入订单处理。
3. 接单、开工、提交完工。
4. 查看当前页抽屉订单详情。

所需数据与文件：

- 完整演示模式数据更完整

### 第 9 步：服务过程记录上传

账号：

- `demo_worker / 123456`

页面：

- `/worker/orders`

演示：

1. 上传上门打卡凭证。
2. 上传服务过程凭证。
3. 上传完工凭证。

所需数据与文件：

- [service-check-in.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-check-in.svg)
- [service-progress.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-progress.svg)
- [service-finish.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\service-finish.svg)

### 第 10 步：消息中心与订单沟通

账号：

- 用户：`user_0044 / 123456`
- 服务人员：`demo_worker / 123456`
- 管理员：`demo_admin / 123456`

页面：

- `/user/messages`
- `/user/conversations`
- `/worker/messages`
- `/worker/conversations`
- `/admin/messages`

演示：

1. 展示消息中心和订单沟通已经拆分为两个入口。
2. 点击“一键全部标记已读”。
3. 打开订单沟通查看会话记录。

所需数据与文件：

- 完整演示模式数据

### 第 11 步：评价与售后

账号：

- 用户：`user_0044 / 123456`
- 管理员：`demo_admin / 123456`

页面：

- `/user/orders`
- `/admin/after-sales`

演示：

1. 用户提交评价。
2. 用户发起售后。
3. 上传售后凭证。
4. 管理员处理售后工单。

所需数据与文件：

- [after-sale-proof.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\after-sale-proof.svg)

### 第 12 步：后台服务项目与用户管理

账号：

- `demo_admin / 123456`

页面：

- `/admin/services`
- `/admin/users`

演示：

1. 查看服务项目列表。
2. 上传或替换服务项目展示图。
3. 说明后台新增服务项目后，资质页服务类型会自动同步。
4. 查看用户管理中默认只保留 3 个核心账号。

所需数据与文件：

- 服务项目 SVG 素材

### 第 13 步：后台订单监管、报表与权限配置

账号：

- `demo_admin / 123456`

页面：

- `/admin/orders`
- `/admin/dashboard`
- `/admin/reports`
- `/admin/permissions`

演示：

1. 展示订单监管和支付状态。
2. 展示数据中心、营业额流水和订单结构。
3. 导出报表。
4. 查看中文权限配置。

所需数据与文件：

- 完整演示模式数据

## 6. 10 分钟精简演示路线

如果时间只有 10 分钟，建议只演示：

1. 首页与服务项目 SVG 展示
2. 登录页双登录方式
3. 用户地图选址与预约下单
4. 支付与订单详情抽屉
5. 服务人员工作台与接单
6. 服务过程附件上传
7. 管理员资质审核与订单监管
8. 数据中心与权限配置  

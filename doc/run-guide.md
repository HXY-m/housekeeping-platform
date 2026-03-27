# 运行说明

## 启动后端

进入目录：

```powershell
cd F:\Desktop\demo\project\housekeeping\backend\housekeeping-server
```

启动前请先准备 MySQL，并执行：

```powershell
mysql -u root -p < F:\Desktop\demo\project\housekeeping\backend\sql\schema.sql
```

然后设置环境变量：

```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DB="housekeeping_platform"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="123456"
```

启动：

```powershell
mvn spring-boot:run
```

默认地址：

- `http://localhost:8080`
- 当前后端仅支持 `MySQL`
- DAO 层使用 `MyBatis-Plus`

## 启动前端

进入目录：

```powershell
cd F:\Desktop\demo\project\housekeeping\frontend\web
```

首次安装依赖：

```powershell
npm install
```

开发模式启动：

```powershell
npm run dev
```

默认地址：

- `http://localhost:5173`

## 认证接口

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`
- `GET /api/auth/demo-accounts`

登录请求示例：

```json
{
  "phone": "13800000033",
  "password": "123456",
  "roleCode": "ADMIN"
}
```

注册请求示例：

```json
{
  "realName": "张三",
  "phone": "13800138000",
  "password": "123456"
}
```

## 订单与售后接口

- `GET /api/orders`
- `POST /api/orders`
- `POST /api/orders/{id}/review`
- `GET /api/worker/orders`
- `POST /api/worker/orders/{id}/accept`
- `POST /api/worker/orders/{id}/start`
- `POST /api/worker/orders/{id}/complete`
- `GET /api/after-sales/my`
- `POST /api/after-sales`
- `POST /api/after-sales/{id}/attachments`
- `GET /api/admin/after-sales`
- `POST /api/admin/after-sales/{id}/handle`

售后凭证上传说明：

- 先调用 `POST /api/after-sales` 创建售后工单
- 再调用 `POST /api/after-sales/{id}/attachments` 逐张上传凭证图片
- 上传接口使用 `multipart/form-data`，字段名为 `file`

图片访问说明：

- 后端会把上传文件暴露到 `/uploads/**`
- 前端开发环境已代理 `/uploads` 到 `http://localhost:8080`
- 因此前端页面和接口返回的图片地址可以直接预览

## 演示账号

- 普通用户：`13800000011 / 123456 / USER`
- 服务人员：`13800000022 / 123456 / WORKER`
- 管理员：`13800000033 / 123456 / ADMIN`

访问受保护接口时，请在请求头里带上：

```text
Authorization: Bearer <token>
```

当前已经对以下接口做了角色保护：

- `/api/admin/**`
- `/api/worker/orders/**`
- `/api/orders/**`
- `/api/after-sales/**`

## API 文档地址

启动后端后可访问：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Knife4j: `http://localhost:8080/doc.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

测试受保护接口时，建议先调用登录接口，拿到 `token` 后点击页面上的 `Authorize`，填入：

```text
Bearer 你的token
```

## 当前可访问页面

- `/` 首页
- `/workers` 服务人员列表
- `/workers/1` 服务人员详情
- `/booking/1` 在线预约
- `/orders` 用户订单中心
- `/worker/apply` 普通用户申请入驻
- `/worker/orders` 服务人员工作台
- `/login` 登录页
- `/register` 注册页
- `/admin/dashboard` 管理员看板
- `/admin/applications` 入驻审核
- `/admin/after-sales` 售后处理中心

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

## 演示账号

- 普通用户：`13800000011 / 123456 / USER`
- 服务人员：`13800000022 / 123456 / WORKER`
- 管理员：`13800000033 / 123456 / ADMIN`

管理员接口访问时，请在请求头里带上：

```text
Authorization: Bearer <token>
```

当前已经对 `/api/admin/**` 和 `/api/worker/orders/**` 做了角色保护。

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

# 运行说明

## 启动后端

进入目录：

```powershell
cd F:\Desktop\demo\project\housekeeping\backend\housekeeping-server
```

启动：

```powershell
mvn spring-boot:run
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

## 当前可访问页面

- `/` 首页
- `/workers` 服务人员列表
- `/workers/1` 服务人员详情
- `/booking/1` 在线预约
- `/orders` 订单进度
- `/admin` 管理员统计看板

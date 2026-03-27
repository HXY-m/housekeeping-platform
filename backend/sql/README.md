# SQL 说明

当前目录用于存放家政服务预约平台的 MySQL 脚本。

## 文件说明

- `schema.sql`：当前 MyBatis-Plus 版本使用的核心表结构，覆盖服务分类、服务人员、预约订单、订单进度

## 使用建议

1. 先在 MySQL 中执行 `schema.sql`
2. 当前后端 DAO 层使用 `MyBatis-Plus`
3. 当前版本先落通主链路表，后续再继续扩展用户、评价、售后等表

# 服务项目演示数据说明

当前系统默认内置 5 个服务项目分类，均已绑定新的 SVG 展示图。

| 服务项目 | 标识 | 价格标签 | 服务时长 | 适用场景 | 展示文件 |
| --- | --- | --- | --- | --- | --- |
| 日常保洁 | `daily-cleaning` | `¥129 起` | `2 小时 / 4 小时` | 日常保洁、入住保洁 | [daily-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\daily-cleaning.svg) |
| 深度清洁 | `deep-cleaning` | `¥229 起` | `4 小时 / 半天` | 退租保洁、开荒保洁、节前大扫除 | [deep-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\deep-cleaning.svg) |
| 母婴护理 | `maternal-care` | `¥368 起` | `半天 / 全天` | 产后家庭、新生儿陪护 | [maternal-care.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\maternal-care.svg) |
| 老人陪护 | `elderly-care` | `¥299 起` | `半天 / 全天` | 居家陪护、陪诊、康复协助 | [elderly-care.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\elderly-care.svg) |
| 家电清洗 | `appliance-cleaning` | `¥149 起` | `2 小时 / 单台计费` | 空调、洗衣机、油烟机 | [appliance-cleaning.svg](F:\Desktop\demo\project\housekeeping\demo-package\files\services\appliance-cleaning.svg) |

这些数据已经同步写入系统默认初始化逻辑：

- [DemoDataInitializer.java](F:\Desktop\demo\project\housekeeping\backend\housekeeping-server\src\main\java\com\housekeeping\bootstrap\DemoDataInitializer.java)

系统运行时实际使用的公开素材目录：

- [uploads/demo/services](F:\Desktop\demo\project\housekeeping\backend\housekeeping-server\uploads\demo\services)

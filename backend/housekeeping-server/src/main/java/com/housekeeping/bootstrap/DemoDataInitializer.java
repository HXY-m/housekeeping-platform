package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderProgressEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderProgressMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final ServiceCategoryMapper categoryMapper;
    private final WorkerMapper workerMapper;
    private final OrderMapper orderMapper;
    private final OrderProgressMapper orderProgressMapper;

    public DemoDataInitializer(ServiceCategoryMapper categoryMapper,
                               WorkerMapper workerMapper,
                               OrderMapper orderMapper,
                               OrderProgressMapper orderProgressMapper) {
        this.categoryMapper = categoryMapper;
        this.workerMapper = workerMapper;
        this.orderMapper = orderMapper;
        this.orderProgressMapper = orderProgressMapper;
    }

    @Override
    public void run(String... args) {
        if (categoryMapper.selectCount(null) == 0) {
            categoryMapper.insert(new ServiceCategoryEntity("日常保洁", "适合两居室与三居室的定期清洁维护", "¥129 起", "daily-cleaning"));
            categoryMapper.insert(new ServiceCategoryEntity("深度清洁", "针对厨房与卫生间的深度处理", "¥229 起", "deep-cleaning"));
            categoryMapper.insert(new ServiceCategoryEntity("母婴护理", "月嫂、育儿嫂与产后基础照护服务", "¥368 起", "maternal-care"));
            categoryMapper.insert(new ServiceCategoryEntity("老人陪护", "上门陪诊、日常陪护与康复辅助", "¥199 起", "elderly-care"));
            categoryMapper.insert(new ServiceCategoryEntity("家电清洗", "空调、洗衣机、油烟机等家电拆洗", "¥149 起", "appliance-cleaning"));
        }

        if (workerMapper.selectCount(null) == 0) {
            workerMapper.insert(new WorkerEntity(null, "李阿姨", "金牌保洁师", 4.9, 286, 68, "上海",
                    "擅长家庭深度保洁和收纳整理，服务稳定，细节到位。",
                    "日常保洁,深度清洁,收纳整理", "今天 18:30 后可约", 6,
                    "家政服务培训证书,消杀培训证明", "浦东新区,杨浦区,虹口区",
                    "厨房重油污治理,租房退租深度清洁,母婴家庭轻柔保洁"));
            workerMapper.insert(new WorkerEntity(null, "王师傅", "家电清洗师", 4.8, 193, 88, "上海",
                    "专注空调与洗衣机清洗，支持现场拍照反馈，适合换季预约。",
                    "家电清洗,空调拆洗,油烟机清洗", "明天 09:00 可约", 5,
                    "家电清洗专项证书", "静安区,徐汇区,长宁区",
                    "中央空调深洗,租客搬家前家电清洁"));
            workerMapper.insert(new WorkerEntity(null, "周阿姨", "母婴护理师", 4.95, 126, 128, "上海",
                    "有育婴经验和护理证书，熟悉新生儿护理与月子期家庭支持。",
                    "母婴护理,育儿嫂,产后陪护", "后天 08:00 可约", 8,
                    "母婴护理证书,健康证", "闵行区,浦东新区",
                    "新生儿夜间护理,产后恢复支持"));
        }

        if (orderMapper.selectCount(null) == 0) {
            List<WorkerEntity> workers = workerMapper.selectList(new LambdaQueryWrapper<WorkerEntity>().orderByAsc(WorkerEntity::getId));
            WorkerEntity cleaningWorker = workers.size() > 0 ? workers.get(0) : null;
            WorkerEntity applianceWorker = workers.size() > 1 ? workers.get(1) : null;

            if (cleaningWorker != null) {
                OrderEntity order1 = new OrderEntity(
                        "日常保洁", cleaningWorker.getId(), "张女士", "13800000001", "上海市浦东新区锦绣路 88 号",
                        "2026-03-27", "09:00-12:00", "待服务", "服务人员已确认，等待上门", "重点清洁厨房和卫生间"
                );
                orderMapper.insert(order1);
                orderProgressMapper.insert(new OrderProgressEntity(order1.getId(), "待服务", "服务人员已确认，等待上门"));
            }

            if (applianceWorker != null) {
                OrderEntity order2 = new OrderEntity(
                        "家电清洗", applianceWorker.getId(), "陈先生", "13800000002", "上海市徐汇区漕溪北路 128 号",
                        "2026-03-28", "14:00-16:00", "服务中", "服务人员已到达，正在进行空调拆洗", "清洗 2 台挂机空调"
                );
                orderMapper.insert(order2);
                orderProgressMapper.insert(new OrderProgressEntity(order2.getId(), "服务中", "服务人员已到达，正在进行空调拆洗"));
            }
        }
    }
}

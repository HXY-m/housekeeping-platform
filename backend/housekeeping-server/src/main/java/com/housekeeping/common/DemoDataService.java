package com.housekeeping.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.common.mapper.CategoryMapper;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.home.dto.Highlight;
import com.housekeeping.home.dto.HomePageDto;
import com.housekeeping.home.dto.ServiceCategoryDto;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerDetailDto;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DemoDataService {

    private final ServiceCategoryMapper serviceCategoryMapper;
    private final WorkerMapper workerMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;
    private final WorkerDtoMapper workerDtoMapper;

    public DemoDataService(ServiceCategoryMapper serviceCategoryMapper,
                           WorkerMapper workerMapper,
                           OrderMapper orderMapper,
                           CategoryMapper categoryMapper,
                           WorkerDtoMapper workerDtoMapper) {
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.workerMapper = workerMapper;
        this.orderMapper = orderMapper;
        this.categoryMapper = categoryMapper;
        this.workerDtoMapper = workerDtoMapper;
    }

    public HomePageDto getHomePage() {
        return new HomePageDto(
                "像 Taskrabbit 一样下单，但更贴合本地家政服务场景",
                "围绕任务书要求，先打通服务浏览、预约下单、订单履约、评价反馈与后台统计的 MVP 闭环。",
                getCategories(),
                List.of(
                        new Highlight("多角色协同", "覆盖用户、服务人员、管理员三类角色，支撑平台基础运营流程。"),
                        new Highlight("预约到履约", "支持下单、接单、开始服务、完成服务与评价的最小闭环。"),
                        new Highlight("数据可视化", "管理员可以查看订单量、完成量、服务销量与平均评分。")
                ),
                getWorkers(null)
        );
    }

    public List<ServiceCategoryDto> getCategories() {
        return serviceCategoryMapper.selectList(
                        new LambdaQueryWrapper<ServiceCategoryEntity>()
                                .and(wrapper -> wrapper
                                        .isNull(ServiceCategoryEntity::getEnabled)
                                        .or()
                                        .eq(ServiceCategoryEntity::getEnabled, 1))
                                .orderByAsc(ServiceCategoryEntity::getId))
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public List<WorkerCardDto> getWorkers(String serviceName) {
        LambdaQueryWrapper<WorkerEntity> wrapper = new LambdaQueryWrapper<WorkerEntity>()
                .eq(WorkerEntity::getQualificationStatus, WorkerQualificationStatus.APPROVED);
        if (serviceName != null && !serviceName.isBlank()) {
            wrapper.like(WorkerEntity::getTags, serviceName);
        }
        wrapper.orderByDesc(WorkerEntity::getRating).orderByDesc(WorkerEntity::getCompletedOrders);
        return workerMapper.selectList(wrapper)
                .stream()
                .map(workerDtoMapper::toCardDto)
                .toList();
    }

    public WorkerDetailDto getWorker(Long id) {
        WorkerEntity worker = workerMapper.selectById(id);
        if (worker == null || !WorkerQualificationStatus.isPublicVisible(worker.getQualificationStatus())) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return workerDtoMapper.toDetailDto(worker);
    }

    public AdminDashboardDto getAdminDashboard() {
        List<OrderEntity> orders = orderMapper.selectList(null);
        List<WorkerEntity> workers = workerMapper.selectList(null);

        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream()
                .filter(order -> OrderStatus.COMPLETED.matches(order.getStatus()))
                .count();
        int activeWorkers = (int) workers.stream()
                .filter(item -> WorkerQualificationStatus.isPublicVisible(item.getQualificationStatus()))
                .count();
        double averageRating = workers.stream()
                .map(WorkerEntity::getRating)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(5.0);

        Map<String, Integer> serviceSales = new LinkedHashMap<>();
        orders.forEach(order -> serviceSales.merge(order.getServiceName(), 1, Integer::sum));

        return new AdminDashboardDto(totalOrders, completedOrders, activeWorkers, averageRating, serviceSales);
    }
}

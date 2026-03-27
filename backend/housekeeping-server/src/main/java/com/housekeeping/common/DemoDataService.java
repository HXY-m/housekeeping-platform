package com.housekeeping.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.common.mapper.CategoryMapper;
import com.housekeeping.common.mapper.OrderDtoMapper;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.home.dto.Highlight;
import com.housekeeping.home.dto.HomePageDto;
import com.housekeeping.home.dto.ServiceCategoryDto;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderRequest;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderProgressEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderProgressMapper;
import com.housekeeping.worker.dto.WorkerDetailDto;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DemoDataService {

    private final ServiceCategoryMapper serviceCategoryMapper;
    private final WorkerMapper workerDaoMapper;
    private final OrderMapper orderDaoMapper;
    private final OrderProgressMapper orderProgressMapper;
    private final CategoryMapper categoryMapper;
    private final WorkerDtoMapper workerDtoMapper;
    private final OrderDtoMapper orderDtoMapper;

    public DemoDataService(ServiceCategoryMapper serviceCategoryMapper,
                           WorkerMapper workerDaoMapper,
                           OrderMapper orderDaoMapper,
                           OrderProgressMapper orderProgressMapper,
                           CategoryMapper categoryMapper,
                           WorkerDtoMapper workerDtoMapper,
                           OrderDtoMapper orderDtoMapper) {
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.workerDaoMapper = workerDaoMapper;
        this.orderDaoMapper = orderDaoMapper;
        this.orderProgressMapper = orderProgressMapper;
        this.categoryMapper = categoryMapper;
        this.workerDtoMapper = workerDtoMapper;
        this.orderDtoMapper = orderDtoMapper;
    }

    public HomePageDto getHomePage() {
        return new HomePageDto(
                "像 Taskrabbit 一样下单，但更贴合本地家政服务场景",
                "围绕任务书要求，先打通服务浏览、预约下单、进度跟踪、评价售后与后台统计的 MVP 闭环。",
                getCategories(),
                List.of(
                        new Highlight("多角色协同", "覆盖雇主、服务人员、平台管理员三类角色，符合任务书要求。"),
                        new Highlight("预约与进度跟踪", "下单后可跟踪待服务、服务中、服务完成等关键节点。"),
                        new Highlight("评价与售后", "完成服务后可评分留言，也可提交售后反馈。")
                ),
                getWorkers(null)
        );
    }

    public List<ServiceCategoryDto> getCategories() {
        return serviceCategoryMapper.selectList(new LambdaQueryWrapper<ServiceCategoryEntity>()
                        .orderByAsc(ServiceCategoryEntity::getId))
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public List<WorkerCardDto> getWorkers(String serviceName) {
        LambdaQueryWrapper<WorkerEntity> wrapper = new LambdaQueryWrapper<>();
        if (serviceName != null && !serviceName.isBlank()) {
            wrapper.like(WorkerEntity::getTags, serviceName);
        }
        wrapper.orderByDesc(WorkerEntity::getRating).orderByDesc(WorkerEntity::getCompletedOrders);
        return workerDaoMapper.selectList(wrapper)
                .stream()
                .map(workerDtoMapper::toCardDto)
                .toList();
    }

    public WorkerDetailDto getWorker(Long id) {
        WorkerEntity worker = workerDaoMapper.selectById(id);
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return workerDtoMapper.toDetailDto(worker);
    }

    public List<OrderDto> getOrders() {
        List<OrderEntity> orders = orderDaoMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>().orderByDesc(OrderEntity::getId)
        );
        Map<Long, WorkerEntity> workerMap = workerDaoMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(WorkerEntity::getId, worker -> worker));

        return orders.stream()
                .map(order -> toOrderDto(order, workerMap))
                .toList();
    }

    @Transactional
    public OrderDto createOrder(OrderRequest request) {
        WorkerEntity worker = workerDaoMapper.selectById(request.workerId());
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        if (!workerDtoMapper.split(worker.getTags()).contains(request.serviceName())) {
            throw new BusinessException("该服务人员暂不支持所选服务项目");
        }

        OrderEntity order = new OrderEntity(
                request.serviceName(),
                worker.getId(),
                request.customerName(),
                request.contactPhone(),
                request.serviceAddress(),
                request.bookingDate(),
                request.bookingSlot(),
                "待服务",
                "订单已创建，等待服务人员确认",
                request.remark()
        );
        orderDaoMapper.insert(order);

        orderProgressMapper.insert(new OrderProgressEntity(
                order.getId(),
                "待服务",
                "订单已创建，等待服务人员确认"
        ));

        return orderDtoMapper.toDto(order, worker.getName(), "订单已创建，等待服务人员确认");
    }

    public AdminDashboardDto getAdminDashboard() {
        List<OrderEntity> orders = orderDaoMapper.selectList(null);
        List<WorkerEntity> workers = workerDaoMapper.selectList(null);

        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream()
                .filter(order -> "已完成".equals(order.getStatus()))
                .count();
        int activeWorkers = workers.size();
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

    private OrderDto toOrderDto(OrderEntity order, Map<Long, WorkerEntity> workerMap) {
        WorkerEntity worker = workerMap.get(order.getWorkerId());
        if (worker == null) {
            throw new BusinessException("订单关联的服务人员不存在");
        }

        OrderProgressEntity latestProgress = orderProgressMapper.selectOne(
                new LambdaQueryWrapper<OrderProgressEntity>()
                        .eq(OrderProgressEntity::getOrderId, order.getId())
                        .orderByDesc(OrderProgressEntity::getId)
                        .last("limit 1")
        );
        String latestProgressNote = latestProgress != null ? latestProgress.getProgressNote() : order.getProgressNote();
        return orderDtoMapper.toDto(order, worker.getName(), latestProgressNote);
    }
}

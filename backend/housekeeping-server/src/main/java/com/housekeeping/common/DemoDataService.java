package com.housekeeping.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.common.mapper.CategoryMapper;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.dashboard.dto.DashboardPaymentFlowDto;
import com.housekeeping.dashboard.dto.DashboardRevenuePointDto;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.home.dto.Highlight;
import com.housekeeping.home.dto.HomePageDto;
import com.housekeeping.home.dto.ServiceCategoryDto;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.PaymentStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderPaymentEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderPaymentMapper;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerDetailDto;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DemoDataService {

    private final ServiceCategoryMapper serviceCategoryMapper;
    private final WorkerMapper workerMapper;
    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final CategoryMapper categoryMapper;
    private final WorkerDtoMapper workerDtoMapper;

    public DemoDataService(ServiceCategoryMapper serviceCategoryMapper,
                           WorkerMapper workerMapper,
                           OrderMapper orderMapper,
                           OrderPaymentMapper orderPaymentMapper,
                           CategoryMapper categoryMapper,
                           WorkerDtoMapper workerDtoMapper) {
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.workerMapper = workerMapper;
        this.orderMapper = orderMapper;
        this.orderPaymentMapper = orderPaymentMapper;
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

    public PageResult<WorkerCardDto> pageWorkers(long current, long size, String serviceName) {
        LambdaQueryWrapper<WorkerEntity> wrapper = new LambdaQueryWrapper<WorkerEntity>()
                .eq(WorkerEntity::getQualificationStatus, WorkerQualificationStatus.APPROVED);
        if (serviceName != null && !serviceName.isBlank()) {
            wrapper.like(WorkerEntity::getTags, serviceName);
        }
        wrapper.orderByDesc(WorkerEntity::getRating).orderByDesc(WorkerEntity::getCompletedOrders);

        Page<WorkerEntity> page = workerMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.from(page, page.getRecords().stream().map(workerDtoMapper::toCardDto).toList());
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
        List<OrderPaymentEntity> payments = orderPaymentMapper.selectList(null).stream()
                .filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus()))
                .sorted((left, right) -> extractPaidDateTime(right).compareTo(extractPaidDateTime(left)))
                .toList();

        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(order -> OrderStatus.COMPLETED.matches(order.getStatus()))
                .count();
        long activeWorkers = workers.stream()
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

        Map<String, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(
                        item -> safeValue(item.getStatus(), "UNKNOWN"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        Map<Long, OrderEntity> orderMap = orders.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(OrderEntity::getId, Function.identity(), (left, right) -> left));
        Map<Long, WorkerEntity> workerMap = workers.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(WorkerEntity::getId, Function.identity(), (left, right) -> left));

        return new AdminDashboardDto(
                totalOrders,
                completedOrders,
                activeWorkers,
                averageRating,
                orders.stream().filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus())).count(),
                payments.stream()
                        .map(OrderPaymentEntity::getAmount)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum(),
                sumRevenueByDate(payments, LocalDate.now()),
                sumRevenueByMonth(payments, YearMonth.now()),
                serviceSales,
                statusDistribution,
                buildRevenueTrend(payments),
                buildPaymentFlows(payments.stream().limit(8).toList(), orderMap, workerMap)
        );
    }

    private long sumRevenueByDate(List<OrderPaymentEntity> payments, LocalDate date) {
        return payments.stream()
                .filter(payment -> date.equals(extractPaidDateTime(payment).toLocalDate()))
                .map(OrderPaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private long sumRevenueByMonth(List<OrderPaymentEntity> payments, YearMonth month) {
        return payments.stream()
                .filter(payment -> month.equals(YearMonth.from(extractPaidDateTime(payment))))
                .map(OrderPaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private List<DashboardRevenuePointDto> buildRevenueTrend(List<OrderPaymentEntity> payments) {
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        Map<LocalDate, long[]> buckets = new LinkedHashMap<>();
        for (int index = 6; index >= 0; index--) {
            buckets.put(today.minusDays(index), new long[]{0L, 0L});
        }
        payments.forEach(payment -> {
            LocalDate date = extractPaidDateTime(payment).toLocalDate();
            long[] bucket = buckets.get(date);
            if (bucket == null) {
                return;
            }
            bucket[0] += payment.getAmount() == null ? 0 : payment.getAmount();
            bucket[1] += 1;
        });
        return buckets.entrySet().stream()
                .map(entry -> new DashboardRevenuePointDto(
                        entry.getKey().format(labelFormatter),
                        entry.getValue()[0],
                        entry.getValue()[1]
                ))
                .toList();
    }

    private List<DashboardPaymentFlowDto> buildPaymentFlows(List<OrderPaymentEntity> payments,
                                                            Map<Long, OrderEntity> orderMap,
                                                            Map<Long, WorkerEntity> workerMap) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return payments.stream()
                .map(payment -> {
                    OrderEntity order = orderMap.get(payment.getOrderId());
                    WorkerEntity worker = order == null ? null : workerMap.get(order.getWorkerId());
                    return new DashboardPaymentFlowDto(
                            payment.getId(),
                            payment.getOrderId(),
                            order == null ? "未知服务" : safeValue(order.getServiceName(), "未知服务"),
                            order == null ? "未知客户" : safeValue(order.getCustomerName(), "未知客户"),
                            worker == null ? "未知服务人员" : safeValue(worker.getName(), "未知服务人员"),
                            payment.getAmount() == null ? 0 : payment.getAmount(),
                            safeValue(payment.getPaymentMethod(), "UNKNOWN"),
                            safeValue(payment.getPaymentStatus(), PaymentStatus.UNPAID.code()),
                            extractPaidDateTime(payment).equals(LocalDateTime.MIN) ? "" : extractPaidDateTime(payment).format(formatter)
                    );
                })
                .toList();
    }

    private LocalDateTime extractPaidDateTime(OrderPaymentEntity payment) {
        if (payment.getPaidAt() != null) {
            return payment.getPaidAt();
        }
        if (payment.getCreatedAt() != null) {
            return payment.getCreatedAt();
        }
        return LocalDateTime.MIN;
    }

    private String safeValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

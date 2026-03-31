package com.housekeeping.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.admin.dto.AdminDashboardDto;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.dashboard.dto.DashboardPaymentFlowDto;
import com.housekeeping.dashboard.dto.DashboardRevenuePointDto;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.PaymentStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderPaymentEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderPaymentMapper;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerDashboardDto;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TREND_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final WorkerMapper workerMapper;
    private final WorkerProfileService workerProfileService;

    public DashboardService(OrderMapper orderMapper,
                            OrderPaymentMapper orderPaymentMapper,
                            WorkerMapper workerMapper,
                            WorkerProfileService workerProfileService) {
        this.orderMapper = orderMapper;
        this.orderPaymentMapper = orderPaymentMapper;
        this.workerMapper = workerMapper;
        this.workerProfileService = workerProfileService;
    }

    public AdminDashboardDto getAdminDashboard() {
        List<OrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>().orderByDesc(OrderEntity::getId));
        List<WorkerEntity> workers = workerMapper.selectList(new LambdaQueryWrapper<WorkerEntity>().orderByDesc(WorkerEntity::getId));
        List<OrderPaymentEntity> payments = listPaidPayments();

        Map<Long, OrderEntity> orderMap = toOrderMap(orders);
        Map<Long, WorkerEntity> workerMap = toWorkerMap(workers);

        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(item -> OrderStatus.COMPLETED.matches(item.getStatus())).count();
        long activeWorkers = workers.stream()
                .filter(item -> WorkerQualificationStatus.isPublicVisible(item.getQualificationStatus()))
                .count();
        double averageRating = workers.stream()
                .map(WorkerEntity::getRating)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(5.0);

        return new AdminDashboardDto(
                totalOrders,
                completedOrders,
                activeWorkers,
                averageRating,
                orders.stream().filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus())).count(),
                sumRevenue(payments),
                sumRevenueByDate(payments, LocalDate.now()),
                sumRevenueByMonth(payments, YearMonth.now()),
                buildServiceSales(orders),
                buildStatusDistribution(orders),
                buildRevenueTrend(payments, 7),
                buildPaymentFlows(payments.stream().limit(8).toList(), orderMap, workerMap)
        );
    }

    public WorkerDashboardDto getCurrentWorkerDashboard() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }

        WorkerEntity worker = workerProfileService.requireWorkerByUserId(currentUser.userId());
        List<OrderEntity> orders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getWorkerId, worker.getId())
                        .orderByDesc(OrderEntity::getId)
        );
        Map<Long, OrderEntity> orderMap = toOrderMap(orders);
        List<OrderPaymentEntity> payments = listWorkerPaidPayments(orderMap.keySet());

        long pendingOrders = orders.stream().filter(item -> OrderStatus.PENDING.matches(item.getStatus())).count();
        long confirmedOrders = orders.stream().filter(item -> OrderStatus.CONFIRMED.matches(item.getStatus())).count();
        long inServiceOrders = orders.stream().filter(item -> OrderStatus.IN_SERVICE.matches(item.getStatus())).count();
        long waitingUserConfirmationOrders = orders.stream()
                .filter(item -> OrderStatus.WAITING_USER_CONFIRMATION.matches(item.getStatus()))
                .count();
        long completedOrders = orders.stream().filter(item -> OrderStatus.COMPLETED.matches(item.getStatus())).count();

        return new WorkerDashboardDto(
                worker.getQualificationStatus(),
                orders.size(),
                pendingOrders,
                confirmedOrders,
                inServiceOrders,
                waitingUserConfirmationOrders,
                completedOrders,
                pendingOrders + confirmedOrders + inServiceOrders,
                orders.stream().filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus())).count(),
                sumRevenue(payments),
                sumRevenueByDate(payments, LocalDate.now()),
                sumRevenueByMonth(payments, YearMonth.now()),
                buildServiceSales(orders),
                buildStatusDistribution(orders),
                buildRevenueTrend(payments, 7),
                buildPaymentFlows(payments.stream().limit(8).toList(), orderMap, Map.of(worker.getId(), worker))
        );
    }

    private List<OrderPaymentEntity> listPaidPayments() {
        return orderPaymentMapper.selectList(
                new LambdaQueryWrapper<OrderPaymentEntity>()
                        .eq(OrderPaymentEntity::getPaymentStatus, PaymentStatus.PAID.code())
                        .orderByDesc(OrderPaymentEntity::getPaidAt)
                        .orderByDesc(OrderPaymentEntity::getId)
        );
    }

    private List<OrderPaymentEntity> listWorkerPaidPayments(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderPaymentMapper.selectList(
                new LambdaQueryWrapper<OrderPaymentEntity>()
                        .in(OrderPaymentEntity::getOrderId, orderIds)
                        .eq(OrderPaymentEntity::getPaymentStatus, PaymentStatus.PAID.code())
                        .orderByDesc(OrderPaymentEntity::getPaidAt)
                        .orderByDesc(OrderPaymentEntity::getId)
        );
    }

    private Map<Long, OrderEntity> toOrderMap(List<OrderEntity> orders) {
        return orders.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(OrderEntity::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, WorkerEntity> toWorkerMap(List<WorkerEntity> workers) {
        return workers.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(WorkerEntity::getId, Function.identity(), (left, right) -> left));
    }

    private Map<String, Integer> buildServiceSales(List<OrderEntity> orders) {
        Map<String, Integer> serviceSales = new LinkedHashMap<>();
        orders.forEach(order -> serviceSales.merge(
                safeValue(order.getServiceName(), "未分类服务"),
                1,
                Integer::sum
        ));
        return serviceSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<String, Long> buildStatusDistribution(List<OrderEntity> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        item -> safeValue(item.getStatus(), "UNKNOWN"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    private List<DashboardRevenuePointDto> buildRevenueTrend(List<OrderPaymentEntity> payments, int days) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, long[]> trendMap = new LinkedHashMap<>();
        for (int index = days - 1; index >= 0; index--) {
            trendMap.put(today.minusDays(index), new long[]{0L, 0L});
        }

        payments.forEach(payment -> {
            LocalDate paidDate = extractPaidDate(payment);
            long[] bucket = trendMap.get(paidDate);
            if (bucket == null) {
                return;
            }
            bucket[0] += payment.getAmount() == null ? 0 : payment.getAmount();
            bucket[1] += 1;
        });

        return trendMap.entrySet().stream()
                .map(entry -> new DashboardRevenuePointDto(
                        entry.getKey().format(TREND_LABEL_FORMATTER),
                        entry.getValue()[0],
                        entry.getValue()[1]
                ))
                .toList();
    }

    private List<DashboardPaymentFlowDto> buildPaymentFlows(List<OrderPaymentEntity> payments,
                                                            Map<Long, OrderEntity> orderMap,
                                                            Map<Long, WorkerEntity> workerMap) {
        return payments.stream()
                .sorted(Comparator.comparing(this::extractPaidDateTime).reversed())
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
                            formatDateTime(payment.getPaidAt() == null ? payment.getCreatedAt() : payment.getPaidAt())
                    );
                })
                .toList();
    }

    private long sumRevenue(List<OrderPaymentEntity> payments) {
        return payments.stream()
                .map(OrderPaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private long sumRevenueByDate(List<OrderPaymentEntity> payments, LocalDate date) {
        return payments.stream()
                .filter(payment -> date.equals(extractPaidDate(payment)))
                .map(OrderPaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private long sumRevenueByMonth(List<OrderPaymentEntity> payments, YearMonth month) {
        return payments.stream()
                .filter(payment -> month.equals(YearMonth.from(extractPaidDate(payment))))
                .map(OrderPaymentEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private LocalDate extractPaidDate(OrderPaymentEntity payment) {
        return extractPaidDateTime(payment).toLocalDate();
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

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private String safeValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

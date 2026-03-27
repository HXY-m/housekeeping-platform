package com.housekeeping.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.common.mapper.OrderDtoMapper;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderRequest;
import com.housekeeping.order.dto.OrderReviewRequest;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderProgressEntity;
import com.housekeeping.order.entity.OrderReviewEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderProgressMapper;
import com.housekeeping.order.mapper.OrderReviewMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderProgressMapper orderProgressMapper;
    private final OrderReviewMapper orderReviewMapper;
    private final WorkerMapper workerMapper;
    private final WorkerDtoMapper workerDtoMapper;
    private final OrderDtoMapper orderDtoMapper;

    public OrderService(OrderMapper orderMapper,
                        OrderProgressMapper orderProgressMapper,
                        OrderReviewMapper orderReviewMapper,
                        WorkerMapper workerMapper,
                        WorkerDtoMapper workerDtoMapper,
                        OrderDtoMapper orderDtoMapper) {
        this.orderMapper = orderMapper;
        this.orderProgressMapper = orderProgressMapper;
        this.orderReviewMapper = orderReviewMapper;
        this.workerMapper = workerMapper;
        this.workerDtoMapper = workerDtoMapper;
        this.orderDtoMapper = orderDtoMapper;
    }

    public List<OrderDto> listCurrentUserOrders() {
        SessionUser currentUser = requireCurrentUser();
        return buildOrderDtos(orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, currentUser.userId())
                        .orderByDesc(OrderEntity::getId)
        ));
    }

    public List<OrderDto> listCurrentWorkerOrders() {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = requireWorkerByUserId(currentUser.userId());
        return buildOrderDtos(orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getWorkerId, worker.getId())
                        .orderByDesc(OrderEntity::getId)
        ));
    }

    @Transactional
    public OrderDto createOrder(OrderRequest request) {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = requireWorker(request.workerId());
        if (!workerDtoMapper.split(worker.getTags()).contains(request.serviceName())) {
            throw new BusinessException("该服务人员暂不支持所选服务项目");
        }

        OrderEntity order = new OrderEntity(
                currentUser.userId(),
                request.serviceName(),
                worker.getId(),
                request.customerName(),
                request.contactPhone(),
                request.serviceAddress(),
                request.bookingDate(),
                request.bookingSlot(),
                OrderStatus.PENDING.code(),
                "订单已创建，等待服务人员接单",
                request.remark()
        );
        orderMapper.insert(order);
        appendProgress(order.getId(), OrderStatus.PENDING.code(), "订单已创建，等待服务人员接单");
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto acceptOrder(Long orderId) {
        return updateOrderByWorker(orderId, OrderStatus.ACCEPTED, "服务人员已接单，请保持电话畅通");
    }

    @Transactional
    public OrderDto startOrder(Long orderId) {
        return updateOrderByWorker(orderId, OrderStatus.IN_SERVICE, "服务已开始，服务人员正在处理");
    }

    @Transactional
    public OrderDto completeOrder(Long orderId) {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = requireWorkerByUserId(currentUser.userId());
        OrderEntity order = requireWorkerOwnedOrder(orderId, worker.getId());

        if (OrderStatus.COMPLETED.matches(order.getStatus())) {
            throw new BusinessException("该订单已经完成");
        }

        updateOrderStatus(order, OrderStatus.COMPLETED, "服务已完成，等待用户评价");
        worker.setCompletedOrders((worker.getCompletedOrders() == null ? 0 : worker.getCompletedOrders()) + 1);
        workerMapper.updateById(worker);
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto reviewOrder(Long orderId, OrderReviewRequest request) {
        SessionUser currentUser = requireCurrentUser();
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("未找到对应的订单");
        }
        if (!Objects.equals(order.getUserId(), currentUser.userId())) {
            throw new BusinessException("只能评价自己的订单");
        }
        if (!OrderStatus.COMPLETED.matches(order.getStatus())) {
            throw new BusinessException("只有已完成订单才可以评价");
        }

        OrderReviewEntity existed = orderReviewMapper.selectOne(
                new LambdaQueryWrapper<OrderReviewEntity>()
                        .eq(OrderReviewEntity::getOrderId, orderId)
                        .last("limit 1")
        );
        if (existed != null) {
            throw new BusinessException("该订单已经评价过了");
        }

        orderReviewMapper.insert(new OrderReviewEntity(
                orderId,
                currentUser.userId(),
                order.getWorkerId(),
                request.rating(),
                request.content().trim(),
                LocalDateTime.now()
        ));
        refreshWorkerRating(order.getWorkerId());
        return buildSingleOrderDto(order);
    }

    private OrderDto updateOrderByWorker(Long orderId, OrderStatus nextStatus, String note) {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = requireWorkerByUserId(currentUser.userId());
        OrderEntity order = requireWorkerOwnedOrder(orderId, worker.getId());

        if (OrderStatus.COMPLETED.matches(order.getStatus())) {
            throw new BusinessException("该订单已经完成，无法继续流转");
        }
        if (nextStatus == OrderStatus.ACCEPTED && OrderStatus.ACCEPTED.matches(order.getStatus())) {
            throw new BusinessException("该订单已经接单");
        }
        if (nextStatus == OrderStatus.IN_SERVICE && OrderStatus.IN_SERVICE.matches(order.getStatus())) {
            throw new BusinessException("该订单已经开始服务");
        }

        updateOrderStatus(order, nextStatus, note);
        return buildSingleOrderDto(order);
    }

    private void updateOrderStatus(OrderEntity order, OrderStatus nextStatus, String note) {
        order.setStatus(nextStatus.code());
        order.setProgressNote(note);
        orderMapper.updateById(order);
        appendProgress(order.getId(), nextStatus.code(), note);
    }

    private void appendProgress(Long orderId, String status, String note) {
        orderProgressMapper.insert(new OrderProgressEntity(orderId, status, note));
    }

    private OrderEntity requireWorkerOwnedOrder(Long orderId, Long workerId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("未找到对应的订单");
        }
        if (!Objects.equals(order.getWorkerId(), workerId)) {
            throw new BusinessException("只能处理分配给自己的订单");
        }
        return order;
    }

    private WorkerEntity requireWorker(Long workerId) {
        WorkerEntity worker = workerMapper.selectById(workerId);
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return worker;
    }

    private WorkerEntity requireWorkerByUserId(Long userId) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            throw new BusinessException("当前账号还没有绑定服务人员档案");
        }
        return worker;
    }

    private OrderDto buildSingleOrderDto(OrderEntity order) {
        return buildOrderDtos(List.of(order)).get(0);
    }

    private List<OrderDto> buildOrderDtos(List<OrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Long> workerIds = orders.stream()
                .map(OrderEntity::getWorkerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, WorkerEntity> workerMap = workerIds.isEmpty()
                ? Map.of()
                : workerMapper.selectBatchIds(workerIds).stream()
                .collect(Collectors.toMap(WorkerEntity::getId, Function.identity()));

        List<Long> orderIds = orders.stream()
                .map(OrderEntity::getId)
                .toList();

        Map<Long, String> progressMap = buildLatestProgressMap(orderIds);
        Map<Long, OrderReviewEntity> reviewMap = buildReviewMap(orderIds);

        return orders.stream()
                .map(order -> {
                    WorkerEntity worker = workerMap.get(order.getWorkerId());
                    if (worker == null) {
                        throw new BusinessException("订单关联的服务人员不存在");
                    }
                    return orderDtoMapper.toDto(
                            order,
                            worker.getName(),
                            progressMap.getOrDefault(order.getId(), order.getProgressNote()),
                            reviewMap.get(order.getId())
                    );
                })
                .toList();
    }

    private Map<Long, String> buildLatestProgressMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> progressMap = new LinkedHashMap<>();
        orderProgressMapper.selectList(
                        new LambdaQueryWrapper<OrderProgressEntity>()
                                .in(OrderProgressEntity::getOrderId, orderIds)
                                .orderByDesc(OrderProgressEntity::getId)
                ).forEach(item -> progressMap.putIfAbsent(item.getOrderId(), item.getProgressNote()));
        return progressMap;
    }

    private Map<Long, OrderReviewEntity> buildReviewMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return orderReviewMapper.selectList(
                        new LambdaQueryWrapper<OrderReviewEntity>()
                                .in(OrderReviewEntity::getOrderId, orderIds)
                                .orderByDesc(OrderReviewEntity::getId)
                ).stream()
                .collect(Collectors.toMap(
                        OrderReviewEntity::getOrderId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private void refreshWorkerRating(Long workerId) {
        List<OrderReviewEntity> reviews = orderReviewMapper.selectList(
                new LambdaQueryWrapper<OrderReviewEntity>()
                        .eq(OrderReviewEntity::getWorkerId, workerId)
        );
        if (reviews.isEmpty()) {
            return;
        }

        WorkerEntity worker = requireWorker(workerId);
        double average = reviews.stream()
                .map(OrderReviewEntity::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(worker.getRating() == null ? 5.0 : worker.getRating());
        worker.setRating(Math.round(average * 100.0) / 100.0);
        workerMapper.updateById(worker);
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }
}

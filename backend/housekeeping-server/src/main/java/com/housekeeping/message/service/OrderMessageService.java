package com.housekeeping.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.message.dto.OrderConversationDto;
import com.housekeeping.message.dto.OrderMessageDto;
import com.housekeeping.message.entity.OrderMessageEntity;
import com.housekeeping.message.mapper.OrderMessageMapper;
import com.housekeeping.notification.NotificationType;
import com.housekeeping.notification.service.NotificationService;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.service.OrderAccessService;
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
public class OrderMessageService {

    private final OrderMessageMapper orderMessageMapper;
    private final OrderAccessService orderAccessService;
    private final WorkerMapper workerMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    public OrderMessageService(OrderMessageMapper orderMessageMapper,
                               OrderAccessService orderAccessService,
                               WorkerMapper workerMapper,
                               NotificationService notificationService,
                               OperationLogService operationLogService) {
        this.orderMessageMapper = orderMessageMapper;
        this.orderAccessService = orderAccessService;
        this.workerMapper = workerMapper;
        this.notificationService = notificationService;
        this.operationLogService = operationLogService;
    }

    public List<OrderConversationDto> listCurrentConversations() {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        List<OrderEntity> orders = orderAccessService.listCurrentRoleOrders();
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();
        List<OrderMessageEntity> messages = orderMessageMapper.selectList(
                new LambdaQueryWrapper<OrderMessageEntity>()
                        .in(OrderMessageEntity::getOrderId, orderIds)
                        .orderByDesc(OrderMessageEntity::getId)
        );
        Map<Long, OrderMessageEntity> latestMessageMap = new LinkedHashMap<>();
        messages.forEach(item -> latestMessageMap.putIfAbsent(item.getOrderId(), item));

        Map<Long, WorkerEntity> workerMap = workerMapper.selectBatchIds(
                        orders.stream().map(OrderEntity::getWorkerId).filter(Objects::nonNull).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(WorkerEntity::getId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    OrderMessageEntity latestMessage = latestMessageMap.get(order.getId());
                    return new OrderConversationDto(
                            order.getId(),
                            order.getServiceName(),
                            resolveCounterpartName(currentUser.roleCode(), order, workerMap),
                            order.getBookingDate(),
                            order.getBookingSlot(),
                            order.getStatus(),
                            order.getProgressNote(),
                            latestMessage == null ? "" : latestMessage.getContent(),
                            latestMessage == null ? null : latestMessage.getCreatedAt()
                    );
                })
                .sorted((left, right) -> {
                    LocalDateTime leftTime = left.latestMessageAt() == null ? LocalDateTime.MIN : left.latestMessageAt();
                    LocalDateTime rightTime = right.latestMessageAt() == null ? LocalDateTime.MIN : right.latestMessageAt();
                    return rightTime.compareTo(leftTime);
                })
                .toList();
    }

    public List<OrderMessageDto> listOrderMessages(Long orderId) {
        orderAccessService.requireCurrentRoleOrder(orderId);
        return orderMessageMapper.selectList(
                        new LambdaQueryWrapper<OrderMessageEntity>()
                                .eq(OrderMessageEntity::getOrderId, orderId)
                                .orderByAsc(OrderMessageEntity::getId)
                ).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderMessageDto sendMessage(Long orderId, String content) {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        OrderEntity order = orderAccessService.requireCurrentRoleOrder(orderId);

        OrderMessageEntity entity = new OrderMessageEntity(
                orderId,
                currentUser.userId(),
                currentUser.roleCode(),
                currentUser.realName(),
                content.trim(),
                LocalDateTime.now()
        );
        orderMessageMapper.insert(entity);
        operationLogService.record(
                OperationLogActions.ORDER_MESSAGE_SEND,
                "ORDER_MESSAGE",
                entity.getId(),
                "订单留言，订单ID=" + orderId
        );
        notifyReceiver(currentUser, order, content.trim());
        return toDto(entity);
    }

    private void notifyReceiver(SessionUser sender, OrderEntity order, String content) {
        String preview = content.length() > 60 ? content.substring(0, 60) + "..." : content;
        String title = "订单 #" + order.getId() + " 有新的沟通消息";
        String actionPath = RoleCodes.USER.equals(sender.roleCode())
                ? "/worker/conversations?orderId=" + order.getId()
                : "/user/conversations?orderId=" + order.getId();

        if (RoleCodes.USER.equals(sender.roleCode())) {
            WorkerEntity worker = workerMapper.selectById(order.getWorkerId());
            if (worker != null && worker.getUserId() != null) {
                notificationService.notifyUser(
                        worker.getUserId(),
                        RoleCodes.WORKER,
                        NotificationType.ORDER_MESSAGE,
                        title,
                        sender.realName() + "：" + preview,
                        "ORDER",
                        order.getId(),
                        actionPath
                );
            }
            return;
        }

        if (RoleCodes.WORKER.equals(sender.roleCode()) && order.getUserId() != null) {
            notificationService.notifyUser(
                    order.getUserId(),
                    RoleCodes.USER,
                    NotificationType.ORDER_MESSAGE,
                    title,
                    sender.realName() + "：" + preview,
                    "ORDER",
                    order.getId(),
                    actionPath
            );
        }
    }

    private String resolveCounterpartName(String roleCode,
                                          OrderEntity order,
                                          Map<Long, WorkerEntity> workerMap) {
        if (RoleCodes.USER.equals(roleCode)) {
            WorkerEntity worker = workerMap.get(order.getWorkerId());
            return worker == null ? "服务人员" : worker.getName();
        }
        return order.getCustomerName();
    }

    private OrderMessageDto toDto(OrderMessageEntity entity) {
        return new OrderMessageDto(
                entity.getId(),
                entity.getOrderId(),
                entity.getSenderUserId(),
                entity.getSenderRoleCode(),
                entity.getSenderName(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}

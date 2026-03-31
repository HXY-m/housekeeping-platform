package com.housekeeping.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.housekeeping.aftersale.entity.AfterSaleEntity;
import com.housekeeping.aftersale.mapper.AfterSaleMapper;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.common.PageResult;
import com.housekeeping.common.mapper.OrderDtoMapper;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.notification.NotificationType;
import com.housekeeping.notification.service.NotificationService;
import com.housekeeping.order.OrderServiceRecordStage;
import com.housekeeping.order.OrderPaymentMethod;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.PaymentStatus;
import com.housekeeping.order.dto.BookingAvailabilityDto;
import com.housekeeping.order.dto.OrderDto;
import com.housekeeping.order.dto.OrderPaymentDto;
import com.housekeeping.order.dto.OrderPaymentRequest;
import com.housekeeping.order.dto.OrderRequest;
import com.housekeeping.order.dto.OrderReviewRequest;
import com.housekeeping.order.dto.OrderServiceRecordDto;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderPaymentEntity;
import com.housekeeping.order.entity.OrderProgressEntity;
import com.housekeeping.order.entity.OrderReviewEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderPaymentMapper;
import com.housekeeping.order.mapper.OrderProgressMapper;
import com.housekeeping.order.mapper.OrderReviewMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final List<String> BOOKING_SLOTS = List.of(
            "08:00-10:00",
            "10:00-12:00",
            "13:00-15:00",
            "15:00-17:00",
            "18:00-20:00"
    );

    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final OrderProgressMapper orderProgressMapper;
    private final OrderReviewMapper orderReviewMapper;
    private final WorkerMapper workerMapper;
    private final WorkerProfileService workerProfileService;
    private final WorkerDtoMapper workerDtoMapper;
    private final OrderDtoMapper orderDtoMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final OrderServiceRecordService orderServiceRecordService;
    private final OperationLogService operationLogService;
    private final OrderAccessService orderAccessService;
    private final NotificationService notificationService;

    public OrderService(OrderMapper orderMapper,
                        OrderPaymentMapper orderPaymentMapper,
                        OrderProgressMapper orderProgressMapper,
                        OrderReviewMapper orderReviewMapper,
                        WorkerMapper workerMapper,
                        WorkerProfileService workerProfileService,
                        WorkerDtoMapper workerDtoMapper,
                        OrderDtoMapper orderDtoMapper,
                        AfterSaleMapper afterSaleMapper,
                        OrderServiceRecordService orderServiceRecordService,
                        OperationLogService operationLogService,
                        OrderAccessService orderAccessService,
                        NotificationService notificationService) {
        this.orderMapper = orderMapper;
        this.orderPaymentMapper = orderPaymentMapper;
        this.orderProgressMapper = orderProgressMapper;
        this.orderReviewMapper = orderReviewMapper;
        this.workerMapper = workerMapper;
        this.workerProfileService = workerProfileService;
        this.workerDtoMapper = workerDtoMapper;
        this.orderDtoMapper = orderDtoMapper;
        this.afterSaleMapper = afterSaleMapper;
        this.orderServiceRecordService = orderServiceRecordService;
        this.operationLogService = operationLogService;
        this.orderAccessService = orderAccessService;
        this.notificationService = notificationService;
    }

    public List<OrderDto> listCurrentUserOrders() {
        return buildOrderDtos(orderAccessService.listCurrentUserOrders());
    }

    public PageResult<OrderDto> pageCurrentUserOrders(long current, long size, String status) {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        Page<OrderEntity> page = orderMapper.selectPage(
                new Page<>(current, size),
                buildCurrentUserOrderWrapper(currentUser.userId(), status)
        );
        return PageResult.from(page, buildOrderDtos(page.getRecords()));
    }

    public OrderDto getCurrentUserOrder(Long orderId) {
        return buildSingleOrderDto(orderAccessService.requireCurrentUserOrder(orderId));
    }

    public List<OrderDto> listCurrentWorkerOrders() {
        return buildOrderDtos(orderAccessService.listCurrentWorkerOrders());
    }

    public PageResult<OrderDto> pageCurrentWorkerOrders(long current, long size, String status) {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        WorkerEntity worker = workerProfileService.requireWorkerByUserId(currentUser.userId());
        Page<OrderEntity> page = orderMapper.selectPage(
                new Page<>(current, size),
                buildCurrentWorkerOrderWrapper(worker.getId(), status)
        );
        return PageResult.from(page, buildOrderDtos(page.getRecords()));
    }

    public OrderDto getCurrentWorkerOrder(Long orderId) {
        return buildSingleOrderDto(orderAccessService.requireCurrentWorkerOrder(orderId));
    }

    public List<OrderDto> listAllOrders() {
        return buildOrderDtos(orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>().orderByDesc(OrderEntity::getId)
        ));
    }

    public PageResult<OrderDto> pageAllOrders(long current,
                                              long size,
                                              String status,
                                              String keyword,
                                              String dateFrom,
                                              String dateTo) {
        Page<OrderEntity> page = orderMapper.selectPage(
                new Page<>(current, size),
                buildAdminOrderWrapper(status, keyword, dateFrom, dateTo)
        );
        return PageResult.from(page, buildOrderDtos(page.getRecords()));
    }

    public Map<String, Long> summarizeCurrentUserOrders(String status) {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        List<OrderEntity> orders = orderMapper.selectList(buildCurrentUserOrderWrapper(currentUser.userId(), status));
        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();
        long afterSales = orderIds.isEmpty()
                ? 0
                : afterSaleMapper.selectCount(new LambdaQueryWrapper<AfterSaleEntity>()
                .eq(AfterSaleEntity::getUserId, currentUser.userId())
                .in(AfterSaleEntity::getOrderId, orderIds));

        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", (long) orders.size());
        summary.put("active", orders.stream().filter(item -> !OrderStatus.COMPLETED.matches(item.getStatus())).count());
        summary.put("needUserAction", orders.stream().filter(item ->
                OrderStatus.ACCEPTED.matches(item.getStatus())
                        || OrderStatus.WAITING_USER_CONFIRMATION.matches(item.getStatus())
        ).count());
        summary.put("paid", orders.stream().filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus())).count());
        summary.put("unpaid", orders.stream().filter(item -> !PaymentStatus.PAID.matches(item.getPaymentStatus())).count());
        summary.put("afterSales", afterSales);
        return summary;
    }

    public Map<String, Long> summarizeCurrentWorkerOrders(String status) {
        SessionUser currentUser = orderAccessService.requireCurrentUser();
        WorkerEntity worker = workerProfileService.requireWorkerByUserId(currentUser.userId());
        List<OrderEntity> orders = orderMapper.selectList(buildCurrentWorkerOrderWrapper(worker.getId(), status));
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", (long) orders.size());
        summary.put("pending", orders.stream().filter(item -> OrderStatus.PENDING.matches(item.getStatus())).count());
        summary.put("accepted", orders.stream().filter(item -> OrderStatus.ACCEPTED.matches(item.getStatus())).count());
        summary.put("confirmed", orders.stream().filter(item -> OrderStatus.CONFIRMED.matches(item.getStatus())).count());
        summary.put("inService", orders.stream().filter(item -> OrderStatus.IN_SERVICE.matches(item.getStatus())).count());
        summary.put("waitingUserConfirmation", orders.stream().filter(item -> OrderStatus.WAITING_USER_CONFIRMATION.matches(item.getStatus())).count());
        summary.put(
                "todo",
                summary.get("pending") + summary.get("confirmed") + summary.get("inService")
        );
        return summary;
    }

    public Map<String, Long> summarizeAllOrders(String status,
                                                String keyword,
                                                String dateFrom,
                                                String dateTo) {
        List<OrderEntity> orders = orderMapper.selectList(buildAdminOrderWrapper(status, keyword, dateFrom, dateTo));
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", (long) orders.size());
        summary.put("pending", orders.stream().filter(item -> OrderStatus.PENDING.matches(item.getStatus())).count());
        summary.put("inService", orders.stream().filter(item -> OrderStatus.IN_SERVICE.matches(item.getStatus())).count());
        summary.put("completed", orders.stream().filter(item -> OrderStatus.COMPLETED.matches(item.getStatus())).count());
        summary.put("paid", orders.stream().filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus())).count());
        summary.put("unpaid", orders.stream().filter(item -> !PaymentStatus.PAID.matches(item.getPaymentStatus())).count());
        summary.put(
                "paidAmount",
                orders.stream()
                        .filter(item -> PaymentStatus.PAID.matches(item.getPaymentStatus()))
                        .map(OrderEntity::getPayableAmount)
                        .filter(Objects::nonNull)
                        .mapToLong(Integer::longValue)
                        .sum()
        );
        return summary;
    }

    public BookingAvailabilityDto getBookingAvailability(Long workerId, String bookingDate) {
        workerProfileService.requireApprovedWorker(workerId);
        LocalDate parsedDate = parseBookingDate(bookingDate);

        List<String> occupiedSlots = orderMapper.selectList(
                        new LambdaQueryWrapper<OrderEntity>()
                                .eq(OrderEntity::getWorkerId, workerId)
                                .eq(OrderEntity::getBookingDate, parsedDate.toString())
                                .notIn(OrderEntity::getStatus, List.of(OrderStatus.COMPLETED.code()))
                                .orderByAsc(OrderEntity::getId))
                .stream()
                .map(OrderEntity::getBookingSlot)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> availableSlots = BOOKING_SLOTS.stream()
                .filter(slot -> !occupiedSlots.contains(slot))
                .toList();

        return new BookingAvailabilityDto(workerId, parsedDate.toString(), availableSlots, occupiedSlots);
    }

    public List<OrderPaymentDto> listCurrentUserOrderPayments(Long orderId) {
        OrderEntity order = orderAccessService.requireCurrentUserOrder(orderId);
        return orderPaymentMapper.selectList(
                        new LambdaQueryWrapper<OrderPaymentEntity>()
                                .eq(OrderPaymentEntity::getOrderId, order.getId())
                                .orderByDesc(OrderPaymentEntity::getId)
                ).stream()
                .map(this::toPaymentDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(OrderRequest request) {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = workerProfileService.requireApprovedWorker(request.workerId());
        if (!workerDtoMapper.split(worker.getTags()).contains(request.serviceName())) {
            throw new BusinessException("该服务人员暂不支持所选服务项目");
        }

        String normalizedDate = parseBookingDate(request.bookingDate()).toString();
        String normalizedSlot = normalizeBookingSlot(request.bookingSlot());
        ensureSlotAvailable(worker.getId(), normalizedDate, normalizedSlot);

        OrderEntity order = new OrderEntity(
                currentUser.userId(),
                request.serviceName(),
                worker.getId(),
                request.customerName().trim(),
                request.contactPhone().trim(),
                request.serviceAddress().trim(),
                normalizedDate,
                normalizedSlot,
                OrderStatus.PENDING.code(),
                "订单已创建，等待服务人员接单",
                request.remark().trim()
        );
        order.setPayableAmount(resolveOrderAmount(worker));
        order.setPaymentStatus(PaymentStatus.UNPAID.code());
        order.setPaymentMethod("");
        order.setPaidAt(null);
        orderMapper.insert(order);
        appendProgress(order.getId(), OrderStatus.PENDING, "订单已创建，等待服务人员接单");
        operationLogService.record(
                OperationLogActions.ORDER_CREATE,
                "ORDER",
                order.getId(),
                "创建订单，服务项目：" + order.getServiceName() + "，服务人员ID=" + order.getWorkerId()
        );
        notifyWorkerStatus(
                worker,
                "你有新的预约待处理",
                currentUser.realName() + " 提交了 " + order.getServiceName() + " 预约，等待你接单。",
                order.getId(),
                "/worker/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long orderId, OrderPaymentRequest request) {
        SessionUser currentUser = requireCurrentUser();
        OrderEntity order = orderAccessService.requireCurrentUserOrder(orderId);
        if (PaymentStatus.PAID.matches(order.getPaymentStatus())) {
            throw new BusinessException("该订单已经支付完成");
        }

        OrderPaymentMethod paymentMethod;
        try {
            paymentMethod = OrderPaymentMethod.from(request.paymentMethod());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(exception.getMessage());
        }

        int payableAmount = order.getPayableAmount() == null ? 0 : order.getPayableAmount();
        LocalDateTime paidAt = LocalDateTime.now();
        String paymentNo = generatePaymentNo(order.getId());

        orderPaymentMapper.insert(new OrderPaymentEntity(
                order.getId(),
                currentUser.userId(),
                payableAmount,
                paymentMethod.code(),
                PaymentStatus.PAID.code(),
                paymentNo,
                paidAt,
                paidAt
        ));

        order.setPaymentStatus(PaymentStatus.PAID.code());
        order.setPaymentMethod(paymentMethod.code());
        order.setPaidAt(paidAt);
        orderMapper.updateById(order);

        operationLogService.record(
                OperationLogActions.ORDER_PAY,
                "ORDER",
                orderId,
                "订单支付成功，方式=" + paymentMethod.code() + "，金额=" + payableAmount
        );
        notifyWorkerStatus(
                requireWorker(order.getWorkerId()),
                "用户已完成支付",
                "订单 #" + order.getId() + " 已完成支付，金额 ¥" + payableAmount + "，可继续关注后续确认进度。",
                order.getId(),
                "/worker/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto acceptOrder(Long orderId) {
        ensureCurrentWorkerQualified();
        OrderEntity order = orderAccessService.requireCurrentWorkerOrder(orderId);
        requireStatus(order, OrderStatus.PENDING, "只有待接单订单才能接单");
        updateOrderStatus(order, OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排");
        operationLogService.record(OperationLogActions.ORDER_ACCEPT, "ORDER", orderId, "服务人员已接单");
        notifyUserStatus(
                order.getUserId(),
                "服务人员已接单",
                "订单 #" + order.getId() + " 已由服务人员接单，请确认预约安排。",
                order.getId(),
                "/user/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto confirmOrderByUser(Long orderId) {
        OrderEntity order = orderAccessService.requireCurrentUserOrder(orderId);
        requireStatus(order, OrderStatus.ACCEPTED, "只有服务人员接单后，用户才能确认预约");
        if (!PaymentStatus.PAID.matches(order.getPaymentStatus())) {
            throw new BusinessException("请先完成支付，再确认预约安排");
        }
        updateOrderStatus(order, OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门");
        appendProgress(order.getId(), OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门");
        notifyWorkerStatus(
                requireWorker(order.getWorkerId()),
                "用户已确认预约安排",
                "订单 #" + order.getId() + " 已由用户确认，你可以按约上门服务。",
                order.getId(),
                "/worker/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto startOrder(Long orderId) {
        ensureCurrentWorkerQualified();
        OrderEntity order = orderAccessService.requireCurrentWorkerOrder(orderId);
        requireStatus(order, OrderStatus.CONFIRMED, "用户确认预约后才能开始服务");
        updateOrderStatus(order, OrderStatus.IN_SERVICE, "服务已开始，服务人员正在上门处理");
        operationLogService.record(OperationLogActions.ORDER_START, "ORDER", orderId, "服务人员开始服务");
        notifyUserStatus(
                order.getUserId(),
                "服务已开始",
                "订单 #" + order.getId() + " 已开始服务，服务人员正在处理你的预约。",
                order.getId(),
                "/user/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto submitCompletionByWorker(Long orderId) {
        ensureCurrentWorkerQualified();
        OrderEntity order = orderAccessService.requireCurrentWorkerOrder(orderId);
        requireStatus(order, OrderStatus.IN_SERVICE, "只有服务中的订单才能提交完工");
        if (!orderServiceRecordService.hasStage(orderId, OrderServiceRecordStage.FINISH_PROOF)) {
            throw new BusinessException("提交完工前，请先上传至少一条完工凭证");
        }
        updateOrderStatus(order, OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认");
        operationLogService.record(OperationLogActions.ORDER_COMPLETE, "ORDER", orderId, "服务人员提交完工");
        notifyUserStatus(
                order.getUserId(),
                "服务人员已提交完工",
                "订单 #" + order.getId() + " 已提交完工，请及时确认服务结果。",
                order.getId(),
                "/user/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderDto confirmCompletionByUser(Long orderId) {
        OrderEntity order = orderAccessService.requireCurrentUserOrder(orderId);
        requireStatus(order, OrderStatus.WAITING_USER_CONFIRMATION, "只有待用户确认完工的订单才能完成");
        updateOrderStatus(order, OrderStatus.COMPLETED, "用户已确认完工，订单完成");

        WorkerEntity worker = requireWorker(order.getWorkerId());
        worker.setCompletedOrders((worker.getCompletedOrders() == null ? 0 : worker.getCompletedOrders()) + 1);
        workerMapper.updateById(worker);
        operationLogService.record(OperationLogActions.ORDER_COMPLETE, "ORDER", orderId, "用户确认完工");
        notifyWorkerStatus(
                worker,
                "用户已确认完工",
                "订单 #" + order.getId() + " 已由用户确认完工，可以等待评价或处理新的预约。",
                order.getId(),
                "/worker/orders"
        );
        return buildSingleOrderDto(order);
    }

    @Transactional
    public OrderServiceRecordDto uploadWorkerServiceRecord(Long orderId,
                                                           String stage,
                                                           String description,
                                                           MultipartFile file) {
        ensureCurrentWorkerQualified();
        OrderEntity order = orderAccessService.requireCurrentWorkerOrder(orderId);

        OrderServiceRecordStage recordStage = OrderServiceRecordStage.from(stage);
        if (recordStage == OrderServiceRecordStage.CHECK_IN) {
            if (!OrderStatus.CONFIRMED.matches(order.getStatus()) && !OrderStatus.IN_SERVICE.matches(order.getStatus())) {
                throw new BusinessException("只有用户确认后的订单才能上传上门打卡");
            }
        } else if (!OrderStatus.IN_SERVICE.matches(order.getStatus()) && !OrderStatus.WAITING_USER_CONFIRMATION.matches(order.getStatus())) {
            throw new BusinessException("当前订单阶段暂不支持上传过程记录");
        }

        return orderServiceRecordService.createRecord(orderId, order.getWorkerId(), recordStage.code(), description, file);
    }

    @Transactional
    public OrderDto reviewOrder(Long orderId, OrderReviewRequest request) {
        SessionUser currentUser = requireCurrentUser();
        OrderEntity order = orderAccessService.requireCurrentUserOrder(orderId);
        requireStatus(order, OrderStatus.COMPLETED, "只有已完成订单才可以评价");

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
        operationLogService.record(
                OperationLogActions.ORDER_REVIEW,
                "ORDER",
                orderId,
                "订单评价，评分=" + request.rating()
        );
        notifyWorkerStatus(
                requireWorker(order.getWorkerId()),
                "你收到了新的订单评价",
                "订单 #" + order.getId() + " 收到了 " + request.rating() + " 星评价。",
                order.getId(),
                "/worker/orders"
        );
        return buildSingleOrderDto(order);
    }

    private void ensureSlotAvailable(Long workerId, String bookingDate, String bookingSlot) {
        BookingAvailabilityDto availability = getBookingAvailability(workerId, bookingDate);
        if (!availability.availableSlots().contains(bookingSlot)) {
            throw new BusinessException("所选时段已被预约，请更换其他时段");
        }
    }

    private String normalizeBookingSlot(String bookingSlot) {
        String normalized = bookingSlot == null ? "" : bookingSlot.trim();
        if (!BOOKING_SLOTS.contains(normalized)) {
            throw new BusinessException("请选择平台提供的预约时段");
        }
        return normalized;
    }

    private LocalDate parseBookingDate(String bookingDate) {
        try {
            LocalDate parsed = LocalDate.parse(bookingDate.trim());
            if (parsed.isBefore(LocalDate.now())) {
                throw new BusinessException("预约日期不能早于今天");
            }
            return parsed;
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new BusinessException("预约日期格式不正确");
        }
    }

    private void updateOrderStatus(OrderEntity order, OrderStatus nextStatus, String note) {
        order.setStatus(nextStatus.code());
        order.setProgressNote(note);
        orderMapper.updateById(order);
        appendProgress(order.getId(), nextStatus, note);
    }

    private void appendProgress(Long orderId, OrderStatus status, String note) {
        orderProgressMapper.insert(new OrderProgressEntity(orderId, status.code(), note));
    }

    private void requireStatus(OrderEntity order, OrderStatus status, String message) {
        if (!status.matches(order.getStatus())) {
            throw new BusinessException(message);
        }
    }

    private WorkerEntity requireWorker(Long workerId) {
        WorkerEntity worker = workerMapper.selectById(workerId);
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return worker;
    }

    private void notifyUserStatus(Long userId, String title, String content, Long orderId, String actionPath) {
        notificationService.notifyUser(
                userId,
                RoleCodes.USER,
                NotificationType.ORDER_STATUS,
                title,
                content,
                "ORDER",
                orderId,
                actionPath
        );
    }

    private void notifyWorkerStatus(WorkerEntity worker, String title, String content, Long orderId, String actionPath) {
        if (worker == null || worker.getUserId() == null) {
            return;
        }
        notificationService.notifyUser(
                worker.getUserId(),
                RoleCodes.WORKER,
                NotificationType.ORDER_STATUS,
                title,
                content,
                "ORDER",
                orderId,
                actionPath
        );
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

        List<Long> orderIds = orders.stream().map(OrderEntity::getId).toList();
        Map<Long, String> progressMap = buildLatestProgressMap(orderIds);
        Map<Long, OrderReviewEntity> reviewMap = buildReviewMap(orderIds);
        Map<Long, List<OrderServiceRecordDto>> serviceRecordMap = orderServiceRecordService.groupByOrderIds(orderIds);

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
                            reviewMap.get(order.getId()),
                            serviceRecordMap.getOrDefault(order.getId(), List.of())
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
        ).stream().collect(Collectors.toMap(
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

    private OrderPaymentDto toPaymentDto(OrderPaymentEntity entity) {
        return new OrderPaymentDto(
                entity.getId(),
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getPaymentNo(),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().format(DATE_TIME_FORMATTER),
                entity.getPaidAt() == null ? null : entity.getPaidAt().format(DATE_TIME_FORMATTER)
        );
    }

    private int resolveOrderAmount(WorkerEntity worker) {
        return worker.getHourlyPrice() == null ? 0 : worker.getHourlyPrice();
    }

    private String generatePaymentNo(Long orderId) {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + orderId;
    }

    private LambdaQueryWrapper<OrderEntity> buildCurrentUserOrderWrapper(Long userId, String status) {
        return new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, userId)
                .eq(hasText(status), OrderEntity::getStatus, status)
                .orderByDesc(OrderEntity::getId);
    }

    private LambdaQueryWrapper<OrderEntity> buildCurrentWorkerOrderWrapper(Long workerId, String status) {
        return new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getWorkerId, workerId)
                .eq(hasText(status), OrderEntity::getStatus, status)
                .orderByDesc(OrderEntity::getId);
    }

    private LambdaQueryWrapper<OrderEntity> buildAdminOrderWrapper(String status,
                                                                   String keyword,
                                                                   String dateFrom,
                                                                   String dateTo) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .eq(hasText(status), OrderEntity::getStatus, status)
                .ge(hasText(dateFrom), OrderEntity::getBookingDate, dateFrom)
                .le(hasText(dateTo), OrderEntity::getBookingDate, dateTo)
                .orderByDesc(OrderEntity::getId);

        if (hasText(keyword)) {
            List<Long> workerIds = findWorkerIdsByKeyword(keyword);
            wrapper.and(query -> query
                    .like(OrderEntity::getServiceName, keyword)
                    .or()
                    .like(OrderEntity::getCustomerName, keyword)
                    .or()
                    .like(OrderEntity::getContactPhone, keyword)
                    .or()
                    .like(OrderEntity::getServiceAddress, keyword)
                    .or(!workerIds.isEmpty(), nested -> nested.in(OrderEntity::getWorkerId, workerIds)));
        }
        return wrapper;
    }

    private List<Long> findWorkerIdsByKeyword(String keyword) {
        List<WorkerEntity> workers = workerMapper.selectList(
                new LambdaQueryWrapper<WorkerEntity>()
                        .like(WorkerEntity::getName, keyword)
                        .or()
                        .like(WorkerEntity::getCity, keyword)
        );
        return workers.stream()
                .map(WorkerEntity::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    private void ensureCurrentWorkerQualified() {
        SessionUser currentUser = requireCurrentUser();
        workerProfileService.requireApprovedWorkerByUserId(currentUser.userId());
    }
}

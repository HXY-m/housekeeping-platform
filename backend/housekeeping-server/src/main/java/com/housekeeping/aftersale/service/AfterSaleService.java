package com.housekeeping.aftersale.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.aftersale.AfterSaleStatus;
import com.housekeeping.aftersale.dto.AfterSaleAttachmentDto;
import com.housekeeping.aftersale.dto.AfterSaleCreateRequest;
import com.housekeeping.aftersale.dto.AfterSaleDto;
import com.housekeeping.aftersale.dto.AfterSaleHandleRequest;
import com.housekeeping.aftersale.entity.AfterSaleEntity;
import com.housekeeping.aftersale.mapper.AfterSaleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AfterSaleService {

    private final AfterSaleMapper afterSaleMapper;
    private final OrderMapper orderMapper;
    private final WorkerMapper workerMapper;
    private final AfterSaleAttachmentService afterSaleAttachmentService;

    public AfterSaleService(AfterSaleMapper afterSaleMapper,
                            OrderMapper orderMapper,
                            WorkerMapper workerMapper,
                            AfterSaleAttachmentService afterSaleAttachmentService) {
        this.afterSaleMapper = afterSaleMapper;
        this.orderMapper = orderMapper;
        this.workerMapper = workerMapper;
        this.afterSaleAttachmentService = afterSaleAttachmentService;
    }

    public List<AfterSaleDto> listCurrentUserAfterSales() {
        SessionUser currentUser = requireCurrentUser();
        List<AfterSaleEntity> records = afterSaleMapper.selectList(
                new LambdaQueryWrapper<AfterSaleEntity>()
                        .eq(AfterSaleEntity::getUserId, currentUser.userId())
                        .orderByAsc(AfterSaleEntity::getStatus)
                        .orderByDesc(AfterSaleEntity::getId)
        );
        return buildDtos(records);
    }

    public List<AfterSaleDto> listAll() {
        List<AfterSaleEntity> records = afterSaleMapper.selectList(
                new LambdaQueryWrapper<AfterSaleEntity>()
                        .orderByAsc(AfterSaleEntity::getStatus)
                        .orderByDesc(AfterSaleEntity::getId)
        );
        return buildDtos(records);
    }

    @Transactional
    public AfterSaleDto create(AfterSaleCreateRequest request) {
        SessionUser currentUser = requireCurrentUser();
        OrderEntity order = requireUserOwnedOrder(request.orderId(), currentUser.userId());
        if (OrderStatus.PENDING.matches(order.getStatus())) {
            throw new BusinessException("订单尚未进入服务阶段，暂不支持提交售后");
        }

        AfterSaleEntity existed = afterSaleMapper.selectOne(
                new LambdaQueryWrapper<AfterSaleEntity>()
                        .eq(AfterSaleEntity::getOrderId, order.getId())
                        .last("limit 1")
        );
        if (existed != null) {
            throw new BusinessException("该订单已经提交过售后反馈");
        }

        AfterSaleEntity entity = new AfterSaleEntity(
                order.getId(),
                currentUser.userId(),
                order.getWorkerId(),
                request.issueType().trim(),
                request.content().trim(),
                request.contactPhone().trim(),
                AfterSaleStatus.PENDING.name(),
                "",
                LocalDateTime.now(),
                null
        );
        afterSaleMapper.insert(entity);
        return buildSingleDto(entity);
    }

    @Transactional
    public AfterSaleDto handle(Long id, AfterSaleHandleRequest request) {
        AfterSaleEntity entity = afterSaleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("未找到对应的售后反馈");
        }

        AfterSaleStatus currentStatus = parseStatus(entity.getStatus());
        if (currentStatus.isFinalStatus()) {
            throw new BusinessException("该售后反馈已经处理完成，不能重复处理");
        }

        AfterSaleStatus nextStatus = parseStatus(request.status());
        entity.setStatus(nextStatus.name());
        entity.setAdminRemark(request.adminRemark() == null ? "" : request.adminRemark().trim());
        entity.setHandledAt(LocalDateTime.now());
        afterSaleMapper.updateById(entity);
        return buildSingleDto(entity);
    }

    private AfterSaleDto buildSingleDto(AfterSaleEntity entity) {
        return buildDtos(List.of(entity)).get(0);
    }

    private List<AfterSaleDto> buildDtos(List<AfterSaleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = entities.stream()
                .map(AfterSaleEntity::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, OrderEntity> orderMap = orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(OrderEntity::getId, Function.identity()));

        List<Long> workerIds = entities.stream()
                .map(AfterSaleEntity::getWorkerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, WorkerEntity> workerMap = workerIds.isEmpty()
                ? Map.of()
                : workerMapper.selectBatchIds(workerIds).stream()
                .collect(Collectors.toMap(WorkerEntity::getId, Function.identity()));
        List<Long> afterSaleIds = entities.stream()
                .map(AfterSaleEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<AfterSaleAttachmentDto>> attachmentMap =
                afterSaleAttachmentService.groupByAfterSaleIds(afterSaleIds);

        return entities.stream()
                .map(item -> {
                    OrderEntity order = orderMap.get(item.getOrderId());
                    if (order == null) {
                        throw new BusinessException("售后反馈关联的订单不存在");
                    }
                    WorkerEntity worker = workerMap.get(item.getWorkerId());
                    String workerName = worker == null ? "服务人员待同步" : worker.getName();
                    return new AfterSaleDto(
                            item.getId(),
                            item.getOrderId(),
                            order.getServiceName(),
                            workerName,
                            order.getCustomerName(),
                            order.getStatus(),
                            item.getIssueType(),
                            item.getContent(),
                            item.getContactPhone(),
                            item.getStatus(),
                            item.getAdminRemark(),
                            attachmentMap.getOrDefault(item.getId(), List.of()),
                            item.getCreatedAt(),
                            item.getHandledAt()
                    );
                })
                .toList();
    }

    private OrderEntity requireUserOwnedOrder(Long orderId, Long userId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("未找到对应的订单");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("只能提交自己订单的售后反馈");
        }
        return order;
    }

    private AfterSaleStatus parseStatus(String value) {
        try {
            return AfterSaleStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("不支持的售后状态");
        }
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }
}

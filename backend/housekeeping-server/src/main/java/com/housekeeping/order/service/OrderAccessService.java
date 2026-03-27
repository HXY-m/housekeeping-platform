package com.housekeeping.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class OrderAccessService {

    private final OrderMapper orderMapper;
    private final WorkerProfileService workerProfileService;

    public OrderAccessService(OrderMapper orderMapper,
                              WorkerProfileService workerProfileService) {
        this.orderMapper = orderMapper;
        this.workerProfileService = workerProfileService;
    }

    public List<OrderEntity> listCurrentUserOrders() {
        SessionUser currentUser = requireCurrentUser();
        return orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, currentUser.userId())
                        .orderByDesc(OrderEntity::getId)
        );
    }

    public List<OrderEntity> listCurrentWorkerOrders() {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = workerProfileService.requireWorkerByUserId(currentUser.userId());
        return orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getWorkerId, worker.getId())
                        .orderByDesc(OrderEntity::getId)
        );
    }

    public List<OrderEntity> listCurrentRoleOrders() {
        SessionUser currentUser = requireCurrentUser();
        if (RoleCodes.USER.equals(currentUser.roleCode())) {
            return listCurrentUserOrders();
        }
        if (RoleCodes.WORKER.equals(currentUser.roleCode())) {
            return listCurrentWorkerOrders();
        }
        throw new BusinessException("当前角色暂不支持查看订单沟通列表");
    }

    public OrderEntity requireCurrentUserOrder(Long orderId) {
        SessionUser currentUser = requireCurrentUser();
        OrderEntity order = requireOrder(orderId);
        if (!Objects.equals(order.getUserId(), currentUser.userId())) {
            throw new BusinessException("只能操作自己的订单");
        }
        return order;
    }

    public OrderEntity requireCurrentWorkerOrder(Long orderId) {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = workerProfileService.requireWorkerByUserId(currentUser.userId());
        OrderEntity order = requireOrder(orderId);
        if (!Objects.equals(order.getWorkerId(), worker.getId())) {
            throw new BusinessException("只能处理分配给自己的订单");
        }
        return order;
    }

    public OrderEntity requireCurrentRoleOrder(Long orderId) {
        SessionUser currentUser = requireCurrentUser();
        if (RoleCodes.USER.equals(currentUser.roleCode())) {
            return requireCurrentUserOrder(orderId);
        }
        if (RoleCodes.WORKER.equals(currentUser.roleCode())) {
            return requireCurrentWorkerOrder(orderId);
        }
        throw new BusinessException("当前角色暂不支持访问订单沟通");
    }

    public SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    private OrderEntity requireOrder(Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("未找到对应的订单");
        }
        return order;
    }
}

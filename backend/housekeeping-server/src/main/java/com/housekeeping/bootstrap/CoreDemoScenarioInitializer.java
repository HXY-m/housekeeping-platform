package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.aftersale.AfterSaleStatus;
import com.housekeeping.aftersale.entity.AfterSaleAttachmentEntity;
import com.housekeeping.aftersale.entity.AfterSaleEntity;
import com.housekeeping.aftersale.mapper.AfterSaleAttachmentMapper;
import com.housekeeping.aftersale.mapper.AfterSaleMapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.favorite.entity.FavoriteWorkerEntity;
import com.housekeeping.favorite.mapper.FavoriteWorkerMapper;
import com.housekeeping.message.entity.OrderMessageEntity;
import com.housekeeping.message.mapper.OrderMessageMapper;
import com.housekeeping.notification.NotificationType;
import com.housekeeping.notification.entity.UserNotificationEntity;
import com.housekeeping.notification.mapper.UserNotificationMapper;
import com.housekeeping.order.OrderPaymentMethod;
import com.housekeeping.order.OrderServiceRecordStage;
import com.housekeeping.order.OrderStatus;
import com.housekeeping.order.PaymentStatus;
import com.housekeeping.order.entity.OrderEntity;
import com.housekeeping.order.entity.OrderPaymentEntity;
import com.housekeeping.order.entity.OrderProgressEntity;
import com.housekeeping.order.entity.OrderReviewEntity;
import com.housekeeping.order.entity.OrderServiceRecordAttachmentEntity;
import com.housekeeping.order.entity.OrderServiceRecordEntity;
import com.housekeeping.order.mapper.OrderMapper;
import com.housekeeping.order.mapper.OrderPaymentMapper;
import com.housekeeping.order.mapper.OrderProgressMapper;
import com.housekeeping.order.mapper.OrderReviewMapper;
import com.housekeeping.order.mapper.OrderServiceRecordAttachmentMapper;
import com.housekeeping.order.mapper.OrderServiceRecordMapper;
import com.housekeeping.user.service.UserProfileService;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.application.entity.WorkerApplicationAttachmentEntity;
import com.housekeeping.worker.application.entity.WorkerApplicationEntity;
import com.housekeeping.worker.application.mapper.WorkerApplicationAttachmentMapper;
import com.housekeeping.worker.application.mapper.WorkerApplicationMapper;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(35)
public class CoreDemoScenarioInitializer implements CommandLineRunner {

    private static final String USER_PHONE = "13800000011";
    private static final String USER_USERNAME = "demo_user";
    private static final String USER_NAME = "Demo User";
    private static final String WORKER_PHONE = "13800000022";
    private static final String WORKER_USERNAME = "demo_worker";
    private static final String WORKER_NAME = "李阿姨";
    private static final String ADMIN_PHONE = "13800000033";
    private static final String ADMIN_USERNAME = "demo_admin";
    private static final String ADMIN_NAME = "Demo Admin";

    private static final String QUALIFICATION_ID_URL = "/uploads/demo/qualification-id.svg";
    private static final String QUALIFICATION_HEALTH_URL = "/uploads/demo/qualification-health.svg";
    private static final String QUALIFICATION_TRAINING_URL = "/uploads/demo/qualification-training.svg";
    private static final String SERVICE_CHECK_IN_URL = "/uploads/demo/service-check-in.svg";
    private static final String SERVICE_PROGRESS_URL = "/uploads/demo/service-progress.svg";
    private static final String SERVICE_FINISH_URL = "/uploads/demo/service-finish.svg";
    private static final String AFTER_SALE_URL = "/uploads/demo/after-sale-proof.svg";

    private final AuthAccountService authAccountService;
    private final UserProfileService userProfileService;
    private final WorkerProfileService workerProfileService;
    private final WorkerMapper workerMapper;
    private final FavoriteWorkerMapper favoriteWorkerMapper;
    private final WorkerApplicationMapper workerApplicationMapper;
    private final WorkerApplicationAttachmentMapper workerApplicationAttachmentMapper;
    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final OrderProgressMapper orderProgressMapper;
    private final OrderReviewMapper orderReviewMapper;
    private final OrderServiceRecordMapper orderServiceRecordMapper;
    private final OrderServiceRecordAttachmentMapper orderServiceRecordAttachmentMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleAttachmentMapper afterSaleAttachmentMapper;
    private final OrderMessageMapper orderMessageMapper;
    private final UserNotificationMapper userNotificationMapper;

    public CoreDemoScenarioInitializer(AuthAccountService authAccountService,
                                       UserProfileService userProfileService,
                                       WorkerProfileService workerProfileService,
                                       WorkerMapper workerMapper,
                                       FavoriteWorkerMapper favoriteWorkerMapper,
                                       WorkerApplicationMapper workerApplicationMapper,
                                       WorkerApplicationAttachmentMapper workerApplicationAttachmentMapper,
                                       OrderMapper orderMapper,
                                       OrderPaymentMapper orderPaymentMapper,
                                       OrderProgressMapper orderProgressMapper,
                                       OrderReviewMapper orderReviewMapper,
                                       OrderServiceRecordMapper orderServiceRecordMapper,
                                       OrderServiceRecordAttachmentMapper orderServiceRecordAttachmentMapper,
                                       AfterSaleMapper afterSaleMapper,
                                       AfterSaleAttachmentMapper afterSaleAttachmentMapper,
                                       OrderMessageMapper orderMessageMapper,
                                       UserNotificationMapper userNotificationMapper) {
        this.authAccountService = authAccountService;
        this.userProfileService = userProfileService;
        this.workerProfileService = workerProfileService;
        this.workerMapper = workerMapper;
        this.favoriteWorkerMapper = favoriteWorkerMapper;
        this.workerApplicationMapper = workerApplicationMapper;
        this.workerApplicationAttachmentMapper = workerApplicationAttachmentMapper;
        this.orderMapper = orderMapper;
        this.orderPaymentMapper = orderPaymentMapper;
        this.orderProgressMapper = orderProgressMapper;
        this.orderReviewMapper = orderReviewMapper;
        this.orderServiceRecordMapper = orderServiceRecordMapper;
        this.orderServiceRecordAttachmentMapper = orderServiceRecordAttachmentMapper;
        this.afterSaleMapper = afterSaleMapper;
        this.afterSaleAttachmentMapper = afterSaleAttachmentMapper;
        this.orderMessageMapper = orderMessageMapper;
        this.userNotificationMapper = userNotificationMapper;
    }

    @Override
    public void run(String... args) {
        SysUserEntity user = ensureUser(USER_PHONE, USER_USERNAME, USER_NAME, RoleCodes.USER);
        SysUserEntity workerUser = ensureUser(WORKER_PHONE, WORKER_USERNAME, WORKER_NAME, RoleCodes.WORKER);
        SysUserEntity admin = ensureUser(ADMIN_PHONE, ADMIN_USERNAME, ADMIN_NAME, RoleCodes.ADMIN);

        userProfileService.ensureProfileExists(user.getId());
        userProfileService.ensureSampleAddress(
                user.getId(),
                USER_NAME,
                USER_PHONE,
                "上海",
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                "常用"
        );

        WorkerEntity worker = ensureWorkerProfile(workerUser);
        ensureFavorite(user.getId(), worker.getId());
        ensureApprovedApplication(workerUser.getId());

        LocalDate today = LocalDate.now();
        OrderEntity pendingOrder = ensureOrder(
                "[CORE_DEMO_PENDING]",
                user.getId(),
                worker.getId(),
                USER_NAME,
                USER_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.plusDays(1).toString(),
                "08:00-10:00",
                "日常保洁",
                OrderStatus.PENDING,
                "用户已提交预约，等待服务人员接单",
                "重点处理厨房台面和浴室镜柜"
        );
        ensureProgresses(pendingOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单")
        ));
        ensurePayment(pendingOrder, 128, null, false, null);

        OrderEntity confirmedOrder = ensureOrder(
                "[CORE_DEMO_CONFIRMED]",
                user.getId(),
                worker.getId(),
                USER_NAME,
                USER_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.plusDays(2).toString(),
                "10:00-12:00",
                "深度清洁",
                OrderStatus.CONFIRMED,
                "用户已确认预约安排，等待服务人员上门",
                "需要重点清理厨房油污与阳台窗轨"
        );
        ensureProgresses(confirmedOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门")
        ));
        ensurePayment(confirmedOrder, 228, OrderPaymentMethod.ALIPAY, true, LocalDateTime.now().minusDays(1).minusHours(3));

        OrderEntity inServiceOrder = ensureOrder(
                "[CORE_DEMO_IN_SERVICE]",
                user.getId(),
                worker.getId(),
                USER_NAME,
                USER_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.toString(),
                "13:00-15:00",
                "家电清洗",
                OrderStatus.IN_SERVICE,
                "服务人员已上门，正在清洗空调与过滤网",
                "本次需要清洗 2 台挂机空调"
        );
        ensureProgresses(inServiceOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已上门，正在清洗空调与过滤网")
        ));
        ensurePayment(inServiceOrder, 168, OrderPaymentMethod.WECHAT_PAY, true, LocalDateTime.now().minusHours(10));
        ensureServiceRecords(inServiceOrder.getId(), worker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "已到达现场并完成上门打卡", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "空调外壳和过滤网已完成第一轮拆洗", SERVICE_PROGRESS_URL)
        ));

        OrderEntity waitingConfirmOrder = ensureOrder(
                "[CORE_DEMO_WAIT_CONFIRM]",
                user.getId(),
                worker.getId(),
                USER_NAME,
                USER_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.toString(),
                "16:00-18:00",
                "收纳整理",
                OrderStatus.WAITING_USER_CONFIRMATION,
                "服务人员已提交完工，等待用户确认",
                "卧室衣柜和玄关储物区整理已完成"
        );
        ensureProgresses(waitingConfirmOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已上门，正在进行空间整理"),
                progress(OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认")
        ));
        ensurePayment(waitingConfirmOrder, 108, OrderPaymentMethod.ALIPAY, true, LocalDateTime.now().minusHours(7));
        ensureServiceRecords(waitingConfirmOrder.getId(), worker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "上门打卡完成，开始整理卧室和玄关", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "已完成衣柜分层、玄关储物区和桌面收纳", SERVICE_PROGRESS_URL),
                serviceRecord(OrderServiceRecordStage.FINISH_PROOF, "整理前后对比已上传，请用户确认完工", SERVICE_FINISH_URL)
        ));

        OrderEntity completedOrder = ensureOrder(
                "[CORE_DEMO_COMPLETED]",
                user.getId(),
                worker.getId(),
                USER_NAME,
                USER_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.minusDays(1).toString(),
                "09:00-11:00",
                "日常保洁",
                OrderStatus.COMPLETED,
                "用户已确认完工，订单完成",
                "完成客厅、卧室和卫浴基础保洁"
        );
        ensureProgresses(completedOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约安排，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已上门，正在进行日常保洁"),
                progress(OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认"),
                progress(OrderStatus.COMPLETED, "用户已确认完工，订单完成")
        ));
        ensurePayment(completedOrder, 136, OrderPaymentMethod.WECHAT_PAY, true, LocalDateTime.now().minusDays(1).minusHours(5));
        ensureReview(completedOrder.getId(), user.getId(), worker.getId(), 5, "服务很细致，过程也记录得很清楚，适合长期预约。");
        ensureAfterSale(
                completedOrder.getId(),
                user.getId(),
                worker.getId(),
                "补充清洁",
                "阳台角落还有少量灰尘，希望平台协助跟进补充处理。",
                USER_PHONE,
                AfterSaleStatus.PROCESSING.name(),
                "已联系服务人员补充处理，预计今晚 20:00 前反馈结果。",
                AFTER_SALE_URL
        );

        ensureOrderMessage(
                inServiceOrder.getId(),
                workerUser.getId(),
                RoleCodes.WORKER,
                WORKER_NAME,
                "我已经到达现场，正在清洗第一台空调，稍后会继续上传过程记录。"
        );
        ensureOrderMessage(
                waitingConfirmOrder.getId(),
                user.getId(),
                RoleCodes.USER,
                USER_NAME,
                "我已经看到完工图片了，晚一点回家后会确认完工。"
        );

        ensureNotification(
                user.getId(),
                RoleCodes.USER,
                NotificationType.ORDER_STATUS,
                "你的订单有新的服务进展",
                "订单 #" + waitingConfirmOrder.getId() + " 已进入待确认完工状态，请查看详情。",
                "ORDER",
                waitingConfirmOrder.getId(),
                "/user/orders"
        );
        ensureNotification(
                workerUser.getId(),
                RoleCodes.WORKER,
                NotificationType.ORDER_MESSAGE,
                "有新的订单沟通消息",
                "用户对订单 #" + waitingConfirmOrder.getId() + " 留下了新的沟通内容。",
                "ORDER",
                waitingConfirmOrder.getId(),
                "/worker/conversations?orderId=" + waitingConfirmOrder.getId()
        );
        ensureNotification(
                admin.getId(),
                RoleCodes.ADMIN,
                NotificationType.AFTER_SALE,
                "有新的售后工单待处理",
                "订单 #" + completedOrder.getId() + " 已创建售后工单，请及时跟进。",
                "AFTER_SALE",
                completedOrder.getId(),
                "/admin/after-sales"
        );
    }

    private SysUserEntity ensureUser(String phone, String username, String realName, String roleCode) {
        SysUserEntity user = authAccountService.findUserByPhone(phone);
        if (user == null) {
            user = authAccountService.createUser(phone, username, "123456", realName);
        } else {
            authAccountService.updateUsernameIfBlank(user.getId(), username);
        }
        authAccountService.bindRole(user.getId(), roleCode);
        return user;
    }

    private WorkerEntity ensureWorkerProfile(SysUserEntity user) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, user.getId())
                        .last("limit 1")
        );
        if (worker != null) {
            return worker;
        }
        return workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                user.getId(),
                WORKER_NAME,
                "上海",
                89,
                "日常保洁,深度清洁,家电清洗,收纳整理",
                "工作日白天,工作日晚间,周末白天",
                6,
                "健康证,家政培训证",
                "浦东新区,杨浦区,虹口区",
                "默认演示服务人员，适合展示接单、履约、支付与售后全链路。",
                WorkerQualificationStatus.APPROVED,
                "平台认证保洁师",
                "退租保洁,日常家庭保洁,空调清洗"
        ));
    }

    private void ensureFavorite(Long userId, Long workerId) {
        Long count = favoriteWorkerMapper.selectCount(
                new LambdaQueryWrapper<FavoriteWorkerEntity>()
                        .eq(FavoriteWorkerEntity::getUserId, userId)
                        .eq(FavoriteWorkerEntity::getWorkerId, workerId)
        );
        if (count != null && count > 0) {
            return;
        }
        favoriteWorkerMapper.insert(new FavoriteWorkerEntity(userId, workerId, LocalDateTime.now().minusDays(2)));
    }

    private void ensureApprovedApplication(Long userId) {
        WorkerApplicationEntity existed = workerApplicationMapper.selectOne(
                new LambdaQueryWrapper<WorkerApplicationEntity>()
                        .eq(WorkerApplicationEntity::getUserId, userId)
                        .eq(WorkerApplicationEntity::getStatus, "APPROVED")
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }

        LocalDateTime createdAt = LocalDateTime.now().minusDays(7);
        WorkerApplicationEntity entity = new WorkerApplicationEntity();
        entity.setUserId(userId);
        entity.setRealName(WORKER_NAME);
        entity.setPhone(WORKER_PHONE);
        entity.setServiceTypes("日常保洁,深度清洁,收纳整理");
        entity.setYearsOfExperience(6);
        entity.setCertificates("健康证,家政服务培训证");
        entity.setServiceAreas("浦东新区,杨浦区,虹口区");
        entity.setAvailableSchedule("工作日白天,工作日晚间,周末白天");
        entity.setIntro("默认演示服务人员资质申请记录，用于后台审核页面展示。");
        entity.setStatus("APPROVED");
        entity.setAdminRemark("资质材料完整，已通过审核。");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        workerApplicationMapper.insert(entity);

        workerApplicationAttachmentMapper.insert(new WorkerApplicationAttachmentEntity(
                entity.getId(),
                "qualification-id.svg",
                QUALIFICATION_ID_URL,
                1680L,
                createdAt
        ));
        workerApplicationAttachmentMapper.insert(new WorkerApplicationAttachmentEntity(
                entity.getId(),
                "qualification-health.svg",
                QUALIFICATION_HEALTH_URL,
                1720L,
                createdAt
        ));
        workerApplicationAttachmentMapper.insert(new WorkerApplicationAttachmentEntity(
                entity.getId(),
                "qualification-training.svg",
                QUALIFICATION_TRAINING_URL,
                1760L,
                createdAt
        ));
    }

    private OrderEntity ensureOrder(String demoKey,
                                    Long userId,
                                    Long workerId,
                                    String customerName,
                                    String contactPhone,
                                    String serviceAddress,
                                    String bookingDate,
                                    String bookingSlot,
                                    String serviceName,
                                    OrderStatus status,
                                    String progressNote,
                                    String detailRemark) {
        OrderEntity existed = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getRemark, demoKey + " " + detailRemark)
                        .last("limit 1")
        );
        if (existed != null) {
            return existed;
        }

        OrderEntity entity = new OrderEntity(
                userId,
                serviceName,
                workerId,
                customerName,
                contactPhone,
                serviceAddress,
                bookingDate,
                bookingSlot,
                status.code(),
                progressNote,
                demoKey + " " + detailRemark
        );
        orderMapper.insert(entity);
        return entity;
    }

    private void ensurePayment(OrderEntity order,
                               Integer amount,
                               OrderPaymentMethod paymentMethod,
                               boolean paid,
                               LocalDateTime paidAt) {
        boolean needUpdate = false;
        if (order.getPayableAmount() == null || order.getPayableAmount() <= 0) {
            order.setPayableAmount(amount == null ? 0 : amount);
            needUpdate = true;
        }

        if (paid) {
            if (!PaymentStatus.PAID.matches(order.getPaymentStatus())) {
                order.setPaymentStatus(PaymentStatus.PAID.code());
                needUpdate = true;
            }
            if (order.getPaymentMethod() == null || order.getPaymentMethod().isBlank()) {
                order.setPaymentMethod(paymentMethod == null ? OrderPaymentMethod.WECHAT_PAY.code() : paymentMethod.code());
                needUpdate = true;
            }
            if (order.getPaidAt() == null) {
                order.setPaidAt(paidAt == null ? LocalDateTime.now().minusHours(2) : paidAt);
                needUpdate = true;
            }
        } else if (order.getPaymentStatus() == null || order.getPaymentStatus().isBlank()) {
            order.setPaymentStatus(PaymentStatus.UNPAID.code());
            order.setPaymentMethod("");
            order.setPaidAt(null);
            needUpdate = true;
        }

        if (needUpdate) {
            orderMapper.updateById(order);
        }

        if (!paid) {
            return;
        }

        Long count = orderPaymentMapper.selectCount(
                new LambdaQueryWrapper<OrderPaymentEntity>()
                        .eq(OrderPaymentEntity::getOrderId, order.getId())
        );
        if (count != null && count > 0) {
            return;
        }

        LocalDateTime createdAt = order.getPaidAt() == null ? LocalDateTime.now().minusHours(2) : order.getPaidAt();
        orderPaymentMapper.insert(new OrderPaymentEntity(
                order.getId(),
                order.getUserId(),
                order.getPayableAmount(),
                order.getPaymentMethod(),
                PaymentStatus.PAID.code(),
                "CORE" + order.getId(),
                createdAt,
                order.getPaidAt()
        ));
    }

    private void ensureProgresses(Long orderId, List<OrderProgressEntity> progresses) {
        Long count = orderProgressMapper.selectCount(
                new LambdaQueryWrapper<OrderProgressEntity>()
                        .eq(OrderProgressEntity::getOrderId, orderId)
        );
        if (count != null && count > 0) {
            return;
        }
        for (OrderProgressEntity progress : progresses) {
            orderProgressMapper.insert(new OrderProgressEntity(orderId, progress.getProgressStatus(), progress.getProgressNote()));
        }
    }

    private OrderProgressEntity progress(OrderStatus status, String note) {
        return new OrderProgressEntity(null, status.code(), note);
    }

    private void ensureServiceRecords(Long orderId, Long workerId, List<ServiceRecordSeed> records) {
        Long count = orderServiceRecordMapper.selectCount(
                new LambdaQueryWrapper<OrderServiceRecordEntity>()
                        .eq(OrderServiceRecordEntity::getOrderId, orderId)
        );
        if (count != null && count > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now().minusHours(5);
        for (ServiceRecordSeed record : records) {
            OrderServiceRecordEntity entity = new OrderServiceRecordEntity(
                    orderId,
                    workerId,
                    record.stage().code(),
                    record.description(),
                    now
            );
            orderServiceRecordMapper.insert(entity);
            orderServiceRecordAttachmentMapper.insert(new OrderServiceRecordAttachmentEntity(
                    entity.getId(),
                    record.fileName(),
                    record.fileUrl(),
                    now
            ));
            now = now.plusMinutes(35);
        }
    }

    private ServiceRecordSeed serviceRecord(OrderServiceRecordStage stage, String description, String fileUrl) {
        return new ServiceRecordSeed(stage, description, stage.name().toLowerCase() + ".svg", fileUrl);
    }

    private void ensureReview(Long orderId, Long userId, Long workerId, int rating, String content) {
        OrderReviewEntity existed = orderReviewMapper.selectOne(
                new LambdaQueryWrapper<OrderReviewEntity>()
                        .eq(OrderReviewEntity::getOrderId, orderId)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }
        orderReviewMapper.insert(new OrderReviewEntity(
                orderId,
                userId,
                workerId,
                rating,
                content,
                LocalDateTime.now().minusHours(18)
        ));
    }

    private void ensureAfterSale(Long orderId,
                                 Long userId,
                                 Long workerId,
                                 String issueType,
                                 String content,
                                 String contactPhone,
                                 String status,
                                 String adminRemark,
                                 String attachmentUrl) {
        AfterSaleEntity existed = afterSaleMapper.selectOne(
                new LambdaQueryWrapper<AfterSaleEntity>()
                        .eq(AfterSaleEntity::getOrderId, orderId)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }

        LocalDateTime createdAt = LocalDateTime.now().minusHours(12);
        AfterSaleEntity entity = new AfterSaleEntity(
                orderId,
                userId,
                workerId,
                issueType,
                content,
                contactPhone,
                status,
                adminRemark,
                createdAt,
                createdAt.plusHours(3)
        );
        afterSaleMapper.insert(entity);
        afterSaleAttachmentMapper.insert(new AfterSaleAttachmentEntity(
                entity.getId(),
                "after-sale-proof.svg",
                attachmentUrl,
                createdAt
        ));
    }

    private void ensureOrderMessage(Long orderId,
                                    Long senderUserId,
                                    String senderRoleCode,
                                    String senderName,
                                    String content) {
        OrderMessageEntity existed = orderMessageMapper.selectOne(
                new LambdaQueryWrapper<OrderMessageEntity>()
                        .eq(OrderMessageEntity::getOrderId, orderId)
                        .eq(OrderMessageEntity::getSenderUserId, senderUserId)
                        .eq(OrderMessageEntity::getContent, content)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }
        orderMessageMapper.insert(new OrderMessageEntity(
                orderId,
                senderUserId,
                senderRoleCode,
                senderName,
                content,
                LocalDateTime.now().minusHours(4)
        ));
    }

    private void ensureNotification(Long recipientUserId,
                                    String recipientRoleCode,
                                    String type,
                                    String title,
                                    String content,
                                    String relatedType,
                                    Long relatedId,
                                    String actionPath) {
        UserNotificationEntity existed = userNotificationMapper.selectOne(
                new LambdaQueryWrapper<UserNotificationEntity>()
                        .eq(UserNotificationEntity::getRecipientUserId, recipientUserId)
                        .eq(UserNotificationEntity::getRecipientRoleCode, recipientRoleCode)
                        .eq(UserNotificationEntity::getTitle, title)
                        .eq(UserNotificationEntity::getRelatedType, relatedType)
                        .eq(UserNotificationEntity::getRelatedId, relatedId)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }
        userNotificationMapper.insert(new UserNotificationEntity(
                recipientUserId,
                recipientRoleCode,
                type,
                title,
                content,
                relatedType,
                relatedId,
                actionPath,
                false,
                LocalDateTime.now().minusHours(2),
                null
        ));
    }

    private record ServiceRecordSeed(
            OrderServiceRecordStage stage,
            String description,
            String fileName,
            String fileUrl
    ) {
    }
}

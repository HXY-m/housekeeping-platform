package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.aftersale.entity.AfterSaleAttachmentEntity;
import com.housekeeping.aftersale.entity.AfterSaleEntity;
import com.housekeeping.aftersale.mapper.AfterSaleAttachmentMapper;
import com.housekeeping.aftersale.mapper.AfterSaleMapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.support.RoleCodes;
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
@Order(40)
public class DemoScenarioInitializer implements CommandLineRunner {

    private static final String USER_A_PHONE = "13800000044";
    private static final String USER_B_PHONE = "13800000045";
    private static final String WORKER_MAIN_PHONE = "13800000022";
    private static final String WORKER_PENDING_PHONE = "13800000055";
    private static final String WORKER_REJECTED_PHONE = "13800000066";

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

    public DemoScenarioInitializer(AuthAccountService authAccountService,
                                   UserProfileService userProfileService,
                                   WorkerProfileService workerProfileService,
                                   WorkerMapper workerMapper,
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
        SysUserEntity userA = ensureUser(USER_A_PHONE, "林若溪", RoleCodes.USER);
        SysUserEntity userB = ensureUser(USER_B_PHONE, "周明哲", RoleCodes.USER);
        SysUserEntity mainWorkerUser = ensureUser(WORKER_MAIN_PHONE, "演示服务人员", RoleCodes.WORKER);
        SysUserEntity pendingWorkerUser = ensureUser(WORKER_PENDING_PHONE, "陈雅琳", RoleCodes.WORKER);
        SysUserEntity rejectedWorkerUser = ensureUser(WORKER_REJECTED_PHONE, "孙海波", RoleCodes.WORKER);

        ensureUserProfile(userA, "上海市", "徐汇区衡山路 106 弄 5 号楼 901", "常住");
        ensureUserProfile(userB, "上海市", "静安区昌平路 228 弄 3 号楼 602", "公司");

        ensureWorkerProfile(
                pendingWorkerUser,
                "陈雅琳",
                "母婴护理,育儿嫂,老人陪护",
                "工作日白天,周末白天",
                "浦东新区,杨浦区",
                "有月嫂和养老护理双线经验，擅长与家庭建立长期服务配合。",
                "健康证、母婴护理证",
                WorkerQualificationStatus.PENDING
        );
        ensureWorkerProfile(
                rejectedWorkerUser,
                "孙海波",
                "家电清洗,深度清洁",
                "工作日晚间,周末晚间",
                "闵行区,徐汇区",
                "擅长空调和油烟机深度清洗，需要补充最近一年的培训证明。",
                "健康证、家电清洗专项证",
                WorkerQualificationStatus.REJECTED
        );

        ensureWorkerApplication(
                mainWorkerUser.getId(),
                "演示服务人员",
                "13800000022",
                "日常保洁,深度清洁,收纳整理",
                6,
                "健康证、家政培训结业证",
                "浦东新区,杨浦区,虹口区",
                "工作日白天,工作日晚间,周末白天",
                "服务风格细致稳定，能配合家庭长期保洁节奏。",
                "APPROVED",
                "资质齐全，已开放接单",
                List.of(
                        new WorkerApplicationAttachmentEntity(null, "身份证明.svg", QUALIFICATION_ID_URL, 1680L, LocalDateTime.now().minusDays(6)),
                        new WorkerApplicationAttachmentEntity(null, "健康证.svg", QUALIFICATION_HEALTH_URL, 1720L, LocalDateTime.now().minusDays(6))
                ),
                LocalDateTime.now().minusDays(6)
        );
        ensureWorkerApplication(
                pendingWorkerUser.getId(),
                "陈雅琳",
                WORKER_PENDING_PHONE,
                "母婴护理,育儿嫂,老人陪护",
                5,
                "健康证、母婴护理证",
                "浦东新区,杨浦区",
                "工作日白天,周末白天",
                "近三年主要服务新生儿家庭，擅长夜间陪护与月子餐协助。",
                "PENDING",
                "",
                List.of(
                        new WorkerApplicationAttachmentEntity(null, "身份证明.svg", QUALIFICATION_ID_URL, 1680L, LocalDateTime.now().minusDays(2)),
                        new WorkerApplicationAttachmentEntity(null, "培训证明.svg", QUALIFICATION_TRAINING_URL, 1760L, LocalDateTime.now().minusDays(2))
                ),
                LocalDateTime.now().minusDays(2)
        );
        ensureWorkerApplication(
                rejectedWorkerUser.getId(),
                "孙海波",
                WORKER_REJECTED_PHONE,
                "家电清洗,深度清洁",
                4,
                "健康证、家电清洗专项证",
                "闵行区,徐汇区",
                "工作日晚间,周末晚间",
                "擅长季节性家电清洗，需要补交最近培训年审材料。",
                "REJECTED",
                "缺少最近一次培训复审证明，请补充后重新提交。",
                List.of(
                        new WorkerApplicationAttachmentEntity(null, "健康证.svg", QUALIFICATION_HEALTH_URL, 1720L, LocalDateTime.now().minusDays(10)),
                        new WorkerApplicationAttachmentEntity(null, "培训证明.svg", QUALIFICATION_TRAINING_URL, 1760L, LocalDateTime.now().minusDays(10))
                ),
                LocalDateTime.now().minusDays(10)
        );

        WorkerEntity mainWorker = ensureMainWorkerProfile(mainWorkerUser);

        LocalDate today = LocalDate.now();
        OrderEntity pendingOrder = ensureOrder(
                "[DEMO_PENDING]",
                userA.getId(),
                mainWorker.getId(),
                "林若溪",
                USER_A_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.plusDays(1).toString(),
                "08:00-10:00",
                "日常保洁",
                OrderStatus.PENDING,
                "用户已提交预约，等待服务人员接单",
                "重点清洁厨房台面与卫生间镜柜"
        );
        ensureProgresses(pendingOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单")
        ));
        ensurePayment(pendingOrder, mainWorker.getHourlyPrice(), null, false, null);

        OrderEntity acceptedOrder = ensureOrder(
                "[DEMO_ACCEPTED]",
                userA.getId(),
                mainWorker.getId(),
                "林若溪",
                USER_A_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.plusDays(2).toString(),
                "10:00-12:00",
                "深度清洁",
                OrderStatus.ACCEPTED,
                "服务人员已接单，等待用户确认预约安排",
                "需要带除垢清洁剂和玻璃清洁工具"
        );
        ensureProgresses(acceptedOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排")
        ));
        ensurePayment(acceptedOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.WECHAT_PAY, true, LocalDateTime.now().minusDays(1).minusHours(4));

        OrderEntity confirmedOrder = ensureOrder(
                "[DEMO_CONFIRMED]",
                userB.getId(),
                mainWorker.getId(),
                "周明哲",
                USER_B_PHONE,
                "上海市静安区昌平路 228 弄 3 号楼 602",
                today.plusDays(3).toString(),
                "13:00-15:00",
                "收纳整理",
                OrderStatus.CONFIRMED,
                "用户已确认预约，等待服务人员上门",
                "衣柜与儿童玩具区需要分区整理"
        );
        ensureProgresses(confirmedOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约，等待服务人员上门")
        ));
        ensurePayment(confirmedOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.ALIPAY, true, LocalDateTime.now().minusDays(1).minusHours(2));

        OrderEntity inServiceOrder = ensureOrder(
                "[DEMO_IN_SERVICE]",
                userA.getId(),
                mainWorker.getId(),
                "林若溪",
                USER_A_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.toString(),
                "15:00-17:00",
                "家电清洗",
                OrderStatus.IN_SERVICE,
                "服务人员已上门，正在进行空调拆洗",
                "需要清洗 2 台挂机空调"
        );
        ensureProgresses(inServiceOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已上门，正在进行空调拆洗")
        ));
        ensurePayment(inServiceOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.WECHAT_PAY, true, LocalDateTime.now().minusHours(8));
        ensureServiceRecords(inServiceOrder.getId(), mainWorker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "到达现场并完成上门打卡", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "已完成首轮过滤网拆洗和外壳除尘", SERVICE_PROGRESS_URL)
        ));

        OrderEntity waitingConfirmOrder = ensureOrder(
                "[DEMO_WAIT_CONFIRM]",
                userB.getId(),
                mainWorker.getId(),
                "周明哲",
                USER_B_PHONE,
                "上海市静安区昌平路 228 弄 3 号楼 602",
                today.toString(),
                "18:00-20:00",
                "深度清洁",
                OrderStatus.WAITING_USER_CONFIRMATION,
                "服务人员已提交完工，等待用户确认",
                "重点清理厨房油污和窗台缝隙"
        );
        ensureProgresses(waitingConfirmOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已上门，正在进行深度清洁"),
                progress(OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认")
        ));
        ensurePayment(waitingConfirmOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.ALIPAY, true, LocalDateTime.now().minusHours(6));
        ensureServiceRecords(waitingConfirmOrder.getId(), mainWorker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "到达现场并拍摄入户打卡", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "完成厨房、卫生间和客厅的重点区域清洁", SERVICE_PROGRESS_URL),
                serviceRecord(OrderServiceRecordStage.FINISH_PROOF, "已完成完工收尾并上传现场照片", SERVICE_FINISH_URL)
        ));

        OrderEntity completedReviewedOrder = ensureOrder(
                "[DEMO_COMPLETED_REVIEW]",
                userA.getId(),
                mainWorker.getId(),
                "林若溪",
                USER_A_PHONE,
                "上海市徐汇区衡山路 106 弄 5 号楼 901",
                today.minusDays(1).toString(),
                "13:00-15:00",
                "日常保洁",
                OrderStatus.COMPLETED,
                "用户已确认完工，订单完成",
                "卧室与客厅基础保洁"
        );
        ensureProgresses(completedReviewedOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已完成现场清洁服务"),
                progress(OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认"),
                progress(OrderStatus.COMPLETED, "用户已确认完工，订单完成")
        ));
        ensurePayment(completedReviewedOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.WECHAT_PAY, true, LocalDateTime.now().minusDays(1).minusHours(6));
        ensureServiceRecords(completedReviewedOrder.getId(), mainWorker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "按预约时间到达并完成签到", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "完成卧室、客厅和地面除尘拖地", SERVICE_PROGRESS_URL),
                serviceRecord(OrderServiceRecordStage.FINISH_PROOF, "完工复核后上传现场整洁照片", SERVICE_FINISH_URL)
        ));
        ensureReview(completedReviewedOrder.getId(), userA.getId(), mainWorker.getId(), 5, "服务人员准时且沟通顺畅，整理和清洁都很细致。");

        OrderEntity completedAfterSaleOrder = ensureOrder(
                "[DEMO_COMPLETED_AFTER_SALE]",
                userB.getId(),
                mainWorker.getId(),
                "周明哲",
                USER_B_PHONE,
                "上海市静安区昌平路 228 弄 3 号楼 602",
                today.minusDays(2).toString(),
                "10:00-12:00",
                "家电清洗",
                OrderStatus.COMPLETED,
                "用户已确认完工，订单完成",
                "油烟机和洗衣机联动清洗"
        );
        ensureProgresses(completedAfterSaleOrder.getId(), List.of(
                progress(OrderStatus.PENDING, "用户已提交预约，等待服务人员接单"),
                progress(OrderStatus.ACCEPTED, "服务人员已接单，等待用户确认预约安排"),
                progress(OrderStatus.CONFIRMED, "用户已确认预约，等待服务人员上门"),
                progress(OrderStatus.IN_SERVICE, "服务人员已开始设备拆洗和除油"),
                progress(OrderStatus.WAITING_USER_CONFIRMATION, "服务人员已提交完工，等待用户确认"),
                progress(OrderStatus.COMPLETED, "用户已确认完工，订单完成")
        ));
        ensurePayment(completedAfterSaleOrder, mainWorker.getHourlyPrice(), OrderPaymentMethod.BANK_CARD, true, LocalDateTime.now().minusDays(2).minusHours(3));
        ensureServiceRecords(completedAfterSaleOrder.getId(), mainWorker.getId(), List.of(
                serviceRecord(OrderServiceRecordStage.CHECK_IN, "服务前完成设备外观拍照", SERVICE_CHECK_IN_URL),
                serviceRecord(OrderServiceRecordStage.SERVICE_PROOF, "已完成油烟机内部除油和洗衣机滚筒清洁", SERVICE_PROGRESS_URL),
                serviceRecord(OrderServiceRecordStage.FINISH_PROOF, "服务完成并上传完工凭证", SERVICE_FINISH_URL)
        ));
        ensureAfterSale(
                completedAfterSaleOrder.getId(),
                userB.getId(),
                mainWorker.getId(),
                "收费或沟通争议",
                "用户希望核对上门附加收费项，当前客服已介入处理中。",
                USER_B_PHONE,
                "PROCESSING",
                "客服已联系双方，等待补充收费依据。",
                AFTER_SALE_URL
        );

        ensureOrderMessage(
                acceptedOrder.getId(),
                userA.getId(),
                RoleCodes.USER,
                "林若溪",
                "我这边上午 10 点前都在家，如果需要提前联系可以直接留言。"
        );
        ensureOrderMessage(
                acceptedOrder.getId(),
                mainWorkerUser.getId(),
                RoleCodes.WORKER,
                "演示服务人员",
                "好的，我已经接单，等你确认后会按时上门。"
        );
        ensureOrderMessage(
                inServiceOrder.getId(),
                mainWorkerUser.getId(),
                RoleCodes.WORKER,
                "演示服务人员",
                "空调第一轮清洗已经完成，正在做滤网和外壳除尘。"
        );
        ensureOrderMessage(
                waitingConfirmOrder.getId(),
                userB.getId(),
                RoleCodes.USER,
                "周明哲",
                "我看到了完工凭证，等会回家后会尽快确认。"
        );

        ensureNotification(
                userA.getId(),
                RoleCodes.USER,
                NotificationType.ORDER_STATUS,
                "服务人员已接单",
                "订单 #" + acceptedOrder.getId() + " 已由服务人员接单，请确认预约安排。",
                "ORDER",
                acceptedOrder.getId(),
                "/user/orders"
        );
        ensureNotification(
                userB.getId(),
                RoleCodes.USER,
                NotificationType.ORDER_STATUS,
                "服务人员已提交完工",
                "订单 #" + waitingConfirmOrder.getId() + " 已提交完工，请及时确认服务结果。",
                "ORDER",
                waitingConfirmOrder.getId(),
                "/user/orders"
        );
        ensureNotification(
                mainWorkerUser.getId(),
                RoleCodes.WORKER,
                NotificationType.ORDER_MESSAGE,
                "订单有新的沟通消息",
                "用户就订单 #" + acceptedOrder.getId() + " 留下了新的备注消息。",
                "ORDER",
                acceptedOrder.getId(),
                "/worker/conversations?orderId=" + acceptedOrder.getId()
        );
        ensureNotification(
                pendingWorkerUser.getId(),
                RoleCodes.WORKER,
                NotificationType.WORKER_APPLICATION,
                "你的资质申请正在审核中",
                "平台已收到你的资质申请材料，审核结果会通过站内通知同步给你。",
                "WORKER_APPLICATION",
                0L,
                "/worker/qualification"
        );
        ensureNotification(
                authAccountService.findUserByPhone("13800000033").getId(),
                RoleCodes.ADMIN,
                NotificationType.AFTER_SALE,
                "有新的售后工单待处理",
                "订单 #" + completedAfterSaleOrder.getId() + " 存在一条处理中售后，请及时跟进。",
                "AFTER_SALE",
                completedAfterSaleOrder.getId(),
                "/admin/after-sales"
        );
    }

    private SysUserEntity ensureUser(String phone, String realName, String roleCode) {
        SysUserEntity user = authAccountService.findUserByPhone(phone);
        if (user == null) {
            user = authAccountService.createUser(phone, buildDemoUsername(phone, roleCode), "123456", realName);
        } else {
            authAccountService.updateUsernameIfBlank(user.getId(), buildDemoUsername(phone, roleCode));
        }
        authAccountService.bindRole(user.getId(), roleCode);
        return user;
    }

    private String buildDemoUsername(String phone, String roleCode) {
        String suffix = phone.length() > 4 ? phone.substring(phone.length() - 4) : phone;
        return switch (roleCode) {
            case RoleCodes.ADMIN -> "admin_" + suffix;
            case RoleCodes.WORKER -> "worker_" + suffix;
            default -> "user_" + suffix;
        };
    }

    private void ensureUserProfile(SysUserEntity user, String city, String detailAddress, String tag) {
        userProfileService.ensureProfileExists(user.getId());
        userProfileService.ensureSampleAddress(user.getId(), user.getRealName(), user.getPhone(), city, detailAddress, tag);
    }

    private void ensureWorkerProfile(SysUserEntity user,
                                     String realName,
                                     String serviceTypes,
                                     String availableSchedule,
                                     String serviceAreas,
                                     String intro,
                                     String certificates,
                                     String qualificationStatus) {
        workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                user.getId(),
                realName,
                "上海",
                109,
                serviceTypes,
                availableSchedule,
                5,
                certificates,
                serviceAreas,
                intro,
                qualificationStatus,
                "待审核服务人员",
                "演示用服务案例"
        ));
    }

    private WorkerEntity ensureMainWorkerProfile(SysUserEntity user) {
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
                "李阿姨",
                "上海",
                89,
                "日常保洁,深度清洁,家电清洗,收纳整理",
                "工作日白天,工作日晚间,周末白天",
                6,
                "健康证、家政培训结业证",
                "浦东新区,杨浦区,虹口区",
                "平台演示主服务人员，擅长家庭深度保洁与上门履约展示。",
                WorkerQualificationStatus.APPROVED,
                "平台认证家政服务人员",
                "新房开荒,租房退租深度清洁,厨房重点去油"
        ));
    }

    private void ensureWorkerApplication(Long userId,
                                         String realName,
                                         String phone,
                                         String serviceTypes,
                                         Integer yearsOfExperience,
                                         String certificates,
                                         String serviceAreas,
                                         String availableSchedule,
                                         String intro,
                                         String status,
                                         String adminRemark,
                                         List<WorkerApplicationAttachmentEntity> attachments,
                                         LocalDateTime createdAt) {
        WorkerApplicationEntity existed = workerApplicationMapper.selectOne(
                new LambdaQueryWrapper<WorkerApplicationEntity>()
                        .eq(WorkerApplicationEntity::getUserId, userId)
                        .eq(WorkerApplicationEntity::getStatus, status)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }

        WorkerApplicationEntity entity = new WorkerApplicationEntity();
        entity.setUserId(userId);
        entity.setRealName(realName);
        entity.setPhone(phone);
        entity.setServiceTypes(serviceTypes);
        entity.setYearsOfExperience(yearsOfExperience);
        entity.setCertificates(certificates);
        entity.setServiceAreas(serviceAreas);
        entity.setAvailableSchedule(availableSchedule);
        entity.setIntro(intro);
        entity.setStatus(status);
        entity.setAdminRemark(adminRemark);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        workerApplicationMapper.insert(entity);

        for (WorkerApplicationAttachmentEntity attachment : attachments) {
            workerApplicationAttachmentMapper.insert(new WorkerApplicationAttachmentEntity(
                    entity.getId(),
                    attachment.getFileName(),
                    attachment.getFileUrl(),
                    attachment.getFileSize(),
                    createdAt
            ));
        }
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
                "SCENE" + order.getId(),
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

        LocalDateTime now = LocalDateTime.now().minusHours(4);
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
            now = now.plusMinutes(40);
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
                LocalDateTime.now().minusDays(1)
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

        LocalDateTime createdAt = LocalDateTime.now().minusHours(10);
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
                createdAt.plusHours(2)
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
                LocalDateTime.now().minusHours(3)
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

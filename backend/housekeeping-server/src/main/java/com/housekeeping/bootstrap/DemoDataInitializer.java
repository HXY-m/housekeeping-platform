package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.user.entity.UserAddressEntity;
import com.housekeeping.user.entity.UserProfileEntity;
import com.housekeeping.user.mapper.UserAddressMapper;
import com.housekeeping.user.mapper.UserProfileMapper;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(30)
public class DemoDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_CITY = "上海";

    private static final String DEMO_USER_PHONE = "13800000011";
    private static final String DEMO_USER_USERNAME = "demo_user";
    private static final String DEMO_USER_NAME = "Demo User";

    private static final String DEMO_USER_EXTRA_A_PHONE = "13800000044";
    private static final String DEMO_USER_EXTRA_A_USERNAME = "demo_user_a";
    private static final String DEMO_USER_EXTRA_A_NAME = "林若溪";

    private static final String DEMO_USER_EXTRA_B_PHONE = "13800000045";
    private static final String DEMO_USER_EXTRA_B_USERNAME = "demo_user_b";
    private static final String DEMO_USER_EXTRA_B_NAME = "周明哲";

    private static final String DEMO_WORKER_PHONE = "13800000022";
    private static final String DEMO_WORKER_USERNAME = "demo_worker";
    private static final String DEMO_WORKER_NAME = "李阿姨";

    private static final String DEMO_WORKER_WANG_PHONE = "13800000023";
    private static final String DEMO_WORKER_WANG_USERNAME = "demo_worker_wang";
    private static final String DEMO_WORKER_WANG_NAME = "王师傅";

    private static final String DEMO_WORKER_ZHOU_PHONE = "13800000024";
    private static final String DEMO_WORKER_ZHOU_USERNAME = "demo_worker_zhou";
    private static final String DEMO_WORKER_ZHOU_NAME = "周阿姨";

    private static final String DEMO_WORKER_PENDING_PHONE = "13800000055";
    private static final String DEMO_WORKER_PENDING_USERNAME = "demo_worker_pending";
    private static final String DEMO_WORKER_PENDING_NAME = "陈雅琪";

    private static final String DEMO_WORKER_REJECTED_PHONE = "13800000066";
    private static final String DEMO_WORKER_REJECTED_USERNAME = "demo_worker_rejected";
    private static final String DEMO_WORKER_REJECTED_NAME = "孙海波";

    private static final String DEMO_ADMIN_PHONE = "13800000033";
    private static final String DEMO_ADMIN_USERNAME = "demo_admin";
    private static final String DEMO_ADMIN_NAME = "Demo Admin";

    private final ServiceCategoryMapper categoryMapper;
    private final WorkerMapper workerMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAddressMapper userAddressMapper;
    private final AuthAccountService authAccountService;

    public DemoDataInitializer(ServiceCategoryMapper categoryMapper,
                               WorkerMapper workerMapper,
                               UserProfileMapper userProfileMapper,
                               UserAddressMapper userAddressMapper,
                               AuthAccountService authAccountService) {
        this.categoryMapper = categoryMapper;
        this.workerMapper = workerMapper;
        this.userProfileMapper = userProfileMapper;
        this.userAddressMapper = userAddressMapper;
        this.authAccountService = authAccountService;
    }

    @Override
    public void run(String... args) {
        ensureCategories();

        Long demoUserId = ensureCoreAccount(DEMO_USER_PHONE, DEMO_USER_USERNAME, DEMO_USER_NAME, RoleCodes.USER);
        Long demoUserExtraAId = ensureCoreAccount(DEMO_USER_EXTRA_A_PHONE, DEMO_USER_EXTRA_A_USERNAME, DEMO_USER_EXTRA_A_NAME, RoleCodes.USER);
        Long demoUserExtraBId = ensureCoreAccount(DEMO_USER_EXTRA_B_PHONE, DEMO_USER_EXTRA_B_USERNAME, DEMO_USER_EXTRA_B_NAME, RoleCodes.USER);
        ensureCoreAccount(DEMO_ADMIN_PHONE, DEMO_ADMIN_USERNAME, DEMO_ADMIN_NAME, RoleCodes.ADMIN);

        Long demoWorkerId = ensureCoreAccount(DEMO_WORKER_PHONE, DEMO_WORKER_USERNAME, DEMO_WORKER_NAME, RoleCodes.WORKER);
        Long demoWorkerWangId = ensureCoreAccount(DEMO_WORKER_WANG_PHONE, DEMO_WORKER_WANG_USERNAME, DEMO_WORKER_WANG_NAME, RoleCodes.WORKER);
        Long demoWorkerZhouId = ensureCoreAccount(DEMO_WORKER_ZHOU_PHONE, DEMO_WORKER_ZHOU_USERNAME, DEMO_WORKER_ZHOU_NAME, RoleCodes.WORKER);
        Long demoWorkerPendingId = ensureCoreAccount(DEMO_WORKER_PENDING_PHONE, DEMO_WORKER_PENDING_USERNAME, DEMO_WORKER_PENDING_NAME, RoleCodes.WORKER);
        Long demoWorkerRejectedId = ensureCoreAccount(DEMO_WORKER_REJECTED_PHONE, DEMO_WORKER_REJECTED_USERNAME, DEMO_WORKER_REJECTED_NAME, RoleCodes.WORKER);

        ensureSampleAddress(demoUserId, DEMO_USER_NAME, DEMO_USER_PHONE, DEFAULT_CITY, "上海市徐汇区衡山路 106 弄 5 号楼 901", "常用");
        ensureSampleAddress(demoUserExtraAId, DEMO_USER_EXTRA_A_NAME, DEMO_USER_EXTRA_A_PHONE, DEFAULT_CITY, "上海市静安区昌平路 228 弄 3 号楼 602", "公司");
        ensureSampleAddress(demoUserExtraBId, DEMO_USER_EXTRA_B_NAME, DEMO_USER_EXTRA_B_PHONE, DEFAULT_CITY, "上海市浦东新区锦绣路 520 弄 8 号楼 703", "父母家");

        ensurePublicWorker(
                demoWorkerId,
                DEMO_WORKER_NAME,
                "平台认证保洁师",
                4.90,
                286,
                68,
                DEFAULT_CITY,
                "擅长家庭日常保洁、深度清洁与空间整理，服务稳定细致。",
                "日常保洁,深度清洁,收纳整理",
                "今天 18:30 后可约",
                6,
                "家政服务培训证书,健康证",
                "浦东新区,杨浦区,虹口区",
                "退租保洁,厨房重点除油,长期家庭保洁"
        );
        ensurePublicWorker(
                demoWorkerWangId,
                DEMO_WORKER_WANG_NAME,
                "平台认证家电清洗师",
                4.82,
                193,
                88,
                DEFAULT_CITY,
                "专注空调、洗衣机和油烟机清洗，支持上门过程反馈。",
                "家电清洗,空调拆洗,油烟机清洗",
                "明天 09:00 可约",
                5,
                "健康证,家电清洗专项证书",
                "徐汇区,闵行区,浦东新区",
                "挂机空调清洗,油烟机深度拆洗,换季家电保养"
        );
        ensurePublicWorker(
                demoWorkerZhouId,
                DEMO_WORKER_ZHOU_NAME,
                "平台认证母婴护理师",
                4.95,
                126,
                128,
                DEFAULT_CITY,
                "有母婴护理经验，熟悉产后家庭支持与新生儿基础照护。",
                "母婴护理,育儿陪护,老人陪护",
                "后天 08:00 可约",
                7,
                "健康证,母婴护理证",
                "浦东新区,长宁区,静安区",
                "新生儿陪护,产后家庭照护,老人日间陪伴"
        );
        ensureScenarioWorker(
                demoWorkerPendingId,
                DEMO_WORKER_PENDING_NAME,
                DEFAULT_CITY,
                109,
                "母婴护理,育儿陪护,老人陪护",
                "工作日白天,周末白天",
                5,
                "健康证,母婴护理证",
                "浦东新区,杨浦区",
                "有月嫂和养老护理双线经验，当前资质待审核。",
                WorkerQualificationStatus.PENDING,
                "待审核服务人员",
                "母婴照护,老人陪护"
        );
        ensureScenarioWorker(
                demoWorkerRejectedId,
                DEMO_WORKER_REJECTED_NAME,
                DEFAULT_CITY,
                99,
                "家电清洗,深度清洁",
                "工作日晚间,周末晚间",
                4,
                "健康证,家电清洗专项证",
                "闵行区,徐汇区",
                "擅长家电清洗，当前因资料缺失处于驳回状态。",
                WorkerQualificationStatus.REJECTED,
                "已驳回服务人员",
                "空调拆洗,油烟机清洗"
        );
    }

    private void ensureCategories() {
        if (categoryMapper.selectCount(null) > 0) {
            return;
        }

        categoryMapper.insert(new ServiceCategoryEntity(
                "日常保洁",
                "适合日常家庭保洁、基础除尘和厨房卫生间维护的上门服务。",
                "¥129 起",
                "daily-cleaning",
                "2 小时 / 4 小时",
                "客厅、卧室、厨房、卫生间",
                "日常保洁、入住保洁",
                "玻璃擦拭,冰箱外部清洁",
                "/uploads/demo/services/daily-cleaning.svg",
                1
        ));
        categoryMapper.insert(new ServiceCategoryEntity(
                "深度清洁",
                "针对重油污、顽固污渍和换季整理场景的深度清洁服务。",
                "¥229 起",
                "deep-cleaning",
                "4 小时 / 半天",
                "厨房、卫生间、阳台重点区域",
                "退租保洁、开荒保洁、节前大扫除",
                "油污去除,玻璃深度清洁",
                "/uploads/demo/services/deep-cleaning.svg",
                1
        ));
        categoryMapper.insert(new ServiceCategoryEntity(
                "母婴护理",
                "面向产后恢复与婴幼儿照护场景的上门护理服务。",
                "¥368 起",
                "maternal-care",
                "半天 / 全天",
                "月子照护、基础育儿支持",
                "产后家庭、新生儿陪护",
                "喂养协助,夜间陪护",
                "/uploads/demo/services/maternal-care.svg",
                1
        ));
        categoryMapper.insert(new ServiceCategoryEntity(
                "老人陪护",
                "提供日常照护、陪诊协助与基础康复陪伴服务。",
                "¥299 起",
                "elderly-care",
                "半天 / 全天",
                "居家陪护、陪诊、康复协助",
                "老人照护、术后陪伴",
                "陪诊,买药协助",
                "/uploads/demo/services/elderly-care.svg",
                1
        ));
        categoryMapper.insert(new ServiceCategoryEntity(
                "家电清洗",
                "提供空调、油烟机、洗衣机等家电拆洗与维护服务。",
                "¥149 起",
                "appliance-cleaning",
                "2 小时 / 单台计费",
                "空调、洗衣机、油烟机",
                "换季清洗、搬家前后维护",
                "过滤网拆洗,外壳除尘",
                "/uploads/demo/services/appliance-cleaning.svg",
                1
        ));
    }

    private Long ensureCoreAccount(String phone, String username, String realName, String roleCode) {
        SysUserEntity user = authAccountService.findUserByPhone(phone);
        if (user == null) {
            user = authAccountService.createUser(phone, username, "123456", realName);
        } else {
            authAccountService.updateUsernameIfBlank(user.getId(), username);
        }
        authAccountService.bindRole(user.getId(), roleCode);
        ensureUserProfileCity(user.getId(), DEFAULT_CITY);
        return user.getId();
    }

    private void ensureUserProfileCity(Long userId, String city) {
        UserProfileEntity profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfileEntity>()
                        .eq(UserProfileEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (profile == null) {
            userProfileMapper.insert(new UserProfileEntity(userId, "", city, "", ""));
            return;
        }
        if (profile.getCity() == null || profile.getCity().isBlank()) {
            profile.setCity(city);
            userProfileMapper.updateById(profile);
        }
    }

    private void ensureSampleAddress(Long userId,
                                     String contactName,
                                     String contactPhone,
                                     String city,
                                     String detailAddress,
                                     String addressTag) {
        Long count = userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId));
        if (count != null && count > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        userAddressMapper.insert(new UserAddressEntity(
                userId,
                contactName,
                contactPhone,
                city,
                detailAddress,
                addressTag,
                true,
                null,
                null,
                now,
                now
        ));
    }

    private void ensurePublicWorker(Long userId,
                                    String realName,
                                    String roleLabel,
                                    Double rating,
                                    Integer completedOrders,
                                    Integer hourlyPrice,
                                    String city,
                                    String intro,
                                    String tags,
                                    String nextAvailable,
                                    Integer yearsOfExperience,
                                    String certificates,
                                    String serviceAreas,
                                    String serviceCases) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            worker = new WorkerEntity();
            worker.setUserId(userId);
            worker.setAvatarUrl("");
        }

        worker.setName(realName);
        worker.setRoleLabel(roleLabel);
        worker.setRating(rating);
        worker.setCompletedOrders(completedOrders);
        worker.setHourlyPrice(hourlyPrice);
        worker.setCity(city);
        worker.setIntro(intro);
        worker.setTags(tags);
        worker.setNextAvailable(nextAvailable);
        worker.setYearsOfExperience(yearsOfExperience);
        worker.setCertificates(certificates);
        worker.setServiceAreas(serviceAreas);
        worker.setServiceCases(serviceCases);
        worker.setQualificationStatus(WorkerQualificationStatus.APPROVED);

        if (worker.getId() == null) {
            workerMapper.insert(worker);
        } else {
            workerMapper.updateById(worker);
        }
    }

    private void ensureScenarioWorker(Long userId,
                                      String realName,
                                      String city,
                                      Integer hourlyPrice,
                                      String serviceTypes,
                                      String availableSchedule,
                                      Integer yearsOfExperience,
                                      String certificates,
                                      String serviceAreas,
                                      String intro,
                                      String qualificationStatus,
                                      String roleLabel,
                                      String serviceCases) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            worker = new WorkerEntity();
            worker.setUserId(userId);
            worker.setRating(5.0);
            worker.setCompletedOrders(0);
            worker.setAvatarUrl("");
        }

        worker.setName(realName);
        worker.setRoleLabel(roleLabel);
        worker.setHourlyPrice(hourlyPrice);
        worker.setCity(city);
        worker.setIntro(intro);
        worker.setTags(serviceTypes);
        worker.setNextAvailable(availableSchedule);
        worker.setYearsOfExperience(yearsOfExperience);
        worker.setCertificates(certificates);
        worker.setServiceAreas(serviceAreas);
        worker.setServiceCases(serviceCases);
        worker.setQualificationStatus(qualificationStatus);

        if (worker.getId() == null) {
            workerMapper.insert(worker);
        } else {
            workerMapper.updateById(worker);
        }
    }
}

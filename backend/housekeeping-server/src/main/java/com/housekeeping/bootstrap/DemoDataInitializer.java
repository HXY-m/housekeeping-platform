package com.housekeeping.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
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

@Component
@Order(30)
public class DemoDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_CITY = "上海";
    private static final String DEMO_USER_PHONE = "13800000011";
    private static final String DEMO_USER_USERNAME = "demo_user";
    private static final String DEMO_USER_NAME = "Demo User";
    private static final String DEMO_WORKER_PHONE = "13800000022";
    private static final String DEMO_WORKER_USERNAME = "demo_worker";
    private static final String DEMO_WORKER_NAME = "李阿姨";
    private static final String DEMO_ADMIN_PHONE = "13800000033";
    private static final String DEMO_ADMIN_USERNAME = "demo_admin";
    private static final String DEMO_ADMIN_NAME = "Demo Admin";

    private final ServiceCategoryMapper categoryMapper;
    private final WorkerMapper workerMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAddressMapper userAddressMapper;
    private final AuthAccountService authAccountService;

    public DemoDataInitializer(ServiceCategoryMapper categoryMapper,
                               WorkerMapper workerMapper,
                               SysUserMapper sysUserMapper,
                               SysUserRoleMapper sysUserRoleMapper,
                               UserProfileMapper userProfileMapper,
                               UserAddressMapper userAddressMapper,
                               AuthAccountService authAccountService) {
        this.categoryMapper = categoryMapper;
        this.workerMapper = workerMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.userProfileMapper = userProfileMapper;
        this.userAddressMapper = userAddressMapper;
        this.authAccountService = authAccountService;
    }

    @Override
    public void run(String... args) {
        ensureCategories();
        ensureCoreAccount(DEMO_USER_PHONE, DEMO_USER_USERNAME, DEMO_USER_NAME, RoleCodes.USER);
        ensureCoreAccount(DEMO_ADMIN_PHONE, DEMO_ADMIN_USERNAME, DEMO_ADMIN_NAME, RoleCodes.ADMIN);
        Long workerUserId = ensureCoreAccount(DEMO_WORKER_PHONE, DEMO_WORKER_USERNAME, DEMO_WORKER_NAME, RoleCodes.WORKER);
        ensureDefaultWorker(workerUserId);
        clearLegacyDefaultWorkerAccounts();
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

        boolean needUpdate = false;
        if (!realName.equals(user.getRealName())) {
            user.setRealName(realName);
            needUpdate = true;
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("ACTIVE");
            needUpdate = true;
        }
        if (needUpdate) {
            sysUserMapper.updateById(user);
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

    private void ensureDefaultWorker(Long userId) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            worker = new WorkerEntity();
        }

        worker.setUserId(userId);
        worker.setName(DEMO_WORKER_NAME);
        worker.setRoleLabel("平台认证保洁师");
        worker.setRating(4.90);
        worker.setCompletedOrders(286);
        worker.setHourlyPrice(68);
        worker.setCity(DEFAULT_CITY);
        worker.setIntro("擅长家庭日常保洁、深度清洁与空间整理，服务稳定细致。");
        worker.setTags("日常保洁,深度清洁,收纳整理");
        worker.setNextAvailable("今天 18:30 后可约");
        worker.setYearsOfExperience(6);
        worker.setCertificates("家政服务培训证书,健康证");
        worker.setServiceAreas("浦东新区,杨浦区,虹口区");
        worker.setServiceCases("退租保洁,厨房重油污清洁,长期家庭保洁");
        worker.setQualificationStatus(WorkerQualificationStatus.APPROVED);
        if (worker.getAvatarUrl() == null) {
            worker.setAvatarUrl("");
        }

        if (worker.getId() == null) {
            workerMapper.insert(worker);
        } else {
            workerMapper.updateById(worker);
        }
    }

    private void clearLegacyDefaultWorkerAccounts() {
        clearLegacyWorker("13800000023", "王师傅");
        clearLegacyWorker("13800000024", "周阿姨");
    }

    private void clearLegacyWorker(String phone, String workerName) {
        SysUserEntity user = authAccountService.findUserByPhone(phone);
        if (user != null) {
            workerMapper.delete(new LambdaQueryWrapper<WorkerEntity>()
                    .eq(WorkerEntity::getUserId, user.getId()));
            userAddressMapper.delete(new LambdaQueryWrapper<UserAddressEntity>()
                    .eq(UserAddressEntity::getUserId, user.getId()));
            userProfileMapper.delete(new LambdaQueryWrapper<UserProfileEntity>()
                    .eq(UserProfileEntity::getUserId, user.getId()));
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>()
                    .eq(SysUserRoleEntity::getUserId, user.getId()));
            sysUserMapper.deleteById(user.getId());
        }

        workerMapper.delete(new LambdaQueryWrapper<WorkerEntity>()
                .isNull(WorkerEntity::getUserId)
                .eq(WorkerEntity::getName, workerName));
    }
}

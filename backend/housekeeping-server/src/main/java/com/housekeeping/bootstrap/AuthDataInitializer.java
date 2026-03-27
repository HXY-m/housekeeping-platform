package com.housekeeping.bootstrap;

import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.user.service.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final AuthAccountService authAccountService;
    private final UserProfileService userProfileService;

    public AuthDataInitializer(AuthAccountService authAccountService,
                               UserProfileService userProfileService) {
        this.authAccountService = authAccountService;
        this.userProfileService = userProfileService;
    }

    @Override
    public void run(String... args) {
        authAccountService.ensureBaseRolesExist();

        SysUserEntity user = ensureUser("13800000011", "演示用户");
        SysUserEntity worker = ensureUser("13800000022", "演示服务人员");
        SysUserEntity admin = ensureUser("13800000033", "演示管理员");

        authAccountService.bindRole(user.getId(), RoleCodes.USER);
        authAccountService.bindRole(worker.getId(), RoleCodes.WORKER);
        authAccountService.bindRole(admin.getId(), RoleCodes.ADMIN);

        userProfileService.ensureProfileExists(user.getId());
        userProfileService.ensureSampleAddress(
                user.getId(),
                "演示用户",
                "13800000011",
                "上海市",
                "浦东新区张杨路 88 弄 6 号楼 1202",
                "家庭"
        );
    }

    private SysUserEntity ensureUser(String phone, String realName) {
        SysUserEntity existed = authAccountService.findUserByPhone(phone);
        if (existed != null) {
            return existed;
        }
        return authAccountService.createUser(phone, "123456", realName);
    }
}

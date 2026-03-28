package com.housekeeping.bootstrap;

import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.service.AuthAccountService;
import com.housekeeping.auth.service.RolePermissionService;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.user.service.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class AuthDataInitializer implements CommandLineRunner {

    private final AuthAccountService authAccountService;
    private final RolePermissionService rolePermissionService;
    private final UserProfileService userProfileService;

    public AuthDataInitializer(AuthAccountService authAccountService,
                               RolePermissionService rolePermissionService,
                               UserProfileService userProfileService) {
        this.authAccountService = authAccountService;
        this.rolePermissionService = rolePermissionService;
        this.userProfileService = userProfileService;
    }

    @Override
    public void run(String... args) {
        authAccountService.ensureBaseRolesExist();
        rolePermissionService.ensureBasePermissionsExist();

        SysUserEntity user = ensureUser("13800000011", "Demo User");
        SysUserEntity worker = ensureUser("13800000022", "Demo Worker");
        SysUserEntity admin = ensureUser("13800000033", "Demo Admin");

        authAccountService.bindRole(user.getId(), RoleCodes.USER);
        authAccountService.bindRole(worker.getId(), RoleCodes.WORKER);
        authAccountService.bindRole(admin.getId(), RoleCodes.ADMIN);

        userProfileService.ensureProfileExists(user.getId());
        userProfileService.ensureSampleAddress(
                user.getId(),
                "Demo User",
                "13800000011",
                "Shanghai",
                "Pudong New Area, Zhangyang Road 188, Building 6, Room 1202",
                "Home"
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

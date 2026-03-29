package com.housekeeping.auth.service;

import com.housekeeping.auth.dto.CurrentUserDto;
import com.housekeeping.auth.dto.DemoAccountDto;
import com.housekeeping.auth.dto.LoginRequest;
import com.housekeeping.auth.dto.LoginResponse;
import com.housekeeping.auth.dto.RegisterRequest;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.auth.util.PasswordUtil;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.user.service.UserProfileService;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthAccountService authAccountService;
    private final JwtTokenService jwtTokenService;
    private final RolePermissionService rolePermissionService;
    private final UserProfileService userProfileService;
    private final WorkerProfileService workerProfileService;

    public AuthService(AuthAccountService authAccountService,
                       JwtTokenService jwtTokenService,
                       RolePermissionService rolePermissionService,
                       UserProfileService userProfileService,
                       WorkerProfileService workerProfileService) {
        this.authAccountService = authAccountService;
        this.jwtTokenService = jwtTokenService;
        this.rolePermissionService = rolePermissionService;
        this.userProfileService = userProfileService;
        this.workerProfileService = workerProfileService;
    }

    public LoginResponse login(LoginRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        SysUserEntity user = authAccountService.requireActiveUserByPhone(request.phone());

        if (!PasswordUtil.matches(request.password(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (!authAccountService.userHasRole(user.getId(), roleCode)) {
            throw new BusinessException("当前账号未绑定所选身份");
        }

        return buildLoginResponse(user, roleCode);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        if (RoleCodes.ADMIN.equals(roleCode)) {
            throw new BusinessException("管理员账号只能在后台创建");
        }

        authAccountService.ensurePhoneAvailable(request.phone());
        SysUserEntity user = authAccountService.createUser(
                request.phone().trim(),
                request.password().trim(),
                request.realName().trim()
        );
        authAccountService.bindRole(user.getId(), roleCode);
        userProfileService.ensureProfileExists(user.getId());

        if (RoleCodes.WORKER.equals(roleCode)) {
            workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                    user.getId(),
                    request.realName().trim(),
                    "",
                    null,
                    "",
                    "",
                    0,
                    "",
                    "",
                    "",
                    WorkerQualificationStatus.UNSUBMITTED,
                    "待资质认证服务人员",
                    "待补充服务案例"
            ));
        }

        return buildLoginResponse(user, roleCode);
    }

    public CurrentUserDto currentUser() {
        SessionUser sessionUser = CurrentUserContext.get();
        if (sessionUser == null) {
            throw new BusinessException("请先登录");
        }
        SysUserEntity user = authAccountService.requireActiveUserById(sessionUser.userId());
        return new CurrentUserDto(
                user.getId(),
                user.getPhone(),
                user.getRealName(),
                sessionUser.roleCode(),
                rolePermissionService.listPermissionCodes(user.getId(), sessionUser.roleCode())
        );
    }

    public List<DemoAccountDto> demoAccounts() {
        return List.of(
                new DemoAccountDto(RoleCodes.USER, "13800000011", "123456", "Demo User"),
                new DemoAccountDto(RoleCodes.WORKER, "13800000022", "123456", "Demo Worker"),
                new DemoAccountDto(RoleCodes.ADMIN, "13800000033", "123456", "Demo Admin")
        );
    }

    private LoginResponse buildLoginResponse(SysUserEntity user, String roleCode) {
        List<String> permissionCodes = rolePermissionService.listPermissionCodes(user.getId(), roleCode);
        String token = jwtTokenService.createToken(
                user.getId(),
                user.getPhone(),
                user.getRealName(),
                roleCode,
                permissionCodes
        );
        return new LoginResponse(
                token,
                new CurrentUserDto(user.getId(), user.getPhone(), user.getRealName(), roleCode, permissionCodes)
        );
    }

    private String normalizeRoleCode(String rawRoleCode) {
        String roleCode = rawRoleCode == null ? "" : rawRoleCode.trim().toUpperCase();
        if (!List.of(RoleCodes.USER, RoleCodes.WORKER, RoleCodes.ADMIN).contains(roleCode)) {
            throw new BusinessException("不支持的身份类型");
        }
        return roleCode;
    }
}

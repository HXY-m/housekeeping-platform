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
import org.springframework.util.StringUtils;

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
            throw new BusinessException("Incorrect password.");
        }
        if (!authAccountService.userHasRole(user.getId(), roleCode)) {
            throw new BusinessException("The selected role is not assigned to this account.");
        }

        return buildLoginResponse(user, roleCode);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        if (RoleCodes.ADMIN.equals(roleCode)) {
            throw new BusinessException("Admin accounts can only be created from the management console.");
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
            validateWorkerRegistration(request);
            workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                    user.getId(),
                    request.realName().trim(),
                    trimOrEmpty(request.city()),
                    request.hourlyPrice(),
                    request.serviceTypes().trim(),
                    request.availableSchedule().trim(),
                    request.yearsOfExperience(),
                    request.certificates().trim(),
                    request.serviceAreas().trim(),
                    request.intro().trim(),
                    WorkerQualificationStatus.UNSUBMITTED,
                    "Qualification not submitted",
                    "New worker account created. Public listing remains hidden until qualification review passes."
            ));
        }

        return buildLoginResponse(user, roleCode);
    }

    public CurrentUserDto currentUser() {
        SessionUser sessionUser = CurrentUserContext.get();
        if (sessionUser == null) {
            throw new BusinessException("Please log in first.");
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
            throw new BusinessException("Unsupported role type.");
        }
        return roleCode;
    }

    private void validateWorkerRegistration(RegisterRequest request) {
        if (!StringUtils.hasText(request.serviceTypes())
                || request.yearsOfExperience() == null
                || !StringUtils.hasText(request.certificates())
                || !StringUtils.hasText(request.serviceAreas())
                || !StringUtils.hasText(request.availableSchedule())
                || !StringUtils.hasText(request.intro())) {
            throw new BusinessException("Worker registration requires service types, experience, certificates, areas, schedule and introduction.");
        }
        if (request.yearsOfExperience() < 0) {
            throw new BusinessException("Years of experience cannot be less than 0.");
        }
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

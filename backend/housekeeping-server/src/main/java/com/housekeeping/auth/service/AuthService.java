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
    private final UserProfileService userProfileService;
    private final WorkerProfileService workerProfileService;

    public AuthService(AuthAccountService authAccountService,
                       JwtTokenService jwtTokenService,
                       UserProfileService userProfileService,
                       WorkerProfileService workerProfileService) {
        this.authAccountService = authAccountService;
        this.jwtTokenService = jwtTokenService;
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
            throw new BusinessException("当前账号不具备所选角色");
        }

        return buildLoginResponse(user, roleCode);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        if (RoleCodes.ADMIN.equals(roleCode)) {
            throw new BusinessException("管理员账号仅支持后台创建");
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
                    "待认证服务人员",
                    "新注册服务人员，等待资质审核通过后对外展示"
            ));
        }

        return buildLoginResponse(user, roleCode);
    }

    public CurrentUserDto currentUser() {
        SessionUser sessionUser = CurrentUserContext.get();
        if (sessionUser == null) {
            throw new BusinessException("未登录");
        }
        SysUserEntity user = authAccountService.requireActiveUserById(sessionUser.userId());
        return new CurrentUserDto(user.getId(), user.getPhone(), user.getRealName(), sessionUser.roleCode());
    }

    public List<DemoAccountDto> demoAccounts() {
        return List.of(
                new DemoAccountDto(RoleCodes.USER, "13800000011", "123456", "演示用户"),
                new DemoAccountDto(RoleCodes.WORKER, "13800000022", "123456", "演示服务人员"),
                new DemoAccountDto(RoleCodes.ADMIN, "13800000033", "123456", "演示管理员")
        );
    }

    private LoginResponse buildLoginResponse(SysUserEntity user, String roleCode) {
        String token = jwtTokenService.createToken(user.getId(), user.getPhone(), user.getRealName(), roleCode);
        return new LoginResponse(token, new CurrentUserDto(user.getId(), user.getPhone(), user.getRealName(), roleCode));
    }

    private String normalizeRoleCode(String rawRoleCode) {
        String roleCode = rawRoleCode == null ? "" : rawRoleCode.trim().toUpperCase();
        if (!List.of(RoleCodes.USER, RoleCodes.WORKER, RoleCodes.ADMIN).contains(roleCode)) {
            throw new BusinessException("不支持的角色类型");
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
            throw new BusinessException("注册服务人员时请完整填写服务类型、从业年限、证书、区域、时段和介绍");
        }
        if (request.yearsOfExperience() < 0) {
            throw new BusinessException("从业年限不能小于 0");
        }
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}

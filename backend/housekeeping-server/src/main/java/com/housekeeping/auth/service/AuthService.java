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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthAccountService authAccountService;
    private final SessionService sessionService;

    public AuthService(AuthAccountService authAccountService,
                       SessionService sessionService) {
        this.authAccountService = authAccountService;
        this.sessionService = sessionService;
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
        authAccountService.ensurePhoneAvailable(request.phone());
        SysUserEntity user = authAccountService.createUser(
                request.phone().trim(),
                request.password().trim(),
                request.realName().trim()
        );
        authAccountService.bindRole(user.getId(), RoleCodes.USER);
        return buildLoginResponse(user, RoleCodes.USER);
    }

    public CurrentUserDto currentUser() {
        SessionUser user = CurrentUserContext.get();
        if (user == null) {
            throw new BusinessException("未登录");
        }
        return new CurrentUserDto(user.userId(), user.phone(), user.realName(), user.roleCode());
    }

    public List<DemoAccountDto> demoAccounts() {
        return List.of(
                new DemoAccountDto(RoleCodes.USER, "13800000011", "123456", "演示用户"),
                new DemoAccountDto(RoleCodes.WORKER, "13800000022", "123456", "演示服务人员"),
                new DemoAccountDto(RoleCodes.ADMIN, "13800000033", "123456", "演示管理员")
        );
    }

    private LoginResponse buildLoginResponse(SysUserEntity user, String roleCode) {
        String token = sessionService.createToken(user.getId(), user.getPhone(), user.getRealName(), roleCode);
        return new LoginResponse(token, new CurrentUserDto(user.getId(), user.getPhone(), user.getRealName(), roleCode));
    }

    private String normalizeRoleCode(String rawRoleCode) {
        String roleCode = rawRoleCode == null ? "" : rawRoleCode.trim().toUpperCase();
        if (!List.of(RoleCodes.USER, RoleCodes.WORKER, RoleCodes.ADMIN).contains(roleCode)) {
            throw new BusinessException("不支持的登录角色");
        }
        return roleCode;
    }
}

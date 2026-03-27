package com.housekeeping.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.dto.CurrentUserDto;
import com.housekeeping.auth.dto.DemoAccountDto;
import com.housekeeping.auth.dto.LoginRequest;
import com.housekeeping.auth.dto.LoginResponse;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.auth.util.PasswordUtil;
import com.housekeeping.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SessionService sessionService;

    public AuthService(SysUserMapper sysUserMapper,
                       SysRoleMapper sysRoleMapper,
                       SysUserRoleMapper sysUserRoleMapper,
                       SessionService sessionService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sessionService = sessionService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUserEntity user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getPhone, request.phone())
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (!PasswordUtil.matches(request.password(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        List<SysUserRoleEntity> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, user.getId())
        );
        if (userRoles.isEmpty()) {
            throw new BusinessException("账号未分配角色");
        }

        Map<Long, SysRoleEntity> roleMap = sysRoleMapper.selectBatchIds(
                        userRoles.stream().map(SysUserRoleEntity::getRoleId).toList())
                .stream()
                .collect(Collectors.toMap(SysRoleEntity::getId, Function.identity()));

        boolean matched = userRoles.stream()
                .map(item -> roleMap.get(item.getRoleId()))
                .filter(role -> role != null)
                .anyMatch(role -> request.roleCode().equalsIgnoreCase(role.getRoleCode()));

        if (!matched) {
            throw new BusinessException("当前账号不具备所选角色");
        }

        String roleCode = request.roleCode().toUpperCase();
        String token = sessionService.createToken(user.getId(), user.getPhone(), user.getRealName(), roleCode);
        return new LoginResponse(token, new CurrentUserDto(user.getId(), user.getPhone(), user.getRealName(), roleCode));
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
                new DemoAccountDto("USER", "13800000011", "123456", "演示用户"),
                new DemoAccountDto("WORKER", "13800000022", "123456", "演示服务人员"),
                new DemoAccountDto("ADMIN", "13800000033", "123456", "演示管理员")
        );
    }
}

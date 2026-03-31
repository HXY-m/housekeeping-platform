package com.housekeeping.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.util.PasswordUtil;
import com.housekeeping.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuthAccountService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public AuthAccountService(SysUserMapper sysUserMapper,
                              SysRoleMapper sysRoleMapper,
                              SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    public SysUserEntity findUserByPhone(String phone) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getPhone, phone)
                .last("limit 1"));
    }

    public SysUserEntity findUserByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, username)
                .last("limit 1"));
    }

    public SysUserEntity requireActiveUserByPhone(String phone) {
        return requireActiveUser(findUserByPhone(phone));
    }

    public SysUserEntity requireActiveUserByUsername(String username) {
        return requireActiveUser(findUserByUsername(username));
    }

    public SysUserEntity requireActiveUserByLogin(String loginType, String account) {
        String normalizedType = loginType == null ? "" : loginType.trim().toUpperCase();
        String normalizedAccount = account == null ? "" : account.trim();
        return switch (normalizedType) {
            case "PHONE" -> requireActiveUserByPhone(normalizedAccount);
            case "USERNAME" -> requireActiveUserByUsername(normalizedAccount);
            default -> throw new BusinessException("不支持的登录方式");
        };
    }

    public SysUserEntity requireActiveUserById(Long userId) {
        return requireActiveUser(sysUserMapper.selectById(userId));
    }

    private SysUserEntity requireActiveUser(SysUserEntity user) {
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        return user;
    }

    public void ensurePhoneAvailable(String phone) {
        if (findUserByPhone(phone) != null) {
            throw new BusinessException("该手机号已经注册");
        }
    }

    public void ensureUsernameAvailable(String username) {
        if (StringUtils.hasText(username) && findUserByUsername(username.trim()) != null) {
            throw new BusinessException("该用户名已被占用");
        }
    }

    @Transactional
    public SysUserEntity createUser(String phone, String username, String rawPassword, String realName) {
        SysUserEntity entity = new SysUserEntity(
                phone,
                StringUtils.hasText(username) ? username.trim() : null,
                PasswordUtil.sha256(rawPassword),
                realName,
                "ACTIVE"
        );
        try {
            sysUserMapper.insert(entity);
            return entity;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("该手机号或用户名已经被使用");
        }
    }

    @Transactional
    public void updateUsernameIfBlank(Long userId, String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        SysUserEntity user = sysUserMapper.selectById(userId);
        if (user == null || StringUtils.hasText(user.getUsername())) {
            return;
        }
        ensureUsernameAvailable(username.trim());
        user.setUsername(username.trim());
        sysUserMapper.updateById(user);
    }

    public SysRoleEntity requireRole(String roleCode) {
        SysRoleEntity role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException("系统缺少角色配置: " + roleCode);
        }
        return role;
    }

    @Transactional
    public void bindRole(Long userId, String roleCode) {
        SysRoleEntity role = requireRole(roleCode);
        Long count = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId)
                .eq(SysUserRoleEntity::getRoleId, role.getId()));
        if (count == 0) {
            sysUserRoleMapper.insert(new SysUserRoleEntity(userId, role.getId()));
        }
    }

    public boolean userHasRole(Long userId, String roleCode) {
        return listRoleCodes(userId).stream().anyMatch(item -> Objects.equals(item, roleCode));
    }

    public List<String> listRoleCodes(Long userId) {
        List<SysUserRoleEntity> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId));
        if (relations.isEmpty()) {
            return List.of();
        }

        Map<Long, SysRoleEntity> roleMap = sysRoleMapper.selectBatchIds(
                        relations.stream().map(SysUserRoleEntity::getRoleId).toList())
                .stream()
                .collect(Collectors.toMap(SysRoleEntity::getId, Function.identity()));

        return relations.stream()
                .map(item -> roleMap.get(item.getRoleId()))
                .filter(Objects::nonNull)
                .map(SysRoleEntity::getRoleCode)
                .toList();
    }

    public void ensureBaseRolesExist() {
        ensureRoleExists(RoleCodes.USER, "普通用户");
        ensureRoleExists(RoleCodes.WORKER, "家政服务人员");
        ensureRoleExists(RoleCodes.ADMIN, "平台管理员");
    }

    private void ensureRoleExists(String roleCode, String roleName) {
        if (sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)) > 0) {
            return;
        }
        sysRoleMapper.insert(new SysRoleEntity(roleCode, roleName));
    }
}

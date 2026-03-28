package com.housekeeping.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysPermissionEntity;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysRolePermissionEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysPermissionMapper;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysRolePermissionMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.PermissionCodes;
import com.housekeeping.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RolePermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public RolePermissionService(SysPermissionMapper sysPermissionMapper,
                                 SysRolePermissionMapper sysRolePermissionMapper,
                                 SysRoleMapper sysRoleMapper,
                                 SysUserRoleMapper sysUserRoleMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    public List<String> listPermissionCodes(Long userId, String roleCode) {
        SysRoleEntity role = requireRole(roleCode);
        Long relationCount = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId)
                .eq(SysUserRoleEntity::getRoleId, role.getId()));
        if (relationCount == null || relationCount == 0) {
            return List.of();
        }
        return listPermissionCodesByRoleId(role.getId());
    }

    public List<String> listPermissionCodesByRoleCode(String roleCode) {
        return listPermissionCodesByRoleId(requireRole(roleCode).getId());
    }

    public Map<String, List<String>> listPermissionCodesByRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Map.of();
        }
        List<SysRoleEntity> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .in(SysRoleEntity::getRoleCode, roleCodes));
        if (roles.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> permissionCodesByRoleId = listPermissionCodesByRoleIds(
                roles.stream().map(SysRoleEntity::getId).toList()
        );
        return roles.stream().collect(Collectors.toMap(
                SysRoleEntity::getRoleCode,
                role -> permissionCodesByRoleId.getOrDefault(role.getId(), List.of()),
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    @Transactional
    public void ensureBasePermissionsExist() {
        Map<String, SysPermissionEntity> existedPermissionMap = sysPermissionMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(SysPermissionEntity::getPermissionCode, Function.identity(), (left, right) -> left));

        PermissionCodes.definitions().forEach(definition -> {
            SysPermissionEntity existed = existedPermissionMap.get(definition.code());
            if (existed == null) {
                sysPermissionMapper.insert(new SysPermissionEntity(
                        definition.code(),
                        definition.name(),
                        definition.groupKey(),
                        definition.description()
                ));
            }
        });

        ensureDefaultRolePermissions();
    }

    @Transactional
    public void syncRolePermissions(String roleCode, List<String> permissionCodes) {
        SysRoleEntity role = requireRole(roleCode);
        List<String> normalizedCodes = normalizePermissionCodes(permissionCodes);
        if (normalizedCodes.isEmpty()) {
            throw new BusinessException("At least one permission must be selected.");
        }

        List<String> requiredPermissions = PermissionCodes.requiredPermissions(roleCode);
        if (!normalizedCodes.containsAll(requiredPermissions)) {
            throw new BusinessException("Required permissions for role " + roleCode + " cannot be removed.");
        }

        Map<String, SysPermissionEntity> permissionMap = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermissionEntity>()
                                .in(SysPermissionEntity::getPermissionCode, normalizedCodes))
                .stream()
                .collect(Collectors.toMap(SysPermissionEntity::getPermissionCode, Function.identity(), (left, right) -> left));
        if (permissionMap.size() != normalizedCodes.size()) {
            throw new BusinessException("Unknown permission code in request.");
        }

        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermissionEntity>()
                .eq(SysRolePermissionEntity::getRoleId, role.getId()));
        normalizedCodes.forEach(code -> sysRolePermissionMapper.insert(
                new SysRolePermissionEntity(role.getId(), permissionMap.get(code).getId())
        ));
    }

    public long countUsersForRole(Long roleId) {
        Long count = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getRoleId, roleId));
        return count == null ? 0L : count;
    }

    private void ensureDefaultRolePermissions() {
        List<SysRoleEntity> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .in(SysRoleEntity::getRoleCode, List.of("USER", "WORKER", "ADMIN")));
        if (roles.isEmpty()) {
            return;
        }

        Map<Long, List<String>> currentRolePermissions = listPermissionCodesByRoleIds(
                roles.stream().map(SysRoleEntity::getId).toList()
        );
        Map<String, SysPermissionEntity> permissionMap = sysPermissionMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(SysPermissionEntity::getPermissionCode, Function.identity(), (left, right) -> left));

        roles.forEach(role -> {
            if (!currentRolePermissions.getOrDefault(role.getId(), List.of()).isEmpty()) {
                return;
            }
            PermissionCodes.defaultPermissions(role.getRoleCode()).forEach(code -> {
                SysPermissionEntity permission = permissionMap.get(code);
                if (permission != null) {
                    sysRolePermissionMapper.insert(new SysRolePermissionEntity(role.getId(), permission.getId()));
                }
            });
        });
    }

    private List<String> listPermissionCodesByRoleId(Long roleId) {
        return listPermissionCodesByRoleIds(List.of(roleId)).getOrDefault(roleId, List.of());
    }

    private Map<Long, List<String>> listPermissionCodesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }

        List<SysRolePermissionEntity> relations = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermissionEntity>()
                        .in(SysRolePermissionEntity::getRoleId, roleIds)
        );
        if (relations.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> permissionCodeById = sysPermissionMapper.selectBatchIds(
                        relations.stream().map(SysRolePermissionEntity::getPermissionId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(SysPermissionEntity::getId, SysPermissionEntity::getPermissionCode));

        Map<Long, List<String>> result = new LinkedHashMap<>();
        relations.forEach(relation -> {
            String code = permissionCodeById.get(relation.getPermissionId());
            if (code != null) {
                result.computeIfAbsent(relation.getRoleId(), key -> new ArrayList<>()).add(code);
            }
        });
        result.replaceAll((key, value) -> value.stream().filter(Objects::nonNull).distinct().toList());
        return result;
    }

    private SysRoleEntity requireRole(String roleCode) {
        SysRoleEntity role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException("Role not configured: " + roleCode);
        }
        return role;
    }

    private List<String> normalizePermissionCodes(List<String> permissionCodes) {
        if (permissionCodes == null) {
            return List.of();
        }
        Set<String> normalized = permissionCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(normalized);
    }
}

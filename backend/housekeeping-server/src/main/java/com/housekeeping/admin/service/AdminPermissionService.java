package com.housekeeping.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.admin.dto.AdminPermissionCatalogDto;
import com.housekeeping.admin.dto.AdminPermissionGroupDto;
import com.housekeeping.admin.dto.AdminPermissionItemDto;
import com.housekeeping.admin.dto.AdminRolePermissionDto;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.service.RolePermissionService;
import com.housekeeping.auth.support.PermissionCodes;
import com.housekeeping.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminPermissionService {

    private final SysRoleMapper sysRoleMapper;
    private final RolePermissionService rolePermissionService;
    private final OperationLogService operationLogService;

    public AdminPermissionService(SysRoleMapper sysRoleMapper,
                                  RolePermissionService rolePermissionService,
                                  OperationLogService operationLogService) {
        this.sysRoleMapper = sysRoleMapper;
        this.rolePermissionService = rolePermissionService;
        this.operationLogService = operationLogService;
    }

    public AdminPermissionCatalogDto getCatalog() {
        List<SysRoleEntity> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .orderByAsc(SysRoleEntity::getId));
        Map<String, List<String>> permissionMap = rolePermissionService.listPermissionCodesByRoleCodes(
                roles.stream().map(SysRoleEntity::getRoleCode).toList()
        );

        List<AdminRolePermissionDto> roleDtos = roles.stream()
                .map(role -> new AdminRolePermissionDto(
                        role.getRoleCode(),
                        role.getRoleName(),
                        rolePermissionService.countUsersForRole(role.getId()),
                        permissionMap.getOrDefault(role.getRoleCode(), List.of()),
                        PermissionCodes.requiredPermissions(role.getRoleCode())
                ))
                .toList();

        Map<String, List<PermissionCodes.PermissionDefinition>> groupedDefinitions = PermissionCodes.definitions().stream()
                .collect(Collectors.groupingBy(
                        PermissionCodes.PermissionDefinition::groupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<AdminPermissionGroupDto> groupDtos = groupedDefinitions.values().stream()
                .map(definitions -> new AdminPermissionGroupDto(
                        definitions.get(0).groupKey(),
                        definitions.get(0).groupName(),
                        definitions.stream()
                                .map(definition -> new AdminPermissionItemDto(
                                        definition.code(),
                                        definition.name(),
                                        definition.description()
                                ))
                                .toList()
                ))
                .toList();

        return new AdminPermissionCatalogDto(groupDtos, roleDtos);
    }

    @Transactional
    public AdminRolePermissionDto updateRolePermissions(String roleCode, List<String> permissionCodes) {
        SysRoleEntity role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException("Role not found: " + roleCode);
        }

        rolePermissionService.syncRolePermissions(roleCode, permissionCodes);
        operationLogService.record(
                OperationLogActions.ROLE_PERMISSION_UPDATE,
                "ROLE",
                role.getId(),
                "Updated permissions for role " + roleCode
        );

        return getCatalog().roles().stream()
                .filter(item -> roleCode.equals(item.roleCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Role permission result not found."));
    }
}

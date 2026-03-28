package com.housekeeping.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.housekeeping.admin.dto.AdminUserDto;
import com.housekeeping.admin.dto.AdminUserSaveRequest;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.auth.util.PasswordUtil;
import com.housekeeping.common.PageResult;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.user.entity.UserProfileEntity;
import com.housekeeping.user.mapper.UserProfileMapper;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
@Transactional(readOnly = true)
public class AdminUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserProfileMapper userProfileMapper;
    private final WorkerMapper workerMapper;
    private final WorkerProfileService workerProfileService;
    private final OperationLogService operationLogService;

    public AdminUserService(SysUserMapper sysUserMapper,
                            SysRoleMapper sysRoleMapper,
                            SysUserRoleMapper sysUserRoleMapper,
                            UserProfileMapper userProfileMapper,
                            WorkerMapper workerMapper,
                            WorkerProfileService workerProfileService,
                            OperationLogService operationLogService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.userProfileMapper = userProfileMapper;
        this.workerMapper = workerMapper;
        this.workerProfileService = workerProfileService;
        this.operationLogService = operationLogService;
    }

    public List<AdminUserDto> listUsers(String roleCode, String realName, String phone, String status) {
        return buildUserDtos(
                sysUserMapper.selectList(buildUserWrapper(realName, phone, status)),
                roleCode
        );
    }

    public PageResult<AdminUserDto> pageUsers(long current,
                                              long size,
                                              String roleCode,
                                              String realName,
                                              String phone,
                                              String status) {
        LambdaQueryWrapper<SysUserEntity> wrapper = buildUserWrapper(realName, phone, status);
        if (StringUtils.hasText(roleCode)) {
            List<Long> matchedUserIds = findUserIdsByRoleCode(roleCode.trim().toUpperCase());
            if (matchedUserIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, current, size, 0);
            }
            wrapper.in(SysUserEntity::getId, matchedUserIds);
        }
        Page<SysUserEntity> page = sysUserMapper.selectPage(new Page<>(current, size), wrapper);
        List<AdminUserDto> dtos = buildUserDtos(page.getRecords(), null);
        if (dtos.isEmpty()) {
            return PageResult.from(page, List.of());
        }
        return PageResult.from(page, dtos);
    }

    public Map<String, Long> summarizeUsers(String roleCode,
                                            String realName,
                                            String phone,
                                            String status) {
        List<AdminUserDto> users = listUsers(roleCode, realName, phone, status);
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", (long) users.size());
        summary.put("active", users.stream().filter(item -> "ACTIVE".equals(item.status())).count());
        summary.put("workers", users.stream().filter(item -> item.roleCodes().contains(RoleCodes.WORKER)).count());
        summary.put("admins", users.stream().filter(item -> item.roleCodes().contains(RoleCodes.ADMIN)).count());
        return summary;
    }

    @Transactional
    public AdminUserDto createUser(AdminUserSaveRequest request) {
        String rawPassword = safeValue(request.password());
        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException("新建用户时必须填写密码");
        }

        SysUserEntity entity = new SysUserEntity(
                request.phone().trim(),
                PasswordUtil.sha256(rawPassword),
                request.realName().trim(),
                normalizeStatus(request.status())
        );
        try {
            sysUserMapper.insert(entity);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("该手机号已经注册");
        }

        saveRoleBindings(entity.getId(), request.roleCodes());
        ensureUserDependentProfiles(entity);
        operationLogService.record(
                OperationLogActions.USER_CREATE,
                "USER",
                entity.getId(),
                "创建用户 " + entity.getRealName() + "，角色：" + String.join(",", normalizeRoleCodes(request.roleCodes()))
        );
        return getUser(entity.getId());
    }

    @Transactional
    public AdminUserDto updateUser(Long id, AdminUserSaveRequest request) {
        SysUserEntity entity = requireUser(id);
        SessionUser currentUser = requireCurrentUser();
        List<String> normalizedRoles = normalizeRoleCodes(request.roleCodes());
        String nextStatus = normalizeStatus(request.status());

        if (Objects.equals(currentUser.userId(), id)) {
            if (!"ACTIVE".equals(nextStatus)) {
                throw new BusinessException("不能禁用或删除当前登录管理员");
            }
            if (!normalizedRoles.contains(RoleCodes.ADMIN)) {
                throw new BusinessException("当前登录管理员必须保留 ADMIN 角色");
            }
        }

        ensurePhoneAvailableForUpdate(id, request.phone().trim());
        entity.setRealName(request.realName().trim());
        entity.setPhone(request.phone().trim());
        entity.setStatus(nextStatus);
        if (StringUtils.hasText(request.password())) {
            entity.setPassword(PasswordUtil.sha256(request.password().trim()));
        }
        sysUserMapper.updateById(entity);

        saveRoleBindings(entity.getId(), normalizedRoles);
        ensureUserDependentProfiles(entity);
        workerProfileService.syncRealName(entity.getId(), entity.getRealName());
        operationLogService.record(
                OperationLogActions.USER_UPDATE,
                "USER",
                entity.getId(),
                "更新用户 " + entity.getRealName() + "，角色：" + String.join(",", normalizedRoles) + "，状态：" + nextStatus
        );
        return getUser(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        SessionUser currentUser = requireCurrentUser();
        if (Objects.equals(currentUser.userId(), id)) {
            throw new BusinessException("不能删除当前登录管理员");
        }

        SysUserEntity entity = requireUser(id);
        entity.setStatus("DELETED");
        sysUserMapper.updateById(entity);
        operationLogService.record(
                OperationLogActions.USER_DELETE,
                "USER",
                id,
                "将用户 " + entity.getRealName() + " 标记为 DELETED"
        );
    }

    public AdminUserDto getUser(Long id) {
        return buildUserDtos(List.of(requireUser(id)), null).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到对应用户"));
    }

    private Map<Long, List<String>> buildRoleCodeMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<SysUserRoleEntity> relations = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRoleEntity>()
                        .in(SysUserRoleEntity::getUserId, userIds)
        );
        if (relations.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> roleCodeMap = sysRoleMapper.selectBatchIds(
                        relations.stream().map(SysUserRoleEntity::getRoleId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleCode));

        Map<Long, List<String>> grouped = new LinkedHashMap<>();
        relations.forEach(item -> grouped
                .computeIfAbsent(item.getUserId(), key -> new ArrayList<>())
                .add(roleCodeMap.getOrDefault(item.getRoleId(), "")));
        return grouped;
    }

    private LambdaQueryWrapper<SysUserEntity> buildUserWrapper(String realName, String phone, String status) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(realName)) {
            wrapper.like(SysUserEntity::getRealName, realName.trim());
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(SysUserEntity::getPhone, phone.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUserEntity::getStatus, status.trim().toUpperCase());
        }
        return wrapper.orderByDesc(SysUserEntity::getId);
    }

    private List<AdminUserDto> buildUserDtos(List<SysUserEntity> users, String roleCodeFilter) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(SysUserEntity::getId).toList();
        Map<Long, List<String>> roleMap = buildRoleCodeMap(userIds);
        List<SysUserEntity> filteredUsers = users;
        if (StringUtils.hasText(roleCodeFilter)) {
            String normalizedRoleCode = roleCodeFilter.trim().toUpperCase();
            filteredUsers = users.stream()
                    .filter(user -> roleMap.getOrDefault(user.getId(), List.of()).contains(normalizedRoleCode))
                    .toList();
        }
        if (filteredUsers.isEmpty()) {
            return List.of();
        }

        List<Long> filteredUserIds = filteredUsers.stream().map(SysUserEntity::getId).toList();
        Map<Long, UserProfileEntity> profileMap = userProfileMapper.selectList(
                        new LambdaQueryWrapper<UserProfileEntity>()
                                .in(UserProfileEntity::getUserId, filteredUserIds))
                .stream()
                .collect(Collectors.toMap(UserProfileEntity::getUserId, Function.identity()));

        Set<Long> workerUserIds = workerMapper.selectList(
                        new LambdaQueryWrapper<WorkerEntity>()
                                .in(WorkerEntity::getUserId, filteredUserIds))
                .stream()
                .map(WorkerEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return filteredUsers.stream()
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getRealName(),
                        user.getPhone(),
                        user.getStatus(),
                        roleMap.getOrDefault(user.getId(), List.of()),
                        profileMap.get(user.getId()) == null ? "" : profileMap.get(user.getId()).getCity(),
                        workerUserIds.contains(user.getId())
                ))
                .toList();
    }

    private List<Long> findUserIdsByRoleCode(String roleCode) {
        SysRoleEntity role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRoleEntity>()
                        .eq(SysRoleEntity::getRoleCode, roleCode)
                        .last("limit 1")
        );
        if (role == null) {
            return List.of();
        }
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRoleEntity>()
                                .eq(SysUserRoleEntity::getRoleId, role.getId()))
                .stream()
                .map(SysUserRoleEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void saveRoleBindings(Long userId, List<String> roleCodes) {
        List<String> normalizedRoles = normalizeRoleCodes(roleCodes);
        if (normalizedRoles.isEmpty()) {
            throw new BusinessException("至少选择一个角色");
        }

        List<SysRoleEntity> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRoleEntity>().in(SysRoleEntity::getRoleCode, normalizedRoles)
        );
        if (roles.size() != normalizedRoles.size()) {
            throw new BusinessException("存在未配置的角色");
        }

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId));
        roles.forEach(role -> sysUserRoleMapper.insert(new SysUserRoleEntity(userId, role.getId())));
    }

    private void ensureUserDependentProfiles(SysUserEntity user) {
        UserProfileEntity profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfileEntity>()
                        .eq(UserProfileEntity::getUserId, user.getId())
                        .last("limit 1")
        );
        if (profile == null) {
            userProfileMapper.insert(new UserProfileEntity(user.getId(), "", "", "", ""));
        }

        List<String> roleCodes = buildRoleCodeMap(List.of(user.getId())).getOrDefault(user.getId(), List.of());
        if (roleCodes.contains(RoleCodes.WORKER)) {
            workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                    user.getId(),
                    user.getRealName(),
                    profile == null ? "" : safeValue(profile.getCity()),
                    99,
                    "待配置服务项目",
                    "待配置服务时段",
                    0,
                    "待补充资质信息",
                    "待补充服务区域",
                    "管理员创建的服务人员档案，待完善详细资料。",
                    "",
                    WorkerQualificationStatus.APPROVED,
                    "平台创建服务人员",
                    "待补充服务案例"
            ));
        }
    }

    private void ensurePhoneAvailableForUpdate(Long userId, String phone) {
        SysUserEntity existed = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getPhone, phone)
                        .last("limit 1")
        );
        if (existed != null && !Objects.equals(existed.getId(), userId)) {
            throw new BusinessException("该手机号已经注册");
        }
    }

    private SysUserEntity requireUser(Long userId) {
        SysUserEntity entity = sysUserMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException("未找到对应用户");
        }
        return entity;
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null) {
            return List.of();
        }
        Set<String> normalized = roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toUpperCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(normalized);
    }

    private String normalizeStatus(String status) {
        String normalized = StringUtils.hasText(status) ? status.trim().toUpperCase() : "ACTIVE";
        if (!List.of("ACTIVE", "DISABLED", "DELETED").contains(normalized)) {
            throw new BusinessException("不支持的用户状态");
        }
        return normalized;
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}

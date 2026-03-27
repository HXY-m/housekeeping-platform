package com.housekeeping.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.notification.dto.NotificationDto;
import com.housekeeping.notification.entity.UserNotificationEntity;
import com.housekeeping.notification.mapper.UserNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final int DEFAULT_LIMIT = 80;

    private final UserNotificationMapper userNotificationMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserMapper sysUserMapper;

    public NotificationService(UserNotificationMapper userNotificationMapper,
                               SysRoleMapper sysRoleMapper,
                               SysUserRoleMapper sysUserRoleMapper,
                               SysUserMapper sysUserMapper) {
        this.userNotificationMapper = userNotificationMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public List<NotificationDto> listCurrentNotifications() {
        SessionUser currentUser = requireCurrentUser();
        return userNotificationMapper.selectList(
                        new LambdaQueryWrapper<UserNotificationEntity>()
                                .eq(UserNotificationEntity::getRecipientUserId, currentUser.userId())
                                .eq(UserNotificationEntity::getRecipientRoleCode, currentUser.roleCode())
                                .orderByAsc(UserNotificationEntity::getReadFlag)
                                .orderByDesc(UserNotificationEntity::getId)
                                .last("limit " + DEFAULT_LIMIT)
                ).stream()
                .map(this::toDto)
                .toList();
    }

    public long countUnreadCurrentUser() {
        SessionUser currentUser = requireCurrentUser();
        Long count = userNotificationMapper.selectCount(
                new LambdaQueryWrapper<UserNotificationEntity>()
                        .eq(UserNotificationEntity::getRecipientUserId, currentUser.userId())
                        .eq(UserNotificationEntity::getRecipientRoleCode, currentUser.roleCode())
                        .eq(UserNotificationEntity::getReadFlag, false)
        );
        return count == null ? 0L : count;
    }

    @Transactional
    public NotificationDto markRead(Long id) {
        SessionUser currentUser = requireCurrentUser();
        UserNotificationEntity notification = requireCurrentNotification(id, currentUser);
        if (Boolean.TRUE.equals(notification.getReadFlag())) {
            return toDto(notification);
        }
        notification.setReadFlag(true);
        notification.setReadAt(LocalDateTime.now());
        userNotificationMapper.updateById(notification);
        return toDto(notification);
    }

    @Transactional
    public long markAllRead() {
        SessionUser currentUser = requireCurrentUser();
        List<UserNotificationEntity> notifications = userNotificationMapper.selectList(
                new LambdaQueryWrapper<UserNotificationEntity>()
                        .eq(UserNotificationEntity::getRecipientUserId, currentUser.userId())
                        .eq(UserNotificationEntity::getRecipientRoleCode, currentUser.roleCode())
                        .eq(UserNotificationEntity::getReadFlag, false)
        );
        if (notifications.isEmpty()) {
            return 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserNotificationEntity notification : notifications) {
            notification.setReadFlag(true);
            notification.setReadAt(now);
            userNotificationMapper.updateById(notification);
        }
        return notifications.size();
    }

    @Transactional
    public void notifyUser(Long recipientUserId,
                           String recipientRoleCode,
                           String type,
                           String title,
                           String content,
                           String relatedType,
                           Long relatedId,
                           String actionPath) {
        if (recipientUserId == null || !StringUtils.hasText(recipientRoleCode) || !StringUtils.hasText(title)) {
            return;
        }
        SysUserEntity user = sysUserMapper.selectById(recipientUserId);
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return;
        }
        userNotificationMapper.insert(new UserNotificationEntity(
                recipientUserId,
                recipientRoleCode.trim().toUpperCase(),
                safeValue(type),
                title.trim(),
                safeValue(content),
                safeValue(relatedType),
                relatedId,
                safeValue(actionPath),
                false,
                LocalDateTime.now(),
                null
        ));
    }

    @Transactional
    public void notifyAdmins(String type,
                             String title,
                             String content,
                             String relatedType,
                             Long relatedId,
                             String actionPath) {
        SysRoleEntity adminRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRoleEntity>()
                        .eq(SysRoleEntity::getRoleCode, RoleCodes.ADMIN)
                        .last("limit 1")
        );
        if (adminRole == null) {
            return;
        }

        List<SysUserRoleEntity> relations = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRoleEntity>()
                        .eq(SysUserRoleEntity::getRoleId, adminRole.getId())
        );
        if (relations.isEmpty()) {
            return;
        }

        Map<Long, SysUserEntity> userMap = sysUserMapper.selectBatchIds(
                        relations.stream().map(SysUserRoleEntity::getUserId).distinct().toList()
                ).stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .collect(Collectors.toMap(SysUserEntity::getId, Function.identity()));

        for (SysUserRoleEntity relation : relations) {
            if (userMap.containsKey(relation.getUserId())) {
                notifyUser(relation.getUserId(), RoleCodes.ADMIN, type, title, content, relatedType, relatedId, actionPath);
            }
        }
    }

    private UserNotificationEntity requireCurrentNotification(Long id, SessionUser currentUser) {
        UserNotificationEntity notification = userNotificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("未找到对应的通知");
        }
        if (!Objects.equals(notification.getRecipientUserId(), currentUser.userId())
                || !Objects.equals(notification.getRecipientRoleCode(), currentUser.roleCode())) {
            throw new BusinessException("只能处理自己的通知");
        }
        return notification;
    }

    private NotificationDto toDto(UserNotificationEntity entity) {
        return new NotificationDto(
                entity.getId(),
                safeValue(entity.getType()),
                safeValue(entity.getTitle()),
                safeValue(entity.getContent()),
                safeValue(entity.getRelatedType()),
                entity.getRelatedId(),
                safeValue(entity.getActionPath()),
                Boolean.TRUE.equals(entity.getReadFlag()),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}

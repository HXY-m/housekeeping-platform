package com.housekeeping.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.user.dto.UserAddressDto;
import com.housekeeping.user.dto.UserAddressSaveRequest;
import com.housekeeping.user.dto.UserProfileDto;
import com.housekeeping.user.dto.UserProfileUpdateRequest;
import com.housekeeping.user.entity.UserAddressEntity;
import com.housekeeping.user.entity.UserProfileEntity;
import com.housekeeping.user.mapper.UserAddressMapper;
import com.housekeeping.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserProfileService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAddressMapper userAddressMapper;

    public UserProfileService(SysUserMapper sysUserMapper,
                              UserProfileMapper userProfileMapper,
                              UserAddressMapper userAddressMapper) {
        this.sysUserMapper = sysUserMapper;
        this.userProfileMapper = userProfileMapper;
        this.userAddressMapper = userAddressMapper;
    }

    public UserProfileDto currentProfile() {
        SessionUser currentUser = requireCurrentUser();
        SysUserEntity user = requireUser(currentUser.userId());
        UserProfileEntity profile = findProfile(user.getId());
        return toProfileDto(user, profile);
    }

    public List<UserAddressDto> currentAddresses() {
        SessionUser currentUser = requireCurrentUser();
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddressEntity>()
                        .eq(UserAddressEntity::getUserId, currentUser.userId())
                        .orderByDesc(UserAddressEntity::getDefaultAddress)
                        .orderByDesc(UserAddressEntity::getUpdatedAt)
                        .orderByDesc(UserAddressEntity::getId))
                .stream()
                .map(this::toAddressDto)
                .toList();
    }

    @Transactional
    public UserProfileDto updateProfile(UserProfileUpdateRequest request) {
        SessionUser currentUser = requireCurrentUser();
        SysUserEntity user = requireUser(currentUser.userId());
        user.setRealName(request.realName().trim());
        sysUserMapper.updateById(user);

        UserProfileEntity profile = findProfile(user.getId());
        if (profile == null) {
            profile = new UserProfileEntity(user.getId(), "", "", "", "");
            userProfileMapper.insert(profile);
        }

        profile.setGender(trimOrEmpty(request.gender()));
        profile.setCity(trimOrEmpty(request.city()));
        profile.setBio(trimOrEmpty(request.bio()));
        profile.setAvatarUrl(trimOrEmpty(request.avatarUrl()));
        userProfileMapper.updateById(profile);

        return toProfileDto(user, profile);
    }

    @Transactional
    public UserAddressDto createAddress(UserAddressSaveRequest request) {
        SessionUser currentUser = requireCurrentUser();
        boolean shouldDefault = shouldMarkDefault(currentUser.userId(), request.defaultAddress());
        if (shouldDefault) {
            clearDefaultAddress(currentUser.userId());
        }

        LocalDateTime now = LocalDateTime.now();
        UserAddressEntity entity = new UserAddressEntity(
                currentUser.userId(),
                request.contactName().trim(),
                request.contactPhone().trim(),
                request.city().trim(),
                request.detailAddress().trim(),
                trimOrEmpty(request.addressTag()),
                shouldDefault,
                request.latitude(),
                request.longitude(),
                now,
                now
        );
        userAddressMapper.insert(entity);
        return toAddressDto(entity);
    }

    @Transactional
    public UserAddressDto updateAddress(Long id, UserAddressSaveRequest request) {
        SessionUser currentUser = requireCurrentUser();
        UserAddressEntity entity = requireOwnedAddress(id, currentUser.userId());

        long addressCount = countAddresses(currentUser.userId());
        boolean shouldDefault = addressCount == 1
                || Boolean.TRUE.equals(request.defaultAddress())
                || Boolean.TRUE.equals(entity.getDefaultAddress());

        if (Boolean.TRUE.equals(request.defaultAddress())) {
            clearDefaultAddress(currentUser.userId());
        }

        entity.setContactName(request.contactName().trim());
        entity.setContactPhone(request.contactPhone().trim());
        entity.setCity(request.city().trim());
        entity.setDetailAddress(request.detailAddress().trim());
        entity.setAddressTag(trimOrEmpty(request.addressTag()));
        entity.setDefaultAddress(shouldDefault);
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setUpdatedAt(LocalDateTime.now());
        userAddressMapper.updateById(entity);
        return toAddressDto(entity);
    }

    @Transactional
    public void deleteAddress(Long id) {
        SessionUser currentUser = requireCurrentUser();
        UserAddressEntity entity = requireOwnedAddress(id, currentUser.userId());
        userAddressMapper.deleteById(entity.getId());

        if (Boolean.TRUE.equals(entity.getDefaultAddress())) {
            UserAddressEntity nextAddress = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddressEntity>()
                    .eq(UserAddressEntity::getUserId, currentUser.userId())
                    .orderByDesc(UserAddressEntity::getUpdatedAt)
                    .last("limit 1"));
            if (nextAddress != null) {
                nextAddress.setDefaultAddress(true);
                nextAddress.setUpdatedAt(LocalDateTime.now());
                userAddressMapper.updateById(nextAddress);
            }
        }
    }

    @Transactional
    public void ensureProfileExists(Long userId) {
        if (findProfile(userId) != null) {
            return;
        }
        userProfileMapper.insert(new UserProfileEntity(userId, "", "", "", ""));
    }

    @Transactional
    public void ensureSampleAddress(Long userId, String contactName, String contactPhone,
                                    String city, String detailAddress, String addressTag) {
        Long count = userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId));
        if (count != null && count > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        userAddressMapper.insert(new UserAddressEntity(
                userId,
                contactName,
                contactPhone,
                city,
                detailAddress,
                addressTag,
                true,
                null,
                null,
                now,
                now
        ));
    }

    private SysUserEntity requireUser(Long userId) {
        SysUserEntity user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        return user;
    }

    private UserProfileEntity findProfile(Long userId) {
        return userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfileEntity>()
                .eq(UserProfileEntity::getUserId, userId)
                .last("limit 1"));
    }

    private UserAddressEntity requireOwnedAddress(Long id, Long userId) {
        UserAddressEntity entity = userAddressMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("地址不存在");
        }
        if (!Objects.equals(entity.getUserId(), userId)) {
            throw new BusinessException("只能操作自己的地址");
        }
        return entity;
    }

    private long countAddresses(Long userId) {
        Long count = userAddressMapper.selectCount(new LambdaQueryWrapper<UserAddressEntity>()
                .eq(UserAddressEntity::getUserId, userId));
        return count == null ? 0L : count;
    }

    private boolean shouldMarkDefault(Long userId, Boolean requestedDefault) {
        long addressCount = countAddresses(userId);
        return addressCount == 0 || Boolean.TRUE.equals(requestedDefault);
    }

    private void clearDefaultAddress(Long userId) {
        List<UserAddressEntity> currentDefaultAddresses = userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddressEntity>()
                        .eq(UserAddressEntity::getUserId, userId)
                        .eq(UserAddressEntity::getDefaultAddress, true)
        );
        LocalDateTime now = LocalDateTime.now();
        for (UserAddressEntity item : currentDefaultAddresses) {
            item.setDefaultAddress(false);
            item.setUpdatedAt(now);
            userAddressMapper.updateById(item);
        }
    }

    private UserProfileDto toProfileDto(SysUserEntity user, UserProfileEntity profile) {
        return new UserProfileDto(
                user.getId(),
                user.getRealName(),
                user.getPhone(),
                profile == null ? "" : nullToEmpty(profile.getGender()),
                profile == null ? "" : nullToEmpty(profile.getCity()),
                profile == null ? "" : nullToEmpty(profile.getBio()),
                profile == null ? "" : nullToEmpty(profile.getAvatarUrl())
        );
    }

    private UserAddressDto toAddressDto(UserAddressEntity entity) {
        return new UserAddressDto(
                entity.getId(),
                entity.getContactName(),
                entity.getContactPhone(),
                entity.getCity(),
                entity.getDetailAddress(),
                entity.getAddressTag(),
                Boolean.TRUE.equals(entity.getDefaultAddress()),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }

    private String trimOrEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }
}

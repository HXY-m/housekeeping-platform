package com.housekeeping.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.dto.WorkerProfileDto;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class WorkerProfileService {

    private final WorkerMapper workerMapper;
    private final SysUserMapper sysUserMapper;

    public WorkerProfileService(WorkerMapper workerMapper,
                                SysUserMapper sysUserMapper) {
        this.workerMapper = workerMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public WorkerProfileDto currentProfile() {
        SessionUser currentUser = requireCurrentUser();
        WorkerEntity worker = requireWorkerByUserId(currentUser.userId());
        SysUserEntity user = requireUser(currentUser.userId());
        return toDto(worker, user);
    }

    public WorkerEntity requireApprovedWorker(Long workerId) {
        WorkerEntity worker = requireWorker(workerId);
        if (!WorkerQualificationStatus.isPublicVisible(worker.getQualificationStatus())) {
            throw new BusinessException("该服务人员尚未通过资质审核");
        }
        return worker;
    }

    public WorkerEntity requireWorker(Long workerId) {
        WorkerEntity worker = workerMapper.selectById(workerId);
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return worker;
    }

    public WorkerEntity requireWorkerByUserId(Long userId) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            throw new BusinessException("当前账号还没有绑定服务人员档案");
        }
        return worker;
    }

    @Transactional
    public WorkerEntity upsertProfile(WorkerProfileUpsertCommand command) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, command.userId())
                        .last("limit 1")
        );

        if (worker == null) {
            worker = new WorkerEntity();
            worker.setUserId(command.userId());
            worker.setRating(5.0);
            worker.setCompletedOrders(0);
            worker.setServiceCases(safeValue(command.serviceCases(), "等待补充服务案例"));
        }

        worker.setName(command.realName().trim());
        worker.setRoleLabel(safeValue(command.roleLabel(), "平台认证家政服务人员"));
        worker.setHourlyPrice(command.hourlyPrice() == null ? defaultPrice(worker.getHourlyPrice()) : command.hourlyPrice());
        worker.setCity(safeValue(command.city(), safeValue(worker.getCity(), "")));
        worker.setIntro(safeValue(command.intro(), safeValue(worker.getIntro(), "")));
        worker.setTags(safeValue(command.serviceTypes(), safeValue(worker.getTags(), "")));
        worker.setNextAvailable(safeValue(command.availableSchedule(), safeValue(worker.getNextAvailable(), "")));
        worker.setYearsOfExperience(command.yearsOfExperience() == null
                ? defaultYears(worker.getYearsOfExperience())
                : command.yearsOfExperience());
        worker.setCertificates(safeValue(command.certificates(), safeValue(worker.getCertificates(), "")));
        worker.setServiceAreas(safeValue(command.serviceAreas(), safeValue(worker.getServiceAreas(), "")));
        worker.setServiceCases(safeValue(command.serviceCases(), safeValue(worker.getServiceCases(), "等待补充服务案例")));
        worker.setAvatarUrl(safeValue(command.avatarUrl(), safeValue(worker.getAvatarUrl(), "")));
        worker.setQualificationStatus(safeValue(
                command.qualificationStatus(),
                safeValue(worker.getQualificationStatus(), WorkerQualificationStatus.UNSUBMITTED)
        ));

        if (worker.getId() == null) {
            workerMapper.insert(worker);
        } else {
            workerMapper.updateById(worker);
        }
        return worker;
    }

    @Transactional
    public void updateQualificationStatus(Long userId, String qualificationStatus) {
        WorkerEntity worker = requireWorkerByUserId(userId);
        worker.setQualificationStatus(qualificationStatus);
        workerMapper.updateById(worker);
    }

    @Transactional
    public void syncRealName(Long userId, String realName) {
        WorkerEntity worker = workerMapper.selectOne(
                new LambdaQueryWrapper<WorkerEntity>()
                        .eq(WorkerEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (worker == null) {
            return;
        }
        worker.setName(realName.trim());
        workerMapper.updateById(worker);
    }

    private WorkerProfileDto toDto(WorkerEntity worker, SysUserEntity user) {
        return new WorkerProfileDto(
                worker.getId(),
                worker.getUserId(),
                worker.getName(),
                user.getPhone(),
                safeValue(worker.getCity(), ""),
                defaultPrice(worker.getHourlyPrice()),
                safeValue(worker.getTags(), ""),
                safeValue(worker.getNextAvailable(), ""),
                defaultYears(worker.getYearsOfExperience()),
                safeValue(worker.getCertificates(), ""),
                safeValue(worker.getServiceAreas(), ""),
                safeValue(worker.getIntro(), ""),
                safeValue(worker.getAvatarUrl(), ""),
                safeValue(worker.getQualificationStatus(), WorkerQualificationStatus.UNSUBMITTED)
        );
    }

    private SysUserEntity requireUser(Long userId) {
        SysUserEntity user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        return user;
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    private int defaultPrice(Integer hourlyPrice) {
        return hourlyPrice == null || hourlyPrice <= 0 ? 99 : hourlyPrice;
    }

    private int defaultYears(Integer yearsOfExperience) {
        return yearsOfExperience == null ? 0 : yearsOfExperience;
    }

    private String safeValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}

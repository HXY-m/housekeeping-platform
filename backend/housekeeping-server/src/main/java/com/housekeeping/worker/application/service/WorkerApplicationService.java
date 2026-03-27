package com.housekeeping.worker.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.application.dto.WorkerApplicationDto;
import com.housekeeping.worker.application.dto.WorkerApplicationReviewRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationSubmitRequest;
import com.housekeeping.worker.application.entity.WorkerApplicationEntity;
import com.housekeeping.worker.application.mapper.WorkerApplicationMapper;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkerApplicationService {

    private final WorkerApplicationMapper workerApplicationMapper;
    private final SysUserMapper sysUserMapper;
    private final WorkerProfileService workerProfileService;
    private final OperationLogService operationLogService;

    public WorkerApplicationService(WorkerApplicationMapper workerApplicationMapper,
                                    SysUserMapper sysUserMapper,
                                    WorkerProfileService workerProfileService,
                                    OperationLogService operationLogService) {
        this.workerApplicationMapper = workerApplicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.workerProfileService = workerProfileService;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public WorkerApplicationDto submit(WorkerApplicationSubmitRequest request) {
        SessionUser currentUser = requireCurrentUser();

        WorkerApplicationEntity latest = workerApplicationMapper.selectOne(
                new LambdaQueryWrapper<WorkerApplicationEntity>()
                        .eq(WorkerApplicationEntity::getUserId, currentUser.userId())
                        .orderByDesc(WorkerApplicationEntity::getId)
                        .last("limit 1")
        );

        if (latest != null && "PENDING".equalsIgnoreCase(latest.getStatus())) {
            throw new BusinessException("你已有待审核的资质申请，请勿重复提交");
        }

        WorkerApplicationEntity entity = new WorkerApplicationEntity();
        entity.setUserId(currentUser.userId());
        entity.setRealName(request.realName());
        entity.setPhone(request.phone());
        entity.setServiceTypes(request.serviceTypes());
        entity.setYearsOfExperience(request.yearsOfExperience());
        entity.setCertificates(request.certificates());
        entity.setServiceAreas(request.serviceAreas());
        entity.setAvailableSchedule(request.availableSchedule());
        entity.setIntro(request.intro());
        entity.setStatus("PENDING");
        entity.setAdminRemark("");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        workerApplicationMapper.insert(entity);
        workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                currentUser.userId(),
                request.realName(),
                null,
                null,
                request.serviceTypes(),
                request.availableSchedule(),
                request.yearsOfExperience(),
                request.certificates(),
                request.serviceAreas(),
                request.intro(),
                WorkerQualificationStatus.PENDING,
                "待资质审核服务人员",
                null
        ));
        operationLogService.record(
                OperationLogActions.WORKER_APPLICATION_SUBMIT,
                "WORKER_APPLICATION",
                entity.getId(),
                "提交服务人员资质申请，姓名=" + entity.getRealName()
        );
        return toDto(entity);
    }

    public List<WorkerApplicationDto> myApplications() {
        SessionUser currentUser = requireCurrentUser();
        return workerApplicationMapper.selectList(
                        new LambdaQueryWrapper<WorkerApplicationEntity>()
                                .eq(WorkerApplicationEntity::getUserId, currentUser.userId())
                                .orderByDesc(WorkerApplicationEntity::getId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<WorkerApplicationDto> listAll() {
        return workerApplicationMapper.selectList(
                        new LambdaQueryWrapper<WorkerApplicationEntity>()
                                .orderByAsc(WorkerApplicationEntity::getStatus)
                                .orderByDesc(WorkerApplicationEntity::getId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public WorkerApplicationDto review(Long id, WorkerApplicationReviewRequest request) {
        WorkerApplicationEntity entity = workerApplicationMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("未找到对应的入驻申请");
        }
        if (!"PENDING".equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("该申请已经审核完成，不能重复处理");
        }

        String action = request.action().trim().toUpperCase();
        if (!List.of("APPROVE", "REJECT").contains(action)) {
            throw new BusinessException("审核动作只支持 APPROVE 或 REJECT");
        }

        if ("APPROVE".equals(action)) {
            entity.setStatus("APPROVED");
            entity.setAdminRemark(blankToDefault(request.adminRemark(), "资质审核通过"));
            workerProfileService.upsertProfile(new WorkerProfileUpsertCommand(
                    entity.getUserId(),
                    entity.getRealName(),
                    null,
                    null,
                    entity.getServiceTypes(),
                    entity.getAvailableSchedule(),
                    entity.getYearsOfExperience(),
                    entity.getCertificates(),
                    entity.getServiceAreas(),
                    entity.getIntro(),
                    WorkerQualificationStatus.APPROVED,
                    "平台认证服务人员",
                    null
            ));
            workerProfileService.updateQualificationStatus(entity.getUserId(), WorkerQualificationStatus.APPROVED);
        } else {
            entity.setStatus("REJECTED");
            entity.setAdminRemark(blankToDefault(request.adminRemark(), "资质审核未通过"));
            workerProfileService.updateQualificationStatus(entity.getUserId(), WorkerQualificationStatus.REJECTED);
        }

        entity.setUpdatedAt(LocalDateTime.now());
        workerApplicationMapper.updateById(entity);
        operationLogService.record(
                OperationLogActions.WORKER_APPLICATION_REVIEW,
                "WORKER_APPLICATION",
                entity.getId(),
                "审核服务人员资质申请，结果=" + entity.getStatus()
        );
        return toDto(entity);
    }

    private WorkerApplicationDto toDto(WorkerApplicationEntity entity) {
        return new WorkerApplicationDto(
                entity.getId(),
                entity.getUserId(),
                entity.getRealName(),
                entity.getPhone(),
                entity.getServiceTypes(),
                entity.getYearsOfExperience(),
                entity.getCertificates(),
                entity.getServiceAreas(),
                entity.getAvailableSchedule(),
                entity.getIntro(),
                entity.getStatus(),
                entity.getAdminRemark(),
                entity.getCreatedAt()
        );
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        SysUserEntity user = sysUserMapper.selectById(currentUser.userId());
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        return currentUser;
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}

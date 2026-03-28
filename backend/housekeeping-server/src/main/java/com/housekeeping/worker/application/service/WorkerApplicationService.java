package com.housekeeping.worker.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.RoleCodes;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.common.PageResult;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.notification.NotificationType;
import com.housekeeping.notification.service.NotificationService;
import com.housekeeping.worker.WorkerQualificationStatus;
import com.housekeeping.worker.application.dto.WorkerApplicationAttachmentDto;
import com.housekeeping.worker.application.dto.WorkerApplicationAttachmentRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationDto;
import com.housekeeping.worker.application.dto.WorkerApplicationReviewRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationSubmitRequest;
import com.housekeeping.worker.application.entity.WorkerApplicationAttachmentEntity;
import com.housekeeping.worker.application.entity.WorkerApplicationEntity;
import com.housekeeping.worker.application.mapper.WorkerApplicationAttachmentMapper;
import com.housekeeping.worker.application.mapper.WorkerApplicationMapper;
import com.housekeeping.worker.dto.WorkerProfileUpsertCommand;
import com.housekeeping.worker.service.WorkerProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkerApplicationService {

    private final WorkerApplicationMapper workerApplicationMapper;
    private final WorkerApplicationAttachmentMapper workerApplicationAttachmentMapper;
    private final SysUserMapper sysUserMapper;
    private final WorkerProfileService workerProfileService;
    private final OperationLogService operationLogService;
    private final NotificationService notificationService;

    public WorkerApplicationService(WorkerApplicationMapper workerApplicationMapper,
                                    WorkerApplicationAttachmentMapper workerApplicationAttachmentMapper,
                                    SysUserMapper sysUserMapper,
                                    WorkerProfileService workerProfileService,
                                    OperationLogService operationLogService,
                                    NotificationService notificationService) {
        this.workerApplicationMapper = workerApplicationMapper;
        this.workerApplicationAttachmentMapper = workerApplicationAttachmentMapper;
        this.sysUserMapper = sysUserMapper;
        this.workerProfileService = workerProfileService;
        this.operationLogService = operationLogService;
        this.notificationService = notificationService;
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

        if (request.attachments() == null || request.attachments().isEmpty()) {
            throw new BusinessException("请至少上传 1 份资质附件");
        }

        LocalDateTime now = LocalDateTime.now();
        WorkerApplicationEntity entity = new WorkerApplicationEntity();
        entity.setUserId(currentUser.userId());
        entity.setRealName(request.realName().trim());
        entity.setPhone(request.phone().trim());
        entity.setServiceTypes(request.serviceTypes().trim());
        entity.setYearsOfExperience(request.yearsOfExperience());
        entity.setCertificates(request.certificates().trim());
        entity.setServiceAreas(request.serviceAreas().trim());
        entity.setAvailableSchedule(request.availableSchedule().trim());
        entity.setIntro(request.intro().trim());
        entity.setStatus("PENDING");
        entity.setAdminRemark("");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        workerApplicationMapper.insert(entity);

        saveAttachments(entity.getId(), request.attachments(), now);

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
        notificationService.notifyAdmins(
                NotificationType.WORKER_APPLICATION,
                "有新的服务人员资质申请待审核",
                entity.getRealName() + " 提交了新的资质申请，请及时审核。",
                "WORKER_APPLICATION",
                entity.getId(),
                "/admin/applications"
        );
        return toDto(entity, toAttachmentDtos(request.attachments()));
    }

    public List<WorkerApplicationDto> myApplications() {
        SessionUser currentUser = requireCurrentUser();
        return toDtos(workerApplicationMapper.selectList(
                new LambdaQueryWrapper<WorkerApplicationEntity>()
                        .eq(WorkerApplicationEntity::getUserId, currentUser.userId())
                        .orderByDesc(WorkerApplicationEntity::getId)
        ));
    }

    public List<WorkerApplicationDto> listAll() {
        return toDtos(workerApplicationMapper.selectList(
                new LambdaQueryWrapper<WorkerApplicationEntity>()
                        .orderByAsc(WorkerApplicationEntity::getStatus)
                        .orderByDesc(WorkerApplicationEntity::getId)
        ));
    }

    public PageResult<WorkerApplicationDto> pageAll(long current, long size, String status, String keyword) {
        LambdaQueryWrapper<WorkerApplicationEntity> wrapper = new LambdaQueryWrapper<WorkerApplicationEntity>()
                .eq(hasText(status), WorkerApplicationEntity::getStatus, status)
                .orderByAsc(WorkerApplicationEntity::getStatus)
                .orderByDesc(WorkerApplicationEntity::getId);
        if (hasText(keyword)) {
            wrapper.and(query -> query
                    .like(WorkerApplicationEntity::getRealName, keyword)
                    .or()
                    .like(WorkerApplicationEntity::getPhone, keyword)
                    .or()
                    .like(WorkerApplicationEntity::getServiceTypes, keyword)
                    .or()
                    .like(WorkerApplicationEntity::getServiceAreas, keyword));
        }
        Page<WorkerApplicationEntity> page = workerApplicationMapper.selectPage(
                new Page<>(current, size),
                wrapper
        );
        return PageResult.from(page, toDtos(page.getRecords()));
    }

    @Transactional
    public WorkerApplicationDto review(Long id, WorkerApplicationReviewRequest request) {
        WorkerApplicationEntity entity = workerApplicationMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("未找到对应的资质申请");
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
        notificationService.notifyUser(
                entity.getUserId(),
                RoleCodes.WORKER,
                NotificationType.WORKER_APPLICATION,
                "你的资质申请已有审核结果",
                "资质申请 #" + entity.getId() + " 已更新为 " + entity.getStatus() + "。",
                "WORKER_APPLICATION",
                entity.getId(),
                "/worker/qualification"
        );
        Map<Long, List<WorkerApplicationAttachmentDto>> attachmentMap = buildAttachmentMap(List.of(entity.getId()));
        return toDto(entity, attachmentMap.getOrDefault(entity.getId(), List.of()));
    }

    private List<WorkerApplicationDto> toDtos(List<WorkerApplicationEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        Map<Long, List<WorkerApplicationAttachmentDto>> attachmentMap = buildAttachmentMap(
                entities.stream().map(WorkerApplicationEntity::getId).toList()
        );
        return entities.stream()
                .map(entity -> toDto(entity, attachmentMap.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private WorkerApplicationDto toDto(WorkerApplicationEntity entity, List<WorkerApplicationAttachmentDto> attachments) {
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
                entity.getCreatedAt(),
                attachments
        );
    }

    private void saveAttachments(Long applicationId,
                                 List<WorkerApplicationAttachmentRequest> attachments,
                                 LocalDateTime createdAt) {
        for (WorkerApplicationAttachmentRequest attachment : attachments) {
            workerApplicationAttachmentMapper.insert(new WorkerApplicationAttachmentEntity(
                    applicationId,
                    attachment.name().trim(),
                    attachment.url().trim(),
                    attachment.size(),
                    createdAt
            ));
        }
    }

    private Map<Long, List<WorkerApplicationAttachmentDto>> buildAttachmentMap(List<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<WorkerApplicationAttachmentDto>> grouped = new LinkedHashMap<>();
        workerApplicationAttachmentMapper.selectList(
                new LambdaQueryWrapper<WorkerApplicationAttachmentEntity>()
                        .in(WorkerApplicationAttachmentEntity::getApplicationId, applicationIds)
                        .orderByAsc(WorkerApplicationAttachmentEntity::getId)
        ).forEach(item -> grouped.computeIfAbsent(item.getApplicationId(), key -> new ArrayList<>())
                .add(new WorkerApplicationAttachmentDto(
                        item.getId(),
                        item.getFileName(),
                        item.getFileUrl(),
                        item.getFileSize() == null ? 0L : item.getFileSize()
                )));
        return grouped;
    }

    private List<WorkerApplicationAttachmentDto> toAttachmentDtos(List<WorkerApplicationAttachmentRequest> attachments) {
        return attachments.stream()
                .map(item -> new WorkerApplicationAttachmentDto(
                        null,
                        item.name().trim(),
                        item.url().trim(),
                        item.size()
                ))
                .toList();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

package com.housekeeping.worker.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.entity.SysRoleEntity;
import com.housekeeping.auth.entity.SysUserEntity;
import com.housekeeping.auth.entity.SysUserRoleEntity;
import com.housekeeping.auth.mapper.SysRoleMapper;
import com.housekeeping.auth.mapper.SysUserMapper;
import com.housekeeping.auth.mapper.SysUserRoleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.worker.application.dto.WorkerApplicationDto;
import com.housekeeping.worker.application.dto.WorkerApplicationReviewRequest;
import com.housekeeping.worker.application.dto.WorkerApplicationSubmitRequest;
import com.housekeeping.worker.application.entity.WorkerApplicationEntity;
import com.housekeeping.worker.application.mapper.WorkerApplicationMapper;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkerApplicationService {

    private final WorkerApplicationMapper workerApplicationMapper;
    private final WorkerMapper workerMapper;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public WorkerApplicationService(WorkerApplicationMapper workerApplicationMapper,
                                    WorkerMapper workerMapper,
                                    SysUserMapper sysUserMapper,
                                    SysRoleMapper sysRoleMapper,
                                    SysUserRoleMapper sysUserRoleMapper) {
        this.workerApplicationMapper = workerApplicationMapper;
        this.workerMapper = workerMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
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
            throw new BusinessException("你已有待审核的服务人员入驻申请");
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
            entity.setAdminRemark(blankToDefault(request.adminRemark(), "审核通过"));
            grantWorkerRole(entity);
            createWorkerProfileIfAbsent(entity);
        } else {
            entity.setStatus("REJECTED");
            entity.setAdminRemark(blankToDefault(request.adminRemark(), "审核未通过"));
        }

        entity.setUpdatedAt(LocalDateTime.now());
        workerApplicationMapper.updateById(entity);
        return toDto(entity);
    }

    private void grantWorkerRole(WorkerApplicationEntity application) {
        SysRoleEntity workerRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRoleEntity>()
                        .eq(SysRoleEntity::getRoleCode, "WORKER")
                        .last("limit 1")
        );
        if (workerRole == null) {
            throw new BusinessException("系统缺少 WORKER 角色配置");
        }

        Long relationCount = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRoleEntity>()
                        .eq(SysUserRoleEntity::getUserId, application.getUserId())
                        .eq(SysUserRoleEntity::getRoleId, workerRole.getId())
        );
        if (relationCount == 0) {
            sysUserRoleMapper.insert(new SysUserRoleEntity(application.getUserId(), workerRole.getId()));
        }
    }

    private void createWorkerProfileIfAbsent(WorkerApplicationEntity application) {
        Long count = workerMapper.selectCount(
                new LambdaQueryWrapper<WorkerEntity>().eq(WorkerEntity::getUserId, application.getUserId())
        );
        if (count > 0) {
            return;
        }

        WorkerEntity worker = new WorkerEntity(
                application.getUserId(),
                application.getRealName(),
                "待上架服务人员",
                5.0,
                0,
                88,
                "上海",
                application.getIntro(),
                application.getServiceTypes(),
                application.getAvailableSchedule(),
                application.getYearsOfExperience(),
                application.getCertificates(),
                application.getServiceAreas(),
                "审核通过后创建的服务人员档案"
        );
        workerMapper.insert(worker);
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

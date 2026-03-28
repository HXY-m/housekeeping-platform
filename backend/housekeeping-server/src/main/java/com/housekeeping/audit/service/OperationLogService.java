package com.housekeeping.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.housekeeping.audit.dto.OperationLogDto;
import com.housekeeping.audit.entity.OperationLogEntity;
import com.housekeeping.audit.mapper.OperationLogMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional
    public void record(String actionType, String targetType, Long targetId, String content) {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null || !StringUtils.hasText(actionType)) {
            return;
        }

        operationLogMapper.insert(new OperationLogEntity(
                currentUser.userId(),
                safeValue(currentUser.realName()),
                safeValue(currentUser.roleCode()),
                actionType,
                safeValue(targetType),
                targetId,
                safeValue(content),
                extractIpAddress(),
                LocalDateTime.now()
        ));
    }

    public List<OperationLogDto> list(String operatorName,
                                      String roleCode,
                                      String actionType,
                                      String dateFrom,
                                      String dateTo) {
        return operationLogMapper.selectList(buildWrapper(operatorName, roleCode, actionType, dateFrom, dateTo))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PageResult<OperationLogDto> page(long current,
                                            long size,
                                            String operatorName,
                                            String roleCode,
                                            String actionType,
                                            String dateFrom,
                                            String dateTo) {
        Page<OperationLogEntity> page = operationLogMapper.selectPage(
                new Page<>(current, size),
                buildWrapper(operatorName, roleCode, actionType, dateFrom, dateTo)
        );
        return PageResult.from(page, page.getRecords().stream().map(this::toDto).toList());
    }

    public Map<String, Long> summarize(String operatorName,
                                       String roleCode,
                                       String actionType,
                                       String dateFrom,
                                       String dateTo) {
        List<OperationLogDto> rows = list(operatorName, roleCode, actionType, dateFrom, dateTo);
        String today = LocalDate.now().toString();
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("total", (long) rows.size());
        summary.put("createCount", rows.stream().filter(item -> String.valueOf(item.actionType()).contains("CREATE")).count());
        summary.put("reviewCount", rows.stream().filter(item -> String.valueOf(item.actionType()).contains("REVIEW")).count());
        summary.put("todayCount", rows.stream().filter(item -> String.valueOf(item.createdAt()).startsWith(today)).count());
        return summary;
    }

    private LambdaQueryWrapper<OperationLogEntity> buildWrapper(String operatorName,
                                                                String roleCode,
                                                                String actionType,
                                                                String dateFrom,
                                                                String dateTo) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(operatorName)) {
            wrapper.like(OperationLogEntity::getOperatorName, operatorName.trim());
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.eq(OperationLogEntity::getRoleCode, roleCode.trim().toUpperCase());
        }
        if (StringUtils.hasText(actionType)) {
            wrapper.eq(OperationLogEntity::getActionType, actionType.trim().toUpperCase());
        }

        LocalDateTime start = parseStart(dateFrom);
        LocalDateTime end = parseEnd(dateTo);
        if (start != null) {
            wrapper.ge(OperationLogEntity::getCreatedAt, start);
        }
        if (end != null) {
            wrapper.le(OperationLogEntity::getCreatedAt, end);
        }

        return wrapper.orderByDesc(OperationLogEntity::getId);
    }

    private OperationLogDto toDto(OperationLogEntity item) {
        return new OperationLogDto(
                item.getId(),
                item.getOperatorUserId(),
                item.getOperatorName(),
                item.getRoleCode(),
                item.getActionType(),
                item.getTargetType(),
                item.getTargetId(),
                item.getContent(),
                item.getIpAddress(),
                item.getCreatedAt()
        );
    }

    private LocalDateTime parseStart(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value.trim()).atStartOfDay();
    }

    private LocalDateTime parseEnd(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value.trim()).plusDays(1).atStartOfDay().minusSeconds(1);
    }

    private String extractIpAddress() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || attributes.getRequest() == null) {
            return "";
        }
        String forwarded = attributes.getRequest().getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return safeValue(attributes.getRequest().getRemoteAddr());
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}

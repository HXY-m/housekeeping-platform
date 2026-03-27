package com.housekeeping.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.admin.dto.AdminCategoryDto;
import com.housekeeping.admin.dto.AdminCategorySaveRequest;
import com.housekeeping.audit.OperationLogActions;
import com.housekeeping.audit.service.OperationLogService;
import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.category.mapper.ServiceCategoryMapper;
import com.housekeeping.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminCategoryService {

    private final ServiceCategoryMapper serviceCategoryMapper;
    private final OperationLogService operationLogService;

    public AdminCategoryService(ServiceCategoryMapper serviceCategoryMapper,
                                OperationLogService operationLogService) {
        this.serviceCategoryMapper = serviceCategoryMapper;
        this.operationLogService = operationLogService;
    }

    public List<AdminCategoryDto> listCategories(String keyword, Integer enabled) {
        LambdaQueryWrapper<ServiceCategoryEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item
                    .like(ServiceCategoryEntity::getName, keyword.trim())
                    .or()
                    .like(ServiceCategoryEntity::getDescription, keyword.trim()));
        }
        if (enabled != null) {
            wrapper.eq(ServiceCategoryEntity::getEnabled, enabled);
        }
        wrapper.orderByAsc(ServiceCategoryEntity::getId);
        return serviceCategoryMapper.selectList(wrapper)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AdminCategoryDto createCategory(AdminCategorySaveRequest request) {
        ServiceCategoryEntity entity = new ServiceCategoryEntity();
        applyRequest(entity, request);
        try {
            serviceCategoryMapper.insert(entity);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("slug 已存在，请更换后重试");
        }

        operationLogService.record(
                OperationLogActions.CATEGORY_CREATE,
                "SERVICE_CATEGORY",
                entity.getId(),
                "创建服务项目 " + entity.getName()
        );
        return toDto(entity);
    }

    @Transactional
    public AdminCategoryDto updateCategory(Long id, AdminCategorySaveRequest request) {
        ServiceCategoryEntity entity = requireCategory(id);
        applyRequest(entity, request);
        try {
            serviceCategoryMapper.updateById(entity);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("slug 已存在，请更换后重试");
        }

        operationLogService.record(
                OperationLogActions.CATEGORY_UPDATE,
                "SERVICE_CATEGORY",
                entity.getId(),
                "更新服务项目 " + entity.getName()
        );
        return toDto(entity);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ServiceCategoryEntity entity = requireCategory(id);
        serviceCategoryMapper.deleteById(id);
        operationLogService.record(
                OperationLogActions.CATEGORY_DELETE,
                "SERVICE_CATEGORY",
                id,
                "删除服务项目 " + entity.getName()
        );
    }

    private ServiceCategoryEntity requireCategory(Long id) {
        ServiceCategoryEntity entity = serviceCategoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("未找到对应服务项目");
        }
        return entity;
    }

    private void applyRequest(ServiceCategoryEntity entity, AdminCategorySaveRequest request) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description().trim());
        entity.setPriceLabel(request.priceLabel().trim());
        entity.setSlug(buildSlug(request));
        entity.setServiceDuration(safeValue(request.serviceDuration()));
        entity.setServiceArea(safeValue(request.serviceArea()));
        entity.setServiceScene(safeValue(request.serviceScene()));
        entity.setExtraServices(safeValue(request.extraServices()));
        entity.setEnabled(Boolean.TRUE.equals(request.enabled()) ? 1 : 0);
    }

    private String buildSlug(AdminCategorySaveRequest request) {
        if (StringUtils.hasText(request.slug())) {
            return request.slug().trim().toLowerCase();
        }
        return request.name().trim().toLowerCase().replace(' ', '-');
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private AdminCategoryDto toDto(ServiceCategoryEntity entity) {
        return new AdminCategoryDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPriceLabel(),
                entity.getSlug(),
                entity.getServiceDuration(),
                entity.getServiceArea(),
                entity.getServiceScene(),
                entity.getExtraServices(),
                entity.getEnabled() == null || entity.getEnabled() == 1
        );
    }
}

package com.housekeeping.aftersale.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.aftersale.dto.AfterSaleAttachmentDto;
import com.housekeeping.aftersale.entity.AfterSaleEntity;
import com.housekeeping.aftersale.entity.AfterSaleAttachmentEntity;
import com.housekeeping.aftersale.mapper.AfterSaleAttachmentMapper;
import com.housekeeping.aftersale.mapper.AfterSaleMapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.upload.dto.UploadedFileDto;
import com.housekeeping.upload.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AfterSaleAttachmentService {

    private static final int MAX_ATTACHMENT_COUNT = 3;

    private final AfterSaleAttachmentMapper afterSaleAttachmentMapper;
    private final AfterSaleMapper afterSaleMapper;
    private final FileStorageService fileStorageService;

    public AfterSaleAttachmentService(AfterSaleAttachmentMapper afterSaleAttachmentMapper,
                                      AfterSaleMapper afterSaleMapper,
                                      FileStorageService fileStorageService) {
        this.afterSaleAttachmentMapper = afterSaleAttachmentMapper;
        this.afterSaleMapper = afterSaleMapper;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public AfterSaleAttachmentDto uploadCurrentUserAttachment(Long afterSaleId, MultipartFile file) {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("Please log in first.");
        }

        AfterSaleEntity afterSale = afterSaleMapper.selectById(afterSaleId);
        if (afterSale == null) {
            throw new BusinessException("After-sale ticket not found.");
        }
        if (!Objects.equals(afterSale.getUserId(), currentUser.userId())) {
            throw new BusinessException("You can only upload attachments for your own after-sale ticket.");
        }

        Long attachmentCount = afterSaleAttachmentMapper.selectCount(
                new LambdaQueryWrapper<AfterSaleAttachmentEntity>()
                        .eq(AfterSaleAttachmentEntity::getAfterSaleId, afterSaleId)
        );
        if (attachmentCount != null && attachmentCount >= MAX_ATTACHMENT_COUNT) {
            throw new BusinessException("At most 3 after-sale attachments are supported.");
        }

        UploadedFileDto uploadedFile = fileStorageService.storeImage(file);
        afterSaleAttachmentMapper.insert(new AfterSaleAttachmentEntity(
                afterSaleId,
                uploadedFile.name(),
                uploadedFile.url(),
                LocalDateTime.now()
        ));
        return new AfterSaleAttachmentDto(uploadedFile.name(), uploadedFile.url());
    }

    public Map<Long, List<AfterSaleAttachmentDto>> groupByAfterSaleIds(List<Long> afterSaleIds) {
        if (afterSaleIds == null || afterSaleIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<AfterSaleAttachmentDto>> grouped = new LinkedHashMap<>();
        afterSaleAttachmentMapper.selectList(
                new LambdaQueryWrapper<AfterSaleAttachmentEntity>()
                        .in(AfterSaleAttachmentEntity::getAfterSaleId, afterSaleIds)
                        .orderByAsc(AfterSaleAttachmentEntity::getId)
        ).forEach(item -> grouped.computeIfAbsent(item.getAfterSaleId(), key -> new ArrayList<>())
                .add(new AfterSaleAttachmentDto(item.getFileName(), item.getFileUrl())));
        return grouped;
    }
}

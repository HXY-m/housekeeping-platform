package com.housekeeping.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.order.OrderServiceRecordStage;
import com.housekeeping.order.dto.OrderServiceRecordAttachmentDto;
import com.housekeeping.order.dto.OrderServiceRecordDto;
import com.housekeeping.order.entity.OrderServiceRecordAttachmentEntity;
import com.housekeeping.order.entity.OrderServiceRecordEntity;
import com.housekeeping.order.mapper.OrderServiceRecordAttachmentMapper;
import com.housekeeping.order.mapper.OrderServiceRecordMapper;
import com.housekeeping.upload.dto.UploadedFileDto;
import com.housekeeping.upload.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class OrderServiceRecordService {

    private final OrderServiceRecordMapper orderServiceRecordMapper;
    private final OrderServiceRecordAttachmentMapper orderServiceRecordAttachmentMapper;
    private final FileStorageService fileStorageService;

    public OrderServiceRecordService(OrderServiceRecordMapper orderServiceRecordMapper,
                                     OrderServiceRecordAttachmentMapper orderServiceRecordAttachmentMapper,
                                     FileStorageService fileStorageService) {
        this.orderServiceRecordMapper = orderServiceRecordMapper;
        this.orderServiceRecordAttachmentMapper = orderServiceRecordAttachmentMapper;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public OrderServiceRecordDto createRecord(Long orderId,
                                              Long workerId,
                                              String stage,
                                              String description,
                                              MultipartFile file) {
        OrderServiceRecordStage recordStage = parseStage(stage);
        if (!StringUtils.hasText(description)) {
            throw new BusinessException("服务记录说明不能为空");
        }

        OrderServiceRecordEntity entity = new OrderServiceRecordEntity(
                orderId,
                workerId,
                recordStage.code(),
                description.trim(),
                LocalDateTime.now()
        );
        orderServiceRecordMapper.insert(entity);

        List<OrderServiceRecordAttachmentDto> attachments = new ArrayList<>();
        if (file != null && !file.isEmpty()) {
            UploadedFileDto uploadedFile = fileStorageService.storeImage(file);
            orderServiceRecordAttachmentMapper.insert(new OrderServiceRecordAttachmentEntity(
                    entity.getId(),
                    uploadedFile.name(),
                    uploadedFile.url(),
                    LocalDateTime.now()
            ));
            attachments.add(new OrderServiceRecordAttachmentDto(uploadedFile.name(), uploadedFile.url()));
        }

        return new OrderServiceRecordDto(entity.getId(), entity.getStage(), entity.getDescription(), entity.getCreatedAt(), attachments);
    }

    public boolean hasStage(Long orderId, OrderServiceRecordStage stage) {
        Long count = orderServiceRecordMapper.selectCount(
                new LambdaQueryWrapper<OrderServiceRecordEntity>()
                        .eq(OrderServiceRecordEntity::getOrderId, orderId)
                        .eq(OrderServiceRecordEntity::getStage, stage.code())
        );
        return count != null && count > 0;
    }

    public Map<Long, List<OrderServiceRecordDto>> groupByOrderIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }

        List<OrderServiceRecordEntity> records = orderServiceRecordMapper.selectList(
                new LambdaQueryWrapper<OrderServiceRecordEntity>()
                        .in(OrderServiceRecordEntity::getOrderId, orderIds)
                        .orderByAsc(OrderServiceRecordEntity::getId)
        );
        if (records.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<OrderServiceRecordAttachmentDto>> attachmentMap = buildAttachmentMap(
                records.stream().map(OrderServiceRecordEntity::getId).toList()
        );

        Map<Long, List<OrderServiceRecordDto>> grouped = new LinkedHashMap<>();
        for (OrderServiceRecordEntity record : records) {
            grouped.computeIfAbsent(record.getOrderId(), key -> new ArrayList<>())
                    .add(new OrderServiceRecordDto(
                            record.getId(),
                            record.getStage(),
                            record.getDescription(),
                            record.getCreatedAt(),
                            attachmentMap.getOrDefault(record.getId(), List.of())
                    ));
        }
        return grouped;
    }

    private Map<Long, List<OrderServiceRecordAttachmentDto>> buildAttachmentMap(List<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<OrderServiceRecordAttachmentDto>> grouped = new LinkedHashMap<>();
        orderServiceRecordAttachmentMapper.selectList(
                new LambdaQueryWrapper<OrderServiceRecordAttachmentEntity>()
                        .in(OrderServiceRecordAttachmentEntity::getServiceRecordId, recordIds)
                        .orderByAsc(OrderServiceRecordAttachmentEntity::getId)
        ).forEach(item -> grouped.computeIfAbsent(item.getServiceRecordId(), key -> new ArrayList<>())
                .add(new OrderServiceRecordAttachmentDto(item.getFileName(), item.getFileUrl())));
        return grouped;
    }

    private OrderServiceRecordStage parseStage(String stage) {
        try {
            return OrderServiceRecordStage.from(stage);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("不支持的服务记录阶段");
        }
    }
}

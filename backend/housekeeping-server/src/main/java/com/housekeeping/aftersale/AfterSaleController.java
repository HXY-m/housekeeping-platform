package com.housekeeping.aftersale;

import com.housekeeping.aftersale.dto.AfterSaleAttachmentDto;
import com.housekeeping.aftersale.dto.AfterSaleCreateRequest;
import com.housekeeping.aftersale.dto.AfterSaleDto;
import com.housekeeping.aftersale.service.AfterSaleAttachmentService;
import com.housekeeping.aftersale.service.AfterSaleService;
import com.housekeeping.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/after-sales")
@PreAuthorize("hasRole('USER')")
@Tag(name = "售后反馈模块", description = "用户提交售后反馈与查看处理状态")
public class AfterSaleController {

    private final AfterSaleService afterSaleService;
    private final AfterSaleAttachmentService afterSaleAttachmentService;

    public AfterSaleController(AfterSaleService afterSaleService,
                               AfterSaleAttachmentService afterSaleAttachmentService) {
        this.afterSaleService = afterSaleService;
        this.afterSaleAttachmentService = afterSaleAttachmentService;
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户售后反馈列表")
    public ApiResponse<List<AfterSaleDto>> myAfterSales() {
        return ApiResponse.ok(afterSaleService.listCurrentUserAfterSales());
    }

    @PostMapping
    @Operation(summary = "提交售后反馈")
    public ApiResponse<AfterSaleDto> create(@Valid @RequestBody AfterSaleCreateRequest request) {
        return ApiResponse.ok(afterSaleService.create(request));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传售后凭证图片")
    public ApiResponse<AfterSaleAttachmentDto> uploadAttachment(@PathVariable Long id,
                                                                @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(afterSaleAttachmentService.uploadCurrentUserAttachment(id, file));
    }
}

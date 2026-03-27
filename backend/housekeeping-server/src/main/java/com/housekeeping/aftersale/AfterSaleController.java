package com.housekeeping.aftersale;

import com.housekeeping.aftersale.dto.AfterSaleCreateRequest;
import com.housekeeping.aftersale.dto.AfterSaleDto;
import com.housekeeping.aftersale.service.AfterSaleService;
import com.housekeeping.auth.annotation.RequireRole;
import com.housekeeping.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/after-sales")
@RequireRole("USER")
@Tag(name = "售后反馈模块", description = "用户提交售后反馈与查看处理状态")
public class AfterSaleController {

    private final AfterSaleService afterSaleService;

    public AfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
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
}

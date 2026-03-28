package com.housekeeping.aftersale;

import com.housekeeping.aftersale.dto.AfterSaleDto;
import com.housekeeping.aftersale.dto.AfterSaleHandleRequest;
import com.housekeeping.aftersale.service.AfterSaleService;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.PageQuery;
import com.housekeeping.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/after-sales")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_AFTER_SALE_MANAGE')")
@Tag(name = "后台售后管理", description = "管理员查看并处理用户售后反馈")
public class AdminAfterSaleController {

    private final AfterSaleService afterSaleService;

    public AdminAfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    @GetMapping
    @Operation(summary = "获取售后反馈列表")
    public ApiResponse<PageResult<AfterSaleDto>> listAll(PageQuery pageQuery,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(afterSaleService.pageAll(
                pageQuery.safeCurrent(),
                pageQuery.safeSize(),
                status,
                keyword
        ));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "处理售后反馈")
    public ApiResponse<AfterSaleDto> handle(@PathVariable Long id,
                                            @Valid @RequestBody AfterSaleHandleRequest request) {
        return ApiResponse.ok(afterSaleService.handle(id, request));
    }
}

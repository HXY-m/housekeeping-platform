package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminCategoryDto;
import com.housekeeping.admin.dto.AdminCategorySaveRequest;
import com.housekeeping.admin.service.AdminCategoryService;
import com.housekeeping.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员服务项目管理", description = "管理员查看、创建、编辑和删除服务项目")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    @Operation(summary = "获取服务项目列表")
    public ApiResponse<List<AdminCategoryDto>> categories(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Integer enabled) {
        return ApiResponse.ok(adminCategoryService.listCategories(keyword, enabled));
    }

    @PostMapping
    @Operation(summary = "创建服务项目")
    public ApiResponse<AdminCategoryDto> create(@Valid @RequestBody AdminCategorySaveRequest request) {
        return ApiResponse.ok(adminCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新服务项目")
    public ApiResponse<AdminCategoryDto> update(@PathVariable Long id,
                                                @Valid @RequestBody AdminCategorySaveRequest request) {
        return ApiResponse.ok(adminCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务项目")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return ApiResponse.ok(null);
    }
}

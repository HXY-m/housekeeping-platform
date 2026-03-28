package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminPermissionCatalogDto;
import com.housekeeping.admin.dto.AdminRolePermissionDto;
import com.housekeeping.admin.dto.AdminRolePermissionUpdateRequest;
import com.housekeeping.admin.service.AdminPermissionService;
import com.housekeeping.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_PERMISSION_MANAGE')")
@Tag(name = "Admin permission config", description = "Configure role to permission bindings.")
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    public AdminPermissionController(AdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @GetMapping("/catalog")
    @Operation(summary = "Get role permission catalog")
    public ApiResponse<AdminPermissionCatalogDto> catalog() {
        return ApiResponse.ok(adminPermissionService.getCatalog());
    }

    @PutMapping("/roles/{roleCode}")
    @Operation(summary = "Update permissions for a role")
    public ApiResponse<AdminRolePermissionDto> update(@PathVariable String roleCode,
                                                      @Valid @RequestBody AdminRolePermissionUpdateRequest request) {
        return ApiResponse.ok(adminPermissionService.updateRolePermissions(roleCode.trim().toUpperCase(), request.permissionCodes()));
    }
}

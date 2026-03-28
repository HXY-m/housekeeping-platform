package com.housekeeping.admin;

import com.housekeeping.admin.dto.AdminUserDto;
import com.housekeeping.admin.dto.AdminUserSaveRequest;
import com.housekeeping.admin.service.AdminUserService;
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
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('ADMIN_USER_MANAGE')")
@Tag(name = "管理员用户管理", description = "管理员查看、创建、更新和停用平台用户")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "获取用户列表")
    public ApiResponse<List<AdminUserDto>> users(@RequestParam(required = false) String roleCode,
                                                 @RequestParam(required = false) String realName,
                                                 @RequestParam(required = false) String phone,
                                                 @RequestParam(required = false) String status) {
        return ApiResponse.ok(adminUserService.listUsers(roleCode, realName, phone, status));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<AdminUserDto> create(@Valid @RequestBody AdminUserSaveRequest request) {
        return ApiResponse.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<AdminUserDto> update(@PathVariable Long id,
                                            @Valid @RequestBody AdminUserSaveRequest request) {
        return ApiResponse.ok(adminUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户（逻辑删除）")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ApiResponse.ok(null);
    }
}

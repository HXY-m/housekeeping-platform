package com.housekeeping.user;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.user.dto.UserAddressDto;
import com.housekeeping.user.dto.UserAddressSaveRequest;
import com.housekeeping.user.dto.UserProfileDto;
import com.housekeeping.user.dto.UserProfileUpdateRequest;
import com.housekeeping.user.service.UserProfileService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
@Tag(name = "用户中心模块", description = "普通用户资料与地址簿接口")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户资料")
    public ApiResponse<UserProfileDto> profile() {
        return ApiResponse.ok(userProfileService.currentProfile());
    }

    @PutMapping("/profile")
    @Operation(summary = "更新当前用户资料")
    public ApiResponse<UserProfileDto> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.ok(userProfileService.updateProfile(request));
    }

    @GetMapping("/addresses")
    @Operation(summary = "获取当前用户地址簿")
    public ApiResponse<List<UserAddressDto>> addresses() {
        return ApiResponse.ok(userProfileService.currentAddresses());
    }

    @PostMapping("/addresses")
    @Operation(summary = "新增地址")
    public ApiResponse<UserAddressDto> createAddress(@Valid @RequestBody UserAddressSaveRequest request) {
        return ApiResponse.ok(userProfileService.createAddress(request));
    }

    @PutMapping("/addresses/{id}")
    @Operation(summary = "更新地址")
    public ApiResponse<UserAddressDto> updateAddress(@PathVariable Long id,
                                                     @Valid @RequestBody UserAddressSaveRequest request) {
        return ApiResponse.ok(userProfileService.updateAddress(id, request));
    }

    @DeleteMapping("/addresses/{id}")
    @Operation(summary = "删除地址")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        userProfileService.deleteAddress(id);
        return ApiResponse.ok(null);
    }
}

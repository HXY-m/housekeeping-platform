package com.housekeeping.auth.controller;

import com.housekeeping.auth.annotation.RequireLogin;
import com.housekeeping.auth.dto.CurrentUserDto;
import com.housekeeping.auth.dto.DemoAccountDto;
import com.housekeeping.auth.dto.LoginRequest;
import com.housekeeping.auth.dto.LoginResponse;
import com.housekeeping.auth.dto.RegisterRequest;
import com.housekeeping.auth.service.AuthService;
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
@RequestMapping("/api/auth")
@Tag(name = "认证模块", description = "登录、注册、当前用户与演示账号接口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据手机号、密码和角色登录，返回 Bearer Token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "普通用户注册", description = "注册成功后自动返回登录态")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @RequireLogin
    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "需要先登录")
    public ApiResponse<CurrentUserDto> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    @GetMapping("/demo-accounts")
    @Operation(summary = "获取演示账号列表")
    public ApiResponse<List<DemoAccountDto>> demoAccounts() {
        return ApiResponse.ok(authService.demoAccounts());
    }
}

package com.housekeeping.home;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.home.dto.HomePageDto;
import com.housekeeping.home.dto.ServiceCategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "首页模块", description = "首页数据与服务分类接口")
public class HomeController {

    private final DemoDataService demoDataService;

    public HomeController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/home")
    @Operation(summary = "获取首页数据")
    public ApiResponse<HomePageDto> home() {
        return ApiResponse.ok(demoDataService.getHomePage());
    }

    @GetMapping("/categories")
    @Operation(summary = "获取服务分类")
    public ApiResponse<List<ServiceCategoryDto>> categories() {
        return ApiResponse.ok(demoDataService.getCategories());
    }
}

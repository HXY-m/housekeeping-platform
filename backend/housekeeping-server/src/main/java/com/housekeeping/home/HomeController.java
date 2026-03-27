package com.housekeeping.home;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.DemoDataService;
import com.housekeeping.home.dto.HomePageDto;
import com.housekeeping.home.dto.ServiceCategoryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final DemoDataService demoDataService;

    public HomeController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/home")
    public ApiResponse<HomePageDto> home() {
        return ApiResponse.ok(demoDataService.getHomePage());
    }

    @GetMapping("/categories")
    public ApiResponse<List<ServiceCategoryDto>> categories() {
        return ApiResponse.ok(demoDataService.getCategories());
    }
}

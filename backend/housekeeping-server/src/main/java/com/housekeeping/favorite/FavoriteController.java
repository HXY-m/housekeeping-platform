package com.housekeeping.favorite;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.common.PageQuery;
import com.housekeeping.common.PageResult;
import com.housekeeping.favorite.service.FavoriteWorkerService;
import com.housekeeping.home.dto.WorkerCardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites/workers")
@PreAuthorize("hasRole('USER') and hasAuthority('USER_FAVORITE_MANAGE')")
@Tag(name = "服务人员收藏", description = "用户收藏、取消收藏和查询收藏的服务人员")
public class FavoriteController {

    private final FavoriteWorkerService favoriteWorkerService;

    public FavoriteController(FavoriteWorkerService favoriteWorkerService) {
        this.favoriteWorkerService = favoriteWorkerService;
    }

    @GetMapping
    @Operation(summary = "获取收藏的服务人员列表")
    public ApiResponse<PageResult<WorkerCardDto>> workers(PageQuery pageQuery) {
        return ApiResponse.ok(favoriteWorkerService.pageFavoriteWorkers(pageQuery.safeCurrent(), pageQuery.safeSize()));
    }

    @GetMapping("/ids")
    @Operation(summary = "获取收藏的服务人员ID列表")
    public ApiResponse<List<Long>> workerIds() {
        return ApiResponse.ok(favoriteWorkerService.listFavoriteWorkerIds());
    }

    @PostMapping("/{workerId}")
    @Operation(summary = "收藏服务人员")
    public ApiResponse<Void> favorite(@PathVariable Long workerId) {
        favoriteWorkerService.favorite(workerId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{workerId}")
    @Operation(summary = "取消收藏服务人员")
    public ApiResponse<Void> unfavorite(@PathVariable Long workerId) {
        favoriteWorkerService.unfavorite(workerId);
        return ApiResponse.ok(null);
    }
}

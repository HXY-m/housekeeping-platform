package com.housekeeping.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.housekeeping.auth.support.CurrentUserContext;
import com.housekeeping.auth.support.SessionUser;
import com.housekeeping.common.mapper.WorkerDtoMapper;
import com.housekeeping.exception.BusinessException;
import com.housekeeping.favorite.entity.FavoriteWorkerEntity;
import com.housekeeping.favorite.mapper.FavoriteWorkerMapper;
import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.worker.entity.WorkerEntity;
import com.housekeeping.worker.mapper.WorkerMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class FavoriteWorkerService {

    private final FavoriteWorkerMapper favoriteWorkerMapper;
    private final WorkerMapper workerMapper;
    private final WorkerDtoMapper workerDtoMapper;

    public FavoriteWorkerService(FavoriteWorkerMapper favoriteWorkerMapper,
                                 WorkerMapper workerMapper,
                                 WorkerDtoMapper workerDtoMapper) {
        this.favoriteWorkerMapper = favoriteWorkerMapper;
        this.workerMapper = workerMapper;
        this.workerDtoMapper = workerDtoMapper;
    }

    public List<Long> listFavoriteWorkerIds() {
        SessionUser currentUser = requireCurrentUser();
        return favoriteWorkerMapper.selectList(
                        new LambdaQueryWrapper<FavoriteWorkerEntity>()
                                .eq(FavoriteWorkerEntity::getUserId, currentUser.userId())
                                .orderByDesc(FavoriteWorkerEntity::getId))
                .stream()
                .map(FavoriteWorkerEntity::getWorkerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public List<WorkerCardDto> listFavoriteWorkers() {
        List<Long> workerIds = listFavoriteWorkerIds();
        if (workerIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> orderMap = new LinkedHashMap<>();
        for (int index = 0; index < workerIds.size(); index++) {
            orderMap.put(workerIds.get(index), index);
        }

        return workerMapper.selectBatchIds(workerIds).stream()
                .sorted((left, right) -> Integer.compare(
                        orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE),
                        orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)
                ))
                .map(workerDtoMapper::toCardDto)
                .toList();
    }

    @Transactional
    public void favorite(Long workerId) {
        SessionUser currentUser = requireCurrentUser();
        requireWorker(workerId);

        FavoriteWorkerEntity existed = favoriteWorkerMapper.selectOne(
                new LambdaQueryWrapper<FavoriteWorkerEntity>()
                        .eq(FavoriteWorkerEntity::getUserId, currentUser.userId())
                        .eq(FavoriteWorkerEntity::getWorkerId, workerId)
                        .last("limit 1")
        );
        if (existed != null) {
            return;
        }

        try {
            favoriteWorkerMapper.insert(new FavoriteWorkerEntity(currentUser.userId(), workerId, LocalDateTime.now()));
        } catch (DuplicateKeyException ignore) {
            // Unique constraint may already have been inserted by a concurrent request.
        }
    }

    @Transactional
    public void unfavorite(Long workerId) {
        SessionUser currentUser = requireCurrentUser();
        favoriteWorkerMapper.delete(
                new LambdaQueryWrapper<FavoriteWorkerEntity>()
                        .eq(FavoriteWorkerEntity::getUserId, currentUser.userId())
                        .eq(FavoriteWorkerEntity::getWorkerId, workerId)
        );
    }

    private WorkerEntity requireWorker(Long workerId) {
        WorkerEntity worker = workerMapper.selectById(workerId);
        if (worker == null) {
            throw new BusinessException("未找到对应的服务人员");
        }
        return worker;
    }

    private SessionUser requireCurrentUser() {
        SessionUser currentUser = CurrentUserContext.get();
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }
}

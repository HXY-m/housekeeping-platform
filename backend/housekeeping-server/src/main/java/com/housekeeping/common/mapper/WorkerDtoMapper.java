package com.housekeeping.common.mapper;

import com.housekeeping.home.dto.WorkerCardDto;
import com.housekeeping.worker.dto.WorkerDetailDto;
import com.housekeeping.worker.entity.WorkerEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class WorkerDtoMapper {

    public WorkerCardDto toCardDto(WorkerEntity worker) {
        return new WorkerCardDto(
                worker.getId(),
                worker.getName(),
                worker.getRoleLabel(),
                worker.getRating(),
                worker.getCompletedOrders(),
                worker.getHourlyPrice(),
                worker.getCity(),
                worker.getIntro(),
                split(worker.getTags()),
                worker.getNextAvailable()
        );
    }

    public WorkerDetailDto toDetailDto(WorkerEntity worker) {
        return new WorkerDetailDto(
                worker.getId(),
                worker.getName(),
                worker.getRoleLabel(),
                worker.getRating(),
                worker.getCompletedOrders(),
                worker.getHourlyPrice(),
                worker.getCity(),
                worker.getIntro(),
                split(worker.getTags()),
                worker.getNextAvailable(),
                worker.getYearsOfExperience(),
                split(worker.getCertificates()),
                split(worker.getServiceAreas()),
                split(worker.getServiceCases())
        );
    }

    public List<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(raw.split("\\s*,\\s*"))
                .filter(value -> !value.isBlank())
                .toList();
    }
}

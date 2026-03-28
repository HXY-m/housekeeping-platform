package com.housekeeping.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long current, long size, long pages) {

    public static <T> PageResult<T> from(IPage<?> page, List<T> records) {
        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }
}

package com.housekeeping.common;

public class PageQuery {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 10L;
    private static final long MAX_SIZE = 100L;

    private Long current = DEFAULT_CURRENT;
    private Long size = DEFAULT_SIZE;

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public long safeCurrent() {
        if (current == null || current < 1) {
            return DEFAULT_CURRENT;
        }
        return current;
    }

    public long safeSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}

package com.housekeeping.order;

public enum OrderServiceRecordStage {

    CHECK_IN("上门打卡"),
    SERVICE_PROOF("服务过程"),
    FINISH_PROOF("完工凭证");

    private final String label;

    OrderServiceRecordStage(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }

    public static OrderServiceRecordStage from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("stage is blank");
        }
        for (OrderServiceRecordStage stage : values()) {
            if (stage.name().equalsIgnoreCase(value)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("unsupported stage");
    }
}

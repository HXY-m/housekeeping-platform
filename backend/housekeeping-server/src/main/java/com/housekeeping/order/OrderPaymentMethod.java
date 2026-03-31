package com.housekeeping.order;

public enum OrderPaymentMethod {

    WECHAT_PAY("微信支付"),
    ALIPAY("支付宝"),
    BANK_CARD("银行卡");

    private final String label;

    OrderPaymentMethod(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }

    public static OrderPaymentMethod from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("支付方式不能为空");
        }
        for (OrderPaymentMethod item : values()) {
            if (item.name().equalsIgnoreCase(value.trim())) {
                return item;
            }
        }
        throw new IllegalArgumentException("暂不支持该支付方式");
    }
}

package com.weipay.enums;

public enum PayType {

    WXPAY("微信"),
    ALIPAY("支付宝");

    /**
     * 类型
     */
    private final String type;

    PayType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

package com.weipay.enums.wxpay;

public enum WxRefundStatus {
    /**
     * 退款成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 退款关闭
     */
    CLOSED("CLOSED"),

    /**
     * 退款处理中
     */
    PROCESSING("PROCESSING"),

    /**
     * 退款异常
     */
    ABNORMAL("ABNORMAL");

    /**
     * 类型
     */
    private final String type;

    WxRefundStatus(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

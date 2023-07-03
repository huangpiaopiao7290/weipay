package com.weipay.enums.wxpay;

/**
 * 微信支付结果通知地址
 */
public enum WxNotifyType {
    /**
     * 支付回调通知 apiV3
     */
    NATIVE_NOTIFY("/api/wx-pay/native/notify"),

    /**
     * 支付回调通知 apiV2
     */
    NATIVE_NOTIFY_V2("/api/wx-pay-v2/native/notify"),


    /**
     * 退款结果通知
     */
    REFUND_NOTIFY("/api/wx-pay/refunds/notify");

    /**
     * 类型
     */
    private final String type;

    WxNotifyType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

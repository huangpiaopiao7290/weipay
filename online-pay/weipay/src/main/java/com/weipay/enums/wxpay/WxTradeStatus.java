package com.weipay.enums.wxpay;

/**
 * 微信支付交易状态——商家与微信之间
 */
public enum WxTradeStatus {
    /**
     * 支付成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 未支付
     */
    NOTPAY("NOTPAY"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED"),

    /**
     * 转入退款
     */
    REFUND("REFUND");

    /**
     * 类型
     */
    private final String type;

    WxTradeStatus(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

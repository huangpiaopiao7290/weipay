package com.weipay.enums;

/**
 * 订单状态——用户与商家之间
 */
public enum OrderStatus {

    NOTPAY("未支付"),
    SUCCESS("支付成功"),
    CLOSED("超时已关闭"),
    CANCEL("用户已取消"),
    REFUND_PROCESSING("退款中"),
    REFUND_SUCCESS("已退款"),
    REFUND_ABNORMAL("退款异常");

    /**
     * 类型
     */
    private final String type;

    OrderStatus(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

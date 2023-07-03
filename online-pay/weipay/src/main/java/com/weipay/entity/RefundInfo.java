package com.weipay.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 退款表的实体类
 */
@TableName("t_refund_info")
public class RefundInfo extends BaseEntity{
    private String orderNo;         // 商品订单编号
    private String refundNo;        // 退款单编号
    private String refundId;        // 支付系统退款单号
    private Integer totalFee;       // 原订单金额(分)
    private Integer refund;         // 实际退款金额(分)
    private String reason;          // 退款原因
    private String refundStatus;    // 退款单状态
    private String contentReturn;   // 申请退款返回参数
    private String contentNotify;   // 退款结果通知参数

    public RefundInfo() {
    }

    public RefundInfo(String id, Date createTime, Date updateTime, String orderNo, String refundNo, String refundId, Integer totalFee, Integer refund, String reason, String refundStatus, String contentReturn, String contentNotify) {
        super(id, createTime, updateTime);
        this.orderNo = orderNo;
        this.refundNo = refundNo;
        this.refundId = refundId;
        this.totalFee = totalFee;
        this.refund = refund;
        this.reason = reason;
        this.refundStatus = refundStatus;
        this.contentReturn = contentReturn;
        this.contentNotify = contentNotify;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getRefund() {
        return refund;
    }

    public void setRefund(Integer refund) {
        this.refund = refund;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getContentReturn() {
        return contentReturn;
    }

    public void setContentReturn(String contentReturn) {
        this.contentReturn = contentReturn;
    }

    public String getContentNotify() {
        return contentNotify;
    }

    public void setContentNotify(String contentNotify) {
        this.contentNotify = contentNotify;
    }

    @Override
    public String toString() {
        return "RefundInfo{" +
                "orderNo='" + orderNo + '\'' +
                ", refundNo='" + refundNo + '\'' +
                ", refundId='" + refundId + '\'' +
                ", totalFee=" + totalFee +
                ", refund=" + refund +
                ", reason='" + reason + '\'' +
                ", refundStatus='" + refundStatus + '\'' +
                ", contentReturn='" + contentReturn + '\'' +
                ", contentNotify='" + contentNotify + '\'' +
                '}';
    }
}

package com.weipay.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 支付表的实体类
 */
@TableName("t_payment_info")
public class PaymentInfo extends BaseEntity{

    private String orderNo;             //商品订单编号
    private String transactionId;       //支付系统交易编号
    private String paymentType;         //支付类型
    private String tradeType;           //交易类型
    private String tradeState;          //交易状态
    private Integer payerTotal;         //支付金额(分)
    private String content;             //通知参数

    public PaymentInfo() {

    }

    public PaymentInfo(String id, Date createTime, Date updateTime, String orderNo, String transactionId, String paymentType, String tradeType, String tradeState, Integer payerTotal, String content) {
        super(id, createTime, updateTime);
        this.orderNo = orderNo;
        this.transactionId = transactionId;
        this.paymentType = paymentType;
        this.tradeType = tradeType;
        this.tradeState = tradeState;
        this.payerTotal = payerTotal;
        this.content = content;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public Integer getPayerTotal() {
        return payerTotal;
    }

    public void setPayerTotal(Integer payerTotal) {
        this.payerTotal = payerTotal;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return super.toString() + "PaymentInfo{" +
                "orderNo='" + orderNo + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", tradeState='" + tradeState + '\'' +
                ", payerTotal=" + payerTotal +
                ", content='" + content + '\'' +
                '}';
    }
}

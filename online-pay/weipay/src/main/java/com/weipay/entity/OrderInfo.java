package com.weipay.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 订单表的实体类
 */
@TableName("t_order_info")
public class OrderInfo  extends BaseEntity{

    private String title;           //订单标题
    private String orderNo;         //商户订单编号
    private Long userId;            //用户id
    private Long productId;         //支付产品id
    private Integer totalFee;       //订单金额(分)
    private String codeUrl;         //订单二维码连接
    private String orderStatus;     //订单状态

    public OrderInfo() {

    }

    public OrderInfo(String id, Date createTime, Date updateTime, String title, String orderNo, Long userId, Long productId, Integer totalFee, String codeUrl, String orderStatus) {
        super(id, createTime, updateTime);
        this.title = title;
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.totalFee = totalFee;
        this.codeUrl = codeUrl;
        this.orderStatus = orderStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        return super.toString() + "OrderInfo{" +
                "title='" + title + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", totalFee=" + totalFee +
                ", codeUrl='" + codeUrl + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}

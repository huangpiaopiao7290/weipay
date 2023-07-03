package com.weipay.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.weipay.entity.OrderInfo;
import com.weipay.enums.OrderStatus;

import java.util.List;

/**
 * 订单信息业务层
 */
public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 创建订单
     * @param productId 商品ID
     * @return OrderInfo
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 保存二维码
     * @param orderNo 订单编号
     * @param codeUrl 二维码连接
     */
    void saveCodeUrl(String orderNo, String codeUrl);

    /**
     * 按时间倒序的订单列表
     * @return
     */
    List<OrderInfo> listOrderByCreateTimeDesc();

    /**
     * 更新订单状态
     * @param orderNo
     * @param orderStatus
     */
    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    /**
     * 查询订单的支付状态
     * @param orderNo 订单编号
     * @return orderStatus
     */
    String getOrderStatus(String orderNo);

    List<OrderInfo> getNoPayOrderByDuration(int minutes);

    OrderInfo getOrderByOrderNo(String orderNo);
}

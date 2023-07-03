package com.weipay.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付信息业务层
 */
public interface PaymentInfoService {

    /**
     * 创建支付日志
     * @param plainText 支付回调信息
     */
    void createPaymentInfo(Map<String, Object> plainText);
}

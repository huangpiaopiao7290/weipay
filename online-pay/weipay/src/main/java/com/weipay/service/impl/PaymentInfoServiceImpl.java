package com.weipay.service.impl;

import com.google.gson.Gson;
import com.weipay.entity.PaymentInfo;
import com.weipay.enums.PayType;
import com.weipay.mapper.PaymentInfoMapper;
import com.weipay.service.PaymentInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 支付日志实现类
 */
@Service("PaymentInfoServiceImpl")
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Resource(name = "PaymentInfoMapper")
    private PaymentInfoMapper paymentInfoMapper;

    @Override
    public void createPaymentInfo(Map<String, Object> plainText) {
        // 获取订单信息
        String orderNo = (String) plainText.get("out_trade_no");
        String transactionId = (String) plainText.get("transaction_id");        // 此次订单业务编号
        String tradeType = (String) plainText.get("trade_type");                // 支付类型
        String tradeState = (String) plainText.get("trade_state");              // 交易状态

        Map<String, Object> amount = (Map) plainText.get("amount");             // 订单金额相关信息

        // 这里有个类型转换的问题
        Integer payerTotal = ((Double) amount.get("payer_total")).intValue();               // 支付总金额

        // 支付日志信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);

        Gson gson = new Gson();
        paymentInfo.setContent(plainText.toString());

        paymentInfoMapper.insert(paymentInfo);



    }
}

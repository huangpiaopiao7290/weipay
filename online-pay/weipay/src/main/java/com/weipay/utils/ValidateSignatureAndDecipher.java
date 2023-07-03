package com.weipay.utils;

import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 对微信支付回调进行验签和解密
 */
public class ValidateSignatureAndDecipher {

    // 验签
    public static NotificationRequest validateNotifyRequest(HttpServletRequest request, String body) {
        // 获取请求头中必要的参数，构造验签名串
        String serialNumber = request.getHeader("Wechatpay-Serial");    //
        String nonce = request.getHeader("Wechatpay-Nonce");            //
        String timestamp = request.getHeader("Wechatpay-Timestamp");    //
        String signature = request.getHeader("Wechatpay-Signature");    // 请求头Wechatpay-Signature

        // 构造微信请求体
        NotificationRequest wxRequest = new NotificationRequest.Builder().withSerialNumber(serialNumber)
                .withNonce(nonce)
                .withTimestamp(timestamp)
                .withSignature(signature)
                .withBody(body)
                .build();
        return wxRequest;
    }

}

package com.weipay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 创建订单号
 */
public class OrderNoUtil {

    // 创建订单号
    public static String createNo() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate = simpleDateFormat.format(new Date());
        StringBuilder res = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            res.append(random.nextInt(10));
        }
        return newDate + res;
    }

    // 获取订单号
    public static String getOrderNo() {
        return "ORDER_" + createNo();
    }

    // 获取退款订单号
    public static String getRefundNo() {
        return "REFUND_" + createNo();
    }
}

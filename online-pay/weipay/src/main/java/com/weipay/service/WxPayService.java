package com.weipay.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * 微信支付业务层
 */
public interface WxPayService {

    /**
     * Native支付是指商户系统按微信支付协议生成支付二维码
     * @param productId 商品ID
     * @return 支付二维码链接和订单号
     * @throws Exception 支付异常
     */
    Map<String, Object> nativePay(Long productId) throws Exception;

    /**
     * 支付后订单处理
     * @param bodyMap 支付回调信息
     */
    void processOrder(Map<String, Object> bodyMap);

    /**
     * 取消订单
     * @param orderNo 订单号
     * @throws Exception
     */
    void cancelOrder(String orderNo) throws Exception;

    /**
     * 查询订单  在支付回调出现异常时主动向微信服务器发送订单查询请求
     * @param orderNo 订单号
     * @return order
     * @throws Exception e
     */
    String queryOrder(String orderNo) throws Exception;

    /**
     * 核实订单状态  调用微信查单接口
     * 如果订单已支付 则更新订单状态为已支付
     * 如果订单未支付，则调用微信关单接口更新订单状态为已取消
     * @param orderNo 订单号
     * @throws Exception
     */
    void checkOrderStatus(String orderNo) throws Exception;

    /**
     * 退款
     * @param orderNo 订单号
     * @param reason 退款原因
     * @throws Exception
     */
    void refund(String orderNo, String reason) throws Exception;

    /**
     * 退款查询
     * @param refundNo 退款单号
     * @return
     * @throws Exception
     */
    String queryRefund(String refundNo) throws Exception;

    /**
     * 核实退款订单的状态
     * @param refundNo
     * @throws Exception
     */
    void checkRefundStatus(String refundNo) throws Exception;

    /**
     * 接收到退款通知回调后退款单的处理
     * @param bodyMap 解密后明文
     */
    void processRefund(Map<String, Object> bodyMap);

    /**
     * 申请账单
     * @param billDate 账单日期
     * @param type 类型
     * @return 下载url
     * @throws Exception
     */
    String queryBill(String billDate, String type) throws Exception;

    /**
     * 下载账单
     * @param billDate 账单日期
     * @param type 类型
     * @return
     * @throws Exception
     */
    String downloadBill(String billDate, String type) throws Exception;

    /**
     * APIv2支付
     * @param productId
     * @param remoteAddr
     * @return
     * @throws Exception
     */
    Map<String, Object> nativePayV2(Long productId, String remoteAddr) throws Exception;

    /**
     * 购物车下单
     * @param totalFee 总金额
     * @return
     */
    Map<String, Object> nativePayCart(Long totalFee) throws IOException;
}

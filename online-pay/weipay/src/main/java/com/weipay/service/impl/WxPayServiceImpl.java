package com.weipay.service.impl;

import com.google.gson.Gson;
import com.weipay.config.WeiPayConfig;
import com.weipay.entity.OrderInfo;
import com.weipay.entity.RefundInfo;
import com.weipay.enums.OrderStatus;
import com.weipay.enums.wxpay.WxApiType;
import com.weipay.enums.wxpay.WxNotifyType;
import com.weipay.enums.wxpay.WxRefundStatus;
import com.weipay.enums.wxpay.WxTradeStatus;
import com.weipay.mapper.OrderInfoMapper;
import com.weipay.service.*;
import com.weipay.utils.OrderNoUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信支付接口实现类
 */
@Service("WxPayServiceImpl")
public class WxPayServiceImpl implements WxPayService {

    @Resource(name = "WeiPayConfig")
    private WeiPayConfig weiPayConfig;

    @Resource(name = "WxPayClient")
    private CloseableHttpClient wxPayClient;

    @Resource(name = "wxPayNoSignClient")
    private CloseableHttpClient wxPayNoSignClient;

    @Resource(name = "OrderInfoServiceImpl")
    private OrderInfoService orderInfoService;

    @Resource(name = "PaymentInfoServiceImpl")
    private PaymentInfoService paymentInfoService;

    @Resource(name = "RefundInfoServiceImpl")
    private RefundInfoService refundInfoService;

    // 可重入锁
    private final ReentrantLock reentrantLock = new ReentrantLock();


    // 购物车——————————写着玩的
    @Resource(name = "OrderInfoMapper")
    private OrderInfoMapper orderInfoMapper;


    /**
     * 购物车下单————————————测试
     * @param totalFee 总金额
     * @return
     */
    @Override
    public Map<String, Object> nativePayCart(Long totalFee) throws IOException {

        // 创建购物车订单
        OrderInfo orderInfoCart = new OrderInfo();
        orderInfoCart.setTotalFee(totalFee.intValue());                 // 订单金额
        orderInfoCart.setTitle("购物车支付");                              // 订单名称
        orderInfoCart.setOrderNo(OrderNoUtil.getOrderNo());             // 订单编号
        orderInfoCart.setOrderStatus(OrderStatus.NOTPAY.getType());     // 订单状态
        orderInfoCart.setProductId(9527L);                              // 订单商品编号

        System.out.println("---------------将这个购物车订单放入t_order_info中-----------------");
        System.out.println(orderInfoCart);
        // 将购物车订单写入订单表中
        orderInfoMapper.insert(orderInfoCart);


        //请求URL，
        HttpPost httpPost = new HttpPost(weiPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));

        // 请求body参数
        Gson gson = new Gson();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", weiPayConfig.getAppId());
        paramsMap.put("mchid", weiPayConfig.getMchId());
        paramsMap.put("description", orderInfoCart.getTitle());
        paramsMap.put("out_trade_no", orderInfoCart.getOrderNo());
        paramsMap.put("notify_url", weiPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        // 订单金额
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total",orderInfoCart.getTotalFee());
        amountMap.put("currency", "CNY");
        // 将订单金额信息放入请求参数中
        paramsMap.put("amount", amountMap);
        // 将请求参数转换成json字符串
        String reqdata = gson.toJson(paramsMap);

        System.out.println("native下单请求参数=" + reqdata);

        StringEntity entity = new StringEntity(reqdata,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        try {
            //响应体
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                System.out.println("成功,返回结果 = " + bodyString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                System.out.println("成功");
            } else {
                System.out.println("失败,响应状态码 = " + statusCode + ",返回结果 = " + bodyString);
                throw new IOException("请求失败");
            }

            // 返回结果
            HashMap<String,String> respResult = gson.fromJson(bodyString, HashMap.class);
            // 提取code_url
            String codeUrl = respResult.get("code_url");

            // 保存二维码，这里可以存到redis中
            // 暂时保存到t_order_info中
            String orderNo = orderInfoCart.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            // 前端需要的参数
            Map<String, Object> needParams = new HashMap<>();
            needParams.put("code_url", codeUrl);
            needParams.put("order_no", orderInfoCart.getOrderNo());

            return needParams;

        } finally {
            response.close();
        }

    }

    /**
     * 创建订单,调用native接口
     * @param productId 商品ID
     * @return code_url
     * @throws Exception e
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {

        System.out.println("生成订单");
        // 生成订单,从订单表中查找
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrlPre = orderInfo.getCodeUrl();
        if (StringUtils.hasText(codeUrlPre)) {
            System.out.println("订单已保存, 二维码已存在");
            // 直接返回二维码
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", codeUrlPre);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }

        /*
            调用统一下单API
            请求URL    https://api.mch.weixin.qq.com/v3/pay/transactions/native 由配置中获取
            请求方式    POST
         */
        //请求URL，
        HttpPost httpPost = new HttpPost(weiPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));

        // 请求body参数
        Gson gson = new Gson();
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("appid", weiPayConfig.getAppId());
        paramsMap.put("mchid", weiPayConfig.getMchId());
        paramsMap.put("description", orderInfo.getTitle());
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        paramsMap.put("notify_url", weiPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        // 订单金额
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total",orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        // 将订单金额信息放入请求参数中
        paramsMap.put("amount", amountMap);
        // 将请求参数转换成json字符串
        String reqdata = gson.toJson(paramsMap);

        System.out.println("native下单请求参数=" + reqdata);

        StringEntity entity = new StringEntity(reqdata,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        try {
            //响应体
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                System.out.println("成功,返回结果 = " + bodyString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                System.out.println("成功");
            } else {
                System.out.println("失败,响应状态码 = " + statusCode + ",返回结果 = " + bodyString);
                throw new IOException("请求失败");
            }

            // 返回结果
            HashMap<String,String> respResult = gson.fromJson(bodyString, HashMap.class);
            // 提取code_url
            String codeUrl = respResult.get("code_url");

            // 保存二维码，这里可以存到redis中
            // 暂时保存到t_order_info中
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);

            // 前端需要的参数
            Map<String, Object> needParams = new HashMap<>();
            needParams.put("code_url", codeUrl);
            needParams.put("order_no", orderInfo.getOrderNo());

            return needParams;

        } finally {
            response.close();
        }

    }

    /**
     * 支付后订单处理
     * @param bodyMap 支付回调信息
     */
    @Override
    public void processOrder(Map<String, Object> bodyMap) {

        // 数据锁实现并发控制  尝试获取锁，成功返回true
        if (reentrantLock.tryLock()) {
            try {
                // 获取订单号
                String orderNo = (String) bodyMap.get("out_trade_no");
                // 处理回调的重复通知
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                // 只有订单表中的支付状态是"未支付"时，才进行订单更新和日志记录
                if (OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    // 更新订单状态
                    orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                    // 记录支付日志paymentInfo
                    paymentInfoService.createPaymentInfo(bodyMap);

                }
            } finally {
                // 主动释放锁
                reentrantLock.unlock();
            }
        }

    }

    /**
     * 取消订单
     * @param orderNo 订单号
     * @throws Exception e
     */
    @Override
    public void cancelOrder(String orderNo) throws Exception {

        // 关闭订单
        this.closeOrder(orderNo);
        // 更新订单状态——已取消
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }

    /**
     * 查询订单  在支付回调出现异常时主动向微信服务器发送订单查询请求
     * @param orderNo 订单号
     * @return order
     * @throws Exception e
     */
    @Override
    public String queryOrder(String orderNo) throws IOException {
        // 请求url https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}
        String queryUrl = weiPayConfig.getDomain().
                concat(String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo)).
                concat("?mchid=").concat(weiPayConfig.getMchId());
        System.out.println(queryUrl);
        // 创建请求
        HttpGet httpGet = new HttpGet(queryUrl);
        httpGet.setHeader("Accept", "application/json");
        // 发送请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);
        // 处理返回结果
        // 处理响应
        try {
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //处理成功
                System.out.println("订单查询成功");
            } else if (statusCode == 204){
                System.out.println("订单查询成功");
            } else {
                System.out.println("失败,响应状态码 = " + statusCode);
                throw new IOException("订单查询失败");
            }
            return bodyString;
        } finally {
            response.close();
        }

    }

    /**
     * 核实订单状态  调用微信查单接口
     * 如果订单已支付 则更新订单状态为已支付
     * 如果订单未支付，则调用微信关单接口更新订单状态为已取消
     * @param orderNo 订单号
     * @throws Exception
     */
    @Override
    public void checkOrderStatus(String orderNo) throws Exception {
        System.out.println("核实订单状态：" + orderNo);
        String checkResult = this.queryOrder(orderNo);

        Gson gson = new Gson();
        Map<String, Object> respMap = gson.fromJson(checkResult, HashMap.class);

        // 获取此次查询的订单状态
        Object tradeState = respMap.get("trade_state");
        System.out.println("核实的订单状态：" + tradeState);
        // 判断订单状态
        if (WxTradeStatus.SUCCESS.getType().equals(tradeState)) {
            System.out.println("核实订单已支付");
            // 更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(respMap);

        } else if (WxTradeStatus.NOTPAY.getType().equals(tradeState)) {
            System.out.println("核实订单未支付");
            // 调用关单接口
            this.closeOrder(orderNo);
            // 更新订单状态——超时已关闭
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 申请退款
     * @param orderNo 订单号
     * @param reason 退款原因
     * @throws Exception
     */
    @Override
    public void refund(String orderNo, String reason) throws Exception {

        // 生成退款订单记录
        RefundInfo refundInfo = refundInfoService.createRefundInfoByOrderNo(orderNo, reason);

        System.out.println("调用退款API");

        // 退款请求url
        String refundUrl = weiPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost httpPost = new HttpPost(refundUrl);
        // 组装请求体参数
        Gson gson = new Gson();
        Map<String, Object> reqMap = new HashMap<>();

        reqMap.put("out_trade_no", refundInfo.getOrderNo());        // 商家订单编号
        reqMap.put("out_refund_no", refundInfo.getRefundNo());      // 退款订单号
        reqMap.put("reason", refundInfo.getReason());               // 退款原因
        // https://huangpiaopiao.natapp4.cc/api/wx-pay/refunds/notify
        reqMap.put("notify_url", weiPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));  // 退款结果回调接口
        // 金额信息封装
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", refundInfo.getTotalFee());       // 原订单金额(分)
        amountMap.put("refund", refundInfo.getRefund());        // 退款金额(分)
        amountMap.put("currency", "CNY");                       // 退款币种
        reqMap.put("amount", amountMap);

        String jsonReq = gson.toJson(reqMap);
        System.out.println("退款请求的body参数====>" + jsonReq);

        // 发送请求
        StringEntity entity = new StringEntity(jsonReq, "utf-8");
        entity.setContentType("application/json");                              // 设置请求报文格式
        httpPost.setEntity(entity);                                             // 将请求报文放入请求对象中
        httpPost.setHeader("Accept", "application/json");           // 设置请求头
        CloseableHttpResponse response = wxPayClient.execute(httpPost);         // 完成签名并发送请求
        // 处理响应
        try {
            // 解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("申请退款======> " + bodyAsString);
            } else if (statusCode == 204) {
                System.out.println("申请退款，无返回值");
            } else {
                throw new RuntimeException("退款异常=========>状态码：" + statusCode + "返回参数" + bodyAsString);
            }

            // 更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);
            // 更新退款单状态
            refundInfoService.updateRefund(bodyAsString);
        } finally {
            response.close();
        }

    }

    /**
     * 退款查询
     * @param refundNo 退款单号
     * @return
     * @throws Exception
     */
    @Override
    public String queryRefund(String refundNo) throws Exception {
        System.out.println("查询退款接口被调用========>" + refundNo);
        // 退款查询请求的url    https://api.mch.weixin.qq.com /v3/refund/domestic/refunds/1217752501201407033233368018
        String queryRefundUrl = weiPayConfig.getDomain().concat(String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), refundNo));
        // 创建get请求
        HttpGet httpGet = new HttpGet(queryRefundUrl);
        httpGet.setHeader("Accept", "application/json");
        // 签名并发送请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            // 解析响应结果
            String bodyAsString = EntityUtils.toString(response.getEntity());
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("退款查询结果======> " + bodyAsString);
            } else if (statusCode == 204) {
                System.out.println("退款查询，但是无返回值，出大事了");
            } else {
                throw new RuntimeException("退款查询异常=========>状态码：" + statusCode + "返回参数" + bodyAsString);
            }

            return bodyAsString;
        } finally {
            response.close();
        }

    }

    /**
     * 根据退款单号核实退款单状态——在订单记录是退款中时主动向微信确认该订单的状态
     * 如果已退款就更新退款记录中的状态
     */
//    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkRefundStatus(String refundNo) throws Exception {

        System.out.println("根据退款单号核实退款单状态 ===> " + refundNo);

        //调用查询退款单接口
        String result = this.queryRefund(refundNo);

        //组装json请求体字符串
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(result, HashMap.class);

        //获取微信支付端退款状态
        String status = resultMap.get("status");

        String orderNo = resultMap.get("out_trade_no");

        if (WxRefundStatus.SUCCESS.getType().equals(status)) {

            System.out.println("核实订单已退款成功 ===> {}" + refundNo);

            //如果确认退款成功，则更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);

            //更新退款单
            refundInfoService.updateRefund(result);
        }

        if (WxRefundStatus.ABNORMAL.getType().equals(status)) {

            System.out.println("核实订单退款异常  ===> {}" + refundNo);

            //如果确认退款成功，则更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_ABNORMAL);

            //更新退款单
            refundInfoService.updateRefund(result);
        }

    }

    /**
     * 接收到退款通知回调后退款单的处理
     * @param bodyMap 解密后明文
     */
    @Override
    public void processRefund(Map<String, Object> bodyMap) {
        // 商家订单号
        String orderNo =(String) bodyMap.get("out_trade_no");

        // 这里需要将bodyMap转换为json字符串格式  updateRefund(String content)
        Gson gson = new Gson();
        String content = gson.toJson(bodyMap);

        if (reentrantLock.tryLock()) {
            try {
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }
                //更新订单状态——退款成功
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
                //更新退款单
                refundInfoService.updateRefund(content);
            } finally {
                reentrantLock.unlock();
            }
        }

    }

    /**
     * 查询账单————测试
     * @param billDate 账单日期
     * @param type 类型
     * @return
     * @throws Exception
     */
    @Override
    public String queryBill(String billDate, String type) throws Exception {
        System.out.println("账单日期===>" + billDate + '\n' + "账单类型====>" + type);

        String url = "";
        if("tradebill".equals(type)){
            url =  WxApiType.TRADE_BILLS.getType();
        }else if("fundflowbill".equals(type)){
            url =  WxApiType.FUND_FLOW_BILLS.getType();
        }else{
            throw new RuntimeException("不支持的账单类型");
        }
        url = weiPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);

        //创建远程Get 请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");

        //使用wxPayClient发送请求得到响应
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("成功, 申请账单返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                System.out.println("成功");
            } else {
                throw new RuntimeException("申请账单异常, 响应码 = " + statusCode+ ", 申请账单返回结果 = " + bodyAsString);
            }

            //获取账单地址
            Gson gson = new Gson();
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            return resultMap.get("download_url");

        } finally {
            response.close();
        }
    }

    /**
     * 下载账单
     * @param billDate 账单日期
     * @param type 类型
     * @return
     * @throws Exception
     */
    @Override
    public String downloadBill(String billDate, String type) throws Exception {
        System.out.println("下载账单接口调用 {}, {}" + billDate + type);

        //获取账单url地址
        String downloadUrl = this.queryBill(billDate, type);
        //创建远程Get 请求对象
        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.addHeader("Accept", "application/json");

        //使用wxPayClient发送请求得到响应
        CloseableHttpResponse response = wxPayNoSignClient.execute(httpGet);

        try {

            String bodyAsString = EntityUtils.toString(response.getEntity());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("成功, 下载账单返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                System.out.println("下载成功");
            } else {
                throw new RuntimeException("下载账单异常, 响应码 = " + statusCode+ ", 下载账单返回结果 = " + bodyAsString);
            }

            return bodyAsString;

        } finally {
            response.close();
        }
    }

    @Override
    public Map<String, Object> nativePayV2(Long productId, String remoteAddr) throws Exception {
        return null;
    }

    /**
     * 调用关单接口关闭（取消订单）
     * @param orderNo 订单号
     */
    public void closeOrder(String orderNo) throws Exception{
        // 调用微信支付的关单接口
        String closeOrderUrl = weiPayConfig.getDomain().concat(String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo));
        HttpPost httpPost = new HttpPost(closeOrderUrl);
        // 组装请求体
        Gson gson = new Gson();
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", weiPayConfig.getMchId());
        // 将请求参数转换成json字符串
        String reqdata = gson.toJson(paramsMap);

        // 将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(reqdata,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);
        // 处理响应
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204 || statusCode == 200) { //处理成功
                System.out.println("订单取消成功");
            } else {
                System.out.println("失败,响应状态码 = " + statusCode);
                throw new IOException("订单取消失败");
            }
        } finally {
            response.close();
        }
    }

}

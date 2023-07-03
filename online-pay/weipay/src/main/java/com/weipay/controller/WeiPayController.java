package com.weipay.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.weipay.entity.Product;
import com.weipay.service.ProductService;
import com.weipay.service.WxPayService;
import com.weipay.utils.HttpUtil;
import com.weipay.utils.ValidateSignatureAndDecipher;
import com.weipay.vo.R;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付接口
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
public class WeiPayController {

    @Resource(name = "WxPayServiceImpl")
    private WxPayService wxPayService;

//    @Resource(name = "ProductServiceImpl")
//    private ProductService productService;

    @Resource(name = "WxPayNotificationRequest")
    private NotificationHandler notificationHandler;    // 回调请求处理器

    /**
     * 购物车下单
     * @param totalFee 购物车合计金额
     * @return
     */
    @GetMapping("/nativepay/cart/{totalFee}")
    public R createCartNativePay(@PathVariable Long totalFee) throws Exception {

        System.out.println("购物车总计" + totalFee);
        // 下单 productId == 0L 时，代表购物车下单
        Map<String, Object> codeURLMapCart = wxPayService.nativePayCart(totalFee);

        return R.ok().setData(codeURLMapCart);
    }

    /**
     * 支付请求
     * @param productId 商品id
     * @return 微信二维码连接
     * @throws Exception
     */
    @GetMapping("/nativePay/{productId}")
    public R createNativePay(@PathVariable Long productId) throws Exception {
        System.out.println("发起支付请求");

        // native下单
        Map<String, Object> codeURLMap = wxPayService.nativePay(productId);

        R r = R.ok();
        r.setData(codeURLMap);

        return r;
    }

    /**
     * 支付回调结果通知
     * @param request 支付回调请求
     * @param response 确认收到支付结果通知
     * @return
     */
    @PostMapping("/native/notify")
    public String nativePay(HttpServletRequest request, HttpServletResponse response) {

        Gson gson = new Gson();
        HashMap<String, String> repMap = new HashMap<>();
        String res = "";

        try {
            // 将通知参数转化为字符串
            String body = HttpUtil.readData(request);
            HashMap<String, Object> bodyMap = gson.fromJson(body, HashMap.class);

            System.out.println("通知id：" + bodyMap.get("id"));
            System.out.println("完整数据(未解密)：" + body);

            // 签名验证
            NotificationRequest wxRequest = ValidateSignatureAndDecipher.validateNotifyRequest(request, body);
            Notification notification = null;

            try {
                notification = notificationHandler.parse(wxRequest);    // 验签和解析请求体
                res = notification.getDecryptData();
                System.out.println("验签并解密的结果：" + res);      // 从notification中获取解密报文
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("通知验签失败");
                //失败应答
                response.setStatus(500);
                repMap.put("code", "ERROR");
                repMap.put("message", "通知验签失败");
                return gson.toJson(repMap);
            }

            // 订单处理 根据回调结果修改订单表中状态
            HashMap<String, Object> bodyMapDecrypt = gson.fromJson(res, HashMap.class);
            wxPayService.processOrder(bodyMapDecrypt);

            // 通知应答 上面的验签和解密成功的话就告知微信验签成功
            response.setStatus(200);
            repMap.put("code", "SUCCESS");
            repMap.put("message", "成功");




            // todo

            // handleMsgPush
            // 组装一个HTTP请求，post，查询对应课程信息，附带在body

            // 获取订单号
//            String orderNo = (String) bodyMap.get("out_trade_no");
//            Product product = productService.queryProductAndSendInfo(orderNo);

//            String testInfo = "123456789";
//
//            HashMap<String, Object> test = new HashMap<>();
//            test.put("code", 200);
//            test.put("msg", testInfo);

            // todo httpclient 将购买的info 主动发送
//            postRequest("localhost:8086/socketServer/" + 1, test);


//            try {
//                CloseableHttpClient httpclient = HttpClients.createDefault();
//                HttpGet httpGet = new HttpGet("http://127.0.0.1:8086/sendmsg?username=1&msg=12321312312313");
//                httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
//                httpclient.execute(httpGet);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            return gson.toJson(repMap);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            response.setStatus(500);
            repMap.put("code", "ERROR");
            repMap.put("message", "失败");
            return gson.toJson(repMap);
        }

    }

    /**
     * 取消订单
     * @param orderNo 订单编号
     * @return
     * @throws Exception
     */
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable String orderNo) throws Exception {
        System.out.println("取消还未支付的订单" + orderNo);
        wxPayService.cancelOrder(orderNo);
        return R.ok().setMessage("该订单已取消");
    }

    /**
     * 查询订单  在支付回调出现异常时主动向微信服务器发送查询订单结果请求
     * @return
     */
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) throws Exception {
        System.out.println("主动查询订单");
        // 查询结果
        String queryResult = wxPayService.queryOrder(orderNo);
        return R.ok().setMessage("查询成功").data("queryOrder", queryResult);
    }

    /**
     * 退款
     * @param orderNo 订单编号
     * @return
     */
    @PostMapping("/refund")
    public R refund(@RequestParam("orderNo") String orderNo, @RequestParam("reason") String reason) throws Exception {
        System.out.println("申请退款");
        wxPayService.refund(orderNo, reason);
        return R.ok().setMessage("该订单已退款");
    }

    /**
     * 退款回调接口  https://huangpiaopiao.natapp4.cc/api/wx-pay/refunds/notify
     * @param request 退款信息
     * @param response 返回微信确认收到退款信息
     * @return
     */
    @PostMapping("/refunds/notify")
    public String refundNotify(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("退款回调通知");

        Gson gson = new Gson();
        String req = "";
        Map<String, String> repMap = new HashMap<>();       // 应答对象

        try {

            // 将退款通知转换为字符串
            String refundNotifyBody = HttpUtil.readData(request);
            Map<String, Object> refundNotifyBodyMap = gson.fromJson(refundNotifyBody, HashMap.class);

            System.out.println("退款通知id=======>" + refundNotifyBodyMap.get("id"));
            System.out.println("退款通知(未解密)=========> " + refundNotifyBody);

            // todo 验签
            NotificationRequest refundNotifyBodyByVerify = ValidateSignatureAndDecipher.validateNotifyRequest(request, refundNotifyBody);
            Notification notification = null;

            try {
                notification = notificationHandler.parse(refundNotifyBodyByVerify);    // 验签和解析请求体
                req = notification.getDecryptData();                                   // 从notification中获取解密报文
                System.out.println("退款通知验签并解密的结果=======>" + req);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("退款通知验签失败");
                //失败应答
                response.setStatus(500);
                repMap.put("code", "ERROR");
                repMap.put("message", "通知验签失败");
                return gson.toJson(repMap);
            }

            // todo: 退款订单处理 根据回调结果修改订单表中状态
            HashMap<String, Object> refundBodyMapDecrypt = gson.fromJson(req, HashMap.class);
            wxPayService.processRefund(refundBodyMapDecrypt);

            // todo: 通知应答 上面的验签和解密成功的话就告知微信验签成功
            response.setStatus(200);
            repMap.put("code", "SUCCESS");
            repMap.put("message", "成功");
            return gson.toJson(repMap);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            response.setStatus(500);
            repMap.put("code", "ERROR");
            repMap.put("message", "失败");
            return gson.toJson(repMap);
        }
    }

    /**
     * 查询退款结果
     * @param refundNo 退款订单号
     * @return
     */
    @GetMapping("/query-refund/{refundNo}")
    public R queryRefund(@PathVariable String refundNo) throws Exception {
        System.out.println("查询退款");
        String queryRefund = wxPayService.queryRefund(refundNo);
        return  R.ok().setMessage("退款查询").data("refundInfo", queryRefund);
    }

    /**
     *  查询账单
     * @param billDate 账单日期
     * @param type 类型
     * @return
     * @throws Exception
     */
    @GetMapping("/querybill/{billDate}/{type}")
    public R queryTradeBill(@PathVariable String billDate, @PathVariable String type) throws Exception {
        System.out.println("获取账单url");
        String downloadUrl = wxPayService.queryBill(billDate, type);
        return R.ok().setMessage("获取账单url").data("downloadUrl", downloadUrl);
    }

    /**
     * 下载账单
     * @param billDate 账单日期
     * @param type 类型
     * @return
     * @throws Exception
     */
    @GetMapping("/downloadbill/{billDate}/{type}")
    public R downloadBill(@PathVariable String billDate, @PathVariable String type) throws Exception {
        System.out.println("下载账单");
        String result = wxPayService.downloadBill(billDate, type);
        return R.ok().data("result", result);
    }


    // todo httpclient
    public String postRequest(String url, Map<String, Object> paramMap) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // 创建httpPost远程连接实例
        HttpPost post = new HttpPost(url);
        String result = "";
        try (CloseableHttpClient closeableHttpClient = httpClientBuilder.build()) {
            // HttpEntity entity = new StringEntity(jsonDataStr);
            // 修复 POST json 导致中文乱码
            HttpEntity entity = new StringEntity(paramMap.toString(), "UTF-8");
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            HttpResponse resp = closeableHttpClient.execute(post);
            try {
                InputStream respIs = resp.getEntity().getContent();
                byte[] respBytes = IOUtils.toByteArray(respIs);
                result = new String(respBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


}

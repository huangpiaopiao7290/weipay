package com.weipay.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 微信支付配置类
 */
@Component("WeiPayConfig")
@ConfigurationProperties(prefix = "wxpay")
public class WeiPayConfig {

    private ResourceLoader resourceLoader;

    @Value("mch-id")
    private String mchId;

    @Value("mch-serial-no")
    private String mchSerialNo;

    // 商户私钥文件
    @Value("private-key-path")
    private String privateKeyPath;

    @Value("api-v3-key")
    private String apiV3Key;

    @Value("appid")
    private String appId;

    @Value("domain")
    private String domain;

    @Value("notify-domain")
    private String notifyDomain;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchSerialNo() {
        return mchSerialNo;
    }

    public void setMchSerialNo(String mchSerialNo) {
        this.mchSerialNo = mchSerialNo;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getApiV3Key() {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key) {
        this.apiV3Key = apiV3Key;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getNotifyDomain() {
        return notifyDomain;
    }

    public void setNotifyDomain(String notifyDomain) {
        this.notifyDomain = notifyDomain;
    }

    @Override
    public String toString() {
        return "WeiPayConfig{" +
                "mchId='" + mchId + '\'' +
                ", mchSerialNo='" + mchSerialNo + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", apiV3Key='" + apiV3Key + '\'' +
                ", appId='" + appId + '\'' +
                ", domain='" + domain + '\'' +
                ", notifyDomain='" + notifyDomain + '\'' +
                '}';
    }

    /**
     *  获取商户私钥文件
     * @param filePath 商户私钥文件路径
     * @return PrivateKey
     */
    public PrivateKey getPrivateKey(String filePath) {
//        Resource resource = resourceLoader.getResource(filePath);

//        try {
//            FileInputStream fileInputStream = new FileInputStream(filePath);
//            return PemUtil.loadPrivateKey(fileInputStream);
//        } catch (IOException e) {
//            throw new RuntimeException("私钥文件未找到", e);
//        }
        InputStream resourceAsStream = WeiPayConfig.class.getClassLoader().getResourceAsStream(filePath);
        return PemUtil.loadPrivateKey(resourceAsStream);

    }


//    @Bean("Verifier")
//    public ScheduledUpdateCertificatesVerifier getVerifier() {
//        System.out.println("获取签名验证器");
//        // 商户私钥
//        PrivateKey privateKey = getPrivateKey(privateKeyPath);
//        // 私钥签名对象
//        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
//        // 身份认证对象
//        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
//        // 使用定时更新的签名验证器，不需要传入证书
//        return new ScheduledUpdateCertificatesVerifier(wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
//    }

    /**
     * 获取签名验证器
     * @return verifier 验证器
     */
    @Bean("Verifier")
    public Verifier getVerifier() throws Exception {
        System.out.println("获取签名验证器");
        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 私钥签名对象
        PrivateKeySigner keySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        // 身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, keySigner);

        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();
        // 向证书管理器增加需要自动更新平台证书的商户信息
        certificatesManager.putMerchant(mchId, wechatPay2Credentials,
                apiV3Key.getBytes(StandardCharsets.UTF_8));
        // ... 若有多个商户号，可继续调用putMerchant添加商户信息
        Verifier verifier = certificatesManager.getVerifier(mchId);

        return verifier;
    }

    /**
     *  获取微信支付http请求
     * @param verifier 验证器
     * @return CloseableHttpClient
     */
    @Bean("WxPayClient")
    public CloseableHttpClient getWxPayClient(@Qualifier("Verifier") Verifier verifier) {
        System.out.println("获取HttpClient对象");
        // 获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 把密钥设置在httpclient里面
        WechatPayHttpClientBuilder wechatPayHttpClientBuilder = WechatPayHttpClientBuilder.create().
                withMerchant(mchId, mchSerialNo, privateKey).
                withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，可以通过builder设置各种参数，来配置你的HttpClient

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
        // 后面跟使用Apache HttpClient一样
        // CloseableHttpResponse response = httpClient.execute(...);
        return wechatPayHttpClientBuilder.build();
    }

    /**
     * 获取HttpClient，无需进行应答签名验证，跳过验签的流程
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient(){

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);

        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                //设置商户信息
                .withMerchant(mchId, mchSerialNo, privateKey)
                //无需进行签名验证、通过withValidator((response) -> true)实现
                .withValidator((response) -> true);

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();

        System.out.println("== getWxPayNoSignClient END ==");

        return httpClient;
    }

    /**
     * 获取支付回调请求处理器
     * @return
     */
    @Bean("WxPayNotificationRequest")
    public NotificationHandler notificationHandler(@Qualifier("Verifier") Verifier verifier) {
        NotificationHandler notificationHandler = new NotificationHandler(verifier, apiV3Key.getBytes(StandardCharsets.UTF_8));

        return notificationHandler;
    }

}

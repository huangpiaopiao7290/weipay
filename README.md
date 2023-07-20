# 微信支付

在微信商户平台申请注册。

需要准备： 如果你没有这些东西，那无法完成申请微信支付。 

1、微信公众号

2、微信商家账号

3、域名

4、营业执照

5、法人身份证

6、微信认证

## 配置信息

> AppID的获取：需要注册一个微信公众号，在商户平台中关联公众号的appID，得到的就是需要的第一个配置参数AppID。

> API密钥和APIv3密钥。（对称加密）

> 获取证书。（非对称加密）

> 证书序列号要与证书对应。

附件中的三份文件（证书pkcs12格式、证书pem格式、证书密钥pem格式）,为接口中强制要求时需携带的证书文件。
证书属于敏感信息，请妥善保管不要泄露和被他人复制。
不同开发语言下的证书格式不同，以下为说明指引：

- 证书pkcs12格式（apiclient_cert.p12）

  - 包含了私钥信息的证书文件，为p12(pfx)格式，由微信支付签发给您用来标识和界定您的身份，部分安全性要求较高的API需要使用该证书来确认您的调用身份，windows上可以直接双击导入系统，导入过程中会提示输入证书密码，证书密码默认为您的商户号（如：1900006031）

- 证书pem格式（apiclient_cert.pem）

  - 从apiclient_cert.p12中导出证书部分的文件，为pem格式，请妥善保管不要泄漏和被他人复制。部分开发语言和环境，不能直接使用p12文件，而需要使用pem。可以使用openssl命令来自己导出：

    ~~~
    openssl pkcs12 -clcerts -nokeys -in apiclient_cert.p12 -out apiclient_cert.pem
    ~~~

- 证书密钥pem格式（apiclient_key.pem）

  - 从apiclient_cert.p12中导出密钥部分的文件，为pem格式，部分开发语言和环境，不能直接使用p12文件可以使用openssl命令来自己导出：

    ~~~
    openssl pkcs12 -nocerts -in apiclient_cert.p12 -out apiclient_key.pem
    ~~~

- 由于绝大部分操作系统已内置了微信支付服务器证书的根CA证书,  2018年3月6日后, 不再提供CA证书文件（rootca.pem）下载.

> 平台证书的下载：【方式1】使用证书下载工具下载证书的物理文件；【方式2】API接口下载



~~~yaml
# 微信支付相关参数
# 商户号
wxpay.mch-id=1558950191
# 商户API证书序列号
wxpay.mch-serial-no=34345964330B66427E0D3D28826C4993C77E631F

# 商户私钥文件
wxpay.private-key-path=apiclient_key.pem
# APIv3密钥
wxpay.api-v3-key=UDuLFDcmy5Eb6o0nTNZdu6ek4DDh4K8B
# APPID
wxpay.appid=wx74862e0dfcf69954
# 微信服务器地址
wxpay.domain=https://api.mch.weixin.qq.com
# 接收结果通知地址
# 注意：每次重新启动ngrok，都需要根据实际情况修改这个配置
wxpay.notify-domain=https://500c-219-143-130-12.ngrok.io

# APIv2密钥
wxpay.partnerKey: T6m9iK73b0kn9g5v426MKfHQH7X8rKwb

~~~

## 常见问题

> 这里有两个微信的SDK包

[GitHub - wechatpay-apiv3/wechatpay-apache-httpclient: 微信支付 APIv3 Apache HttpClient装饰器（decorator）](https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient)

`官方文档中说0.4.0版本的不兼容0.3.0`

### 如何加载商户私钥

商户申请商户API证书时，会生成商户私钥，并保存在本地证书文件夹的文件`apiclient_key.pem`中。商户开发者可以使用方法`PemUtil.loadPrivateKey()`加载证书。

```java
# 示例：私钥存储在文件
PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
        new FileInputStream("/path/to/apiclient_key.pem"));

# 示例：私钥为String字符串
PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
        new ByteArrayInputStream(privateKey.getBytes("utf-8")));
```

### 如何下载平台证书？

使用`WechatPayHttpClientBuilder`需要调用`withWechatPay`设置[微信支付平台证书](https://wechatpay-api.gitbook.io/wechatpay-api-v3/ren-zheng/zheng-shu#ping-tai-zheng-shu)，而平台证书又只能通过调用[获取平台证书接口](https://wechatpay-api.gitbook.io/wechatpay-api-v3/jie-kou-wen-dang/ping-tai-zheng-shu#huo-qu-ping-tai-zheng-shu-lie-biao)下载。为了解开"死循环"，你可以在第一次下载平台证书时，按照下述方法临时"跳过”应答签名的验证。

```java
CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
  .withMerchant(merchantId, merchantSerialNumber, merchantPrivateKey)
  .withValidator(response -> true) // NOTE: 设置一个空的应答签名验证器，**不要**用在业务请求
  .build();
```

**注意**：业务请求请使用标准的初始化流程，务必验证应答签名。



[GitHub - wechatpay-apiv3/wechatpay-java: 微信支付 APIv3 的官方 Java Library](https://github.com/wechatpay-apiv3/wechatpay-java)

~~~java
如果是 SDK 尚未支持的接口，你可以使用 cipher 中的 RSAPrivacyEncryptor 和 RSAPrivacyDecryptor ，手动对敏感信息加解密。

// 微信支付平台证书中的公钥
PublicKey wechatPayPublicKey = null;
String plaintext = "";
PrivacyEncryptor encryptor = new RSAPrivacyEncryptor(wechatPayPublicKey);
String ciphertext = encryptor.encryptToString(plaintext);
// 商户私钥
PrivateKey merchantPrivateKey = null;
String ciphertext = "";
PrivacyDecryptor decryptor = new RSAPrivacyDecryptor(merchantPrivateKey);
String plaintext = decryptor.decryptToString(ciphertext);
RSAPrivacyEncryptorTest 和 RSAPrivacyDecryptorTest 中演示了如何使用以上函数做敏感信息加解密。
~~~





## 具体步骤

1、创建springboot项目。

2、引入swagger（接口文档和测试页面）。但是这里不引入了，这里直接用postman做测试。

3、定义统一结果（规范前后端数据交互格式）。

4、创建和连接数据库。

5、集成mybatis-plus.

6、搭建前端环境。

按照微信支付文档来

[产品能力概览 | 微信支付商户平台文档中心 (qq.com)](https://pay.weixin.qq.com/wiki/doc/apiv3/index.shtml)





在线学习平台的计费功能需求分析可以包括以下几个方面：

1. 购买课程功能：用户可以浏览课程列表，选择感兴趣的课程进行购买。购买流程应该简单明了，包括选择课程、确认订单、选择支付方式等步骤。
2. 订单管理：系统应该记录用户的购买订单信息，包括订单号、课程信息、购买时间、支付状态等。管理员和用户可以查看订单信息，方便跟踪订单状态和进行管理。
3. 观看权限控制：购买课程后，用户应该获得观看该课程的权限。系统需要对用户的观看权限进行管理，确保只有购买了课程的用户才能访问相关的学习内容。
4. 退款机制：对于不满意的课程，用户应该可以申请退款。系统需要提供退款申请功能，用户提交退款申请后，管理员可以审核并进行退款操作。退款流程需要确保用户的合理权益，同时也要避免滥用退款的情况发生。
5. 支付接口集成：在线学习平台需要集成支付接口，以便用户可以选择合适的支付方式进行购买课程。支付接口需要确保安全可靠，并且支持常见的支付方式，如支付宝、微信支付等。
6. 数据统计和报表：系统应该记录用户的购买行为和退款行为，并提供数据统计和报表功能。管理员可以通过报表分析用户的购买偏好和退款情况，为后续的课程调整和优化提供参考。

在设计计费功能时，需要注意用户体验和数据安全。用户购买和退款的流程应该简洁明了，系统应该保护用户的支付信息和个人隐私。同时，需要与相关法规和政策保持一致，确保计费功能的合法性和合规性。



### 1、创建springboot项目

springboot 2.7.7 + springmvc + mybatis-plus + mysql 5.7

前端：vue 2 + element ui

### 2、统一前后端数据交互格式

创建一个package：vo 

~~~java
package com.weipay.vo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 定义前后端数交互的统一格式
 */
public class R {

    private Integer code;                   // 响应码
    private String message;                 // 响应消息
    private Map<String, Object> data = new HashMap<>();       // 具体返回数据

    public R() {
    }

    public R(Integer code, String message, Map<String, Object> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        R r = (R) o;
        return Objects.equals(code, r.code) && Objects.equals(message, r.message) && Objects.equals(data, r.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     *  成功的响应
     * @return R 统一的响应格式
     */
    public static R ok() {

        R r = new R();
        r.setCode(2000);                // 成功的响应码2000
        r.setMessage("success");        // 成功的响应消息

        return r;
    }

    /**
     * 失败的响应
     * @return R
     */
    public static R error() {

        R r = new R();
        r.setCode(5000);                // 成功的响应码2000
        r.setMessage("unsuccessful");        // 成功的响应消息

        return r;
    }

    public R data(String key, Object value) {
        data.put(key, value);
        return this;
    }

}

~~~



### 3、创建表

> 表设计

- 订单表

~~~sql
CREATE TABLE `t_order_info` (
                                `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
                                `title` varchar(256) DEFAULT NULL COMMENT '订单标题',
                                `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
                                `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                                `product_id` bigint(20) DEFAULT NULL COMMENT '支付产品id',
                                `total_fee` int(11) DEFAULT NULL COMMENT '订单金额(分)',
                                `code_url` varchar(50) DEFAULT NULL COMMENT '订单二维码连接',
                                `order_status` varchar(10) DEFAULT NULL COMMENT '订单状态',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
~~~

- 支付表

~~~sql
CREATE TABLE `t_payment_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '支付记录id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `transaction_id` varchar(50) DEFAULT NULL COMMENT '支付系统交易编号',
  `payment_type` varchar(20) DEFAULT NULL COMMENT '支付类型',
  `trade_type` varchar(20) DEFAULT NULL COMMENT '交易类型',
  `trade_state` varchar(50) DEFAULT NULL COMMENT '交易状态',
  `payer_total` int(11) DEFAULT NULL COMMENT '支付金额(分)',
  `content` text COMMENT '通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
~~~

- 商品表

~~~sql
CREATE TABLE `t_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `title` varchar(20) DEFAULT NULL COMMENT '商品名称',
  `price` int(11) DEFAULT NULL COMMENT '价格（分）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

insert  into `t_product`(`title`,`price`) values ('Java课程',1);
insert  into `t_product`(`title`,`price`) values ('大数据课程',1);
insert  into `t_product`(`title`,`price`) values ('前端课程',1);
insert  into `t_product`(`title`,`price`) values ('UI课程',1);
~~~

- 退款表

~~~sql
CREATE TABLE `t_refund_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '退款单id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `refund_no` varchar(50) DEFAULT NULL COMMENT '商户退款单编号',
  `refund_id` varchar(50) DEFAULT NULL COMMENT '支付系统退款单号',
  `total_fee` int(11) DEFAULT NULL COMMENT '原订单金额(分)',
  `refund` int(11) DEFAULT NULL COMMENT '退款金额(分)',
  `reason` varchar(50) DEFAULT NULL COMMENT '退款原因',
  `refund_status` varchar(10) DEFAULT NULL COMMENT '退款状态',
  `content_return` text COMMENT '申请退款返回参数',
  `content_notify` text COMMENT '退款结果通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

~~~



### 4、mybatis配置

mapper目录下的定义的操作数库的接口，但是每个接口的xml配置文件默认情下并不会被maven打包。需要在pom.xml中添加

~~~xml
        <!--  项目打包的时候会将xml文件一同打包  -->
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
        </resources>
~~~

- mybatis-plus配置类

~~~java
package com.weipay.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatis-plus配置类
 */
@Configuration
@MapperScan("com.weipay.mapper")
@EnableTransactionManagement
public class MyBatisPlusConfig {
}

~~~



### 5、引入支付配置

> 基本配置

> 获取私钥

> 定时更新平台证书功能

~~~java
   /**
     *  获取签名验证器
     * @return verifier 验证器
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier() {
        System.out.println("获取签名验证器");
        // 商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        // 身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
        // 使用定时更新的签名验证器，不需要传入证书
        return new ScheduledUpdateCertificatesVerifier(wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
    }

    /***
     *  获取微信支付http请求
     * @param verifier 验证器
     * @return CloseableHttpClient
     */
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier) {
        System.out.println("获取HttpClient对象");
        // 获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 把密钥设置在httpclient里面，密钥又被封装到了verifier里面
        WechatPayHttpClientBuilder wechatPayHttpClientBuilder = WechatPayHttpClientBuilder.create().
                withMerchant(mchId, mchSerialNo, privateKey).
                withValidator(new WechatPay2Validator(verifier));
        // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
        // 后面跟使用Apache HttpClient一样
        // CloseableHttpResponse response = httpClient.execute(...);
        return wechatPayHttpClientBuilder.build();
    }
~~~



```
public abstract class CloseableHttpClient implements HttpClient, Closeable {
```

```
StandardCharsets      nio包下的
```



### 6、一些枚举类与工具类

- 在微信支付请求中需要许多参数，而有些参数是规定好的，我们可以直接把它们封装到枚举类中。

- 向微信支付平台发起支付请求时，最重要的一个参数就是订单号，它必须保证唯一性。将创建订单号封装成一个工具类`OrderNoUtil`。

- 在接收支付回调的时候，我们需要解析微信返回的http请求，将json参数转换成字符串

### 7、Native支付流程

整个流程时序图

![微信native下单流程图](images/微信native下单流程图.png)

#### 1)Native调起支付

```java
WxPayServiceImpl
```

请求的body参数：按照接口文档

[微信支付-开发者文档 (qq.com)](https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml)

#### 2)签名原理

[签名生成-接口规则 | 微信支付商户平台文档中心 (qq.com)](https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_0.shtml)

> 构造签名串：WechatPay2Credentials

~~~java
    /**
     *  版本0.3.0 
     *  获取签名验证器
     * @return verifier 验证器
     */
    @Bean("Verifier")
    public ScheduledUpdateCertificatesVerifier getVerifier() {
        System.out.println("获取签名验证器");
        // 商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 私钥签名对象
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, privateKey);
        // 身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);
        // 使用定时更新的签名验证器，不需要传入证书
        return new ScheduledUpdateCertificatesVerifier(wechatPay2Credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
    }

	/**
	 * 版本0.4.8 与0.3.0不兼容，不支持ScheduledUpdateCertificatesVerifier，用CertificatesManager代替
	 * 可以管理多个商户号
	 * -------------------
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


----------------------------具体实现原理----------------------------
    public final String getToken(HttpRequestWrapper request) throws IOException {
        String nonceStr = this.generateNonceStr();
        long timestamp = this.generateTimestamp();
        String message = this.buildMessage(nonceStr, timestamp, request);
        log.debug("authorization message=[{}]", message);
        SignatureResult signature = this.signer.sign(message.getBytes(StandardCharsets.UTF_8));
        String token = "mchid=\"" + this.getMerchantId() + "\",nonce_str=\"" + nonceStr + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + signature.certificateSerialNumber + "\",signature=\"" + signature.sign + "\"";
        log.debug("authorization token=[{}]", token);
        return token;
	}
~~~



> 计算签名值

绝大多数编程语言提供的签名函数支持对*签名数据*进行签名。强烈建议商户调用该类函数，使用商户私钥对*待签名串*进行SHA256 with RSA签名，并对签名结果进行*Base64编码*得到签名值.



> 设置http请求头

微信支付商户API v3要求请求通过`HTTP Authorization`头来传递签名。` Authorization`由*认证类型*和*签名信息*两个部分组成。

~~~
具体组成为：

1.认证类型，目前为WECHATPAY2-SHA256-RSA2048

2.签名信息

- 发起请求的商户（包括直连商户、服务商或渠道商）的商户号` mchid`
- [商户API证书](https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay3_1.shtml)`序列号serial_no`，用于[声明所使用的证书](https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay3_1.shtml#part-3)
- 请求随机串`nonce_str`
- 时间戳`timestamp`
- 签名值`signature`
~~~



- 签名验证

> 构造验签名串

~~~java
HTTP头Wechatpay-Timestamp 中的应答时间戳。
HTTP头Wechatpay-Nonce 中的应答随机串。
应答主体（response Body），就是微信返回的code_url，需要按照接口返回的顺序进行验签，错误的顺序将导致验签失败。
    
    
格式：
应答时间戳\n
应答随机串\n
应答报文主体\n
~~~

com.github.wechatpay-apiv3:wechatpay-apache-httpclient0.3.0 > auth > WechatPay2Validator

~~~java
    protected final String buildMessage(CloseableHttpResponse response) throws IOException {
        String timestamp = response.getFirstHeader("Wechatpay-Timestamp").getValue();
        String nonce = response.getFirstHeader("Wechatpay-Nonce").getValue();
        String body = this.getResponseBody(response);
        return timestamp + "\n" + nonce + "\n" + body + "\n";
    }
~~~



> 获取应答签名

微信支付的应答签名通过HTTP头`Wechatpay-Signature`传递。

对` Wechatpay-Signature`的字段值使用Base64进行解码，得到应答签名。



> 验证签名

先从微信验证平台的平台证书中导出平台公钥，然后用公钥对上面得到的应答签名进行摘要运算。

用sha256对签名串进行摘要运算。

然后将这两个进行比对，一样就证明签名验证成功

~~~java
            // todo: 签名验证
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
            Notification notification = null;

            try {

                /*
                 * 使用微信支付回调请求处理器解析构造的微信请求体
                 * 在这个过程中会进行签名验证，并解密加密过的内容
                 * 签名源码：  com.wechat.pay.contrib.apache.httpclient.cert; 271行开始
                 * 解密源码：  com.wechat.pay.contrib.apache.httpclient.notification 76行
                 *           com.wechat.pay.contrib.apache.httpclient.notification 147行 使用私钥获取AesUtil
                 *           com.wechat.pay.contrib.apache.httpclient.notification 147行 使用Aes对称解密获得原文
                 */
                notification = notificationHandler.parse(wxRequest);
                System.out.println(notification.getDecryptData());
            } catch (Exception e) {
                System.out.println("通知验签失败");
                //失败应答
                response.setStatus(500);
            }
~~~





#### 3)发起支付请求

> 创建订单，并保存到数据库

这里存在一个小问题，如果用户发起了支付请求，即会在后台创建一个订单，当该用户中途取消支付，并重复发起支付请求操作，那么不做任何处理的t_order_info表，将数据爆炸和污染。

所以在创建订单之前，做一次查询操作。

~~~java
    /**
     * 查找已存在但未支付的订单
     * @param productId 商品ID
     * @return OrderInfo 订单信息
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status",OrderStatus.NOTPAY);
        // 实际情况还需要判断是哪个用户的订单
        // queryWrapper.eq("user_id", userId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        return orderInfo;
    }
    /**
     * 根据商品ID创建订单
     * @param productId 商品ID
     * @return OrderInfo 订单信息
     */
    @Override
    public OrderInfo createOrderByProductId(Long productId) {

        // 查找已存在该商品的订单但还没支付
        OrderInfo noPayOrderByProductId = this.getNoPayOrderByProductId(productId);
        if (noPayOrderByProductId != null) {
            return noPayOrderByProductId;
        }
        // 获取商品信息
        Product product = productMapper.selectById(productId);
        // 生成订单
        OrderInfo order = new OrderInfo();
        order.setTitle(product.getTitle());                     // 订单名称
        order.setOrderNo("123456789");                          // 订单编号
        order.setProductId(productId);                          // 订单商品编号
        order.setTotalFee(product.getPrice());                  // 订单金额
        order.setOrderStatus(OrderStatus.NOTPAY.getType());     // 订单状态
        // 将订单写入订单表中
        orderInfoMapper.insert(order);

        return order;
    }
~~~

还可以将code_url存储起来（官方默认有效期为两个小时）：redis







目前还需要把前端数据传回来（就是一个product_id）



> 前端收到code_url

type check failed for prop "value". Expected String, got Undefined 

这里转换成二维码展示，扫码之后就卡住了

<div style="background-color:pink">这里axios获取响应确实拿到了code_url，但是赋值给data中属性时赋值失败</div>

目前已解决





#### 4)回调通知♥

> #### 注意：
>
> • 同样的通知可能会多次发送给商户系统。**商户系统必须能够正确处理重复的通知**。 推荐的做法是，当商户系统收到通知进行处理时，先检查对应业务数据的状态，并判断该通知是否已经处理。如果未处理，则再进行处理；如果已处理，则直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
>
> • 如果在所有通知频率后没有收到微信侧回调，商户应调用查询订单接口确认订单状态。
>
> **特别提醒：**商户系统对于开启结果通知的内容一定要做签名验证，并校验通知的信息是否与商户侧的信息一致，防止数据泄露导致出现“假通知”，造成资金损失。





首先要保证微信能访问到当前主机

> 通知规则

用户支付完成后，微信会把相关支付结果和用户信息发送给商户，商户需要接收处理该消息，并返回应答。

对后台通知交互时，如果微信收到商户的应答不符合规范或超时，微信认为通知失败，微信会通过一定的策略定期重新发起通知，尽可能提高通知的成功率，但微信不保证通知最终能成功。

（通知频率为15s/15s/30s/3m/10m/20m/30m/30m/30m/60m/3h/3h/3h/6h/6h - 总计 24h4m）

虽然官方文档上写了通知频率如上，但实际支付回调时会有并发问题，同一时间多次回调，可能第一次的回调还未处理完，相同的回调又来了。



> 通知报文

- 支付结果通知是以POST 方法访问商户设置的通知url，通知的数据以JSON 格式通过请求主体（BODY）传输。通知的数据包括了加密的支付结果详情。
  （注：由于涉及到回调加密和解密，商户必须先设置好apiv3秘钥后才能解密回调通知，apiv3秘钥设置文档指引详见[APIv3秘钥设置指引](https://kf.qq.com/faq/180830E36vyQ180830AZFZvu.html)）
- 微信多台服务器并行发送通知的，相同通知会在短时间内（几秒之内）多次发送



> 参数验证、解密

1、用商户平台上设置的APIv3密钥【[微信商户平台](https://pay.weixin.qq.com/)—>账户设置—>API安全—>设置APIv3密钥】，记为key；

2、针对resource.algorithm中描述的算法（目前为AEAD_AES_256_GCM），取得对应的参数nonce和associated_data；

3、使用key、nonce和associated_data，对数据密文resource.ciphertext进行解密，得到JSON形式的资源对象；

**注：** AEAD_AES_256_GCM算法的接口细节，请参考[rfc5116](https://datatracker.ietf.org/doc/html/rfc5116)。微信支付使用的密钥key长度为32个字节，随机串nonce长度12个字节，associated_data长度小于16个字节并可能为空字符串。

~~~java
                /*
                 * 使用微信支付回调请求处理器解析构造的微信请求体
                 * 在这个过程中会进行签名验证，并解密加密过的内容
                 * 签名源码：  com.wechat.pay.contrib.apache.httpclient.cert; 271行开始
                 * 解密源码：  com.wechat.pay.contrib.apache.httpclient.notification 76行
                 *           com.wechat.pay.contrib.apache.httpclient.notification 147行 使用私钥获取AesUtil
                 *           com.wechat.pay.contrib.apache.httpclient.notification 147行 使用Aes对称解密获得原文
                 */
~~~



> 通知签名

加密不能保证通知请求来自微信。微信会对发送给商户的通知进行签名，并将签名值放在通知的HTTP头Wechatpay-Signature。商户应当验证签名，以确认请求来自微信，而不是其他的第三方。签名验证的算法请参考 [《微信支付API v3签名验证》](https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml)。

首先，商户先从应答中获取以下信息。

- HTTP头`Wechatpay-Timestamp` 中的应答时间戳。
- HTTP头`Wechatpay-Nonce` 中的应答随机串。
- 应答主体（response Body），需要按照接口返回的顺序进行验签，错误的顺序将导致验签失败。

然后，请按照以下规则构造应答的验签名串。签名串共有三行，行尾以`\n` 结束，包括最后一行。`\n`为换行符（ASCII编码值为0x0A）。若应答报文主体为空（如HTTP状态码为`204 No Content`），最后一行仅为一个`\n`换行符。

~~~java
应答时间戳\n
应答随机串\n
应答报文主体\n
~~~

[gdeg名验证-接口规则 | 微信支付商户平台文档中心 (qq.com)](https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml):签名验证

- 在0.3.0的版本中，需要手动写异步回调时微信返回的通知请的验签。

~~~java
// 手写通知请求的验签
改造wechatPay2Validator方法
这里用的是weichatpay-httpclient0.4.8自带的处理request验签功能
~~~



- 版本>=`0.4.2`可使用 `NotificationHandler.parse(request)` 对回调通知验签和解密：
  - 使用`NotificationRequest`构造一个回调通知请求体，需设置应答平台证书序列号、应答随机串、应答时间戳、应答签名串、应答主体。
  - 使用`NotificationHandler`构造一个回调通知处理器，需设置验证器、apiV3密钥。调用`parse(request)`得到回调通知`notification`。

~~~java
// 构建request，传入必要参数
 NotificationRequest request = new NotificationRequest.Builder().withSerialNumber(wechatPaySerial)
        .withNonce(nonce)
        .withTimestamp(timestamp)
        .withSignature(signature)
        .withBody(body)
        .build();
NotificationHandler handler = new NotificationHandler(verifier, apiV3Key.getBytes(StandardCharsets.UTF_8));
// 验签和解析请求体
Notification notification = handler.parse(request);
// 从notification中获取解密报文
System.out.println(notification.getDecryptData());
~~~

这里可以看出请求参数

~~~java
通知id：8aba0534-0419-5a7b-b839-4422ca20407a
完整数据(未解密)：{"id":"8aba0534-0419-5a7b-b839-4422ca20407a",
      "create_time":"2023-04-03T16:55:33+08:00",
      "resource_type":"encrypt-resource",
      "event_type":"TRANSACTION.SUCCESS",
      "summary":"支付成功",
      "resource":{"original_type":"transaction",
                  "algorithm":"AEAD_AES_256_GCM",
                  "ciphertext":"fXxcqH802rbam/MGtVForRaeLi9r1KGaPsG8dP7p8JwuXWYJOCwfuL/fd/0n5HfAJtgXax1fmxyDAp4jLNS/A68Yf2nhN2S0UQUaiKRKQ2y8wbYxqVd3v+hJEh3ud2nTvbj0nUwnso6yBUfcS0EEnYaxrpgSbsr8KN1K8y+IcWzS8Ji5cv61EOemnHi1vIl4SF6D8v0c5szskZxpRyG7bKfalSV3/wsBPRJI5BdeR293M/fMVUcfsWX5Zq38HbJXAlrItgaGCVALvw6hYePkP/PJB4vK9cH9Udt4MGJDHwjUEZQNSQxJfE+NCLNsm4MsiJ9olyPl9KLPVJW4puMxNCiF2oTolUGf6cJwLj8XkRK6dMAXfEDUVKk9AmLQqUXFCgwWR9hmra21+S2EpQUY675gVawwZ6+zvOMfTpMh8p2s6/uwhFdpBcAfgcuXHhszLO7beyetyNkV8dsKfz98pf2oxwVBuijFrkcbMa4U/6pyd+SR5k1gXjcHgoMB0Ov2vv9GO/KmbefwjuXfbvvAo151g8Z6G3dvXRbKBmgq2WH7uOKrD1DW5B7J2vKx1zVoRUtDYTMoYA==",
                  "associated_data":"transaction",
                  "nonce":"FfFCX0z0Bvtf"}}
验签解密成功：{"mchid":"1558950191",
      "appid":"wx74862e0dfcf69954",
      "out_trade_no":"ORDER_20230403165513779",
      "transaction_id":"4200001761202304031254529083",
      "trade_type":"NATIVE",
      "trade_state":"SUCCESS",
      "trade_state_desc":"支付成功",
      "bank_type":"OTHERS",
      "attach":"",
      "success_time":"2023-04-03T16:55:33+08:00",
      "payer":{"openid":"oHwsHuPcXUWqmUHbYkY22vfJyfaU"},
      "amount":{"total":1,"payer_total":1,"currency":"CNY","payer_currency":"CNY"}}

~~~

##### 注意事项

- 取出回调的请求头
- 注意返回的参数的顺序
- 在获取amount（支付金额信息）里面的payer_total时，存在一个类型转换问题
- 在支付回调接口那里存在一个微信重复通知的问题。在本小节前面部分也重点提到过，虽然不做处理对订单状态的更新结果没有影响，但是支付日志会重复记录



> 通知应答

**接收成功：**HTTP应答状态码需返回200或204，无需返回应答报文。

**接收失败：**HTTP应答状态码需返回5XX或4XX，同时需返回应答报文。

~~~java
接收失败时
{  
    "code": "FAIL",
    "message": "失败"
}
~~~



> 回调通知注意事项

- `notify_url`需要填写商户自己系统的真实地址
- `notify_url`必须是以https://或http://开头的完整全路径地址，并且确保url中的域名和IP是外网可以访问的，不能填写localhost、127.0.0.1、192.168.x.x等本地或内网IP。

- `notify_url`不能携带参数。

- `notify_url`的代码处理逻辑不能做登录态校验。

- 商户系统收到支付结果通知，需要在5秒内返回应答报文，否则微信支付认为通知失败，后续会重复发送通知。

- 同样的通知可能会多次发送给商户系统，商户系统必须能够正确处理重复的通知。如果已处理过，直接给微信支付返回成功。

- 商户侧对微信支付回调IP有防火墙策略限制的，需要对以下IP段开通白名单:。。。。





#### 5) 查询订单

商户可以通过查询订单接口主动查询订单状态，完成下一步的业务逻辑。查询订单状态可通过微信支付订单号或商户订单号两种方式查询

> #### 注意：
>
> 查询订单可通过[微信支付订单号](https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_2.shtml#menu1)和[商户订单号](https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_2.shtml#menu2)两种方式查询，两种查询方式返回结果相同
>
> **需要调用查询接口的情况**：
>
> • 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知。
>
> • 调用支付接口后，返回系统错误或未知交易状态情况。
>
> • 调用付款码支付API，返回USERPAYING的状态。
>
> • 调用关单或撤销接口API之前，需确认支付状态。

- `微信支付订单号查询`



- `商户订单号查询`

~~~java
适用对象： 直连商户
请求URL： https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}
请求方式：GET
示例：    
https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/1217752501201407033233368018?mchid=1230000109
~~~

> 请求参数

| 参数名     | 变量         | 类型[长度限制] | 必填 | 描述                                                         |
| :--------- | :----------- | :------------- | :--- | :----------------------------------------------------------- |
| 直连商户号 | mchid        | string[1,32]   | 是   | query 直连商户的商户号，由微信支付生成并下发。 示例值：1230000109 |
| 商户订单号 | out_trade_no | string[6,32]   | 是   | path 商户系统内部订单号，只能是数字、大小写字母_-*且在同一个商户号下唯一。 特殊规则：最小字符长度为6 示例值：1217752501201407033233368018 |

> 返回参数

必要的信息



~~~java
    /** controller
     * 查询订单  在支付回调出现异常时主动向微信服务器发送查询订单结果请求
     * @return
     */
    @GetMapping("/query/{orderNo}")
    public R queryOrder(@PathVariable String orderNo) throws Exception {
        System.out.println("查询订单");
        // 查询结果
        String queryResult = wxPayService.queryOrder(orderNo);
        return R.ok().setMessage("查询成功").data("queryOrder", queryResult);
    }
------------------------------------------
     /** service.impl
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

~~~



- 回调通知解密之后的订单信息与直接向微信发送查询请求的信息		一致的

~~~java
http://localhost:7290/api/wx-pay/query/ORDER_20230408144355425

{
    "mchid":"1558950191",
    "appid":"wx74862e0dfcf69954",
    "out_trade_no":"ORDER_20230403165513779",
    "transaction_id":"4200001761202304031254529083",
    "trade_type":"NATIVE",
    "trade_state":"SUCCESS",
    "trade_state_desc":"支付成功",
    "bank_type":"OTHERS",
    "attach":"",
    "success_time":"2023-04-03T16:55:33+08:00",
    "payer":{
        "openid":"oHwsHuPcXUWqmUHbYkY22vfJyfaU"
    },
    "amount":{
        "total":1,
        "payer_total":1,
        "currency":"CNY",
        "payer_currency":"CNY"
    }
}

--------------------------------------------------------------

"{
    "amount\":{
        "currency\":\"CNY\",
        "payer_currency\":\"CNY\",
        "payer_total\":1,
        "total\":1
    },
    "appid\":\"wx74862e0dfcf69954\",
    "attach\":\"\",
    "bank_type\":\"OTHERS\",
    "mchid\":\"1558950191\",
    "out_trade_no\":\"ORDER_20230408144355425\",
    "payer\":{
    	"openid\":\"oHwsHuPcXUWqmUHbYkY22vfJyfaU\"
		},
    "promotion_detail\":[],
    "success_time":"2023-04-08T14:44:14+08:00",
    "trade_state":"SUCCESS",
    "trade_state_desc":"支付成功",
    "trade_type":"NATIVE",
    "transaction_id":"4200001736202304084431828716"
}"
    
~~~





#### 6)取消/关闭订单

以下情况需要调用关单接口：
1、商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
2、系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。

> #### 注意：
>
> • 关单没有时间限制，建议在订单生成后间隔几分钟（最短5分钟）再调用关单接口，避免出现订单状态同步不及时导致关单失败。

~~~java
接口说明
适用对象： 直连商户
请求URL： https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{out_trade_no}/close
请求方式： POST
    
请求参数：
    直连商户号：mchid
    商户订单号：out_trade_no
    
返回示例：
    无数据（Http状态码为204）

~~~

代码实现主要三个步骤，前端发送取消订单请求=》调用关单接口=》更新订单状态“已取消”

~~~java
    /**
     * 取消订单
     * @param orderNo 订单编号
     * @return
     * @throws Exception
     */
    @PostMapping("/cancel/{orderNo}")
    public R cancel(@PathVariable String orderNo) throws Exception {
        System.out.println("取消还未支付的订单");
        wxPayService.cancelOrder(orderNo);
        return R.ok().setMessage("该订单已取消");
    }

-----------------------------------------------------
    
     /**
     * 取消订单
     * @param orderNo 订单号
     * @throws Exception e
     */
    @Override
    public void cancelOrder(String orderNo) throws Exception {
        // 调用微信支付的关单接口
        String closeOrderUrl = weiPayConfig.getDomain().concat(String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo));
        HttpPost httpPost = new HttpPost(closeOrderUrl);
        // 组装请求体
        Gson gson = new Gson();
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", weiPayConfig.getMchId());
        // 将请求参数转换成json字符串
        String reqdata = gson.toJson(paramsMap);

        System.out.println("关闭订单请求参数=" + reqdata);

        StringEntity entity = new StringEntity(reqdata,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        //完成签名并执行请求
    	CloseableHttpResponse response = wxPayClient.execute(httpPost)
        // 处理响应
        try {
            //响应体
            String bodyString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) { //处理成功
                System.out.println("订单取消成功");
            } else {
                System.out.println("失败,响应状态码 = " + statusCode);
                throw new IOException("订单取消失败");
            }
        }

        // 更新订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }
~~~

#### 7) 定时任务

> ​    定时查询未支付且间隔创建时间超过5min的订单
>
> - spring的定时任务
> - sql
> - 为什么需要定时任务？
>   - 正常的流程是在用户发起下单到付款一气呵成，与之相反的是在商家后台返回支付二维码时用户未扫码付款，或者在用户扫码付款这段时间内商家后台出现某些问题导致无法直接获取支付回调通知。此时需要商家主动向微信服务器查询订单表中未支付订单的真实状态，如果已支付就更新订单状态，超时就取消该订单。

---------------------------------------------

```java
// 引入定时任务，在spring启动类中加上定时的注解
@EnableScheduling

----------------------------------------------
/**
 * 定时查单
 */
@Component("WxPayTask")
public class WxPayTask {

    @Resource(name = "OrderInfoServiceImpl")
    private OrderInfoService orderInfoService;

    @Resource(name = "WxPayServiceImpl")
    private WxPayService wxPayService;

    /**
     * 每隔60s查询 超过创建时间5min的未支付订单的状态
     * 秒分时日月年
     * 每4min查询一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws Exception {
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(1);
        if (orderInfoList != null && !orderInfoList.isEmpty()) {
            // 提取订单号
            for (OrderInfo orderInfo : orderInfoList) {
                String orderNo = orderInfo.getOrderNo();
                System.out.println("未支付且超过5min的订单号=" + orderNo);
                // 核实订单状态  调用微信查单接口
                wxPayService.checkOrderStatus(orderNo);
            }
        }
    }

}
----------------------------------------------
/**
 * 查询从创建时间开始间隔时间超过指定时间未支付的订单
 * @param minutes 时间间隔 单位 min
 * @return
 */
@Override
public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
    // 系统5min之前
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime before = now.minusMinutes(5);
    // 定义格式
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String scheduleTime = dateTimeFormatter.format(before);
    System.out.println("schedule_time:" + scheduleTime);

    QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
    // 查询条件  未支付 且 超过minutes 分钟的订单
    queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
    // schedule_time：当前时间减去5min, 查询create_time < schedule_time的订单
    queryWrapper.le("create_time", scheduleTime);

    List<OrderInfo> orderInfoList = orderInfoMapper.selectList(queryWrapper);

    return orderInfoList;
}


---------------------------------------------
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
```
![定时任务](images/定时任务.png)



#### 7)退款

当交易发生之后一年内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付金额退还给买家，微信支付将在收到退款请求并且验证成功之后，将支付款按原路退还至买家账号上。

7-1）申请退款

> ## 申请退款API
>
> 1、交易时间超过一年的订单无法提交退款
>
> 2、微信支付退款支持单笔交易分多次退款（不超50次），多次退款需要提交原支付订单的商户订单号和设置不同的退款单号。申请退款总金额不能超过订单金额。 一笔退款失败后重新提交，请不要更换退款单号，请使用原商户退款单号
>
> 3、错误或无效请求频率限制：6qps，即每秒钟异常或错误的退款申请请求不超过6次
>
> 4、每个支付订单的部分退款次数不能超过50次
>
> 5、如果同一个用户有多笔退款，建议分不同批次进行退款，避免并发退款导致退款失败
>
> 6、申请退款接口的返回仅代表业务的受理情况，具体退款是否成功，需要通过退款查询接口获取结果
>
> 7、一个月之前的订单申请退款频率限制为：5000/min
>
> 8、同一笔订单多次退款的请求需相隔1分钟
>
> ## 接口说明
>
> 请求URL：https://api.mch.weixin.qq.com/v3/refund/domestic/refunds
>
> 请求方式：POST
>
> 接口频率：150qps
>
> 请求示例：
>
> ~~~java
> {
>   "transaction_id": "1217752501201407033233368018",
>   "out_refund_no": "1217752501201407033233368018",
>   "reason": "商品已售完",
>   "notify_url": "https://weixin.qq.com",
>   "funds_account": "AVAILABLE",
>   "amount": {
>     "refund": 888,
>     "from": [
>       {
>         "account": "AVAILABLE",
>         "amount": 444
>       }
>     ],
>     "total": 888,
>     "currency": "CNY"
>   },
>   "goods_detail": [
>     {
>       "merchant_goods_id": "1217752501201407033233368018",
>       "wechatpay_goods_id": "1001",
>       "goods_name": "iPhone6s 16G",
>       "unit_price": 528800,
>       "refund_amount": 528800,
>       "refund_quantity": 1
>     }
>   ]
> }
> ~~~
>
> 返回示例：
>
> ~~~java
> {
>   "refund_id": "50000000382019052709732678859",
>   "out_refund_no": "1217752501201407033233368018",
>   "transaction_id": "1217752501201407033233368018",
>   "out_trade_no": "1217752501201407033233368018",
>   "channel": "ORIGINAL",
>   "user_received_account": "招商银行信用卡0403",
>   "success_time": "2020-12-01T16:18:12+08:00",
>   "create_time": "2020-12-01T16:18:12+08:00",
>   "status": "SUCCESS",
>   "funds_account": "UNSETTLED",
>   "amount": {
>     "total": 100,
>     "refund": 100,
>     "from": [
>       {
>         "account": "AVAILABLE",
>         "amount": 444
>       }
>     ],
>     "payer_total": 90,
>     "payer_refund": 90,
>     "settlement_refund": 100,
>     "settlement_total": 100,
>     "discount_refund": 10,
>     "currency": "CNY"
>   },
>   "promotion_detail": [
>     {
>       "promotion_id": "109519",
>       "scope": "SINGLE",
>       "type": "DISCOUNT",
>       "amount": 5,
>       "refund_amount": 100,
>       "goods_detail": [
> 	    {
> 			"merchant_goods_id": "1217752501201407033233368018",
> 			"wechatpay_goods_id": "1001",
> 			"goods_name": "iPhone6s 16G",
> 			"unit_price": 528800,
> 			"refund_amount": 528800,
> 			"refund_quantity": 1
> 		}
>       ]
>     }
>   ]
> }
> ~~~
>

- 退款请求必要的参数

| 参数名          | 变量           | 类型[长度限制] | 必填   | 描述                                                         |
| :-------------- | :------------- | :------------- | :----- | :----------------------------------------------------------- |
| 微信支付订单号  | transaction_id | string[1, 32]  | 二选一 | body原支付交易对应的微信订单号 示例值：1217752501201407033233368018 |
| 商户订单号      | out_trade_no   | string[6, 32]  |        | body原支付交易对应的商户订单号 示例值：1217752501201407033233368018 |
| 商户退款单号    | out_refund_no  | string[1, 64]  | 是     | body商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-\|*@ ，同一退款单号多次请求只退一笔。 示例值：1217752501201407033233368018 |
| 退款原因        | reason         | string[1, 80]  | 否     | body若商户传入，会在下发给用户的退款消息中体现退款原因 示例值：商品已售完 |
| 退款结果回调url | notify_url     | string[8, 256] | 否     | body异步接收微信支付退款结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。 如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效，优先回调当前传的这个地址。 示例值：https://weixin.qq.com |
| 退款金额        | refund         | int            | 是     | 退款金额，单位为分，只能为整数，不能超过原订单支付金额。 示例值：888 |
| 原订单金额      | total          | int            | 是     | 原支付交易的订单总金额，单位为分，只能为整数。 示例值：888   |
| 退款币种        | currency       | string[1, 16]  | 是     | 符合ISO 4217标准的三位字母代码，目前只支持人民币：CNY。 示例值：CNY |



~~~java
支付记录表
1,
ORDER_20230408144355425,
4200001736202304084431828716,
微信,
NATIVE,
SUCCESS,
1,
"{transaction_id=4200001736202304084431828716, amount={total=1.0, payer_total=1.0, currency=CNY, payer_currency=CNY}, mchid=1558950191, out_trade_no=ORDER_20230408144355425, trade_state=SUCCESS, bank_type=OTHERS, appid=wx74862e0dfcf69954, trade_state_desc=支付成功, trade_type=NATIVE, attach=, success_time=2023-04-08T14:44:14+08:00, payer={openid=oHwsHuPcXUWqmUHbYkY22vfJyfaU}}",
2023-04-08 14:44:14,
2023-04-08 14:44:14
---------------------------------------------
订单表
1,
不好吃的汉堡,
ORDER_20230408144355425,
,
1,
1,
weixin://wxpay/bizpayurl?pr=PWOmXCUzz,
支付成功,
2023-04-08 14:43:55,
2023-04-08 14:44:14

~~~



##### 7-2）退款结果通知（异步回调）

退款状态改变后，微信会把相关退款结果发送给商户。

> 对后台通知交互时，如果微信收到应答不是成功或超时，微信认为通知失败，微信会通过一定的策略定期重新发起通知，尽可能提高通知的成功率，但微信不保证通知最终能成功
>
> - 同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。 推荐的做法是，当商户系统收到通知进行处理时，先检查对应业务数据的状态，并判断该通知是否已经处理。如果未处理，则再进行处理；如果已处理，则直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
>
> - 如果在所有通知频率后没有收到微信侧回调。商户应调用查询订单接口确认订单状态。
>
> **特别提醒：**商户系统对于开启结果通知的内容一定要做签名验证，并校验通知的信息是否与商户侧的信息一致，防止数据泄露导致出现“假通知”，造成资金损失。

类似支付回调的流程

也需要验签和解密

退款通知回调参数：

~~~java
{
    "id":"EV-2018022511223320873",
    "create_time":"2018-06-08T10:34:56+08:00",
    "resource_type":"encrypt-resource",
    "event_type":"REFUND.SUCCESS",
    "summary":"退款成功",
    "resource" : {
        "original_type": "refund",
        "algorithm":"AEAD_AES_256_GCM",
        "ciphertext": "...",
        "associated_data": "",
        "nonce": "..."
    }
}
~~~

商户对resource对象进行解密后，得到的资源对象示例：

~~~java
{
    "mchid": "1900000100",
    "transaction_id": "1008450740201411110005820873",
    "out_trade_no": "20150806125346",
    "refund_id": "50200207182018070300011301001",
    "out_refund_no": "7752501201407033233368018",
    "refund_status": "SUCCESS",
    "success_time": "2018-06-08T10:34:56+08:00",
    "user_received_account": "招商银行信用卡0403",
    "amount" : {
        "total": 999,
        "refund": 999,
        "payer_total": 999,
        "payer_refund": 999
    }
}
~~~

接收到微信的退款回调之后需要告诉微信（通知应答）

**接收成功：**HTTP应答状态码需返回200或204，无需返回应答报文。

**接收失败：**HTTP应答状态码需返回5XX或4XX，同时需返回应答报文，格式如下：

**注意：**重试过多会导致微信支付端积压过多通知而堵塞，影响其他正常通知。

| 参数名     | 变量    | 类型[长度限制] | 必填 | 描述                                                        |
| :--------- | :------ | :------------- | :--- | :---------------------------------------------------------- |
| 返回状态码 | code    | string[1,32]   | 是   | 错误码，SUCCESS为接收成功，其他错误码为失败 示例值：SUCCESS |
| 返回信息   | message | string[1,256]  | 否   | 返回信息，如非空，为错误原因 示例值：系统错误               |

~~~java
{   
    "code": "SUCCESS"
}
~~~



> 代码示例

~~~java

退款通知(未解密)=========> 
{"id":"e5e66e42-f05f-5c21-9421-d82e49387717",
 "create_time":"2023-04-12T12:15:06+08:00",
 "resource_type":"encrypt-resource",
 "event_type":"REFUND.SUCCESS",
 "summary":"退款成功",
 "resource":{"original_type":"refund",
             "algorithm":"AEAD_AES_256_GCM",
             "ciphertext":"3WyN+6fwreP05DbtOmhjIp99vvSEYkGBDwH4bcfSqIb0F9AZcNpEgfqP9bgYdCDavz7B8ho9JNEE8jixHXWfkWG5/y7dLofpGRi8hBUkygUiTK5OBjWKOUg/dMVqWfS/4yB2fBkTJ0nGQmMNsMvc/eMo066PuKryR1dPF10M0DVMRc1BLSO5136gKy47u3nZKMaFhZy3zO+tBKIRWc4kSKjxkg2pfnck0Aq9Nr0JIrs6qlXknVyvHaZaTbNWcPvQ8wS7PQtDj9y5F8BUP8nuWJjAGqJoN+CqQcYrNR7wW7k30naIjcyIWW6FWiYPUx9IR+SD/4Rrql5VIia6bx43FokRWeBAE0Lu6DVyfSHFfjVZ7O5qXeLm6P0Ud2d+BIcsSEREyez8n6DMAg7rnd8NZnGBY7mWN918L6k+p7+Ns/eeIgdi15kvFTg2W/xk0f0ys5FBnUzAV1CeSwSbEMyxj3iqs2KDHUirUIUXFo3lkmXAn7s5kX3gCoxtmQaXGdoY1PtOAC5K680=",
             "associated_data":"refund",
             "nonce":"eObYf1re2IZ0"}}

退款通知验签并解密的结果=======>
{"mchid":"1558950191",
 "out_trade_no":"ORDER_20230408144355425",
 "transaction_id":"4200001736202304084431828716",
 "out_refund_no":"REFUND20230412121456353",
 "refund_id":"50301405352023041233178680856",
 "refund_status":"SUCCESS",
 "success_time":"2023-04-12T12:15:06+08:00",
 "amount":{"total":1,
           "refund":1,
           "payer_total":1,
           "payer_refund":1},
 "user_received_account":"支付用户零钱"}

~~~

  



	at com.weipay.service.impl.RefundInfoServiceImpl.updateRefund(RefundInfoServiceImpl.java:68) ~[classes/:na]
	at com.weipay.service.impl.WxPayServiceImpl.refund(WxPayServiceImpl.java:331) ~[classes/:na]
	at com.weipay.controller.WeiPayController.refund(WeiPayController.java:146) ~[classes/:na]


​	
​	出问题的方法：（初步认为）
​	bodyMap.toString()与gson.toJson(bodyMap)格式不一样









##### 7-3）查询单笔退款（主动查询）

提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，**建议在提交退款申请后1分钟发起查询退款状态**，一般来说零钱支付的退款5分钟内到账，银行卡支付的退款1-3个工作日到账。

> 适用对象：直连商户
>
> 请求URL：https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/{out_refund_no}
>
> 请求方式：GET
>
> 请求示例：
>
> ~~~java
> https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/1217752501201407033233368018
> ~~~
>
> 

- 请求参数

| 参数名       | 变量          | 类型[长度限制] | 必填 | 描述                                                         |
| :----------- | :------------ | :------------- | :--- | :----------------------------------------------------------- |
| 商户退款单号 | out_refund_no | string[1, 64]  | 是   | path商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-\|*@ ，同一退款单号多次请求只退一笔。 示例值：1217752501201407033233368018 |

- 返回示例

~~~java
{
  "refund_id": "50000000382019052709732678859",
  "out_refund_no": "1217752501201407033233368018",
  "transaction_id": "1217752501201407033233368018",
  "out_trade_no": "1217752501201407033233368018",
  "channel": "ORIGINAL",
  "user_received_account": "招商银行信用卡0403",
  "success_time": "2020-12-01T16:18:12+08:00",
  "create_time": "2020-12-01T16:18:12+08:00",
  "status": "SUCCESS",
  "funds_account": "UNSETTLED",
  "amount": {
    "total": 100,
    "refund": 100,
    "from": [
      {
        "account": "AVAILABLE",
        "amount": 444
      }
    ],
    "payer_total": 90,
    "payer_refund": 90,
    "settlement_refund": 100,
    "settlement_total": 100,
    "discount_refund": 10,
    "currency": "CNY"
  },
  "promotion_detail": [
    {
      "promotion_id": "109519",
      "scope": "SINGLE",
      "type": "DISCOUNT",
      "amount": 5,
      "refund_amount": 100,
      "goods_detail": [
 		{
			"merchant_goods_id": "1217752501201407033233368018",
			"wechatpay_goods_id": "1001",
			"goods_name": "iPhone6s 16G",
			"unit_price": 528800,
			"refund_amount": 528800,
			"refund_quantity": 1
       }
		]
    }
  ]
}
~~~



退款功能已初步解决



#### 8）下载账单

日期至少是前一天，当天的会提示日期格式有误。

> 申请交易账单

• 微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致；

• 对账单中涉及金额的字段单位为“元”；

• 对账单接口只能下载三个月以内的账单。

~~~java
适用对象： 直连商户
请求URL：https://api.mch.weixin.qq.com/v3/bill/tradebill
请求方式：GET
    
示例：
    https://api.mch.weixin.qq.com/v3/bill/tradebill?bill_date=2020-12-31&bill_type=ALL
~~~

- 请求参数

| 参数名   | 变量      | 类型[长度限制] | 必填 | 描述                                                         |
| :------- | :-------- | :------------- | :--- | :----------------------------------------------------------- |
| 账单日期 | bill_date | string[1,10]   | 是   | query 格式yyyy-MM-dd 仅支持三个月内的账单下载申请。 示例值：2019-06-11 |
| 账单类型 | bill_type | string[1,32]   | 否   | query 不填则默认是ALL 枚举值： ALL：返回当日所有订单信息（不含充值退款订单） SUCCESS：返回当日成功支付的订单（不含充值退款订单） REFUND：返回当日退款订单（不含充值退款订单） 示例值：ALL |
| 压缩类型 | tar_type  | string[1,32]   | 否   | query 不填则默认是数据流 枚举值： GZIP：返回格式为.gzip的压缩包账单 示例值：GZIP |

- 返回参数

| 参数名       | 变量         | 类型[长度限制] | 必填 | 描述                                                         |
| :----------- | :----------- | :------------- | :--- | :----------------------------------------------------------- |
| 哈希类型     | hash_type    | string[1,32]   | 是   | 枚举值： SHA1：SHA1值 示例值：SHA1                           |
| 哈希值       | hash_value   | string[1,1024] | 是   | 原始账单（gzip需要解压缩）的摘要值，用于校验文件的完整性。 示例值：79bb0f45fc4c42234a918000b2668d689e2bde04 |
| 账单下载地址 | download_url | string[1,2048] | 是   | 供下一步请求账单文件的下载地址，该地址30s内有效。 示例值：https://api.mch.weixin.qq.com/v3/billdownload/file?token=xxx |

~~~java
{
	"download_url": "https://api.mch.weixin.qq.com/v3/billdownload/file?token=6XIv5TUPto7pByrTQKhd6kwvyKLG2uY2wMMR8cNXqaA_Cv_isgaUtBzp4QtiozLO",
	"hash_type": "SHA1",
	"hash_value": "8823044c286bea726f149bfcfce0b0318122d755"
}
~~~



> 申请资金账单

• 资金账单中的数据反映的是商户微信支付账户资金变动情况；

• 对账单中涉及金额的字段单位为“元”。

~~~java
适用对象：直连商户
请求URL：https://api.mch.weixin.qq.com/v3/bill/fundflowbill
请求方式：GET
示例：
    https://api.mch.weixin.qq.com/v3/bill/fundflowbill?bill_date=2020-12-31
~~~

- 请求参数

| 参数名       | 变量         | 类型[长度限制] | 必填 | 描述                                                         |
| :----------- | :----------- | :------------- | :--- | :----------------------------------------------------------- |
| 账单日期     | bill_date    | string[1,10]   | 是   | query 格式yyyy-MM-dd 仅支持三个月内的账单下载申请。 示例值：2019-06-11 |
| 资金账户类型 | account_type | string[1,32]   | 否   | query 不填则默认是BASIC 枚举值： BASIC：基本账户 OPERATION：运营账户 FEES：手续费账户 示例值：BASIC |
| 压缩类型     | tar_type     | string[1,32]   | 否   | query 不填则默认是数据流 枚举值： GZIP：返回格式为.gzip的压缩包账单 示例值：GZIP |

- 返回参数

| 参数名       | 变量         | 类型[长度限制] | 必填 | 描述                                                         |
| :----------- | :----------- | :------------- | :--- | :----------------------------------------------------------- |
| 哈希类型     | hash_type    | string[1,32]   | 是   | 枚举值： SHA1：SHA1值 示例值：SHA1                           |
| 哈希值       | hash_value   | string[1,1024] | 是   | 原始账单（gzip需要解压缩）的摘要值，用于校验文件的完整性。 示例值：79bb0f45fc4c42234a918000b2668d689e2bde04 |
| 账单下载地址 | download_url | string[1,2048] | 是   | 供下一步请求账单文件的下载地址，该地址30s内有效。 示例值：https://api.mch.weixin.qq.com/v3/billdownload/file?token=xxx |

~~~java
{
	"download_url": "https://api.mch.weixin.qq.com/v3/billdownload/file?token=6XIv5TUPto7pByrTQKhd6kwvyKLG2uY2wMMR8cNXqaA_Cv_isgaUtBzp4QtiozLO",
	"hash_type": "SHA1",
	"hash_value": "8823044c286bea726f149bfcfce0b0318122d755"
}
~~~



> 下载账单

• 账单文件的下载地址的有效时间为30s。

• 强烈建议商户将实际账单文件的哈希值和之前从接口获取到的哈希值进行比对，以确认数据的完整性。

• 该接口响应的信息请求头中不包含微信接口响应的签名值，因此需要跳过验签的流程。

•微信在次日9点启动生成前一天的对账单，建议商户10点后再获取。








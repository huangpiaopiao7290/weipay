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

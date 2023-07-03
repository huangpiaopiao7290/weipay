package com.weipay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 引入定时任务
@EnableScheduling
public class WeipayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeipayApplication.class, args);
    }

}

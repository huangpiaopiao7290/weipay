package com.weipay;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class WeipayApplicationTests {



    @Test
    void contextLoads() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusMinutes(5);
        // 定义格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String scheduleTime = dateTimeFormatter.format(before);
        System.out.println(now);
        System.out.println(before);
        System.out.println("schedule_time:" + scheduleTime);
    }

}

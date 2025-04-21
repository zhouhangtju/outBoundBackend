package com.mobile.smartcalling;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({"com.mobile.smartcalling.dao"})
@EnableScheduling
@EnableAsync
@EnableCaching
public class SmartcallingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartcallingApplication.class, args);
    }

}

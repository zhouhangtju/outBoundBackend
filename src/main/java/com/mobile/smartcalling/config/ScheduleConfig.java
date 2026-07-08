package com.mobile.smartcalling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "schedule")
public class ScheduleConfig {

    private boolean enabled = true;

    private String cron = "0 0 19 * * ?"; // 下午7点
}

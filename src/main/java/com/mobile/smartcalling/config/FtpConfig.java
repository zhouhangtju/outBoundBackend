package com.mobile.smartcalling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {

    private String host;
    private int port = 21;
    private String username;
    private String password;
    private String remoteDir;
    private boolean passiveMode = true;
}

package com.mobile.smartcalling;

import com.mobile.smartcalling.config.FtpConfigSatisfaction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;

/**
 * 临时验证测试：yml 中 camelCase 的 ftpSatisfaction 是否能绑定到 prefix=ftp-satisfaction
 */
public class FtpConfigBindingTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testFtpSatisfactionBinding() throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> props = yaml.load(new FileInputStream("src/main/resources/application.yml"));
        Map<String, Object> ftpSat = (Map<String, Object>) props.get("ftpSatisfaction");
        System.out.println("yml 中 ftpSatisfaction 原始配置: " + ftpSat);

        Map<String, Object> flat = new java.util.HashMap<>();
        ftpSat.forEach((k, v) -> flat.put("ftpSatisfaction." + k, v));
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(flat);

        FtpConfigSatisfaction cfg = new Binder(source)
                .bind("ftp-satisfaction", Bindable.of(FtpConfigSatisfaction.class)).get();

        System.out.println("host=" + cfg.getHost());
        System.out.println("port=" + cfg.getPort());
        System.out.println("username=" + cfg.getUsername());
        System.out.println("password=" + cfg.getPassword());
        System.out.println("remoteDir=" + cfg.getRemoteDir());
        System.out.println("passiveMode=" + cfg.isPassiveMode());
        System.out.println("connectTimeout=" + cfg.getConnectTimeout());
        System.out.println("dataTimeout=" + cfg.getDataTimeout());
    }
}

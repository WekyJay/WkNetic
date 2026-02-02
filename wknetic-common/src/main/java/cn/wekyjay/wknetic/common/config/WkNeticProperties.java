package cn.wekyjay.wknetic.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wknetic")
public class WkNeticProperties {
    private Socket socket = new Socket();
    private Boolean dbAutoInit;

    @Data
    public static class Socket {
        private Integer port;
    }
}

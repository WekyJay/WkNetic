package cn.wekyjay.wknetic.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String type;
    private Local local = new Local();
    private Minio minio = new Minio();
    private Imgbed imgbed = new Imgbed();

    @Data
    public static class Local {
        private String basePath;
        private String baseUrl;
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
    }

    @Data
    public static class Imgbed {
        private String type;
        private String apiUrl;
        private String apiKey;
    }
}

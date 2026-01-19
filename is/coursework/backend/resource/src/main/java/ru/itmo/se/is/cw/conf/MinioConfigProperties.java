package ru.itmo.se.is.cw.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioConfigProperties {
    private String url = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
}

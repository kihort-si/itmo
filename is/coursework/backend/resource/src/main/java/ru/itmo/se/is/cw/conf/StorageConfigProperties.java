package ru.itmo.se.is.cw.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageConfigProperties {

    private String fileBucket = "files";
}

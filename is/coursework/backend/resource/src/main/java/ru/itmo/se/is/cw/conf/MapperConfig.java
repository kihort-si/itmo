package ru.itmo.se.is.cw.conf;

import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.mapstruct.MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        typeConversionPolicy = ReportingPolicy.ERROR
)
public class MapperConfig {
}

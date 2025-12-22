package ru.itmo.is.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDto implements Serializable {
    @NotNull
    private Double x;
    private double y;
    private float z;
    @NotNull
    private String name;
}

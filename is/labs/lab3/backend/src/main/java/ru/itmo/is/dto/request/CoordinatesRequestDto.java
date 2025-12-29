package ru.itmo.is.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesRequestDto implements Serializable {
    @Max(347)
    private double x;
    @NotNull
    private Integer y;
}

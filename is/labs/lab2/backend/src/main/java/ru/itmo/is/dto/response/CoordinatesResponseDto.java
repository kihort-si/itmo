package ru.itmo.is.dto.response;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesResponseDto implements Serializable {
    private double x;
    private Integer y;
}
